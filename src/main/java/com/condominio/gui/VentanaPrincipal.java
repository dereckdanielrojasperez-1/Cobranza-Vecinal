package com.condominio.gui;

import com.condominio.gui.paneles.PanelCobranzas;
import com.condominio.gui.paneles.PanelControl;
import com.condominio.gui.paneles.PanelInmuebles;
import com.condominio.gui.paneles.PanelOperaciones;
import com.condominio.gui.paneles.PanelResidentes;
import com.condominio.gui.util.ChipIcono;
import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.RielIconosLateral;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

// Interfaz gráfica principal del sistema "Cobranza Vecinal".
public class VentanaPrincipal extends JFrame {

    private final CobranzaFachada fachadaCobranza;
    private final com.condominio.gui.animacion.AnimadorSubrayado animadorSubrayado = new com.condominio.gui.animacion.AnimadorSubrayado();

    private CardLayout layoutCards;
    private JPanel panelCards;
    private RielIconosLateral rielLateral;
    private BufferedImage bufferFondoCache;
    private Color acentoActivoCache;
    private JLabel lblSaludoUsuario;
    private JLabel lblSubtituloModulo;
    private PanelControl panelControlRef;

    // Acentos cromáticos por orden de pestaña (Punto 2.1)
    private static final Color[] ACENTOS_PESTANAS = {
            EstilosUI.DORADO_PRINCIPAL,   // Pestaña 0: Panel de Control (Dorado #E8A33D)
            EstilosUI.ACENTO_INMUEBLES,   // Pestaña 1: Inmuebles (Menta #58C99A)
            EstilosUI.ACENTO_RESIDENTES,  // Pestaña 2: Residentes (Lavanda #B39DDB)
            EstilosUI.ACENTO_COBRANZAS,   // Pestaña 3: Cobranzas (Ámbar #FFB454)
            EstilosUI.ACENTO_OPERACIONES  // Pestaña 4: Operaciones (Cielo #6EC1E4)
    };

    public static Color obtenerAcentoPorTituloPestana(String titulo) {
        if (titulo == null) return EstilosUI.DORADO_PRINCIPAL;
        String t = titulo.toLowerCase();
        if (t.contains("control") || t.contains("dashboard")) return EstilosUI.DORADO_PRINCIPAL;
        if (t.contains("inmueble")) return EstilosUI.ACENTO_INMUEBLES;
        if (t.contains("residente") || t.contains("directorio")) return EstilosUI.ACENTO_RESIDENTES;
        if (t.contains("cobranza") || t.contains("caja") || t.contains("cuota") || t.contains("pago")) return EstilosUI.ACENTO_COBRANZAS;
        if (t.contains("operacion") || t.contains("bitácora") || t.contains("bitacora") || t.contains("incidencia") || t.contains("conserjería") || t.contains("conserjeria")) return EstilosUI.ACENTO_OPERACIONES;
        if (t.contains("visita") || t.contains("garita") || t.contains("acceso")) return new Color(0x38, 0xBD, 0xF8);
        if (t.contains("reserva")) return new Color(0x38, 0xBD, 0xF8); // Cyan cielo suave
        return EstilosUI.DORADO_PRINCIPAL;
    }

    public VentanaPrincipal(CobranzaFachada fachadaCobranza) {
        super("Cobranza Vecinal - Sistema de Administración Condominal");
        this.fachadaCobranza = (fachadaCobranza != null) ? fachadaCobranza : new CobranzaFachada();
        configurarVentana();
        inicializarComponentes();
    }

    public VentanaPrincipal() {
        this(new CobranzaFachada());
    }

