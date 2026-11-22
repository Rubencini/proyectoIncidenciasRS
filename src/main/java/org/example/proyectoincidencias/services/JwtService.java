package org.example.proyectoincidencias.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.proyectoincidencias.entities.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long jwtExpiration) {
        // Clave fija leída de las propiedades: los tokens siguen valiendo tras reiniciar
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(Authentication authentication) {
        // Tras un login correcto, el principal es el Usuario que devolvió CustomUserDetailsService
        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Solo los roles (los que empiezan por ROLE_), como lista
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        return Jwts.builder()
                .subject(usuario.getEmail())            // quién es el usuario
                .issuer("gestion-centro-api")           // quién emite el token
                .issuedAt(new Date())                   // cuándo se creó
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claim("uid", usuario.getId())          // identificador del usuario
                .claim("roles", roles)                  // roles, como lista
                .claim("nombre", usuario.getNombre())   // claim adicional no sensible
                .signWith(secretKey)                    // firma con la clave
                .compact();                             // lo convierte en el texto final
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}