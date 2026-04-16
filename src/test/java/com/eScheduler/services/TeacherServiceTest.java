package com.eScheduler.services;

import com.eScheduler.TestDataProvider;
import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.Teacher;
import com.eScheduler.model.UserLogin;
import com.eScheduler.repositories.TeacherRepository;
import com.eScheduler.repositories.UserLoginRepository;
import com.eScheduler.requests.TeacherRequestDTO;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserLoginRepository userLoginRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTeachers_returnsListOfTeacherDTOs() {
        Teacher teacher1 = TestDataProvider.createTeacher1(); // mora da sadrži userLogin.email
        Teacher teacher2 = TestDataProvider.createTeacher2();
        List<Teacher> teachers = List.of(teacher1, teacher2);

        when(teacherRepository.findAll()).thenReturn(teachers);

        List<TeacherDTO> result = teacherService.getTeachers();

        assertEquals(2, result.size());
        assertEquals("Marko", result.get(0).getFirstName());
        assertEquals(teacher1.getUserLogin().getEmail(), result.get(0).getEmail());
    }

    @Test
    void addNewTeacher_savesAndReturnsTeacherDTO() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                null, "mMarkovic@example.com", "Marko", "Markovic", "Profesor", true
        );

        UserLogin userLogin = new UserLogin();
        userLogin.setEmail(request.getEmail());
        userLogin.setPassword("encodedPassword");

        Teacher newTeacher = new Teacher();
        newTeacher.setId(1L);
        newTeacher.setFirstName(request.getFirstName());
        newTeacher.setLastName(request.getLastName());
        newTeacher.setTitle(request.getTitle());
        newTeacher.setUserLogin(userLogin);

        when(teacherRepository.findByUserLoginEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userLoginRepository.save(any(UserLogin.class))).thenReturn(userLogin);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(newTeacher);

        TeacherDTO result = teacherService.addNewTeacher(request);

        assertEquals("Marko", result.getFirstName());
        assertEquals("mMarkovic@example.com", result.getEmail());
    }

    @Test
    void addNewTeacher_throwsConflictException_whenTeacherExists() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                null, "mMarkovic@example.com", "Marko", "Markovic", "Profesor", true
        );

        UserLogin userLogin = new UserLogin();
        userLogin.setEmail(request.getEmail());

        when(teacherRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(userLogin));

        assertThrows(ConflictException.class, () -> teacherService.addNewTeacher(request));
    }

    @Test
    void deleteTeacherById_deletesTeacher() {
        Teacher teacher = TestDataProvider.createTeacher1();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        teacherService.deleteTeacherById(1L);

        verify(userLoginRepository, times(1)).deleteById(teacher.getUserLogin().getId());
        verify(teacherRepository, times(1)).deleteById(teacher.getId());
    }

    @Test
    void deleteTeacherById_throwsNotFoundException_whenTeacherNotFound() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> teacherService.deleteTeacherById(1L));
    }

    @Test
    void updateTeacher_updatesAndReturnsTeacherDTO() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                1L, "mMarkovic@example.com", "Marko", "Markovic", "Profesor", true
        );

        Teacher oldTeacher = TestDataProvider.createTeacher1();
        when(teacherRepository.findById(request.getId())).thenReturn(Optional.of(oldTeacher));

        TeacherDTO result = teacherService.updateTeacher(request);

        assertEquals("Marko", result.getFirstName());
        assertEquals("mMarkovic@example.com", result.getEmail());
    }

    @Test
    void updateTeacher_throwsNotFoundException_whenTeacherNotFound() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                1L, "mMarkovic@example.com", "Marko", "Markovic", "Profesor", true
        );
        when(teacherRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> teacherService.updateTeacher(request));
    }

    @Test
    void updateTeacher_updatesUserLoginEmailAndAdminFlag() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                1L,
                "novi.email@example.com",
                "Marko",
                "Markovic",
                "Profesor",
                true
        );

        UserLogin userLogin = new UserLogin();
        userLogin.setId(1L);
        userLogin.setEmail("stari.email@example.com");
        userLogin.setAdmin(false);

        Teacher oldTeacher = new Teacher();
        oldTeacher.setId(1L);
        oldTeacher.setFirstName("Marko");
        oldTeacher.setLastName("Markovic");
        oldTeacher.setTitle("Asistent");
        oldTeacher.setUserLogin(userLogin);

        when(teacherRepository.findById(request.getId()))
                .thenReturn(Optional.of(oldTeacher));

        TeacherDTO result = teacherService.updateTeacher(request);

        assertEquals("novi.email@example.com", oldTeacher.getUserLogin().getEmail());
        assertTrue(oldTeacher.getUserLogin().isAdmin());
        assertEquals("novi.email@example.com", result.getEmail());
        assertEquals("Profesor", result.getTitle());
    }

    @Test
    void updateTeacher_handlesNullUserLogin() {
        TeacherRequestDTO request = new TeacherRequestDTO(
                1L,
                "mMarkovic@example.com",
                "Marko",
                "Markovic",
                "Profesor",
                true
        );

        Teacher oldTeacher = new Teacher();
        oldTeacher.setId(1L);
        oldTeacher.setFirstName("StaroIme");
        oldTeacher.setLastName("StaroPrezime");
        oldTeacher.setTitle("Asistent");
        oldTeacher.setUserLogin(null);

        when(teacherRepository.findById(request.getId()))
                .thenReturn(Optional.of(oldTeacher));

        TeacherDTO result = teacherService.updateTeacher(request);

        assertEquals("Marko", result.getFirstName());
        assertEquals("Markovic", result.getLastName());
        assertEquals("Profesor", result.getTitle());
    }
}