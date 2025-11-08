package com.example.demo.Repository;

import com.example.demo.Model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    
    List<Calificacion> findByTareaId(Long tareaId);
    Optional<Calificacion> findByTareaIdAndAlumnoId(Long tareaId, Long alumnoId);
}
