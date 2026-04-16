package com.eScheduler.services;

import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.*;
import com.eScheduler.repositories.*;
import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.responses.customDTOClasses.DistributionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DistributionServiceTest {

    @Mock
    private DistributionRepository distributionRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserLoginRepository userLoginRepository;

    @Mock
    private SchoolYearService schoolYearService;

    @Mock
    private SchoolYearRepository schoolYearRepository;

    @InjectMocks
    private DistributionService distributionService;

    private SchoolYear activeYear;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activeYear = new SchoolYear();
        activeYear.setId(1L);
        when(schoolYearService.getActiveSchoolYears()).thenReturn(activeYear);
    }

    @Test
    void getAllDistributions_returnsListOfDistributionDTOs() {

        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setUserLogin(userLogin);
        Subject subject = new Subject();
        subject.setId(1L);

        Distribution dist1 = new Distribution(1L, teacher, subject, "predavanja", 2, activeYear);
        Distribution dist2 = new Distribution(2L, teacher, subject, "vezbe", 1, activeYear);

        when(distributionRepository.findAll()).thenReturn(List.of(dist1, dist2));

        List<DistributionDTO> result = distributionService.getAllDistributions();

        assertEquals(2, result.size());
        assertEquals("predavanja", result.get(0).getClassType());
        assertEquals("vezbe", result.get(1).getClassType());
    }

    @Test
    void addNewDistribution_savesAndReturnsDistributionDTO() {
        // --- USER LOGIN ---
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        // --- TEACHER ---
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUserLogin(userLogin);

        // --- SUBJECT ---
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Baze podataka");
        subject.setLectureSessions(4);
        subject.setExerciseSessions(4);

        // --- REQUEST DTO ---
        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,
                "teacher@example.com",
                "Baze podataka",
                "predavanja",
                2,
                "Informatika",
                1
        );

        // --- ENTITY KOJI CE SAVE VRATITI ---
        Distribution savedDistribution = new Distribution();
        savedDistribution.setId(1L);
        savedDistribution.setTeacher(teacher);
        savedDistribution.setSubject(subject);
//        savedDistribution.setSessionType("predavanja");
        savedDistribution.setSessionCount(2);
