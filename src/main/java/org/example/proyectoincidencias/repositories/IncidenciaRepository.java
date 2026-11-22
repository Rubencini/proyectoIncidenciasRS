package org.example.proyectoincidencias.repositories;

import org.example.proyectoincidencias.entities.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByAutorEmail(String email);

    Optional<Incidencia> findByIdAndAutorEmail(Long id, String email);
}
