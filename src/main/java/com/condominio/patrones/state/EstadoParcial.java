package com.condominio.patrones.state;

import com.condominio.modelo.financiero.CuotaMantenimiento;

public class EstadoParcial implements EstadoCuota {

    @Override
    public void pagar(CuotaMantenimiento cuota, double monto) {
        if (monto <= 0 || !Double.isFinite(monto)) {
            throw new IllegalArgumentException("El monto a pagar debe ser positivo y finito");
        }

        double saldoActual = cuota.getMontoTotal() != null ? cuota.getMontoTotal() : cuota.getMontoBase();
        if (monto > saldoActual) {
            throw new IllegalArgumentException("El monto abonado (S/ " + monto + ") excede el saldo pendiente (S/ " + saldoActual + ")");
        }

        double saldoRestante = saldoActual - monto;
        if (saldoRestante <= 0.000001) {
            cuota.setMontoTotal(0.0);
            cuota.setEstadoLogico(new EstadoPagado());
        } else {
            cuota.setMontoTotal(saldoRestante);
            cuota.setEstadoLogico(this);
        }
    }

    @Override
    public void aplicarMora(CuotaMantenimiento cuota) {
        // Cuota con amortizaciones parciales registradas
    }

    @Override
    public String getNombreEstado() {
        return "PARCIAL";
    }
}
