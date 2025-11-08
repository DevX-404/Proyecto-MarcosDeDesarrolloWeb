package com.example.demo.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// CLASE WRAPPER para el formulario masivo
@Data
@NoArgsConstructor
public class CalificacionListWrapper {
    private List<CalificacionDTO> calificacionesList;

    public CalificacionListWrapper(List<CalificacionDTO> calificacionesList) {
        this.calificacionesList = calificacionesList;
    }
}
