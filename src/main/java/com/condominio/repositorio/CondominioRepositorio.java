package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Condominio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CondominioRepositorio extends JpaRepository<Condominio, Long> {
    Optional<Condominio> findByNombre(String nombre);
}
