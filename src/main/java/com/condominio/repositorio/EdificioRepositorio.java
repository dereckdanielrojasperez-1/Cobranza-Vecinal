package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EdificioRepositorio extends JpaRepository<Edificio, Long> {
    Optional<Edificio> findByNombre(String nombre);
}
