package com.condominio.gui.util;

import javax.swing.*;
import java.awt.*;

// Campo de texto especializado que renderiza un texto fantasma (placeholder)
public class CampoPlaceholder extends JTextField {

    private static final Color COLOR_PLACEHOLDER = new Color(0x6B, 0x72, 0x80); // #6B7280
    private String placeholder;

    public CampoPlaceholder() {
        this("");
    }

    public CampoPlaceholder(String placeholder) {
        super();
        this.placeholder = placeholder != null ? placeholder : "";
    }

    public CampoPlaceholder(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder != null ? placeholder : "";
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if ((getText() == null || getText().isEmpty()) && !hasFocus() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(COLOR_PLACEHOLDER);
            g2.setFont(getFont().deriveFont(Font.ITALIC));

            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int x = insets.left + 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}
