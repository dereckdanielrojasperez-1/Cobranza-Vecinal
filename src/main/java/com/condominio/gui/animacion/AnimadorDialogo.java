package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Cinética de entrada para diálogos modales oscuros.
public class AnimadorDialogo {

    public static void deslizarEntrada(JComponent panelContenido) {
        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS || panelContenido == null) {
            return;
        }

        final int desplazamientoTotal = 20;
        final Insets insetsBase = panelContenido.getInsets();
        final int baseTop = insetsBase != null ? insetsBase.top : 0;
        final int baseLeft = insetsBase != null ? insetsBase.left : 0;
        final int baseBottom = insetsBase != null ? insetsBase.bottom : 0;
        final int baseRight = insetsBase != null ? insetsBase.right : 0;

        panelContenido.setBorder(new EmptyBorder(
                baseTop + desplazamientoTotal,
                baseLeft,
                Math.max(0, baseBottom - desplazamientoTotal),
                baseRight
        ));
        panelContenido.revalidate();
        panelContenido.repaint();

        TemporizadorAnimacion.iniciar(
                TokensMotion.ENTRADA_MS,
                frac -> {
                    int delta = (int) Math.round(desplazamientoTotal * (1.0 - frac));
                    panelContenido.setBorder(new EmptyBorder(
                            baseTop + delta,
                            baseLeft,
                            Math.max(0, baseBottom - delta),
                            baseRight
                    ));
                    panelContenido.revalidate();
                    panelContenido.repaint();
                },
                () -> {
                    panelContenido.setBorder(new EmptyBorder(baseTop, baseLeft, baseBottom, baseRight));
                    panelContenido.revalidate();
                    panelContenido.repaint();
                }
        );
    }
}
