package com.condominio.gui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

// Contenedor corporativo de vidrio esmerilado (Glassmorphism) para Swing.
public class GlassPanel extends JPanel {

    private int radio;
    private Color acento;
    private boolean conSombra;
    private int grosorSombra;
    private float alfaFondoBase;
    private float alfaHighlight;
    private float alfaBorde;

    public GlassPanel(LayoutManager layout, int radio, Color acento, boolean conSombra) {
        super(layout);
        this.radio = radio;
        this.acento = acento;
        this.conSombra = conSombra;
        this.grosorSombra = conSombra ? 4 : 0;
        this.alfaFondoBase = 0.70f;
        this.alfaHighlight = 0.08f;
        this.alfaBorde = 0.15f;
        setOpaque(false);
    }

    public GlassPanel(int radio, Color acento) {
        this(new BorderLayout(), radio, acento, true);
    }

    public GlassPanel(int radio, boolean conSombra, Color acento) {
        this(new BorderLayout(), radio, acento, conSombra);
    }

    public GlassPanel(int radio) {
        this(radio, null);
    }

    public GlassPanel(Color acento) {
        this(24, acento);
    }

    public GlassPanel() {
        this(24, null);
    }

    public GlassPanel(LayoutManager layout) {
        this(layout, 24, null, true);
    }

    public GlassPanel(LayoutManager layout, int radio) {
        this(layout, radio, null, true);
    }

    public GlassPanel(LayoutManager layout, Color acento) {
        this(layout, 24, acento, true);
    }

    public int getRadio() {
        return radio;
    }

    public void setRadio(int radio) {
        this.radio = radio;
        repaint();
    }

    public Color getAcento() {
        return acento;
    }

    public void setAcento(Color acento) {
        this.acento = acento;
        repaint();
    }

    public boolean isConSombra() {
        return conSombra;
    }

    public void setConSombra(boolean conSombra) {
        this.conSombra = conSombra;
        this.grosorSombra = conSombra ? 4 : 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int ancho = getWidth();
        int alto = getHeight();

        int pad = conSombra ? grosorSombra : 0;
        int x = pad;
        int y = pad;
        int w = ancho - (pad * 2) - 1;
        int h = alto - (pad * 2) - 1;

        if (w <= 0 || h <= 0) {
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        // 1. PROFUNDIDAD POR CAPAS: Sombra suave simulada con trazos alfa decrecientes (Regla 3)
        if (conSombra && grosorSombra > 0) {
            for (int s = grosorSombra; s >= 1; s--) {
                int alfaSombra = (int) (14 * ((float) (grosorSombra - s + 1) / grosorSombra));
                g2.setColor(new Color(0, 0, 0, Math.min(255, alfaSombra)));
                g2.draw(new RoundRectangle2D.Float(
                        x - s,
                        y - s + 2,
                        w + (s * 2),
                        h + (s * 2),
                        radio + (s * 2),
                        radio + (s * 2)
                ));
            }
        }

        RoundRectangle2D formaPanel = new RoundRectangle2D.Float(x, y, w, h, radio, radio);

        // 2. RELLENO DE VIDRIO ESMERILADO (Regla 2: #16202E alfa 70% + blanco alfa 6-10%)
        // Base oscura Slate #16202E con alfa 70%
        Color fondoSlate70 = new Color(0x16, 0x20, 0x2E, (int) (255 * alfaFondoBase));
        g2.setColor(fondoSlate70);
        g2.fill(formaPanel);

        // Capa de tinte de vidrio: gradiente blanco neutro (8% a 4%) o acento suave (máximo 8%)
        Color colorTinteTop;
        Color colorTinteBottom;
        if (acento != null) {
            // Tinte acento atenuado al 8% (máximo 12% por regla de diseño)
            int r = acento.getRed();
            int gr = acento.getGreen();
            int b = acento.getBlue();
            colorTinteTop = new Color(r, gr, b, (int) (255 * 0.08f));
            colorTinteBottom = new Color(255, 255, 255, (int) (255 * 0.03f));
        } else {
            // Blanco neutro 8% en la parte superior, 4% en la inferior
            colorTinteTop = new Color(255, 255, 255, (int) (255 * 0.08f));
            colorTinteBottom = new Color(255, 255, 255, (int) (255 * 0.04f));
        }

        GradientPaint gradTinte = new GradientPaint(x, y, colorTinteTop, x, y + h, colorTinteBottom);
        g2.setPaint(gradTinte);
        g2.fill(formaPanel);

        // 3. BORDE DE 1 PIXEL BLANCO ALFA 15% (Regla 2)
        Color bordeBlanco15;
        if (acento != null) {
            // Mezcla suave con el acento para realce perimetral
            int br = Math.min(255, (int) (255 * 0.6 + acento.getRed() * 0.4));
            int bg = Math.min(255, (int) (255 * 0.6 + acento.getGreen() * 0.4));
            int bb = Math.min(255, (int) (255 * 0.6 + acento.getBlue() * 0.4));
            bordeBlanco15 = new Color(br, bg, bb, (int) (255 * alfaBorde));
        } else {
            bordeBlanco15 = new Color(255, 255, 255, (int) (255 * alfaBorde));
        }
        g2.setColor(bordeBlanco15);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(formaPanel);

        // 4. LÍNEA SUPERIOR DE HIGHLIGHT BLANCO ALFA 8% (Regla 2)
        int radioAtenuado = Math.max(4, radio / 2);
        int xInicioHL = x + radioAtenuado;
        int xFinHL = x + w - radioAtenuado;
        if (xFinHL > xInicioHL) {
            g2.setColor(new Color(255, 255, 255, (int) (255 * alfaHighlight)));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(xInicioHL, y + 1, xFinHL, y + 1);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
