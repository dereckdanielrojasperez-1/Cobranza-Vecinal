package com.condominio.gui;

import com.condominio.gui.animacion.AnimadorToast;
import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.service.chatbot.IChatbotService;
import com.condominio.service.chatbot.MensajeChatbotDTO;
import com.condominio.service.chatbot.RespuestaChatbotDTO;
import com.condominio.service.chatbot.SintetizadorVoz;
import com.condominio.service.chatbot.TipoRespuestaChatbot;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Ventana flotante interactiva del Asistente Virtual IA con control de voz, modo limpieza y tutorial spotlight.
@Slf4j
public class DialogoChatbotAsistente extends JDialog {

    private final IChatbotService chatbotService;
    private final Runnable callbackModoInstructivo;

    private JPanel panelHistorialMensajes;
    private JScrollPane scrollHistorial;
    private JTextField txtEntradaMensaje;
    private JButton btnEnviar;
    private JButton btnModoVoz;
    private JButton btnLimpiarChat;
    private JButton btnModoInstructivo;
    private JPanel panelSugerencias;

    public DialogoChatbotAsistente(Frame padre, IChatbotService chatbotService, Runnable callbackModoInstructivo) {
        super(padre, "Asistente Virtual IA - " + ConstantesUI.TITULO_APLICACION, false);
        this.chatbotService = chatbotService;
        this.callbackModoInstructivo = callbackModoInstructivo;

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        configurarVentana();
        inicializarComponentes();
        cargarHistorialInicial();
    }

    private void configurarVentana() {
        setSize(560, 680);
        setMinimumSize(new Dimension(480, 560));
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(0, 0));

        JPanel panelContenedor = new JPanel(new BorderLayout(0, 10));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. CABECERA SUPERIOR MODERNA
        JPanel panelCabecera = crearCabecera();
        panelContenedor.add(panelCabecera, BorderLayout.NORTH);

        // 2. PANEL CENTRAL DE CONVERSACIÓN
        panelHistorialMensajes = new JPanel();
        panelHistorialMensajes.setLayout(new BoxLayout(panelHistorialMensajes, BoxLayout.Y_AXIS));
        panelHistorialMensajes.setBackground(new Color(0x0E, 0x14, 0x1F));
        panelHistorialMensajes.setBorder(new EmptyBorder(12, 12, 12, 12));

