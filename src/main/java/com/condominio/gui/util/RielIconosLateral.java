package com.condominio.gui.util;

import com.condominio.modelo.seguridad.SesionActual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Riel lateral vertical de navegación por iconos con estética de vidrio esmerilado (Glassmorphism).
public class RielIconosLateral extends JPanel {

    public static class ElementoRiel {
        private final String idCard;
        private final String etiqueta;
        private final String tipoIcono;
        private final Color acento;
        private final String tooltip;

        public ElementoRiel(String idCard, String etiqueta, String tipoIcono, Color acento, String tooltip) {
            this.idCard = idCard;
            this.etiqueta = etiqueta;
            this.tipoIcono = tipoIcono;
            this.acento = acento;
            this.tooltip = tooltip;
        }

        public String getIdCard() { return idCard; }
        public String getEtiqueta() { return etiqueta; }
        public String getTipoIcono() { return tipoIcono; }
        public Color getAcento() { return acento; }
        public String getTooltip() { return tooltip; }
    }

    private final List<BotonItemRiel> botonesModulos = new ArrayList<>();
    private final Consumer<ElementoRiel> callbackSeleccion;
    private final Runnable callbackComunicados;
    private final Runnable callbackConfiguracion;
    private final Runnable callbackAcercaDe;
    private final Runnable callbackCerrarSesion;
    private BotonItemRiel botonActivo;

