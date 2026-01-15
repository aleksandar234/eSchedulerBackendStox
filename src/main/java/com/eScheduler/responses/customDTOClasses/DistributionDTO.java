package com.eScheduler.responses.customDTOClasses;

import com.eScheduler.model.SchoolYear;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionDTO {
    private Long id;
    private TeacherDTO teacher;
    private SubjectDTO subject;
    private String classType;
    private Integer sessionCount;
}
