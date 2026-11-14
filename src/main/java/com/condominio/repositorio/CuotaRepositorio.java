package com.condominio.repositorio;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CuotaRepositorio extends JpaRepository<CuotaMantenimiento, Long> {
    List<CuotaMantenimiento> findByEstado(String estado);
    List<CuotaMantenimiento> findByDepartamentoId(Long departamentoId);
    long countByDepartamentoId(Long departamentoId);

    @Query("SELECT DISTINCT c FROM CuotaMantenimiento c LEFT JOIN FETCH c.departamento d LEFT JOIN FETCH d.torre")
    List<CuotaMantenimiento> findAllConRelaciones();
}
