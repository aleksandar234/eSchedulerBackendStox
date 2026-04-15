package com.eScheduler.responses.customDTOClasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardUserDTO {
    private Long teacherId;
    private String firstName;
    private String lastName;
    private String email;

    private String name;
    private String studyProgram;
    private Integer semester;
    private Integer lectureHours;
    private Integer exerciseHours;
//    private Integer practicumHours;
//    private String mandatory;
//    private Integer lectureSessions;
//    private Integer exerciseSessions;
//
    private String classType;
    private Integer sessionCount;
}