//        savedDistribution.set("Informatika");
//        savedDistribution.setS(1);

        // --- MOCKOVI ---
        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                request.getStudyProgram(),
                request.getSemestar()
        )).thenReturn(subject);

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);

        when(distributionRepository.findBySubject(subject, request.getClassType()))
                .thenReturn(List.of());

        when(distributionRepository.save(any(Distribution.class)))
                .thenReturn(savedDistribution);

        // --- POZIV ---
        DistributionDTO result = distributionService.addNewDistribution(
                request,
                request.getStudyProgram(),
                request.getSemestar()
        );

        // --- PROVERE ---
        assertEquals("predavanja", result.getClassType());
        assertEquals(2, result.getSessionCount());
        assertEquals("teacher@example.com", result.getTeacher().getEmail());
        assertEquals("Baze podataka", result.getSubject().getName());
    }

    @Test
    void addNewDistribution_throwsConflictException_whenDistributionExceedsSessions() {
        Teacher teacher = new Teacher();
        Subject subject = new Subject();
        subject.setLectureSessions(3); // max predavanja

        DistributionRequestDTO request = new DistributionRequestDTO(
                0L,                     // id
                "teacher@example.com",   // teacher email
                "Matematika",            // subject
                "predavanja",            // classType
                2,                       // sessionCount
                "ETF",                   // studyProgram (primer)
                3                        // semester (primer)
        );

        Distribution existing = new Distribution(1L, teacher, subject, "predavanja", 2, activeYear);

        when(distributionRepository.findBySubjectNameStudyProgramSemester(anyString(), anyString(), anyInt()))
                .thenReturn(subject);
        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);
        when(distributionRepository.findBySubject(subject, "predavanja"))
                .thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> distributionService.addNewDistribution(request, "Informatika", 1));
        verify(distributionRepository, never()).save(any());
    }

    @Test
    void deleteDistributionById_deletesDistribution() {
        Distribution dist = new Distribution();
        when(distributionRepository.findById(1L)).thenReturn(Optional.of(dist));

        distributionService.deleteDistributionById(1L);

        verify(distributionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteDistributionById_throwsNotFoundException() {
        when(distributionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> distributionService.deleteDistributionById(1L));
    }

    @Test
    void updateDistribution_updatesAndReturnsDistributionDTO() {
        // --- USER LOGIN ---
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        // --- TEACHER ---
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUserLogin(userLogin);

        // --- SUBJECT ---
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Matematika");
        subject.setLectureSessions(4);
        subject.setExerciseSessions(4);

        // --- POSTOJECA DISTRIBUCIJA ---
        Distribution oldDist = new Distribution();
        oldDist.setId(1L);
        oldDist.setTeacher(teacher);
        oldDist.setSubject(subject);
//        oldDist.setS("vezbe");
        oldDist.setSessionCount(1);
//        oldDist.setStudyProgram("Informatika");
//        oldDist.setS(1);

        // --- REQUEST DTO ---
        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,                        // BITNO: mora isti ID
                "teacher@example.com",
                "Matematika",
                "predavanja",
                2,
                "Informatika",
                1
        );

        // --- MOCKOVI ---
        when(distributionRepository.findById(1L))
                .thenReturn(Optional.of(oldDist));

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);

        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                request.getStudyProgram(),
                request.getSemestar()
        )).thenReturn(subject);

        // ako servis koristi ovo za proveru limita
        when(distributionRepository.findBySubject(subject, request.getClassType()))
                .thenReturn(List.of());

        // --- POZIV ---
        DistributionDTO result = distributionService.updateDistribution(request);

        // --- ASSERT ---
        assertEquals("predavanja", result.getClassType());
        assertEquals(2, result.getSessionCount());
        assertEquals("teacher@example.com", result.getTeacher().getEmail());
    }

    @Test
    void updateDistribution_throwsNotFoundException() {
        DistributionRequestDTO request = new DistributionRequestDTO(
                0L,                     // id
                "teacher@example.com",   // teacher email
                "Matematika",            // subject
                "predavanja",            // classType
                2,                       // sessionCount
                "ETF",                   // studyProgram (primer)
                1                        // semester (primer)
        );
        when(distributionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> distributionService.updateDistribution(request));
    }

    @Test
    void addNewDistribution_throwsConflictException_whenTeacherDoesNotExist() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setLectureSessions(4);
        subject.setExerciseSessions(4);

        DistributionRequestDTO request = new DistributionRequestDTO(
                0L,
                "teacher@example.com",
                "Matematika",
                "predavanja",
                2,
                "ETF",
                1
        );

        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                "Informatika",
                1
        )).thenReturn(subject);

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(null);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> distributionService.addNewDistribution(request, "Informatika", 1)
        );

        assertEquals("Raspodela sa tim predmetom ili nastavnikom ne postoji", ex.getMessage());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }

    @Test
    void addNewDistribution_throwsConflictException_whenSubjectDoesNotExist() {
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUserLogin(userLogin);

        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,
                "teacher@example.com",
                "Matematika",
                "predavanja",
                2,
                "Informatika",
                1
        );

        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                "Informatika",
                1
        )).thenReturn(null);

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> distributionService.addNewDistribution(request, "Informatika", 1)
        );

        assertEquals("Raspodela sa tim predmetom ili nastavnikom ne postoji", ex.getMessage());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }

    // Ovo je test za proveru da updateDistribution_... baci NotFoundException kada ne postoji distribucija sa datim ID-jem.
    @Test
    void updateDistribution_throwsNotFoundException_whenDistributionDoesNotExist() {
        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,
                "teacher@example.com",
                "Matematika",
                "predavanja",
                2,
                "Informatika",
                1
        );

        when(distributionRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> distributionService.updateDistribution(request)
        );

        assertEquals("Raspodela sa tim Id ne postoji", ex.getMessage());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }


    // Test proverava da ne mozes da dodas raspodelu ako bi time presao maximalan broj casova za predavanje.
    // Znaci ako vec imam 2 casova predavanja, a limit je 4, ne mogu da dodam jos 2 casova predavanja, jer bi time doslo do 5 casova, sto je vise od limita.
    @Test
    void addNewDistribution_throwsConflictException_whenLectureSessionsLimitIsExceeded() {
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Pera");
        teacher.setLastName("Peric");
        teacher.setUserLogin(userLogin);

        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Matematika");
        subject.setLectureSessions(4);   // limit za predavanja
        subject.setExerciseSessions(4);

        Distribution existing1 = new Distribution();
        existing1.setId(10L);
        existing1.setSubject(subject);
        existing1.setTeacher(teacher);
        existing1.setSessionCount(2);

        Distribution existing2 = new Distribution();
        existing2.setId(11L);
        existing2.setSubject(subject);
        existing2.setTeacher(teacher);
        existing2.setSessionCount(1);

        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,
                "teacher@example.com",
                "Matematika",
                "predavanja",
                2,              // 2 + 1 + 2 = 5 > 4
                "Informatika",
                1
        );

        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                "Informatika",
                1
        )).thenReturn(subject);

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);

        when(distributionRepository.findBySubject(subject, request.getClassType()))
                .thenReturn(List.of(existing1, existing2));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> distributionService.addNewDistribution(request, "Informatika", 1)
        );

        assertEquals("Prekoracen broj casova za ovaj tip nastave", ex.getMessage());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }


    // Ne mozes da izmenis postojecu raspodelu ako bi time presao limit za predavanje.
    @Test
    void updateDistribution_throwsConflictException_whenLectureSessionsLimitIsExceeded() {
        // --- USER LOGIN ---
        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("teacher@example.com");

        // --- TEACHER ---
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUserLogin(userLogin);

        // --- SUBJECT ---
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Matematika");
        subject.setLectureSessions(4); // limit
        subject.setExerciseSessions(4);

        // --- POSTOJECI ZAPISI ---
        Distribution existing1 = new Distribution();
        existing1.setId(10L);
        existing1.setSubject(subject);
        existing1.setTeacher(teacher);
//        existing1.setSessionType("predavanja");
        existing1.setSessionCount(2);

        Distribution existing2 = new Distribution();
        existing2.setId(11L);
        existing2.setSubject(subject);
        existing2.setTeacher(teacher);
//        existing2.setSessionType("predavanja");
        existing2.setSessionCount(1);

        // --- OVAJ KOJI MENJAS ---
        Distribution toUpdate = new Distribution();
        toUpdate.setId(1L);
        toUpdate.setSubject(subject);
        toUpdate.setTeacher(teacher);
//        toUpdate.setSessionType("predavanja");
        toUpdate.setSessionCount(1); // stara vrednost

        // --- REQUEST ---
        DistributionRequestDTO request = new DistributionRequestDTO(
                1L,
                "teacher@example.com",
                "Matematika",
                "predavanja",
                3, // nova vrednost -> 2 + 1 + 3 = 6 > 4
                "Informatika",
                1
        );

        // --- MOCKOVI ---
        when(distributionRepository.findById(1L))
                .thenReturn(Optional.of(toUpdate));

        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);

        when(subjectRepository.findBySubjectNameStudyProgramSemester(
                request.getSubject(),
                request.getStudyProgram(),
                request.getSemestar()
        )).thenReturn(subject);

        when(distributionRepository.findBySubject(subject, request.getClassType()))
                .thenReturn(List.of(existing1, existing2, toUpdate));

        // --- EXPECT ---
        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> distributionService.updateDistribution(request)
        );

        assertEquals("Prekoracen broj casova za ovaj tip nastave", ex.getMessage());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }



    // Ako pokusam da obrisem raspodelu sa ID-jem koji ne postoji, service treba da baci NotFoundException.
    @Test
    void deleteDistribution_throwsNotFoundException_whenDistributionDoesNotExist() {
        when(distributionRepository.findById(1L))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> distributionService.deleteDistributionById(1L)
        );

        assertEquals("Raspodela nije pronadjena", ex.getMessage());
        verify(distributionRepository, never()).delete(any(Distribution.class));
    }

}