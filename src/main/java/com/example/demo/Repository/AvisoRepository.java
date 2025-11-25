package com.example.demo.Repository;

import com.example.demo.Model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    
    // 1. Consulta para avisos generales (curso IS NULL) O avisos de un curso que el alumno tiene matriculado.
    // El orden por fecha de publicación descendente (más recientes primero) es clave.
    @Query("SELECT a FROM Aviso a WHERE a.curso IS NULL OR a.curso.id IN :cursoIds ORDER BY a.fechaPublicacion DESC")
    List<Aviso> findGeneralAndCourseSpecificAvisos(@Param("cursoIds") List<Long> cursoIds);

    // 2. Método de respaldo (por si el alumno no tiene cursos, solo se muestran los generales)
    List<Aviso> findByCursoIsNullOrderByFechaPublicacionDesc();

    // 1. Método faltante (causa del error): Obtiene avisos por el ID del profesor
    List<Aviso> findByProfesorIdOrderByFechaPublicacionDesc(Long profesorId); // <-- SOLUCIÓN APLICADA
}