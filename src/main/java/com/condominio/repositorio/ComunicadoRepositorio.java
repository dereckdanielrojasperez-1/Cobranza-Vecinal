package com.condominio.repositorio;

import com.condominio.modelo.operaciones.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicadoRepositorio extends JpaRepository<Comunicado, Long> {
    List<Comunicado> findAllByOrderByFechaPublicacionDesc();
}
