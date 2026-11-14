package com.condominio.patrones;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.patrones.command.ComandoCobranza;
import com.condominio.patrones.command.InvocadorCobranza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    @DisplayName("Debe ejecutar un comando de cobranza y apilarlo en el historial del invocador")
    public void debeEjecutarComandoRegistrarPago() {
        InvocadorCobranza invocador = new InvocadorCobranza();
        CuotaMantenimiento cuota = CuotaMantenimiento.builder()
                .id(1L)
                .montoBase(300.0)
                .montoTotal(300.0)
                .estado("PENDIENTE")
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        ComandoCobranza comando = new ComandoCobranza() {
            private final double montoPagado = 100.0;
            private boolean ejecutado = false;

            @Override
            public void ejecutar() {
                cuota.pagar(montoPagado);
                ejecutado = true;
            }

            @Override
            public void deshacer() {
                if (ejecutado) {
                    cuota.setMontoTotal(cuota.getMontoTotal() + montoPagado);
                    cuota.setEstado("PENDIENTE");
                    ejecutado = false;
                }
            }
        };

        invocador.ejecutarComando(comando);

        assertEquals(200.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PARCIAL", cuota.getEstado());
        assertEquals(1, invocador.cantidadComandos());
        assertTrue(invocador.tieneComandosPorDeshacer());
    }

    @Test
    @DisplayName("Debe revertir (deshacer) la última operación de pago restaurando saldo y estado")
    public void debeDeshacerComandoRegistrarPago() {
        InvocadorCobranza invocador = new InvocadorCobranza();
        CuotaMantenimiento cuota = CuotaMantenimiento.builder()
                .id(2L)
                .montoBase(250.0)
                .montoTotal(250.0)
                .estado("PENDIENTE")
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        double montoPago = 250.0;
        ComandoCobranza comando = new ComandoCobranza() {
            @Override
            public void ejecutar() {
                cuota.pagar(montoPago);
            }

            @Override
            public void deshacer() {
                cuota.setMontoTotal(250.0);
                cuota.setEstado("PENDIENTE");
            }
        };

        // 1. Ejecutar pago
        invocador.ejecutarComando(comando);
        assertEquals(0.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PAGADO", cuota.getEstado());

        // 2. Deshacer pago
        boolean deshecho = invocador.deshacerUltimoComando();
        assertTrue(deshecho);
        assertEquals(250.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PENDIENTE", cuota.getEstado());
        assertEquals(0, invocador.cantidadComandos());
        assertFalse(invocador.tieneComandosPorDeshacer());
    }
}
