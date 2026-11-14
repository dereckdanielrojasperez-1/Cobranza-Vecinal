package com.condominio.repositorio;

import com.condominio.modelo.financiero.GastoComun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GastoComunRepositorio extends JpaRepository<GastoComun, Long> {
    List<GastoComun> findByFechaBetween(LocalDate inicio, LocalDate fin);
}
