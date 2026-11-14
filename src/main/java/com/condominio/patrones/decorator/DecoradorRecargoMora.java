package com.condominio.patrones.decorator;

// Decorador concreto que anade recargo por mora (Semana 9)
public class DecoradorRecargoMora extends DecoradorCuota {

    private final double porcentajeMora;

    public DecoradorRecargoMora(CuotaBase cuotaDecorada, double porcentajeMora) {
        super(cuotaDecorada);
        this.porcentajeMora = porcentajeMora;
    }

    @Override
    public double calcularMontoTotal() {
        double montoPrevio = cuotaDecorada.calcularMontoTotal();
        double recargo = montoPrevio * (porcentajeMora / 100.0);
        return montoPrevio + recargo;
    }

    @Override
    public String obtenerDescripcion() {
        double montoPrevio = cuotaDecorada.calcularMontoTotal();
        double recargo = montoPrevio * (porcentajeMora / 100.0);
        return cuotaDecorada.obtenerDescripcion() + " + Recargo Mora (" + porcentajeMora + "%: S/ " + String.format("%.2f", recargo) + ")";
    }
}
