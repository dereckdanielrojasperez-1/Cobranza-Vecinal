package com.condominio.patrones.decorator;

import lombok.Getter;

// Componente base concreto para el calculo de cuotas (Patron Decorator - Semana 9)
@Getter
public class CuotaBase {

    private final double monto;
    private final String concepto;

    public CuotaBase(double monto, String concepto) {
        this.monto = monto;
        this.concepto = concepto;
    }

    public CuotaBase(double monto) {
        this(monto, "Cuota de Mantenimiento");
    }

    public double calcularMontoTotal() {
        return monto;
    }

    public String obtenerDescripcion() {
        return concepto + " (Monto base: S/ " + String.format("%.2f", monto) + ")";
    }
}
