package com.condominio.repositorio;

import com.condominio.modelo.personas.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepositorio extends JpaRepository<Mascota, Long> {
    List<Mascota> findByDepartamentoId(Long departamentoId);
}
