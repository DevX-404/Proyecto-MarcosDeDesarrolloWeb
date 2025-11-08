package com.example.demo.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionDTO {

    private Long id; 
    
    @NotNull
    private Long tareaId;

    @NotNull
    private Long alumnoId;

    private String nombreAlumno; 

    @NotNull(message = "La nota no puede estar vacía.")
    @DecimalMin(value = "0.0", inclusive = true, message = "La nota debe ser 0.0 o mayor.")
    @DecimalMax(value = "20.0", inclusive = true, message = "La nota máxima es 20.0.")
    private Double nota; 

    private String comentario; 
    
    private String estadoEntrega; 
}