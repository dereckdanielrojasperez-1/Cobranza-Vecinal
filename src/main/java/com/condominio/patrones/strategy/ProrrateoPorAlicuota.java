package com.condominio.patrones.strategy;

public class ProrrateoPorAlicuota implements EstrategiaProrrateo {

    @Override
    public double calcularProrrateo(double montoGastoTotal, double factorUnidad, double factorTotal) {
        if (factorTotal <= 0) {
            throw new IllegalArgumentException("El área total del condominio debe ser mayor a cero");
        }
        if (montoGastoTotal < 0 || factorUnidad < 0) {
            throw new IllegalArgumentException("Los valores de gasto y área deben ser no negativos");
        }
        return (factorUnidad / factorTotal) * montoGastoTotal;
    }
}
