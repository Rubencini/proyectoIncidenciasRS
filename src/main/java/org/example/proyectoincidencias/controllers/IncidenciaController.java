package org.example.proyectoincidencias.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.proyectoincidencias.dto.IncidenciaRequest;
import org.example.proyectoincidencias.dto.IncidenciaResponse;
import org.example.proyectoincidencias.services.IncidenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.example.proyectoincidencias.dto.CambioEstadoRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    @PostMapping
    public ResponseEntity<IncidenciaResponse> crear(@Valid @RequestBody IncidenciaRequest request,
                                                    Authentication authentication) {
        IncidenciaResponse creada = incidenciaService.crear(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<IncidenciaResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.listar(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponse> obtener(@PathVariable Long id,
                                                      Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.obtener(id, authentication));
    }
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<IncidenciaResponse> cambiarEstado(@PathVariable Long id,
                                                            @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(incidenciaService.cambiarEstado(id, request.estado()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incidenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}