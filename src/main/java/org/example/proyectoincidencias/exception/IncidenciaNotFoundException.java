package org.example.proyectoincidencias.exception;

public class IncidenciaNotFoundException extends RuntimeException {

    public IncidenciaNotFoundException(Long id) {
        super("Incidencia no encontrada: " + id);
    }
}
