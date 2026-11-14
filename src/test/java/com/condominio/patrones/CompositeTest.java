package com.condominio.patrones;

import com.condominio.patrones.composite.CompuestoInmueble;
import com.condominio.patrones.composite.HojaInmueble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Estructural Composite (Semana 9)
public class CompositeTest {

    @Test
    @DisplayName("Debe calcular el area total en estructura de arbol con hojas y compuestos")
    public void debeCalcularAreaJerarquica() {
        // Hojas (Departamentos)
        HojaInmueble depto101 = new HojaInmueble("Depto 101", 80.0);
        HojaInmueble depto102 = new HojaInmueble("Depto 102", 90.0);
        HojaInmueble depto201 = new HojaInmueble("Depto 201", 110.0);

        assertEquals(80.0, depto101.calcularAreaTotal());
        assertEquals("Depto 101", depto101.obtenerNombre());

        // Compuesto Nivel 2: Edificios
        CompuestoInmueble torreA = new CompuestoInmueble("Torre A");
        torreA.agregarHijo(depto101);
        torreA.agregarHijo(depto102);

        CompuestoInmueble torreB = new CompuestoInmueble("Torre B");
        torreB.agregarHijo(depto201);

        assertEquals(170.0, torreA.calcularAreaTotal(), 0.001);
        assertEquals(110.0, torreB.calcularAreaTotal(), 0.001);

        // Compuesto Nivel 1: Condominio General
        CompuestoInmueble condominio = new CompuestoInmueble("Condominio General");
        condominio.agregarHijo(torreA);
        condominio.agregarHijo(torreB);

        // Total: 80 + 90 + 110 = 280.0 m2
        assertEquals(280.0, condominio.calcularAreaTotal(), 0.001);
        assertEquals(2, condominio.getHijos().size());
    }
}
