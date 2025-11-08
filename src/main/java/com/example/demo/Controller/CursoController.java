package com.example.demo.Controller;

import com.example.demo.DTO.CursoDTO;
import com.example.demo.Model.Curso;
import com.example.demo.Model.Profesor;
import com.example.demo.Repository.ProfesorRepository;
import com.example.demo.Service.CursoService;
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
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private ProfesorRepository profesorRepository;

    // Read: Listar Cursos
    @GetMapping("/cursos/{profesorId}")
    public String listarCursos(@PathVariable Long profesorId, Model model) {
        List<Curso> cursos = cursoService.listarCursosPorProfesor(profesorId);

        model.addAttribute("cursos", cursos);
        model.addAttribute("profesorId", profesorId);
        model.addAttribute("profesorNombre",
                profesorRepository.findById(profesorId).map(Profesor::getNombre).orElse("Profesor"));

        if (!model.containsAttribute("cursoDTO")) {
            model.addAttribute("cursoDTO", new CursoDTO());
        }

        return "Profesor/cursos"; 
    }

    // Create y Update: Crear Curso
    @PostMapping("/cursos/{profesorId}/crear")
    public String crearCurso(@PathVariable Long profesorId,
            @Valid @ModelAttribute("cursoDTO") CursoDTO cursoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) { 

        if (result.hasErrors()) {
            // 1. Envía un flag de error
            redirectAttributes.addFlashAttribute("showModal", true);

            // 2. Envía el objeto DTO con los datos que el usuario ingresó
            redirectAttributes.addFlashAttribute("cursoDTO", cursoDTO);

            // 3. Envía los errores de validación
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.cursoDTO", result);

            // 4. Agrega un mensaje de error flash
            redirectAttributes.addFlashAttribute("error", "Error al crear el curso. Revisa los campos.");

            return "redirect:/profesor/cursos/" + profesorId;
        }

        // Lógica de creación exitosa
        Optional<Profesor> optionalProfesor = profesorRepository.findById(profesorId);

        if (optionalProfesor.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Profesor no encontrado.");
            return "redirect:/profesor/cursos/" + profesorId;
        }

        Curso curso = new Curso();
        curso.setNombre(cursoDTO.getNombre());
        curso.setCodigo(cursoDTO.getCodigo());
        curso.setModalidad(cursoDTO.getModalidad());
        curso.setProfesor(optionalProfesor.get());

        cursoService.guardarCurso(curso);
        redirectAttributes.addFlashAttribute("mensaje", "Curso creado exitosamente!");
        return "redirect:/profesor/cursos/" + profesorId;
    }

    // Delete: Eliminar Curso
    @GetMapping("/cursos/eliminar/{cursoId}/{profesorId}")
    public String eliminarCurso(@PathVariable Long cursoId,
            @PathVariable Long profesorId,
            RedirectAttributes redirectAttributes) {

        cursoService.eliminarCurso(cursoId);
        redirectAttributes.addFlashAttribute("mensaje", "Curso eliminado con éxito.");
        return "redirect:/profesor/cursos/" + profesorId;
    }
}