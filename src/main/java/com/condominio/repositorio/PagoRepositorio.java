package com.condominio.repositorio;

import com.condominio.modelo.financiero.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepositorio extends JpaRepository<Pago, Long> {
    List<Pago> findByDepartamentoId(Long departamentoId);

    @Query("SELECT DISTINCT p FROM Pago p LEFT JOIN FETCH p.departamento d LEFT JOIN FETCH d.torre")
    List<Pago> findAllConRelaciones();
}
