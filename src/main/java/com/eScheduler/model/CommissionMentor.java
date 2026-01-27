package com.eScheduler.model;


import com.eScheduler.model.enums.Types;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "commission_mentor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionMentor {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_angazmana", nullable = false)
    private String type;

    @Column(name = "stepen_studija", nullable = false)
    private String degree;

    @Column(name = "ime_studenta", nullable = false)
    private String studentName;

    @Column(name = "tema_rada", nullable = false)
    private String topic;

    @Column(name = "napomena", nullable = true)
    private String note;

    @Column(name = "datum_unosa", nullable = false, updatable = false)
    private LocalDateTime datumUnosa = LocalDateTime.now();

    @Column(name = "nastavnik_id", nullable = false)
    private Long nastavnikId;

    @Column(name = "id_skolska_godina", nullable = false)
    private Long skolskaGodinaId;




}
