package com.condominio.patrones;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.patrones.proxy.ProxyAccesoCobranza;
import com.condominio.patrones.proxy.ServicioAccesoCobranzaReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyTest {

    private UsuarioSistema crearUsuario(String username, RolUsuario rol) {
        return UsuarioSistema.builder()
                .id(1L)
                .usuario(username)
                .clave("segura123")
                .rol(rol)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe permitir al Administrador consultar el balance global y ejecutar el cierre mensual")
    public void debePermitirAccesoAdminACierreYReporte() {
        ServicioAccesoCobranzaReal servicioReal = new ServicioAccesoCobranzaReal();
        ProxyAccesoCobranza proxy = new ProxyAccesoCobranza(servicioReal);
        UsuarioSistema admin = crearUsuario("admin", RolUsuario.ADMINISTRADOR);

        String reporte = proxy.consultarReporteFinanciero(admin, "2026-10");
        assertNotNull(reporte);
        assertTrue(reporte.contains("REPORTE FINANCIERO OFICIAL"));
        assertTrue(reporte.contains("Superávit Fiscal Condominio"));

        boolean cierreExitoso = proxy.ejecutarCierreMensual(admin, "2026-10");
        assertTrue(cierreExitoso);
        assertEquals(2, proxy.getContadorEjecucionesReales());
    }

    @Test
    @DisplayName("Debe denegar la ejecución del cierre contable a usuarios sin rol Administrador (Residente y Conserje)")
    public void debeDenegarCierreMensualAResidenteYConserje() {
        ProxyAccesoCobranza proxy = new ProxyAccesoCobranza();
        UsuarioSistema conserje = crearUsuario("conserje", RolUsuario.CONSERJE);
        UsuarioSistema residente = crearUsuario("residente", RolUsuario.RESIDENTE);

        SecurityException exConserje = assertThrows(SecurityException.class, () -> {
            proxy.ejecutarCierreMensual(conserje, "2026-10");
        });
        assertTrue(exConserje.getMessage().contains("no posee privilegios"));

        SecurityException exResidente = assertThrows(SecurityException.class, () -> {
            proxy.ejecutarCierreMensual(residente, "2026-10");
        });
        assertTrue(exResidente.getMessage().contains("no posee privilegios"));
    }

    @Test
    @DisplayName("Debe responder desde caché en consultas sucesivas sin invocar al servicio real reiteradamente")
    public void debeRetornarReporteDesdeCacheEnSegundaConsulta() {
        ServicioAccesoCobranzaReal servicioReal = new ServicioAccesoCobranzaReal();
        ProxyAccesoCobranza proxy = new ProxyAccesoCobranza(servicioReal);
        UsuarioSistema admin = crearUsuario("admin", RolUsuario.ADMINISTRADOR);

        // Primera llamada: ejecuta el servicio real
        String primerReporte = proxy.consultarReporteFinanciero(admin, "2026-10");
        assertEquals(1, proxy.getContadorEjecucionesReales());
        assertFalse(primerReporte.contains("[DESDE CACHÉ PROXY]"));

        // Segunda llamada con el mismo periodo: responde desde la caché del proxy
        String segundoReporte = proxy.consultarReporteFinanciero(admin, "2026-10");
        assertEquals(1, proxy.getContadorEjecucionesReales()); // No incrementa la llamada al servicio real
        assertTrue(segundoReporte.contains("[DESDE CACHÉ PROXY]"));

        // Verificación de blindaje y aislamiento RBAC: El residente no debe recibir el reporte del admin desde la caché
        UsuarioSistema residente = crearUsuario("residente", RolUsuario.RESIDENTE);
        String reporteResidente = proxy.consultarReporteFinanciero(residente, "2026-10");
        assertTrue(reporteResidente.contains("ESTADO DE CUENTA RESIDENTE"));
        assertFalse(reporteResidente.contains("Superávit Fiscal Condominio"));
    }
}
