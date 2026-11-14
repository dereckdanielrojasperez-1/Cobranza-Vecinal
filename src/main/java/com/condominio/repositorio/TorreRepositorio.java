package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Torre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TorreRepositorio extends JpaRepository<Torre, Long> {
    Optional<Torre> findByNombre(String nombre);
    Optional<Torre> findByNombreAndEdificioId(String nombre, Long edificioId);
    List<Torre> findByEdificioId(Long edificioId);

    @Query("SELECT DISTINCT t FROM Torre t LEFT JOIN FETCH t.edificio")
    List<Torre> findAllConEdificio();
}
