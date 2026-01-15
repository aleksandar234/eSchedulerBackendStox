package com.eScheduler.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "nastavnik")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnastavnik")
    private Long id;

    @Column(name = "ime")
    private String firstName;

    @Column(name = "prezime")
    private String lastName;

    @Column(name = "zvanje")
    private String title;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email")
    private UserLogin userLogin;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Distribution> distributions;

    @ManyToOne
    @JoinColumn(name = "id_skolska_godina")
    private SchoolYear schoolYear;

}
