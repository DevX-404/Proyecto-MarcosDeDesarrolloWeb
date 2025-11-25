package com.example.demo.Repository;

import com.example.demo.Model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findByCodigoProfesor(String codigo);
}
