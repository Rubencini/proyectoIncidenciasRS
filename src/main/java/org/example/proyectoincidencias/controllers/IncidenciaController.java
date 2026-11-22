package org.example.proyectoincidencias.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.proyectoincidencias.dto.IncidenciaRequest;
import org.example.proyectoincidencias.dto.IncidenciaResponse;
import org.example.proyectoincidencias.services.IncidenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}