package com.example.demo.Controller;

import com.example.demo.DTO.FotoDTO;
import com.example.demo.Model.Aviso;
import com.example.demo.Model.Profesor;
import com.example.demo.Model.Curso;
import com.example.demo.Repository.ProfesorRepository;
import com.example.demo.Repository.CursoRepository;
import com.example.demo.Repository.AvisoRepository;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Controller
@RequestMapping("/profesor")
public class ProfesorController {

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AvisoRepository avisoRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/perfiles/";

    // --- MÉTODOS DE PERFIL ---

    @GetMapping("/perfil/{id}") // RUTA CORREGIDA
    public String verPerfil(@PathVariable Long id, Model model) {
        Optional<Profesor> profesorOpt = profesorRepository.findById(id);
        if (profesorOpt.isPresent()) {
            Profesor profesor = profesorOpt.get();
            model.addAttribute("profesor", profesor);
            model.addAttribute("profesorId", id);
            model.addAttribute("fotoDTO", new FotoDTO());
            return "Profesor/mi_perfil_profesor";
        }
        return "redirect:/login";
    }

    @PostMapping("/perfil/{id}/cambiar-clave") // RUTA CORREGIDA
    public String cambiarClave(
            @PathVariable Long id,
            @RequestParam String contrasenaAnterior,
            @RequestParam String nuevaClave,
            @RequestParam String confirmarClave,
            RedirectAttributes redirectAttributes) {

        Optional<Profesor> profesorOpt = profesorRepository.findById(id);
        if (!profesorOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Error: Profesor no encontrado.");
            return "redirect:/profesor/perfil/" + id;
        }

        Profesor profesor = profesorOpt.get();
        String storedPassword = profesor.getPassword();

        // 1. Validar Contraseña Anterior (Maneja el caso de storedPassword nulo)
        if (storedPassword == null || !storedPassword.equals(contrasenaAnterior)) {
            redirectAttributes.addFlashAttribute("error", "Error: La contraseña anterior es incorrecta.");
            return "redirect:/profesor/perfil/" + id;
        }

        // 2. Validar que las nuevas claves coincidan
        if (!nuevaClave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error", "Error: Las nuevas contraseñas no coinciden.");
            return "redirect:/profesor/perfil/" + id;
        }

        // 3. Validar longitud mínima
        if (nuevaClave.length() < 4) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: La nueva contraseña debe tener al menos 4 caracteres.");
            return "redirect:/profesor/perfil/" + id;
        }

        // 4. Actualizar Contraseña
        profesor.setPassword(nuevaClave);
        profesorRepository.save(profesor);

        redirectAttributes.addFlashAttribute("mensaje", "¡Contraseña actualizada exitosamente!");
        return "redirect:/profesor/perfil/" + id;
    }

    @PostMapping(value = "/perfil/{profesorId}/actualizar-foto", consumes = { "multipart/form-data" })
    public String actualizarFoto(
            @PathVariable Long profesorId,
            @Valid @ModelAttribute("fotoDTO") FotoDTO fotoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors() || fotoDTO.getFoto().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo de imagen.");
            return "redirect:/profesor/perfil/" + profesorId;
        }

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado."));

        String filename = "profesor_" + profesorId + "_perfil." + getFileExtension(fotoDTO.getFoto().getOriginalFilename());

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
            profesor.setFotoUrl("/img/perfiles/" + filename);
            profesorRepository.save(profesor); // Guardar la entidad actualizada

            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada con éxito.");

        } catch (Exception e) {
            System.err.println("Error al guardar la foto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error interno al subir la foto.");
        }

        return "redirect:/profesor/perfil/" + profesorId;
    }

    // Método auxiliar para obtener la extensión del archivo
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // Devuelve default si no hay extensión
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
    
    // --- MÉTODOS DE AVISOS ---
    
    @GetMapping("/perfil/{id}/avisos") // RUTA CORREGIDA
    public String verAvisos(@PathVariable Long id, Model model) {
        Optional<Profesor> profesorOpt = profesorRepository.findById(id);
        if (profesorOpt.isPresent()) {
            Profesor profesor = profesorOpt.get();
            model.addAttribute("profesor", profesor);
            model.addAttribute("profesorId", id);
            
            // 1. Obtener los cursos del profesor para el modal de creación
            model.addAttribute("cursosProfesor", cursoRepository.findByProfesorId(id));

            // 2. Obtener los avisos publicados por este profesor
            List<Aviso> avisos = avisoRepository.findByProfesorIdOrderByFechaPublicacionDesc(id);
            model.addAttribute("avisos", avisos);

            return "Profesor/avisos";
        }
        return "redirect:/login"; 
    }
    
    @PostMapping("/perfil/{id}/avisos/crear") // RUTA CORREGIDA
    public String crearAviso(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam String contenido,
            @RequestParam(required = false) Long cursoId,
            RedirectAttributes redirectAttributes) {

        Optional<Profesor> profesorOpt = profesorRepository.findById(id);
        if (!profesorOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Error: Profesor no encontrado.");
            return "redirect:/profesor/perfil/" + id + "/avisos";
        }
        
        Profesor profesor = profesorOpt.get();
        Curso curso = null;
        if (cursoId != null) {
            curso = cursoRepository.findById(cursoId).orElse(null);
        }
        
        Aviso nuevoAviso = new Aviso();
        nuevoAviso.setTitulo(titulo);
        nuevoAviso.setContenido(contenido);
        nuevoAviso.setProfesor(profesor);
        nuevoAviso.setCurso(curso);
        avisoRepository.save(nuevoAviso);
        
        redirectAttributes.addFlashAttribute("mensaje", "¡Aviso publicado exitosamente!");
        return "redirect:/profesor/perfil/" + id + "/avisos";
    }
}