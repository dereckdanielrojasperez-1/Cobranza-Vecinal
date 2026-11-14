package com.condominio.patrones;

import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.patrones.adapter.AdaptadorSistemaLegado;
import com.condominio.patrones.adapter.NuevoFormatoPago;
import com.condominio.patrones.adapter.SistemaLegado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Estructural Adapter (Semana 8)
public class AdapterTest {

    @Test
    @DisplayName("Debe adaptar la transaccion del sistema legado al formato de Pago del nuevo sistema")
    public void debeAdaptarPagoLegado() {
        SistemaLegado sistemaLegadoSimulado = (codigoInmueble, valorMonetario, fechaTexto) ->
                "FOLIO-LEGADO-" + codigoInmueble + "-S/" + valorMonetario;

        NuevoFormatoPago adaptador = new AdaptadorSistemaLegado(sistemaLegadoSimulado);
        Departamento depto = Departamento.builder().id(101L).numero("A-101").build();

        Pago pagoResultado = adaptador.procesarPago(999L, depto, 250.0, "EFECTIVO");

        assertNotNull(pagoResultado);
        assertEquals(999L, pagoResultado.getId());
        assertEquals(250.0, pagoResultado.getMonto());
        assertEquals(depto, pagoResultado.getDepartamento());
        assertTrue(pagoResultado.getMetodoPago().contains("ADAPTADO: FOLIO-LEGADO-A-101-S/250.0"));
    }
}