    private ImageIcon cargarIconoLogo(int ancho, int alto) {
        try {
            java.net.URL recursoUrl = getClass().getResource("/logo.png");
            if (recursoUrl == null) {
                recursoUrl = getClass().getResource("/logo.jpg");
            }
            if (recursoUrl != null) {
                ImageIcon iconoOriginal = new ImageIcon(recursoUrl);
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                return new ImageIcon(imagenEscalada);
            }
            java.io.File archivoLocal = new java.io.File("src/main/resources/logo.png");
            if (!archivoLocal.exists()) {
                archivoLocal = new java.io.File("imagenes_informe/logo.png");
            }
            if (!archivoLocal.exists()) {
                archivoLocal = new java.io.File("C:/Users/Computecno/Documents/DP/src/main/resources/logo.png");
            }
            if (archivoLocal.exists()) {
                ImageIcon iconoOriginal = new ImageIcon(archivoLocal.getAbsolutePath());
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                return new ImageIcon(imagenEscalada);
            }
        } catch (Exception excepcion) {
            // Continúa sin icono si no se encuentra
        }
        return null;
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 850);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);

        ImageIcon logoAplicacion = cargarIconoLogo(64, 64);
        if (logoAplicacion != null) {
            setIconImage(logoAplicacion.getImage());
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception excepcion) {
            // Mantiene el aspecto estándar en caso de error
        }
        EstilosUI.aplicarTemaGlobal();
    }

    private void pintarFondoCanvas(Graphics g, int w, int h) {
        Color acentoModulo = (rielLateral != null && rielLateral.getElementoActivo() != null)
                ? rielLateral.getElementoActivo().getAcento()
                : EstilosUI.DORADO_PRINCIPAL;

        if (bufferFondoCache == null || bufferFondoCache.getWidth() != w
                || bufferFondoCache.getHeight() != h
                || !acentoModulo.equals(acentoActivoCache)) {

            bufferFondoCache = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferFondoCache.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 1. Degradado vertical Slate oscuro de base (#0E1724 a #0B131E)
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x0E, 0x17, 0x24), 0, h, EstilosUI.FONDO_PRINCIPAL);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // 2. Glow radial suave del acento del módulo activo (alfa 8%, máximo 12% - Regla 1)
            int radioGlow = Math.min(w, h);
            float[] dist = {0.0f, 0.45f, 1.0f};
            Color cGlowCentro = new Color(acentoModulo.getRed(), acentoModulo.getGreen(), acentoModulo.getBlue(), (int) (255 * 0.08f));
            Color cGlowMedio  = new Color(acentoModulo.getRed(), acentoModulo.getGreen(), acentoModulo.getBlue(), (int) (255 * 0.03f));
            Color cGlowBorde  = new Color(acentoModulo.getRed(), acentoModulo.getGreen(), acentoModulo.getBlue(), 0);
            Color[] colors = {cGlowCentro, cGlowMedio, cGlowBorde};
            RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point(180, 140),
                    radioGlow,
                    dist,
                    colors
            );
            g2.setPaint(rgp);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
            acentoActivoCache = acentoModulo;
        }

        g.drawImage(bufferFondoCache, 0, 0, null);
    }

    private void inicializarComponentes() {
        JPanel panelCanvas = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                pintarFondoCanvas(g, getWidth(), getHeight());
            }
        };
        panelCanvas.setOpaque(false);
        setContentPane(panelCanvas);

        // 1. ÁREA PRINCIPAL CENTRAL (HEADER ELEGANTE + CARDS DE MÓDULOS)

        JPanel panelAreaPrincipal = new JPanel(new BorderLayout());
        panelAreaPrincipal.setOpaque(false);

        // 1.1 Header superior con saludo personalizado estilo referencia "Hello, Jack!" (Regla 5)
        JPanel panelEncabezado = crearEncabezadoModerno();
        panelAreaPrincipal.add(panelEncabezado, BorderLayout.NORTH);

        // 1.2 Contenedor central con CardLayout (Cero saturación, sub-interfaces directas)
        layoutCards = new CardLayout();
        panelCards = new JPanel(layoutCards);
        panelCards.setOpaque(false);

        inicializarModulosPorRol();

        panelAreaPrincipal.add(panelCards, BorderLayout.CENTER);

        // 1.3 Barra de estado inferior corporativa
        JPanel panelBarraEstado = crearBarraEstadoInferior();
        panelAreaPrincipal.add(panelBarraEstado, BorderLayout.SOUTH);

        panelCanvas.add(panelAreaPrincipal, BorderLayout.CENTER);

        // 2. BARRA DE NAVEGACIÓN LATERAL (RIEL VERTICAL DE ICONOS - REGLA 4)

        rielLateral = new RielIconosLateral(
                this::alSeleccionarModuloRiel,
                this::mostrarDialogoComunicados,
                this::mostrarDialogoConfiguracion,
                this::mostrarDialogoAcercaDe,
                this::cerrarSesionYCambiarRol
        );
        panelCanvas.add(rielLateral, BorderLayout.WEST);

        if (rielLateral.getElementoActivo() != null) {
            alSeleccionarModuloRiel(rielLateral.getElementoActivo());
        }
    }

    private void alSeleccionarModuloRiel(RielIconosLateral.ElementoRiel elem) {
        if (elem == null) return;
        if (layoutCards != null && panelCards != null) {
            layoutCards.show(panelCards, elem.getIdCard());
        }

        if (lblSubtituloModulo != null) {
            lblSubtituloModulo.setText(elem.getTooltip());
            lblSubtituloModulo.setForeground(elem.getAcento());
        }

        if ("CARD_CONTROL".equals(elem.getIdCard()) && panelControlRef != null) {
            panelControlRef.actualizarDatos();
        } else if (panelCards != null) {
            for (Component comp : panelCards.getComponents()) {
                if (comp.isVisible()) {
                    if (comp instanceof com.condominio.gui.paneles.PanelVisitas pv) {
                        pv.cargarDatosTabla();
                    } else if (comp instanceof com.condominio.gui.paneles.PanelResidentes pr) {
                        pr.cargarDatosTabla();
                    } else if (comp instanceof com.condominio.gui.paneles.PanelOperaciones po) {
                        po.cargarDatosTabla();
                    } else if (comp instanceof com.condominio.gui.paneles.PanelCobranzas pc) {
                        pc.cargarDatosTabla();
                    } else if (comp instanceof com.condominio.gui.paneles.PanelInmuebles pi) {
                        pi.cargarDatosTabla();
                    }
                }
            }
        }

        // Invalidar caché del fondo para aplicar el suave glow cromático del módulo activo (Regla 1)
        bufferFondoCache = null;
        repaint();
    }

    // Navega directamente al módulo indicado por su identificador de tarjeta,
    public void navegarModuloPorId(String idModulo) {
        if (idModulo == null) return;
        if (rielLateral != null) {
            rielLateral.seleccionarPorId(idModulo);
        } else if (layoutCards != null && panelCards != null) {
            layoutCards.show(panelCards, idModulo);
        }
        repaint();
    }

    private void inicializarModulosPorRol() {
        if (SesionActual.esAdministrador()) {
            panelControlRef = new PanelControl(this.fachadaCobranza);
            panelCards.add(panelControlRef, "CARD_CONTROL");
            panelCards.add(new PanelInmuebles(this.fachadaCobranza), "CARD_INMUEBLES");
            panelCards.add(new PanelResidentes(this.fachadaCobranza), "CARD_RESIDENTES");
            panelCards.add(new PanelCobranzas(this.fachadaCobranza), "CARD_COBRANZAS");
            panelCards.add(new PanelOperaciones(this.fachadaCobranza), "CARD_OPERACIONES");

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    if (panelControlRef != null) {
                        panelControlRef.desuscribir();
                    }
                }
            });

        } else if (SesionActual.esConserje()) {
            panelCards.add(new PanelOperaciones(this.fachadaCobranza), "CARD_OPERACIONES");
            panelCards.add(new com.condominio.gui.paneles.PanelVisitas(this.fachadaCobranza), "CARD_VISITAS");
            panelCards.add(new PanelResidentes(this.fachadaCobranza), "CARD_RESIDENTES");

        } else {
            com.condominio.gui.paneles.PanelPortalResidente portal =
                    new com.condominio.gui.paneles.PanelPortalResidente(this.fachadaCobranza, "101");
            panelCards.add(portal.crearPanelCuotasYPagos(), "CARD_CUOTAS");
            panelCards.add(portal.crearPanelReservas(), "CARD_RESERVAS");
            panelCards.add(portal.crearPanelIncidencias(), "CARD_REPORTES");
        }
    }

    private JPanel crearEncabezadoModerno() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(EstilosUI.FONDO_CABECERA);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstilosUI.BORDE_LINEA),
                new EmptyBorder(12, 22, 12, 22)
        ));

        // Lado Izquierdo: Saludo elegante estilo referencia "Hello, Jack!"
        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 3));
        panelIzquierdo.setOpaque(false);

        String nombreUsuario = SesionActual.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = (SesionActual.getRol() != null) ? SesionActual.getRol().name() : "Usuario";
        }
        lblSaludoUsuario = new JLabel("¡Hola, " + nombreUsuario + "!");
        lblSaludoUsuario.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSaludoUsuario.setForeground(EstilosUI.TEXTO_CLARO);

        lblSubtituloModulo = new JLabel("Cobranza Vecinal | Sistema de Administración y Gestión Condominal");
        lblSubtituloModulo.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSubtituloModulo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        panelIzquierdo.add(lblSaludoUsuario, BorderLayout.NORTH);
        panelIzquierdo.add(lblSubtituloModulo, BorderLayout.SOUTH);
        header.add(panelIzquierdo, BorderLayout.WEST);

        // Lado Derecho: Buscador global tipo vidrio + iconos de notificación y ayuda + badges (Regla 5)
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        panelDerecho.setOpaque(false);

        // Buscador global interactivo con fondo vidrio y RBAC adaptativo
        final String placeholderGlobal = SesionActual.esAdministrador()
                ? "Buscar gastos, avisos, cuotas..."
                : (SesionActual.esConserje() ? "Buscar visitas, portería..." : "Buscar avisos, gastos, cuotas...");

        GlassPanel panelBuscador = new GlassPanel(new FlowLayout(FlowLayout.LEFT, 8, 3), 16, null, false);
        panelBuscador.setPreferredSize(new Dimension(230, 32));
        panelBuscador.setBorder(new EmptyBorder(2, 6, 2, 6));

        ChipIcono chipLupa = new ChipIcono("SEARCH", EstilosUI.TEXTO_SECUNDARIO, 16, false);
        chipLupa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chipLupa.setToolTipText("Abrir Centro de Búsqueda Global (Enter o clic)");

        JTextField txtBuscarGlobal = new JTextField(placeholderGlobal);
        txtBuscarGlobal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtBuscarGlobal.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        txtBuscarGlobal.setCaretColor(EstilosUI.TEXTO_PRINCIPAL);
        txtBuscarGlobal.setBorder(null);
        txtBuscarGlobal.setOpaque(false);
        txtBuscarGlobal.setPreferredSize(new Dimension(180, 22));
        txtBuscarGlobal.setToolTipText(placeholderGlobal + " (Presione Enter para buscar)");

        Runnable abrirBuscador = () -> {
            String texto = txtBuscarGlobal.getText().trim();
            if (placeholderGlobal.equals(texto)) texto = "";
            DialogoBusquedaGlobal.mostrar(VentanaPrincipal.this, this.fachadaCobranza, texto);
        };

        chipLupa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirBuscador.run();
            }
        });

        panelBuscador.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelBuscador.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtBuscarGlobal.requestFocusInWindow();
            }
        });

        txtBuscarGlobal.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (placeholderGlobal.equals(txtBuscarGlobal.getText())) {
                    txtBuscarGlobal.setText("");
                    txtBuscarGlobal.setForeground(EstilosUI.TEXTO_PRINCIPAL);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtBuscarGlobal.getText().isBlank()) {
                    txtBuscarGlobal.setText(placeholderGlobal);
                    txtBuscarGlobal.setForeground(EstilosUI.TEXTO_SECUNDARIO);
                }
            }
        });

        txtBuscarGlobal.addActionListener(e -> abrirBuscador.run());

        getRootPane().registerKeyboardAction(
                e -> abrirBuscador.run(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        panelBuscador.add(chipLupa);
        panelBuscador.add(txtBuscarGlobal);
        panelDerecho.add(panelBuscador);

        // Icono de Notificaciones en chip de vidrio (Regla 5)
        JButton btnNotif = new JButton();
        btnNotif.setFocusPainted(false);
        btnNotif.setContentAreaFilled(false);
        btnNotif.setBorderPainted(false);
        btnNotif.setOpaque(false);
        btnNotif.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotif.setToolTipText("Notificaciones y avisos automáticos de cobranza");
        btnNotif.setLayout(new BorderLayout());
        btnNotif.add(new ChipIcono("BELL", EstilosUI.DORADO_PRINCIPAL, 30, true), BorderLayout.CENTER);
        btnNotif.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Centro de Notificaciones y Alertas activado.\nMonitoreo de cobranzas, morosidad, visitas y eventos en tiempo real.",
                    "Notificaciones - Cobranza Vecinal", JOptionPane.INFORMATION_MESSAGE);
        });
        panelDerecho.add(btnNotif);

        // Icono de Ayuda / Soporte en chip de vidrio (Regla 5)
        JButton btnAyuda = new JButton();
        btnAyuda.setFocusPainted(false);
        btnAyuda.setContentAreaFilled(false);
        btnAyuda.setBorderPainted(false);
        btnAyuda.setOpaque(false);
        btnAyuda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAyuda.setToolTipText("Mesa de Ayuda e Información del Sistema");
        btnAyuda.setLayout(new BorderLayout());
        btnAyuda.add(new ChipIcono("HELP", EstilosUI.AZUL_INFO, 30, true), BorderLayout.CENTER);

        JPopupMenu menuAyuda = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.AZUL_INFO);

        JMenuItem itemAcerca = EstilosUI.crearItemMenuOscuro("Acerca de Cobranza Vecinal", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.AZUL_INFO);
        itemAcerca.addActionListener(e -> mostrarDialogoAcercaDe());
        menuAyuda.add(itemAcerca);

        JMenuItem itemBD = EstilosUI.crearItemMenuOscuro("Base de Datos Externa (PostgreSQL / MySQL)", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.AZUL_INFO);
        itemBD.addActionListener(e -> DialogoConexionBaseDatosExterna.mostrar(this));
        menuAyuda.add(itemBD);

        btnAyuda.addActionListener(e -> menuAyuda.show(btnAyuda, 0, btnAyuda.getHeight() + 4));
        panelDerecho.add(btnAyuda);

        Color cPeriodo = new Color(0x4D, 0xA3, 0xFF);     // Cielo #4DA3FF
        Color cComunicados = new Color(0x58, 0xC9, 0x9A); // Menta #58C99A
        Color cPoliticas = new Color(0xE8, 0xA3, 0x3D);   // Dorado #E8A33D

        // Badge de Periodo Contable Activo
        panelDerecho.add(crearPildoraPastel("Periodo: Noviembre 2026", new Color(0x4D, 0xA3, 0xFF, (int)(255 * 0.15)), cPeriodo, cPeriodo));

        // Botón Píldora: Tablón de Comunicados
        JLabel pildoraComunicados = crearPildoraPastel("Comunicados", new Color(0x58, 0xC9, 0x9A, (int)(255 * 0.15)), cComunicados, cComunicados);
        pildoraComunicados.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pildoraComunicados.setToolTipText("Abrir el Tablón Oficial de Comunicados y Circulares a Vecinos");
        pildoraComunicados.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mostrarDialogoComunicados();
            }
        });
        panelDerecho.add(pildoraComunicados);

        // Botón Píldora: Políticas y Parámetros (Exclusivo Administrador)
        if (SesionActual.esAdministrador()) {
            JLabel pildoraPoliticas = crearPildoraPastel("Políticas & Tasas", new Color(0xE8, 0xA3, 0x3D, (int)(255 * 0.15)), cPoliticas, cPoliticas);
            pildoraPoliticas.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pildoraPoliticas.setToolTipText("Configuración de tasa de mora, días de gracia y parámetros contables");
            pildoraPoliticas.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    mostrarDialogoConfiguracion();
                }
            });
            panelDerecho.add(pildoraPoliticas);
        }

        Color cUsuario = SesionActual.esAdministrador() ? EstilosUI.DORADO_PRINCIPAL
                : (SesionActual.esConserje() ? EstilosUI.ACENTO_OPERACIONES : EstilosUI.ACENTO_RESIDENTES);
        String etiquetaRol = "Rol: " + SesionActual.getRol() +
                (SesionActual.esResidente() ? " (Depto 101)" : (SesionActual.esConserje() ? " (Operaciones)" : " (Total)"));
        panelDerecho.add(crearPildoraPastel(etiquetaRol, new Color(cUsuario.getRed(), cUsuario.getGreen(), cUsuario.getBlue(), (int)(255 * 0.15)), cUsuario, cUsuario));

        header.add(panelDerecho, BorderLayout.EAST);
        return header;
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

                int d = Math.min(getWidth(), getHeight()) - 2;
                g2.setColor(new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 55));
                g2.fillOval(1, 1, d, d);
                g2.setColor(cAcento);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, d, d);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(cAcento);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(inicial)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(inicial, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setToolTipText("Usuario activo: " + SesionActual.getNombreUsuario() + " | Rol: " + SesionActual.getRol());
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cerrarSesionYCambiarRol();
            }
        });

        pnl.add(avatar);
        return pnl;
    }

    private JPanel crearBarraEstadoInferior() {
        JPanel panelBarraEstado = new JPanel(new BorderLayout());
        panelBarraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x2A, 0x3A, 0x4E)),
                new EmptyBorder(8, 20, 8, 20)
        ));
        panelBarraEstado.setBackground(new Color(0x0A, 0x0F, 0x16));

        JPanel panelEstadoIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        panelEstadoIzquierda.setOpaque(false);
        JLabel indBD = crearIndicadorEstado("BD Conectada", new Color(0x3D, 0xDC, 0x84));
        indBD.setCursor(new Cursor(Cursor.HAND_CURSOR));
        indBD.setToolTipText("Haga clic para ver o configurar Base de Datos Externa (PostgreSQL / MySQL)");
        indBD.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                DialogoConexionBaseDatosExterna.mostrar(VentanaPrincipal.this);
            }
        });
        panelEstadoIzquierda.add(indBD);
        panelEstadoIzquierda.add(crearIndicadorEstado("Fachada Lista", new Color(0x4D, 0xA3, 0xFF)));

        String perfilActivo = System.getProperty("spring.profiles.active", "dev");
        String textoMotor = ("external".equalsIgnoreCase(perfilActivo) || "prod".equalsIgnoreCase(perfilActivo)) ? "PostgreSQL / Externa" : "H2 Storage";
        JLabel indStorage = crearIndicadorEstado(textoMotor, new Color(0xE8, 0xC5, 0x47));
        indStorage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        indStorage.setToolTipText("Haga clic para ver o configurar Base de Datos Externa (PostgreSQL / MySQL)");
        indStorage.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                DialogoConexionBaseDatosExterna.mostrar(VentanaPrincipal.this);
            }
        });
        panelEstadoIzquierda.add(indStorage);
        panelEstadoIzquierda.add(crearIndicadorEstadoPulsante(ConstantesUI.ESTADO_FOOTER, new Color(0x3D, 0xDC, 0x84)));

        JLabel etiquetaVersion = new JLabel(ConstantesUI.VERSION_FOOTER);
        etiquetaVersion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiquetaVersion.setForeground(new Color(0xE8, 0xA3, 0x3D));
        etiquetaVersion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        etiquetaVersion.setToolTipText("Haga clic para ver información del sistema (Acerca de)");
        etiquetaVersion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mostrarDialogoAcercaDe();
            }
        });

        panelBarraEstado.add(panelEstadoIzquierda, BorderLayout.WEST);
        panelBarraEstado.add(etiquetaVersion, BorderLayout.EAST);
        return panelBarraEstado;
    }

    // Botón interactivo para el dock de navegación lateral con soporte para iconos vectoriales
    public static class BotonDockLateral extends JButton {
        private final String idCard;
        private final String etiquetaCorta;
        private final String tipoIcono;
        private final Color acento;
        private boolean activo = false;

        public BotonDockLateral(String idCard, String etiquetaCorta, String tipoIcono, Color acento, String tooltip) {
            this.idCard = idCard;
            this.etiquetaCorta = etiquetaCorta;
            this.tipoIcono = tipoIcono;
            this.acento = (acento != null) ? acento : EstilosUI.DORADO_PRINCIPAL;
            setToolTipText(tooltip);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(70, 52));
            setMaximumSize(new Dimension(70, 52));
            setMinimumSize(new Dimension(70, 52));
        }

        public void setActivo(boolean a) {
            this.activo = a;
            repaint();
        }

        public boolean isActivo() {
            return activo;
        }

        public String getIdCard() {
            return idCard;
        }

        public Color getAcento() {
            return acento;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radio = 14;

            ButtonModel model = getModel();
            boolean hover = model.isRollover();

            if (activo) {
                g2.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 60));
                g2.fillRoundRect(3, 2, w - 6, h - 4, radio, radio);

                g2.setColor(new Color(255, 255, 255, 45));
                g2.drawRoundRect(4, 3, w - 8, h - 6, radio, radio);

                g2.setColor(acento);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(3, 2, w - 6, h - 4, radio, radio);

                // Indicador vertical a la izquierda
                g2.setColor(acento);
                g2.fillRoundRect(0, h / 4, 3, h / 2, 2, 2);
            } else if (hover) {
                g2.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 30));
                g2.fillRoundRect(3, 2, w - 6, h - 4, radio, radio);
                g2.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(3, 2, w - 6, h - 4, radio, radio);
            } else {
                g2.setColor(new Color(16, 26, 38, 140));
                g2.fillRoundRect(3, 2, w - 6, h - 4, radio, radio);
                g2.setColor(new Color(42, 58, 78, 110));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(3, 2, w - 6, h - 4, radio, radio);
            }

            Color colorDibujo = activo ? acento : (hover ? Color.WHITE : EstilosUI.TEXTO_SECUNDARIO);

            int cx = w / 2;
            int cy = 17;
            dibujarIconoVector(g2, cx, cy, tipoIcono, colorDibujo);

            if (etiquetaCorta != null && !etiquetaCorta.isEmpty()) {
                g2.setFont(new Font("Segoe UI", activo ? Font.BOLD : Font.PLAIN, 10));
                g2.setColor(activo ? colorDibujo : (hover ? Color.WHITE : EstilosUI.TEXTO_MUTED));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(etiquetaCorta)) / 2;
                g2.drawString(etiquetaCorta, tx, h - 7);
            }

            g2.dispose();
        }

        private void dibujarIconoVector(Graphics2D g2, int cx, int cy, String tipo, Color c) {
            g2.setColor(c);
            switch (tipo) {
                case "DASHBOARD" -> {
                    int s = 5;
                    g2.fillRoundRect(cx - 7, cy - 7, s, s, 2, 2);
                    g2.fillRoundRect(cx + 2, cy - 7, s, s, 2, 2);
                    g2.fillRoundRect(cx - 7, cy + 2, s, s, 2, 2);
                    g2.fillRoundRect(cx + 2, cy + 2, s, s, 2, 2);
                }
                case "INMUEBLES" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRect(cx - 6, cy - 7, 12, 14);
                    g2.fillRect(cx - 4, cy - 4, 3, 3);
                    g2.fillRect(cx + 1, cy - 4, 3, 3);
                    g2.fillRect(cx - 4, cy + 1, 3, 3);
                    g2.fillRect(cx + 1, cy + 1, 3, 3);
                }
                case "RESIDENTES" -> {
                    g2.fillOval(cx - 3, cy - 7, 6, 6);
                    g2.fillArc(cx - 7, cy - 1, 14, 10, 0, 180);
                }
                case "COBRANZAS" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(cx - 8, cy - 6, 16, 12, 3, 3);
                    g2.fillRect(cx - 8, cy - 2, 16, 2);
                    g2.fillRect(cx - 5, cy + 2, 3, 2);
                }
                case "OPERACIONES" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(cx - 6, cy - 5, 12, 13, 2, 2);
                    g2.fillRect(cx - 3, cy - 7, 6, 3);
                    g2.drawLine(cx - 3, cy, cx + 3, cy);
                    g2.drawLine(cx - 3, cy + 4, cx + 3, cy + 4);
                }
                case "VISITAS" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    Polygon p = new Polygon();
                    p.addPoint(cx, cy - 7);
                    p.addPoint(cx + 6, cy - 4);
                    p.addPoint(cx + 6, cy + 2);
                    p.addPoint(cx, cy + 7);
                    p.addPoint(cx - 6, cy + 2);
                    p.addPoint(cx - 6, cy - 4);
                    g2.drawPolygon(p);
                    g2.fillOval(cx - 2, cy - 1, 4, 4);
                }
                case "RESERVAS" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(cx - 7, cy - 5, 14, 12, 2, 2);
                    g2.drawLine(cx - 7, cy - 1, cx + 7, cy - 1);
                    g2.drawLine(cx - 4, cy - 7, cx - 4, cy - 5);
                    g2.drawLine(cx + 4, cy - 7, cx + 4, cy - 5);
                }
                case "PATRONES" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    Polygon p = new Polygon();
                    p.addPoint(cx, cy - 7);
                    p.addPoint(cx + 6, cy);
                    p.addPoint(cx, cy + 7);
                    p.addPoint(cx - 6, cy);
                    g2.drawPolygon(p);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                    g2.drawString("15", cx - 4, cy + 3);
                }
                case "SALIR" -> {
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawArc(cx - 6, cy - 6, 12, 12, 45, 270);
                    g2.drawLine(cx, cy - 7, cx, cy - 2);
                }
                case "ACERCA" -> {
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawOval(cx - 6, cy - 6, 12, 12);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString("i", cx - 2, cy + 3);
                }
                default -> {
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                }
            }
        }
    }

    // Muestra el diálogo institucional Acerca de en tema Glassmorphic dark consistente.
    public void mostrarDialogoAcercaDe() {
        JDialog dlg = new JDialog(this, "Acerca de " + ConstantesUI.TITULO_APLICACION, true);
        dlg.setSize(640, 490);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelContenedor = new JPanel(new BorderLayout(0, 14));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Banner superior dorado
        JPanel banner = EstilosUI.crearPanelBanner(EstilosUI.DORADO_PRINCIPAL);
        JLabel lblTit = new JLabel(ConstantesUI.TITULO_APLICACION + " " + ConstantesUI.VERSION);
        lblTit.setFont(EstilosUI.FUENTE_BANNER);
        lblTit.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel lblSub = new JLabel(ConstantesUI.SUBTITULO_APLICACION);
        lblSub.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSub.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        banner.add(lblTit, BorderLayout.NORTH);
        banner.add(lblSub, BorderLayout.SOUTH);
        panelContenedor.add(banner, BorderLayout.NORTH);

        // 2. Tarjeta central de vidrio esmerilado con la ficha técnica institucional
        GlassPanel tarjetaInfo = new GlassPanel(18, false, EstilosUI.DORADO_PRINCIPAL);
        tarjetaInfo.setLayout(new BorderLayout(0, 8));
        tarjetaInfo.setBorder(new EmptyBorder(14, 16, 14, 16));

        JTextArea txt = new JTextArea(ConstantesUI.MENSAJE_ACERCA_DE);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setFont(EstilosUI.FUENTE_TEXTO);
        txt.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        tarjetaInfo.add(txt, BorderLayout.CENTER);

        panelContenedor.add(tarjetaInfo, BorderLayout.CENTER);

        // 3. Barra inferior de control
        JPanel pnlSur = new JPanel(new BorderLayout(10, 0));
        pnlSur.setOpaque(false);

        JPanel pnlSurDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSurDerecha.setOpaque(false);

        if (SesionActual.esAdministrador()) {
            JCheckBox chkAnim = new JCheckBox("Efectos visuales activos", ConstantesUI.ANIMACIONES_ACTIVAS);
            chkAnim.setOpaque(false);
            chkAnim.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chkAnim.setForeground(EstilosUI.TEXTO_SECUNDARIO);
            chkAnim.addActionListener(e -> ConstantesUI.ANIMACIONES_ACTIVAS = chkAnim.isSelected());
            pnlSurDerecha.add(chkAnim);
        }

        JButton btnEntendido = new JButton("Entendido");
        EstilosUI.estilizarBoton(btnEntendido, EstilosUI.DORADO_PRINCIPAL);
        btnEntendido.setPreferredSize(new Dimension(110, 34));
        btnEntendido.addActionListener(e -> dlg.dispose());
        pnlSurDerecha.add(btnEntendido);

        JButton btnBD = new JButton("Base de Datos Externa");
        EstilosUI.estilizarBoton(btnBD, EstilosUI.AZUL_INFO);
        btnBD.setPreferredSize(new Dimension(175, 34));
        btnBD.addActionListener(e -> {
            dlg.dispose();
            DialogoConexionBaseDatosExterna.mostrar(this);
        });
        pnlSur.add(btnBD, BorderLayout.WEST);

        pnlSur.add(pnlSurDerecha, BorderLayout.EAST);
        panelContenedor.add(pnlSur, BorderLayout.SOUTH);

        dlg.setContentPane(panelContenedor);
        dlg.setVisible(true);
    }

    // Abre el tablón oficial de comunicados y circulares condominales.
    public void mostrarDialogoComunicados() {
        DialogoComunicados dialogo = new DialogoComunicados(this, this.fachadaCobranza);
        dialogo.setVisible(true);
    }

    // Abre el diálogo de configuración de políticas y parámetros operacionales del condominio.
    public void mostrarDialogoConfiguracion() {
        DialogoConfiguracionCondominio dialogo = new DialogoConfiguracionCondominio(this, this.fachadaCobranza);
        dialogo.setVisible(true);
    }

    // Abre el catálogo interactivo de los 15 patrones GoF implementados en el sistema.
    public void mostrarDialogoCatalogoPatrones() {
        DialogoCatalogoPatrones dialogo = new DialogoCatalogoPatrones(this);
        dialogo.setVisible(true);
    }

    // COMPONENTES DECORATIVOS Y UTILITARIOS (SIN EMOJIS)

    private JLabel crearPildoraPastel(String texto, Color colorFondo, Color colorTexto, Color colorBorde) {
        JLabel etiqueta = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D grafico = (Graphics2D) g.create();
                grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                grafico.setColor(colorFondo);
                grafico.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                grafico.setColor(colorBorde);
                grafico.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                grafico.dispose();
                super.paintComponent(g);
            }
        };
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiqueta.setForeground(colorTexto);
        etiqueta.setBorder(new EmptyBorder(4, 12, 4, 12));
        etiqueta.setOpaque(false);
        return etiqueta;
    }

    private JLabel crearIndicadorEstado(String texto, Color colorPunto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiqueta.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        etiqueta.setIcon(new IconoPunto(colorPunto, 8));
        etiqueta.setIconTextGap(8);
        return etiqueta;
    }

    private JLabel crearIndicadorEstadoPulsante(String texto, Color colorPunto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiqueta.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        IconoPuntoPulsante icono = new IconoPuntoPulsante(colorPunto, 9);
        etiqueta.setIcon(icono);
        etiqueta.setIconTextGap(8);
        return etiqueta;
    }

    private static class IconoPunto implements Icon {
        private final Color color;
        private final int diametro;

        public IconoPunto(Color color, int diametro) {
            this.color = color;
            this.diametro = diametro;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D grafico = (Graphics2D) g.create();
            grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            grafico.setColor(color);
            grafico.fillOval(x, y + 2, diametro, diametro);
            grafico.dispose();
        }

        @Override public int getIconWidth() { return diametro; }
        @Override public int getIconHeight() { return diametro + 4; }
    }

    private static class IconoPuntoPulsante implements Icon {
        private final Color color;
        private final int diametro;
        private float opacidad = 1.0f;
        private boolean decreciendo = true;

        public IconoPuntoPulsante(Color color, int diametro) {
            this.color = color;
            this.diametro = diametro;
            Timer temporizador = new Timer(90, evento -> {
                if (decreciendo) {
                    opacidad -= 0.06f;
                    if (opacidad <= 0.40f) {
                        opacidad = 0.40f;
                        decreciendo = false;
                    }
                } else {
                    opacidad += 0.06f;
                    if (opacidad >= 1.0f) {
                        opacidad = 1.0f;
                        decreciendo = true;
                    }
                }
            });
            temporizador.start();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D grafico = (Graphics2D) g.create();
            grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int alfa = Math.max(0, Math.min(255, (int) (opacidad * 255)));
            grafico.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa));
            grafico.fillOval(x, y + 2, diametro, diametro);
            grafico.dispose();
            c.repaint();
        }

        @Override public int getIconWidth() { return diametro; }
        @Override public int getIconHeight() { return diametro + 4; }
    }

    // Componente gráfico que muestra el título y subtítulo corporativo completo
    public static class TituloCobranzaDinamico extends JComponent {
        private final String titulo = "Cobranza Vecinal";
        private final String subtitulo = "SISTEMA DE ADMINISTRACIÓN Y GESTIÓN DE CONDOMINIOS";
        private float faseGlint = 0.0f;
        private final Timer temporizadorBrillo;

        public TituloCobranzaDinamico() {
            setOpaque(false);
            temporizadorBrillo = new Timer(40, evento -> {
                faseGlint += 0.02f;
                if (faseGlint > 3.2f) {
                    faseGlint = 0.0f;
                }
                // Repintar únicamente durante el paso activo del destello para máxima ligereza y fluidez
                if (faseGlint <= 1.4f) {
                    repaint();
                }
            });
            temporizadorBrillo.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(480, 52);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D grafico = (Graphics2D) g.create();
            grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            grafico.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 22);
            grafico.setFont(fuenteTitulo);

            int baseLineY = 28;

            // Título elegante en degradado dorado noble sobre fondo oscuro
            GradientPaint gradiente = new GradientPaint(
                    0, 10, new Color(255, 215, 0),       // #FFD700 Dorado
                    260, 36, new Color(245, 158, 11)     // #F59E0B Ámbar Cálido
            );
            grafico.setPaint(gradiente);
            grafico.drawString(titulo, 0, baseLineY);

            // Efecto glint
            if (faseGlint <= 1.2f) {
                int glintX = (int) (faseGlint * 240);
                Graphics2D gGlint = (Graphics2D) grafico.create();
                gGlint.setClip(glintX - 20, 0, 40, getHeight());
                gGlint.setColor(new Color(255, 255, 255, 220));
                gGlint.drawString(titulo, 0, baseLineY);
                gGlint.dispose();
            }

            // Subtítulo completo sin emojis ni truncamiento
            grafico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            grafico.setColor(EstilosUI.TEXTO_SECUNDARIO);
            grafico.drawString(subtitulo, 0, baseLineY + 16);

            grafico.dispose();
        }
    }

    public void abrirEstadoCuentaVecino() {
        if (!GraphicsEnvironment.isHeadless()) {
            DialogoEstadoCuentaVecino dialogo = new DialogoEstadoCuentaVecino(this, fachadaCobranza, "101");
            dialogo.setVisible(true);
        }
    }

    // Cierra la sesión activa actual y despliega la pantalla de acceso RBAC
    public void cerrarSesionYCambiarRol() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar la sesión actual de '" + SesionActual.getNombreUsuario() +
                        "' (" + SesionActual.getRol() + ") y cambiar a otro rol?",
                "Cerrar Sesión - " + ConstantesUI.TITULO_APLICACION,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SesionActual.cerrarSesion();
        this.dispose();

        SwingUtilities.invokeLater(() -> {
            DialogoLogin login = new DialogoLogin(null, this.fachadaCobranza);
            login.setVisible(true);

            if (login.isLoginExitoso()) {
                VentanaPrincipal nuevaVentana = new VentanaPrincipal(this.fachadaCobranza);
                GestorBandejaSistema.registrarVentana(nuevaVentana, nuevaVentana::abrirEstadoCuentaVecino);
                nuevaVentana.setVisible(true);
            } else {
                GestorBandejaSistema.removerTray();
                System.exit(0);
            }
        });
    }
}
