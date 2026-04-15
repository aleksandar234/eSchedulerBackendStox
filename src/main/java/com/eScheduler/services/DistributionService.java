package com.eScheduler.services;


import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.exceptions.custom.ServerErrorException;
import com.eScheduler.model.*;
import com.eScheduler.repositories.*;
import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.responses.customDTOClasses.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DistributionService {
    private final DistributionRepository distributionRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final UserLoginRepository userLoginRepository;
    private final SchoolYearService schoolYearService;
    private final SchoolYearRepository schoolYearRepository;



    @Autowired
    public DistributionService(DistributionRepository distributionRepository, TeacherRepository teacherRepository, SubjectRepository subjectRepository, UserLoginRepository userLoginRepository, SchoolYearService schoolYearService, SchoolYearRepository schoolYearRepository) {
        this.distributionRepository = distributionRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.userLoginRepository = userLoginRepository;
        this.schoolYearService = schoolYearService;
        this.schoolYearRepository = schoolYearRepository;
    }

    public List<DistributionDTO> getAllDistributions() {
         List<Distribution> distributions = distributionRepository.findAll();
         List<DistributionDTO> distributionDTOS = new ArrayList<>();
        distributions.forEach(distribution -> {
            distributionDTOS.add(mapToDistributionDTO(distribution));
        });
        return distributionDTOS;
    }

    public List<StandardUserDTO> getDistributionByTeacher(String email) {
        List<Distribution> distributions = distributionRepository.getDistributionByTeacherEmail(email);
        List<StandardUserDTO> standardUserDTOS = new ArrayList<>();
        distributions.forEach(distribution -> {
            standardUserDTOS.add(new StandardUserDTO(distribution.getTeacher().getId(), distribution.getTeacher().getFirstName(),distribution.getTeacher().getLastName(),
                    distribution.getTeacher().getUserLogin().getEmail(),distribution.getSubject().getName(),
                    distribution.getSubject().getStudyProgram(), distribution.getSubject().getSemester(),
                    distribution.getSubject().getLectureHours(), distribution.getSubject().getExerciseHours(),
                    distribution.getClassType(), distribution.getSessionCount()));
        });
        return standardUserDTOS;
    }

    public DistributionDTO addNewDistribution(DistributionRequestDTO distribution,String studyProgram, Integer semester){
        System.out.println("Distribucija DTO subject: " + distribution.getSubject());
        System.out.println("Study program: " + studyProgram);
        System.out.println("Semester: " + semester);
        Subject subject = subjectRepository.findBySubjectNameStudyProgramSemester(
                distribution.getSubject(), studyProgram, semester);

        System.out.println("Pronadjeni predmet: " + (subject != null ? subject.getName() : "null"));
        List<Distribution> distributionsWithSameSubject = distributionRepository.findBySubject(subject,distribution.getClassType());

        Teacher teacher = distributionRepository.findByTeacherEmail(distribution.getTeacher());
        System.out.println("Pronadjeni profesor: " + (teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "null"));
        if (subject == null || teacher == null) {
            throw new ConflictException("Raspodela sa tim predmetom ili nastavnikom ne postoji");
        }else{
            AtomicInteger sum = new AtomicInteger();
            distributionsWithSameSubject.forEach(tmp -> {
                sum.addAndGet(tmp.getSessionCount());
            });
            sum.addAndGet(distribution.getSessionCount());

            if(Objects.equals(distribution.getClassType(), "vezbe") && sum.get() > subject.getExerciseSessions() ||
                    Objects.equals(distribution.getClassType(), "predavanja") && sum.get() > subject.getLectureSessions()){
                throw new ConflictException("Prekoracen broj casova za ovaj tip nastave");
            }
        }

        SchoolYear activeSchoolYear = schoolYearService.getActiveSchoolYears();

        Distribution newDistribution = new Distribution(0L,teacher,subject,distribution.getClassType(),distribution.getSessionCount(), activeSchoolYear);

        distributionRepository.save(newDistribution);
        return mapToDistributionDTO(newDistribution);

    }

    public void deleteDistributionById(Long id){
        distributionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Raspodela nije pronadjena"));
        distributionRepository.deleteById(id);
    }

    @Transactional
    public DistributionDTO updateDistribution(DistributionRequestDTO distribution){
        Distribution oldDistribution = distributionRepository.findById(distribution.getId())
                .orElseThrow(() -> new NotFoundException("Raspodela sa tim Id ne postoji"));

//        Subject subject = distributionRepository.findBySubjectName(distribution.getSubject());
        Subject subject = oldDistribution.getSubject();
        List<Distribution> distributionsWithSameSubject = distributionRepository.findBySubject(subject,distribution.getClassType());

        Teacher teacher = distributionRepository.findByTeacherEmail(distribution.getTeacher());

        if (subject == null || teacher == null) {
            throw new ConflictException("Raspodela sa tim predmetom ili nastavnikom ne postoji");
        }else{
            AtomicInteger sum = new AtomicInteger();
            distributionsWithSameSubject.forEach(tmp -> {
                if(!Objects.equals(tmp.getId(), distribution.getId())){
                    sum.addAndGet(tmp.getSessionCount());
                }
            });
            sum.addAndGet(distribution.getSessionCount());

            if(Objects.equals(distribution.getClassType(), "vezbe") && sum.get() > subject.getExerciseSessions() ||
               Objects.equals(distribution.getClassType(), "predavanja") && sum.get() > subject.getLectureSessions()){
                throw new ConflictException("Prekoracen broj casova za ovaj tip nastave");
            }
        }

        SchoolYear activeSchoolYear = schoolYearService.getActiveSchoolYears();

        Distribution newDistribution = new Distribution(distribution.getId(),teacher,subject,distribution.getClassType(),distribution.getSessionCount(), activeSchoolYear);
        for (Field field : Distribution.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object newValue = field.get(newDistribution);
                Object oldValue = field.get(oldDistribution);

                if (newValue != null && !newValue.equals(oldValue)) {
                    field.set(oldDistribution, newValue);
                }
            } catch (Exception e) {
                throw new ServerErrorException("Greska prilikom ažuriranja raspodele");
            }
        }
        return mapToDistributionDTO(newDistribution);
    }

    @Transactional
    public void importDistributionsFromJson(List<Map<String, Object>> jsonData) {

        // === DROP ALL EXISTING DATA ===
        distributionRepository.deleteAll();
        subjectRepository.deleteAll();
        teacherRepository.deleteAll();
        userLoginRepository.deleteAll();

        for (Map<String, Object> item : jsonData) {
            // === Teacher ===
            Map<String, Object> teacherMap = (Map<String, Object>) item.get("teacher");
            String email = (String) teacherMap.get("email");

            UserLogin userLogin = userLoginRepository.findByEmail(email)
                    .orElseGet(() -> {
                        UserLogin newLogin = new UserLogin();
                        newLogin.setEmail(email);
                        newLogin.setPassword("default"); // postavi default lozinku
                        newLogin.setAdmin((Boolean) teacherMap.get("admin"));
                        return userLoginRepository.save(newLogin);
                    });

            Teacher teacher = teacherRepository.findByUserLoginEmail(email)
                    .orElseGet(() -> {
                        Teacher newTeacher = new Teacher();
                        newTeacher.setFirstName((String) teacherMap.get("firstName"));
                        newTeacher.setLastName((String) teacherMap.get("lastName"));
                        newTeacher.setTitle((String) teacherMap.get("title"));
                        newTeacher.setUserLogin(userLogin);
                        return teacherRepository.save(newTeacher);
                    });

            // === Subject ===
            Map<String, Object> subjectMap = (Map<String, Object>) item.get("subject");
            String subjectName = (String) subjectMap.get("name");
            String program = (String) subjectMap.get("studyProgram");
            Integer semester = (Integer) subjectMap.get("semester");

            Subject subject = subjectRepository.findByNameAndStudyProgramAndSemester(subjectName, program, semester)
                    .orElseGet(() -> {
                        Subject newSubject = new Subject();
                        newSubject.setName(subjectName);
                        newSubject.setStudyProgram(program);
                        newSubject.setSemester(semester);
                        newSubject.setLectureHours((Integer) subjectMap.get("lectureHours"));
                        newSubject.setExerciseHours((Integer) subjectMap.get("exerciseHours"));
                        newSubject.setPracticumHours(subjectMap.get("practicumHours") == null ? null : (Integer) subjectMap.get("practicumHours"));
                        newSubject.setMandatory((String) subjectMap.get("mandatory"));
                        newSubject.setLectureSessions((Integer) subjectMap.get("lectureSessions"));
                        newSubject.setExerciseSessions((Integer) subjectMap.get("exerciseSessions"));
                        return subjectRepository.save(newSubject);
                    });

            // === Distribution ===
            Distribution distribution = new Distribution();
            distribution.setTeacher(teacher);
            distribution.setSubject(subject);
            distribution.setClassType((String) item.get("classType"));
            distribution.setSessionCount((Integer) item.get("sessionCount"));
            distributionRepository.save(distribution);
        }
    }

    public DistributionDTO mapToDistributionDTO(Distribution distribution){
        TeacherDTO teacherDTO = new TeacherDTO(distribution.getTeacher().getId(),distribution.getTeacher().getUserLogin().getEmail(), distribution.getTeacher().getFirstName(), distribution.getTeacher().getLastName(), distribution.getTeacher().getTitle(), distribution.getTeacher().getUserLogin().isAdmin());
        SubjectDTO subjectDTO = new SubjectDTO(distribution.getSubject().getId(), distribution.getSubject().getName(), distribution.getSubject().getStudyProgram(), distribution.getSubject().getSemester(),distribution.getSubject().getLectureHours(), distribution.getSubject().getExerciseHours(),distribution.getSubject().getPracticumHours(), distribution.getSubject().getMandatory(),distribution.getSubject().getLectureSessions(),distribution.getSubject().getExerciseSessions());
        return new DistributionDTO(distribution.getId(),teacherDTO,subjectDTO, distribution.getClassType(),distribution.getSessionCount());
    }

    public DistributionDTO addNewDistribution(DistributionRequestDTO dto) {
        return addNewDistribution(dto, dto.getStudyProgram(), dto.getSemestar());
    }


    public List<DistributionDTO> getBySchoolYear(Long schoolYear) {
        List<Distribution> distributions = distributionRepository.findBySchoolYearId(schoolYear);
        List<DistributionDTO> distributionDTOS = new ArrayList<>();
        distributions.forEach(distribution -> {
            distributionDTOS.add(mapToDistributionDTO(distribution));
        });
        return distributionDTOS;
    }

    @Transactional
    public void copySubjects(Long sourceYearId, Long targetYearId) {
        SchoolYear targetYear = schoolYearService.getSchoolYearById(targetYearId);

        List<Subject> sourceSubjects = subjectRepository.findBySchoolYearId(sourceYearId);

        for(Subject s: sourceSubjects) {
            boolean exists = subjectRepository.existsByNameAndStudyProgramAndSchoolYearId(s.getName(), s.getStudyProgram(), targetYearId);

            if(!exists) {
                Subject copy = new Subject();
                copy.setName(s.getName());
                copy.setStudyProgram(s.getStudyProgram());
                copy.setSemester(s.getSemester());
                copy.setLectureHours(s.getLectureHours());
                copy.setExerciseHours(s.getExerciseHours());
                copy.setPracticumHours(s.getPracticumHours());
                copy.setMandatory(s.getMandatory());
                copy.setLectureSessions(s.getLectureSessions());
                copy.setExerciseSessions(s.getExerciseSessions());
                copy.setSchoolYear(targetYear);

                subjectRepository.save(copy);
            }

        }

    }

    @Transactional
    public void copyTeachers(Long sourceYearId, Long targetYearId) {
        SchoolYear targetYear = schoolYearService.getSchoolYearById(targetYearId);

        List<Teacher> sourceTeachers =
                teacherRepository.findBySchoolYearId(sourceYearId);

        for (Teacher t : sourceTeachers) {

            boolean exists = teacherRepository
                    .existsByUserLoginEmailAndSchoolYearId(
                            t.getUserLogin().getEmail(),
                            targetYearId
                    );

            if (!exists) {
                Teacher copy = new Teacher();
                copy.setFirstName(t.getFirstName());
                copy.setLastName(t.getLastName());
                copy.setTitle(t.getTitle());
                copy.setUserLogin(t.getUserLogin()); // isti login
                copy.setSchoolYear(targetYear);

                teacherRepository.save(copy);
            }
        }
    }


    @Transactional
    public void copyDistributionsToYear(Long sourceYearId, Long targetYearId) {
        SchoolYear targetYear = schoolYearService.getSchoolYearById(targetYearId);
        List<Distribution> sourceDistributions = distributionRepository.findBySchoolYearId(sourceYearId);

        for (Distribution dist : sourceDistributions) {

            // --- Profesor ---
            Teacher newTeacher = teacherRepository.findByEmailAndYear(
                    dist.getTeacher().getUserLogin().getEmail(), targetYearId
            ).orElseGet(() -> {
                Teacher teacherCopy = new Teacher();
                teacherCopy.setFirstName(dist.getTeacher().getFirstName());
                teacherCopy.setLastName(dist.getTeacher().getLastName());
                teacherCopy.setTitle(dist.getTeacher().getTitle());
                teacherCopy.setUserLogin(dist.getTeacher().getUserLogin());
                teacherCopy.setSchoolYear(targetYear); // obavezno setovati školsku godinu
                return teacherRepository.save(teacherCopy);
            });

            // --- Predmet ---
            Subject newSubject = subjectRepository.findByNameAndStudyProgramAndSemesterAndSchoolYearId(
                    dist.getSubject().getName(), dist.getSubject().getStudyProgram(),dist.getSubject().getSemester(), targetYearId
            ).orElseGet(() -> {
                Subject subjectCopy = new Subject();
                subjectCopy.setName(dist.getSubject().getName());
                subjectCopy.setStudyProgram(dist.getSubject().getStudyProgram());
                subjectCopy.setSemester(dist.getSubject().getSemester());
                subjectCopy.setLectureHours(dist.getSubject().getLectureHours());
                subjectCopy.setExerciseHours(dist.getSubject().getExerciseHours());
                subjectCopy.setPracticumHours(dist.getSubject().getPracticumHours());
                subjectCopy.setMandatory(dist.getSubject().getMandatory());
                subjectCopy.setLectureSessions(dist.getSubject().getLectureSessions());
                subjectCopy.setExerciseSessions(dist.getSubject().getExerciseSessions());
                subjectCopy.setSchoolYear(targetYear); // vežemo za ciljnu godinu
                return subjectRepository.save(subjectCopy);
            });

            // --- Kreiranje nove raspodele ---
//            boolean exists = distributionRepository.existsByTeacherIdAndSubjectIdAndSchoolYearAndClassType(newTeacher.getId(), newSubject.getId(), targetYear, dist.getClassType());
//            if (!exists) {
            Distribution copy = new Distribution();
            copy.setTeacher(newTeacher);
            copy.setSubject(newSubject);
            copy.setClassType(dist.getClassType());
            copy.setSessionCount(dist.getSessionCount());
            copy.setSchoolYear(targetYear);
            distributionRepository.save(copy);
//            } else {
//                System.out.println("Treba ovde da udjem 2 puta");
//            }
        }
    }

