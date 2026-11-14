package com.condominio.patrones.strategy;

public class CalculadoraGastos {

    private EstrategiaProrrateo estrategia;

    public CalculadoraGastos(EstrategiaProrrateo estrategia) {
        this.estrategia = estrategia;
    }

    public CalculadoraGastos() {
        this(new ProrrateoPorAlicuota());
    }

    public void setEstrategia(EstrategiaProrrateo estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia de prorrateo no puede ser nula");
        }
        this.estrategia = estrategia;
    }

    public EstrategiaProrrateo getEstrategia() {
        return this.estrategia;
    }

    public double ejecutarCalculo(double montoGastoTotal, double factorUnidad, double factorTotal) {
        if (estrategia == null) {
            throw new IllegalStateException("No se ha configurado ninguna estrategia de prorrateo");
        }
        return estrategia.calcularProrrateo(montoGastoTotal, factorUnidad, factorTotal);
    }
}
