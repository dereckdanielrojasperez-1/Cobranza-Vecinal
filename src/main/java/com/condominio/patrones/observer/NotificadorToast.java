package com.condominio.patrones.observer;

import com.condominio.gui.util.EstilosUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Canal de notificacion visual mediante notificacion flotante emergente (Toast).
@Slf4j
public class NotificadorToast implements ObservadorCobranza {

    @Override
    public void actualizar(String mensaje) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        com.condominio.gui.animacion.AnimadorToast.mostrar(mensaje);
    }

    private void mostrarToast(String mensaje) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        try {
            JWindow toast = new JWindow();
            toast.setAlwaysOnTop(true);
            toast.setContentPane(construirPanelToast(mensaje));
            toast.pack();

            Point ubicacion = calcularUbicacionEsquina(toast);
            toast.setLocation(ubicacion);
            toast.setVisible(true);

            programarAutoCierre(toast);
        } catch (Exception ex) {
            log.warn("No se pudo desplegar la notificacion Toast en pantalla: {}", ex.getMessage());
        }
    }

    private JPanel construirPanelToast(String mensaje) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(EstilosUI.FONDO_TARJETA);
        panel.setBorder(new CompoundBorder(
                new LineBorder(EstilosUI.DORADO_PRINCIPAL, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        com.condominio.gui.util.ChipIcono chipIcono = new com.condominio.gui.util.ChipIcono("CHECK", EstilosUI.VERDE_EXITO, 26, true);
        panel.add(chipIcono, BorderLayout.WEST);

        JPanel pnlTexto = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTexto.setOpaque(false);

        JLabel lblTitulo = new JLabel("PAGO REGISTRADO EXITOSAMENTE");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitulo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel lblMensaje = new JLabel(mensaje);
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensaje.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        pnlTexto.add(lblTitulo);
        pnlTexto.add(lblMensaje);
        panel.add(pnlTexto, BorderLayout.CENTER);
        return panel;
    }

    private Point calcularUbicacionEsquina(JWindow toast) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(defaultScreen.getDefaultConfiguration());

        int x = rect.x + rect.width - toast.getWidth() - insets.right - 20;
        int y = rect.y + rect.height - toast.getHeight() - insets.bottom - 20;
        return new Point(x, y);
    }

    private void programarAutoCierre(JWindow toast) {
        Timer timer = new Timer(3000, e -> ejecutarDesvanecimiento(toast));
        timer.setRepeats(false);
        timer.start();
    }

    private void ejecutarDesvanecimiento(JWindow toast) {
        final float[] opacidad = {1.0f};
        Timer fadeTimer = new Timer(40, null);
        fadeTimer.addActionListener(ev -> {
            opacidad[0] -= 0.1f;
            if (opacidad[0] <= 0.1f) {
                fadeTimer.stop();
                cerrarToast(toast);
                return;
            }
            aplicarOpacidad(toast, opacidad[0], fadeTimer);
        });
        fadeTimer.start();
    }

    private void aplicarOpacidad(JWindow toast, float nivel, Timer fadeTimer) {
        try {
            toast.setOpacity(nivel);
        } catch (Exception ignored) {
            fadeTimer.stop();
            cerrarToast(toast);
        }
    }

    private void cerrarToast(JWindow toast) {
        toast.setVisible(false);
        toast.dispose();
    }
}
