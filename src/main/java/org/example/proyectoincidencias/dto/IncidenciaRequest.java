package org.example.proyectoincidencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

    public record IncidenciaRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 255, message = "El título no puede superar los 255 caracteres")
            String titulo,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
            String descripcion
    ) {}

