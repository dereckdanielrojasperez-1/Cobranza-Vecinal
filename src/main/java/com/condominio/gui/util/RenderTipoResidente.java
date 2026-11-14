package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// Renderizador de celda para el tipo de residente (PROPIETARIO / INQUILINO).
public class RenderTipoResidente extends DefaultTableCellRenderer {

    private Color colorFondoPildora;
    private Color colorBordePildora;
    private Color colorTextoPildora;
    private Color colorFondoCelda;
    private boolean filaSeleccionada;

    public RenderTipoResidente() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(getFont().deriveFont(Font.BOLD, 11.5f));
        setBorder(new EmptyBorder(2, 6, 2, 6));

        this.filaSeleccionada = isSelected;
        this.colorFondoCelda = isSelected
                ? table.getSelectionBackground()
                : ((row % 2 == 1) ? EstilosUI.FONDO_TABLA_CEBRA : EstilosUI.FONDO_TARJETA);

        if (value != null) {
            String tipo = value.toString().trim().toUpperCase();
            if (tipo.contains("PROPIETARIO")) {
                // Lavanda pastel suave
                colorFondoPildora = new Color(179, 157, 219, 45);
                colorBordePildora = new Color(179, 157, 219, 160);
                colorTextoPildora = new Color(209, 196, 233);
            } else {
                // Azul cielo / océano pastel suave
                colorFondoPildora = new Color(110, 193, 228, 45);
                colorBordePildora = new Color(110, 193, 228, 160);
                colorTextoPildora = new Color(155, 215, 238);
            }
        } else {
            colorFondoPildora = new Color(0, 0, 0, 0);
            colorBordePildora = new Color(0, 0, 0, 0);
            colorTextoPildora = EstilosUI.TEXTO_PRINCIPAL;
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Pintar fondo de la celda de la tabla
        g2.setColor(colorFondoCelda != null ? colorFondoCelda : EstilosUI.FONDO_TARJETA);
        g2.fillRect(0, 0, getWidth(), getHeight());

        String texto = getText();
        if (texto != null && !texto.isBlank()) {
            FontMetrics fm = g2.getFontMetrics(getFont());
            int altoPildora = 22;
            int yPildora = Math.max(2, (getHeight() - altoPildora) / 2);
            int anchoTexto = fm.stringWidth(texto);
            int anchoPildora = Math.min(getWidth() - 8, anchoTexto + 18);
            int xPildora = (getWidth() - anchoPildora) / 2;

            // 2. Relleno suave con tinte pastel
            if (colorFondoPildora != null) {
                g2.setColor(filaSeleccionada ? new Color(255, 255, 255, 40) : colorFondoPildora);
                g2.fillRoundRect(xPildora, yPildora, anchoPildora, altoPildora, 12, 12);
            }

            // 3. Contorno redondeado sutil
            if (colorBordePildora != null) {
                g2.setColor(filaSeleccionada ? Color.WHITE : colorBordePildora);
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(xPildora, yPildora, anchoPildora - 1, altoPildora - 1, 12, 12);
            }

            // 4. Texto centrado y nítido
            g2.setFont(getFont());
            g2.setColor(filaSeleccionada ? Color.WHITE : colorTextoPildora);
            int xTexto = (getWidth() - anchoTexto) / 2;
            int yTexto = yPildora + ((altoPildora - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(texto, xTexto, yTexto);
        }

        g2.dispose();
    }
}
