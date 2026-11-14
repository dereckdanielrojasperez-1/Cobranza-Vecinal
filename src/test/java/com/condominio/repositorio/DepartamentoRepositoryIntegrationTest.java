package com.condominio.repositorio;

import com.condominio.modelo.inmuebles.Departamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de integración sobre DepartamentoRepositorio contra base de datos real H2 (Punto 6.1).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
public class DepartamentoRepositoryIntegrationTest {

    @Autowired
    private DepartamentoRepositorio departamentoRepositorio;

    @Test
    @DisplayName("6.1.6 Conteo global de departamentos en la base de datos")
    void testConteoDepartamentos() {
        long total = departamentoRepositorio.count();
        assertTrue(total >= 6, "Deben existir al menos 6 departamentos sembrados en el condominio");
    }

    @Test
    @DisplayName("6.1.7 Búsqueda de departamento por número único")
    void testBuscarDepartamentoPorNumero() {
        Optional<Departamento> opt = departamentoRepositorio.findByNumero("401");
        assertTrue(opt.isPresent(), "El departamento 401 del seed debe existir");

        Departamento depto = opt.get();
        assertEquals("401", depto.getNumero());
        assertEquals(95.0, depto.getMetrosCuadrados(), 0.001);
    }

    @Test
    @DisplayName("6.1.8 Consulta de departamentos por torre")
    void testBuscarDepartamentosPorTorre() {
        List<Departamento> deptos = departamentoRepositorio.findAll();
        assertFalse(deptos.isEmpty());

        Long torreId = deptos.get(0).getTorre() != null ? deptos.get(0).getTorre().getId() : null;
        if (torreId != null) {
            List<Departamento> porTorre = departamentoRepositorio.findByTorreId(torreId);
            assertFalse(porTorre.isEmpty(), "La torre debe contener al menos un departamento");
        }
    }
}
