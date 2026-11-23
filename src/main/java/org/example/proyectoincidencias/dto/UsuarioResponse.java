package org.example.proyectoincidencias.dto;

import org.example.proyectoincidencias.entities.Usuario;

public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String roles,
        boolean enabled
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRoles(),
                usuario.isEnabled()
        );
    }
}
