package com.condominio.repositorio;

import com.condominio.modelo.personas.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropietarioRepositorio extends JpaRepository<Propietario, Long> {
    Optional<Propietario> findByDni(String dni);
    boolean existsByDni(String dni);
}
