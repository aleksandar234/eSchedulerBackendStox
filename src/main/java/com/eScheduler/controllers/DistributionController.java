package com.eScheduler.controllers;

import com.eScheduler.model.Distribution;
import com.eScheduler.model.SchoolYear;
import com.eScheduler.repositories.SchoolYearRepository;
import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.responses.customDTOClasses.CopySchoolYearDTO;
import com.eScheduler.responses.customDTOClasses.DistributionDTO;
import com.eScheduler.responses.customDTOClasses.SchoolYearDTO;
import com.eScheduler.responses.customDTOClasses.StandardUserDTO;
import com.eScheduler.services.DistributionService;
import com.eScheduler.services.SchoolYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/distributions")
@Tag(name = "Distribution API", description = "API for managing distribution")
public class DistributionController {
    private final DistributionService distributionService;
    private final SchoolYearRepository schoolYearRepository;
    private final SchoolYearService schoolYearService;

    @Autowired
    public DistributionController(DistributionService distributionService, SchoolYearRepository schoolYearRepository, SchoolYearService schoolYearService) {
        this.distributionService = distributionService;
        this.schoolYearRepository = schoolYearRepository;
        this.schoolYearService = schoolYearService;
    }

    @GetMapping
    @Operation(summary = "Get all distribution", description = "Retrieve a list of all distribution")
    public ResponseEntity<List<DistributionDTO>> getDistributions() {
        List<DistributionDTO> distributions = distributionService.getAllDistributions();
        return ResponseEntity.status(HttpStatus.OK).body(distributions);
    }

    @PostMapping
    @Operation(summary = "Create a new distribution", description = "Add a new distribution to the system")
    public ResponseEntity<DistributionDTO> createDistribution(
            @RequestBody @Parameter(description = "Details of the new distribution") DistributionRequestDTO distribution,
            @RequestParam String studyProgram,
            @RequestParam String semester) {

        DistributionDTO savedDistribution = distributionService.addNewDistribution(distribution, studyProgram, Integer.valueOf(semester));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDistribution);
    }

    @DeleteMapping(path = "{distributionId}")
    @Operation(summary = "Delete a distribution", description = "Delete a distribution by their ID")
    public ResponseEntity<Void> deleteDistributionById(
            @PathVariable ("distributionId") @Parameter(description = "ID of the distribution to be deleted") Long id){
        distributionService.deleteDistributionById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PutMapping()
    @Operation(summary = "Update a distribution", description = "Update the details of an existing distribution")
    public ResponseEntity<DistributionDTO> updateDistribution(
            @RequestBody @Parameter(description = "Updated distribution details") DistributionRequestDTO distribution){
        DistributionDTO distributionDTO =distributionService.updateDistribution(distribution);
        return ResponseEntity.status(HttpStatus.OK).body(distributionDTO);
    }

    @GetMapping(path = {"{teacherEmail}"})
    @Operation(summary = "get distribution by teacher email", description = "Retrieve a list of all distribution by teacher email")
    public ResponseEntity<List<StandardUserDTO>> getDistributionByTeacher(
            @PathVariable("teacherEmail") @Parameter(description = "email of teacher that need to be returnes") String email ) {
        if(email.equals("astojanovic725m3@raf.rs")) {
            email = "mstanojevic@raf.rs";
        }
        List<StandardUserDTO> standardUserDTO = distributionService.getDistributionByTeacher(email);
        return ResponseEntity.status(HttpStatus.OK).body(standardUserDTO);
    }
    @PostMapping("/import")
    @Operation(summary = "Import distributions from JSON", description = "Imports distribution entries from JSON structure")
    public ResponseEntity<String> importDistributions(@RequestBody List<Map<String, Object>> jsonList) {
        distributionService.importDistributionsFromJson(jsonList);
        return ResponseEntity.status(HttpStatus.CREATED).body("Uvoz uspešno završen.");
    }

    @GetMapping("/school-year/{id}")
    public List<DistributionDTO> getBySchoolYear(@PathVariable Long id) {
        return distributionService.getBySchoolYear(id);
    }

    @Transactional
    @PostMapping("/copy")
    public ResponseEntity<SchoolYear> copyDistributionsToYear(@RequestBody CopySchoolYearDTO copySchoolYearDTO) {



        Long sourceYearId = copySchoolYearDTO.getSourceYearId();
        Long targetYearId = copySchoolYearDTO.getTargetYearId();
        String targetYearLabel = copySchoolYearDTO.getOznaka();
        String targetYearSD = copySchoolYearDTO.getDatum_pocetka();
        String targetYearED = copySchoolYearDTO.getDatum_zavrsetka();
        boolean targetYearActive = copySchoolYearDTO.isAktivna();

        System.out.println("Source year:" + sourceYearId);
        System.out.println("Target year:" + targetYearId);
        System.out.println("Target label:" + targetYearLabel);
        System.out.println("Target SD:" + targetYearSD);
        System.out.println("Target ED:" + targetYearED);
        System.out.println("Target active:" + targetYearActive);

        SchoolYear errorsy = new SchoolYear();
        errorsy.setLabel("Nije prosledjen sourceId");

        if (sourceYearId == null) {
            return ResponseEntity.badRequest().body(errorsy);
        }

        // Okej ovde dobijam sve informacije koje mi trebaju
        // sad sledeci korak je da pre nego sto kopiram godinu, moram prvo da je napravim u bazi, i onda kada je napravim
        // pozovem ovu funkciju ispod i to je to

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false); // obavezno – da ne prihvata nevalidne datume

        Date sdate = null;
        Date edate = null;
        try {
            sdate = formatter.parse(targetYearSD);
            edate = formatter.parse(targetYearED);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SchoolYear copiedSchoolYear = new SchoolYear();
        copiedSchoolYear.setLabel(targetYearLabel);
        copiedSchoolYear.setStartDate(sdate);
        copiedSchoolYear.setEndDate(edate);
        copiedSchoolYear.setActive(targetYearActive);

        if(copiedSchoolYear.isActive()) {
            // Ako mi je aktivna ova godina, onda treba da deaktiviram sve ostale godine
            schoolYearRepository.deactivatePreviousYear();
        }

        SchoolYear savedYear = schoolYearRepository.save(copiedSchoolYear);
        Long targetId = savedYear.getId();



        // to je manje vise jedna linija koda, ali i sutra cu da istesitiram sta mi treba, pa mi ostaje da se zezam na frontu da
        // oznacim nekako koja je godina aktivna i da ne dam neku vrstu mlitave barijere da ako korisnik zeli da menja ne aktivne godine, tj prethodne
        // nije nuzno da su neaktivne, samo one sa manjom lable oznakom da mu tu dam kao neki vid restrikcije

        distributionService.copyDistributionsToYear(sourceYearId, targetId);
//        schoolYearService.createSchoolYear();


        return ResponseEntity.ok(copiedSchoolYear);
    }

}
