package com.condominio.patrones.proxy;

import com.condominio.modelo.seguridad.UsuarioSistema;

// Sujeto Real (Real Subject) en el Patrón Proxy GoF.
public class ServicioAccesoCobranzaReal implements ServicioAccesoCobranza {

    private int contadorEjecucionesReales = 0;

    @Override
    public String consultarReporteFinanciero(UsuarioSistema usuario, String periodo) {
        contadorEjecucionesReales++;
        return "REPORTE FINANCIERO OFICIAL [" + periodo + "]\n"
                + "- Solicitante: " + (usuario != null ? usuario.getUsuario() : "ANÓNIMO") + "\n"
                + "- Ingresos Totales por Cuotas: S/ 14,850.00\n"
                + "- Egresos Operacionales: S/ 9,200.00\n"
                + "- Superávit Fiscal Condominio: S/ 5,650.00\n"
                + "- Estado de Cuentas: Balance Auditado y Conforme";
    }

    @Override
    public boolean ejecutarCierreMensual(UsuarioSistema usuario, String periodo) {
        contadorEjecucionesReales++;
        System.out.println("LOG CONTABLE: Cierre mensual del periodo " + periodo
                + " ejecutado por " + (usuario != null ? usuario.getUsuario() : "SISTEMA"));
        return true;
    }

    @Override
    public int getContadorEjecucionesReales() {
        return contadorEjecucionesReales;
    }
}
