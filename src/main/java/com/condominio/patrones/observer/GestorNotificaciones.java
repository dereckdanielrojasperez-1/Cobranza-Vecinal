package com.condominio.patrones.observer;

import java.util.ArrayList;
import java.util.List;

public class GestorNotificaciones {

    private final List<ObservadorCobranza> observadores = new ArrayList<>();

    public void suscribir(ObservadorCobranza observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void desuscribir(ObservadorCobranza observador) {
        observadores.remove(observador);
    }

    public void notificar(String mensaje) {
        for (ObservadorCobranza obs : observadores) {
            obs.actualizar(mensaje);
        }
    }

    public int cantidadObservadores() {
        return observadores.size();
    }
}
