package com.eScheduler.controllers;


import com.eScheduler.model.SchoolYear;
import com.eScheduler.repositories.SchoolYearRepository;
import com.eScheduler.responses.customDTOClasses.SchoolYearDTO;
import com.eScheduler.services.SchoolYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/api/schoolYear")
@Tag(name = "SchoolYear API", description = "API for managing school year")
public class SchoolYearController {

    private final SchoolYearService schoolYearService;
    private final SchoolYearRepository schoolYearRepository;

    public SchoolYearController(SchoolYearService schoolYearService, SchoolYearRepository schoolYearRepository) {
        this.schoolYearService = schoolYearService;
        this.schoolYearRepository = schoolYearRepository;
    }

    @GetMapping("/active")
    @Operation(summary = "Get active school year", description = "Retrieve the currently active school year")
    public ResponseEntity<SchoolYearDTO> getActiveSchoolYear() {
        System.out.println("Usao sam ovde");
        SchoolYearDTO schoolYear = schoolYearService.getActiveSchoolYear();

        return ResponseEntity.status(HttpStatus.OK).body(schoolYear);
    }

    @GetMapping("/findAll")
    @Operation(summary = "Get all school years", description = "Retrieve all school years in data base")
    public ResponseEntity<List<SchoolYear>> getAllSchoolYears() {
        List<SchoolYear> allSchoolYears = schoolYearService.findAllSchoolYears();

        return ResponseEntity.status(HttpStatus.OK).body(allSchoolYears);
    }

    @PostMapping("/createEmptyYear")
    @Operation(summary = "Creates a year", description = "Creates an empty new school year")
    public SchoolYear createEmptyYear(@RequestBody SchoolYearDTO schoolYearDTO) {
        SchoolYear newYear = new SchoolYear();
        System.out.println("Oznaka:" + schoolYearDTO.getOznaka());
        System.out.println("Pocetak:" + schoolYearDTO.getDatum_pocetka());
        System.out.println("Kraj:" + schoolYearDTO.getDatum_zavrsetka());
        System.out.println("Aktivna:" + schoolYearDTO.isAktivna());
        newYear.setLabel(schoolYearDTO.getOznaka());
        newYear.setStartDate(schoolYearDTO.getDatum_pocetka());
        newYear.setEndDate(schoolYearDTO.getDatum_zavrsetka());
        newYear.setActive(schoolYearDTO.isAktivna());

        if(newYear.isActive()) {
            // Ako mi je aktivna ova godina, onda treba da deaktiviram sve ostale godine
            schoolYearRepository.deactivatePreviousYear();
        }

        return schoolYearRepository.save(newYear);

    }

    @PostMapping("/activateSelectedYear")
    public ResponseEntity<SchoolYear> activateSelectedYear(@RequestBody SchoolYear year) {
        if (year == null || year.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        SchoolYear updatedYear = schoolYearService.activateYearAndDeactivateOthers(year.getId());
        return ResponseEntity.ok(updatedYear);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchoolYear(@PathVariable Long id) {
        schoolYearService.removeSelectedYear(id);
        return ResponseEntity.noContent().build(); // 204
    }


}
