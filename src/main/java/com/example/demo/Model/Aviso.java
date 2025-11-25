package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "avisos")
@Data
@NoArgsConstructor
public class Aviso {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, length = 150)
    private String titulo;

    @Lob 
    @Column(nullable = false)
    private String contenido; 

    @CreationTimestamp
    private LocalDateTime fechaPublicacion; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = true) 
    private Curso curso;

    public Aviso(String titulo, String contenido, Profesor profesor, Curso curso) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.profesor = profesor;
        this.curso = curso;
    }
}