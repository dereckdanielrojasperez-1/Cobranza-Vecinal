package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

// Orquestador de motion para gráficos nativos 2D.
public class AnimadorGraficos {

    public static void animarBarras(JComponent componenteGrafico, int totalBarras, Consumer<double[]> actualizadorFactores) {
        if (componenteGrafico == null || actualizadorFactores == null) {
            return;
        }

        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS) {
            double[] finales = new double[totalBarras];
            java.util.Arrays.fill(finales, 1.0);
            actualizadorFactores.accept(finales);
            componenteGrafico.repaint();
            return;
        }

        final double[] factores = new double[totalBarras];
        final long t0 = System.currentTimeMillis();
        final int duracionTotal = TokensMotion.ENFASIS_MS + (totalBarras * TokensMotion.ESCALONADO_MS);

        TemporizadorAnimacion.iniciar(
                duracionTotal,
                fracGlobal -> {
                    long transcurrido = System.currentTimeMillis() - t0;
                    for (int i = 0; i < totalBarras; i++) {
                        long inicioBarra = (long) i * TokensMotion.ESCALONADO_MS;
                        if (transcurrido < inicioBarra) {
                            factores[i] = 0.0;
                        } else {
                            double tLocal = UtilidadEasing.clamp((double) (transcurrido - inicioBarra) / TokensMotion.ENFASIS_MS, 0.0, 1.0);
                            factores[i] = UtilidadEasing.easeOutCubic(tLocal);
                        }
                    }
                    actualizadorFactores.accept(factores);
                    componenteGrafico.repaint();
                },
                () -> {
                    java.util.Arrays.fill(factores, 1.0);
                    actualizadorFactores.accept(factores);
                    componenteGrafico.repaint();
                }
        );
    }

    public static void animarCircular(JComponent componenteGrafico, Consumer<Double> actualizadorFactor) {
        if (componenteGrafico == null || actualizadorFactor == null) {
            return;
        }

        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS) {
            actualizadorFactor.accept(1.0);
            componenteGrafico.repaint();
            return;
        }

        TemporizadorAnimacion.iniciar(
                TokensMotion.ENFASIS_MS,
                frac -> {
                    actualizadorFactor.accept(frac);
                    componenteGrafico.repaint();
                },
                () -> {
                    actualizadorFactor.accept(1.0);
                    componenteGrafico.repaint();
                }
        );
    }
}
