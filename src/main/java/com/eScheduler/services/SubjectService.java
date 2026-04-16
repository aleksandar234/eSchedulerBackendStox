package com.eScheduler.services;

import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.exceptions.custom.ServerErrorException;
import com.eScheduler.model.Subject;
import com.eScheduler.repositories.SubjectRepository;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectDTO> getSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectDTO> subjectDTOS = new ArrayList<>();
        subjects.forEach(subject -> {
            subjectDTOS.add(mapToSubjectDTO(subject));
        });
        return subjectDTOS;
    }

    public SubjectDTO addNewSubject(Subject subject) {
        if (subjectRepository.findByName(subject.getName()).isEmpty()) {
            Subject savedSubject = subjectRepository.save(subject);
            return mapToSubjectDTO(savedSubject);
        } else {
            throw new ConflictException("Predmet sa tim imenom vec postoji");
        }
    }

//    public SubjectDTO addNewSubject(Subject subject){
//        if(subjectRepository.findByName(subject.getName()).isEmpty()){
//            subjectRepository.save(subject);
//            return mapToSubjectDTO(subject);
//        }else{
//            throw new ConflictException("Predmet sa tim imenom vec postoji");
//        }
//    }

    public void deleteSubjectById(Long id){
        subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Predmet nije pronadjen"));
        subjectRepository.deleteById(id);
    }

    @Transactional
    public SubjectDTO updateSubject(Subject subject) {
        Subject oldSubject = subjectRepository.findById(subject.getId())
                .orElseThrow(() -> new NotFoundException("Predmet nije pronadjen"));

        for (Field field : Subject.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object newValue = field.get(subject);
                Object oldValue = field.get(oldSubject);

                if (newValue != null && !newValue.equals(oldValue)) {
                    field.set(oldSubject, newValue);
                }
            } catch (Exception e) {
                throw new ServerErrorException("Greska prilikom azuriranja predmeta");
            }
        }
        return mapToSubjectDTO(oldSubject);
    }

    public SubjectDTO mapToSubjectDTO(Subject subject){
    return new SubjectDTO(subject.getId(), subject.getName(),
            subject.getStudyProgram(), subject.getSemester(), subject.getLectureHours(),
            subject.getExerciseHours(), subject.getPracticumHours(), subject.getMandatory(),
            subject.getLectureSessions(), subject.getExerciseSessions());
    }


    public List<SubjectDTO> findBySchoolYearId(Long id) {
        List<Subject> subjects = subjectRepository.findBySchoolYearId(id);
        return subjects.stream()
                .map(s -> new SubjectDTO(
                        s.getId(),
                        s.getName(),
                        s.getStudyProgram(),
                        s.getSemester(),
                        s.getLectureHours(),
                        s.getExerciseHours(),
                        s.getPracticumHours(),
                        s.getMandatory(),
                        s.getLectureSessions(),
                        s.getExerciseSessions()
                ))
                .collect(Collectors.toList());
    }
}
