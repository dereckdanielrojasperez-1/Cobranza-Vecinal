package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;

// Efecto de retroalimentación de destello suave para filas recién insertadas o actualizadas en JTable.
public class DestelloFila {

    public static final String PROP_FILA = "DESTELLO_FILA";
    public static final String PROP_ALPHA = "DESTELLO_ALPHA";
    public static final String PROP_COLOR = "DESTELLO_COLOR";

    public static void resaltar(JTable tabla, int fila, Color acento) {
        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS || tabla == null) {
            return;
        }

        if (fila < 0 || fila >= tabla.getRowCount()) {
            return;
        }

        Color colorDestello = (acento != null) ? acento : new Color(0xE8, 0xA3, 0x3D);
        tabla.putClientProperty(PROP_FILA, fila);
        tabla.putClientProperty(PROP_COLOR, colorDestello);

        TemporizadorAnimacion.iniciar(
                TokensMotion.DESTELLO_MS,
                frac -> {
                    float alpha = (float) UtilidadEasing.clamp((1.0 - frac) * 0.60, 0.0, 0.60);
                    tabla.putClientProperty(PROP_ALPHA, alpha);
                    repintarFila(tabla, fila);
                },
                () -> {
                    tabla.putClientProperty(PROP_ALPHA, 0f);
                    tabla.putClientProperty(PROP_FILA, -1);
                    repintarFila(tabla, fila);
                }
        );
    }

    private static void repintarFila(JTable tabla, int fila) {
        if (fila >= 0 && fila < tabla.getRowCount()) {
            Rectangle rect = tabla.getCellRect(fila, 0, true);
            rect.x = 0;
            rect.width = tabla.getWidth();
            tabla.repaint(rect);
        }
    }
}
