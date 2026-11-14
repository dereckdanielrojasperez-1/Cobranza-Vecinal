package com.condominio.patrones;

import com.condominio.AplicacionCobranzaVecinal;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.repositorio.CuotaRepositorio;
import com.condominio.repositorio.DepartamentoRepositorio;
import com.condominio.repositorio.PagoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Prueba automatizada de integración (SeedTest) para verificar la consistencia,
@ActiveProfiles("dev")
@SpringBootTest(classes = AplicacionCobranzaVecinal.class)
public class SeedTest {

    @Autowired
    private DepartamentoRepositorio departamentoRepositorio;

    @Autowired
    private CuotaRepositorio cuotaRepositorio;

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Test
    @DisplayName("2.3 Verificación de integridad, unicidad y consistencia del Seed en H2")
    void testIntegridadYConsistenciaDelSeed() {
        // 1. Afirmar que no existen números de departamento duplicados
        List<Departamento> deptos = departamentoRepositorio.findAll();
        assertFalse(deptos.isEmpty(), "La base de datos debe contener departamentos sembrados");

        Set<String> numerosVistos = new HashSet<>();
        for (Departamento d : deptos) {
            String num = d.getNumero();
            assertNotNull(num, "El número de departamento no puede ser nulo");
            assertTrue(numerosVistos.add(num.trim()), "No deben existir números de departamento duplicados. Duplicado encontrado: " + num);
        }

        // 2. Afirmar que existen los departamentos 401 y 402 sin números '4' duplicados ni metrajes anómalos
        Optional<Departamento> depto401 = deptos.stream()
                .filter(d -> "401".equals(d.getNumero()))
                .findFirst();
        assertTrue(depto401.isPresent(), "Debe existir el departamento 401 en el seed");
        assertEquals(4, depto401.get().getPiso(), "El departamento 401 debe estar en el piso 4");
        assertEquals(95.00, depto401.get().getMetrosCuadrados(), 0.01, "El departamento 401 debe tener 95.00 m²");

        Optional<Departamento> depto402 = deptos.stream()
                .filter(d -> "402".equals(d.getNumero()))
                .findFirst();
        assertTrue(depto402.isPresent(), "Debe existir el departamento 402 en el seed");
        assertEquals(4, depto402.get().getPiso(), "El departamento 402 debe estar en el piso 4");
        assertEquals(68.00, depto402.get().getMetrosCuadrados(), 0.01, "El departamento 402 debe tener 68.00 m²");

        boolean existeDepto4Duplicado = deptos.stream()
                .anyMatch(d -> "4".equals(d.getNumero()) || "Depto 4".equalsIgnoreCase(d.getNumero()));
        assertFalse(existeDepto4Duplicado, "No debe existir ningún departamento denominado '4' o 'Depto 4'");

        // 3. Afirmar que toda cuota en estado PAGADO tiene saldo pendiente 0.00 y pago no nulo
        List<CuotaMantenimiento> cuotas = cuotaRepositorio.findAll();
        assertFalse(cuotas.isEmpty(), "La base de datos debe contener cuotas sembradas");

        List<Pago> pagos = pagoRepositorio.findAll();

        long cuotasPagadasCount = 0;
        for (CuotaMantenimiento c : cuotas) {
            if ("PAGADO".equalsIgnoreCase(c.getEstado())) {
                cuotasPagadasCount++;
                assertEquals(0.00, c.getMontoTotal(), 0.001,
                        "Toda cuota en estado PAGADO debe tener saldo pendiente 0.00. Cuota ID: " + c.getId());

                assertNotNull(c.getDepartamento(), "La cuota pagada debe estar asociada a un departamento");
                Long idDepto = c.getDepartamento().getId();

                boolean existePago = pagos.stream()
                        .anyMatch(p -> p.getDepartamento() != null && p.getDepartamento().getId().equals(idDepto));
                assertTrue(existePago, "Toda cuota PAGADO debe tener al menos una entidad Pago persistida asociada a su departamento. Cuota ID: " + c.getId());
            }
        }
        assertTrue(cuotasPagadasCount >= 2, "Deben existir al menos 2 cuotas en estado PAGADO sembradas (cuota 2 y cuota 5)");
    }
}
