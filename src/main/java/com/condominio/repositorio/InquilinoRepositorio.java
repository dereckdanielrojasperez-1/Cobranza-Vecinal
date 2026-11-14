package com.condominio.repositorio;

import com.condominio.modelo.personas.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquilinoRepositorio extends JpaRepository<Inquilino, Long> {
    Optional<Inquilino> findByDni(String dni);
    boolean existsByDni(String dni);
}
