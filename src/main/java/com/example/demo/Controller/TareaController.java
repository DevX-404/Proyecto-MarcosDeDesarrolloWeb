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

    // Create & Update: Guardar Tarea (AJUSTADO para Multipart y manejo de errores de curso)
    @PostMapping(value = "/tareas/{profesorId}/guardar", consumes = {"multipart/form-data"})
    public String guardarTarea(@PathVariable Long profesorId,
                               @Valid @ModelAttribute("tareaDTO") TareaDTO tareaDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        Long redirectCourseId = tareaDTO.getCursoId();
        
        // 1. Manejo del Archivo y Nombre
        String nombreAdjuntoGuardar = null;
        if (tareaDTO.getArchivoAdjunto() != null && !tareaDTO.getArchivoAdjunto().isEmpty()) {
            nombreAdjuntoGuardar = tareaDTO.getArchivoAdjunto().getOriginalFilename();
            // Lógica PENDIENTE: Aquí iría el código para guardar físicamente el archivo.
        }

        // 2. Validación de BindingResult (Validación de campos obligatorios)
        if (result.hasErrors()) {
            // Si hay errores de validación (@NotBlank, @FutureOrPresent), reabre el modal
            redirectAttributes.addFlashAttribute("showTareaModal", true);
            redirectAttributes.addFlashAttribute("tareaDTO", tareaDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.tareaDTO", result);
            redirectAttributes.addFlashAttribute("error", "Error al guardar la tarea. Revisa los campos obligatorios."); 
            
            return "redirect:/profesor/tareas/" + profesorId + (redirectCourseId != null ? "?cursoId=" + redirectCourseId : "");
        }
        
        // 3. Obtener el Curso (CRÍTICO: Verifica que el ID no se haya perdido en el Multipart form)
        if (tareaDTO.getCursoId() == null) {
            redirectAttributes.addFlashAttribute("error", "Error interno: El ID del curso no fue proporcionado en el formulario.");
            return "redirect:/profesor/cursos/" + profesorId;
        }

        Optional<Curso> optionalCurso = cursoService.obtenerCursoPorId(tareaDTO.getCursoId());
        if (optionalCurso.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Error: El curso con ID " + tareaDTO.getCursoId() + " no fue encontrado.");
            return "redirect:/profesor/cursos/" + profesorId;
        }

        // 4. Mapear DTO a Entidad y Guardar
        Tarea tarea;
        if (tareaDTO.getId() != null) {
            tarea = tareaService.obtenerTareaPorId(tareaDTO.getId())
                                .orElse(new Tarea());
        } else {
            tarea = new Tarea();
        }
        
        tarea.setTitulo(tareaDTO.getTitulo());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setFechaLimite(tareaDTO.getFechaLimite());
        tarea.setTipo(tareaDTO.getTipo());
        tarea.setUrlRecurso(tareaDTO.getUrlRecurso());
        
        // 5. Asignar relaciones y adjunto
        tarea.setCurso(optionalCurso.get()); 
        
        if (nombreAdjuntoGuardar != null) {
            tarea.setNombreAdjunto(nombreAdjuntoGuardar);
        } else if (tarea.getId() == null) {
            // Nueva tarea sin archivo adjunto
            tarea.setNombreAdjunto(null); 
        }
        // Si es edición y no subió un archivo nuevo, mantendrá el anterior (comportamiento por defecto de JPA)

        tareaService.guardarTarea(tarea);
        
        redirectAttributes.addFlashAttribute("success", "Tarea guardada correctamente.");
        return "redirect:/profesor/tareas/" + profesorId + (redirectCourseId != null ? "?cursoId=" + redirectCourseId : "");
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