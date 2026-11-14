package com.condominio.repositorio;

import com.condominio.modelo.personas.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitaRepositorio extends JpaRepository<Visita, Long> {
    List<Visita> findByDepartamentoId(Long departamentoId);

    @Query("SELECT DISTINCT v FROM Visita v LEFT JOIN FETCH v.departamento d LEFT JOIN FETCH d.torre")
    List<Visita> findAllConRelaciones();
}
