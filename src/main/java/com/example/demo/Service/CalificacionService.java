package com.example.demo.Service;

import com.example.demo.DTO.CalificacionDTO;
import com.example.demo.Model.Alumno;
import com.example.demo.Model.Tarea;
import com.example.demo.Model.Calificacion; 
import com.example.demo.Repository.AlumnoRepository;
import com.example.demo.Repository.TareaRepository;
import com.example.demo.Repository.CalificacionRepository; 
import com.example.demo.Repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalificacionService {

    @Autowired
    private AlumnoRepository alumnoRepository; 
    
    @Autowired
    private CalificacionRepository calificacionRepository; 
    
    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository; 

    public List<CalificacionDTO> getCalificacionesForTarea(Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId)
                                      .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        Long cursoId = tarea.getCurso().getId();

        // 1. Obtener todos los alumnos matriculados en ese curso
        List<Alumno> alumnos = matriculaRepository.findAlumnosByCursoId(cursoId);
        
        // 2. Obtener calificaciones existentes para esta tarea
        List<Calificacion> calificacionesExistentes = calificacionRepository.findByTareaId(tareaId);
        
        // 3. Mapear a DTOs
        return alumnos.stream().map(alumno -> {
            Calificacion calExistente = calificacionesExistentes.stream()
                .filter(c -> c.getAlumno().getId().equals(alumno.getId())) 
                .findFirst()
                .orElse(null);

            CalificacionDTO dto = new CalificacionDTO();
            dto.setTareaId(tareaId);
            dto.setAlumnoId(alumno.getId());
            dto.setNombreAlumno(alumno.getNombreCompleto()); 
            
            if (calExistente != null) {
                dto.setId(calExistente.getId());
                dto.setNota(calExistente.getNota());
                dto.setComentario(calExistente.getComentario());
                dto.setEstadoEntrega("Calificado"); 
            } else {
                dto.setNota(null); 
                dto.setEstadoEntrega("Pendiente"); 
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public void saveCalificaciones(List<CalificacionDTO> calificacionesList) {
        calificacionesList.forEach(dto -> {
            if (dto.getNota() != null && dto.getNota() >= 0) {
                
                Calificacion calificacion = calificacionRepository.findByTareaIdAndAlumnoId(dto.getTareaId(), dto.getAlumnoId())
                        .orElseGet(Calificacion::new);

                Tarea tarea = tareaRepository.findById(dto.getTareaId())
                                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
                Alumno alumno = alumnoRepository.findById(dto.getAlumnoId())
                                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
                
                calificacion.setTarea(tarea); 
                calificacion.setAlumno(alumno); 
                calificacion.setNota(dto.getNota());
                calificacion.setComentario(dto.getComentario());

                calificacionRepository.save(calificacion);
            }
        });
    }
}