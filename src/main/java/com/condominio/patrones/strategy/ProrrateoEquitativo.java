package com.condominio.patrones.strategy;

public class ProrrateoEquitativo implements EstrategiaProrrateo {

    @Override
    public double calcularProrrateo(double montoGastoTotal, double factorUnidad, double factorTotal) {
        if (factorTotal <= 0) {
            throw new IllegalArgumentException("El total de departamentos debe ser mayor a cero");
        }
        if (montoGastoTotal < 0) {
            throw new IllegalArgumentException("El monto de gasto total debe ser no negativo");
        }
        return montoGastoTotal / factorTotal;
    }
}
