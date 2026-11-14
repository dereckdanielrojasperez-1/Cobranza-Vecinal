package com.condominio.patrones.command;

import java.util.Stack;

public class InvocadorCobranza {

    private final Stack<ComandoCobranza> historialComandos = new Stack<>();

    public void ejecutarComando(ComandoCobranza comando) {
        if (comando == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        comando.ejecutar();
        historialComandos.push(comando);
    }

    public boolean deshacerUltimoComando() {
        if (!historialComandos.isEmpty()) {
            ComandoCobranza comando = historialComandos.pop();
            comando.deshacer();
            return true;
        }
        return false;
    }

    public boolean tieneComandosPorDeshacer() {
        return !historialComandos.isEmpty();
    }

    public int cantidadComandos() {
        return historialComandos.size();
    }
}
