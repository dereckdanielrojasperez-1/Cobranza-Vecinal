package com.condominio.patrones.proxy;

import com.condominio.modelo.seguridad.UsuarioSistema;

// Interfaz sujeta para el Patrón Estructural Proxy GoF.
public interface ServicioAccesoCobranza {

    // Consulta el balance y estado financiero de cobranzas del condominio.
    String consultarReporteFinanciero(UsuarioSistema usuario, String periodo);

    // Ejecuta el cierre mensual de caja y conciliación de cuentas.
    boolean ejecutarCierreMensual(UsuarioSistema usuario, String periodo);

    // Retorna el número de solicitudes directas atendidas por el servicio real.
    int getContadorEjecucionesReales();
}
