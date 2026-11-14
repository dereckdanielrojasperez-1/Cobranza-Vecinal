package com.condominio.patrones.command;

import com.condominio.patrones.facade.CobranzaFachada;

public class ComandoRegistrarPago implements ComandoCobranza {

    private final CobranzaFachada fachada;
    private final Long idCuota;
    private final double monto;
    private final String codigoOperacion;
    private boolean ejecutado = false;

    public ComandoRegistrarPago(CobranzaFachada fachada, Long idCuota, double monto, String codigoOperacion) {
        if (fachada == null) {
            throw new IllegalArgumentException("La fachada de cobranza no puede ser nula");
        }
        this.fachada = fachada;
        this.idCuota = idCuota;
        this.monto = monto;
        this.codigoOperacion = codigoOperacion;
    }

    @Override
    public void ejecutar() {
        fachada.procesarPagoConRepositorio(idCuota, monto, codigoOperacion);
        this.ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (ejecutado) {
            fachada.anularPagoConRepositorio(idCuota, monto);
            this.ejecutado = false;
        }
    }

    public Long getIdCuota() {
        return idCuota;
    }

    public double getMonto() {
        return monto;
    }
}
