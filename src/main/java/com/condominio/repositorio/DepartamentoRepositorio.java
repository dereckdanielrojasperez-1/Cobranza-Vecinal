package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepositorio extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNumero(String numero);
    List<Departamento> findByTorreId(Long torreId);
    List<Departamento> findByPropietarioId(Long propietarioId);
    List<Departamento> findByInquilinoActualId(Long inquilinoId);

    @Query("SELECT DISTINCT d FROM Departamento d LEFT JOIN FETCH d.torre t LEFT JOIN FETCH t.edificio LEFT JOIN FETCH d.propietario LEFT JOIN FETCH d.inquilinoActual")
    List<Departamento> findAllConRelaciones();
}
