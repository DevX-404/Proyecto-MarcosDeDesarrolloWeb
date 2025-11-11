package com.example.demo.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FotoDTO {
    @NotNull(message = "Debe seleccionar un archivo de imagen.")
    private MultipartFile foto;
}