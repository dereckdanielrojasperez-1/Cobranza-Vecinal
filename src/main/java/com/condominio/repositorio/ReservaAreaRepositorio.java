package com.condominio.repositorio;

import com.condominio.modelo.operaciones.ReservaArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaAreaRepositorio extends JpaRepository<ReservaArea, Long> {
    List<ReservaArea> findByFechaReserva(LocalDate fecha);
    List<ReservaArea> findByAreaComunId(Long areaComunId);
    List<ReservaArea> findByEstado(String estado);

    @Query("SELECT DISTINCT r FROM ReservaArea r LEFT JOIN FETCH r.propietario LEFT JOIN FETCH r.areaComun")
    List<ReservaArea> findAllConRelaciones();
}
