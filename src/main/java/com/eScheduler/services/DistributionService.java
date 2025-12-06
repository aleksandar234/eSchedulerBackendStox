package com.eScheduler.services;


import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.exceptions.custom.ServerErrorException;
import com.eScheduler.model.Distribution;
import com.eScheduler.model.Subject;
import com.eScheduler.model.Teacher;
import com.eScheduler.model.UserLogin;
import com.eScheduler.repositories.DistributionRepository;
import com.eScheduler.repositories.SubjectRepository;
import com.eScheduler.repositories.TeacherRepository;
import com.eScheduler.repositories.UserLoginRepository;
import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.responses.customDTOClasses.DistributionDTO;
import com.eScheduler.responses.customDTOClasses.StandardUserDTO;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
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

@Service
public class DistributionService {
    private final DistributionRepository distributionRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final UserLoginRepository userLoginRepository;



    @Autowired
    public DistributionService(DistributionRepository distributionRepository,TeacherRepository teacherRepository, SubjectRepository subjectRepository, UserLoginRepository userLoginRepository) {
        this.distributionRepository = distributionRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.userLoginRepository = userLoginRepository;
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
            standardUserDTOS.add(new StandardUserDTO(distribution.getTeacher().getFirstName(),distribution.getTeacher().getLastName(),
                    distribution.getTeacher().getUserLogin().getEmail(),distribution.getSubject().getName(),
                    distribution.getSubject().getStudyProgram(), distribution.getSubject().getSemester(),
                    distribution.getSubject().getLectureHours(), distribution.getSubject().getExerciseHours(),
                    distribution.getClassType(), distribution.getSessionCount()));
        });
        return standardUserDTOS;
    }

    public DistributionDTO addNewDistribution(DistributionRequestDTO distribution,String studyProgram, String semester){
        Subject subject = distributionRepository.findBySubjectNameStudyProgramSemester(distribution.getSubject(),studyProgram,semester);
        List<Distribution> distributionsWithSameSubject = distributionRepository.findBySubject(subject,distribution.getClassType());

        Teacher teacher = distributionRepository.findByTeacherEmail(distribution.getTeacher());
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
        Distribution newDistribution = new Distribution(0L,teacher,subject,distribution.getClassType(),distribution.getSessionCount());

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

        Distribution newDistribution = new Distribution(distribution.getId(),teacher,subject,distribution.getClassType(),distribution.getSessionCount());
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
        return addNewDistribution(dto, dto.getSubject(), dto.getClassType());
    }
}
