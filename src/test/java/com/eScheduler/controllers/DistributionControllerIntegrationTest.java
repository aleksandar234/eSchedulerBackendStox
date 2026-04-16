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

    // Ako posaljem POST sa nepostojecim teacherom, api treba da vrati gresku 409 Conflict, jer ne moze da se napravi raspodela sa nepostojecim nastavnikom
    @Test
    void createDistribution_returnsConflict_whenTeacherDoesNotExist() throws Exception {
        String requestJson = """
    {
        "teacher": "nepostojeci@raf.rs",
        "subject": "Baze podataka",
        "classType": "predavanja",
        "sessionCount": 2
    }
    """;

        mockMvc.perform(post("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .param("studyProgram", "RN")
                        .param("semester", "1"))
                .andExpect(status().isConflict());
    }

    // Ako posaljem POST sa nepostojecim predmetom, api treba da vrati gresku 409 Conflict, jer ne moze da se napravi raspodela sa nepostojecim predmetom
    @Test
    void createDistribution_returnsConflict_whenSubjectDoesNotExist() throws Exception {
        String requestJson = """
    {
        "teacher": "pPetrovic@raf.rs",
        "subject": "Nepostojeci predmet",
        "classType": "predavanja",
        "sessionCount": 2
    }
    """;

        mockMvc.perform(post("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .param("studyProgram", "RN")
                        .param("semester", "1"))
                .andExpect(status().isConflict());
    }

    // Ako posaljem POST sa sessionCount koji je veci od preostalih casova za taj predmet, api treba da vrati gresku 409 Conflict, jer ne moze da se napravi raspodela sa vise casova nego sto ih ima preostalo
    @Test
    void createDistribution_returnsConflict_whenLectureLimitIsExceeded() throws Exception {
        String requestJson = """
    {
        "teacher": "pPetrovic@raf.rs",
        "subject": "Baze podataka",
        "classType": "predavanja",
        "sessionCount": 6
    }
    """;

        mockMvc.perform(post("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .param("studyProgram", "RN")
                        .param("semester", "1"))
                .andExpect(status().isConflict());
    }

    // Ako posaljem PUT sa id-jem koji ne postoji, api treba da vrati gresku 404 Not Found, jer ne moze da se azurira raspodela koja ne postoji
    @Test
    void updateDistribution_returnsNotFound_whenDistributionDoesNotExist() throws Exception {
        String requestJson = """
    {
        "id": 999,
        "teacher": "pPetrovic@raf.rs",
        "subject": "Baze podataka",
        "classType": "predavanja",
        "sessionCount": 2,
        "studyProgram": "RN",
        "semester": 1
    }
    """;

        mockMvc.perform(put("/api/distributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    // Pokusam da obrisem id koji ne postoji, vrati ce mi 404 NotFound.
    @Test
    void deleteDistribution_returnsNotFound_whenDistributionDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/distributions/999"))
                .andExpect(status().isNotFound());
    }

}