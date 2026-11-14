package com.condominio.patrones;

import com.condominio.patrones.prototype.CuotaPrototipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Creacional Prototype (Semana 6)
public class PrototypeTest {

    @Test
    @DisplayName("Debe clonar una cuota base creando una instancia independiente")
    public void debeClonarCuotaIndependiente() {
        CuotaPrototipo original = CuotaPrototipo.builder()
                .id(1L)
                .montoBase(150.0)
                .concepto("Mantenimiento Base")
                .mesPeriodo("2026-09")
                .estado("PENDIENTE")
                .build();

        CuotaPrototipo clon = original.clonar();

        assertNotNull(clon);
        assertNotSame(original, clon);
        assertEquals(original.getMontoBase(), clon.getMontoBase());
        assertEquals(original.getConcepto(), clon.getConcepto());

        // Modificar el clon no afecta el original
        clon.setMesPeriodo("2026-10");
        clon.setMontoBase(200.0);

        assertEquals("2026-09", original.getMesPeriodo());
        assertEquals(150.0, original.getMontoBase());
        assertEquals("2026-10", clon.getMesPeriodo());
        assertEquals(200.0, clon.getMontoBase());
    }
}
