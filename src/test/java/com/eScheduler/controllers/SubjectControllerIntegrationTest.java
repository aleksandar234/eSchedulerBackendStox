package com.eScheduler.controllers;

import com.eScheduler.model.Subject;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import com.eScheduler.services.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubjectService subjectService;

    @BeforeEach
    void setUp() {
        // Kreiramo prvi predmet
        Subject matematika = new Subject();
        matematika.setName("Matematika");
        matematika.setStudyProgram("RN");
        matematika.setSemester(1);
        matematika.setLectureHours(3);
        matematika.setExerciseHours(2);
        matematika.setPracticumHours(1);
        matematika.setMandatory("obavezan");
        matematika.setLectureSessions(15);
        matematika.setExerciseSessions(10);
        subjectService.addNewSubject(matematika);

        // Kreiramo drugi predmet
        Subject programiranje = new Subject();
        programiranje.setName("Programiranje");
        programiranje.setStudyProgram("RN");
        programiranje.setSemester(2);
        programiranje.setLectureHours(4);
        programiranje.setExerciseHours(2);
        programiranje.setPracticumHours(0);
        programiranje.setMandatory("izborni");
        programiranje.setLectureSessions(16);
        programiranje.setExerciseSessions(8);
        subjectService.addNewSubject(programiranje);
    }

    @Test
    void getAllSubjects_returnsSubjectsList() throws Exception {
        mockMvc.perform(get("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Matematika")))
                .andExpect(jsonPath("$[1].name", is("Programiranje")));
    }

    @Test
    void createSubject_createsNewSubject() throws Exception {
        String newSubjectJson = """
            {
                "name": "Fizika",
                "studyProgram": "RN",
                "semester": 1,
                "lectureHours": 3,
                "exerciseHours": 2,
                "practicumHours": 0,
                "mandatory": "obavezan",
                "lectureSessions": 10,
                "exerciseSessions": 5
            }
            """;

        mockMvc.perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newSubjectJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Fizika")))
                .andExpect(jsonPath("$.studyProgram", is("RN")))
                .andExpect(jsonPath("$.semester", is(1)))
                .andExpect(jsonPath("$.lectureHours", is(3)))
                .andExpect(jsonPath("$.exerciseSessions", is(5)));
    }

    @Test
    void deleteSubjectById_deletesSubject() throws Exception {
        SubjectDTO subject = subjectService.getSubjects().stream()
                .filter(s -> s.getName().equals("Matematika"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/subjects/" + subject.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateSubjectById_updatesSubject() throws Exception {
        SubjectDTO subject = subjectService.getSubjects().stream()
                .filter(s -> s.getName().equals("Programiranje"))
                .findFirst()
                .orElseThrow();

        String updatedSubjectJson = String.format("""
            {
                "id": %d,
                "name": "Napredno Programiranje",
                "studyProgram": "RN",
                "semester": 3,
                "lectureHours": 4,
                "exerciseHours": 2,
                "practicumHours": 1,
                "mandatory": "obavezan",
                "lectureSessions": 12,
                "exerciseSessions": 6
            }
            """, subject.getId());

        mockMvc.perform(put("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedSubjectJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(subject.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Napredno Programiranje")))
                .andExpect(jsonPath("$.practicumHours", is(1)))
                .andExpect(jsonPath("$.mandatory", is("obavezan")));
    }
}