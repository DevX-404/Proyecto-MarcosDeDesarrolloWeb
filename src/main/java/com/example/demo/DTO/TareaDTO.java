package com.example.demo.DTO;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TareaDTO {

    private Long id;

    @NotBlank(message = "El título es obligatorio.")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres.")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria.")
    private String descripcion;

    @NotNull(message = "La fecha límite es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "La fecha límite debe ser hoy o en el futuro.")
    private LocalDate fechaLimite;

    @NotBlank(message = "El tipo es obligatorio.")
    private String tipo;

    @NotNull(message = "Debe estar asociado a un curso.")
    private Long cursoId;

    private MultipartFile archivoAdjunto;
    
    private String urlRecurso;

}