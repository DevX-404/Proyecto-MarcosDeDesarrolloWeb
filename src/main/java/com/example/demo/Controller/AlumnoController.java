package com.example.demo.Controller;

import com.example.demo.DTO.EntregaDTO;
import com.example.demo.DTO.NotasDetalleDTO; 
import com.example.demo.DTO.FotoDTO;
import com.example.demo.Model.Alumno;
import com.example.demo.Model.Aviso;
import com.example.demo.Model.Matricula;
import com.example.demo.Model.Tarea;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Service.AlumnoService;
import com.example.demo.Service.CalificacionService; 
import com.example.demo.Service.TareaService;
import com.example.demo.Repository.AvisoRepository;
import com.example.demo.Repository.MatriculaRepository;
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
import java.util.Optional;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    @Autowired
    private AvisoRepository avisoRepository;
    
    @Autowired
    private MatriculaRepository matriculaRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/perfiles/";

    // Módulo: Mis Cursos
    @GetMapping("/cursos/{alumnoId}")
    public String misCursos(@PathVariable Long alumnoId, Model model) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                         .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));
        
        List<Map<String, Object>> cursosConProgreso = alumnoService.getCursosMatriculadosConProgreso(alumnoId);

        model.addAttribute("alumnoId", alumnoId);
        model.addAttribute("nombreAlumno", alumno.getNombreCompleto());
        model.addAttribute("cursos", cursosConProgreso);
        model.addAttribute("alumno",alumno);
        
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
        model.addAttribute("fotoDTO", new FotoDTO());
        
        return "Alumno/mi_perfil";
    }

    @PostMapping(value = "/perfil/{alumnoId}/actualizar-foto", consumes = {"multipart/form-data"})
    public String actualizarFoto(
            @PathVariable Long alumnoId,
            @Valid @ModelAttribute("fotoDTO") FotoDTO fotoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) 
    {
        if (result.hasErrors() || fotoDTO.getFoto().isEmpty()) {
             redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo de imagen.");
             return "redirect:/alumno/perfil/" + alumnoId;
        } 
        
        Alumno alumno = alumnoRepository.findById(alumnoId)
                                     .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));

        String filename = "alumno_" + alumnoId + "_perfil." + getFileExtension(fotoDTO.getFoto().getOriginalFilename());
        
        try {
            // Asegura que el directorio exista (creando si es necesario)
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 1. Guardar archivo físicamente
            Path filePath = uploadPath.resolve(filename);
            Files.copy(fotoDTO.getFoto().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 2. Actualizar la URL en la base de datos
            alumno.setFotoUrl("/img/perfiles/" + filename);
            alumnoRepository.save(alumno); // Guardar la entidad actualizada
            
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada con éxito.");
            
        } catch (Exception e) {
            System.err.println("Error al guardar la foto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error interno al subir la foto.");
        }
        
        return "redirect:/alumno/perfil/" + alumnoId;
    }
    
    // Método auxiliar para obtener la extensión del archivo
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // Devuelve default si no hay extensión
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // Módulo: Procesar Cambio de Contraseña
    @PostMapping("/perfil/{id}/cambiar-clave")
    public String cambiarClave(
        @PathVariable Long id,
        @RequestParam String contrasenaAnterior,
        @RequestParam String nuevaClave,
        @RequestParam String confirmarClave,
        RedirectAttributes redirectAttributes)
        {
            Alumno alumno = alumnoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));

            String storedPassword = alumno.getPassword();
            
            if (storedPassword == null || !storedPassword.equals(contrasenaAnterior)) { 
            redirectAttributes.addFlashAttribute("error", "La contraseña anterior ingresada es incorrecta.");
            return "redirect:/alumno/perfil/" + id;
            }

            if (nuevaClave.length() < 4) {
             redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 4 caracteres.");
             return "redirect:/alumno/perfil/" + id;
            }

            if (!nuevaClave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error", "La nueva contraseña y la confirmación no coinciden.");
            return "redirect:/alumno/perfil/" + id;
            }

            alumno.setPassword(nuevaClave); 
            alumnoRepository.save(alumno); 
        
            redirectAttributes.addFlashAttribute("mensaje", "¡Contraseña actualizada con éxito!");
            return "redirect:/alumno/perfil/" + id;
        }

        // Modulo: Avisos
        @GetMapping("/avisos/{alumnoId}")
    public String verAvisos(@PathVariable Long alumnoId, Model model) {
        
        Optional<Alumno> alumnoOpt = alumnoRepository.findById(alumnoId);
        if (alumnoOpt.isEmpty()) {
            return "redirect:/login"; 
        }
        Alumno alumno = alumnoOpt.get();
        model.addAttribute("alumno", alumno);
        
        // 1. Obtener los IDs de todos los cursos en los que el alumno está matriculado
        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(alumnoId);
        List<Long> cursoIds = matriculas.stream()
                                     .map(m -> m.getCurso().getId())
                                     .collect(Collectors.toList());

        List<Aviso> avisos;
        
        // 2. Filtrar los avisos: generales O específicos de los cursos del alumno.
        if (cursoIds.isEmpty()) {
             // Si no tiene cursos matriculados, solo mostramos avisos generales
             avisos = avisoRepository.findByCursoIsNullOrderByFechaPublicacionDesc();
        } else {
             // Usamos la consulta personalizada para filtrar por generales o cursos específicos
             avisos = avisoRepository.findGeneralAndCourseSpecificAvisos(cursoIds);
        }

        model.addAttribute("avisos", avisos);

        return "Alumno/mis_avisos";
    }
}