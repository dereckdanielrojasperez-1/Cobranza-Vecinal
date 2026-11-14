package com.condominio.patrones.singleton;

import lombok.Getter;

// Patron Creacional Singleton (Semana 6 - Bill Pugh Singleton seguro en multihilo)
@Getter
public final class ConfiguracionCondominio {

    private final double tasaInteresMora;
    private final int diasGracia;
    private final int diasParaMora;

    private ConfiguracionCondominio() {
        this.tasaInteresMora = 1.5;
        this.diasGracia = 3;
        this.diasParaMora = 5;
    }

    private static class Instancia {
        private static final ConfiguracionCondominio UNICA =
                new ConfiguracionCondominio();
    }

    public static ConfiguracionCondominio obtenerInstancia() {
        return Instancia.UNICA;
    }
}
