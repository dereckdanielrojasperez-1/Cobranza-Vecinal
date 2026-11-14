package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepositorio extends JpaRepository<Deposito, Long> {
    Optional<Deposito> findByNumero(String numero);
    List<Deposito> findByDepartamentoId(Long departamentoId);
}
