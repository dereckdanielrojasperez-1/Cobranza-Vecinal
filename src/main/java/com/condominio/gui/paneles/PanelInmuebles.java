package com.condominio.gui.paneles;

import com.condominio.dto.InmuebleDTO;
import com.condominio.gui.DialogoEstadoCuentaVecino;
import com.condominio.gui.VentanaEdificiosTotales;
import com.condominio.gui.util.CampoPlaceholder;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroAlfabetico;
import com.condominio.gui.util.FiltroDecimal;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.Validaciones;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.patrones.composite.CompuestoInmueble;
import com.condominio.patrones.composite.HojaInmueble;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import com.condominio.patrones.strategy.CalculadoraGastos;
import com.condominio.patrones.strategy.ProrrateoEquitativo;
import com.condominio.patrones.strategy.ProrrateoPorAlicuota;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Panel interactivo para el módulo de Gestión de Inmuebles, Departamentos, Torres y Edificios.
@Slf4j
public class PanelInmuebles extends JPanel {

    private final CobranzaFachada fachada;
    private JTable tablaInmuebles;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;
    private JLabel lblContadorRegistros;
    private JTextField txtNumero;
    private JTextField txtPiso;
    private JTextField txtMetrosCuadrados;
    private JComboBox<String> comboTorre;
    private JComboBox<String> comboEdificio;
    private JButton btnRegistrar;
    private JButton btnComposite;
    private JButton btnRefrescar;
    private JButton btnDesborde;
    private JLabel etiquetaMensajeEstado;

    // Sub-panel Registrar Gasto Común (Strategy) - Tarjeta Plegable
    private JComboBox<String> comboTipoGasto;
    private JTextField txtMontoGasto;
    private JComboBox<String> comboEstrategiaGasto;
    private JButton btnRegistrarGasto;
    private JButton btnToggleGastoComun;
    private JPanel panelCuerpoGastoComun;

