package com.eScheduler.responses.customDTOClasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearDTO {

    private Long id_skolska_godina;
    private String oznaka;
    private Date datum_pocetka;
    private Date datum_zavrsetka;
    private boolean aktivna;

}
