package org.example.proyectoincidencias.dto;

import jakarta.validation.constraints.NotNull;
import org.example.proyectoincidencias.entities.Rol;

public record CambioRolRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {}
