package com.condominio.patrones;

import com.condominio.dto.ResultadoBusquedaDTO;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BusquedaGlobalRBACTest {

    private final CobranzaFachada fachada = new CobranzaFachada();

    @Test
    @DisplayName("Administrador debe tener búsqueda total con acceso a gastos, cuotas y padrón")
    public void adminDebeTenerControlTotalDeBusqueda() {
        List<ResultadoBusquedaDTO> resultados = fachada.buscarEnSistema("", RolUsuario.ADMINISTRADOR, null);
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty(), "El administrador debe obtener registros de todas las categorías");

        boolean encontroGasto = resultados.stream().anyMatch(r -> r.getCategoria().contains("GASTO"));
        boolean encontroAviso = resultados.stream().anyMatch(r -> r.getCategoria().contains("AVISO") || r.getCategoria().contains("COMUNICADO"));
        boolean encontroCuota = resultados.stream().anyMatch(r -> r.getCategoria().contains("CUOTA"));
        boolean encontroResidente = resultados.stream().anyMatch(r -> r.getCategoria().contains("RESIDENTE") || r.getCategoria().contains("PADRÓN"));

        assertTrue(encontroGasto, "Administrador debe ver gastos comunes");
        assertTrue(encontroAviso, "Administrador debe ver comunicados oficiales");
        assertTrue(encontroCuota, "Administrador debe ver cuotas de mantenimiento");
        assertTrue(encontroResidente, "Administrador debe ver el padrón de residentes");
    }

    @Test
    @DisplayName("Conserje debe tener búsqueda limitada a portería sin acceso a finanzas o cuotas")
    public void conserjeDebeTenerBusquedaLimitadaAPorteria() {
        List<ResultadoBusquedaDTO> resultados = fachada.buscarEnSistema("", RolUsuario.CONSERJE, null);
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        boolean tieneCuotas = resultados.stream().anyMatch(r -> r.getCategoria().contains("CUOTA"));
        boolean tieneGastos = resultados.stream().anyMatch(r -> r.getCategoria().contains("GASTO"));
        boolean tieneVisitas = resultados.stream().anyMatch(r -> r.getCategoria().contains("VISITA") || r.getCategoria().contains("GARITA"));
        boolean tieneContactos = resultados.stream().anyMatch(r -> r.getCategoria().contains("CONTACTO"));

        assertFalse(tieneCuotas, "Conserje NO debe ver cuotas ni deudas financieras");
        assertFalse(tieneGastos, "Conserje NO debe tener acceso a egresos ni gastos");
        assertTrue(tieneVisitas, "Conserje debe tener acceso al control de visitas de portería");
        assertTrue(tieneContactos, "Conserje debe tener acceso al directorio telefónico de emergencia");
    }

    @Test
    @DisplayName("Residente debe tener acceso solo a avisos, gastos generales y sus propias cuotas")
    public void residenteDebeTenerAccesoLimitadoASuPropioAmbito() {
        String miDepto = "101";
        List<ResultadoBusquedaDTO> resultados = fachada.buscarEnSistema("", RolUsuario.RESIDENTE, miDepto);
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        boolean tieneAvisos = resultados.stream().anyMatch(r -> r.getCategoria().contains("AVISO") || r.getCategoria().contains("COMUNICADO"));
        boolean tieneGastos = resultados.stream().anyMatch(r -> r.getCategoria().contains("GASTO"));
        boolean tieneVisitasDeOtros = resultados.stream().anyMatch(r -> r.getCategoria().contains("VISITA"));
        boolean tienePadronPrivado = resultados.stream().anyMatch(r -> r.getCategoria().contains("PADRÓN") || r.getCategoria().contains("CONTACTO PORTERÍA"));

        assertTrue(tieneAvisos, "Residente debe ver comunicados y avisos comunitarios");
        assertTrue(tieneGastos, "Residente debe ver gastos comunes generales por transparencia");
        assertFalse(tieneVisitasDeOtros, "Residente NO debe ver bitácora de visitas ajenas");
        assertFalse(tienePadronPrivado, "Residente NO debe tener acceso a datos privados de otros vecinos");

        // Validar que las cuotas que ve el residente correspondan ÚNICAMENTE a su departamento (101)
        long cuotasAjenas = resultados.stream()
                .filter(r -> r.getCategoria().contains("CUOTA"))
                .filter(r -> !r.getCategoria().contains("101"))
                .count();
        assertEquals(0, cuotasAjenas, "El residente no debe ver cuotas de otros departamentos");
    }

    @Test
    @DisplayName("Búsqueda por término específico debe filtrar coincidencias correctamente")
    public void busquedaPorTerminoDebeFiltrarCoincidencias() {
        List<ResultadoBusquedaDTO> resultados = fachada.buscarEnSistema("agua", RolUsuario.ADMINISTRADOR, null);
        assertNotNull(resultados);
        for (ResultadoBusquedaDTO r : resultados) {
            String blob = (r.getTitulo() + " " + r.getSubtitulo() + " " + r.getCategoria() + " " + r.getDetalleCompleto()).toLowerCase();
            assertTrue(blob.contains("agua"), "Cada resultado debe contener el término de búsqueda 'agua'");
        }
    }
}
