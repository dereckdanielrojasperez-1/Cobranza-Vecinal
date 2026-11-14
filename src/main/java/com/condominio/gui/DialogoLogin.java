package com.condominio.gui;

import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.patrones.facade.CobranzaFachada;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

// Dialogo modal de autenticacion con control de acceso y diseno corporativo
public class DialogoLogin extends JDialog {

    private final CobranzaFachada fachada;
    private JTextField campoTextoUsuario;
    private JPasswordField campoTextoClave;
    private JLabel etiquetaMensajeError;
    private JButton botonEntrar;
    private int intentosFallidos = 0;
    private boolean loginExitoso = false;

    // Constructor principal desacoplado que utiliza la fachada de la aplicacion
    public DialogoLogin(Frame padre, CobranzaFachada fachada) {
        super(padre, "Acceso al Sistema - " + ConstantesUI.TITULO_APLICACION, true);
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(440, 490);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(0, 0));

        JPanel panelContenedor = new JPanel(new BorderLayout(0, 16));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Cabecera con Título y Subtítulo
        JPanel panelCabecera = new JPanel();
        panelCabecera.setLayout(new BoxLayout(panelCabecera, BoxLayout.Y_AXIS));
        panelCabecera.setOpaque(false);

        JLabel etiquetaDistintivo = new JLabel("SISTEMA DE SEGURIDAD RBAC", SwingConstants.CENTER);
        etiquetaDistintivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        etiquetaDistintivo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        etiquetaDistintivo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel etiquetaTitulo = new JLabel(ConstantesUI.TITULO_APLICACION, SwingConstants.CENTER);
        etiquetaTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        etiquetaTitulo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel etiquetaSubtitulo = new JLabel("Ingrese sus credenciales de acceso institucional", SwingConstants.CENTER);
        etiquetaSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        etiquetaSubtitulo.setFont(EstilosUI.FUENTE_SUBTITULO);
        etiquetaSubtitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelCabecera.add(etiquetaDistintivo);
        panelCabecera.add(Box.createVerticalStrut(4));
        panelCabecera.add(etiquetaTitulo);
        panelCabecera.add(Box.createVerticalStrut(4));
        panelCabecera.add(etiquetaSubtitulo);
        panelContenedor.add(panelCabecera, BorderLayout.NORTH);

        // Tarjeta Central con Formulario estilo GlassPanel (Regla 2)
        GlassPanel panelTarjeta = new GlassPanel(new GridBagLayout(), 24, EstilosUI.DORADO_PRINCIPAL, true);
        panelTarjeta.setBorder(new EmptyBorder(20, 20, 16, 20));

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(6, 4, 6, 4);
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.gridx = 0;

        // Campo Usuario
        restricciones.gridy = 0;
        panelTarjeta.add(EstilosUI.crearEtiquetaCampo("Usuario:"), restricciones);

        restricciones.gridy = 1;
        campoTextoUsuario = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        campoTextoUsuario.setPreferredSize(new Dimension(320, 34));
        campoTextoUsuario.setText("admin");
        panelTarjeta.add(campoTextoUsuario, restricciones);

        // Campo Contraseña
        restricciones.gridy = 2;
        panelTarjeta.add(EstilosUI.crearEtiquetaCampo("Contraseña:"), restricciones);

        restricciones.gridy = 3;
        campoTextoClave = new JPasswordField("admin123");
        campoTextoClave.setPreferredSize(new Dimension(320, 34));
        campoTextoClave.setBackground(EstilosUI.FONDO_CAMPO);
        campoTextoClave.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        campoTextoClave.setCaretColor(EstilosUI.DORADO_PRINCIPAL);
        campoTextoClave.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.DORADO_PRINCIPAL, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        panelTarjeta.add(campoTextoClave, restricciones);

