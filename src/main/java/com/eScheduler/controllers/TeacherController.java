package com.eScheduler.controllers;

import com.eScheduler.model.Teacher;
import com.eScheduler.requests.TeacherRequestDTO;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
import com.eScheduler.services.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/teachers")
@Tag(name = "Teacher API", description = "API for managing teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @Autowired
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    @Operation(summary = "Get all teachers", description = "Retrieve a list of all teachers")
    public ResponseEntity<List<TeacherDTO>> teachers() {
        List<TeacherDTO> teachers = teacherService.getTeachers();
        return ResponseEntity.status(HttpStatus.OK).body(teachers);
    }

    @PostMapping
    @Operation(summary = "Register a new teacher", description = "Add a new teacher to the system")
    public ResponseEntity<TeacherDTO> registerNewTeacher(
            @RequestBody @Parameter(description = "Details of the new teacher") TeacherRequestDTO teacher) {
        TeacherDTO savedTeacher = teacherService.addNewTeacher(teacher);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTeacher);
    }

    @DeleteMapping(path = {"{teacherId}"})
    @Operation(summary = "Delete a teacher", description = "Delete a teacher by their ID")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable("teacherId") @Parameter(description = "ID of the teacher to be deleted") Long id ) {
        teacherService.deleteTeacherById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping()
    @Operation(summary = "Update a teacher", description = "Update the details of an existing teacher")
    public ResponseEntity<TeacherDTO> updateTeacher(
            @RequestBody @Parameter(description = "Updated teacher details") TeacherRequestDTO teacher){
        TeacherDTO updatedTeacher = teacherService.updateTeacher(teacher);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTeacher);
    }

    @GetMapping("/professors")
    @Operation(summary = "Get all professors", description = "Retrieve a list of all professors")
    public ResponseEntity<List<TeacherDTO>> getProfessors() {
        List<TeacherDTO> professors = teacherService.getProfessors();
        return ResponseEntity.status(HttpStatus.OK).body(professors);
    }

    @GetMapping("/assistants")
    @Operation(summary = "Get all assistants", description = "Retrieve a list of all assistants")
    public ResponseEntity<List<TeacherDTO>> getAssistants() {
        List<TeacherDTO> assistants = teacherService.getAssistants();
        return ResponseEntity.status(HttpStatus.OK).body(assistants);
    }

    // Dohvatanje svih profesora za određenu školsku godinu
    @GetMapping("/school-year/{schoolYearId}")
    public List<TeacherDTO> getTeachersBySchoolYear(@PathVariable("schoolYearId") Long schoolYearId) {
        return teacherService.getTeachersBySchoolYear(schoolYearId);
    }


    //  Dohvatanje profesora za aktivnu školsku godinu
    @GetMapping("/active")
    public ResponseEntity<List<Teacher>> getTeachersForActiveYear() {
        List<Teacher> teachers = teacherService.getTeachersForActiveYear();
        return ResponseEntity.ok(teachers);
    }

}
