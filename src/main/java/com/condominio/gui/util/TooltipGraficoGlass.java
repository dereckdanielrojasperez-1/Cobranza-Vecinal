package com.condominio.gui.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Renderizador de tooltip flotante estilo chip de vidrio esmerilado para gráficos 2D.
public final class TooltipGraficoGlass {

    private TooltipGraficoGlass() {
    }

    public static void pintarTooltip(Graphics2D g2, int x, int y, String titulo, String valor, Color acento) {
        if (g2 == null || titulo == null) return;
        Color cAcento = (acento != null) ? acento : EstilosUI.DORADO_PRINCIPAL;

        Font fuenteTit = new Font("Segoe UI", Font.BOLD, 10);
        Font fuenteVal = new Font("Segoe UI", Font.PLAIN, 10);

        FontMetrics fmTit = g2.getFontMetrics(fuenteTit);
        FontMetrics fmVal = g2.getFontMetrics(fuenteVal);

        String textoCompleto = titulo + " | " + valor;
        int anchoTexto = fmTit.stringWidth(titulo) + fmVal.stringWidth(" | " + valor);
        int padH = 10;
        int padV = 6;
        int w = anchoTexto + (padH * 2);
        int h = fmTit.getHeight() + (padV * 2);
        int radio = 10;

        // 1. Sombra suave simulada (2 capas de trazo alfa)
        g2.setColor(new Color(0, 0, 0, 30));
        g2.draw(new RoundRectangle2D.Float(x - 1, y, w + 2, h + 2, radio + 2, radio + 2));
        g2.setColor(new Color(0, 0, 0, 15));
        g2.draw(new RoundRectangle2D.Float(x - 2, y + 1, w + 4, h + 4, radio + 4, radio + 4));

        // 2. Fondo frosted glass oscuro #16202E al 85%
        g2.setColor(new Color(0x16, 0x20, 0x2E, 220));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radio, radio));

        // 3. Tinte blanco neutro 6%
        g2.setColor(new Color(255, 255, 255, 16));
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radio, radio));

        // 4. Borde fino blanco 15% combinado con el acento
        g2.setColor(new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 110));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, radio, radio));

        // 5. Highlight superior blanco 8%
        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawLine(x + 4, y + 1, x + w - 4, y + 1);

        // 6. Texto en alto contraste
        int textY = y + padV + fmTit.getAscent() - 1;
        g2.setFont(fuenteTit);
        g2.setColor(cAcento);
        g2.drawString(titulo, x + padH, textY);

        g2.setFont(fuenteVal);
        g2.setColor(EstilosUI.TEXTO_PRINCIPAL);
        g2.drawString(" | " + valor, x + padH + fmTit.stringWidth(titulo), textY);
    }
}
