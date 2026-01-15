package com.eScheduler.responses.customDTOClasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopySchoolYearDTO {

    private Long sourceYearId;
    private Long targetYearId;
    private String oznaka;
    private String datum_pocetka;
    private String datum_zavrsetka;
    private boolean aktivna;

}