        // Mensaje de Error en Rojo
        restricciones.gridy = 4;
        etiquetaMensajeError = new JLabel(" ", SwingConstants.CENTER);
        etiquetaMensajeError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiquetaMensajeError.setForeground(EstilosUI.ROJO_ERROR);
        panelTarjeta.add(etiquetaMensajeError, restricciones);

        // Acceso Rápido Demo (Píldoras de selección estilizadas con acentos por rol)
        restricciones.gridy = 5;
        JPanel panelAccesoRapido = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelAccesoRapido.setOpaque(false);

        JButton botonRolAdmin = crearBotonRolRapido("Admin", "admin", "admin123", EstilosUI.DORADO_PRINCIPAL);
        JButton botonRolConserje = crearBotonRolRapido("Conserje", "conserje", "conserje123", EstilosUI.ACENTO_OPERACIONES);
        JButton botonRolResidente = crearBotonRolRapido("Residente", "residente", "residente123", EstilosUI.ACENTO_RESIDENTES);

        panelAccesoRapido.add(botonRolAdmin);
        panelAccesoRapido.add(botonRolConserje);
        panelAccesoRapido.add(botonRolResidente);
        panelTarjeta.add(panelAccesoRapido, restricciones);

        panelContenedor.add(panelTarjeta, BorderLayout.CENTER);

        // Botones de Acción (Salir del Sistema / Iniciar Sesión)
        JPanel panelSur = new JPanel(new BorderLayout(0, 8));
        panelSur.setOpaque(false);

        JPanel panelBotonesAccion = new JPanel(new GridLayout(1, 2, 10, 0));
        panelBotonesAccion.setOpaque(false);

        JButton botonSalirAplicacion = new JButton("Salir");
        EstilosUI.estilizarBotonSecundario(botonSalirAplicacion, EstilosUI.ROJO_ERROR_TEXTO);
        botonSalirAplicacion.setPreferredSize(new Dimension(110, 38));
        botonSalirAplicacion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonSalirAplicacion.setToolTipText("Cierra la aplicación por completo");
        botonSalirAplicacion.addActionListener(e -> {
            loginExitoso = false;
            dispose();
            GestorBandejaSistema.removerTray();
            System.exit(0);
        });

        botonEntrar = new JButton("Iniciar Sesión");
        EstilosUI.estilizarBoton(botonEntrar, EstilosUI.DORADO_PRINCIPAL);
        botonEntrar.setPreferredSize(new Dimension(210, 38));
        botonEntrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonEntrar.addActionListener(e -> ejecutarAutenticacion());

