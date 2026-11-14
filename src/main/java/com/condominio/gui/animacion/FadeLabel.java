package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;

// Etiqueta de texto (JLabel) con fundido alfa de entrada suave al cambiar de contenido.
public class FadeLabel extends JLabel {

    private float alpha = 1.0f;
    private TemporizadorAnimacion temporizador;

    public FadeLabel(String texto) {
        super(texto);
    }

    public FadeLabel() {
        super();
    }

    // Actualiza el texto con un fundido suave de entrada (0.0 a 1.0 en 180 ms).
    public void setTextAnimado(String nuevoTexto) {
        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS) {
            setText(nuevoTexto);
            this.alpha = 1.0f;
            repaint();
            return;
        }

        if (temporizador != null) {
            temporizador.cancelar();
        }

        setText(nuevoTexto);
        this.alpha = 0.0f;
        repaint();

        temporizador = TemporizadorAnimacion.iniciar(
                TokensMotion.ENTRADA_MS,
                frac -> {
                    this.alpha = frac.floatValue();
                    repaint();
                },
                () -> {
                    this.alpha = 1.0f;
                    repaint();
                }
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha >= 0.99f || GraphicsEnvironment.isHeadless()) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) UtilidadEasing.clamp(alpha, 0.0, 1.0)));
        super.paintComponent(g2);
        g2.dispose();
    }
}
