package com.eScheduler.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "skolska_godina")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_skolska_godina")
    private Long id;

    @Column(name = "oznaka", nullable = false, unique = true)
    private String label;

    @Column(name = "datum_pocetka")
    private Date startDate;

    @Column(name = "datum_zavrsetka")
    private Date endDate;

    @Column(name = "aktivna")
    private boolean active;


}
