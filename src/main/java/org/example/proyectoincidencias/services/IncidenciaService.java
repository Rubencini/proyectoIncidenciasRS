package org.example.proyectoincidencias.services;

import lombok.RequiredArgsConstructor;
import org.example.proyectoincidencias.dto.IncidenciaRequest;
import org.example.proyectoincidencias.dto.IncidenciaResponse;
import org.example.proyectoincidencias.entities.Incidencia;
import org.example.proyectoincidencias.entities.Usuario;
import org.example.proyectoincidencias.exception.IncidenciaNotFoundException;
import org.example.proyectoincidencias.repositories.IncidenciaRepository;
import org.example.proyectoincidencias.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaResponse crear(IncidenciaRequest request, Authentication authentication) {
        // El autor sale de la autenticación (el email del token), nunca de lo que envía el cliente
        Usuario autor = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo(request.titulo());
        incidencia.setDescripcion(request.descripcion());
        incidencia.setAutor(autor);
        // estado y fecha de creación toman su valor inicial en la entidad

        return IncidenciaResponse.from(incidenciaRepository.save(incidencia));
    }

    public List<IncidenciaResponse> listar(Authentication authentication) {
        List<Incidencia> incidencias;

        if (esPersonal(authentication)) {
            incidencias = incidenciaRepository.findAll();
        } else {
            incidencias = incidenciaRepository.findByAutorEmail(authentication.getName());
        }

        return incidencias.stream()
                .map(IncidenciaResponse::from)
                .toList();
    }

    public IncidenciaResponse obtener(Long id, Authentication authentication) {
        Optional<Incidencia> incidencia;

        if (esPersonal(authentication)) {
            incidencia = incidenciaRepository.findById(id);
        } else {
            // Un USER solo encuentra la incidencia si además es suya
            incidencia = incidenciaRepository.findByIdAndAutorEmail(id, authentication.getName());
        }

        return IncidenciaResponse.from(
                incidencia.orElseThrow(() -> new IncidenciaNotFoundException(id))
        );
    }

    // TECNICO y ADMIN pueden ver incidencias de cualquier autor
    private boolean esPersonal(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO")
                        || a.getAuthority().equals("ROLE_ADMIN"));
    }
}
