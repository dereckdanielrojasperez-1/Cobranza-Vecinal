package com.condominio.patrones.state;

import com.condominio.modelo.financiero.CuotaMantenimiento;

public interface EstadoCuota {

    void pagar(CuotaMantenimiento cuota, double monto);

    void aplicarMora(CuotaMantenimiento cuota);

    String getNombreEstado();
}
