package com.eScheduler.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionRequestDTO {
    private Long id;
    private String teacher;
    private String subject;
    private String classType;
    private Integer sessionCount;
    private String studyProgram;
    private Integer semestar;
}
