package com.example.demo.Service;

import com.example.demo.Model.Tarea;
import com.example.demo.Repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    public List<Tarea> listarTareasPorCurso(Long cursoId) {
        return tareaRepository.findByCursoIdOrderByFechaLimiteAsc(cursoId);
    }

    public Tarea guardarTarea(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id);
    }

    public List<Tarea> getTareasVencidasByCursoId(Long cursoId) {
        // Implementación usando el repositorio de Tarea
        return tareaRepository.findByCursoIdAndFechaLimiteBefore(cursoId, LocalDate.now());
    }
}