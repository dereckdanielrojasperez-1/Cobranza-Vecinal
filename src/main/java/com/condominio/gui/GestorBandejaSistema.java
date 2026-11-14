package com.condominio.gui;

import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

// Administrador de integración con la bandeja del sistema (SystemTray).
@Slf4j
public class GestorBandejaSistema {

    private static TrayIcon trayIcon;
    private static JFrame ventanaActiva;
    private static Runnable accionEstadoCuentaActiva;

    public static void inicializar(JFrame ventana, Runnable accionAbrirEstadoCuenta) {
        registrarVentana(ventana, accionAbrirEstadoCuenta);
    }

    public static void registrarVentana(JFrame ventana, Runnable accionAbrirEstadoCuenta) {
        ventanaActiva = ventana;
        accionEstadoCuentaActiva = accionAbrirEstadoCuenta;

        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            log.info("SystemTray no soportado o en entorno headless. Se omite integración.");
            return;
        }

        if (ventana != null) {
            ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            // Evitar acumular múltiples listeners si se reasigna la ventana
            for (java.awt.event.WindowListener wl : ventana.getWindowListeners()) {
                ventana.removeWindowListener(wl);
            }
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int opc = JOptionPane.showOptionDialog(
                            ventana,
                            "¿Desea cerrar el sistema por completo o minimizarlo a la bandeja en segundo plano?",
                            ConstantesUI.TITULO_APLICACION,
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new Object[]{"Salir del Sistema", "Minimizar a Bandeja", "Cancelar"},
                            "Salir del Sistema"
                    );
                    if (opc == 0) { // Salir del Sistema
                        removerTray();
                        System.exit(0);
                    } else if (opc == 1) { // Minimizar a Bandeja
                        ventana.setVisible(false);
                        if (trayIcon != null) {
                            trayIcon.displayMessage(
                                    ConstantesUI.TITULO_APLICACION,
                                    "La aplicación continúa ejecutándose en segundo plano. Haga clic en el ícono para reabrir.",
                                    TrayIcon.MessageType.INFO
                            );
                        }
                    }
                }
            });
        }

        if (trayIcon == null) {
            crearTrayIcon();
        }
    }

    private static void crearTrayIcon() {
        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Ícono nativo generado dinámicamente (16x16) con acento dorado
            BufferedImage iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = iconImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(EstilosUI.FONDO_PRINCIPAL);
            g2d.fillRoundRect(0, 0, 16, 16, 4, 4);
            g2d.setColor(EstilosUI.DORADO_PRINCIPAL);
            g2d.drawRoundRect(0, 0, 15, 15, 4, 4);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.drawString("C", 4, 12);
            g2d.dispose();

            PopupMenu popup = new PopupMenu();

            MenuItem mnuAbrir = new MenuItem("Abrir " + ConstantesUI.TITULO_APLICACION);
            mnuAbrir.addActionListener(e -> {
                if (ventanaActiva != null) {
                    ventanaActiva.setVisible(true);
                    ventanaActiva.setExtendedState(Frame.NORMAL);
                    ventanaActiva.toFront();
                }
            });
            popup.add(mnuAbrir);

            MenuItem mnuEstadoCuenta = new MenuItem("Estado de Cuenta Vecino");
            mnuEstadoCuenta.addActionListener(e -> {
                if (ventanaActiva != null) {
                    ventanaActiva.setVisible(true);
                    ventanaActiva.setExtendedState(Frame.NORMAL);
                    ventanaActiva.toFront();
                }
                if (accionEstadoCuentaActiva != null) {
                    accionEstadoCuentaActiva.run();
                }
            });
            popup.add(mnuEstadoCuenta);

            MenuItem mnuCerrarSesion = new MenuItem("Cerrar Sesión / Cambiar Rol");
            mnuCerrarSesion.addActionListener(e -> {
                if (ventanaActiva instanceof VentanaPrincipal vp) {
                    vp.setVisible(true);
                    vp.setExtendedState(Frame.NORMAL);
                    vp.toFront();
                    vp.cerrarSesionYCambiarRol();
                }
            });
            popup.add(mnuCerrarSesion);

            popup.addSeparator();

            MenuItem mnuSalir = new MenuItem("Salir de la Aplicación");
            mnuSalir.addActionListener(e -> {
                log.info("Cerrando aplicación desde SystemTray.");
                removerTray();
                System.exit(0);
            });
            popup.add(mnuSalir);

            trayIcon = new TrayIcon(iconImage, ConstantesUI.TITULO_APLICACION + " " + ConstantesUI.VERSION, popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> {
                if (ventanaActiva != null) {
                    ventanaActiva.setVisible(true);
                    ventanaActiva.setExtendedState(Frame.NORMAL);
                    ventanaActiva.toFront();
                }
            });

            tray.add(trayIcon);
            log.info("SystemTray configurado exitosamente para {}", ConstantesUI.TITULO_APLICACION);
        } catch (Exception ex) {
            log.warn("No se pudo registrar el ícono en la bandeja del sistema: {}", ex.getMessage());
        }
    }

    public static void removerTray() {
        if (trayIcon != null && SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
                trayIcon = null;
            } catch (Exception ignored) {
            }
        }
    }

    public static void notificar(String titulo, String mensaje, TrayIcon.MessageType tipo) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo, mensaje, tipo);
        }
    }
}
