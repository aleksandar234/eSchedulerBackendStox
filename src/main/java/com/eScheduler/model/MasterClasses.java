package com.eScheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "master_predmeti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterClasses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "predmet_postakademskim_studijama", nullable = false)
    private String predmetNaPostakademskimStudijama;

    @Column(name = "odrzano_casova", nullable = false)
    private Integer odrzanoCasova;

    @Column(name = "datum_odrzavanja_casova", nullable = false)
    private LocalDate datumOdrzavanjaCasova;

    @Column(name = "datum_unosa", nullable = false, updatable = false)
    private LocalDateTime datumUnosa = LocalDateTime.now();

    @Column(name = "nastavnik_id", nullable = false)
    private Long nastavnikId;

    @Column(name = "stepenStudija", nullable = false)
    private String stepenStudija;

    @Column(name = "napomena", nullable = true)
    private String napomena;

}
