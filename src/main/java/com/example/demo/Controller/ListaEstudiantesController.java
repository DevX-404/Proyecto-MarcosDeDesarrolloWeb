package com.example.demo.Controller;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Curso;
import com.example.demo.Repository.MatriculaRepository;
import com.example.demo.Service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/profesor")
public class ListaEstudiantesController {

    @Autowired
    private CursoService cursoService;
    
    @Autowired
    private MatriculaRepository matriculaRepository;
    // Muestra la vista de Lista de Estudiantes
    @GetMapping("/lista/{profesorId}")
    public String listarEstudiantes(@PathVariable Long profesorId,
                                    @RequestParam(required = false) Long cursoId, // Filtro por Curso
                                    Model model) {
        
        // Cargar todos los cursos del profesor para el filtro
        List<Curso> cursosAsignados = cursoService.listarCursosPorProfesor(profesorId);
        model.addAttribute("cursosAsignados", cursosAsignados);
        model.addAttribute("profesorId", profesorId);
        
        List<Alumno> alumnos;
        String nombreCurso = "Selecciona un Curso";

        if (cursoId != null) {
            alumnos = matriculaRepository.findAlumnosByCursoId(cursoId); 
            
            nombreCurso = cursoService.obtenerCursoPorId(cursoId).map(Curso::getNombre).orElse(nombreCurso);
        } else {
            // Si no se selecciona un curso, la tabla estará vacía
            alumnos = List.of(); 
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("cursoSeleccionadoId", cursoId);
        model.addAttribute("nombreCurso", nombreCurso);
        
        return "Profesor/alumnos"; 
    }
}