        // Atajo Enter para autenticar
        KeyAdapter adaptadorEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarAutenticacion();
                }
            }
        };
        campoTextoUsuario.addKeyListener(adaptadorEnter);
        campoTextoClave.addKeyListener(adaptadorEnter);

        panelBotonesAccion.add(botonSalirAplicacion);
        panelBotonesAccion.add(botonEntrar);
        panelSur.add(panelBotonesAccion, BorderLayout.CENTER);
        panelContenedor.add(panelSur, BorderLayout.SOUTH);

        add(panelContenedor);
        com.condominio.gui.animacion.AnimadorDialogo.deslizarEntrada(panelContenedor);
    }

    private JButton crearBotonRolRapido(String etiqueta, String usuarioTexto, String claveTexto, Color colorRol) {
        JButton botonRol = new JButton(etiqueta) {
            private boolean cursorEncima = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        cursorEncima = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        cursorEncima = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graficos2D = (Graphics2D) g.create();
                graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int opacidadFondo = cursorEncima ? 50 : 25;
                graficos2D.setColor(new Color(colorRol.getRed(), colorRol.getGreen(), colorRol.getBlue(), opacidadFondo));
                graficos2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                graficos2D.setColor(new Color(colorRol.getRed(), colorRol.getGreen(), colorRol.getBlue(), cursorEncima ? 230 : 130));
                graficos2D.setStroke(new BasicStroke(1.2f));
                graficos2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                graficos2D.dispose();
                super.paintComponent(g);
            }
        };
        botonRol.setFont(new Font("Segoe UI", Font.BOLD, 11));
        botonRol.setOpaque(false);
        botonRol.setContentAreaFilled(false);
        botonRol.setForeground(colorRol);
        botonRol.setFocusPainted(false);
        botonRol.setBorder(new EmptyBorder(4, 14, 4, 14));
        botonRol.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonRol.setToolTipText("Acceso rápido como " + etiqueta);
        botonRol.addActionListener(e -> {
            campoTextoUsuario.setText(usuarioTexto);
            campoTextoClave.setText(claveTexto);
            etiquetaMensajeError.setText(" ");
            com.condominio.gui.animacion.AnimadorToast.mostrar("CONTROL DE ACCESO", "Perfil seleccionado: " + etiqueta, colorRol);
        });
        return botonRol;
    }

    private void ejecutarAutenticacion() {
        if (intentosFallidos >= 3) {
            etiquetaMensajeError.setText("Acceso bloqueado tras 3 intentos fallidos.");
            botonEntrar.setEnabled(false);
            campoTextoUsuario.setEnabled(false);
            campoTextoClave.setEnabled(false);
            return;
        }

        String usuarioIngresado = campoTextoUsuario.getText().trim();
        String claveIngresada = new String(campoTextoClave.getPassword()).trim();

        if (usuarioIngresado.isEmpty() || claveIngresada.isEmpty()) {
            etiquetaMensajeError.setText("Por favor, ingrese usuario y contraseña.");
            return;
        }

        // Autenticacion a traves de CobranzaFachada (Inversion de Dependencias)
        UsuarioSistema usuarioEncontrado = null;
        if (fachada != null) {
            usuarioEncontrado = fachada.autenticarUsuario(usuarioIngresado, claveIngresada).orElse(null);
        } else {
            // Fallback para previsualizaciones graficas sin contexto Spring Boot activo
            if ("admin".equalsIgnoreCase(usuarioIngresado) && "admin123".equals(claveIngresada)) {
                usuarioEncontrado = UsuarioSistema.builder()
                        .usuario("admin").clave("admin123").rol(RolUsuario.ADMINISTRADOR)
                        .nombreCompleto("Administrador General").activo(true).build();
            } else if ("conserje".equalsIgnoreCase(usuarioIngresado) && "conserje123".equals(claveIngresada)) {
                usuarioEncontrado = UsuarioSistema.builder()
                        .usuario("conserje").clave("conserje123").rol(RolUsuario.CONSERJE)
                        .nombreCompleto("Conserjería Principal").activo(true).build();
            } else if ("residente".equalsIgnoreCase(usuarioIngresado) && "residente123".equals(claveIngresada)) {
                usuarioEncontrado = UsuarioSistema.builder()
                        .usuario("residente").clave("residente123").rol(RolUsuario.RESIDENTE)
                        .nombreCompleto("Residente Habitante").activo(true).build();
            }
        }

        if (usuarioEncontrado != null) {
            SesionActual.iniciarSesion(usuarioEncontrado);
            loginExitoso = true;
            dispose();
        } else {
            intentosFallidos++;
            int restantes = 3 - intentosFallidos;
            if (restantes > 0) {
                etiquetaMensajeError.setText("Credenciales incorrectas (" + restantes + " intento" + (restantes > 1 ? "s" : "") + " restante" + (restantes > 1 ? "s" : "") + ").");
            } else {
                etiquetaMensajeError.setText("Acceso bloqueado tras 3 intentos fallidos.");
                botonEntrar.setEnabled(false);
                campoTextoUsuario.setEnabled(false);
                campoTextoClave.setEnabled(false);
            }
            campoTextoClave.setText("");
            campoTextoClave.requestFocusInWindow();
        }
    }

    public boolean isLoginExitoso() {
        return loginExitoso;
    }
}
