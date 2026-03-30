package com.eScheduler.controllers;

import com.eScheduler.model.Subject;
import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.requests.TeacherRequestDTO;
import com.eScheduler.responses.customDTOClasses.DistributionDTO;
import com.eScheduler.services.DistributionService;
import com.eScheduler.services.SubjectService;
import com.eScheduler.services.TeacherService;
import com.eScheduler.repositories.DistributionRepository;
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
class DistributionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private DistributionRepository distributionRepository;

    @BeforeEach
    void setUp() {
        // Predmeti
        Subject baze = new Subject();
        baze.setName("Baze podataka");
        baze.setStudyProgram("RN"); // mora da se slaže sa testom
        baze.setSemester(1);        // validan semestar za test
        baze.setLectureHours(3);
        baze.setExerciseHours(2);
        baze.setPracticumHours(1);
        baze.setMandatory("obavezan");
        baze.setLectureSessions(15);
        baze.setExerciseSessions(10);
        subjectService.addNewSubject(baze);

        Subject prepoznavanje = new Subject();
        prepoznavanje.setName("Prepoznavanje govora");
        prepoznavanje.setStudyProgram("RN");
        prepoznavanje.setSemester(1);
        prepoznavanje.setLectureHours(4);
        prepoznavanje.setExerciseHours(2);
        prepoznavanje.setPracticumHours(0);
        prepoznavanje.setMandatory("izborni");
        prepoznavanje.setLectureSessions(16);
        prepoznavanje.setExerciseSessions(8);
        subjectService.addNewSubject(prepoznavanje);

        // Nastavnici
        teacherService.addNewTeacher(new TeacherRequestDTO(
                null, "pPetrovic@raf.rs", "Petar", "Petrovic", "nastavnik", false
        ));
        teacherService.addNewTeacher(new TeacherRequestDTO(
                null, "aAnic@raf.rs", "Ana", "Anic", "saradnik", true
        ));

        // Raspodele
        distributionService.addNewDistribution(new DistributionRequestDTO(
                null,
                "pPetrovic@raf.rs",
                "Baze podataka",
                "predavanja",
                10,
                "RN",  // studyProgram
                1      // semester
        ));

        distributionService.addNewDistribution(new DistributionRequestDTO(
                null,
                "aAnic@raf.rs",
                "Prepoznavanje govora",
                "vezbe",
                5,
                "RN",
                1
        ));
    }

    @Test
    void getAllDistributions_returnsDistributionsList() throws Exception {
        mockMvc.perform(get("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].subject.name", is("Baze podataka")))
                .andExpect(jsonPath("$[1].subject.name", is("Prepoznavanje govora")));
    }

    @Test
    void createDistribution_createsNewDistribution() throws Exception {
        String newDistributionJson = """
        {
            "teacher": "pPetrovic@raf.rs",
            "subject": "Baze podataka",
            "classType": "vezbe",
            "sessionCount": 3
        }
        """;

        mockMvc.perform(post("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newDistributionJson)
                        .param("studyProgram", "RN")   // <-- ovde šalješ query param
                        .param("semester", "1"))       // <-- ovde šalješ query param
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject.name", is("Baze podataka")))
                .andExpect(jsonPath("$.classType", is("vezbe")))
                .andExpect(jsonPath("$.sessionCount", is(3)));
    }

    @Test
    void deleteDistributionById_deletesDistribution() throws Exception {
        DistributionDTO distribution = distributionService.getAllDistributions().stream()
                .filter(d -> d.getSubject().getName().equals("Prepoznavanje govora"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/distributions/" + distribution.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateDistribution_updatesDistribution() throws Exception {
        DistributionDTO distribution = distributionService.getAllDistributions().stream()
                .filter(d -> d.getSubject().getName().equals("Baze podataka"))
                .findFirst()
                .orElseThrow();

        String updatedDistributionJson = String.format("""
            {
                "id": %d,
                "teacher": "pPetrovic@raf.rs",
                "subject": "Baze podataka",
                "classType": "vezbe",
                "sessionCount": 1,
                "studyProgram": "RN",
                "semester": 4
            }
            """, distribution.getId());

        mockMvc.perform(put("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedDistributionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(distribution.getId().intValue())))
                .andExpect(jsonPath("$.subject.name", is("Baze podataka")))
                .andExpect(jsonPath("$.classType", is("vezbe")))
                .andExpect(jsonPath("$.sessionCount", is(1)));
    }
}