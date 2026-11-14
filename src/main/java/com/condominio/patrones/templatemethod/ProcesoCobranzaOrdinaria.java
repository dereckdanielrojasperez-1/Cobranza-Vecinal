package com.condominio.patrones.templatemethod;

// Subclase Concreta del Patrón Template Method GoF.
public class ProcesoCobranzaOrdinaria extends PlantillaProcesoCobranza {

    private final double porcentajeDescuento;

    public ProcesoCobranzaOrdinaria(double porcentajeDescuento) {
        this.porcentajeDescuento = Math.max(0.0, porcentajeDescuento);
    }

    public ProcesoCobranzaOrdinaria() {
        this(5.0); // 5% de descuento por pronto pago por defecto
    }

    @Override
    public String obtenerTipoCobranza() {
        return "Cobranza Ordinaria de Mantenimiento";
    }

    @Override
    protected double calcularMontoAjustado(double montoBase) {
        double factor = (100.0 - porcentajeDescuento) / 100.0;
        return Math.round(montoBase * factor * 100.0) / 100.0;
    }

    @Override
    protected String generarComprobante(Long idCuota, double montoAjustado) {
        return "REC-ORD-" + String.format("%05d", idCuota) + "-2026";
    }
}
