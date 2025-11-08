package com.example.demo.Controller;

import com.example.demo.Model.Alumno;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Service.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alumno") 
public class AlumnoController {

    @Autowired
    private AlumnoRepository alumnoRepository; 
    
    @Autowired
    private AlumnoService alumnoService;

    // Módulo: Mis Cursos
    @GetMapping("/cursos/{alumnoId}")
    public String misCursos(@PathVariable Long alumnoId, Model model) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        
        List<Map<String, Object>> cursosConProgreso = alumnoService.getCursosMatriculadosConProgreso(alumnoId);

        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());
        model.addAttribute("cursos", cursosConProgreso);
        
        return "Alumno/mis_cursos"; 
    }

    // Módulo: Mis Tareas
    @GetMapping("/tareas/{alumnoId}")
    public String misTareas(@PathVariable Long alumnoId, Model model) {
        model.addAttribute("alumnoId", alumnoId);
        
        return "Alumno/mis_tareas";
    }

    // Módulo: Mis Notas
    @GetMapping("/notas/{alumnoId}")
    public String misNotas(@PathVariable Long alumnoId, Model model) {
        model.addAttribute("alumnoId", alumnoId);
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