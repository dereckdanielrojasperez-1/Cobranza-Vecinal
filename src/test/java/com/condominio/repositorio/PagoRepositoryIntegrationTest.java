package com.condominio.repositorio;

import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de integración sobre PagoRepositorio contra base de datos real H2 (Punto 6.1).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
public class PagoRepositoryIntegrationTest {

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Autowired
    private DepartamentoRepositorio departamentoRepositorio;

    @Test
    @DisplayName("6.1.4 Persistencia transaccional de pagos")
    void testPersistirPago() {
        Departamento depto = departamentoRepositorio.findByNumero("101").orElse(null);
        assertNotNull(depto, "El departamento 101 debe existir en BD");

        Pago nuevoPago = Pago.builder()
                .monto(150.0)
                .fechaPago(LocalDate.now())
                .metodoPago("TRANSFERENCIA_BCP")
                .departamento(depto)
                .build();

        Pago persistido = pagoRepositorio.save(nuevoPago);
        assertNotNull(persistido.getId(), "El ID del pago debe ser generado automáticamente");
        assertEquals(150.0, persistido.getMonto(), 0.001);
        assertEquals("TRANSFERENCIA_BCP", persistido.getMetodoPago());
    }

    @Test
    @DisplayName("6.1.5 Consulta de pagos por departamento")
    void testConsultarPagosPorDepartamento() {
        Departamento depto = departamentoRepositorio.findByNumero("102").orElse(null);
        assertNotNull(depto, "El departamento 102 debe existir en el seed");

        List<Pago> pagos = pagoRepositorio.findByDepartamentoId(depto.getId());
        assertNotNull(pagos, "La lista de pagos no debe ser nula");
        assertFalse(pagos.isEmpty(), "El departamento 102 con cuota pagada debe poseer al menos un registro de pago");

        for (Pago p : pagos) {
            assertEquals(depto.getId(), p.getDepartamento().getId());
            assertTrue(p.getMonto() > 0, "El monto del pago debe ser mayor a cero");
        }
    }
}
