package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

// Orquestador de fotogramas mediante javax.swing.Timer acotado a 60 FPS.
public class TemporizadorAnimacion {

    private final Timer timer;
    private boolean completado = false;

    private TemporizadorAnimacion(Timer timer) {
        this.timer = timer;
    }

    // Inicia una animación con interpolación cúbica de salida (easeOutCubic).
    public static TemporizadorAnimacion iniciar(int duracionMs, Consumer<Double> onStep, Runnable onComplete) {
        return iniciarConEasing(duracionMs, UtilidadEasing::easeOutCubic, onStep, onComplete);
    }

    // Inicia una animación con una función de aceleración personalizada.
    public static TemporizadorAnimacion iniciarConEasing(
            int duracionMs,
            java.util.function.DoubleUnaryOperator easing,
            Consumer<Double> onStep,
            Runnable onComplete) {

        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS || duracionMs <= 0) {
            if (onStep != null) {
                onStep.accept(1.0);
            }
            if (onComplete != null) {
                onComplete.run();
            }
            return new TemporizadorAnimacion(null);
        }

        long tiempoInicio = System.currentTimeMillis();
        final TemporizadorAnimacion[] ref = new TemporizadorAnimacion[1];

        Timer timer = new Timer(TokensMotion.FRAME_DELAY_MS, null);
        timer.addActionListener(e -> {
            long transcurrido = System.currentTimeMillis() - tiempoInicio;
            double progresoLineal = UtilidadEasing.clamp((double) transcurrido / duracionMs, 0.0, 1.0);
            double progresoSuavizado = (easing != null) ? easing.applyAsDouble(progresoLineal) : progresoLineal;

            if (onStep != null) {
                onStep.accept(progresoSuavizado);
            }

            if (progresoLineal >= 1.0) {
                timer.stop();
                if (ref[0] != null) {
                    ref[0].completado = true;
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        ref[0] = new TemporizadorAnimacion(timer);
        timer.start();
        return ref[0];
    }

    public void cancelar() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            completado = true;
        }
    }

    public boolean isCompletado() {
        return completado;
    }
}
