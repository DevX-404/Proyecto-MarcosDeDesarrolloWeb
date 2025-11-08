package com.example.demo.Repository;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    @Query("SELECT m.alumno FROM Matricula m WHERE m.curso.id = :cursoId")
    List<Alumno> findAlumnosByCursoId(@Param("cursoId") Long cursoId);
    List<Matricula> findByAlumnoId(Long alumnoId);
}