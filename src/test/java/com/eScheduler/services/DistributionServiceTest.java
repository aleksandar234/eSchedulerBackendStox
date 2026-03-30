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
        Teacher teacher = new Teacher();
        teacher.setId(1L);
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
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        Subject subject = new Subject();
        subject.setId(1L);
        DistributionRequestDTO request = new DistributionRequestDTO(
                0L,                     // id
                "teacher@example.com",   // teacher email
                "Matematika",            // subject
                "predavanja",            // classType
                2,                       // sessionCount
                "ETF",                   // studyProgram (primer)
                1                        // semester (primer)
        );

        when(distributionRepository.findBySubjectNameStudyProgramSemester(anyString(), anyString(), anyInt()))
                .thenReturn(subject);
        when(distributionRepository.findByTeacherEmail(request.getTeacher()))
                .thenReturn(teacher);
        when(distributionRepository.findBySubject(subject, "predavanja")).thenReturn(List.of());
        when(distributionRepository.save(any(Distribution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DistributionDTO result = distributionService.addNewDistribution(request, "Informatika", 1);

        assertEquals("predavanja", result.getClassType());
        assertEquals(2, result.getSessionCount());
        verify(distributionRepository, times(1)).save(any(Distribution.class));
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
        Teacher teacher = new Teacher();
        Subject subject = new Subject();
        Distribution oldDist = new Distribution(1L, teacher, subject, "vezbe", 1, activeYear);
        DistributionRequestDTO request = new DistributionRequestDTO(
                0L,                     // id
                "teacher@example.com",   // teacher email
                "Matematika",            // subject
                "predavanja",            // classType
                2,                       // sessionCount
                "ETF",                   // studyProgram (primer)
                1                        // semester (primer)
        );

        when(distributionRepository.findById(1L)).thenReturn(Optional.of(oldDist));
        when(distributionRepository.findByTeacherEmail(request.getTeacher())).thenReturn(teacher);
        when(distributionRepository.findBySubject(subject, request.getClassType())).thenReturn(List.of());

        DistributionDTO result = distributionService.updateDistribution(request);

        assertEquals("predavanja", result.getClassType());
        assertEquals(2, result.getSessionCount());
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
}