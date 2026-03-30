package com.eScheduler.controllers;

import com.eScheduler.requests.TeacherRequestDTO;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
import com.eScheduler.services.TeacherService;
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
class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherService teacherService;

    @BeforeEach
    void setUp() {
        teacherService.addNewTeacher(
                new TeacherRequestDTO(null, "pPetrovic@raf.rs", "Petar", "Petrovic", "nastavnik", false)
        );
        teacherService.addNewTeacher(
                new TeacherRequestDTO(null, "aAnic@raf.rs", "Ana", "Anic", "saradnik", true)
        );
    }

    @Test
    void getAllTeachers_returnsTeachersList() throws Exception {
        mockMvc.perform(get("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Petar")))
                .andExpect(jsonPath("$[1].firstName", is("Ana")));
    }

    @Test
    void createTeacher_createsNewTeacher() throws Exception {
        String newTeacherJson = """
            {
                "email": "mMarkovic@raf.rs",
                "firstName": "Marko",
                "lastName": "Markovic",
                "title": "nastavnik",
                "isAdmin": false
            }
            """;

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTeacherJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Marko")))
                .andExpect(jsonPath("$.email", is("mMarkovic@raf.rs")))
                .andExpect(jsonPath("$.title", is("nastavnik")));
    }

    @Test
    void deleteTeacherById_deletesTeacher() throws Exception {
        TeacherDTO teacher = teacherService.getTeachers().stream()
                .filter(t -> t.getFirstName().equals("Petar"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/teachers/" + teacher.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateTeacher_updatesTeacher() throws Exception {
        TeacherDTO teacher = teacherService.getTeachers().stream()
                .filter(t -> t.getFirstName().equals("Ana"))
                .findFirst()
                .orElseThrow();

        String updatedTeacherJson = String.format("""
            {
                "id": %d,
                "email": "aAnic@raf.rs",
                "firstName": "Ana",
                "lastName": "Andelkovic",
                "title": "nastavnik",
                "isAdmin": true
            }
            """, teacher.getId());

        mockMvc.perform(put("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTeacherJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(teacher.getId().intValue())))
                .andExpect(jsonPath("$.lastName", is("Andelkovic")))
                .andExpect(jsonPath("$.email", is("aAnic@raf.rs")))
                .andExpect(jsonPath("$.title", is("nastavnik")));
    }

    @Test
    void getProfessors_returnsOnlyProfessors() throws Exception {
        mockMvc.perform(get("/api/teachers/professors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("nastavnik")));
    }

    @Test
    void getAssistants_returnsOnlyAssistants() throws Exception {
        mockMvc.perform(get("/api/teachers/assistants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("saradnik")));
    }
}