//    @Transactional
//    public void copyDistributionsToYear(Long sourceYearId, Long targetYearId) {
//        SchoolYear targetYear = schoolYearService.getSchoolYearById(targetYearId);
//        List<Distribution> sourceDistributions = distributionRepository.findBySchoolYearId(sourceYearId);
//
//        // Napravi mapu profesora i predmeta koji su već kopirani
//        Map<String, Teacher> teachersMap = teacherRepository.findBySchoolYearId(targetYearId)
//                .stream()
//                .collect(Collectors.toMap(t -> t.getUserLogin().getEmail(), t -> t));
//
//        Map<String, Subject> subjectsMap = subjectRepository.findBySchoolYearId(targetYearId)
//                .stream()
//                .collect(Collectors.toMap(s -> s.getName() + "|" + s.getStudyProgram(), s -> s));
//
//        for (Distribution dist : sourceDistributions) {
//            // Profesor
//            Teacher targetTeacher = teachersMap.computeIfAbsent(
//                    dist.getTeacher().getUserLogin().getEmail(),
//                    email -> {
//                        Teacher copy = new Teacher();
//                        copy.setFirstName(dist.getTeacher().getFirstName());
//                        copy.setLastName(dist.getTeacher().getLastName());
//                        copy.setTitle(dist.getTeacher().getTitle());
//                        copy.setUserLogin(dist.getTeacher().getUserLogin());
//                        copy.setSchoolYear(targetYear);
//                        return teacherRepository.save(copy);
//                    });
//
//            // Predmet
//            String subjectKey = dist.getSubject().getName() + "|" + dist.getSubject().getStudyProgram();
//            Subject targetSubject = subjectsMap.computeIfAbsent(
//                    subjectKey,
//                    key -> {
//                        Subject copy = new Subject();
//                        copy.setName(dist.getSubject().getName());
//                        copy.setStudyProgram(dist.getSubject().getStudyProgram());
//                        copy.setSemester(dist.getSubject().getSemester());
//                        copy.setLectureHours(dist.getSubject().getLectureHours());
//                        copy.setExerciseHours(dist.getSubject().getExerciseHours());
//                        copy.setPracticumHours(dist.getSubject().getPracticumHours());
//                        copy.setMandatory(dist.getSubject().getMandatory());
//                        copy.setLectureSessions(dist.getSubject().getLectureSessions());
//                        copy.setExerciseSessions(dist.getSubject().getExerciseSessions());
//                        copy.setSchoolYear(targetYear);
//                        return subjectRepository.save(copy);
//                    });
//
//            // Kreiranje nove raspodele
//            Distribution copy = new Distribution();
//            copy.setTeacher(targetTeacher);
//            copy.setSubject(targetSubject);
//            copy.setClassType(dist.getClassType());
//            copy.setSessionCount(dist.getSessionCount());
//            copy.setSchoolYear(targetYear);
//
//            distributionRepository.save(copy);
//        }
//    }




    @Transactional
    public void copyDistributions(Long sourceYearId, Long targetYearId) {
        SchoolYear targetYear = schoolYearService.getSchoolYearById(targetYearId);

        List<Distribution> sourceDistributions =
                distributionRepository.findBySchoolYearId(sourceYearId);

        for (Distribution d : sourceDistributions) {

            Teacher targetTeacher =
                    teacherRepository.findByUserLoginEmailAndSchoolYearId(
                            d.getTeacher().getUserLogin().getEmail(),
                            targetYearId
                    ).orElseThrow(() ->
                            new RuntimeException("Profesor ne postoji u ciljnoj godini"));

            Subject targetSubject =
                    subjectRepository.findByNameAndStudyProgramAndSchoolYearId(
                            d.getSubject().getName(),
                            d.getSubject().getStudyProgram(),
                            targetYearId
                    ).orElseThrow(() ->
                            new RuntimeException("Predmet ne postoji u ciljnoj godini"));

            Distribution copy = new Distribution();
            copy.setTeacher(targetTeacher);
            copy.setSubject(targetSubject);
            copy.setClassType(d.getClassType());
            copy.setSessionCount(d.getSessionCount());
            copy.setSchoolYear(targetYear);

            distributionRepository.save(copy);
        }
    }


    @Transactional
    public void copyWholeSchoolYear(Long sourceYearId, Long targetYearId) {
        copySubjects(sourceYearId, targetYearId);
        copyTeachers(sourceYearId, targetYearId);
        copyDistributions(sourceYearId, targetYearId);
    }


}
