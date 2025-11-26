package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "profesores")
@Data
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 50)
    private String codigoProfesor; 

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = true)
    private String correo;

    @Column(length = 225)
    private String fotoUrl;

    @OneToMany(mappedBy = "profesor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Curso> cursosDictados;

    public String getRole(){
        return "ROLE_PROFESOR";
    }
}