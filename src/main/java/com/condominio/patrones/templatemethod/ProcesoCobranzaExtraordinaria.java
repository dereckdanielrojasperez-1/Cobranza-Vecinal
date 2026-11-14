package com.condominio.patrones.templatemethod;

// Subclase Concreta del Patrón Template Method GoF.
public class ProcesoCobranzaExtraordinaria extends PlantillaProcesoCobranza {

    private final double porcentajeFondoReserva;

    public ProcesoCobranzaExtraordinaria(double porcentajeFondoReserva) {
        this.porcentajeFondoReserva = Math.max(0.0, porcentajeFondoReserva);
    }

    public ProcesoCobranzaExtraordinaria() {
        this(10.0); // 10% de recargo para fondo de reserva por defecto
    }

    @Override
    public String obtenerTipoCobranza() {
        return "Cobranza Extraordinaria (Fondo de Reserva)";
    }

    @Override
    protected double calcularMontoAjustado(double montoBase) {
        double factor = (100.0 + porcentajeFondoReserva) / 100.0;
        return Math.round(montoBase * factor * 100.0) / 100.0;
    }

    @Override
    protected String generarComprobante(Long idCuota, double montoAjustado) {
        return "REC-EXT-" + String.format("%05d", idCuota) + "-2026";
    }
}
