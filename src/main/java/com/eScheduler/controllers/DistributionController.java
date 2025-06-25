package com.eScheduler.controllers;

import com.eScheduler.requests.DistributionRequestDTO;
import com.eScheduler.responses.customDTOClasses.DistributionDTO;
import com.eScheduler.responses.customDTOClasses.StandardUserDTO;
import com.eScheduler.services.DistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/distributions")
@Tag(name = "Distribution API", description = "API for managing distribution")
public class DistributionController {
    private final DistributionService distributionService;

    @Autowired
    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
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

        DistributionDTO savedDistribution = distributionService.addNewDistribution(distribution, studyProgram, semester);
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
        List<StandardUserDTO> standardUserDTO = distributionService.getDistributionByTeacher(email);
        return ResponseEntity.status(HttpStatus.OK).body(standardUserDTO);
    }
    @PostMapping("/import")
    @Operation(summary = "Import distributions from JSON", description = "Imports distribution entries from JSON structure")
    public ResponseEntity<String> importDistributions(@RequestBody List<Map<String, Object>> jsonList) {
        distributionService.importDistributionsFromJson(jsonList);
        return ResponseEntity.status(HttpStatus.CREATED).body("Uvoz uspešno završen.");
    }
}
