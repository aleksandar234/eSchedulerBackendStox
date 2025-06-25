package com.eScheduler.services;

import com.eScheduler.TestDataProvider;
import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.Distribution;
import com.eScheduler.model.Subject;
import com.eScheduler.model.Teacher;
import com.eScheduler.repositories.DistributionRepository;
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

    @InjectMocks
    private DistributionService distributionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllDistributions_returnsListOfDistributionDTOs() {
        Teacher teacher1 = TestDataProvider.createTeacher1();
        Teacher teacher2 = TestDataProvider.createTeacher2();

        List<Distribution> distributions = List.of(
                new Distribution(1L, teacher1, new Subject(), "predavanja", 2),
                new Distribution(2L, teacher2, new Subject(), "vezbe", 2)
        );
        when(distributionRepository.findAll()).thenReturn(distributions);

        List<DistributionDTO> result = distributionService.getAllDistributions();

        assertEquals(2, result.size());
        assertEquals("predavanja", result.get(0).getClassType());
        assertEquals("vezbe", result.get(1).getClassType());
    }

    @Test
    void addNewDistribution_savesAndReturnsDistributionDTO() {
        DistributionRequestDTO request = new DistributionRequestDTO(1L, "mMarkovic@example.com", "Programiranje","predavanja", 2);
        Subject subject = TestDataProvider.createSubject1();
        Teacher teacher = TestDataProvider.createTeacher1();
        Distribution savedDistribution = new Distribution(1L, teacher, subject, "predavanja", 2);


        when(distributionRepository.findBySubjectName(request.getSubject())).thenReturn(subject);
        when(distributionRepository.findByTeacherEmail("mMarkovic@example.com")).thenReturn(teacher);
        when(distributionRepository.findBySubject(subject, request.getClassType())).thenReturn(List.of());
        when(distributionRepository.save(any(Distribution.class))).thenReturn(savedDistribution);

        DistributionDTO result = distributionService.addNewDistribution(request);

        assertEquals("predavanja", result.getClassType());
    }
    @Test
    void addNewDistribution_throwsNotFoundException_whenTeacherDoNotExist() {
        DistributionRequestDTO request = new DistributionRequestDTO(1L, "jKlinko@example.com", "Programiranje","predavanja", 2);
        Subject subject = new Subject();
        when(distributionRepository.findBySubjectName(request.getSubject())).thenReturn(subject);
        when(distributionRepository.findByUserEmail("jKlinko@example.com")).thenReturn(null);
        when(distributionRepository.findBySubject(subject, request.getClassType())).thenReturn(List.of(new Distribution()));

        assertThrows(ConflictException.class, () -> distributionService.addNewDistribution(request));

        verify(distributionRepository, times(1)).findBySubjectName(request.getSubject());
        verify(distributionRepository, never()).save(any(Distribution.class));
    }

    @Test
    void deleteDistributionById_deletesDistribution() {
        Long distributionId = 1L;
        when(distributionRepository.findById(distributionId)).thenReturn(Optional.of(new Distribution()));

        distributionService.deleteDistributionById(distributionId);

        verify(distributionRepository, times(1)).deleteById(distributionId);
    }

    @Test
    void deleteDistributionById_throwsNotFoundException_whenDistributionNotFound() {
        Long distributionId = 1L;
        when(distributionRepository.findById(distributionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> distributionService.deleteDistributionById(distributionId));
    }

    @Test
    void updateDistribution_updatesAndReturnsDistributionDTO() {
        DistributionRequestDTO request = new DistributionRequestDTO(1L, "mMarkovic@example.com", "Programiranje", "predavanja", 2);
        Subject subject = TestDataProvider.createSubject1();
        Teacher teacher = TestDataProvider.createTeacher1();
        Distribution oldDistribution = new Distribution(1L, teacher, subject, "vezbe", 1);
        when(distributionRepository.findById(request.getId())).thenReturn(Optional.of(oldDistribution));
        when(distributionRepository.findBySubjectName(request.getSubject())).thenReturn(subject);
        when(distributionRepository.findByTeacherEmail(request.getTeacher())).thenReturn(teacher);
        when(distributionRepository.findBySubject(subject, request.getClassType())).thenReturn(List.of());

        // Act
        DistributionDTO result = distributionService.updateDistribution(request);

        // Assert
        assertEquals("predavanja", result.getClassType());
        assertEquals(2, result.getSessionCount());
        assertEquals(subject.getName(), result.getSubject().getName());

        verify(distributionRepository, times(1)).findById(request.getId());
        verify(distributionRepository, times(1)).findByTeacherEmail(request.getTeacher());
    }

    @Test
    void containerupdateDistribution_throwsNotFoundException_whenDistributionNotFound() {
        DistributionRequestDTO request = new DistributionRequestDTO(1L, "mMarkovic@example.com", "Programiranje", "predavanja", 2);
        when(distributionRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> distributionService.updateDistribution(request));
    }
}