    public PanelInmuebles(CobranzaFachada fachada) {
        this.fachada = fachada;
        inicializarComponentes();
        cargarCombosTorresYEdificios();
        cargarDatosTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 8));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        add(construirBannerInformativo(), BorderLayout.NORTH);
        add(construirTablaCentro(), BorderLayout.CENTER);
        add(construirPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel construirBannerInformativo() {
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_INMUEBLES);

        JLabel tituloBanner = new JLabel("MÓDULO DE GESTIÓN Y ADMINISTRACIÓN DE INMUEBLES");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.ACENTO_INMUEBLES);

        JLabel subtituloBanner = new JLabel("Catálogo de departamentos, torres y complejos residenciales.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        return panelBanner;
    }

    private JPanel construirTablaCentro() {
        String[] columnas = { "ID", "Número", "Piso", "Metros Cuadrados", "Torre", "Edificio" };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaInmuebles = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.ACENTO_INMUEBLES);
        aplicarRenderizadoresTabla();
        enlazarEventosTabla();

        tablaInmuebles.setPreferredScrollableViewportSize(new Dimension(800, 142));
        scrollTabla = new JScrollPane(tablaInmuebles);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollTabla.setPreferredSize(new Dimension(800, 178));

        JPanel panelCentro = new JPanel(new BorderLayout(0, 6));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelContador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelContador.setOpaque(false);
        lblContadorRegistros = new JLabel("Mostrando 0 de 0 departamentos registrados");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContadorRegistros.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelContador.add(lblContadorRegistros);
        panelCentro.add(panelContador, BorderLayout.SOUTH);

        return panelCentro;
    }

    private void aplicarRenderizadoresTabla() {
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaInmuebles.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaInmuebles.getColumnModel().getColumn(1).setCellRenderer(renderCentro);
        tablaInmuebles.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaInmuebles.getColumnModel().getColumn(4).setCellRenderer(renderCentro);
        tablaInmuebles.getColumnModel().getColumn(5).setCellRenderer(renderCentro);

        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaInmuebles.getColumnModel().getColumn(3).setCellRenderer(renderDerecha);
    }

    private void enlazarEventosTabla() {
        tablaInmuebles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaInmuebles.getSelectedRow();
                if (fila != -1) {
                    Object num = tablaInmuebles.getValueAt(fila, 1);
                    Object torre = tablaInmuebles.getValueAt(fila, 4);
                    Object edificio = tablaInmuebles.getValueAt(fila, 5);
                    etiquetaMensajeEstado.setText("Departamento seleccionado: Depto " + num + " (" + torre + " - " + edificio + ")");
                    etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);
                }
            }
        });

        tablaInmuebles.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            private void procesarPopup(MouseEvent e) {
                int fila = tablaInmuebles.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaInmuebles.getRowCount()) {
                    tablaInmuebles.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualFila(e.getX(), e.getY(), fila);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    int fila = tablaInmuebles.rowAtPoint(e.getPoint());
                    if (fila >= 0 && fila < tablaInmuebles.getRowCount()) {
                        tablaInmuebles.setRowSelectionInterval(fila, fila);
                        Object numObj = tablaInmuebles.getValueAt(fila, 1);
                        if (numObj != null) {
                            DialogoEstadoCuentaVecino dlg = new DialogoEstadoCuentaVecino(
                                    SwingUtilities.getWindowAncestor(PanelInmuebles.this),
                                    fachada,
                                    numObj.toString()
                            );
                            dlg.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    private JPanel construirFormularioRegistro() {
        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_INMUEBLES, 14);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Etiquetas
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Número Depto:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Piso:"), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Metraje (m²):"), gbc);
        gbc.gridx = 3; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Nombre Torre:"), gbc);
        gbc.gridx = 4; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Nombre Edificio:"), gbc);

        // Fila 1: Inputs
        gbc.gridx = 0; gbc.gridy = 1;
        txtNumero = EstilosUI.crearCampoPlaceholder("Ej: 101", EstilosUI.ACENTO_INMUEBLES);
        FiltroAlfabetico.aplicarParaDepartamento(txtNumero, 6);
        txtNumero.setPreferredSize(new Dimension(100, 32));
        txtNumero.setToolTipText("Número de departamento (1 a 6 caracteres sin espacios, ej: 101, A-102)");
        panelFormulario.add(txtNumero, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtPiso = EstilosUI.crearCampoPlaceholder("Ej: 1 a 30", EstilosUI.ACENTO_INMUEBLES);
        FiltroNumerico.aplicar(txtPiso, 2);
        txtPiso.setPreferredSize(new Dimension(80, 32));
        txtPiso.setToolTipText("Número de piso (entero del 1 al 30)");
        panelFormulario.add(txtPiso, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        txtMetrosCuadrados = EstilosUI.crearCampoPlaceholder("Ej: 85.50", EstilosUI.ACENTO_INMUEBLES);
        FiltroDecimal.aplicar(txtMetrosCuadrados, 7);
        txtMetrosCuadrados.setPreferredSize(new Dimension(100, 32));
        txtMetrosCuadrados.setToolTipText("Área construida en metros cuadrados (rango 10.00 a 1000.00 m²)");
        panelFormulario.add(txtMetrosCuadrados, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        comboTorre = new JComboBox<>();
        comboTorre.setEditable(false);
        EstilosUI.estilizarCombo(comboTorre, EstilosUI.ACENTO_INMUEBLES);
        EstilosUI.configurarComboPrototipoYTooltip(comboTorre, "Torre Residencial B");
        comboTorre.setPreferredSize(new Dimension(150, 32));
        comboTorre.setToolTipText("Seleccione la torre correspondiente");
        panelFormulario.add(comboTorre, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        comboEdificio = new JComboBox<>();
        comboEdificio.setEditable(false);
        EstilosUI.estilizarCombo(comboEdificio, EstilosUI.ACENTO_INMUEBLES);
        EstilosUI.configurarComboPrototipoYTooltip(comboEdificio, "Edificio Los Sauces Central");
        comboEdificio.setPreferredSize(new Dimension(190, 32));
        comboEdificio.setToolTipText("Seleccione el complejo o edificio residencial");
        panelFormulario.add(comboEdificio, gbc);

        gbc.gridx = 5; gbc.gridy = 1;
        btnRegistrar = new JButton("Registrar Departamento");
        EstilosUI.estilizarBoton(btnRegistrar, EstilosUI.ACENTO_INMUEBLES);
        btnRegistrar.setToolTipText("Valida e ingresa un nuevo departamento en la base de datos");
        btnRegistrar.addActionListener(e -> ejecutarRegistroDepartamento());
        panelFormulario.add(btnRegistrar, gbc);

        gbc.gridx = 6; gbc.gridy = 1;
        btnDesborde = new JButton("...");
        EstilosUI.estilizarBotonSecundario(btnDesborde, EstilosUI.ACENTO_INMUEBLES);
        btnDesborde.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDesborde.setPreferredSize(new Dimension(38, 32));
        btnDesborde.setToolTipText("Más acciones: Árbol Jerárquico, Edificios Totales y Refrescar");
        btnDesborde.addActionListener(e -> mostrarMenuDesborde(btnDesborde, 0, btnDesborde.getHeight()));
        panelFormulario.add(btnDesborde, gbc);

        btnComposite = new JButton("Árbol Jerárquico");
        btnRefrescar = new JButton("Refrescar");

        return panelFormulario;
    }

    private JPanel construirPanelAcordeonGastoComun() {
        JPanel panelGastoComunAcordeon = new JPanel(new BorderLayout(0, 4));
        panelGastoComunAcordeon.setOpaque(false);

        btnToggleGastoComun = new JButton("[+] REGISTRAR GASTO COMÚN (PRORRATEO POR ALÍCUOTA / EQUITATIVO)");
        btnToggleGastoComun.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleGastoComun.setFocusPainted(false);
        btnToggleGastoComun.setContentAreaFilled(false);
        btnToggleGastoComun.setOpaque(false);
        btnToggleGastoComun.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleGastoComun.setPreferredSize(new Dimension(btnToggleGastoComun.getPreferredSize().width, 38));
        btnToggleGastoComun.setToolTipText("Haga clic para desplegar o plegar el formulario de prorrateo de gastos comunes");

        btnToggleGastoComun.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = b.getWidth();
                int h = b.getHeight();

                Color fondoSuperior = new Color(0x13, 0x2E, 0x27);
                Color fondoInferior = new Color(0x0C, 0x1E, 0x19);
                Color colorBorde    = EstilosUI.INMUEBLES_NORMAL;
                Color colorBadge    = EstilosUI.INMUEBLES_BRILLO;

                if (model.isPressed()) {
                    fondoSuperior = new Color(0x1A, 0x42, 0x38);
                    fondoInferior = new Color(0x14, 0x33, 0x2B);
                    colorBorde    = Color.WHITE;
                } else if (model.isRollover()) {
                    fondoSuperior = new Color(0x1D, 0x4C, 0x40);
                    fondoInferior = new Color(0x13, 0x35, 0x2C);
                    colorBorde    = new Color(0x8A, 0xF5, 0xCB);
                    colorBadge    = Color.WHITE;
                }

                GradientPaint gp = new GradientPaint(0, 0, fondoSuperior, 0, h, fondoInferior);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

                g2.setColor(colorBadge);
                g2.fillRoundRect(8, 7, 24, h - 14, 6, 6);

                g2.setColor(new Color(0x0D, 0x1A, 0x15));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String textoCompleto = b.getText();
                boolean esMenos = textoCompleto.startsWith("[-]");
                String signo = esMenos ? "-" : "+";
                FontMetrics fmSigno = g2.getFontMetrics();
                int xSigno = 8 + (24 - fmSigno.stringWidth(signo)) / 2;
                int ySigno = 7 + (h - 14 - fmSigno.getHeight()) / 2 + fmSigno.getAscent() - 1;
                g2.drawString(signo, xSigno, ySigno);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int yTexto = (h - fm.getHeight()) / 2 + fm.getAscent();

                String textoLimpio = textoCompleto.replace("[+]", "").replace("[-]", "").trim();
                g2.drawString(textoLimpio, 40, yTexto);

                String hintDerecho = esMenos ? "(Clic para plegar formulario)" : "(Clic para desplegar y prorratear)";
                g2.setColor(new Color(0xA3, 0xD9, 0xC4));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                FontMetrics fmHint = g2.getFontMetrics();
                int xHint = w - fmHint.stringWidth(hintDerecho) - 16;
                if (xHint > 400) {
                    g2.drawString(hintDerecho, xHint, yTexto);
                }

                g2.dispose();
            }
        });

        panelCuerpoGastoComun = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_INMUEBLES, 10);
        panelCuerpoGastoComun.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelCuerpoGastoComun.setBorder(new EmptyBorder(6, 12, 6, 12));
        panelCuerpoGastoComun.setVisible(false);

        panelCuerpoGastoComun.add(EstilosUI.crearEtiquetaCampo("Tipo:"));
        comboTipoGasto = new JComboBox<>(new String[]{"AGUA", "LUZ", "LIMPIEZA"});
        comboTipoGasto.setEditable(false);
        EstilosUI.estilizarCombo(comboTipoGasto, EstilosUI.ACENTO_INMUEBLES);
        EstilosUI.configurarComboPrototipoYTooltip(comboTipoGasto, "LIMPIEZA");
        comboTipoGasto.setPreferredSize(new Dimension(120, 32));
        comboTipoGasto.setToolTipText("Tipo de gasto común ordinario");
        panelCuerpoGastoComun.add(comboTipoGasto);

        panelCuerpoGastoComun.add(EstilosUI.crearEtiquetaCampo("Monto Total (S/):"));
        txtMontoGasto = EstilosUI.crearCampoPlaceholder("Ej: 600.00", EstilosUI.ACENTO_INMUEBLES);
        FiltroDecimal.aplicar(txtMontoGasto, 9);
        txtMontoGasto.setPreferredSize(new Dimension(100, 32));
        txtMontoGasto.setToolTipText("Monto global facturado (máximo S/ 999,999.99)");
        panelCuerpoGastoComun.add(txtMontoGasto);

        panelCuerpoGastoComun.add(EstilosUI.crearEtiquetaCampo("Criterio:"));
        comboEstrategiaGasto = new JComboBox<>(new String[]{
                "Prorrateo por Alícuota (m²)",
                "Prorrateo Equitativo"
        });
        comboEstrategiaGasto.setEditable(false);
        EstilosUI.estilizarCombo(comboEstrategiaGasto, EstilosUI.ACENTO_INMUEBLES);
        EstilosUI.configurarComboPrototipoYTooltip(comboEstrategiaGasto, "Prorrateo por Alícuota (m²)");
        comboEstrategiaGasto.setPreferredSize(new Dimension(240, 32));
        comboEstrategiaGasto.setToolTipText("Algoritmo de distribución proporcional de gastos");
        panelCuerpoGastoComun.add(comboEstrategiaGasto);

        btnRegistrarGasto = new JButton("Registrar Gasto Común");
        EstilosUI.estilizarBoton(btnRegistrarGasto, EstilosUI.ACENTO_INMUEBLES);
        btnRegistrarGasto.setToolTipText("Calcula el prorrateo y muestra el desglose antes de persistir");
        btnRegistrarGasto.addActionListener(e -> ejecutarRegistroGasto());
        panelCuerpoGastoComun.add(btnRegistrarGasto);

        btnToggleGastoComun.addActionListener(e -> {
            boolean visible = !panelCuerpoGastoComun.isVisible();
            panelCuerpoGastoComun.setVisible(visible);
            btnToggleGastoComun.setText(visible
                    ? "[-] REGISTRAR GASTO COMÚN (PRORRATEO POR ALÍCUOTA / EQUITATIVO)"
                    : "[+] REGISTRAR GASTO COMÚN (PRORRATEO POR ALÍCUOTA / EQUITATIVO)");
            panelGastoComunAcordeon.revalidate();
            panelGastoComunAcordeon.repaint();
            if (getRootPane() != null) {
                getRootPane().revalidate();
            }
        });

        panelGastoComunAcordeon.add(btnToggleGastoComun, BorderLayout.NORTH);
        panelGastoComunAcordeon.add(panelCuerpoGastoComun, BorderLayout.CENTER);
        return panelGastoComunAcordeon;
    }

    private JPanel construirPanelInferior() {
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);

        JPanel panelContenedor = new JPanel(new BorderLayout(0, 6));
        panelContenedor.setOpaque(false);
        panelContenedor.add(construirFormularioRegistro(), BorderLayout.CENTER);
        panelContenedor.add(construirPanelAcordeonGastoComun(), BorderLayout.SOUTH);

        panelInferior.add(panelContenedor, BorderLayout.CENTER);

        etiquetaMensajeEstado = new JLabel("Listo para administrar unidades inmobiliarias.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);

        return panelInferior;
    }

    public void cargarCombosTorresYEdificios() {
        if (fachada == null) return;

        try {
            comboEdificio.removeAllItems();
            List<Edificio> edificios = fachada.listarEdificios();
            if (edificios != null && !edificios.isEmpty()) {
                for (Edificio ed : edificios) {
                    if (ed.getNombre() != null && !ed.getNombre().isBlank()) {
                        comboEdificio.addItem(ed.getNombre().trim());
                    }
                }
            } else {
                comboEdificio.addItem("Edificio Los Sauces");
                comboEdificio.addItem("Edificio Las Palmeras");
            }

            comboTorre.removeAllItems();
            List<Torre> torres = fachada.listarTorres();
            if (torres != null && !torres.isEmpty()) {
                for (Torre t : torres) {
                    if (t.getNombre() != null && !t.getNombre().isBlank()) {
                        comboTorre.addItem(t.getNombre().trim());
                    }
                }
            } else {
                comboTorre.addItem("Torre A");
                comboTorre.addItem("Torre B");
            }
        } catch (Exception ex) {
            log.warn("No se pudieron cargar combos dinámicos de torres y edificios: {}", ex.getMessage());
            if (comboEdificio.getItemCount() == 0) comboEdificio.addItem("Edificio Los Sauces");
            if (comboTorre.getItemCount() == 0) comboTorre.addItem("Torre A");
        }
        if (comboEdificio.getSelectedItem() != null) {
            comboEdificio.setToolTipText(comboEdificio.getSelectedItem().toString());
        }
        if (comboTorre.getSelectedItem() != null) {
            comboTorre.setToolTipText(comboTorre.getSelectedItem().toString());
        }
    }

    // Consulta asíncrona de inmuebles mediante SwingWorker para proteger el EDT.
    public void cargarDatosTabla() {
        if (fachada == null) return;

        etiquetaMensajeEstado.setText("Sincronizando departamentos con la base de datos...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<List<InmuebleDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InmuebleDTO> doInBackground() {
                return fachada.listarInmuebles();
            }

            @Override
            protected void done() {
                try {
                    poblarFilasTablaInmuebles(get());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    ManejadorErroresUI.mostrarError(PanelInmuebles.this, "Operación interrumpida durante la carga de inmuebles", ie);
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelInmuebles.this, "Error al sincronizar inmuebles desde la persistencia", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private void poblarFilasTablaInmuebles(List<InmuebleDTO> inmuebles) {
        modeloTabla.setRowCount(0);
        int total = (inmuebles != null) ? inmuebles.size() : 0;
        if (inmuebles != null) {
            for (InmuebleDTO i : inmuebles) {
                modeloTabla.addRow(new Object[]{
                        i.getId(),
                        i.getNumero(),
                        i.getPiso(),
                        String.format("%.2f m²", i.getMetrosCuadrados()),
                        i.getNombreTorre(),
                        i.getNombreEdificio()
                });
            }
        }
        if (scrollTabla != null) {
            scrollTabla.getVerticalScrollBar().setValue(0);
        }
        if (tablaInmuebles != null) {
            tablaInmuebles.setPreferredScrollableViewportSize(new Dimension(800, 142));
        }
        if (btnRefrescar != null) {
            btnRefrescar.setEnabled(true);
        }
        lblContadorRegistros.setText("Mostrando " + total + " de " + total + " departamentos registrados");
        etiquetaMensajeEstado.setText("Módulo sincronizado: " + total + " registros.");
        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
    }

    private void ejecutarRegistroDepartamento() {
        String numStr = txtNumero.getText().trim();
        String pisoStr = txtPiso.getText().trim();
        String metrosStr = txtMetrosCuadrados.getText().trim();
        String torreStr = (comboTorre.getSelectedItem() != null) ? comboTorre.getSelectedItem().toString().trim() : "";
        String edificioStr = (comboEdificio.getSelectedItem() != null) ? comboEdificio.getSelectedItem().toString().trim() : "";

        // 1. Validación de número de departamento
        try {
            Validaciones.validarNumeroDepartamento(numStr);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtNumero.requestFocusInWindow();
            return;
        }

        // 2. Validación de piso (entero 1 a 30)
        int piso;
        try {
            piso = Validaciones.validarPiso(pisoStr);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtPiso.requestFocusInWindow();
            return;
        }

        // 3. Validación de metraje (decimal 10.00 a 1000.00)
        double metros;
        try {
            metros = Validaciones.validarMetraje(metrosStr);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtMetrosCuadrados.requestFocusInWindow();
            return;
        }

        // 4. Validación de torre y edificio
        if (torreStr.isEmpty()) {
            etiquetaMensajeEstado.setText("Error: Debe seleccionar una torre.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            comboTorre.requestFocusInWindow();
            return;
        }
        if (edificioStr.isEmpty()) {
            etiquetaMensajeEstado.setText("Error: Debe seleccionar un edificio.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            comboEdificio.requestFocusInWindow();
            return;
        }

        // 5. Validación de unicidad de departamento en torre y edificio
        if (fachada != null && fachada.existeDepartamentoPorNumeroTorreYEdificio(numStr, torreStr, edificioStr)) {
            etiquetaMensajeEstado.setText("Error: Ya existe un departamento con el número " + numStr + " en " + torreStr + " (" + edificioStr + ").");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtNumero.requestFocusInWindow();
            return;
        }

        // 6. Persistencia mediante SwingWorker para evitar congelamiento y doble clic
        btnRegistrar.setEnabled(false);
        etiquetaMensajeEstado.setText("Registrando departamento en la base de datos...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        InmuebleDTO dto = InmuebleDTO.builder()
                .numero(numStr)
                .piso(piso)
                .metrosCuadrados(metros)
                .nombreTorre(torreStr)
                .nombreEdificio(edificioStr)
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.registrarDepartamento(dto);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    get();
                    ManejadorErroresUI.mostrarExito(PanelInmuebles.this,
                            "Departamento Nº " + numStr + " registrado correctamente en " + torreStr + " (" + edificioStr + ").",
                            "Registro Inmobiliario Exitoso");

                    limpiarFormulario();
                    cargarDatosTabla();
                    com.condominio.gui.animacion.DestelloFila.resaltar(tablaInmuebles, tablaInmuebles.getRowCount() - 1, EstilosUI.ACENTO_INMUEBLES);
                    etiquetaMensajeEstado.setText("Departamento " + numStr + " registrado exitosamente.");
                    etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelInmuebles.this, "No se pudo registrar el departamento", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                    txtNumero.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void abrirDialogoJerarquiaComposite() {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Estructura Jerárquica del Condominio", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(540, 440);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(12, 12));
        dialogo.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        List<InmuebleDTO> lista = fachada.listarInmuebles();

        CompuestoInmueble raizCondominio = new CompuestoInmueble("Condominio Residencial");
        java.util.Map<String, CompuestoInmueble> mapaEdificios = new java.util.HashMap<>();
        java.util.Map<String, CompuestoInmueble> mapaTorres = new java.util.HashMap<>();

        if (lista != null) {
            for (InmuebleDTO dto : lista) {
                String nomEdificio = dto.getNombreEdificio() != null ? dto.getNombreEdificio() : "Edificio General";
                String nomTorre = dto.getNombreTorre() != null ? dto.getNombreTorre() : "Torre General";

                CompuestoInmueble compEdificio = mapaEdificios.computeIfAbsent(nomEdificio, k -> {
                    CompuestoInmueble ed = new CompuestoInmueble(k);
                    raizCondominio.agregarHijo(ed);
                    return ed;
                });

                String claveTorre = nomEdificio + " - " + nomTorre;
                CompuestoInmueble compTorre = mapaTorres.computeIfAbsent(claveTorre, k -> {
                    CompuestoInmueble tr = new CompuestoInmueble(nomTorre);
                    compEdificio.agregarHijo(tr);
                    return tr;
                });

                HojaInmueble hojaDepto = new HojaInmueble("Depto " + dto.getNumero(), dto.getMetrosCuadrados());
                compTorre.agregarHijo(hojaDepto);
            }
        }

        double areaTotal = raizCondominio.calcularAreaTotal();

        StringBuilder sb = new StringBuilder();
        sb.append("ESTRUCTURA JERÁRQUICA DEL CONDOMINIO\n\n");
        sb.append("Condominio General (Área Total Construida: ").append(String.format("%.2f m²", areaTotal)).append(")\n");

        for (com.condominio.patrones.composite.ComponenteInmueble hijoEdificio : raizCondominio.getHijos()) {
            sb.append("+-- [Edificio] ").append(hijoEdificio.obtenerNombre())
                    .append(" (Área: ").append(String.format("%.2f m²", hijoEdificio.calcularAreaTotal())).append(")\n");

            if (hijoEdificio instanceof CompuestoInmueble compEd) {
                for (com.condominio.patrones.composite.ComponenteInmueble hijoTorre : compEd.getHijos()) {
                    sb.append("|   +-- [Torre] ").append(hijoTorre.obtenerNombre())
                            .append(" (Área: ").append(String.format("%.2f m²", hijoTorre.calcularAreaTotal())).append(")\n");

                    if (hijoTorre instanceof CompuestoInmueble compTr) {
                        for (com.condominio.patrones.composite.ComponenteInmueble hijoDepto : compTr.getHijos()) {
                            sb.append("|   |   \\-- ").append(hijoDepto.obtenerNombre())
                                    .append(" : ").append(String.format("%.2f m²", hijoDepto.calcularAreaTotal())).append("\n");
                        }
                    }
                }
            }
        }

        JTextArea areaTexto = new JTextArea(sb.toString());
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaTexto.setBackground(EstilosUI.FONDO_TARJETA);
        areaTexto.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        areaTexto.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scroll.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        dialogo.add(scroll, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.ACENTO_INMUEBLES);
        btnCerrar.addActionListener(e -> dialogo.dispose());

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setOpaque(false);
        panelSur.setBorder(new EmptyBorder(0, 16, 12, 16));
        panelSur.add(btnCerrar);
        dialogo.add(panelSur, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    private void ejecutarRegistroGasto() {
        String tipo = (String) comboTipoGasto.getSelectedItem();
        String montoStr = txtMontoGasto.getText().trim();
        String estStr = (String) comboEstrategiaGasto.getSelectedItem();

        double montoTotal;
        try {
            montoTotal = Validaciones.validarMontoGasto(montoStr);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtMontoGasto.requestFocusInWindow();
            return;
        }

        List<InmuebleDTO> departamentos = fachada.listarInmuebles();
        if (departamentos == null || departamentos.isEmpty()) {
            etiquetaMensajeEstado.setText("Error: No hay departamentos registrados para prorratear el gasto.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            ManejadorErroresUI.mostrarAdvertencia(this, "No hay departamentos registrados para prorratear el gasto.");
            return;
        }

        // Patrón Strategy
        boolean esPorAlicuota = estStr != null && estStr.contains("Alícuota");
        CalculadoraGastos calculadora = new CalculadoraGastos(esPorAlicuota ? new ProrrateoPorAlicuota() : new ProrrateoEquitativo());

        double factorTotal = esPorAlicuota
                ? departamentos.stream().mapToDouble(InmuebleDTO::getMetrosCuadrados).sum()
                : departamentos.size();

        if (factorTotal <= 0) factorTotal = 1.0;

        StringBuilder desglose = new StringBuilder();
        desglose.append("PRORRATEO Y DISTRIBUCIÓN DE GASTO COMÚN\n\n");
        desglose.append("Tipo de Gasto: ").append(tipo).append("\n");
        desglose.append("Monto Facturado: S/ ").append(String.format("%.2f", montoTotal)).append("\n");
        desglose.append("Criterio: ").append(esPorAlicuota ? "Por Alícuota (Área m²)" : "Equitativo (Partes Iguales)").append("\n");
        desglose.append("Total Inmuebles Evaluados: ").append(departamentos.size()).append("\n\n");
        desglose.append("CUOTAS ASIGNADAS A CADA DEPARTAMENTO:\n\n");

        for (InmuebleDTO depto : departamentos) {
            double factorUnidad = esPorAlicuota ? depto.getMetrosCuadrados() : 1.0;
            double cuotaDepto = calculadora.ejecutarCalculo(montoTotal, factorUnidad, factorTotal);
            desglose.append(String.format(" - Depto %s (%s - %s) [%.2f m²]: S/ %.2f\n",
                    depto.getNumero(), depto.getNombreTorre(), depto.getNombreEdificio(),
                    depto.getMetrosCuadrados(), cuotaDepto));
        }

        // Regla: mostrar diálogo con el prorrateo resultante y solicitar confirmación ANTES de persistir
        int opcionConfirmacion = JOptionPane.showConfirmDialog(
                this,
                desglose.toString() + "\n¿Desea confirmar y registrar este gasto común en el sistema?",
                "Prorrateo de Gasto Común",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcionConfirmacion != JOptionPane.YES_OPTION) {
            etiquetaMensajeEstado.setText("Registro de gasto común cancelado por el usuario.");
            etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
            return;
        }

        // Persistir GastoComun polimórfico con Factory Method
        btnRegistrarGasto.setEnabled(false);
        GastoComun nuevoGasto;
        String desc = "Gasto General de " + tipo + " - Prorrateo " + (esPorAlicuota ? "por Alícuota" : "Equitativo");
        if ("AGUA".equalsIgnoreCase(tipo)) {
            nuevoGasto = GastoComunFabrica.crearGastoAgua(null, montoTotal, LocalDate.now(), desc, null, 1500.0, 1400.0);
        } else if ("LUZ".equalsIgnoreCase(tipo)) {
            nuevoGasto = GastoComunFabrica.crearGastoLuz(null, montoTotal, LocalDate.now(), desc, null, montoTotal / 0.55);
        } else {
            nuevoGasto = GastoComunFabrica.crearGastoLimpieza(null, montoTotal, LocalDate.now(), desc, null, "Clean Corp Perú SAC");
        }

        SwingWorker<Void, Void> workerGasto = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.registrarGastoComun(nuevoGasto);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrarGasto.setEnabled(true);
                txtMontoGasto.setText("");
                etiquetaMensajeEstado.setText("Gasto común de " + tipo + " (S/ " + String.format("%.2f", montoTotal) + ") registrado y prorrateado con éxito.");
                etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                ManejadorErroresUI.mostrarExito(PanelInmuebles.this,
                        "El gasto común de " + tipo + " por S/ " + String.format("%.2f", montoTotal) + " ha sido persistido con éxito.",
                        "Gasto Común Registrado");
            }
        };
        workerGasto.execute();
    }

    private void mostrarMenuContextualFila(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaInmuebles.getRowCount()) return;
        Object numObj = tablaInmuebles.getValueAt(fila, 1);
        String depto = (numObj != null) ? numObj.toString() : "";

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_INMUEBLES);

        JMenuItem itemCuotas = EstilosUI.crearItemMenuOscuro("Ver cuotas de este departamento (Depto " + depto + ")", EstilosUI.ACENTO_INMUEBLES, EstilosUI.ACENTO_INMUEBLES);
        itemCuotas.setToolTipText("Abre directamente el estado de cuenta y cuotas del depto " + depto);
        itemCuotas.addActionListener(e -> {
            DialogoEstadoCuentaVecino dlg = new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto);
            dlg.setVisible(true);
        });
        popup.add(itemCuotas);

        JMenuItem itemResidentes = EstilosUI.crearItemMenuOscuro("Ver residentes de Depto " + depto, EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemResidentes.setToolTipText("Muestra el padrón de residentes asociados a este departamento");
        itemResidentes.addActionListener(e -> mostrarResidentesDepartamento(depto));
        popup.add(itemResidentes);

        JMenuItem itemComposite = EstilosUI.crearItemMenuOscuro("Ver en Estructura Jerárquica", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemComposite.setToolTipText("Visualiza el árbol estructural Condominio -> Edificio -> Torre -> Depto");
        itemComposite.addActionListener(e -> abrirDialogoJerarquiaComposite());
        popup.add(itemComposite);

        if (com.condominio.modelo.seguridad.SesionActual.esAdministrador()) {
            JMenuItem itemEditar = EstilosUI.crearItemMenuOscuro("Modificar datos del departamento (Depto " + depto + ")", EstilosUI.DORADO_PRINCIPAL, EstilosUI.DORADO_PRINCIPAL);
            itemEditar.setToolTipText("Actualiza metros cuadrados, piso y habitantes del departamento");
            itemEditar.addActionListener(e -> abrirDialogoEditarDepartamento(depto, fila));
            popup.add(itemEditar);

            JMenuItem itemBaja = EstilosUI.crearItemMenuOscuro("Dar de baja departamento (Depto " + depto + ")", EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
            itemBaja.setToolTipText("Elimina el departamento del inventario (valida ausencia de deudas o residentes)");
            itemBaja.addActionListener(e -> ejecutarBajaDepartamento(depto));
            popup.add(itemBaja);
        }

        popup.show(tablaInmuebles, x, y);
    }

    private void abrirDialogoEditarDepartamento(String depto, int fila) {
        Object pisoObj = tablaInmuebles.getValueAt(fila, 2);
        Object m2Obj = tablaInmuebles.getValueAt(fila, 3);
        String pisoActual = (pisoObj != null) ? pisoObj.toString() : "1";
        String m2Actual = (m2Obj != null) ? m2Obj.toString().replace(" m²", "").trim() : "75.0";

        JTextField txtPiso = EstilosUI.crearCampoPlaceholder("Piso", EstilosUI.ACENTO_INMUEBLES);
        txtPiso.setText(pisoActual);
        JTextField txtM2 = EstilosUI.crearCampoPlaceholder("Metros Cuadrados", EstilosUI.ACENTO_INMUEBLES);
        txtM2.setText(m2Actual);
        JTextField txtHab = EstilosUI.crearCampoPlaceholder("Habitantes", EstilosUI.ACENTO_INMUEBLES);
        txtHab.setText("3");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Piso:"));
        form.add(txtPiso);
        form.add(new JLabel("Metros Cuadrados:"));
        form.add(txtM2);
        form.add(new JLabel("Número de Habitantes:"));
        form.add(txtHab);

        int res = JOptionPane.showConfirmDialog(this, form, "Modificar Departamento " + depto + " (CRUD Update)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int p = Integer.parseInt(txtPiso.getText().trim());
                double m2 = Double.parseDouble(txtM2.getText().trim());
                int h = Integer.parseInt(txtHab.getText().trim());
                boolean ok = fachada.actualizarDepartamento(depto, p, m2, h);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Departamento " + depto + " actualizado exitosamente.", "Actualización Exitosa (CRUD Update)", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosTabla();
                } else {
                    ManejadorErroresUI.mostrarAdvertencia(this, "No se encontró el departamento a actualizar.");
                }
            } catch (Exception ex) {
                ManejadorErroresUI.mostrarError(this, "Error al actualizar departamento: " + ex.getMessage(), ex);
            }
        }
    }

    private void ejecutarBajaDepartamento(String depto) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de dar de baja y eliminar el Departamento " + depto + " del inventario?\nEsta acción no se puede deshacer.",
                "Confirmar Baja de Departamento (CRUD Delete)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = fachada.eliminarDepartamento(depto);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Departamento " + depto + " eliminado exitosamente.", "Baja Exitosa (CRUD Delete)", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosTabla();
                } else {
                    ManejadorErroresUI.mostrarAdvertencia(this, "No se encontró el departamento " + depto + ".");
                }
            } catch (Exception ex) {
                ManejadorErroresUI.mostrarError(this, "No se pudo dar de baja el departamento", ex);
            }
        }
    }

    private void mostrarResidentesDepartamento(String depto) {
        try {
            List<com.condominio.dto.ResidenteDTO> todos = fachada.listarResidentes();
            StringBuilder sb = new StringBuilder();
            int encontrados = 0;
            if (todos != null) {
                for (com.condominio.dto.ResidenteDTO r : todos) {
                    if (depto.equalsIgnoreCase(r.getNumeroDepartamento())) {
                        sb.append("- ").append(r.getNombres()).append(" ").append(r.getApellidos())
                          .append(" (").append(r.getTipoUsuario()).append(") | Tel: ").append(r.getTelefono())
                          .append(" | DNI: ").append(r.getDni()).append("\n");
                        encontrados++;
                    }
                }
            }
            if (encontrados == 0) {
                sb.append("No se registran residentes asignados al Departamento ").append(depto).append(".");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Directorio de Residentes - Depto " + depto, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al consultar residentes del departamento", ex);
        }
    }

    private void mostrarMenuDesborde(Component invocador, int x, int y) {
        JPopupMenu menu = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_INMUEBLES);

        JMenuItem itemComp = EstilosUI.crearItemMenuOscuro("Estructura Jerárquica del Inmueble", EstilosUI.ACENTO_INMUEBLES, EstilosUI.ACENTO_INMUEBLES);
        itemComp.setToolTipText("Visualiza el árbol estructural Condominio -> Edificio -> Torre -> Depto");
        itemComp.addActionListener(e -> abrirDialogoJerarquiaComposite());
        menu.add(itemComp);

        JMenuItem itemEdificios = EstilosUI.crearItemMenuOscuro("Edificios Totales (Métricas y Catálogo)", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemEdificios.setToolTipText("Abre el panel de métricas consolidadas y catálogo de edificios totales");
        itemEdificios.addActionListener(e -> new VentanaEdificiosTotales(SwingUtilities.getWindowAncestor(this), this.fachada).setVisible(true));
        menu.add(itemEdificios);

        JMenuItem itemRefrescar = EstilosUI.crearItemMenuOscuro("Refrescar Inventario", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemRefrescar.setToolTipText("Recarga el inventario de departamentos desde la base de datos");
        itemRefrescar.addActionListener(e -> {
            cargarCombosTorresYEdificios();
            cargarDatosTabla();
        });
        menu.add(itemRefrescar);

        EstilosUI.agregarSeparadorOscuro(menu);

        int fila = tablaInmuebles.getSelectedRow();
        String depto = (fila != -1 && tablaInmuebles.getValueAt(fila, 1) != null) ? tablaInmuebles.getValueAt(fila, 1).toString() : null;

        JMenuItem itemCuotas = EstilosUI.crearItemMenuOscuro(depto != null ? "Ver cuotas de Depto " + depto : "Ver cuotas de Depto seleccionado...", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemCuotas.addActionListener(e -> {
            if (depto != null) {
                new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto).setVisible(true);
            } else {
                ManejadorErroresUI.mostrarAdvertencia(this, "Seleccione un departamento en la tabla para consultar sus cuotas.");
            }
        });
        menu.add(itemCuotas);

        JMenuItem itemRes = EstilosUI.crearItemMenuOscuro(depto != null ? "Ver residentes de Depto " + depto : "Ver residentes de Depto seleccionado...", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_INMUEBLES);
        itemRes.addActionListener(e -> {
            if (depto != null) {
                mostrarResidentesDepartamento(depto);
            } else {
                ManejadorErroresUI.mostrarAdvertencia(this, "Seleccione un departamento en la tabla para ver sus residentes.");
            }
        });
        menu.add(itemRes);

        menu.show(invocador, x, y);
    }

    public void limpiarFormulario() {
        txtNumero.setText("");
        txtPiso.setText("");
        txtMetrosCuadrados.setText("");
        txtNumero.requestFocusInWindow();
    }
}
