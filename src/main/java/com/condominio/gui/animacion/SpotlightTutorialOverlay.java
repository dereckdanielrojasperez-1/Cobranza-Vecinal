package com.condominio.gui.animacion;

import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.service.chatbot.SintetizadorVoz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Componente interactivo tipo GlassPane para el Modo Instructivo / Spotlight Tutorial.
// Oscurece la pantalla completa y resalta el componente objetivo con borde luminoso y tarjeta flotante.
public class SpotlightTutorialOverlay extends JComponent {

    public static class PasoTutorial {
        private final Component componente;
        private final String titulo;
        private final String explicacion;
        private final String narracionVoz;

        public PasoTutorial(Component componente, String titulo, String explicacion, String narracionVoz) {
            this.componente = componente;
            this.titulo = titulo;
            this.explicacion = explicacion;
            this.narracionVoz = (narracionVoz != null && !narracionVoz.isBlank()) ? narracionVoz : explicacion;
        }

        public PasoTutorial(Component componente, String titulo, String explicacion) {
            this(componente, titulo, explicacion, explicacion);
        }

        public Component getComponente() { return componente; }
        public String getTitulo() { return titulo; }
        public String getExplicacion() { return explicacion; }
        public String getNarracionVoz() { return narracionVoz; }
    }

    private final JFrame ventanaPadre;
    private final List<PasoTutorial> pasos = new ArrayList<>();
    private int indicePasoActual = 0;
    private Consumer<Boolean> callbackFinalizacion;

    // Componentes de la tarjeta flotante
    private final GlassPanel tarjetaExplicacion;
    private final JLabel lblPasoContador;
    private final JLabel lblTituloPaso;
    private final JTextArea txtExplicacion;
    private final JButton btnSaltar;
    private final JButton btnAnterior;
    private final JButton btnSiguiente;
    private final JButton btnEscucharVoz;

