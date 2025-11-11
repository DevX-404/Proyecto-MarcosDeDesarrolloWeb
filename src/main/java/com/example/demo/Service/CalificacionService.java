package com.example.demo.Service;

import com.example.demo.DTO.CalificacionDTO;
import com.example.demo.DTO.NotasDetalleDTO;
import com.example.demo.Model.Alumno;
import com.example.demo.Model.Tarea;
import com.example.demo.Model.Curso;
import com.example.demo.Model.Calificacion; 
import com.example.demo.Repository.CalificacionRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalificacionService {

    @Autowired
    private CalificacionRepository calificacionRepository; 
    
    @Autowired 
    private TareaService tareaService;

    @Autowired 
    private CursoService cursoService; 
    
    @Autowired 
    private AlumnoService alumnoService;

    // MÉTODO 1: USADO POR CalificacionController para la vista de calificación del profesor.
    public List<CalificacionDTO> getCalificacionesForTarea(Long tareaId) {
        
        Tarea tarea = tareaService.obtenerTareaPorId(tareaId)
                                      .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        Long cursoId = tarea.getCurso().getId();

        // Usamos CursoService para obtener la lista de alumnos
        List<Alumno> alumnos = cursoService.findAlumnosMatriculadosByCursoId(cursoId); 
        
        List<Calificacion> calificacionesExistentes = calificacionRepository.findByTareaId(tareaId);
        
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
    
    // MÉTODO 2: USADO PARA GUARDAR NOTAS DEL PROFESOR
    public void saveCalificaciones(List<CalificacionDTO> calificacionesList) {
        calificacionesList.forEach(dto -> {
            if (dto.getNota() != null && dto.getNota() >= 0) {
                
                Calificacion calificacion = calificacionRepository.findByTareaIdAndAlumnoId(dto.getTareaId(), dto.getAlumnoId())
                        .orElseGet(Calificacion::new);

                Tarea tarea = tareaService.obtenerTareaPorId(dto.getTareaId())
                                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
                Alumno alumno = alumnoService.getAlumnoById(dto.getAlumnoId());
                                
                calificacion.setTarea(tarea); 
                calificacion.setAlumno(alumno); 
                calificacion.setNota(dto.getNota());
                calificacion.setComentario(dto.getComentario());

                calificacionRepository.save(calificacion);
            }
        });
    }

    // MÉTODO 3: USADO POR AlumnoController para la vista "Mis Notas"
    public NotasDetalleDTO getNotasDetalladasByAlumnoAndCursoId(Long alumnoId, Long cursoId) {
        
        Curso curso = cursoService.obtenerCursoPorId(cursoId)
                                  .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + cursoId));

        // 1. OBTENER Y FILTRAR TAREAS: Solo Práctica y Examen
        List<Tarea> tareas = tareaService.listarTareasPorCurso(cursoId);
        List<Tarea> evaluacionesFiltradas = tareas.stream()
            .filter(t -> "Práctica".equals(t.getTipo()) || "Examen".equals(t.getTipo()))
            .collect(Collectors.toList());
        
        // 2. PROCESAR NOTAS Y PREPARAR DATOS BIMESTRALES
        List<Map<String, Object>> evaluaciones = new ArrayList<>();
        
        Map<String, List<Double>> notasPorBimestre = new java.util.HashMap<>();
        String[] nombresBimestres = {"Bimestre 1", "Bimestre 2", "Bimestre 3", "Bimestre 4"};
        
        for (int i = 0; i < evaluacionesFiltradas.size(); i++) {
            Tarea tarea = evaluacionesFiltradas.get(i);
            String bimestreActual = nombresBimestres[i / 4]; 

            Calificacion calificacion = calificacionRepository.findByTareaIdAndAlumnoId(tarea.getId(), alumnoId)
                .orElse(null);
            
            Double nota = (calificacion != null && calificacion.getNota() != null) ? calificacion.getNota() : null;
            String estado = (nota != null) ? "Calificado" : "Pendiente";
            
            if (nota != null) {
                notasPorBimestre.computeIfAbsent(bimestreActual, k -> new ArrayList<>()).add(nota);
            }

            evaluaciones.add(Map.of(
                "titulo", tarea.getTitulo(),
                "tipo", tarea.getTipo(),
                "bimestre", bimestreActual, 
                "nota", nota != null ? String.format("%.1f", nota) : "--",
                "estado", estado
            ));
        }
        
        // 3. CALCULAR PROMEDIOS BIMESTRALES
        double sumaPromediosBimestrales = 0.0;
        int bimestresContados = 0;
        List<Map<String, Object>> resumenBimestral = new ArrayList<>();

        for (String nombreBimestre : nombresBimestres) {
            List<Double> notas = notasPorBimestre.getOrDefault(nombreBimestre, List.of());
            String promedioTexto = "--";

            if (!notas.isEmpty()) {
                double promedioBimestre = notas.stream().mapToDouble(Double::doubleValue).sum() / notas.size();
                promedioTexto = String.format("%.1f", promedioBimestre);
                
                sumaPromediosBimestrales += promedioBimestre;
                bimestresContados++;
            }

            resumenBimestral.add(Map.of(
                "nombre", nombreBimestre,
                "promedio", promedioTexto
            ));
        }

        // 4. CALCULAR PROMEDIO FINAL
        String promedioFinal = "--";
        if (bimestresContados > 0) {
            promedioFinal = String.format("%.1f", sumaPromediosBimestrales / bimestresContados);
        }

        // 5. MAPEAR AL DTO FINAL
        NotasDetalleDTO dto = new NotasDetalleDTO();
        dto.setCursoId(curso.getId());
        dto.setCursoNombre(curso.getNombre());
        dto.setProfesorNombre(curso.getProfesor().getNombre());
        dto.setModalidad(curso.getModalidad());
        
        dto.setEvaluaciones(evaluaciones); 
        dto.setResumenBimestral(resumenBimestral); 
        
        dto.setPromedioFinal(promedioFinal); 

        dto.setFormulaTexto("((B1_E1 + B1_E2 + B1_E3 + B1_E4)/4 + ... + (B4_E1 + B4_E2 + B4_E3 + B4_E4)/4) / 4 Bimestres"); 
        
        return dto;
    }
}