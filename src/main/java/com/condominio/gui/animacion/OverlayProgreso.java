package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Capa de superposición visual (Overlay) para procesos asíncronos y SwingWorkers.
public class OverlayProgreso extends JPanel {

    private float alphaFondo = 0.0f;
    private final JLabel lblMensaje;
    private final JProgressBar progressBar;
    private final JPanel tarjetaCentral;
    private TemporizadorAnimacion timerActivo;

    public OverlayProgreso(String mensaje, Color acento) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        Color colorAcento = (acento != null) ? acento : EstilosUI.DORADO_PRINCIPAL;

        tarjetaCentral = new JPanel(new BorderLayout(0, 12));
        tarjetaCentral.setBackground(EstilosUI.FONDO_TARJETA);
        tarjetaCentral.setBorder(new CompoundBorder(
                new LineBorder(colorAcento, 1),
                new EmptyBorder(16, 24, 16, 24)
        ));

        lblMensaje = new JLabel(mensaje, SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMensaje.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        tarjetaCentral.add(lblMensaje, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(280, 8));
        progressBar.setBackground(EstilosUI.FONDO_CAMPO);
        progressBar.setForeground(colorAcento);
        progressBar.setBorder(new LineBorder(EstilosUI.BORDE_PANEL, 1));
        tarjetaCentral.add(progressBar, BorderLayout.CENTER);

        add(tarjetaCentral, new GridBagConstraints());
    }

    // Despliega la superposición sobre el área del componente objetivo.
    public static OverlayProgreso mostrar(JComponent componenteObjetivo, String mensaje, Color acento) {
        if (GraphicsEnvironment.isHeadless() || componenteObjetivo == null) {
            return null;
        }

        JRootPane root = componenteObjetivo.getRootPane();
        if (root == null) {
            return null;
        }

        JLayeredPane layeredPane = root.getLayeredPane();
        if (layeredPane == null) {
            return null;
        }

        OverlayProgreso overlay = new OverlayProgreso(mensaje, acento);
        overlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        layeredPane.add(overlay, JLayeredPane.MODAL_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();

        if (!ConstantesUI.ANIMACIONES_ACTIVAS) {
            overlay.alphaFondo = 0.40f;
            overlay.repaint();
            return overlay;
        }

        overlay.timerActivo = TemporizadorAnimacion.iniciar(
                TokensMotion.ENTRADA_MS,
                frac -> {
                    overlay.alphaFondo = (float) UtilidadEasing.clamp(frac * 0.40, 0.0, 0.40);
                    overlay.repaint();
                },
                () -> {
                    overlay.alphaFondo = 0.40f;
                    overlay.repaint();
                }
        );

        return overlay;
    }

    // Oculta el overlay con fundido de salida y lo remueve del LayeredPane.
    public void ocultar(Runnable onTerminar) {
        if (timerActivo != null) {
            timerActivo.cancelar();
        }

        if (GraphicsEnvironment.isHeadless() || !ConstantesUI.ANIMACIONES_ACTIVAS) {
            removerDePadre(onTerminar);
            return;
        }

        timerActivo = TemporizadorAnimacion.iniciarConEasing(
                TokensMotion.SALIDA_MS,
                UtilidadEasing::easeInCubic,
                frac -> {
                    alphaFondo = (float) UtilidadEasing.clamp((1.0 - frac) * 0.40, 0.0, 0.40);
                    repaint();
                },
                () -> removerDePadre(onTerminar)
        );
    }

    private void removerDePadre(Runnable onTerminar) {
        Container padre = getParent();
        if (padre != null) {
            padre.remove(this);
            padre.repaint();
        }
        if (onTerminar != null) {
            onTerminar.run();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alphaFondo > 0.001f) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x0B, 0x13, 0x1E, (int) (255 * alphaFondo)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
