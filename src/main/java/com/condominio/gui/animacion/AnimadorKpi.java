package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;

import javax.swing.*;
import java.awt.*;

// Animador de números y métricas ejecutivas para tarjetas KPI.
public class AnimadorKpi {

    public static void animar(JLabel etiqueta, double valorObjetivo, String prefijo, String sufijo, int decimales) {
        if (etiqueta == null) {
            return;
        }

        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS) {
            etiqueta.setText(formatear(valorObjetivo, prefijo, sufijo, decimales));
            return;
        }

        TemporizadorAnimacion.iniciar(
                TokensMotion.ENFASIS_MS,
                frac -> {
                    double actual = valorObjetivo * frac;
                    etiqueta.setText(formatear(actual, prefijo, sufijo, decimales));
                },
                () -> etiqueta.setText(formatear(valorObjetivo, prefijo, sufijo, decimales))
        );
    }

    private static String formatear(double val, String prefijo, String sufijo, int decimales) {
        String p = prefijo != null ? prefijo : "";
        String s = sufijo != null ? sufijo : "";
        if (decimales <= 0) {
            return String.format("%s%,d%s", p, Math.round(val), s);
        } else {
            return String.format("%s%,." + decimales + "f%s", p, val, s);
        }
    }
}
