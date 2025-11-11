package com.example.demo.Controller;

import com.example.demo.DTO.EntregaDTO;
import com.example.demo.DTO.NotasDetalleDTO; 
import com.example.demo.DTO.FotoDTO;
import com.example.demo.Model.Alumno;
import com.example.demo.Model.Tarea;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Service.AlumnoService;
import com.example.demo.Service.CalificacionService; 
import com.example.demo.Service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; 

@Controller
@RequestMapping("/alumno") 
public class AlumnoController {

    @Autowired
    private AlumnoRepository alumnoRepository; 
    
    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private TareaService tareaService;
    
    @Autowired 
    private CalificacionService calificacionService;

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
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
                                         
        List<Map<String, Object>> tareasPorCurso = alumnoService.getTareasPorCursoYEstado(alumnoId);
        
        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());
        model.addAttribute("tareasPorCurso", tareasPorCurso);
        
        return "Alumno/mis_tareas";
    }

    // Módulo: Detalle de Tarea (Ruta asociada a "Ir a Actividad")
    @GetMapping("/tarea/detalle/{tareaId}")
    public String mostrarDetalleTarea(
            @PathVariable Long tareaId,
            @RequestParam Long alumnoId,
            Model model) 
    {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        Tarea tarea = tareaService.obtenerTareaPorId(tareaId)
                                  .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());
        model.addAttribute("tarea", tarea);
        
        if (!model.containsAttribute("entregaDTO")) {
            model.addAttribute("entregaDTO", new EntregaDTO());
        }

        String estadoActual = alumnoService.getEstadoTarea(tareaId, alumnoId); 
        model.addAttribute("estadoEntrega", estadoActual);

        if (estadoActual.equals("Calificada")) {
            model.addAttribute("notaPlaceholder", "17.5 / 20"); 
        }
        
        return "Alumno/detalle_tarea";
    }

    // Módulo: Procesar Entrega de Tarea
    @PostMapping("/tarea/entregar/{tareaId}/{alumnoId}")
    public String procesarEntregaTarea(
            @PathVariable Long tareaId,
            @PathVariable Long alumnoId,
            @Valid @ModelAttribute("entregaDTO") EntregaDTO entregaDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) 
    {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.entregaDTO", result);
            redirectAttributes.addFlashAttribute("entregaDTO", entregaDTO);
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo para la entrega.");
            return "redirect:/alumno/tarea/detalle/" + tareaId + "?alumnoId=" + alumnoId;
        }

        String nombreArchivo = entregaDTO.getArchivoEntrega().getOriginalFilename();
        
        redirectAttributes.addFlashAttribute("mensaje", 
            "¡Entrega realizada con éxito! Archivo: " + nombreArchivo + " subido. (Lógica de guardado simulada)");
            
        return "redirect:/alumno/tareas/" + alumnoId;
    }

    // Módulo: Mis Notas - CORREGIDO para selección automática
    @GetMapping("/notas/{alumnoId}")
    public String misNotas(@PathVariable Long alumnoId, 
                           @RequestParam(required = false) Long cursoId,
                           Model model) {
        
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        
        // 1. Obtener Cursos matriculados
        List<Map<String, Object>> cursos = alumnoService.getCursosMatriculadosConProgreso(alumnoId);
        
        // 2. Lógica para SELECCIÓN AUTOMÁTICA si no se proporciona cursoId (SOLUCIÓN AL PROBLEMA)
        Long cursoSeleccionadoId = cursoId;
        if (cursoSeleccionadoId == null && !cursos.isEmpty()) {
            cursoSeleccionadoId = (Long) cursos.get(0).get("id");
        }
        
        model.addAttribute("cursosMatriculados", cursos);
        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("cursoSeleccionadoId", cursoSeleccionadoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());

        if (cursoSeleccionadoId != null) {
            // 3. Cargar los detalles solo si ya seleccionamos un curso
            NotasDetalleDTO notasDetalle = calificacionService.getNotasDetalladasByAlumnoAndCursoId(alumnoId, cursoSeleccionadoId);
            model.addAttribute("notasDetalle", notasDetalle);
        }
        
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
    
    // Módulo: Procesar Cambio de Contraseña
    @PostMapping("/perfil/{id}/cambiar-clave")
    public String cambiarClave(
            @PathVariable Long id,
            @RequestParam String nuevaClave,
            @RequestParam String confirmarClave,
            RedirectAttributes redirectAttributes) 
    {
        if (nuevaClave.length() < 4) {
             redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 4 caracteres.");
             return "redirect:/alumno/perfil/" + id;
        }

        if (!nuevaClave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/alumno/perfil/" + id;
        }

        redirectAttributes.addFlashAttribute("mensaje", 
            "¡Contraseña actualizada con éxito! (Simulación).");
        
        return "redirect:/alumno/perfil/" + id;
    }

    // Módulo: Procesar Actualización de Foto
    @PostMapping(value = "/perfil/{id}/actualizar-foto", consumes = {"multipart/form-data"})
    public String actualizarFoto(
            @PathVariable Long id,
            @ModelAttribute FotoDTO fotoDTO,
            RedirectAttributes redirectAttributes) 
    {
        if (fotoDTO.getFoto() == null || fotoDTO.getFoto().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo de imagen para actualizar la foto.");
        } else {
            // Lógica para guardar el archivo en disco (simulada)
            
            String uploadDir = "target/classes/static/img/perfiles/";
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String filename = "alumno_" + id + "_foto.jpg"; 
                Path filePath = uploadPath.resolve(filename);
                Files.copy(fotoDTO.getFoto().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                redirectAttributes.addFlashAttribute("mensaje", 
                    "Foto de perfil actualizada con éxito.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la foto: " + e.getMessage());
            }
        }
        
        return "redirect:/alumno/perfil/" + id;
    }
}