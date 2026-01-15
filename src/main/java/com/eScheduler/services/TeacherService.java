package com.eScheduler.services;

import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.exceptions.custom.ServerErrorException;
import com.eScheduler.model.Teacher;
import com.eScheduler.model.UserLogin;
import com.eScheduler.repositories.TeacherRepository;
import com.eScheduler.repositories.UserLoginRepository;
import com.eScheduler.requests.TeacherRequestDTO;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final UserLoginRepository userLoginRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public TeacherService(TeacherRepository teacherRepository, UserLoginRepository userLoginRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userLoginRepository = userLoginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TeacherDTO> getTeachers() {
      List<Teacher> teachers = teacherRepository.findAll();
      List<TeacherDTO> teacherDTOS = new ArrayList<>();
        teachers.forEach(teacher -> {
            teacherDTOS.add(mapToTeacherDTO(teacher));
        });
        return teacherDTOS;
    }

    public List<TeacherDTO> getProfessors() {
        List<Teacher> teachers = teacherRepository.findByType("nastavnik");
        List<TeacherDTO> teacherDTOS = new ArrayList<>();
        teachers.forEach(teacher -> {
            teacherDTOS.add(mapToTeacherDTO(teacher));
        });
        return teacherDTOS;

    }

    public List<TeacherDTO> getAssistants() {
        List<Teacher> assistants = teacherRepository.findByType("saradnik");
        List<TeacherDTO> teacherDTOS = new ArrayList<>();
        assistants.forEach(assistant -> {
            teacherDTOS.add(mapToTeacherDTO(assistant));
        });
        return teacherDTOS;
    }

    public TeacherDTO addNewTeacher(TeacherRequestDTO teacher) {
        if (teacherRepository.findByEmail(teacher.getEmail()).isPresent()) {
             throw new ConflictException("Profesor sa tim mailom već postoji");
        }else{
            Teacher newTeacher = new Teacher();
            newTeacher.setFirstName(teacher.getFirstName());
            newTeacher.setLastName(teacher.getLastName());
            newTeacher.setTitle(teacher.getTitle());


            UserLogin userLogin = new UserLogin();
            userLogin.setEmail(teacher.getEmail());
//            String hashedPassword = passwordEncoder.encode("password");
//            userLogin.setPassword(hashedPassword);
            userLogin.setPassword("password");
            userLogin.setAdmin(teacher.isAdmin());
            userLoginRepository.save(userLogin);

            newTeacher.setUserLogin(userLogin);
            teacherRepository.save(newTeacher);
            teacher.setId(newTeacher.getId());
            return mapRequestTeacherDTOToTeacherDTO(teacher);
        }
    }

    public void deleteTeacherById(Long id){
        Teacher teacher =teacherRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Profesor nije pronađen"));
        userLoginRepository.deleteById(teacher.getUserLogin().getId());
        if (teacherRepository.findById(id).isPresent()) {
            teacherRepository.deleteById(id);
        }
    }

    @Transactional
    public TeacherDTO updateTeacher(TeacherRequestDTO teacher) {
        Teacher oldTeacher = teacherRepository.findById(teacher.getId())
                .orElseThrow(()->new NotFoundException("Profesor nije pronađen"));



        oldTeacher.setFirstName(teacher.getFirstName());
        oldTeacher.setLastName(teacher.getLastName());
        oldTeacher.setTitle(teacher.getTitle());

        UserLogin user =oldTeacher.getUserLogin();
        if(user != null){
            user.setEmail(teacher.getEmail());
            user.setAdmin(teacher.isAdmin());
            userLoginRepository.save(user);
            oldTeacher.setUserLogin(user);
        }

        return mapRequestTeacherDTOToTeacherDTO(teacher);

    }
    public TeacherDTO mapToTeacherDTO(Teacher teacher){
        return new TeacherDTO(teacher.getId(), teacher.getUserLogin().getEmail(), teacher.getFirstName(), teacher.getLastName(), teacher.getTitle(), teacher.getUserLogin().isAdmin());
    }
    public TeacherDTO mapRequestTeacherDTOToTeacherDTO(TeacherRequestDTO teacher){
        return new TeacherDTO(teacher.getId(), teacher.getEmail(), teacher.getFirstName(), teacher.getLastName(), teacher.getTitle(), teacher.isAdmin());
    }

    // Dohvatanje svih profesora po školskoj godini
    public List<TeacherDTO> getTeachersBySchoolYear(Long schoolYearId) {
        List<Teacher> teachers = teacherRepository.findBySchoolYearId(schoolYearId);
        return teachers.stream()
                .map(t -> new TeacherDTO(
                        t.getId(),
                        t.getUserLogin().getEmail(),
                        t.getFirstName(),
                        t.getLastName(),
                        t.getTitle(),
                        t.getUserLogin().isAdmin()
                ))
                .collect(Collectors.toList());
    }

    // Dohvatanje profesora po imenu i školskoj godini
    public Optional<Teacher> getTeacherByNameAndYear(String firstName, Long schoolYearId) {
        return teacherRepository.findByNameAndYear(firstName, schoolYearId);
    }

    // Dohvatanje profesora koji pripadaju aktivnoj školskoj godini
    public List<Teacher> getTeachersForActiveYear() {
        return teacherRepository.findAllByActiveSchoolYear();
    }

}
