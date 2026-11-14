package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.operaciones.ReservaArea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de integración sobre ReservaAreaRepositorio contra base de datos real H2 (Punto 6.1).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
public class ReservaRepositoryIntegrationTest {

    @Autowired
    private ReservaAreaRepositorio reservaAreaRepositorio;

    @Autowired
    private AreaComunRepositorio areaComunRepositorio;

    @Test
    @DisplayName("6.1.9 Inserción de nueva reserva de área común")
    void testInsertarReserva() {
        List<AreaComun> areas = areaComunRepositorio.findAll();
        AreaComun area = !areas.isEmpty() ? areas.get(0) : null;

        ReservaArea nuevaReserva = ReservaArea.builder()
                .fechaReserva(LocalDate.now().plusDays(5))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(18, 0))
                .estado("CONFIRMADA")
                .areaComun(area)
                .build();

        ReservaArea guardada = reservaAreaRepositorio.save(nuevaReserva);
        assertNotNull(guardada.getId(), "El ID de la reserva debe ser generado");
        assertEquals("CONFIRMADA", guardada.getEstado());
    }

    @Test
    @DisplayName("6.1.10 Consulta de reservas por estado")
    void testConsultarReservasPorEstado() {
        List<ReservaArea> confirmadas = reservaAreaRepositorio.findByEstado("CONFIRMADA");
        assertNotNull(confirmadas, "La lista no debe ser nula");
        for (ReservaArea r : confirmadas) {
            assertEquals("CONFIRMADA", r.getEstado());
        }
    }

    @Test
    @DisplayName("6.1.11 Consulta de reservas por fecha")
    void testConsultarReservasPorFecha() {
        LocalDate hoy = LocalDate.now();
        List<ReservaArea> deHoy = reservaAreaRepositorio.findByFechaReserva(hoy);
        assertNotNull(deHoy, "La lista por fecha no debe ser nula");
    }
}
