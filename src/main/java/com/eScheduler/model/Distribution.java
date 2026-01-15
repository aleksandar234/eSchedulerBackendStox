package com.eScheduler.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "raspodela")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Distribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_raspodela")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_nastavnik", referencedColumnName = "idnastavnik")
    @JsonIgnore
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "id_predmet", referencedColumnName = "idpredmet")
    private Subject subject;

    @Column(name = "vrsta_casa")
    private String classType;

    @Column(name = "broj_termina")
    private Integer sessionCount;

    @ManyToOne
    @JoinColumn(name = "id_skolska_godina")
    private SchoolYear schoolYear;

}
