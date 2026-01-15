package com.eScheduler.model;

import com.eScheduler.model.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "korisnik")
@Data
public class UserLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idkorisnik")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "admin")
    private boolean isAdmin;

    @Column(name = "lozinka")
    private String password;

    @OneToMany(mappedBy = "userLogin", cascade = CascadeType.ALL)
    private List<Teacher> teacherList = new ArrayList<>();

}
