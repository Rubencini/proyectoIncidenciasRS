package org.example.proyectoincidencias.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.proyectoincidencias.dto.LoginRequest;
import org.example.proyectoincidencias.dto.RegisterRequest;
import org.example.proyectoincidencias.entities.Usuario;
import org.example.proyectoincidencias.repositories.UsuarioRepository;
import org.example.proyectoincidencias.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Comprueba email y contraseña (usa CustomUserDetailsService y BCrypt por dentro)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            // Si las credenciales son correctas, generamos el token
            String token = jwtService.generateToken(authentication);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Comprobamos que el email no esté ya registrado
        if (usuarioRepository.existsByEmail(registerRequest.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(registerRequest.email());
        usuario.setNombre(registerRequest.nombre());
        usuario.setPassword(passwordEncoder.encode(registerRequest.password())); // cifrada con BCrypt
        usuario.setRoles("ROLE_USER"); // siempre ROLE_USER: el cliente no elige el rol
        usuario.setEnabled(true);

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado correctamente"));
    }
}