    public RielIconosLateral(Consumer<ElementoRiel> callbackSeleccion,
                             Runnable callbackComunicados,
                             Runnable callbackConfiguracion,
                             Runnable callbackAcercaDe,
                             Runnable callbackCerrarSesion) {
        super(new BorderLayout(0, 10));
        this.callbackSeleccion = callbackSeleccion;
        this.callbackComunicados = callbackComunicados;
        this.callbackConfiguracion = callbackConfiguracion;
        this.callbackAcercaDe = callbackAcercaDe;
        this.callbackCerrarSesion = callbackCerrarSesion;

        setPreferredSize(new Dimension(84, 0));
        setMinimumSize(new Dimension(84, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 8, 12, 8));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // 1. ZONA SUPERIOR: LOGO / ESCUDO EN CHIP DE VIDRIO
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelNorte.setOpaque(false);

        JLabel lblLogo = new JLabel("CV", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo chip vidrio dorado suave
                g2.setColor(new Color(0xE8, 0xA3, 0x3D, 35));
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                // Borde dorado 15%
                g2.setColor(new Color(0xE8, 0xA3, 0x3D, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 16, 16));
                // Highlight blanco
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawLine(6, 2, getWidth() - 6, 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLogo.setPreferredSize(new Dimension(50, 50));
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLogo.setForeground(EstilosUI.DORADO_PRINCIPAL);
        lblLogo.setToolTipText("Cobranza Vecinal v2.2.0 - Sistema de Gestión y Administración Condominal");
        lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelNorte.add(lblLogo);
        add(panelNorte, BorderLayout.NORTH);

        // 2. ZONA CENTRAL: LISTA VERTICAL DE MÓDULOS FILTRADOS POR RBAC
        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.setOpaque(false);
        panelCentro.setBorder(new EmptyBorder(12, 0, 0, 0));

        configurarModulosPorRol(panelCentro);
        add(panelCentro, BorderLayout.CENTER);

        // 3. ZONA INFERIOR: ACCIONES DEL SISTEMA Y AVATAR
        JPanel panelSur = new JPanel();
        panelSur.setLayout(new BoxLayout(panelSur, BoxLayout.Y_AXIS));
        panelSur.setOpaque(false);

        // Botón Tablón de Comunicados y Circulares
        BotonAccionRiel btnComunicados = new BotonAccionRiel("COMUNICADOS", EstilosUI.ACENTO_INMUEBLES, "Tablón Oficial de Comunicados y Circulares");
        btnComunicados.addActionListener(e -> {
            if (callbackComunicados != null) callbackComunicados.run();
        });
        panelSur.add(btnComunicados);
        panelSur.add(Box.createVerticalStrut(6));

        // Botón Políticas y Parámetros del Condominio (Administrador) / Tarifas y Directorio (Residente/Conserje)
        String tipPoliticas = SesionActual.esAdministrador()
                ? "Políticas y Parámetros del Condominio"
                : "Tarifas Oficiales y Directorio de Contacto (Solo Lectura)";
        BotonAccionRiel btnPoliticas = new BotonAccionRiel("POLITICAS", EstilosUI.DORADO_PRINCIPAL, tipPoliticas);
        btnPoliticas.addActionListener(e -> {
            if (callbackConfiguracion != null) callbackConfiguracion.run();
        });
        panelSur.add(btnPoliticas);
        panelSur.add(Box.createVerticalStrut(6));

        // Botón Acerca de
        BotonAccionRiel btnAcerca = new BotonAccionRiel("ACERCA", EstilosUI.TEXTO_SECUNDARIO, "Información del Sistema");
        btnAcerca.addActionListener(e -> {
            if (callbackAcercaDe != null) callbackAcercaDe.run();
        });
        panelSur.add(btnAcerca);
        panelSur.add(Box.createVerticalStrut(6));

        // Botón Salir / Cambiar Sesión
        BotonAccionRiel btnSalir = new BotonAccionRiel("SALIR", EstilosUI.ROJO_ERROR, "Cerrar sesión activa y alternar rol");
        btnSalir.addActionListener(e -> {
            if (callbackCerrarSesion != null) callbackCerrarSesion.run();
        });
        panelSur.add(btnSalir);
        panelSur.add(Box.createVerticalStrut(10));

        // Avatar de Usuario con Inicial del Rol
        JPanel panelAvatar = crearAvatarUsuario();
        panelSur.add(panelAvatar);

        add(panelSur, BorderLayout.SOUTH);

        // Activar el primer módulo por defecto
        if (!botonesModulos.isEmpty()) {
            seleccionar(botonesModulos.get(0));
        }
    }

    private void configurarModulosPorRol(JPanel contenedor) {
        botonesModulos.clear();

        if (SesionActual.esAdministrador()) {
            agregarModulo(contenedor, new ElementoRiel("CARD_CONTROL", "Dashboard", "DASHBOARD",
                    EstilosUI.DORADO_PRINCIPAL, "Panel de Control y Gestión Analítica (Dashboard)"));
            agregarModulo(contenedor, new ElementoRiel("CARD_INMUEBLES", "Inmuebles", "INMUEBLES",
                    EstilosUI.ACENTO_INMUEBLES, "Módulo de Gestión de Inmuebles, Torres y Departamentos"));
            agregarModulo(contenedor, new ElementoRiel("CARD_RESIDENTES", "Residentes", "RESIDENTES",
                    EstilosUI.ACENTO_RESIDENTES, "Módulo de Gestión y Directorio de Residentes"));
            agregarModulo(contenedor, new ElementoRiel("CARD_COBRANZAS", "Cobranzas", "COBRANZAS",
                    EstilosUI.ACENTO_COBRANZAS, "Módulo Interactivo de Cobranzas y Recaudación Vecinal"));
            agregarModulo(contenedor, new ElementoRiel("CARD_OPERACIONES", "Operaciones", "OPERACIONES",
                    EstilosUI.ACENTO_OPERACIONES, "Módulo de Operaciones, Incidentes y Reservas"));

        } else if (SesionActual.esConserje()) {
            agregarModulo(contenedor, new ElementoRiel("CARD_OPERACIONES", "Bitácora", "OPERACIONES",
                    EstilosUI.ACENTO_OPERACIONES, "Bitácora de Novedades e Incidentes de Portería"));
            agregarModulo(contenedor, new ElementoRiel("CARD_VISITAS", "Garita", "VISITAS",
                    new Color(0x38, 0xBD, 0xF8), "Control de Visitas, Garita y Accesos a Portería"));
            agregarModulo(contenedor, new ElementoRiel("CARD_RESIDENTES", "Directorio", "RESIDENTES",
                    EstilosUI.ACENTO_RESIDENTES, "Directorio Telefónico y Contactos para Emergencias"));

        } else {
            // Rol Residente / Inquilino
            agregarModulo(contenedor, new ElementoRiel("CARD_CUOTAS", "Mis Cuotas", "COBRANZAS",
                    EstilosUI.ACENTO_COBRANZAS, "Portal de Cuotas, Saldos y Pagos en Línea"));
            agregarModulo(contenedor, new ElementoRiel("CARD_RESERVAS", "Reservas", "RESERVAS",
                    new Color(0x38, 0xBD, 0xF8), "Mis Reservas de Áreas Comunes"));
            agregarModulo(contenedor, new ElementoRiel("CARD_REPORTES", "Reportar", "OPERACIONES",
                    EstilosUI.AMBAR_AVISO, "Reportes de Averías y Novedades a Conserjería"));
        }
    }

    private void agregarModulo(JPanel contenedor, ElementoRiel elem) {
        BotonItemRiel btn = new BotonItemRiel(elem);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> seleccionar(btn));
        botonesModulos.add(btn);
        contenedor.add(btn);
        contenedor.add(Box.createVerticalStrut(8));
    }

    public void seleccionarPorId(String idCard) {
        if (idCard == null) return;
        for (BotonItemRiel b : botonesModulos) {
            if (idCard.equals(b.getElemento().getIdCard())) {
                seleccionar(b);
                return;
            }
        }
    }

    private void seleccionar(BotonItemRiel btn) {
        if (btn == null) return;
        this.botonActivo = btn;
        for (BotonItemRiel b : botonesModulos) {
            b.setActivo(b == btn);
        }
        if (callbackSeleccion != null) {
            try {
                callbackSeleccion.accept(btn.getElemento());
            } catch (Exception ex) {
                // Previene que una falla en listeners tempranos bloquee la inicialización del riel
            }
        }
    }

    public ElementoRiel getElementoActivo() {
        return (botonActivo != null) ? botonActivo.getElemento() : null;
    }

    private JPanel crearAvatarUsuario() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnl.setOpaque(false);

        Color cAcento = SesionActual.esAdministrador() ? EstilosUI.DORADO_PRINCIPAL
                : (SesionActual.esConserje() ? EstilosUI.ACENTO_OPERACIONES : EstilosUI.ACENTO_RESIDENTES);
        String inicial = SesionActual.esAdministrador() ? "A"
                : (SesionActual.esConserje() ? "C" : "R");

        JLabel avatar = new JLabel(inicial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Círculo base con acento al 25%
                g2.setColor(new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 60));
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                // Borde luminoso de 1px
                g2.setColor(cAcento);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                // Punto de estado activo
                g2.setColor(EstilosUI.VERDE_EXITO);
                g2.fillOval(getWidth() - 9, getHeight() - 9, 7, 7);
                g2.setColor(new Color(0x0B, 0x13, 0x1E));
                g2.drawOval(getWidth() - 9, getHeight() - 9, 7, 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setForeground(EstilosUI.TEXTO_CLARO);
        avatar.setToolTipText("Usuario activo: " + SesionActual.getNombreUsuario() + " | Rol: " + SesionActual.getRol());
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (callbackCerrarSesion != null) callbackCerrarSesion.run();
            }
        });

        pnl.add(avatar);
        return pnl;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Riel con gradiente oscuro Frosted Glass
        GradientPaint gp = new GradientPaint(0, 0, new Color(16, 24, 36, 240), 0, h, new Color(11, 18, 28, 245));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Bisel de luz superior (1px)
        g2.setColor(new Color(255, 255, 255, 28));
        g2.drawLine(0, 1, w, 1);

        // Línea divisoria vertical derecha
        g2.setColor(EstilosUI.BORDE_PANEL);
        g2.drawLine(w - 1, 0, w - 1, h);

        g2.dispose();
        super.paintComponent(g);
    }

    // BOTÓN DE ÍCONO DE MÓDULO (PÍLDORA DE ACENTO ACTIVA)

    public static class BotonItemRiel extends JButton {
        private final ElementoRiel elemento;
        private boolean activo = false;

        public BotonItemRiel(ElementoRiel elemento) {
            this.elemento = elemento;
            setToolTipText(elemento.getTooltip());
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(68, 52));
            setMaximumSize(new Dimension(68, 52));
            setMinimumSize(new Dimension(68, 52));
        }

        public ElementoRiel getElemento() { return elemento; }
        public boolean isActivo() { return activo; }

        public void setActivo(boolean a) {
            this.activo = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radio = 16;
            Color acento = elemento.getAcento();
            ButtonModel model = getModel();
            boolean hover = model.isRollover();

            if (activo) {
                // Píldora de acento del módulo detrás del icono activo (Regla 4)
                g2.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 60));
                g2.fill(new RoundRectangle2D.Float(4, 3, w - 8, h - 6, radio, radio));

                // Borde de realce blanco 15%
                g2.setColor(new Color(255, 255, 255, 45));
                g2.draw(new RoundRectangle2D.Float(5, 4, w - 10, h - 8, radio, radio));

                // Borde de acento
                g2.setColor(acento);
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(4, 3, w - 8, h - 6, radio, radio));

            } else if (hover) {
                // Hover de vidrio (alfa sube 4% sutilmente)
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fill(new RoundRectangle2D.Float(4, 3, w - 8, h - 6, radio, radio));
                g2.setColor(new Color(255, 255, 255, 25));
                g2.draw(new RoundRectangle2D.Float(4, 3, w - 8, h - 6, radio, radio));
            }

            int cx = w / 2;
            int cy = h / 2 - 4;
            Color colorIcono = activo ? acento : (hover ? EstilosUI.TEXTO_CLARO : EstilosUI.TEXTO_SECUNDARIO);
            g2.setColor(colorIcono);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            dibujarIconoVectorial(g2, cx, cy, elemento.getTipoIcono());

            // Etiqueta compacta en parte inferior
            g2.setFont(new Font("Segoe UI", activo ? Font.BOLD : Font.PLAIN, 10));
            g2.setColor(activo ? colorIcono : (hover ? EstilosUI.TEXTO_CLARO : EstilosUI.TEXTO_MUTED));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(elemento.getEtiqueta())) / 2;
            g2.drawString(elemento.getEtiqueta(), tx, h - 8);

            g2.dispose();
        }

        private void dibujarIconoVectorial(Graphics2D g2, int cx, int cy, String tipo) {
            switch (tipo) {
                case "DASHBOARD":
                    // Matriz cuadrícula analítica 2x2
                    g2.drawRoundRect(cx - 7, cy - 7, 6, 6, 2, 2);
                    g2.drawRoundRect(cx + 1, cy - 7, 6, 6, 2, 2);
                    g2.drawRoundRect(cx - 7, cy + 1, 6, 6, 2, 2);
                    g2.drawRoundRect(cx + 1, cy + 1, 6, 6, 2, 2);
                    break;
                case "INMUEBLES":
                    // Silueta de torre / edificios
                    g2.drawRoundRect(cx - 7, cy - 8, 8, 16, 2, 2);
                    g2.drawRoundRect(cx + 1, cy - 4, 6, 12, 2, 2);
                    g2.drawLine(cx - 4, cy - 4, cx - 2, cy - 4);
                    g2.drawLine(cx - 4, cy, cx - 2, cy);
                    break;
                case "RESIDENTES":
                    // Silueta de usuario / personas
                    g2.drawOval(cx - 4, cy - 8, 8, 8);
                    g2.drawArc(cx - 7, cy + 1, 14, 8, 0, 180);
                    break;
                case "COBRANZAS":
                    // Tarjeta financiera con chip
                    g2.drawRoundRect(cx - 8, cy - 6, 16, 12, 3, 3);
                    g2.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
                    g2.fillRect(cx - 5, cy + 1, 3, 3);
                    break;
                case "OPERACIONES":
                    // Portapapeles con checklist
                    g2.drawRoundRect(cx - 6, cy - 6, 12, 14, 2, 2);
                    g2.drawLine(cx - 3, cy - 8, cx + 3, cy - 8);
                    g2.drawLine(cx - 3, cy - 2, cx + 3, cy - 2);
                    g2.drawLine(cx - 3, cy + 2, cx + 3, cy + 2);
                    break;
                case "VISITAS":
                    // Escudo de seguridad y control
                    int[] sx = {cx, cx + 6, cx + 6, cx, cx - 6, cx - 6};
                    int[] sy = {cy - 7, cy - 5, cy + 2, cy + 7, cy + 2, cy - 5};
                    g2.drawPolygon(sx, sy, 6);
                    break;
                case "RESERVAS":
                    // Calendario interactivo
                    g2.drawRoundRect(cx - 6, cy - 6, 12, 12, 2, 2);
                    g2.drawLine(cx - 6, cy - 2, cx + 6, cy - 2);
                    g2.drawLine(cx - 3, cy - 8, cx - 3, cy - 6);
                    g2.drawLine(cx + 3, cy - 8, cx + 3, cy - 6);
                    break;
                default:
                    g2.drawOval(cx - 5, cy - 5, 10, 10);
                    break;
            }
        }
    }

    // BOTÓN DE ACCIÓN COMPACTO DEL RIEL (PATRONES, ACERCA, SALIR)

    public static class BotonAccionRiel extends JButton {
        private final String accion;
        private final Color acento;

        public BotonAccionRiel(String accion, Color acento, String tooltip) {
            this.accion = accion;
            this.acento = acento;
            setToolTipText(tooltip);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(46, 36));
            setMaximumSize(new Dimension(46, 36));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            ButtonModel model = getModel();
            boolean hover = model.isRollover();

            if (hover) {
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 10, 10));
                g2.setColor(new Color(255, 255, 255, 30));
                g2.draw(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 10, 10));
            }

            int cx = w / 2;
            int cy = h / 2;
            Color cIcono = hover ? EstilosUI.TEXTO_CLARO : acento;
            g2.setColor(cIcono);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (accion) {
                case "COMUNICADOS":
                    // Megáfono / Tablón de avisos
                    g2.drawRoundRect(cx - 5, cy - 4, 8, 8, 2, 2);
                    g2.drawLine(cx + 3, cy - 3, cx + 6, cy - 5);
                    g2.drawLine(cx + 3, cy + 3, cx + 6, cy + 5);
                    break;
                case "POLITICAS":
                case "CONFIGURACION":
                    // Engranaje de configuración
                    g2.drawOval(cx - 3, cy - 3, 6, 6);
                    g2.drawLine(cx, cy - 6, cx, cy + 6);
                    g2.drawLine(cx - 6, cy, cx + 6, cy);
                    break;
                case "ACERCA":
                    // Círculo con i
                    g2.drawOval(cx - 6, cy - 6, 12, 12);
                    g2.drawLine(cx, cy - 2, cx, cy + 3);
                    g2.fillOval(cx - 1, cy - 4, 2, 2);
                    break;
                case "SALIR":
                    // Flecha salir hacia la izquierda
                    g2.drawRoundRect(cx - 6, cy - 6, 12, 12, 2, 2);
                    g2.drawLine(cx - 2, cy, cx + 3, cy);
                    g2.drawLine(cx + 1, cy - 3, cx + 3, cy);
                    g2.drawLine(cx + 1, cy + 3, cx + 3, cy);
                    break;
                default:
                    g2.drawOval(cx - 4, cy - 4, 8, 8);
                    break;
            }

            g2.dispose();
        }
    }
}
