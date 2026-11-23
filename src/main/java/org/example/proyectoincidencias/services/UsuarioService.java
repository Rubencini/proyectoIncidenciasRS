package org.example.proyectoincidencias.services;

import lombok.RequiredArgsConstructor;
import org.example.proyectoincidencias.dto.UsuarioResponse;
import org.example.proyectoincidencias.entities.Rol;
import org.example.proyectoincidencias.entities.Usuario;
import org.example.proyectoincidencias.exception.UsuarioNotFoundException;
import org.example.proyectoincidencias.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse cambiarRol(Long id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuario.setRoles(nuevoRol.name()); // name() devuelve el texto del enum: "ROLE_TECNICO"

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }
}