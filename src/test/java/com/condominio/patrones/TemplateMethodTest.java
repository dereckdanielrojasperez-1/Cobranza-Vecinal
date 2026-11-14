package com.condominio.patrones;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.patrones.templatemethod.PlantillaProcesoCobranza;
import com.condominio.patrones.templatemethod.ProcesoCobranzaExtraordinaria;
import com.condominio.patrones.templatemethod.ProcesoCobranzaOrdinaria;
import com.condominio.patrones.templatemethod.ResultadoCobranza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateMethodTest {

    private UsuarioSistema crearUsuarioDemo() {
        return UsuarioSistema.builder()
                .id(1L)
                .usuario("admin")
                .clave("admin123")
                .rol(RolUsuario.ADMINISTRADOR)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe ejecutar el algoritmo plantilla para cobranza ordinaria aplicando 5% de descuento")
    public void debeEjecutarProcesoCobranzaOrdinariaConDescuento() {
        PlantillaProcesoCobranza proceso = new ProcesoCobranzaOrdinaria(5.0);
        UsuarioSistema usuario = crearUsuarioDemo();

        ResultadoCobranza resultado = proceso.procesarCobranza(101L, 200.0, usuario);

        assertNotNull(resultado);
        assertEquals(101L, resultado.getIdCuota());
        assertEquals(200.0, resultado.getMontoBase(), 0.001);
        assertEquals(190.0, resultado.getMontoFinalAjustado(), 0.001); // 200 - 5% = 190
        assertEquals("REC-ORD-00101-2026", resultado.getCodigoComprobante());
        assertEquals("LIQUIDADA_CON_EXITO", resultado.getEstadoOperacion());
        assertTrue(resultado.getTipoCobranza().contains("Ordinaria"));
    }

    @Test
    @DisplayName("Debe ejecutar el algoritmo plantilla para cobranza extraordinaria aplicando 10% de fondo de reserva")
    public void debeEjecutarProcesoCobranzaExtraordinariaConFondoReserva() {
        PlantillaProcesoCobranza proceso = new ProcesoCobranzaExtraordinaria(10.0);
        UsuarioSistema usuario = crearUsuarioDemo();

        ResultadoCobranza resultado = proceso.procesarCobranza(202L, 500.0, usuario);

        assertNotNull(resultado);
        assertEquals(202L, resultado.getIdCuota());
        assertEquals(500.0, resultado.getMontoBase(), 0.001);
        assertEquals(550.0, resultado.getMontoFinalAjustado(), 0.001); // 500 + 10% = 550
        assertEquals("REC-EXT-00202-2026", resultado.getCodigoComprobante());
        assertEquals("LIQUIDADA_CON_EXITO", resultado.getEstadoOperacion());
        assertTrue(resultado.getTipoCobranza().contains("Extraordinaria"));
    }

    @Test
    @DisplayName("Debe validar y rechazar argumentos incorrectos en el primer paso del método plantilla")
    public void debeValidarParametrosEnPlantillaBase() {
        PlantillaProcesoCobranza proceso = new ProcesoCobranzaOrdinaria();
        UsuarioSistema usuario = crearUsuarioDemo();

        // 1. ID de cuota inválido
        assertThrows(IllegalArgumentException.class, () -> {
            proceso.procesarCobranza(null, 100.0, usuario);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            proceso.procesarCobranza(-5L, 100.0, usuario);
        });

        // 2. Monto base no positivo
        assertThrows(IllegalArgumentException.class, () -> {
            proceso.procesarCobranza(10L, 0.0, usuario);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            proceso.procesarCobranza(10L, -50.0, usuario);
        });

        // 3. Usuario nulo
        assertThrows(IllegalArgumentException.class, () -> {
            proceso.procesarCobranza(10L, 100.0, null);
        });
    }
}
