package com.condominio.repositorio;

import com.condominio.modelo.financiero.Multa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultaRepositorio extends JpaRepository<Multa, Long> {
    List<Multa> findByEstado(String estado);
    List<Multa> findByDepartamentoId(Long departamentoId);
}
