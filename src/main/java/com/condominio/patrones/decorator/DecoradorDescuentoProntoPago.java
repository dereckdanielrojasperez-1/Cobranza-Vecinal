package com.condominio.patrones.decorator;

// Decorador concreto que aplica descuento por pronto pago anticipado (Semana 9)
public class DecoradorDescuentoProntoPago extends DecoradorCuota {

    private final double porcentajeDescuento;

    public DecoradorDescuentoProntoPago(CuotaBase cuotaDecorada, double porcentajeDescuento) {
        super(cuotaDecorada);
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public double calcularMontoTotal() {
        double montoPrevio = cuotaDecorada.calcularMontoTotal();
        double descuento = montoPrevio * (porcentajeDescuento / 100.0);
        return Math.max(0.0, montoPrevio - descuento);
    }

    @Override
    public String obtenerDescripcion() {
        double montoPrevio = cuotaDecorada.calcularMontoTotal();
        double descuento = montoPrevio * (porcentajeDescuento / 100.0);
        return cuotaDecorada.obtenerDescripcion() + " - Descuento Pronto Pago (" + porcentajeDescuento + "%: S/ " + String.format("%.2f", descuento) + ")";
    }
}
