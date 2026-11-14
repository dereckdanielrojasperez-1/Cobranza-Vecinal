package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// Renderizador de celda para tipos de operación (INCIDENTE / RESERVA) y sus estados asociados.
public class RenderTipoOperacion extends DefaultTableCellRenderer {

    private Color colorFondoPildora;
    private Color colorBordePildora;
    private Color colorTextoPildora;
    private Color colorFondoCelda;
    private boolean filaSeleccionada;

    public RenderTipoOperacion() {
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
            String valor = value.toString().trim().toUpperCase();

            if (valor.contains("INCIDENTE") || valor.equals("ABIERTO") || valor.equals("ALTA") || valor.equals("URGENTE") || valor.equals("VENCIDO")) {
                colorFondoPildora = new Color(255, 107, 107, 45); // Coral suave
                colorBordePildora = new Color(255, 107, 107, 160);
                colorTextoPildora = new Color(255, 138, 138);
            } else if (valor.contains("RESERVA")) {
                colorFondoPildora = new Color(179, 157, 219, 45); // Lavanda suave
                colorBordePildora = new Color(179, 157, 219, 160);
                colorTextoPildora = new Color(209, 196, 233);
            } else if (valor.equals("CONFIRMADA") || valor.equals("PAGADO") || valor.equals("RESUELTO")) {
                colorFondoPildora = new Color(61, 220, 132, 45); // Verde esmeralda suave
                colorBordePildora = new Color(61, 220, 132, 160);
                colorTextoPildora = new Color(61, 220, 132);
            } else if (valor.equals("FINALIZADA") || valor.equals("LIBERADA")) {
                colorFondoPildora = new Color(56, 189, 248, 45); // Azul cielo pastel
                colorBordePildora = new Color(56, 189, 248, 160);
                colorTextoPildora = new Color(56, 189, 248);
            } else if (valor.equals("EN_PROCESO") || valor.equals("PARCIAL") || valor.equals("MEDIA")) {
                colorFondoPildora = new Color(255, 180, 84, 45); // Ámbar suave
                colorBordePildora = new Color(255, 180, 84, 160);
                colorTextoPildora = new Color(255, 195, 115);
            } else if (valor.equals("PENDIENTE") || valor.equals("BAJA")) {
                colorFondoPildora = new Color(77, 163, 255, 45); // Azul cielo suave
                colorBordePildora = new Color(77, 163, 255, 160);
                colorTextoPildora = new Color(96, 175, 255);
            } else {
                colorFondoPildora = new Color(143, 163, 184, 30);
                colorBordePildora = new Color(143, 163, 184, 100);
                colorTextoPildora = EstilosUI.TEXTO_PRINCIPAL;
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
