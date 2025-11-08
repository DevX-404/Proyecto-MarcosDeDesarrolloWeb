package com.example.demo.Repository;

import com.example.demo.Model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByNombreCompletoContainingIgnoreCase(String nombre); 
    Optional<Alumno> findByCodigoAlumno(String codigo);
}