        scrollHistorial = new JScrollPane(panelHistorialMensajes);
        scrollHistorial.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1, true));
        scrollHistorial.getViewport().setBackground(new Color(0x0E, 0x14, 0x1F));
        scrollHistorial.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollHistorial.getVerticalScrollBar().setUnitIncrement(16);

        panelContenedor.add(scrollHistorial, BorderLayout.CENTER);

        // 3. PIE DE PÁGINA (PÍLDORAS DE SUGERENCIA + BARRA DE ENTRADA)
        JPanel panelSur = new JPanel(new BorderLayout(0, 8));
        panelSur.setOpaque(false);

        panelSugerencias = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panelSugerencias.setOpaque(false);
        actualizarPildorasSugerencia();
        panelSur.add(panelSugerencias, BorderLayout.NORTH);

        JPanel panelBarraEntrada = crearBarraEntrada();
        panelSur.add(panelBarraEntrada, BorderLayout.SOUTH);

        panelContenedor.add(panelSur, BorderLayout.SOUTH);
        add(panelContenedor, BorderLayout.CENTER);
    }

    private JPanel crearCabecera() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstilosUI.BORDE_LINEA),
                new EmptyBorder(0, 0, 10, 0)
        ));

        // Título y Perfil Activo
        JPanel panelInfo = new JPanel(new BorderLayout(0, 2));
        panelInfo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Asistente Virtual IA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        RolUsuario rol = SesionActual.getRol();
        String descRol = (rol != null) ? rol.name() : "RESIDENTE";
        JLabel lblRol = new JLabel("Perfil activo: " + descRol + " | Modo RBAC");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRol.setForeground(EstilosUI.TEXTO_MUTED);

        panelInfo.add(lblTitulo, BorderLayout.NORTH);
        panelInfo.add(lblRol, BorderLayout.SOUTH);
        header.add(panelInfo, BorderLayout.WEST);

        // Botonera de acciones superiores
        JPanel panelAccionesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelAccionesHeader.setOpaque(false);

        // Botón Modo Instructivo (Tutorial Spotlight)
        btnModoInstructivo = new JButton("Modo Instructivo");
        btnModoInstructivo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnModoInstructivo.setFocusPainted(false);
        btnModoInstructivo.setContentAreaFilled(false);
        btnModoInstructivo.setOpaque(false);
        btnModoInstructivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModoInstructivo.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnModoInstructivo.setToolTipText("Activar recorrido guiado con iluminación y explicación de botones");
        btnModoInstructivo.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fondo = model.isPressed() ? new Color(0x1C, 0x38, 0x56) :
                        (model.isRollover() ? new Color(0x18, 0x2E, 0x48) : new Color(0x10, 0x1E, 0x30));
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);
                g2.setColor(new Color(0x38, 0xBD, 0xF8));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);

                FontMetrics fm = g2.getFontMetrics(b.getFont());
                g2.setColor(new Color(0x38, 0xBD, 0xF8));
                int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(b.getText(), textX, textY);
                g2.dispose();
            }
        });
        btnModoInstructivo.addActionListener(e -> activarTutorialSpotlight());

        // Botón Modo Voz (Toggle Audio TTS)
        btnModoVoz = new JButton(SintetizadorVoz.isHabilitado() ? "Voz: ON" : "Voz: OFF");
        btnModoVoz.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnModoVoz.setFocusPainted(false);
        btnModoVoz.setContentAreaFilled(false);
        btnModoVoz.setOpaque(false);
        btnModoVoz.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModoVoz.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnModoVoz.setToolTipText("Activar o silenciar narración hablada de las respuestas");
        actualizarEstiloBotonVoz();
        btnModoVoz.addActionListener(e -> {
            SintetizadorVoz.conmutarModoVoz();
            if (chatbotService != null) {
                chatbotService.setModoVozHabilitado(SintetizadorVoz.isHabilitado());
            }
            actualizarEstiloBotonVoz();
            String estado = SintetizadorVoz.isHabilitado() ? "activado" : "desactivado";
            AnimadorToast.mostrar("MODO VOZ", "Sintetizador de voz " + estado,
                    SintetizadorVoz.isHabilitado() ? new Color(0x58, 0xC9, 0x9A) : EstilosUI.ROJO_ERROR_TEXTO);
        });

        // Botón Limpiar Chat (Modo Limpieza)
        btnLimpiarChat = new JButton("Limpiar Chat");
        btnLimpiarChat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLimpiarChat.setFocusPainted(false);
        btnLimpiarChat.setContentAreaFilled(false);
        btnLimpiarChat.setOpaque(false);
        btnLimpiarChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarChat.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnLimpiarChat.setToolTipText("Restablecer conversación y borrar historial");
        btnLimpiarChat.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fondo = model.isPressed() ? new Color(0x28, 0x1E, 0x22) :
                        (model.isRollover() ? new Color(0x22, 0x1A, 0x20) : new Color(0x16, 0x1E, 0x28));
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);
                g2.setColor(model.isRollover() ? EstilosUI.ROJO_ERROR_TEXTO : EstilosUI.BORDE_LINEA);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);

                FontMetrics fm = g2.getFontMetrics(b.getFont());
                g2.setColor(model.isRollover() ? EstilosUI.ROJO_ERROR_TEXTO : EstilosUI.TEXTO_SECUNDARIO);
                int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(b.getText(), textX, textY);
                g2.dispose();
            }
        });
        btnLimpiarChat.addActionListener(e -> ejecutarLimpiezaChat());

        panelAccionesHeader.add(btnModoInstructivo);
        panelAccionesHeader.add(btnModoVoz);
        panelAccionesHeader.add(btnLimpiarChat);
        header.add(panelAccionesHeader, BorderLayout.EAST);

        return header;
    }

    private void actualizarEstiloBotonVoz() {
        boolean activo = SintetizadorVoz.isHabilitado();
        btnModoVoz.setText(activo ? "Voz: ON" : "Voz: OFF");
        final Color colorTexto = activo ? new Color(0x58, 0xC9, 0x9A) : EstilosUI.TEXTO_MUTED;
        final Color colorFondo = activo ? new Color(0x13, 0x30, 0x22) : new Color(0x14, 0x1C, 0x26);
        final Color colorBorde = activo ? new Color(0x58, 0xC9, 0x9A) : EstilosUI.BORDE_LINEA;

        btnModoVoz.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color f = model.isRollover() ? colorFondo.brighter() : colorFondo;
                g2.setColor(f);
                g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);
                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);

                FontMetrics fm = g2.getFontMetrics(b.getFont());
                g2.setColor(colorTexto);
                int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(b.getText(), textX, textY);
                g2.dispose();
            }
        });
        btnModoVoz.repaint();
    }

    private JPanel crearBarraEntrada() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        txtEntradaMensaje = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtEntradaMensaje.setPreferredSize(new Dimension(380, 38));
        txtEntradaMensaje.setToolTipText("Escriba su consulta o comando (Presione Enter para enviar)");
        txtEntradaMensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enviarMensajeUsuario();
                }
            }
        });

        btnEnviar = new JButton("Enviar");
        EstilosUI.estilizarBotonPrimario(btnEnviar);
        btnEnviar.setPreferredSize(new Dimension(90, 38));
        btnEnviar.addActionListener(e -> enviarMensajeUsuario());

        bar.add(txtEntradaMensaje, BorderLayout.CENTER);
        bar.add(btnEnviar, BorderLayout.EAST);
        return bar;
    }

    private void actualizarPildorasSugerencia() {
        actualizarPildorasSugerencia(null);
    }

    private void actualizarPildorasSugerencia(List<String> sugerenciasEspecificas) {
        panelSugerencias.removeAll();
        if (chatbotService == null) return;

        List<String> sugerencias = (sugerenciasEspecificas != null && !sugerenciasEspecificas.isEmpty())
                ? sugerenciasEspecificas
                : chatbotService.obtenerSugerenciasPorRol(SesionActual.getRol());

        if (sugerencias != null) {
            for (String s : sugerencias) {
                JButton pill = new JButton(s);
                pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
                pill.setForeground(EstilosUI.DORADO_PRINCIPAL);
                pill.setFocusPainted(false);
                pill.setContentAreaFilled(false);
                pill.setOpaque(false);
                pill.setCursor(new Cursor(Cursor.HAND_CURSOR));
                pill.setBorder(new EmptyBorder(4, 10, 4, 10));

                pill.setUI(new BasicButtonUI() {
                    @Override
                    public void paint(Graphics g, JComponent c) {
                        AbstractButton b = (AbstractButton) c;
                        ButtonModel model = b.getModel();
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                        Color fondo = model.isPressed() ? new Color(0x28, 0x36, 0x4A) :
                                (model.isRollover() ? new Color(0x22, 0x30, 0x42) : new Color(0x16, 0x22, 0x32));
                        Color borde = model.isRollover() ? EstilosUI.DORADO_PRINCIPAL : new Color(0xE8, 0xA3, 0x3D, 140);

                        g2.setColor(fondo);
                        g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 14, 14);
                        g2.setColor(borde);
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 14, 14);

                        FontMetrics fm = g2.getFontMetrics(b.getFont());
                        g2.setColor(EstilosUI.DORADO_PRINCIPAL);
                        int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                        int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(b.getText(), textX, textY);
                        g2.dispose();
                    }
                });

                pill.addActionListener(e -> {
                    txtEntradaMensaje.setText(s);
                    enviarMensajeUsuario();
                });
                panelSugerencias.add(pill);
            }
        }
        panelSugerencias.revalidate();
        panelSugerencias.repaint();
    }

    private void cargarHistorialInicial() {
        panelHistorialMensajes.removeAll();
        if (chatbotService != null) {
            List<MensajeChatbotDTO> historial = chatbotService.getHistorial();
            if (historial == null || historial.isEmpty()) {
                chatbotService.reiniciarConversacion(SesionActual.getRol(), SesionActual.getNombreUsuario());
                historial = chatbotService.getHistorial();
            }
            if (historial != null) {
                for (MensajeChatbotDTO m : historial) {
                    agregarBurbujaMensaje(m);
                }
            }
        }
        desplazarAlFinal();
    }

    private void enviarMensajeUsuario() {
        String texto = txtEntradaMensaje.getText().trim();
        if (texto.isEmpty() || chatbotService == null) {
            return;
        }

        txtEntradaMensaje.setText("");
        txtEntradaMensaje.requestFocusInWindow();

        // 1. Mostrar mensaje de usuario en UI
        MensajeChatbotDTO msgUsuario = MensajeChatbotDTO.builder()
                .emisor(MensajeChatbotDTO.Emisor.USUARIO)
                .texto(texto)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .build();
        agregarBurbujaMensaje(msgUsuario);

        // 2. Procesar con el servicio de chatbot en segundo plano para fluidez
        SwingWorker<RespuestaChatbotDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected RespuestaChatbotDTO doInBackground() {
                return chatbotService.procesarMensaje(texto, SesionActual.getRol(), "101");
            }

            @Override
            protected void done() {
                try {
                    RespuestaChatbotDTO respuesta = get();
                    MensajeChatbotDTO msgAsistente = MensajeChatbotDTO.builder()
                            .emisor(MensajeChatbotDTO.Emisor.ASISTENTE)
                            .texto(respuesta.getTextoRespuesta())
                            .tipo(respuesta.getTipo())
                            .archivoAdjunto(respuesta.getArchivoGenerado())
                            .comandoEjecutado(respuesta.getComandoEjecutado())
                            .build();
                    agregarBurbujaMensaje(msgAsistente);

                    // Si la respuesta desencadena el tutorial spotlight
                    if (respuesta.getTipo() == TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT) {
                        activarTutorialSpotlight();
                    } else if (respuesta.getTipo() == TipoRespuestaChatbot.LIMPIEZA_CONVERSACION) {
                        cargarHistorialInicial();
                    }

                    actualizarPildorasSugerencia(respuesta.getSugerenciasAccion());
                    desplazarAlFinal();
                } catch (Exception ex) {
                    log.error("Error al procesar respuesta del chatbot: {}", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void ejecutarLimpiezaChat() {
        if (chatbotService != null) {
            chatbotService.reiniciarConversacion(SesionActual.getRol(), SesionActual.getNombreUsuario());
            cargarHistorialInicial();
            AnimadorToast.mostrar("MODO LIMPIEZA", "Conversación y contexto reiniciados", EstilosUI.AZUL_INFO);
        }
    }

    private void activarTutorialSpotlight() {
        setVisible(false);
        if (callbackModoInstructivo != null) {
            callbackModoInstructivo.run();
        }
    }

    private void agregarBurbujaMensaje(MensajeChatbotDTO mensaje) {
        boolean esUsuario = (mensaje.getEmisor() == MensajeChatbotDTO.Emisor.USUARIO);

        JPanel panelFila = new JPanel(new FlowLayout(esUsuario ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 4));
        panelFila.setOpaque(false);
        panelFila.setMaximumSize(new Dimension(520, 1000));

        // Tarjeta con efecto glassmorphic
        Color colorBorde = esUsuario ? new Color(0x38, 0xBD, 0xF8) :
                (mensaje.getTipo() == TipoRespuestaChatbot.RESTRICCION_RBAC ? EstilosUI.ROJO_ERROR_TEXTO : EstilosUI.DORADO_PRINCIPAL);
        Color colorFondo = esUsuario ? new Color(0x13, 0x29, 0x40) : new Color(0x15, 0x1F, 0x2C);

        GlassPanel tarjeta = new GlassPanel(new BorderLayout(0, 6), 14, colorBorde, false);
        tarjeta.setBackground(colorFondo);
        tarjeta.setBorder(new EmptyBorder(8, 12, 8, 12));
        tarjeta.setMaximumSize(new Dimension(420, 1000));

        // Emisor y Hora
        String nombreEmisor = esUsuario ? "Usted (" + SesionActual.getNombreUsuario() + ")" : "Asistente Virtual IA";
        String hora = mensaje.getFechaHora() != null ? mensaje.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        JLabel lblCab = new JLabel(nombreEmisor + "  " + hora);
        lblCab.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCab.setForeground(esUsuario ? new Color(0x38, 0xBD, 0xF8) : EstilosUI.DORADO_PRINCIPAL);
        tarjeta.add(lblCab, BorderLayout.NORTH);

        // Texto del mensaje
        JTextArea txt = new JTextArea(mensaje.getTexto());
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setColumns(34);
        tarjeta.add(txt, BorderLayout.CENTER);

        // Si contiene un archivo exportado adjunto (PDF o Excel)
        if (mensaje.getArchivoAdjunto() != null && mensaje.getArchivoAdjunto().exists()) {
            File archivo = mensaje.getArchivoAdjunto();
            boolean esPdf = archivo.getName().toLowerCase().endsWith(".pdf");

            JPanel panelAdjunto = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            panelAdjunto.setOpaque(false);

            JButton btnAbrirArchivo = new JButton(esPdf ? "Abrir Documento PDF" : "Abrir Planilla Excel");
            btnAbrirArchivo.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnAbrirArchivo.setFocusPainted(false);
            btnAbrirArchivo.setContentAreaFilled(false);
            btnAbrirArchivo.setOpaque(false);
            btnAbrirArchivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAbrirArchivo.setBorder(new EmptyBorder(6, 14, 6, 14));

            final Color cFondo = esPdf ? new Color(0x88, 0x13, 0x37) : new Color(0x06, 0x4E, 0x3B);
            final Color cHover = esPdf ? new Color(0xBE, 0x12, 0x3C) : new Color(0x04, 0x78, 0x57);
            final Color cBorde = esPdf ? new Color(0xFB, 0x71, 0x85) : new Color(0x34, 0xD3, 0x99);

            btnAbrirArchivo.setUI(new BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    Color fondo = model.isPressed() ? cHover.darker() : (model.isRollover() ? cHover : cFondo);
                    g2.setColor(fondo);
                    g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);
                    g2.setColor(cBorde);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);

                    FontMetrics fm = g2.getFontMetrics(b.getFont());
                    g2.setColor(Color.WHITE);
                    int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                    int textY = (b.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(b.getText(), textX, textY);
                    g2.dispose();
                }
            });
            btnAbrirArchivo.addActionListener(e -> abrirArchivoEnSistema(archivo));

            panelAdjunto.add(btnAbrirArchivo);
            tarjeta.add(panelAdjunto, BorderLayout.SOUTH);
        }

        panelFila.add(tarjeta);
        panelHistorialMensajes.add(panelFila);
        panelHistorialMensajes.add(Box.createVerticalStrut(4));
        panelHistorialMensajes.revalidate();
        panelHistorialMensajes.repaint();
    }

    private void abrirArchivoEnSistema(File archivo) {
        if (archivo == null || !archivo.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo ya no está disponible en la ruta especificada.", "Archivo no encontrado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(this, "Ruta del archivo generado:\n" + archivo.getAbsolutePath(), "Archivo Generado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            log.error("No se pudo abrir el archivo {}: {}", archivo.getName(), ex.getMessage());
            JOptionPane.showMessageDialog(this, "No se pudo abrir el visor predeterminado: " + ex.getMessage(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void desplazarAlFinal() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollHistorial.getVerticalScrollBar();
            if (v != null) {
                v.setValue(v.getMaximum());
            }
        });
    }
}
