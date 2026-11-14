package com.condominio.patrones.observer;

public class NotificadorResidente implements ObservadorCobranza {

    private String ultimoMensaje;

    @Override
    public void actualizar(String mensaje) {
        this.ultimoMensaje = mensaje;
        System.out.println("NOTIFICACIÓN AL RESIDENTE: " + mensaje);
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }
}
