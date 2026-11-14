package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.AreaComun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaComunRepositorio extends JpaRepository<AreaComun, Long> {
    Optional<AreaComun> findByNombre(String nombre);
}
