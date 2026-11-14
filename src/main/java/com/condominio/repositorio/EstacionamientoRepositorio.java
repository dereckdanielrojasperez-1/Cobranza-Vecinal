package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Estacionamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstacionamientoRepositorio extends JpaRepository<Estacionamiento, Long> {
    Optional<Estacionamiento> findByNumero(String numero);
    List<Estacionamiento> findByDepartamentoId(Long departamentoId);
}
