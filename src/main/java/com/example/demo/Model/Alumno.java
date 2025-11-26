package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alumnos")
@Data
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 10)
    private String codigoAlumno; 

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false)
    private String correo;

    @Column(length = 225)
    private String fotoUrl;

    public String getRole(){
        return "ROLE_ALUMNO";
    }
}