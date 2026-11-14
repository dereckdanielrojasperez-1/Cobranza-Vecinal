package com.condominio.gui.animacion;

import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Notificacion flotante Toast con cinematica de entrada deslizada y salida con desvanecimiento alfa.
@Slf4j
public class AnimadorToast {

    public static void mostrar(String mensaje) {
        String titulo = resolverTituloSegunMensaje(mensaje);
        Color colorAcento = resolverColorSegunMensaje(mensaje);
        mostrar(titulo, mensaje, colorAcento);
    }

    private static String resolverTituloSegunMensaje(String mensaje) {
        if (mensaje == null) return "NOTIFICACIÓN DEL SISTEMA";
        String m = mensaje.toLowerCase();
        if (m.contains("pago") || m.contains("abonado")) return "CONFIRMACIÓN DE PAGO";
        if (m.contains("reserva")) return "RESERVA CONFIRMADA";
        if (m.contains("incidente") || m.contains("avería")) return "REPORTE REGISTRADO";
        if (m.contains("perfil") || m.contains("rol")) return "CONTROL DE ACCESO";
        return "NOTIFICACIÓN DEL SISTEMA";
    }

    private static Color resolverColorSegunMensaje(String mensaje) {
        if (mensaje == null) return EstilosUI.DORADO_PRINCIPAL;
        String m = mensaje.toLowerCase();
        if (m.contains("pago") || m.contains("abonado")) return EstilosUI.VERDE_EXITO;
        if (m.contains("reserva")) return new Color(0x38, 0xBD, 0xF8);
        if (m.contains("incidente") || m.contains("avería")) return EstilosUI.AMBAR_AVISO;
        if (m.contains("perfil") || m.contains("rol")) return EstilosUI.AZUL_INFO;
        return EstilosUI.DORADO_PRINCIPAL;
    }

    public static void mostrar(String titulo, String mensaje) {
        mostrar(titulo, mensaje, EstilosUI.DORADO_PRINCIPAL);
    }

    public static void mostrar(String titulo, String mensaje, Color colorAcento) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        SwingUtilities.invokeLater(() -> desplegarToastSeguro(titulo, mensaje, colorAcento));
    }

    private static void desplegarToastSeguro(String titulo, String mensaje, Color colorAcento) {
        try {
            JWindow ventanaToast = crearVentanaToast(titulo, mensaje, colorAcento);
            Point objetivo = calcularPuntoObjetivo(ventanaToast);
            Point inicial = new Point(objetivo.x + 40, objetivo.y);

            ventanaToast.setLocation(inicial);
            ventanaToast.setVisible(true);

            if (!ConstantesUI.ANIMACIONES_ACTIVAS) {
                desplegarSinAnimacion(ventanaToast, objetivo);
                return;
            }

            ejecutarCinematicaEntrada(ventanaToast, inicial.x, objetivo);
        } catch (Exception excepcion) {
            log.warn("Fallo al presentar AnimadorToast: {}", excepcion.getMessage());
        }
    }

    private static JWindow crearVentanaToast(String titulo, String mensaje, Color colorAcento) {
        JWindow ventanaToast = new JWindow();
        ventanaToast.setAlwaysOnTop(true);
        Color acentoFinal = (colorAcento != null) ? colorAcento : EstilosUI.DORADO_PRINCIPAL;
        ventanaToast.setContentPane(construirContenedorToast(titulo, mensaje, acentoFinal));
        ventanaToast.pack();
        return ventanaToast;
    }

    private static JPanel construirContenedorToast(String titulo, String mensaje, Color acentoFinal) {
        JPanel panelContenedor = new JPanel(new BorderLayout(12, 0));
        panelContenedor.setBackground(EstilosUI.FONDO_TARJETA);
        panelContenedor.setBorder(new CompoundBorder(
                new LineBorder(acentoFinal, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        com.condominio.gui.util.ChipIcono chipIcono = new com.condominio.gui.util.ChipIcono("CHECK", acentoFinal, 26, true);
        panelContenedor.add(chipIcono, BorderLayout.WEST);

        JPanel panelTexto = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTexto.setOpaque(false);

        JLabel etiquetaTitulo = new JLabel(titulo != null ? titulo : "NOTIFICACIÓN DEL SISTEMA");
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiquetaTitulo.setForeground(acentoFinal);

        JLabel etiquetaMensaje = new JLabel(mensaje != null ? mensaje : "");
        etiquetaMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaMensaje.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        panelTexto.add(etiquetaTitulo);
        panelTexto.add(etiquetaMensaje);
        panelContenedor.add(panelTexto, BorderLayout.CENTER);
        return panelContenedor;
    }

    private static Point calcularPuntoObjetivo(JWindow ventanaToast) {
        GraphicsEnvironment entornoGrafico = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dispositivoPantalla = entornoGrafico.getDefaultScreenDevice();
        Rectangle limitesPantalla = dispositivoPantalla.getDefaultConfiguration().getBounds();
        Insets margenesPantalla = Toolkit.getDefaultToolkit().getScreenInsets(dispositivoPantalla.getDefaultConfiguration());

        int x = limitesPantalla.x + limitesPantalla.width - ventanaToast.getWidth() - margenesPantalla.right - 24;
        int y = limitesPantalla.y + limitesPantalla.height - ventanaToast.getHeight() - margenesPantalla.bottom - 24;
        return new Point(x, y);
    }

    private static void desplegarSinAnimacion(JWindow ventanaToast, Point objetivo) {
        ventanaToast.setLocation(objetivo);
        Timer timer = new Timer(2500, evento -> cerrarVentanaToast(ventanaToast));
        timer.setRepeats(false);
        timer.start();
    }

    private static void ejecutarCinematicaEntrada(JWindow ventanaToast, int posXInicial, Point objetivo) {
        TemporizadorAnimacion.iniciar(TokensMotion.ENTRADA_MS, fraccion -> {
            int posXActual = (int) (posXInicial - (40 * fraccion));
            ventanaToast.setLocation(posXActual, objetivo.y);
        }, () -> programarSalidaConDesvanecimiento(ventanaToast, objetivo));
    }

    private static void programarSalidaConDesvanecimiento(JWindow ventanaToast, Point objetivo) {
        ventanaToast.setLocation(objetivo);
        Timer timerPermanencia = new Timer(2500, evento -> ejecutarCinematicaSalida(ventanaToast));
        timerPermanencia.setRepeats(false);
        timerPermanencia.start();
    }

    private static void ejecutarCinematicaSalida(JWindow ventanaToast) {
        TemporizadorAnimacion.iniciarConEasing(
                TokensMotion.SALIDA_MS,
                UtilidadEasing::easeInCubic,
                fraccionSalida -> aplicarDesvanecimientoAlfa(ventanaToast, fraccionSalida),
                () -> cerrarVentanaToast(ventanaToast)
        );
    }

    private static void aplicarDesvanecimientoAlfa(JWindow ventanaToast, double fraccionSalida) {
        float opacidadAlfa = (float) UtilidadEasing.clamp(1.0 - fraccionSalida, 0.0, 1.0);
        try {
            ventanaToast.setOpacity(opacidadAlfa);
        } catch (Exception excepcionIgnorada) {
            // Plataformas sin soporte de translucidez
        }
    }

    private static void cerrarVentanaToast(JWindow ventanaToast) {
        ventanaToast.setVisible(false);
        ventanaToast.dispose();
    }
}
