package com.example.demo.Controller;

import com.example.demo.Model.Alumno;
import com.example.demo.Repository.AlumnoRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alumno") 
public class AlumnoController {

    @Autowired
    private AlumnoRepository alumnoRepository; 

    // Módulo: Mis Cursos
    @GetMapping("/cursos/{alumnoId}")
    public String misCursos(@PathVariable Long alumnoId, Model model) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        
        List<Map<String, Object>> cursosConProgreso = List.of(
            Map.of("nombre", "Programación Orientada a Objetos", "profesorNombre", "Prof. Demo", "progreso", 50),
            Map.of("nombre", "Algoritmos y Estructura de Datos", "profesorNombre", "Prof. Demo", "progreso", 25)
        );

        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());
        model.addAttribute("cursos", cursosConProgreso);
        
        return "Alumno/mis_cursos"; 
    }

    // Módulo: Mis Tareas
    @GetMapping("/tareas/{alumnoId}")
    public String misTareas(@PathVariable Long alumnoId, Model model) {
        
        List<Map<String, Object>> tareasPorCurso = List.of(
             Map.of("cursoNombre", "Programación Orientada a Objetos", "tareas", List.of(
                Map.of("titulo", "Examen Parcial", "estado", "Pendiente", "fechaLimite", LocalDate.now().plusDays(2)),
                Map.of("titulo", "Laboratorio 1", "estado", "Completada", "fechaLimite", LocalDate.now().minusDays(10))
            ))
        );
        
        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("tareasPorCurso", tareasPorCurso); 
        
        return "Alumno/mis_tareas";
    }

    // Módulo: Mis Notas
    @GetMapping("/notas/{alumnoId}")
    public String misNotas(@PathVariable Long alumnoId, Model model) {
        
        List<Map<String, Object>> notasPorCurso = List.of(
            Map.of("cursoNombre", "Programación Orientada a Objetos", "promedio", 16.5, "calificaciones", List.of(
                Map.of("tareaTitulo", "Examen Parcial", "nota", 18.0, "comentario", "Excelente trabajo"),
                Map.of("tareaTitulo", "Taller 1", "nota", 15.0, "comentario", null)
            ))
        );

        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("notasPorCurso", notasPorCurso); 
        
        return "Alumno/mis_notas";
    }

    // Módulo: Mi Perfil
    @GetMapping("/perfil/{alumnoId}")
    public String miPerfil(@PathVariable Long alumnoId, Model model) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        
        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("alumno", alumno);
        
        return "Alumno/mi_perfil";
    }
}