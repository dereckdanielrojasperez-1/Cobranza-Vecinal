package com.condominio.repositorio;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.inmuebles.Departamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de integración sobre CuotaRepositorio contra base de datos real H2 (Punto 6.1).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
public class CuotaRepositoryIntegrationTest {

    @Autowired
    private CuotaRepositorio cuotaRepositorio;

    @Autowired
    private DepartamentoRepositorio departamentoRepositorio;

    @Test
    @DisplayName("6.1.1 Búsqueda de cuotas por estado en base de datos H2")
    void testBuscarCuotasPorEstado() {
        List<CuotaMantenimiento> pagadas = cuotaRepositorio.findByEstado("PAGADO");
        assertNotNull(pagadas, "La lista de cuotas pagadas no debe ser nula");
        assertFalse(pagadas.isEmpty(), "Debe existir al menos una cuota pagada en el seed");

        for (CuotaMantenimiento c : pagadas) {
            assertEquals("PAGADO", c.getEstado(), "El estado debe coincidir exactamente");
        }
    }

    @Test
    @DisplayName("6.1.2 Guardado y persistencia de una nueva cuota de mantenimiento")
    void testGuardarNuevaCuota() {
        Departamento depto = departamentoRepositorio.findByNumero("101").orElse(null);
        assertNotNull(depto, "El departamento 101 debe existir para asociar la cuota");

        CuotaMantenimiento nuevaCuota = CuotaMantenimiento.builder()
                .montoBase(220.0)
                .montoTotal(220.0)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(20))
                .estado("PENDIENTE")
                .departamento(depto)
                .build();

        CuotaMantenimiento guardada = cuotaRepositorio.save(nuevaCuota);
        assertNotNull(guardada.getId(), "El ID debe ser generado por la secuencia JPA");
        assertTrue(guardada.getId() > 0, "El ID generado debe ser positivo");
        assertEquals("PENDIENTE", guardada.getEstado());
    }

    @Test
    @DisplayName("6.1.3 Actualización de saldo y amortización de cuota")
    void testActualizarSaldoCuota() {
        List<CuotaMantenimiento> pendientes = cuotaRepositorio.findByEstado("PENDIENTE");
        assertFalse(pendientes.isEmpty(), "Debe existir al menos una cuota pendiente");

        CuotaMantenimiento cuota = pendientes.get(0);
        double saldoInicial = cuota.getMontoTotal();

        cuota.setMontoTotal(saldoInicial - 50.0);
        CuotaMantenimiento actualizada = cuotaRepositorio.save(cuota);

        assertEquals(saldoInicial - 50.0, actualizada.getMontoTotal(), 0.001);
    }
}
