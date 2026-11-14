package com.condominio.patrones;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.patrones.abstractfactory.FabricaComercial;
import com.condominio.patrones.abstractfactory.FabricaInmueble;
import com.condominio.patrones.abstractfactory.FabricaResidencial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Prueba unitaria para el Patron Creacional Abstract Factory (Semana 7)
public class AbstractFactoryTest {

    @Test
    @DisplayName("Debe crear familia coherente de inmuebles residenciales")
    public void debeCrearFamiliaResidencial() {
        FabricaInmueble fabrica = new FabricaResidencial();
        Condominio cond = Condominio.builder().id(1L).nombre("Condominio Residencial").build();

        Edificio edificio = fabrica.crearEdificio(1L, "Primavera", 10, cond);
        Departamento depto = fabrica.crearDepartamento(101L, "101", 1, 75.0);
        AreaComun area = fabrica.crearAreaComun(201L, "Jardin", 25, 50.0, cond);

        assertNotNull(edificio);
        assertTrue(edificio.getNombre().contains("Residencial"));
        assertNotNull(depto);
        assertEquals(3, depto.getNumeroHabitantes());
        assertNotNull(area);
        assertTrue(area.getNombre().contains("Social"));
    }

    @Test
    @DisplayName("Debe crear familia coherente de inmuebles comerciales")
    public void debeCrearFamiliaComercial() {
        FabricaInmueble fabrica = new FabricaComercial();
        Condominio cond = Condominio.builder().id(2L).nombre("Centro Corporativo").build();

        Edificio edificio = fabrica.crearEdificio(2L, "Titanium", 20, cond);
        Departamento depto = fabrica.crearDepartamento(301L, "301", 3, 120.0);
        AreaComun area = fabrica.crearAreaComun(401L, "Auditorio", 100, 300.0, cond);

        assertNotNull(edificio);
        assertTrue(edificio.getNombre().contains("Empresarial"));
        assertNotNull(depto);
        assertTrue(depto.getNumero().startsWith("OFICINA-"));
        assertNotNull(area);
        assertTrue(area.getNombre().contains("Conferencias"));
    }
}
