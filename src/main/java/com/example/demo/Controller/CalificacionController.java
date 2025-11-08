package com.example.demo.Controller;

import com.example.demo.DTO.CalificacionDTO;
import com.example.demo.DTO.CalificacionListWrapper;
import com.example.demo.Model.Curso;
import com.example.demo.Model.Tarea;
import com.example.demo.Service.CursoService;
import com.example.demo.Service.TareaService;
import com.example.demo.Service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profesor")
public class CalificacionController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private CalificacionService calificacionService; 
    
    // 1. Muestra la lista de cursos y las tareas vencidas del curso seleccionado
    @GetMapping("/notas/{profesorId}")
    public String listarTareasVencidas(
            @PathVariable Long profesorId,
            @RequestParam(required = false) Long cursoId,
            Model model) 
    {
        List<Curso> cursosAsignados = cursoService.listarCursosPorProfesor(profesorId);
        model.addAttribute("profesorId", profesorId);
        model.addAttribute("cursosAsignados", cursosAsignados);
        model.addAttribute("cursoSeleccionadoId", cursoId);

        if (cursoId != null) {
            List<Tarea> tareasVencidas = tareaService.getTareasVencidasByCursoId(cursoId);
            
            model.addAttribute("tareasVencidas", tareasVencidas);
            
            model.addAttribute("nombreCurso", cursosAsignados.stream()
                .filter(c -> c.getId().equals(cursoId))
                .findFirst()
                .map(Curso::getNombre)
                .orElse("Curso no encontrado"));
        } else {
             model.addAttribute("tareasVencidas", List.of());
             model.addAttribute("nombreCurso", "Selecciona un curso");
        }
        
        return "Profesor/notas";
    }
    
    // 2. Muestra el formulario para calificar una tarea específica
    @GetMapping("/notas/calificar/{tareaId}/{profesorId}")
    public String showCalificarTarea(
            @PathVariable Long tareaId, 
            @PathVariable Long profesorId,
            Model model) 
    {
        Tarea tarea = tareaService.obtenerTareaPorId(tareaId)
                            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        model.addAttribute("tarea", tarea);
        model.addAttribute("profesorId", profesorId);

        // Obtener la lista de CalificacionDTOs (uno por alumno matriculado en el curso)
        // Este método debe inicializar los DTOs, cargando la nota existente si la hay.
        List<CalificacionDTO> calificaciones = calificacionService.getCalificacionesForTarea(tareaId);
        
        // El modelo para el formulario es una lista
        CalificacionListWrapper wrapper = new CalificacionListWrapper(calificaciones);
        model.addAttribute("calificacionesWrapper", wrapper); 
        
        return "Profesor/calificar_tarea";
    }

    // 3. Guarda las calificaciones 
    @PostMapping("/notas/guardar/{tareaId}/{profesorId}")
    public String guardarCalificaciones(
            @PathVariable Long tareaId,
            @PathVariable Long profesorId,
            @ModelAttribute("calificacionesWrapper") CalificacionListWrapper wrapper, 
            RedirectAttributes redirectAttributes) 
    {
        
        try {
            calificacionService.saveCalificaciones(wrapper.getCalificacionesList()); 
            redirectAttributes.addFlashAttribute("mensaje", "Calificaciones guardadas correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar las calificaciones: " + e.getMessage());
        }
        Long cursoId = tareaService.obtenerTareaPorId(tareaId)
                                   .map(t -> t.getCurso().getId())
                                   .orElse(null);

        return "redirect:/profesor/notas/" + profesorId + (cursoId != null ? "?cursoId=" + cursoId : "");
    }
}