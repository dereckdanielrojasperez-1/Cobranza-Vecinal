package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;

// Animador del indicador de pestaña activa (subrayado cromático).
public class AnimadorSubrayado {

    private double xActual = 0;
    private double anchoActual = 0;
    private boolean animando = false;
    private Color colorActual = new Color(0xE8, 0xA3, 0x3D);
    private TemporizadorAnimacion temporizador;

    public void animarTransicion(JTabbedPane tabbedPane, int indiceOrigen, int indiceDestino, Color colorDestino) {
        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS || tabbedPane == null) {
            this.animando = false;
            return;
        }

        if (indiceDestino < 0 || indiceDestino >= tabbedPane.getTabCount()) {
            this.animando = false;
            return;
        }

        Rectangle rDestino = tabbedPane.getBoundsAt(indiceDestino);
        if (rDestino == null || rDestino.width <= 0) {
            this.animando = false;
            return;
        }

        Rectangle rOrigen = (indiceOrigen >= 0 && indiceOrigen < tabbedPane.getTabCount())
                ? tabbedPane.getBoundsAt(indiceOrigen)
                : rDestino;

        if (rOrigen == null) {
            rOrigen = rDestino;
        }

        final double startX = rOrigen.x + 2;
        final double startW = Math.max(10, rOrigen.width - 4);
        final double targetX = rDestino.x + 2;
        final double targetW = Math.max(10, rDestino.width - 4);

        if (temporizador != null) {
            temporizador.cancelar();
        }

        this.animando = true;
        this.colorActual = colorDestino;
        this.xActual = startX;
        this.anchoActual = startW;

        temporizador = TemporizadorAnimacion.iniciar(
                TokensMotion.ENTRADA_MS,
                frac -> {
                    this.xActual = startX + ((targetX - startX) * frac);
                    this.anchoActual = startW + ((targetW - startW) * frac);
                    tabbedPane.repaint();
                },
                () -> {
                    this.xActual = targetX;
                    this.anchoActual = targetW;
                    this.animando = false;
                    tabbedPane.repaint();
                }
        );
    }

    public boolean isAnimando() {
        return animando;
    }

    public int getX() {
        return (int) Math.round(xActual);
    }

    public int getAncho() {
        return (int) Math.round(anchoActual);
    }

    public Color getColorActual() {
        return colorActual;
    }
}
