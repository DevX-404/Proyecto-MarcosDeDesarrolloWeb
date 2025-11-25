package com.example.demo.Controller;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Aviso;
import com.example.demo.Model.Calificacion;
import com.example.demo.Model.Curso;
import com.example.demo.Model.Matricula;
import com.example.demo.Model.Profesor;
import com.example.demo.Model.Tarea;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Repository.CursoRepository;
import com.example.demo.Repository.MatriculaRepository;
import com.example.demo.Repository.ProfesorRepository;
import com.example.demo.Repository.TareaRepository;
import com.example.demo.Repository.CalificacionRepository;
import com.example.demo.Repository.AvisoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller 
public class AuthController {

    @Autowired
    private ProfesorRepository profesorRepository;

    @GetMapping("/login") 
    public String showLoginForm() {
        return "login"; 
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
            @RequestParam String password,
            Model model) {

        Optional<Profesor> optionalProfesor = profesorRepository.findByCodigoProfesor(username);
        if (optionalProfesor.isPresent() && password.equals(optionalProfesor.get().getPassword())) {
            return "redirect:/profesor/cursos/" + optionalProfesor.get().getId();
        }
        
        Optional<Alumno> optionalAlumno = alumnoRepository.findByCodigoAlumno(username);
        if (optionalAlumno.isPresent() && password.equals(optionalAlumno.get().getPassword())) {
             return "redirect:/alumno/cursos/" + optionalAlumno.get().getId();
        }
        
        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private CursoRepository cursoRepository; 
    @Autowired
    private AvisoRepository avisoRepository;
    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private CalificacionRepository calificacionRepository;

    @GetMapping("/initdata")
    public String initData() {
        // 1. OBTENER/CREAR Profesor (p)
        Profesor p = profesorRepository.findByCodigoProfesor("P001").orElseGet(() -> {
            Profesor newP = new Profesor();
            newP.setNombreCompleto("Profesora Luz Teresa Morales Vega");
            newP.setCodigoProfesor("P001");
            newP.setCorreo("luz.morales@inst.edu");
            newP.setPassword("1234"); // Contraseña inicial
            return profesorRepository.save(newP);
        });

        // 2. OBTENER/CREAR Alumnos (a1, a2, a3)
        Alumno a1 = alumnoRepository.findByCodigoAlumno("U001").orElseGet(() -> {
            Alumno newA = new Alumno();
            newA.setNombreCompleto("Ximena Burga Mendo");
            newA.setCodigoAlumno("U001");
            newA.setCorreo("xime@inst.edu");
            newA.setPassword("1234");
            return alumnoRepository.save(newA);
        });
        Alumno a2 = alumnoRepository.findByCodigoAlumno("U002").orElseGet(() -> {
            Alumno newA = new Alumno();
            newA.setNombreCompleto("Luis Bances Oliden");
            newA.setCodigoAlumno("U002");
            newA.setCorreo("luis@inst.edu");
            newA.setPassword("1234");
            return alumnoRepository.save(newA);
        });
        Alumno a3 = alumnoRepository.findByCodigoAlumno("U003").orElseGet(() -> {
            Alumno newA = new Alumno();
            newA.setNombreCompleto("Antony Quispe Rodas");
            newA.setCodigoAlumno("U003");
            newA.setCorreo("anto@inst.edu");
            newA.setPassword("1234");
            return alumnoRepository.save(newA);
        });

        // 3. OBTENER/CREAR Cursos (c1, c2)
        Curso c1 = cursoRepository.findByCodigo("12345").orElseGet(() -> {
            Curso newC = new Curso();
            newC.setNombre("Marcos de Desarrollo Web");
            newC.setCodigo("12345");
            newC.setModalidad("Presencial");
            newC.setProfesor(p);
            return cursoRepository.save(newC);
        });

        Curso c2 = cursoRepository.findByCodigo("202020").orElseGet(() -> {
            Curso newC = new Curso();
            newC.setNombre("Algoritmos y Estructura de Datos");
            newC.setCodigo("202020");
            newC.setModalidad("Presencial");
            newC.setProfesor(p);
            return cursoRepository.save(newC);
        });

        // 4. Crear MATRÍCULAS (Solo si no existen)
        if (matriculaRepository.count() == 0) {
            // CURSO 1: Ximena y Luis
            Matricula m1 = new Matricula(); m1.setCurso(c1); m1.setAlumno(a1); matriculaRepository.save(m1);
            Matricula m2 = new Matricula(); m2.setCurso(c1); m2.setAlumno(a2); matriculaRepository.save(m2);

            // CURSO 2: Antony
            Matricula m3 = new Matricula(); m3.setCurso(c2); m3.setAlumno(a3); matriculaRepository.save(m3);
        }
        
        // 5. Crear TAREAS
        Tarea t1 = tareaRepository.findById(1L).orElseGet(() -> {
            // Tarea VENCIDA para NOTAS: Fecha limite HACE 5 DÍAS
            Tarea newT = new Tarea();
            newT.setTitulo("Examen Parcial de POO (VENCIDO)");
            newT.setDescripcion("Prueba de polimorfismo y herencia.");
            newT.setFechaLimite(LocalDate.now().minusDays(5)); // <--- Tarea VENCIDA
            newT.setTipo("Examen");
            newT.setCurso(c1);
            return tareaRepository.save(newT);
        });

        Tarea t2 = tareaRepository.findById(2L).orElseGet(() -> {
            // Tarea FUTURA para TAREAS: Fecha limite DENTRO DE 5 DÍAS
            Tarea newT = new Tarea();
            newT.setTitulo("Laboratorio 3: Interfaces (FUTURO)");
            newT.setDescripcion("Implementación de interfaces y clases abstractas.");
            newT.setFechaLimite(LocalDate.now().plusDays(5)); // <--- Tarea FUTURA
            newT.setTipo("Práctica");
            newT.setCurso(c1);
            return tareaRepository.save(newT);
        });

        // 6. Crear CALIFICACIÓN DE PRUEBA (Simula que Juan ya entregó y está pendiente de nota)
        if (calificacionRepository.count() == 0) {
            // Juan entrega el examen (t1) y está pendiente de calificación
            Calificacion calJuan = new Calificacion();
            calJuan.setTarea(t1);
            calJuan.setAlumno(a1);
            calJuan.setNota(null); // Esto ya no fallará si la columna es nullable
            calificacionRepository.save(calJuan);
        }

        if (p != null && avisoRepository.count() == 0) {
            
            // 1. Aviso general del profesor
            Aviso aviso1 = new Aviso();
            aviso1.setTitulo("Inicio de Semestre");
            aviso1.setContenido("Bienvenidos al nuevo periodo académico. Revisen sus horarios.");
            aviso1.setProfesor(p); 
            avisoRepository.save(aviso1);

            // 2. Aviso para un curso específico
            if (c1 != null) {
                Aviso aviso2 = new Aviso();
                aviso2.setTitulo("Tarea 3 de Álgebra");
                aviso2.setContenido("Recordatorio: La Tarea #3 de Álgebra se cierra mañana.");
                aviso2.setProfesor(p);
                aviso2.setCurso(c1);
                avisoRepository.save(aviso2);
            }
            
            // 3. Aviso para otro curso
            if (c2 != null) {
                Aviso aviso3 = new Aviso();
                aviso3.setTitulo("Clase de Biología suspendida");
                aviso3.setContenido("La clase de hoy de Biología se pospone.");
                aviso3.setProfesor(p);
                aviso3.setCurso(c2);
                avisoRepository.save(aviso3);
            }
        }
        
        return "redirect:/login";
    }
}

