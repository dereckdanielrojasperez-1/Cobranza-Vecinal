package com.condominio.gui.animacion;

// Tokens de diseño del sistema de motion para Cobranza Vecinal.
public final class TokensMotion {

    private TokensMotion() {
        // Clase de constantes no instanciable
    }

    // Duración para transiciones de entrada y despliegue rápido (180 ms).
    public static final int ENTRADA_MS = 180;

    // Duración para transiciones de énfasis, conteos y crecimientos analíticos (500 ms).
    public static final int ENFASIS_MS = 500;

    // Duración para desvanecimiento y transiciones de salida (250 ms).
    public static final int SALIDA_MS = 250;

    // Duración para destellos de retroalimentación en tablas y filas (800 ms).
    public static final int DESTELLO_MS = 800;

    // Intervalo de escalonado entre elementos sucesivos (60 ms).
    public static final int ESCALONADO_MS = 60;

    // Intervalo entre cuadros para un refresco fluido a 60 FPS (16 ms).
    public static final int FRAME_DELAY_MS = 16;
}
