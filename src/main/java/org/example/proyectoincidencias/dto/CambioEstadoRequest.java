package org.example.proyectoincidencias.dto;

import jakarta.validation.constraints.NotNull;
import org.example.proyectoincidencias.entities.EstadoIncidencia;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoIncidencia estado
) {}
