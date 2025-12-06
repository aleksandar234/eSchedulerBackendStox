package com.eScheduler.controllers;


import com.eScheduler.model.Subject;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import com.eScheduler.services.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/subjects")
@Tag(name = "Subject API", description = "API for managing subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "Get all subjects", description = "Retrieve a list of all subjects")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        System.out.println("Hello Aleksadnar from Backend");
        List<SubjectDTO> subjects = subjectService.getSubjects();
        return ResponseEntity.status(HttpStatus.OK).body(subjects);
    }

    @PostMapping
    @Operation(summary = "Create a new subject", description = "Add a new subject to the system")
    public ResponseEntity<SubjectDTO> createSubject(
            @RequestBody @Parameter(description = "Details of the new subject") Subject subject){
        SubjectDTO savedSubject = subjectService.addNewSubject(subject);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSubject);
    }

    @DeleteMapping(path = {"{subjectId}"})
    @Operation(summary = "Delete a subject", description = "Delete a subject by their ID")
    public ResponseEntity<Void> deleteSubjectById(
            @PathVariable("subjectId") @Parameter(description = "ID of the subject to be deleted") Long id){
        subjectService.deleteSubjectById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping()
    @Operation(summary = "Update a subject", description = "Update the details of an existing subject")
    public ResponseEntity<SubjectDTO> updateSubjectById(
            @RequestBody @Parameter(description = "Updated subject details") Subject subject){
        SubjectDTO updatedSubject = subjectService.updateSubject(subject);
        return ResponseEntity.status(HttpStatus.OK).body(updatedSubject);
    }
}
