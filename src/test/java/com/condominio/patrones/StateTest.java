package com.condominio.patrones;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.patrones.state.EstadoPagado;
import com.condominio.patrones.state.EstadoParcial;
import com.condominio.patrones.state.EstadoPendiente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class StateTest {

    private CuotaMantenimiento crearCuotaEjemplo(double monto) {
        return CuotaMantenimiento.builder()
                .id(1L)
                .montoBase(monto)
                .montoTotal(monto)
                .fechaEmision(LocalDate.now().minusDays(5))
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .estado("PENDIENTE")
                .build();
    }

    @Test
    @DisplayName("Debe transicionar de PENDIENTE a PARCIAL al realizar un pago inferior al saldo")
    public void debeTransicionarDePendienteAParcial() {
        CuotaMantenimiento cuota = crearCuotaEjemplo(200.0);

        cuota.pagar(80.0);

        assertEquals(120.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PARCIAL", cuota.getEstado());
        assertInstanceOf(EstadoParcial.class, cuota.getEstadoLogico());
    }

    @Test
    @DisplayName("Debe transicionar de PARCIAL a PAGADO al liquidar la totalidad de la deuda")
    public void debeTransicionarDeParcialAPagado() {
        CuotaMantenimiento cuota = crearCuotaEjemplo(200.0);

        // Primer pago: pasa a PARCIAL
        cuota.pagar(50.0);
        assertEquals("PARCIAL", cuota.getEstado());
        assertInstanceOf(EstadoParcial.class, cuota.getEstadoLogico());

        // Segundo pago liquidando el resto: pasa a PAGADO
        cuota.pagar(150.0);
        assertEquals(0.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PAGADO", cuota.getEstado());
        assertInstanceOf(EstadoPagado.class, cuota.getEstadoLogico());
    }

    @Test
    @DisplayName("Debe rechazar un pago sobre una cuota que ya se encuentra en estado PAGADO")
    public void debeRechazarPagoEnEstadoPagado() {
        CuotaMantenimiento cuota = crearCuotaEjemplo(100.0);
        cuota.pagar(100.0); // Liquidada

        assertEquals("PAGADO", cuota.getEstado());
        assertInstanceOf(EstadoPagado.class, cuota.getEstadoLogico());

        IllegalStateException excepcion = assertThrows(IllegalStateException.class, () -> {
            cuota.pagar(20.0);
        });

        assertTrue(excepcion.getMessage().contains("totalmente pagada"));
    }
}
