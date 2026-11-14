package com.condominio.patrones;

import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import com.condominio.patrones.factorymethod.TipoGasto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Creacional Factory Method (Semana 7)
public class FactoryMethodTest {

    @Test
    @DisplayName("Debe crear correctamente subtipos especializados de GastoComun")
    public void debeCrearSubtiposGasto() {
        Condominio cond = Condominio.builder().id(1L).nombre("Las Palmeras").build();

        GastoAgua agua = GastoComunFabrica.crearGastoAgua(10L, 300.0, LocalDate.now(), "Recibo Agua", cond, 1400.0, 1250.0);
        GastoLuz luz = GastoComunFabrica.crearGastoLuz(20L, 500.0, LocalDate.now(), "Recibo Luz", cond, 680.0);
        GastoLimpieza limpieza = GastoComunFabrica.crearGastoLimpieza(30L, 800.0, LocalDate.now(), "Limpieza", cond, "CleanService");

        assertNotNull(agua);
        assertEquals(10L, agua.getId());
        assertEquals(1400.0, agua.getLecturaActual());

        assertNotNull(luz);
        assertEquals(20L, luz.getId());
        assertEquals(680.0, luz.getConsumoKwh());

        assertNotNull(limpieza);
        assertEquals(30L, limpieza.getId());
        assertEquals("CleanService", limpieza.getContratoEmpresa());
    }

    @Test
    @DisplayName("Debe crear gastos mediante el metodo parametrizado con enum TipoGasto")
    public void debeCrearGastoPorTipo() {
        Condominio cond = Condominio.builder().id(1L).nombre("Las Palmeras").build();
        GastoComun gastoAgua = GastoComunFabrica.crearGasto(TipoGasto.AGUA, 100L, 400.0, LocalDate.now(), "Consumo Agua", cond);

        assertNotNull(gastoAgua);
        assertTrue(gastoAgua instanceof GastoAgua);
        assertEquals(100L, gastoAgua.getId());
        assertEquals(400.0, gastoAgua.getMonto());
    }
}
