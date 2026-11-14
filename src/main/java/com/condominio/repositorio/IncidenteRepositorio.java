package com.condominio.repositorio;

import com.condominio.modelo.operaciones.Incidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenteRepositorio extends JpaRepository<Incidente, Long> {
    List<Incidente> findByEstado(String estado);
}