    public SpotlightTutorialOverlay(JFrame ventanaPadre) {
        this.ventanaPadre = ventanaPadre;
        setLayout(null);
        setOpaque(false);
        setFocusable(true);

        // Bloquear clics fuera de la tarjeta explicativa para mantener el foco en el tutorial
        MouseAdapter bloqueadorMouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }
        };
        addMouseListener(bloqueadorMouse);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    finalizarTutorial(false);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    avanzarPaso();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    retrocederPaso();
                }
            }
        });

        // Construcción de la tarjeta explicativa flotante
        tarjetaExplicacion = new GlassPanel(new BorderLayout(0, 10), 20, EstilosUI.DORADO_PRINCIPAL, true);
        tarjetaExplicacion.setBorder(new EmptyBorder(16, 20, 16, 20));
        tarjetaExplicacion.setSize(440, 220);

        // Cabecera de la tarjeta
        JPanel panelCabecera = new JPanel(new BorderLayout(8, 0));
        panelCabecera.setOpaque(false);

        lblPasoContador = new JLabel("TUTORIAL GUIADO - PASO 1 DE 1");
        lblPasoContador.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPasoContador.setForeground(EstilosUI.DORADO_PRINCIPAL);

        btnEscucharVoz = new JButton("Escuchar");
        btnEscucharVoz.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEscucharVoz.setForeground(new Color(0x38, 0xBD, 0xF8));
        btnEscucharVoz.setContentAreaFilled(false);
        btnEscucharVoz.setBorderPainted(false);
        btnEscucharVoz.setFocusPainted(false);
        btnEscucharVoz.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEscucharVoz.setToolTipText("Reproducir explicación en audio TTS");
        btnEscucharVoz.addActionListener(e -> reproducirAudioPasoActual());

        panelCabecera.add(lblPasoContador, BorderLayout.WEST);
        panelCabecera.add(btnEscucharVoz, BorderLayout.EAST);

        lblTituloPaso = new JLabel("Título del Módulo");
        lblTituloPaso.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloPaso.setForeground(EstilosUI.TEXTO_CLARO);

        JPanel panelNorte = new JPanel(new BorderLayout(0, 4));
        panelNorte.setOpaque(false);
        panelNorte.add(panelCabecera, BorderLayout.NORTH);
        panelNorte.add(lblTituloPaso, BorderLayout.CENTER);
        tarjetaExplicacion.add(panelNorte, BorderLayout.NORTH);

        // Cuerpo explicativo
        txtExplicacion = new JTextArea();
        txtExplicacion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtExplicacion.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        txtExplicacion.setOpaque(false);
        txtExplicacion.setEditable(false);
        txtExplicacion.setLineWrap(true);
        txtExplicacion.setWrapStyleWord(true);
        txtExplicacion.setFocusable(false);
        tarjetaExplicacion.add(txtExplicacion, BorderLayout.CENTER);

        // Botonera de control
        JPanel panelBotones = new JPanel(new BorderLayout(10, 0));
        panelBotones.setOpaque(false);

        btnSaltar = new JButton("Saltar Tutorial");
        EstilosUI.estilizarBotonSecundario(btnSaltar, EstilosUI.ROJO_ERROR_TEXTO);
        btnSaltar.setPreferredSize(new Dimension(130, 32));
        btnSaltar.addActionListener(e -> finalizarTutorial(false));

        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelNavegacion.setOpaque(false);

        btnAnterior = new JButton("Anterior");
        EstilosUI.estilizarBotonSecundario(btnAnterior, EstilosUI.AZUL_INFO);
        btnAnterior.setPreferredSize(new Dimension(95, 32));
        btnAnterior.addActionListener(e -> retrocederPaso());

        btnSiguiente = new JButton("Siguiente Paso");
        EstilosUI.estilizarBotonPrimario(btnSiguiente);
        btnSiguiente.setPreferredSize(new Dimension(135, 32));
        btnSiguiente.addActionListener(e -> avanzarPaso());

        panelNavegacion.add(btnAnterior);
        panelNavegacion.add(btnSiguiente);

        panelBotones.add(btnSaltar, BorderLayout.WEST);
        panelBotones.add(panelNavegacion, BorderLayout.EAST);
        tarjetaExplicacion.add(panelBotones, BorderLayout.SOUTH);

        add(tarjetaExplicacion);
    }

    public void registrarPaso(Component comp, String titulo, String explicacion) {
        pasos.add(new PasoTutorial(comp, titulo, explicacion));
    }

    public void registrarPaso(Component comp, String titulo, String explicacion, String narracionVoz) {
        pasos.add(new PasoTutorial(comp, titulo, explicacion, narracionVoz));
    }

    public void limpiarPasos() {
        pasos.clear();
        indicePasoActual = 0;
    }

    public void iniciar(Consumer<Boolean> callbackFinalizacion) {
        if (pasos.isEmpty()) {
            return;
        }
        this.callbackFinalizacion = callbackFinalizacion;
        this.indicePasoActual = 0;

        if (ventanaPadre != null) {
            ventanaPadre.setGlassPane(this);
            setVisible(true);
            requestFocusInWindow();
        }

        actualizarEstadoVisualPaso();
    }

    public void avanzarPaso() {
        if (indicePasoActual < pasos.size() - 1) {
            indicePasoActual++;
            actualizarEstadoVisualPaso();
        } else {
            finalizarTutorial(true);
        }
    }

    public void retrocederPaso() {
        if (indicePasoActual > 0) {
            indicePasoActual--;
            actualizarEstadoVisualPaso();
        }
    }

    public void finalizarTutorial(boolean completadoExitosamente) {
        SintetizadorVoz.detenerAudio();
        setVisible(false);
        if (ventanaPadre != null) {
            ventanaPadre.getGlassPane().setVisible(false);
        }
        if (callbackFinalizacion != null) {
            callbackFinalizacion.accept(completadoExitosamente);
        }
    }

    private void actualizarEstadoVisualPaso() {
        if (indicePasoActual < 0 || indicePasoActual >= pasos.size()) {
            return;
        }

        PasoTutorial paso = pasos.get(indicePasoActual);
        lblPasoContador.setText("TUTORIAL GUIADO - PASO " + (indicePasoActual + 1) + " DE " + pasos.size());
        lblTituloPaso.setText(paso.getTitulo());
        txtExplicacion.setText(paso.getExplicacion());

        btnAnterior.setEnabled(indicePasoActual > 0);
        btnSiguiente.setText((indicePasoActual == pasos.size() - 1) ? "Finalizar" : "Siguiente Paso");

        posicionarTarjetaFlotante(paso.getComponente());
        repaint();

        // Si el sintetizador de voz está habilitado, narra automáticamente el paso
        if (SintetizadorVoz.isHabilitado()) {
            reproducirAudioPasoActual();
        }
    }

    private void reproducirAudioPasoActual() {
        if (indicePasoActual >= 0 && indicePasoActual < pasos.size()) {
            PasoTutorial paso = pasos.get(indicePasoActual);
            String locucion = paso.getTitulo() + ". " + paso.getNarracionVoz();
            SintetizadorVoz.reproducirTextoAsync(locucion);
        }
    }

    private void posicionarTarjetaFlotante(Component objetivo) {
        int anchoVentana = getWidth() > 0 ? getWidth() : ventanaPadre.getWidth();
        int altoVentana = getHeight() > 0 ? getHeight() : ventanaPadre.getHeight();

        int tw = tarjetaExplicacion.getWidth();
        int th = tarjetaExplicacion.getHeight();

        if (objetivo == null || !objetivo.isShowing()) {
            // Centrar tarjeta en pantalla si el componente no es visible
            tarjetaExplicacion.setLocation((anchoVentana - tw) / 2, (altoVentana - th) / 2);
            return;
        }

        Point p = SwingUtilities.convertPoint(objetivo, 0, 0, this);
        int ox = p.x;
        int oy = p.y;
        int ow = objetivo.getWidth();
        int oh = objetivo.getHeight();

        int targetX;
        int targetY;

        // Si hay espacio debajo del componente objetivo
        if (oy + oh + th + 20 < altoVentana) {
            targetY = oy + oh + 14;
            targetX = Math.max(20, Math.min(ox, anchoVentana - tw - 20));
        } else if (oy - th - 20 > 0) {
            // Espacio arriba
            targetY = oy - th - 14;
            targetX = Math.max(20, Math.min(ox, anchoVentana - tw - 20));
        } else if (ox + ow + tw + 20 < anchoVentana) {
            // Espacio a la derecha
            targetX = ox + ow + 14;
            targetY = Math.max(20, Math.min(oy, altoVentana - th - 20));
        } else {
            // Espacio a la izquierda o centro
            targetX = Math.max(20, ox - tw - 14);
            targetY = Math.max(20, Math.min(oy, altoVentana - th - 20));
        }

        tarjetaExplicacion.setLocation(targetX, targetY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible() || pasos.isEmpty() || indicePasoActual >= pasos.size()) {
            return;
        }

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Crear buffer con máscara alpha oscura
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gb = buffer.createGraphics();
        gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Capa oscura general que cubre toda la interfaz (190 de opacidad)
        gb.setColor(new Color(10, 15, 22, 215));
        gb.fillRect(0, 0, w, h);

        // 2. Recorte tipo Spotlight (Clear) sobre el componente objetivo
        Component objetivo = pasos.get(indicePasoActual).getComponente();
        Rectangle boundsHighlight = null;

        if (objetivo != null && objetivo.isShowing()) {
            Point p = SwingUtilities.convertPoint(objetivo, 0, 0, this);
            int pad = 6;
            boundsHighlight = new Rectangle(p.x - pad, p.y - pad, objetivo.getWidth() + (pad * 2), objetivo.getHeight() + (pad * 2));

            gb.setComposite(AlphaComposite.Clear);
            gb.fill(new RoundRectangle2D.Float(boundsHighlight.x, boundsHighlight.y, boundsHighlight.width, boundsHighlight.height, 16, 16));
        }
        gb.dispose();

        g2.drawImage(buffer, 0, 0, null);

        // 3. Dibujar borde luminoso alrededor del área resaltada (Spotlight)
        if (boundsHighlight != null) {
            // Borde exterior suave dorado/cyan
            g2.setColor(new Color(0x38, 0xBD, 0xF8, 120));
            g2.setStroke(new BasicStroke(3.5f));
            g2.draw(new RoundRectangle2D.Float(boundsHighlight.x, boundsHighlight.y, boundsHighlight.width, boundsHighlight.height, 16, 16));

            // Borde interior fino dorado brillante
            g2.setColor(EstilosUI.DORADO_PRINCIPAL);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(boundsHighlight.x, boundsHighlight.y, boundsHighlight.width, boundsHighlight.height, 16, 16));
        }

        g2.dispose();
    }
}
