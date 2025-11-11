package com.example.demo.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class NotasDetalleDTO {

    private Long cursoId;
    private String cursoNombre;
    private String profesorNombre;
    private String modalidad;
  // Lista de Evaluaciones (tareas filtradas)
    private List<Map<String, Object>> evaluaciones; 

     // Campo que causaba el error
    private List<Map<String, Object>> resumenBimestral; 

    // Promedio Final
    private String promedioFinal = "--";
    
    // Fórmula de ejemplo (Texto plano para mostrar)
    private String formulaTexto;
    
    public void setResumenBimestral(List<Map<String, Object>> resumenBimestral) {
        this.resumenBimestral = resumenBimestral;
    }
}
