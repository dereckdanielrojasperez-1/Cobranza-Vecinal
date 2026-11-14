package com.condominio.gui;

import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Pantalla de bienvenida (Splash Screen) corporativa con barra de progreso indeterminada.
public class VentanaSplash extends JWindow {

    private final JLabel lblEstado;
    private final JProgressBar progressBar;

    public VentanaSplash() {
        if (GraphicsEnvironment.isHeadless()) {
            lblEstado = new JLabel();
            progressBar = new JProgressBar();
            return;
        }

        setSize(480, 260);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new CompoundBorder(
                new LineBorder(EstilosUI.DORADO_PRINCIPAL, 2),
                new EmptyBorder(24, 28, 24, 28)
        ));

        // Cabecera
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JLabel lblBadge = new JLabel("SISTEMA DE GESTIÓN CONDOMINAL", SwingConstants.CENTER);
        lblBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel lblTitulo = new JLabel(ConstantesUI.TITULO_APLICACION, SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        JLabel lblSubtitulo = new JLabel(ConstantesUI.SUBTITULO_APLICACION + " | " + ConstantesUI.VERSION, SwingConstants.CENTER);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubtitulo.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSubtitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        pnlHeader.add(lblBadge);
        pnlHeader.add(Box.createVerticalStrut(4));
        pnlHeader.add(lblTitulo);
        pnlHeader.add(Box.createVerticalStrut(4));
        pnlHeader.add(lblSubtitulo);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // Centro: Mensaje de estado
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 8));
        pnlCentro.setOpaque(false);

        lblEstado = new JLabel("Cargando módulos JPA, servicios transaccionales y motor H2...", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        pnlCentro.add(lblEstado, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(420, 8));
        progressBar.setBackground(EstilosUI.FONDO_CAMPO);
        progressBar.setForeground(EstilosUI.DORADO_PRINCIPAL);
        progressBar.setBorder(new LineBorder(EstilosUI.BORDE_PANEL, 1));
        pnlCentro.add(progressBar, BorderLayout.CENTER);

        panel.add(pnlCentro, BorderLayout.CENTER);

        // Pie
        JLabel lblFooter = new JLabel("Plataforma Integral de Administración Condominal | Edición Corporativa", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(EstilosUI.TEXTO_MUTED);
        panel.add(lblFooter, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    public void actualizarMensaje(String mensaje) {
        if (!GraphicsEnvironment.isHeadless() && lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    public void mostrar() {
        if (!GraphicsEnvironment.isHeadless()) {
            if (!ConstantesUI.ANIMACIONES_ACTIVAS) {
                setVisible(true);
                return;
            }
            try {
                setOpacity(0.0f);
            } catch (Exception ignored) {}
            setVisible(true);
            com.condominio.gui.animacion.TemporizadorAnimacion.iniciar(
                    com.condominio.gui.animacion.TokensMotion.ENTRADA_MS,
                    frac -> {
                        try {
                            setOpacity(frac.floatValue());
                        } catch (Exception ignored) {}
                    },
                    () -> {
                        try {
                            setOpacity(1.0f);
                        } catch (Exception ignored) {}
                    }
            );
        }
    }

    public void cerrar() {
        cerrar(null);
    }

    public void cerrar(Runnable onComplete) {
        if (!GraphicsEnvironment.isHeadless()) {
            if (!ConstantesUI.ANIMACIONES_ACTIVAS) {
                setVisible(false);
                dispose();
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }
            com.condominio.gui.animacion.TemporizadorAnimacion.iniciarConEasing(
                    com.condominio.gui.animacion.TokensMotion.SALIDA_MS,
                    com.condominio.gui.animacion.UtilidadEasing::easeInCubic,
                    frac -> {
                        float alpha = (float) com.condominio.gui.animacion.UtilidadEasing.clamp(1.0 - frac, 0.0, 1.0);
                        try {
                            setOpacity(alpha);
                        } catch (Exception ignored) {}
                    },
                    () -> {
                        setVisible(false);
                        dispose();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
            );
        } else if (onComplete != null) {
            onComplete.run();
        }
    }
}
