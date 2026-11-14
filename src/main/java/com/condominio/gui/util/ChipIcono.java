package com.condominio.gui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Componente visual de micro-vidrio (Glass Chip) con icono vectorial nativo.
public class ChipIcono extends JComponent {

    private final String tipoIcono;
    private final Color colorAcento;
    private final int tamano;
    private final boolean circular;

    public ChipIcono(String tipoIcono, Color colorAcento, int tamano, boolean circular) {
        this.tipoIcono = (tipoIcono != null) ? tipoIcono.toUpperCase() : "DEFECTO";
        this.colorAcento = (colorAcento != null) ? colorAcento : EstilosUI.DORADO_PRINCIPAL;
        this.tamano = tamano;
        this.circular = circular;
        setPreferredSize(new Dimension(tamano, tamano));
        setMinimumSize(new Dimension(tamano, tamano));
        setMaximumSize(new Dimension(tamano, tamano));
        setOpaque(false);
    }

    public ChipIcono(String tipoIcono, Color colorAcento, int tamano) {
        this(tipoIcono, colorAcento, tamano, true);
    }

    public ChipIcono(String tipoIcono, Color colorAcento) {
        this(tipoIcono, colorAcento, 32, true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();

        // 1. Fondo de vidrio traslúcido para el chip
        Color fondoChip = new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 30);
        g2.setColor(fondoChip);
        if (circular) {
            g2.fillOval(1, 1, w - 2, h - 2);
        } else {
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
        }

        // 2. Borde sutil blanco / acento (alfa 15%)
        g2.setColor(new Color(255, 255, 255, 38));
        g2.setStroke(new BasicStroke(1.0f));
        if (circular) {
            g2.drawOval(1, 1, w - 3, h - 3);
        } else {
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 8, 8));
        }

        // 3. Highlight superior de 1px
        g2.setColor(new Color(255, 255, 255, 25));
        if (circular) {
            g2.drawArc(3, 2, w - 6, (h - 4) / 2, 20, 140);
        } else {
            g2.drawLine(4, 2, w - 4, 2);
        }

        // 4. Dibujo vectorial del glifo según el tipo
        int cx = w / 2;
        int cy = h / 2;
        g2.setColor(colorAcento);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        dibujarGlifo(g2, cx, cy);

        g2.dispose();
    }

    private void dibujarGlifo(Graphics2D g2, int cx, int cy) {
        switch (tipoIcono) {
            case "RECAUDACION":
            case "PAGO":
            case "MONEDA":
                // Símbolo de moneda / billetera
                g2.drawRoundRect(cx - 7, cy - 5, 14, 10, 3, 3);
                g2.drawLine(cx - 3, cy, cx + 3, cy);
                g2.fillOval(cx + 1, cy - 1, 2, 2);
                break;

            case "DEUDA":
            case "SALDO":
            case "ALERTA":
                // Escudo o triángulo de aviso
                int[] tx = {cx, cx + 6, cx - 6};
                int[] ty = {cy - 6, cy + 5, cy + 5};
                g2.drawPolygon(tx, ty, 3);
                g2.drawLine(cx, cy - 2, cx, cy + 1);
                g2.fillOval(cx - 1, cy + 3, 2, 2);
                break;

            case "MOROSIDAD":
            case "TENDENCIA":
                // Mini gráfico de barras ascendentes
                g2.drawLine(cx - 6, cy + 5, cx - 6, cy + 1);
                g2.drawLine(cx - 2, cy + 5, cx - 2, cy - 2);
                g2.drawLine(cx + 2, cy + 5, cx + 2, cy - 5);
                g2.drawLine(cx + 6, cy + 5, cx + 6, cy);
                break;

            case "OCUPACION":
            case "INMUEBLE":
            case "CASA":
                // Silueta de casa / edificio
                int[] px = {cx - 6, cx, cx + 6, cx + 6, cx - 6};
                int[] py = {cy - 1, cy - 6, cy - 1, cy + 6, cy + 6};
                g2.drawPolygon(px, py, 5);
                g2.drawLine(cx - 2, cy + 6, cx - 2, cy + 1);
                g2.drawLine(cx + 2, cy + 6, cx + 2, cy + 1);
                g2.drawLine(cx - 2, cy + 1, cx + 2, cy + 1);
                break;

            case "FONDO":
            case "RESERVA_FONDO":
            case "SEGURIDAD":
                // Escudo protector con símbolo de valor o fondo de reserva
                int[] sx = {cx, cx + 6, cx + 5, cx, cx - 5, cx - 6};
                int[] sy = {cy - 6, cy - 4, cy + 3, cy + 7, cy + 3, cy - 4};
                g2.drawPolygon(sx, sy, 6);
                g2.drawLine(cx, cy - 3, cx, cy + 4);
                break;

            case "COMUNICADOS":
            case "MEGAPHONE":
            case "TABLON":
                // Megáfono o tablón oficial
                g2.drawRoundRect(cx - 5, cy - 4, 8, 8, 2, 2);
                g2.drawLine(cx + 3, cy - 3, cx + 6, cy - 5);
                g2.drawLine(cx + 3, cy + 3, cx + 6, cy + 5);
                break;

            case "CONFIGURACION":
            case "POLITICAS":
            case "AJUSTES":
                // Engranaje de configuración
                g2.drawOval(cx - 3, cy - 3, 6, 6);
                g2.drawLine(cx, cy - 6, cx, cy + 6);
                g2.drawLine(cx - 6, cy, cx + 6, cy);
                break;

            case "TESTS":
            case "COBERTURA":
            case "CHECK":
                // Escudo de verificación o checkmark
                g2.drawArc(cx - 6, cy - 6, 12, 12, 0, 360);
                g2.drawLine(cx - 3, cy, cx - 1, cy + 3);
                g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);
                break;

            case "RESERVA":
            case "CALENDARIO":
                // Calendario
                g2.drawRoundRect(cx - 6, cy - 5, 12, 11, 2, 2);
                g2.drawLine(cx - 6, cy - 1, cx + 6, cy - 1);
                g2.drawLine(cx - 3, cy - 7, cx - 3, cy - 5);
                g2.drawLine(cx + 3, cy - 7, cx + 3, cy - 5);
                break;

            case "INCIDENCIA":
            case "HERRAMIENTA":
                // Llave o exclamación
                g2.drawLine(cx - 4, cy + 4, cx + 4, cy - 4);
                g2.drawOval(cx + 1, cy - 6, 5, 5);
                break;

            case "SEARCH":
            case "BUSQUEDA":
                // Lupa
                g2.drawOval(cx - 5, cy - 5, 8, 8);
                g2.drawLine(cx + 1, cy + 1, cx + 5, cy + 5);
                break;

            case "BELL":
            case "NOTIFICACION":
                // Campana
                g2.drawArc(cx - 5, cy - 4, 10, 8, 0, 180);
                g2.drawLine(cx - 5, cy, cx + 5, cy);
                g2.drawLine(cx - 1, cy + 3, cx + 1, cy + 3);
                // Punto indicador
                g2.setColor(EstilosUI.VERDE_EXITO);
                g2.fillOval(cx + 2, cy - 5, 4, 4);
                break;

            case "HELP":
            case "CHAT":
            case "AYUDA":
                // Globo de diálogo
                g2.drawRoundRect(cx - 6, cy - 5, 12, 9, 3, 3);
                g2.drawLine(cx - 3, cy + 4, cx - 5, cy + 7);
                g2.drawLine(cx - 5, cy + 7, cx, cy + 4);
                break;

            default:
                // Círculo neutro
                g2.drawOval(cx - 4, cy - 4, 8, 8);
                break;
        }
    }
}
