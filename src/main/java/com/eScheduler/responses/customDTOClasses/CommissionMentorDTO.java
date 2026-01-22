package com.eScheduler.responses.customDTOClasses;

import com.eScheduler.model.enums.Types;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionMentorDTO {

    private Long id; // ID unosa, može da se prikaže u tabeli
    private String tip_angazmana;
    private String stepenStudija;
    private String imeStudenta;
    private String temaRada; // frontend može da ga prikaže
    private String napomena;
    private LocalDateTime datumUnosa;

}
