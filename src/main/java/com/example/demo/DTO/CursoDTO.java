package com.example.demo.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CursoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio") 
    @Size(min = 5, max = 150, message = "El nombre debe tener entre 5 y 150 caracteres")
    private String nombre;

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 5, max = 10, message = "El código debe tener entre 5 y 10 caracteres")
    private String codigo;

    @NotBlank(message = "La modalidad es obligatoria")
    private String modalidad;
    
    // Necesario para relacionar el curso con el profesor
    private Long profesorId = 1L; 
}