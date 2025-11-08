package com.example.demo.Repository;

import com.example.demo.Model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByCursoIdOrderByFechaLimiteAsc(Long cursoId);
    List<Tarea> findByCursoIdAndFechaLimiteBefore(Long cursoId, LocalDate fechaLimite);
}