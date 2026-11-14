package com.condominio.patrones.state;

import com.condominio.modelo.financiero.CuotaMantenimiento;

public class EstadoPagado implements EstadoCuota {

    @Override
    public void pagar(CuotaMantenimiento cuota, double monto) {
        throw new IllegalStateException("La cuota ya se encuentra totalmente pagada");
    }

    @Override
    public void aplicarMora(CuotaMantenimiento cuota) {
        // Una cuota cancelada no admite aplicacion de mora
    }

    @Override
    public String getNombreEstado() {
        return "PAGADO";
    }
}
