package com.example.demo.Service;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Matricula;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    public Alumno getAlumnoById(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con ID: " + id));
    }

    // Módulo: Mis Cursos 
    public List<Map<String, Object>> getCursosMatriculadosConProgreso(Long alumnoId) {
        
        // 1. Obtener todas las matrículas del alumno
        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(alumnoId);

        // 2. Mapear las matrículas a la estructura de la vista
        return matriculas.stream()
            .map(matricula -> {
                // Obtener el objeto Curso completo
                var curso = matricula.getCurso();
                var profesor = curso.getProfesor();

                int progresoSimulado = 0; 

                return Map.<String, Object>of(
                    "id", curso.getId(),
                    "nombre", curso.getNombre(),
                    "profesorNombre", profesor.getNombre(), 
                    "progreso", progresoSimulado
                );
            })
            .collect(Collectors.toList());
    }

    // Módulo: Mis Tareas 
    public List<Map<String, Object>> getTareasPorCursoYEstado(Long alumnoId) {
        if (alumnoId.equals(1L)) { // Juan (U001)
            return List.of(
                    Map.of("cursoNombre", "Programación Orientada a Objetos", "tareas", List.of(
                            Map.of("titulo", "Examen Parcial (VENCIDO)", "estado", "Pendiente",
                                    "fechaLimite", LocalDate.now().minusDays(5)),
                            Map.of("titulo", "Laboratorio 3", "estado", "Completada",
                                    "fechaLimite", LocalDate.now().plusDays(5)))),
                    Map.of("cursoNombre", "Algoritmos y Estructura de Datos", "tareas", List.of(
                            Map.of("titulo", "Proyecto Final", "estado", "Pendiente",
                                    "fechaLimite", LocalDate.now().plusDays(20)))));
        } else if (alumnoId.equals(3L)) { // Carlos (U003) - Solo debería ver C2
            return List.of(
                    Map.of("cursoNombre", "Algoritmos y Estructura de Datos", "tareas", List.of(
                            Map.of("titulo", "Práctica de Grafos", "estado", "Pendiente",
                                    "fechaLimite", LocalDate.now().plusDays(15)))));
        }

        return List.of();
    }
}