package com.condominio.patrones.decorator;

// Decorador abstracto que envuelve un componente CuotaBase (Semana 9)
public abstract class DecoradorCuota extends CuotaBase {

    protected final CuotaBase cuotaDecorada;

    public DecoradorCuota(CuotaBase cuotaDecorada) {
        super(
            cuotaDecorada != null ? cuotaDecorada.calcularMontoTotal() : 0.0,
            cuotaDecorada != null ? cuotaDecorada.obtenerDescripcion() : ""
        );
        if (cuotaDecorada == null) {
            throw new IllegalArgumentException("La cuota a decorar no puede ser nula");
        }
        this.cuotaDecorada = cuotaDecorada;
    }

    @Override
    public abstract double calcularMontoTotal();

    @Override
    public abstract String obtenerDescripcion();
}
