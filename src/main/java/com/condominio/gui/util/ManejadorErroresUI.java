package com.condominio.gui.util;

import com.condominio.exception.CuotaNoEncontradaException;
import com.condominio.exception.CuotaYaPagadaException;
import com.condominio.exception.DepartamentoDuplicadoException;
import com.condominio.exception.MontoInvalidoException;
import com.condominio.exception.ReservaSolapadaException;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Gestor centralizado de errores y notificaciones visuales para la interfaz de usuario Swing.
@Slf4j
public final class ManejadorErroresUI {

    private ManejadorErroresUI() {
        // Constructor privado para clase utilitaria
    }

    // Registra el fallo en los logs y despliega un cuadro de diálogo informativo de error
    public static void mostrarError(Component padre, String mensaje, Throwable ex) {
        String detalle = (ex != null && ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Consulte el registro de eventos en el log del sistema.";

        int httpCode = 500;
        String httpText = "INTERNAL SERVER ERROR (500)";
        Color accentColor = new Color(0xC0, 0x39, 0x2B); // Rojo oscuro

        if (ex instanceof MontoInvalidoException) {
            httpCode = 400;
            httpText = "BAD REQUEST (400)";
            accentColor = new Color(0xFF, 0xB4, 0x54); // Ámbar
        } else if (ex instanceof CuotaNoEncontradaException) {
            httpCode = 404;
            httpText = "NOT FOUND (404)";
            accentColor = new Color(0x6E, 0xC1, 0xE4); // Cielo
        } else if (ex instanceof CuotaYaPagadaException || ex instanceof ReservaSolapadaException || ex instanceof DepartamentoDuplicadoException) {
            httpCode = 409;
            httpText = "CONFLICT (409)";
            accentColor = new Color(0xFF, 0x6B, 0x6B); // Rojo
        }

        if (ex != null) {
            log.error("[HTTP {}] {}: {}", httpCode, mensaje, detalle, ex);
        } else {
            log.error("[HTTP {}] {}", httpCode, mensaje);
        }

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(0x16, 0x20, 0x2E));
        panel.setBorder(new CompoundBorder(new LineBorder(accentColor, 1), new EmptyBorder(12, 14, 12, 14)));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);
        JLabel badge = new JLabel(" " + httpText + " ");
        badge.setOpaque(true);
        badge.setBackground(accentColor);
        badge.setForeground(httpCode == 400 ? new Color(0x0D, 0x15, 0x20) : Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.add(badge);

        String msgSanitizado = (mensaje != null) ? mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : "";
        JLabel lblMsg = new JLabel("<html><b>" + msgSanitizado + "</b></html>");
        lblMsg.setForeground(new Color(0xDF, 0xE7, 0xF0));
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(lblMsg);
        panel.add(header, BorderLayout.NORTH);

        JTextArea txtDetalle = new JTextArea("Detalle: " + detalle);
        txtDetalle.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtDetalle.setForeground(new Color(0x8F, 0xA3, 0xB8));
        txtDetalle.setBackground(new Color(0x10, 0x1A, 0x26));
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);
        txtDetalle.setEditable(false);
        txtDetalle.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(txtDetalle, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(padre, panel, "Notificación del Sistema", JOptionPane.PLAIN_MESSAGE);
    }

    // Despliega un cuadro de diálogo de advertencia para validaciones de negocio.
    public static void mostrarAdvertencia(Component padre, String mensaje) {
        log.warn("Validación de usuario: {}", mensaje);
        if (GraphicsEnvironment.isHeadless()) return;
        JOptionPane.showMessageDialog(padre, mensaje, "Validación de Datos", JOptionPane.WARNING_MESSAGE);
    }

    // Despliega un cuadro de diálogo de transacción exitosa.
    public static void mostrarExito(Component padre, String mensaje, String titulo) {
        log.info("Operación exitosa en GUI: {}", mensaje);
        if (GraphicsEnvironment.isHeadless()) return;
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
