package com.condominio.patrones;

import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.modelo.personas.Inquilino;
import com.condominio.modelo.personas.Propietario;
import com.condominio.patrones.builder.DepartamentoBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Creacional Builder (Semana 7)
public class BuilderTest {

    @Test
    @DisplayName("Debe construir un departamento paso a paso con todos sus atributos")
    public void debeConstruirDepartamentoConBuilder() {
        Propietario prop = Propietario.builder().id(1L).nombre("Dereck Rojas").dni("24216436").build();
        Inquilino inq = Inquilino.builder().id(2L).nombre("Inquilino Temporal").build();
        Torre torre = Torre.builder().id(5L).nombre("Torre B").build();

        Departamento depto = new DepartamentoBuilder()
                .conId(501L)
                .conNumero("B-501")
                .conPiso(5)
                .conMetrosCuadrados(105.0)
                .conNumeroHabitantes(4)
                .conTorre(torre)
                .conPropietario(prop)
                .conInquilinoActual(inq)
                .build();

        assertNotNull(depto);
        assertEquals(501L, depto.getId());
        assertEquals("B-501", depto.getNumero());
        assertEquals(5, depto.getPiso());
        assertEquals(105.0, depto.getMetrosCuadrados());
        assertEquals(4, depto.getNumeroHabitantes());
        assertEquals(torre, depto.getTorre());
        assertEquals(prop, depto.getPropietario());
        assertEquals(inq, depto.getInquilinoActual());
    }
}
