package com.example.demo.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EntregaDTO {

    @NotNull(message = "Debe subir un archivo para realizar la entrega.")
    private MultipartFile archivoEntrega;
}