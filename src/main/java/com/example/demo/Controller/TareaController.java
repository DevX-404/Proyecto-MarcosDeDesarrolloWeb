package com.example.demo.Controller;

import com.example.demo.DTO.TareaDTO;
import com.example.demo.Model.Curso;
import com.example.demo.Model.Tarea;
import com.example.demo.Service.CursoService;
import com.example.demo.Service.TareaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profesor")
public class TareaController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private TareaService tareaService;

    // Read : Listar Tareas
    @GetMapping("/tareas/{profesorId}")
    public String listarTareas(@PathVariable Long profesorId,
                               @RequestParam(required = false) Long cursoId,
                               Model model) {

        List<Curso> cursosAsignados = cursoService.listarCursosPorProfesor(profesorId);
        List<Tarea> tareas;
        String nombreCurso = "Selecciona un Curso";

        if (cursoId != null) {
            tareas = tareaService.listarTareasPorCurso(cursoId);
            nombreCurso = cursoService.obtenerCursoPorId(cursoId).map(Curso::getNombre).orElse(nombreCurso);
        } else {
            tareas = List.of();
        }

        model.addAttribute("cursosAsignados", cursosAsignados);
        model.addAttribute("profesorId", profesorId);
        model.addAttribute("cursoSeleccionadoId", cursoId);
        model.addAttribute("nombreCurso", nombreCurso);
        model.addAttribute("tareas", tareas);

        // Inicializar DTO para el modal, si no viene de una redirección con errores
        if (!model.containsAttribute("tareaDTO")) {
            TareaDTO tareaDTO = new TareaDTO();
            // Asignar el curso seleccionado al DTO para que el campo oculto funcione
            if (cursoId != null) {
                 tareaDTO.setCursoId(cursoId); 
            }
            model.addAttribute("tareaDTO", tareaDTO);
        }

        return "Profesor/tareas"; 
    }

    // Create & Update: Guardar Tarea
    @PostMapping("/tareas/{profesorId}/guardar")
    public String guardarTarea(@PathVariable Long profesorId,
                               @Valid @ModelAttribute("tareaDTO") TareaDTO tareaDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        Long redirectCourseId = tareaDTO.getCursoId(); 
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("showTareaModal", true); 
            redirectAttributes.addFlashAttribute("tareaDTO", tareaDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.tareaDTO", result);
            redirectAttributes.addFlashAttribute("error", "Error al guardar la tarea. Revisa los campos.");
            
            return "redirect:/profesor/tareas/" + profesorId + (redirectCourseId != null ? "?cursoId=" + redirectCourseId : "");
        }

        Optional<Curso> optionalCurso = cursoService.obtenerCursoPorId(tareaDTO.getCursoId());
        if (optionalCurso.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Curso no encontrado.");
            return "redirect:/profesor/tareas/" + profesorId + (redirectCourseId != null ? "?cursoId=" + redirectCourseId : "");
        }

        // Mapear DTO a Entidad 
        Tarea tarea = new Tarea();
        if (tareaDTO.getId() != null) {
            tarea = tareaService.obtenerTareaPorId(tareaDTO.getId()).orElse(tarea);
        }
        
        tarea.setTitulo(tareaDTO.getTitulo());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setFechaLimite(tareaDTO.getFechaLimite());
        tarea.setTipo(tareaDTO.getTipo());
        tarea.setCurso(optionalCurso.get());

        tareaService.guardarTarea(tarea);
        redirectAttributes.addFlashAttribute("mensaje", "Tarea guardada exitosamente!");
        return "redirect:/profesor/tareas/" + profesorId + "?cursoId=" + redirectCourseId;
    }

    // Delete: Eliminar Tarea
    @GetMapping("/tareas/eliminar/{tareaId}/{profesorId}")
    public String eliminarTarea(@PathVariable Long tareaId, 
                                @PathVariable Long profesorId,
                                @RequestParam Long cursoId,
                                RedirectAttributes redirectAttributes) {
        
        tareaService.eliminarTarea(tareaId);
        redirectAttributes.addFlashAttribute("mensaje", "Tarea eliminada con éxito.");
        return "redirect:/profesor/tareas/" + profesorId + "?cursoId=" + cursoId;
    }
}