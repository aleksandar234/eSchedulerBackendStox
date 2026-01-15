package com.eScheduler.responses.customDTOClasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterClassDTO {

    private Long id; // ID unosa, može da se prikaže u tabeli
    private String predmetNaMasterStudijama;
    private Integer odrzanoCasova;
    private LocalDate datumOdrzavanjaCasova;
    private LocalDateTime datumUnosa; // frontend može da ga prikaže

}
