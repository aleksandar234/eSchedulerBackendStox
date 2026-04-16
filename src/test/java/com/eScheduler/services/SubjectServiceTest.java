package com.eScheduler.services;

import com.eScheduler.exceptions.custom.ConflictException;
import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.Subject;
import com.eScheduler.repositories.SubjectRepository;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Subject createSubject(Long id, String name) {
        Subject subject = new Subject();  // default konstruktor
        subject.setId(id);
        subject.setName(name);
        subject.setStudyProgram("RN");
        subject.setSemester(2);
        subject.setLectureHours(2);
        subject.setExerciseHours(2);
        subject.setPracticumHours(2);
        subject.setMandatory("obavezni");
        subject.setLectureSessions(2);
        subject.setExerciseSessions(2);
//        subject.setTeacher(null); // ili postavi stvarnog Teacher objekta ako je potreban
        return subject;
    }

    @Test
    void getSubjects_returnsListOfSubjectDTOs() {
        List<Subject> subjects = List.of(
                createSubject(1L,"OOP"),
                createSubject(2L,"NMA")
        );
        when(subjectRepository.findAll()).thenReturn(subjects);

        List<SubjectDTO> result = subjectService.getSubjects();

        assertEquals(2, result.size());
        assertEquals("OOP", result.get(0).getName());
        assertEquals("NMA", result.get(1).getName());
    }

    @Test
    void addNewSubject_savesAndReturnsSubjectDTO() {
        Subject subject = createSubject(null,"OOP");
        when(subjectRepository.findByName(subject.getName())).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(createSubject(1L, "OOP"));

        SubjectDTO result = subjectService.addNewSubject(subject);

        assertEquals("OOP", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void addNewSubject_throwsConflictException_whenSubjectExists() {
        Subject subject = createSubject(1L,"OOP");
        when(subjectRepository.findByName(subject.getName())).thenReturn(Optional.of(subject));

        assertThrows(ConflictException.class, () -> subjectService.addNewSubject(subject));
    }

    @Test
    void deleteSubjectById_deletesSubject() {
        Subject subject = createSubject(1L,"OOP");
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        subjectService.deleteSubjectById(subject.getId());

        verify(subjectRepository, times(1)).deleteById(subject.getId());
    }

    @Test
    void deleteSubjectById_throwsNotFoundException_whenSubjectNotFound() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> subjectService.deleteSubjectById(1L));
    }

    @Test
    void updateSubject_updatesAndReturnsSubjectDTO() {
        Subject oldSubject = createSubject(1L,"OOP");
        Subject updatedSubject = createSubject(1L,"NMA");
        when(subjectRepository.findById(oldSubject.getId())).thenReturn(Optional.of(oldSubject));
        when(subjectRepository.save(oldSubject)).thenReturn(updatedSubject);

        SubjectDTO result = subjectService.updateSubject(updatedSubject);

        assertEquals("NMA", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void updateSubject_throwsNotFoundException_whenSubjectNotFound() {
        Subject subject = createSubject(1L,"OOP");
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subjectService.updateSubject(subject));
    }


    // Kada radim update menjaju se samo polja koja nisu null, ostala ostaju ista.
    @Test
    void updateSubject_updatesOnlyNonNullFields() {
        // --- STARI SUBJECT ---
        Subject oldSubject = createSubject(1L, "OOP");

        // --- NOVI (PARCIJALNI UPDATE) ---
        Subject updateRequest = new Subject();
        updateRequest.setId(1L);
        updateRequest.setName("NMA");   // samo ovo menjamo
        // ostalo ostaje null

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(oldSubject));

        when(subjectRepository.save(oldSubject))
                .thenReturn(oldSubject);

        SubjectDTO result = subjectService.updateSubject(updateRequest);

        // --- PROVERE ---
        assertEquals("NMA", result.getName());         // promenjeno
        assertEquals("RN", result.getStudyProgram());  // ostalo isto
        assertEquals(2, result.getSemester());         // ostalo isto
    }

}