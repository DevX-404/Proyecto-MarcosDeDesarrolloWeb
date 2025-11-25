package com.example.demo.Service;

import com.example.demo.Model.Alumno;
import com.example.demo.Model.Calificacion;
import com.example.demo.Model.Matricula;
import com.example.demo.Model.Tarea;
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Repository.CalificacionRepository;
import com.example.demo.Repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private TareaService tareaService;
    
    @Autowired 
    private CalificacionRepository calificacionRepository; 

    public Alumno getAlumnoById(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con ID: " + id));
    }

    // Módulo: Mis Cursos 
    public List<Map<String, Object>> getCursosMatriculadosConProgreso(Long alumnoId) {
        
        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(alumnoId);

        return matriculas.stream()
            .map(matricula -> {
                var curso = matricula.getCurso();
                var profesor = curso.getProfesor();

                int progresoSimulado = 0; 

                return Map.<String, Object>of(
                    "id", curso.getId(),
                    "nombre", curso.getNombre(),
                    "profesorNombre", profesor.getNombreCompleto(), 
                    "progreso", progresoSimulado
                );
            })
            .collect(Collectors.toList());
    }

    // Módulo: Mis Tareas 
    public List<Map<String, Object>> getTareasPorCursoYEstado(Long alumnoId) {
        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(alumnoId);
        
        List<Map<String, Object>> tareasPorCurso = new ArrayList<>();
        
        for (Matricula matricula : matriculas) {
            Long cursoId = matricula.getCurso().getId();
            String cursoNombre = matricula.getCurso().getNombre();
            
            List<Tarea> tareasCurso = tareaService.listarTareasPorCurso(cursoId);

            List<Map<String, Object>> listaTareasMap = tareasCurso.stream()
                .map(tarea -> {
                    String estado = getEstadoTarea(tarea.getId(), alumnoId);
                    
                    Map<String, Object> tareaMap = new HashMap<>();
                    tareaMap.put("id", tarea.getId());
                    tareaMap.put("titulo", tarea.getTitulo());
                    tareaMap.put("tipo", tarea.getTipo());
                    tareaMap.put("fechaLimite", tarea.getFechaLimite());
                    tareaMap.put("descripcion", tarea.getDescripcion());
                    tareaMap.put("urlRecurso", tarea.getUrlRecurso() != null ? tarea.getUrlRecurso() : "");
                    tareaMap.put("nombreAdjunto", tarea.getNombreAdjunto() != null ? tarea.getNombreAdjunto() : "");
                    tareaMap.put("estado", estado);

                    return tareaMap;
                }).collect(Collectors.toList());

            if (!listaTareasMap.isEmpty()) {
                tareasPorCurso.add(Map.of(
                    "cursoNombre", cursoNombre,
                    "tareas", listaTareasMap
                ));
            }
        }
        
        return tareasPorCurso;
    }

    // Método auxiliar para obtener el estado de una tarea específica
    public String getEstadoTarea(Long tareaId, Long alumnoId) {
        Tarea tarea = tareaService.obtenerTareaPorId(tareaId)
                                  .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        Optional<Calificacion> optionalCalificacion = calificacionRepository.findByTareaIdAndAlumnoId(tareaId, alumnoId);
        String estado;

        if (optionalCalificacion.isPresent()) {
            Calificacion calificacion = optionalCalificacion.get();
            if (calificacion.getNota() != null) {
                estado = "Calificada";
            } else {
                // Entregado = true, nota = null (Pendiente de calificación)
                estado = "Entregada"; 
            }
        } else {
            // Si no existe registro de Calificación (No ha entregado)
            if (tarea.getFechaLimite().isBefore(LocalDate.now())) {
                estado = "VENCIDO";
            } else {
                estado = "Pendiente"; 
            }
        }
        return estado;
    }
}