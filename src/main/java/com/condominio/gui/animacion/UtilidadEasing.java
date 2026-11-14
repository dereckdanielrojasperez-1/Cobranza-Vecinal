package com.condominio.gui.animacion;

// Funciones de aceleración (easing) cúbicas estandarizadas del sistema de motion.
public final class UtilidadEasing {

    private UtilidadEasing() {
        // Clase utilitaria no instanciable
    }

    // Easing de desaceleración cúbico para entradas y énfasis: f(t) = 1 - (1 - t)^3.
    public static double easeOutCubic(double t) {
        double clamped = clamp(t, 0.0, 1.0);
        double inv = 1.0 - clamped;
        return 1.0 - (inv * inv * inv);
    }

    // Easing de aceleración cúbico para transiciones de salida: f(t) = t^3.
    public static double easeInCubic(double t) {
        double clamped = clamp(t, 0.0, 1.0);
        return clamped * clamped * clamped;
    }

    // Limita un valor numérico dentro de un rango cerrado [min, max].
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
