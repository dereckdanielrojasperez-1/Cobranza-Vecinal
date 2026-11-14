package com.condominio.patrones;

import com.condominio.patrones.decorator.CuotaBase;
import com.condominio.patrones.decorator.DecoradorDescuentoProntoPago;
import com.condominio.patrones.decorator.DecoradorRecargoMora;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Estructural Decorator (Semana 9)
public class DecoratorTest {

    @Test
    @DisplayName("Debe calcular cuota base correctamente")
    public void debeCalcularCuotaBase() {
        CuotaBase cuota = new CuotaBase(180.0, "Cuota Ordinaria");
        assertEquals(180.0, cuota.calcularMontoTotal());
        assertTrue(cuota.obtenerDescripcion().contains("180.00"));
    }

    @Test
    @DisplayName("Debe aplicar recargo por mora mediante decorador")
    public void debeAplicarRecargoMora() {
        CuotaBase cuota = new CuotaBase(200.0, "Cuota Octubre");
        CuotaBase conMora = new DecoradorRecargoMora(cuota, 2.0); // 200 + 2% de 200 = 204.0

        assertEquals(204.0, conMora.calcularMontoTotal(), 0.001);
        assertTrue(conMora.obtenerDescripcion().contains("Recargo Mora"));
    }

    @Test
    @DisplayName("Debe aplicar descuento por pronto pago mediante decorador")
    public void debeAplicarDescuentoProntoPago() {
        CuotaBase cuota = new CuotaBase(200.0, "Cuota Octubre");
        CuotaBase conDescuento = new DecoradorDescuentoProntoPago(cuota, 10.0); // 200 - 10% = 180.0

        assertEquals(180.0, conDescuento.calcularMontoTotal(), 0.001);
        assertTrue(conDescuento.obtenerDescripcion().contains("Descuento Pronto Pago"));
    }

    @Test
    @DisplayName("Debe encadenar decoradores acumulativamente")
    public void debeEncadenarDecoradores() {
        CuotaBase cuota = new CuotaBase(100.0, "Cuota Anual");
        // 100 + 10% = 110.0
        CuotaBase conMora = new DecoradorRecargoMora(cuota, 10.0);
        // 110 - 10% = 99.0
        CuotaBase conDescuento = new DecoradorDescuentoProntoPago(conMora, 10.0);

        assertEquals(99.0, conDescuento.calcularMontoTotal(), 0.001);
    }
}
