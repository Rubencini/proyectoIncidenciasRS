package org.example.proyectoincidencias.dto;

import org.example.proyectoincidencias.entities.EstadoIncidencia;
import org.example.proyectoincidencias.entities.Incidencia;

import java.time.LocalDateTime;

public record IncidenciaResponse(
        Long id,
        String titulo,
        String descripcion,
        LocalDateTime fechaCreacion,
        EstadoIncidencia estado,
        String autorEmail,
        String autorNombre
) {

    // Convierte una entidad Incidencia en este DTO
    public static IncidenciaResponse from(Incidencia incidencia) {
        return new IncidenciaResponse(
                incidencia.getId(),
                incidencia.getTitulo(),
                incidencia.getDescripcion(),
                incidencia.getFechaCreacion(),
                incidencia.getEstado(),
                incidencia.getAutor().getEmail(),
                incidencia.getAutor().getNombre()
        );
    }
}