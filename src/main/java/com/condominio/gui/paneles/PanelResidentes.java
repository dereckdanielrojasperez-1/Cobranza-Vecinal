package com.condominio.gui.paneles;

import com.condominio.dto.InmuebleDTO;
import com.condominio.dto.ResidenteDTO;
import com.condominio.gui.DialogoEstadoCuentaVecino;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroAlfabetico;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.Validaciones;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

// Panel interactivo para el modulo de Gestion de Residentes (Propietarios e Inquilinos).
@Slf4j
public class PanelResidentes extends JPanel {

    public record OpcionDepartamento(Long id, String etiqueta) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final CobranzaFachada fachada;
    private JTable tablaResidentes;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;
    private JLabel lblContadorRegistros;
    private TableRowSorter<DefaultTableModel> sorter;

    // Cache en memoria para autocompletado y preview
    private List<ResidenteDTO> listaResidentesCache = new ArrayList<>();

    // Componentes del Panel de Filtrado Inteligente
    private JTextField txtBuscarDni;
    private JTextField txtFiltroNombre;
    private JComboBox<String> comboFiltroTorre;
    private JButton btnLimpiarFiltros;
    private String letraFiltroActual = "TODOS";
    private final Map<String, JButton> botonesLetras = new HashMap<>();

    // Subventana / Popup flotante de autocompletado y ficha preview
    private JPopupMenu popupAutocompletado;
    private JWindow ventanaPreviewDni;
    private JLabel lblPreviewNombre;
    private JLabel lblPreviewDni;
    private JLabel lblPreviewTipo;
    private JLabel lblPreviewUbicacion;
    private JLabel lblPreviewContacto;

    // Componentes del Formulario de Registro / Edicion (CRUD)
    private JTextField txtDni;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JComboBox<String> comboTipo;
    private JComboBox<OpcionDepartamento> comboDepartamento;
    private JButton btnRegistrar;
    private JButton btnCancelarEdicion;
    private JButton btnRefrescar;
    private JButton btnDesborde;
    private JLabel etiquetaMensajeEstado;

    private boolean modoEdicion = false;
    private boolean autocompletandoSeleccion = false;
    private boolean listenersVentanaVinculados = false;

    public PanelResidentes(CobranzaFachada fachada) {
        this.fachada = fachada;
        inicializarComponentes();
        cargarDatosTabla();
        cargarComboDepartamentos();
        cargarComboTorresFiltro();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 10));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel panelBanner = construirBannerInformativo();
        add(panelBanner, BorderLayout.NORTH);

        JPanel panelCentro = construirPanelCentro();
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelInferior = construirFormularioAcciones();
        add(panelInferior, BorderLayout.SOUTH);

        enlazarEventosYListeners(panelBanner, panelCentro, panelInferior);
    }

    private JPanel construirBannerInformativo() {
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_RESIDENTES);
        boolean esConserje = SesionActual.esConserje();
        String titulo = esConserje
                ? "DIRECTORIO DE RESIDENTES Y CONTACTOS DE PORTERIA"
                : "MODULO DE GESTION Y ADMINISTRACION DE RESIDENTES";
        String subtitulo = esConserje
                ? "Directorio telefonico y datos de contacto de vecinos para emergencias y porteria (Solo Lectura)."
                : "Directorio de residentes, padron de propietarios, inquilinos y asignacion habitacional.";

        JLabel tituloBanner = new JLabel(titulo);
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.ACENTO_RESIDENTES);

        JLabel subtituloBanner = new JLabel(subtitulo);
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        return panelBanner;
    }

    private JPanel construirPanelCentro() {
        JPanel panelCentro = new JPanel(new BorderLayout(0, 8));
        panelCentro.setOpaque(false);

        JPanel panelFiltros = crearPanelFiltrosAvanzados();
        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        JPanel panelTablaContenedor = construirTablaDatos();
        panelCentro.add(panelTablaContenedor, BorderLayout.CENTER);
        return panelCentro;
    }

    private JPanel construirTablaDatos() {
        String[] columnas = {
                "ID", "DNI", "Nombres", "Apellidos", "Telefono", "Correo", "Tipo", "Torre", "Depto"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResidentes = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.ACENTO_RESIDENTES);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaResidentes.setRowSorter(sorter);

        aplicarRenderizadoresTabla();

        tablaResidentes.setPreferredScrollableViewportSize(new Dimension(800, 142));
        scrollTabla = new JScrollPane(tablaResidentes);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollTabla.setPreferredSize(new Dimension(800, 160));

        JPanel panelTablaContenedor = new JPanel(new BorderLayout(0, 4));
        panelTablaContenedor.setOpaque(false);
        panelTablaContenedor.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelContador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelContador.setOpaque(false);
        lblContadorRegistros = new JLabel("Mostrando 0 de 0 residentes registrados");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContadorRegistros.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelContador.add(lblContadorRegistros);
        panelTablaContenedor.add(panelContador, BorderLayout.SOUTH);
        return panelTablaContenedor;
    }

    private void aplicarRenderizadoresTabla() {
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer renderIzquierda = new DefaultTableCellRenderer();
        renderIzquierda.setHorizontalAlignment(SwingConstants.LEFT);

        tablaResidentes.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaResidentes.getColumnModel().getColumn(2).setCellRenderer(renderIzquierda);
        tablaResidentes.getColumnModel().getColumn(3).setCellRenderer(renderIzquierda);
        tablaResidentes.getColumnModel().getColumn(4).setCellRenderer(renderCentro);
        tablaResidentes.getColumnModel().getColumn(5).setCellRenderer(renderIzquierda);
        tablaResidentes.getColumnModel().getColumn(6).setCellRenderer(new com.condominio.gui.util.RenderTipoResidente());
        tablaResidentes.getColumnModel().getColumn(7).setCellRenderer(renderCentro);
        tablaResidentes.getColumnModel().getColumn(8).setCellRenderer(renderCentro);

        tablaResidentes.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (value != null && !isSelected) {
                    c.setForeground(EstilosUI.DORADO_PRINCIPAL);
                }
                if (value != null) {
                    int modelRow = table.convertRowIndexToModel(row);
                    Object nom = modeloTabla.getValueAt(modelRow, 2);
                    Object ape = modeloTabla.getValueAt(modelRow, 3);
                    Object tel = modeloTabla.getValueAt(modelRow, 4);
                    Object cor = modeloTabla.getValueAt(modelRow, 5);
                    Object tip = modeloTabla.getValueAt(modelRow, 6);
                    Object tor = modeloTabla.getValueAt(modelRow, 7);
                    Object dep = modeloTabla.getValueAt(modelRow, 8);
                    c.setToolTipText("<html><div style='background:#16202E; color:#DFE7F0; padding:6px; border:1px solid #B39DDB; border-radius:6px;'>" +
                            "<b style='color:#B39DDB; font-size:12px;'>" + nom + " " + ape + "</b><br>" +
                            "<span style='color:#8FA3B8;'>DNI: " + value + "  |  Tipo: " + tip + "</span><br>" +
                            "<span style='color:#58C99A;'>Ubicacion: " + tor + " - Depto " + dep + "</span><br>" +
                            "<span style='color:#DFE7F0;'>Contacto: Tel " + tel + "  |  " + cor + "</span></div></html>");
                }
                return c;
            }
        });
    }

    private JPanel construirFormularioAcciones() {
        JPanel panelInferior = new JPanel(new BorderLayout(8, 6));
        panelInferior.setOpaque(false);

        if (SesionActual.esConserje()) {
            construirBarraPorteria(panelInferior);
        } else {
            construirFormularioAdministracion(panelInferior);
        }
        return panelInferior;
    }

    private void construirBarraPorteria(JPanel panelInferior) {
        JPanel panelBarraPorteria = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_RESIDENTES, 14);
        panelBarraPorteria.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelBarraPorteria.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel lblInfoPorteria = new JLabel("Vista de Garita / Porteria: Filtre en el panel superior por Torre o DNI para consulta rapida.");
        lblInfoPorteria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoPorteria.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        btnRefrescar = new JButton("Refrescar Padrón");
        EstilosUI.estilizarBoton(btnRefrescar, EstilosUI.ACENTO_RESIDENTES);
        btnRefrescar.addActionListener(e -> cargarDatosTabla());
        btnRefrescar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cerrarAutocompletadoYPreview();
            }
        });

        panelBarraPorteria.add(lblInfoPorteria);
        panelBarraPorteria.add(btnRefrescar);
        panelInferior.add(panelBarraPorteria, BorderLayout.CENTER);

        etiquetaMensajeEstado = new JLabel("Modo Consulta de Portería: Directorio telefónico para emergencias, paquetería y visitas.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);
    }

    private void construirFormularioAdministracion(JPanel panelInferior) {
        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_RESIDENTES, 14);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(10, 14, 10, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("DNI:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Nombres:"), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Apellidos:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Teléfono:"), gbc);
        gbc.gridx = 4; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Correo Electrónico:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Tipo Residente:"), gbc);
        gbc.gridx = 6; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Departamento:"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        txtDni = EstilosUI.crearCampoPlaceholder("Ej: 8 dígitos", EstilosUI.ACENTO_RESIDENTES);
        FiltroNumerico.aplicar(txtDni, 8);
        txtDni.setPreferredSize(new Dimension(95, 32));
        txtDni.setToolTipText("Documento Nacional de Identidad (8 dígitos numéricos)");
        panelFormulario.add(txtDni, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtNombres = EstilosUI.crearCampoPlaceholder("Ej: Carlos Alberto", EstilosUI.ACENTO_RESIDENTES);
        FiltroAlfabetico.aplicar(txtNombres, 50);
        txtNombres.setPreferredSize(new Dimension(120, 32));
        txtNombres.setToolTipText("Nombres del residente (2 a 50 caracteres)");
        panelFormulario.add(txtNombres, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        txtApellidos = EstilosUI.crearCampoPlaceholder("Ej: Mendoza Torres", EstilosUI.ACENTO_RESIDENTES);
        FiltroAlfabetico.aplicar(txtApellidos, 50);
        txtApellidos.setPreferredSize(new Dimension(120, 32));
        txtApellidos.setToolTipText("Apellidos del residente (2 a 50 caracteres)");
        panelFormulario.add(txtApellidos, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        txtTelefono = EstilosUI.crearCampoPlaceholder("Ej: 987654321", EstilosUI.ACENTO_RESIDENTES);
        FiltroNumerico.aplicar(txtTelefono, 9);
        txtTelefono.setPreferredSize(new Dimension(95, 32));
        txtTelefono.setToolTipText("Número de teléfono (9 dígitos, inicia con 9, 6 o 7)");
        panelFormulario.add(txtTelefono, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        txtCorreo = EstilosUI.crearCampoPlaceholder("Ej: carlos@email.com", EstilosUI.ACENTO_RESIDENTES);
        txtCorreo.setPreferredSize(new Dimension(135, 32));
        txtCorreo.setToolTipText("Correo electrónico");
        panelFormulario.add(txtCorreo, gbc);

        gbc.gridx = 5; gbc.gridy = 1;
        comboTipo = new JComboBox<>(new String[]{"PROPIETARIO", "INQUILINO"});
        comboTipo.setEditable(false);
        EstilosUI.estilizarCombo(comboTipo, EstilosUI.ACENTO_RESIDENTES);
        EstilosUI.configurarComboPrototipoYTooltip(comboTipo, "PROPIETARIO");
        comboTipo.setPreferredSize(new Dimension(130, 32));
        panelFormulario.add(comboTipo, gbc);

        gbc.gridx = 6; gbc.gridy = 1;
        comboDepartamento = new JComboBox<>();
        comboDepartamento.setEditable(false);
        EstilosUI.estilizarCombo(comboDepartamento, EstilosUI.ACENTO_RESIDENTES);
        EstilosUI.configurarComboPrototipoYTooltip(comboDepartamento, new OpcionDepartamento(null, "Depto 502 (Torre Residencial B)"));
        comboDepartamento.setPreferredSize(new Dimension(175, 32));
        panelFormulario.add(comboDepartamento, gbc);

        gbc.gridx = 7; gbc.gridy = 1;
        btnRegistrar = new JButton("Registrar Residente");
        EstilosUI.estilizarBoton(btnRegistrar, EstilosUI.ACENTO_RESIDENTES);
        btnRegistrar.addActionListener(e -> ejecutarAccionFormulario());
        panelFormulario.add(btnRegistrar, gbc);

        gbc.gridx = 8; gbc.gridy = 1;
        btnCancelarEdicion = new JButton("Cancelar");
        EstilosUI.estilizarBotonSecundario(btnCancelarEdicion, EstilosUI.TEXTO_SECUNDARIO);
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.addActionListener(e -> salirModoEdicion());
        panelFormulario.add(btnCancelarEdicion, gbc);

        gbc.gridx = 9; gbc.gridy = 1;
        btnDesborde = new JButton("...");
        EstilosUI.estilizarBotonSecundario(btnDesborde, EstilosUI.ACENTO_RESIDENTES);
        btnDesborde.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDesborde.setPreferredSize(new Dimension(36, 32));
        btnDesborde.setToolTipText("Más opciones: Exportar CSV, Refrescar Padrón");
        btnDesborde.addActionListener(e -> mostrarMenuDesborde(btnDesborde, 0, btnDesborde.getHeight()));
        panelFormulario.add(btnDesborde, gbc);

        btnRefrescar = new JButton("Refrescar");

        FocusListener flCerrar = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cerrarAutocompletadoYPreview();
            }
        };
        txtDni.addFocusListener(flCerrar);
        txtNombres.addFocusListener(flCerrar);
        txtApellidos.addFocusListener(flCerrar);
        txtTelefono.addFocusListener(flCerrar);
        txtCorreo.addFocusListener(flCerrar);
        comboTipo.addFocusListener(flCerrar);
        comboDepartamento.addFocusListener(flCerrar);
        btnRegistrar.addFocusListener(flCerrar);
        btnCancelarEdicion.addFocusListener(flCerrar);
        btnDesborde.addFocusListener(flCerrar);

        panelInferior.add(panelFormulario, BorderLayout.CENTER);

        etiquetaMensajeEstado = new JLabel("Listo para administrar el padrón de residentes.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);
    }

    private void enlazarEventosYListeners(JPanel panelBanner, JPanel panelCentro, JPanel panelInferior) {
        addHierarchyListener(e -> {
            if (!isShowing()) {
                cerrarAutocompletadoYPreview();
            } else {
                vincularListenersVentanaPrincipal();
            }
        });

        tablaResidentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaResidentes.getSelectedRow();
                if (fila != -1) {
                    Object dni = tablaResidentes.getValueAt(fila, 1);
                    Object nombres = tablaResidentes.getValueAt(fila, 2);
                    Object apellidos = tablaResidentes.getValueAt(fila, 3);
                    Object tipo = tablaResidentes.getValueAt(fila, 6);
                    Object torre = tablaResidentes.getValueAt(fila, 7);
                    Object depto = tablaResidentes.getValueAt(fila, 8);
                    etiquetaMensajeEstado.setText("Residente seleccionado: " + nombres + " " + apellidos + " (" + tipo + ") - " + torre + " Depto: " + depto + " [DNI " + dni + "]");
                    etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);
                }
            }
        });

        tablaResidentes.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cerrarAutocompletadoYPreview();
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            private void procesarPopup(MouseEvent e) {
                int fila = tablaResidentes.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaResidentes.getRowCount()) {
                    tablaResidentes.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualFila(e.getX(), e.getY(), fila);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    int fila = tablaResidentes.rowAtPoint(e.getPoint());
                    if (fila >= 0 && fila < tablaResidentes.getRowCount()) {
                        tablaResidentes.setRowSelectionInterval(fila, fila);
                        Object deptoObj = tablaResidentes.getValueAt(fila, 8);
                        if (deptoObj != null && !deptoObj.toString().isBlank() && !deptoObj.toString().contains("Sin")) {
                            DialogoEstadoCuentaVecino dlg = new DialogoEstadoCuentaVecino(
                                    SwingUtilities.getWindowAncestor(PanelResidentes.this),
                                    fachada,
                                    deptoObj.toString().trim()
                            );
                            dlg.setVisible(true);
                        }
                    }
                }
            }
        });

        tablaResidentes.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cerrarAutocompletadoYPreview();
            }
        });

        MouseAdapter maCerrar = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cerrarAutocompletadoYPreview();
            }
        };
        panelBanner.addMouseListener(maCerrar);
        panelCentro.addMouseListener(maCerrar);
        panelInferior.addMouseListener(maCerrar);
    }

    // SECCIÓN DE FILTRADO AVANZADO (DNI PREVIEW, A-Z, TORRES)

    private JPanel crearPanelFiltrosAvanzados() {
        GlassPanel panelFiltros = new GlassPanel(16, false, EstilosUI.ACENTO_RESIDENTES);
        panelFiltros.setLayout(new BorderLayout(8, 8));
        panelFiltros.setBorder(new EmptyBorder(10, 14, 10, 14));

        // 1. Fila Superior de Filtros: DNI con Autocompletado + Nombre + Torre + Limpiar
        JPanel filaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        filaSuperior.setOpaque(false);

        // A) Buscador reactivo por DNI
        JLabel lblDni = new JLabel("Buscar DNI:");
        lblDni.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDni.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        txtBuscarDni = EstilosUI.crearCampoPlaceholder("Ej: 84736291...", EstilosUI.ACENTO_RESIDENTES);
        txtBuscarDni.setPreferredSize(new Dimension(140, 28));
        txtBuscarDni.setToolTipText("Escriba dígitos del DNI para autocompletar con preview en vivo");

        txtBuscarDni.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { procesarAutocompletadoDni(); }
            @Override public void removeUpdate(DocumentEvent e) { procesarAutocompletadoDni(); }
            @Override public void changedUpdate(DocumentEvent e) { procesarAutocompletadoDni(); }
        });

        txtBuscarDni.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cerrarAutocompletadoYPreview();
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        aplicarFiltrosCombinados();
                    }
                }
            }
        });

        txtBuscarDni.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (ventanaPreviewDni != null && ventanaPreviewDni.isVisible()) {
                    ventanaPreviewDni.setVisible(false);
                }
                Timer t = new Timer(100, ev -> {
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusOwner != txtBuscarDni && (popupAutocompletado == null || !popupAutocompletado.isVisible())) {
                        cerrarAutocompletadoYPreview();
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        });

        // B) Buscador por Nombre / Apellido
        JLabel lblNombre = new JLabel("Nombre / Apellido:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNombre.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        txtFiltroNombre = EstilosUI.crearCampoPlaceholder("Ej: Ana, Carlos, Quispe...", EstilosUI.ACENTO_RESIDENTES);
        txtFiltroNombre.setPreferredSize(new Dimension(170, 28));
        txtFiltroNombre.setToolTipText("Búsqueda instantánea por nombres o apellidos");

        txtFiltroNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltrosCombinados(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltrosCombinados(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltrosCombinados(); }
        });

        txtFiltroNombre.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cerrarAutocompletadoYPreview();
            }
        });

        // C) Filtro por Torre / Edificio
        JLabel lblTorre = new JLabel("Torre / Edificio:");
        lblTorre.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTorre.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        comboFiltroTorre = new JComboBox<>();
        comboFiltroTorre.setEditable(false);
        EstilosUI.estilizarCombo(comboFiltroTorre, EstilosUI.ACENTO_RESIDENTES);
        comboFiltroTorre.setPreferredSize(new Dimension(160, 28));
        comboFiltroTorre.setToolTipText("Filtra el padrón por Torre o Complejo residencial");
        comboFiltroTorre.addItem("Todas las Torres");
        comboFiltroTorre.addItem("Torre A");
        comboFiltroTorre.addItem("Torre B");
        comboFiltroTorre.addItem("Torre Central");
        comboFiltroTorre.addItem("Torre Principal");
        comboFiltroTorre.addItem("Edificio Los Sauces");

        comboFiltroTorre.addActionListener(e -> {
            cerrarAutocompletadoYPreview();
            aplicarFiltrosCombinados();
        });

        comboFiltroTorre.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cerrarAutocompletadoYPreview();
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        comboFiltroTorre.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cerrarAutocompletadoYPreview();
            }
        });

        // D) Botón Limpiar Filtros
        btnLimpiarFiltros = new JButton("Limpiar Filtros");
        EstilosUI.estilizarBotonSecundario(btnLimpiarFiltros, EstilosUI.TEXTO_SECUNDARIO);
        btnLimpiarFiltros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLimpiarFiltros.setPreferredSize(new Dimension(110, 28));
        btnLimpiarFiltros.setToolTipText("Restablece todos los criterios de búsqueda al padrón general");
        btnLimpiarFiltros.addActionListener(e -> resetearTodosLosFiltros());

        filaSuperior.add(lblDni);
        filaSuperior.add(txtBuscarDni);
        filaSuperior.add(lblNombre);
        filaSuperior.add(txtFiltroNombre);
        filaSuperior.add(lblTorre);
        filaSuperior.add(comboFiltroTorre);
        filaSuperior.add(btnLimpiarFiltros);

        // 2. Fila Inferior: Abecedario Rápido A-Z
        JPanel filaInferior = crearBarraAbecedario();

        panelFiltros.add(filaSuperior, BorderLayout.NORTH);
        panelFiltros.add(filaInferior, BorderLayout.SOUTH);

        return panelFiltros;
    }

    private JPanel crearBarraAbecedario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        panel.setOpaque(false);

        JLabel lblTit = new JLabel("Filtro Alfabético A-Z:");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTit.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panel.add(lblTit);

        String[] letras = {
                "TODOS", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        };

        for (String l : letras) {
            JButton btn = new JButton(l);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(2, 5, 2, 5));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension("TODOS".equals(l) ? 56 : 26, 24));

            actualizarEstiloBotonLetra(btn, "TODOS".equals(l));

            btn.addActionListener(e -> {
                cerrarAutocompletadoYPreview();
                letraFiltroActual = l;
                for (Map.Entry<String, JButton> entry : botonesLetras.entrySet()) {
                    actualizarEstiloBotonLetra(entry.getValue(), entry.getKey().equals(l));
                }
                aplicarFiltrosCombinados();
            });

            botonesLetras.put(l, btn);
            panel.add(btn);
        }

        return panel;
    }

    private void actualizarEstiloBotonLetra(JButton btn, boolean activo) {
        if (activo) {
            btn.setBackground(EstilosUI.ACENTO_RESIDENTES);
            btn.setForeground(new Color(0x0B, 0x13, 0x1E));
            btn.setBorder(BorderFactory.createLineBorder(EstilosUI.ACENTO_RESIDENTES, 1));
        } else {
            btn.setBackground(new Color(0x16, 0x20, 0x2E));
            btn.setForeground(EstilosUI.TEXTO_SECUNDARIO);
            btn.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1));
        }
    }

    private void resetearTodosLosFiltros() {
        if (txtBuscarDni != null) txtBuscarDni.setText("");
        if (txtFiltroNombre != null) txtFiltroNombre.setText("");
        if (comboFiltroTorre != null && comboFiltroTorre.getItemCount() > 0) comboFiltroTorre.setSelectedIndex(0);

        letraFiltroActual = "TODOS";
        for (Map.Entry<String, JButton> entry : botonesLetras.entrySet()) {
            actualizarEstiloBotonLetra(entry.getValue(), "TODOS".equals(entry.getKey()));
        }

        cerrarAutocompletadoYPreview();
        aplicarFiltrosCombinados();
    }

    // Aplica simultáneamente todos los criterios de filtro (DNI, Texto, Torre y Letra A-Z).
    private void aplicarFiltrosCombinados() {
        if (sorter == null || modeloTabla == null) return;

        List<RowFilter<DefaultTableModel, Object>> filtros = new ArrayList<>();

        // 1. Filtro por DNI (Columna 1)
        String dniText = (txtBuscarDni != null) ? txtBuscarDni.getText().trim() : "";
        if (!dniText.isEmpty() && !dniText.startsWith("Ej:")) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(dniText), 1));
        }

        // 2. Filtro por Nombre / Apellido (Columnas 2 y 3)
        String nombreText = (txtFiltroNombre != null) ? txtFiltroNombre.getText().trim() : "";
        if (!nombreText.isEmpty() && !nombreText.startsWith("Ej:")) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(nombreText), 2, 3));
        }

        // 3. Filtro Alfabético A-Z (Nombres o Apellidos que inicien con la letra seleccionada)
        if (letraFiltroActual != null && !letraFiltroActual.equals("TODOS")) {
            filtros.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(letraFiltroActual) + "|(?i)\\b" + Pattern.quote(letraFiltroActual), 2, 3));
        }

        // 4. Filtro por Torre (Columna 7)
        if (comboFiltroTorre != null && comboFiltroTorre.getSelectedIndex() > 0) {
            String torreSel = (String) comboFiltroTorre.getSelectedItem();
            if (torreSel != null && !torreSel.startsWith("Todas")) {
                filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(torreSel.trim()), 7));
            }
        }

        if (filtros.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filtros));
        }

        int visibles = tablaResidentes.getRowCount();
        int total = modeloTabla.getRowCount();
        StringBuilder detalle = new StringBuilder();
        if (letraFiltroActual != null && !letraFiltroActual.equals("TODOS")) {
            detalle.append(" [Letra: ").append(letraFiltroActual).append("]");
        }
        if (comboFiltroTorre != null && comboFiltroTorre.getSelectedIndex() > 0) {
            detalle.append(" [").append(comboFiltroTorre.getSelectedItem()).append("]");
        }
        lblContadorRegistros.setText("Mostrando " + visibles + " de " + total + " residentes registrados" + detalle);
    }

    // SISTEMA DE AUTOCOMPLETADO Y SUBVENTANA PREVIEW DE DNI

    private void vincularListenersVentanaPrincipal() {
        if (listenersVentanaVinculados) return;
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            parent.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    cerrarAutocompletadoYPreview();
                }
            });
            parent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    cerrarAutocompletadoYPreview();
                }
                @Override
                public void componentResized(ComponentEvent e) {
                    cerrarAutocompletadoYPreview();
                }
            });
            listenersVentanaVinculados = true;
        }
    }

    private void inicializarVentanaPreview() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (ventanaPreviewDni != null && ventanaPreviewDni.getOwner() != parent) {
            ventanaPreviewDni.dispose();
            ventanaPreviewDni = null;
        }
        if (ventanaPreviewDni != null) {
            return;
        }

        ventanaPreviewDni = new JWindow(parent);
        ventanaPreviewDni.setBackground(new Color(0x16, 0x20, 0x2E));
        ventanaPreviewDni.setFocusableWindowState(false);

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(true);
        card.setBackground(new Color(0x16, 0x20, 0x2E));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.ACENTO_RESIDENTES, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setPreferredSize(new Dimension(320, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cerrarAutocompletadoYPreview();
            }
        });

        JLabel lblTit = new JLabel("FICHA TECNICA DE RESIDENTE (VISTA PREVIA)");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTit.setForeground(EstilosUI.ACENTO_RESIDENTES);

        lblPreviewNombre = new JLabel("Nombre Completo");
        lblPreviewNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPreviewNombre.setForeground(EstilosUI.TEXTO_CLARO);

        lblPreviewDni = new JLabel("DNI: 00000000 | Tipo: PROPIETARIO");
        lblPreviewDni.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreviewDni.setForeground(EstilosUI.DORADO_PRINCIPAL);

        lblPreviewUbicacion = new JLabel("Ubicacion: Torre A - Depto 101");
        lblPreviewUbicacion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPreviewUbicacion.setForeground(EstilosUI.VERDE_EXITO);

        lblPreviewContacto = new JLabel("Tel: 987654321 | email@condominio.pe");
        lblPreviewContacto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreviewContacto.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        JPanel panelCuerpo = new JPanel(new GridLayout(4, 1, 0, 2));
        panelCuerpo.setOpaque(false);
        panelCuerpo.add(lblPreviewNombre);
        panelCuerpo.add(lblPreviewDni);
        panelCuerpo.add(lblPreviewUbicacion);
        panelCuerpo.add(lblPreviewContacto);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(panelCuerpo, BorderLayout.CENTER);

        ventanaPreviewDni.getContentPane().add(card);
        ventanaPreviewDni.pack();
    }

    private void procesarAutocompletadoDni() {
        if (autocompletandoSeleccion) {
            return;
        }

        String texto = (txtBuscarDni != null) ? txtBuscarDni.getText().trim() : "";
        if (texto.isEmpty() || texto.startsWith("Ej:")) {
            cerrarAutocompletadoYPreview();
            aplicarFiltrosCombinados();
            return;
        }

        List<ResidenteDTO> coincidencias = new ArrayList<>();
        for (ResidenteDTO r : listaResidentesCache) {
            if (r.getDni() != null && r.getDni().contains(texto)) {
                coincidencias.add(r);
            }
        }

        if (coincidencias.isEmpty() || !txtBuscarDni.isShowing()) {
            cerrarAutocompletadoYPreview();
            aplicarFiltrosCombinados();
            return;
        }

        if (popupAutocompletado != null && popupAutocompletado.isVisible()) {
            popupAutocompletado.setVisible(false);
        }

        popupAutocompletado = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x16, 0x20, 0x2E));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        popupAutocompletado.setOpaque(true);
        popupAutocompletado.setBackground(new Color(0x16, 0x20, 0x2E));
        popupAutocompletado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB3, 0x9D, 0xDB), 1),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));

        // Listener de ciclo de vida del popup: cuando se oculta o cancela, se cierra la ventana preview
        popupAutocompletado.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (ventanaPreviewDni != null && ventanaPreviewDni.isVisible()) {
                    ventanaPreviewDni.setVisible(false);
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                if (ventanaPreviewDni != null && ventanaPreviewDni.isVisible()) {
                    ventanaPreviewDni.setVisible(false);
                }
            }
        });

        int limite = Math.min(coincidencias.size(), 6);
        for (int i = 0; i < limite; i++) {
            ResidenteDTO r = coincidencias.get(i);

            JMenuItem item = new JMenuItem();
            item.setUI(new BasicMenuItemUI() {
                @Override
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color defaultBg) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    ButtonModel model = menuItem.getModel();
                    if (model.isArmed() || model.isRollover() || model.isPressed()) {
                        g2.setColor(new Color(0x2A, 0x3A, 0x4E));
                        g2.fillRoundRect(2, 1, menuItem.getWidth() - 4, menuItem.getHeight() - 2, 4, 4);
                        g2.setColor(new Color(0xB3, 0x9D, 0xDB));
                        g2.drawRoundRect(2, 1, menuItem.getWidth() - 4, menuItem.getHeight() - 2, 4, 4);
                    } else {
                        g2.setColor(new Color(0x16, 0x20, 0x2E));
                        g2.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                    }
                    g2.dispose();
                }
            });
            item.setOpaque(false);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.setPreferredSize(new Dimension(360, 32));
            item.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            String torreTxt = (r.getNombreTorre() != null && !r.getNombreTorre().isBlank()) ? r.getNombreTorre() : "Torre A";
            String deptoTxt = (r.getNumeroDepartamento() != null && !r.getNumeroDepartamento().isBlank()) ? ("Depto " + r.getNumeroDepartamento()) : "Sin Depto";

            String itemHtml = "<html><body style='font-family:Segoe UI; padding:2px;'>"
                    + "<b style='color:#E8A33D; font-size:12px;'>DNI " + r.getDni() + "</b>"
                    + "&nbsp;&nbsp;<span style='color:#FFFFFF; font-weight:bold; font-size:11px;'>" + r.getNombreCompleto() + "</span>"
                    + "&nbsp;&nbsp;<span style='color:#58C99A; font-size:11px;'>(" + torreTxt + " - " + deptoTxt + ")</span>"
                    + "</body></html>";
            item.setText(itemHtml);

            // Al pasar el cursor por el item se actualiza y muestra la subventana preview flotante
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    mostrarPreviewDni(r, item);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (ventanaPreviewDni != null && ventanaPreviewDni.isVisible()) {
                        ventanaPreviewDni.setVisible(false);
                    }
                }
            });

            // Al hacer clic se selecciona el DNI y se aplica el filtro
            item.addActionListener(e -> {
                autocompletandoSeleccion = true;
                try {
                    txtBuscarDni.setText(r.getDni());
                } finally {
                    autocompletandoSeleccion = false;
                }
                cerrarAutocompletadoYPreview();
                aplicarFiltrosCombinados();
                seleccionarFilaPorDni(r.getDni());
            });

            popupAutocompletado.add(item);
        }

        popupAutocompletado.show(txtBuscarDni, 0, txtBuscarDni.getHeight() + 2);
        aplicarFiltrosCombinados();
    }

    private void mostrarPreviewDni(ResidenteDTO r, Component invocador) {
        if (ventanaPreviewDni == null) inicializarVentanaPreview();
        if (r == null || invocador == null || !invocador.isShowing()) return;

        lblPreviewNombre.setText(r.getNombreCompleto());
        lblPreviewDni.setText("DNI: " + r.getDni() + "  |  " + r.getTipoUsuario());
        String torre = (r.getNombreTorre() != null) ? r.getNombreTorre() : "Torre A";
        String depto = (r.getNumeroDepartamento() != null) ? r.getNumeroDepartamento() : "Sin asignar";
        lblPreviewUbicacion.setText("Ubicacion: " + torre + " - Depto " + depto);
        lblPreviewContacto.setText("Tel: " + (r.getTelefono() != null ? r.getTelefono() : "-") + " | " +
                (r.getCorreoElectronico() != null ? r.getCorreoElectronico() : "-"));

        try {
            if (!invocador.isShowing()) return;
            Point pt = invocador.getLocationOnScreen();
            int posX = pt.x + invocador.getWidth() + 8;
            int posY = pt.y;

            // Ajuste en pantalla para evitar desbordes fuera de los limites de resolucion
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (posX + ventanaPreviewDni.getWidth() > screenSize.width) {
                posX = pt.x - ventanaPreviewDni.getWidth() - 8;
            }
            if (posX < 0) {
                posX = 10;
            }
            if (posY + ventanaPreviewDni.getHeight() > screenSize.height) {
                posY = screenSize.height - ventanaPreviewDni.getHeight() - 10;
            }

            ventanaPreviewDni.setLocation(posX, Math.max(posY, 10));
            ventanaPreviewDni.setVisible(true);
        } catch (IllegalComponentStateException ex) {
            // Manejo preventivo si el componente cambió de estado visual concurrentemente
        }
    }

    private void cerrarAutocompletadoYPreview() {
        if (popupAutocompletado != null && popupAutocompletado.isVisible()) {
            popupAutocompletado.setVisible(false);
        }
        if (ventanaPreviewDni != null && ventanaPreviewDni.isVisible()) {
            ventanaPreviewDni.setVisible(false);
        }
    }

    private void seleccionarFilaPorDni(String dni) {
        if (dni == null || tablaResidentes == null) return;
        for (int i = 0; i < tablaResidentes.getRowCount(); i++) {
            Object val = tablaResidentes.getValueAt(i, 1);
            if (val != null && dni.equalsIgnoreCase(val.toString().trim())) {
                tablaResidentes.setRowSelectionInterval(i, i);
                tablaResidentes.scrollRectToVisible(tablaResidentes.getCellRect(i, 0, true));
                break;
            }
        }
    }

    // SINCRONIZACIÓN Y CARGA DE DATOS DESDE LA PERSISTENCIA

    public void cargarDatosTabla() {
        if (fachada == null) return;

        if (etiquetaMensajeEstado != null) {
            etiquetaMensajeEstado.setText("Sincronizando residentes con la base de datos...");
            etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);
        }

        SwingWorker<List<ResidenteDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ResidenteDTO> doInBackground() {
                return fachada.listarResidentes();
            }

            @Override
            protected void done() {
                try {
                    poblarFilasTablaResidentes(get());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    ManejadorErroresUI.mostrarError(PanelResidentes.this, "Operación interrumpida durante la sincronización", ie);
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelResidentes.this, "Error al sincronizar residentes desde la persistencia", causa);
                }
            }
        };
        worker.execute();
    }

    private void poblarFilasTablaResidentes(List<ResidenteDTO> residentes) {
        listaResidentesCache = (residentes != null) ? residentes : Collections.emptyList();
        modeloTabla.setRowCount(0);
        int total = listaResidentesCache.size();

        for (ResidenteDTO r : listaResidentesCache) {
            modeloTabla.addRow(new Object[]{
                    r.getId(),
                    r.getDni(),
                    r.getNombres(),
                    r.getApellidos(),
                    r.getTelefono() != null ? r.getTelefono() : "-",
                    r.getCorreoElectronico() != null ? r.getCorreoElectronico() : "-",
                    r.getTipoUsuario(),
                    r.getNombreTorre() != null ? r.getNombreTorre() : "Torre A",
                    r.getNumeroDepartamento() != null ? r.getNumeroDepartamento() : "Sin asignar"
            });
        }

        if (scrollTabla != null) {
            scrollTabla.getVerticalScrollBar().setValue(0);
        }
        if (btnRefrescar != null) {
            btnRefrescar.setEnabled(true);
        }
        lblContadorRegistros.setText("Mostrando " + total + " de " + total + " residentes registrados");
        if (etiquetaMensajeEstado != null) {
            etiquetaMensajeEstado.setText("Módulo sincronizado: " + total + " registros.");
            etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
        }
        aplicarFiltrosCombinados();
    }

    public void cargarComboDepartamentos() {
        if (fachada == null || comboDepartamento == null) return;

        SwingWorker<List<InmuebleDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InmuebleDTO> doInBackground() {
                return fachada.listarInmuebles();
            }

            @Override
            protected void done() {
                try {
                    poblarOpcionesDepartamentos(get());
                } catch (Exception ex) {
                    log.warn("No se pudo cargar la lista de departamentos: {}", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void poblarOpcionesDepartamentos(List<InmuebleDTO> inmuebles) {
        comboDepartamento.removeAllItems();
        comboDepartamento.addItem(new OpcionDepartamento(null, "-- Sin asignar --"));
        if (inmuebles == null) return;
        for (InmuebleDTO inm : inmuebles) {
            String etiqueta = "Depto " + inm.getNumero() + " (" + inm.getNombreTorre() + ")";
            comboDepartamento.addItem(new OpcionDepartamento(inm.getId(), etiqueta));
        }
    }

    public void cargarComboTorresFiltro() {
        if (fachada == null || comboFiltroTorre == null) return;

        SwingWorker<List<com.condominio.modelo.inmuebles.Torre>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<com.condominio.modelo.inmuebles.Torre> doInBackground() {
                return fachada.listarTorres();
            }

            @Override
            protected void done() {
                try {
                    poblarOpcionesTorres(get());
                } catch (Exception ex) {
                    log.warn("No se pudo cargar torres dinámicas para filtro: {}", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void poblarOpcionesTorres(List<com.condominio.modelo.inmuebles.Torre> torres) {
        if (torres == null || torres.isEmpty()) return;
        Object prevSel = comboFiltroTorre.getSelectedItem();
        comboFiltroTorre.removeAllItems();
        comboFiltroTorre.addItem("Todas las Torres");
        for (com.condominio.modelo.inmuebles.Torre t : torres) {
            if (t.getNombre() != null && !t.getNombre().isBlank()) {
                comboFiltroTorre.addItem(t.getNombre().trim());
            }
        }
        if (prevSel != null) {
            comboFiltroTorre.setSelectedItem(prevSel);
        }
    }

    // GESTIÓN FORMULARIO CRUD: REGISTRO Y ACTUALIZACIÓN EN VIVO

    private void ejecutarAccionFormulario() {
        if (modoEdicion) {
            ejecutarActualizacionResidente();
        } else {
            ejecutarRegistroResidente();
        }
    }

    private void ejecutarRegistroResidente() {
        String dni = txtDni.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();
        OpcionDepartamento deptoSeleccionado = (OpcionDepartamento) comboDepartamento.getSelectedItem();

        // 1. Validación de DNI
        try {
            Validaciones.validarDni(dni);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtDni.requestFocusInWindow();
            return;
        }

        // 2. Unicidad de DNI
        if (fachada != null && fachada.existeResidentePorDni(dni)) {
            etiquetaMensajeEstado.setText("Error: Ya existe un residente registrado con el DNI: " + dni);
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtDni.requestFocusInWindow();
            return;
        }

        // 3. Validación de Nombres y Apellidos
        try {
            Validaciones.validarNombresApellidos(nombres, "Nombres");
            Validaciones.validarNombresApellidos(apellidos, "Apellidos");
            Validaciones.validarTelefono(telefono);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            return;
        }

        // 4. Correo
        String correoNormalizado;
        try {
            correoNormalizado = Validaciones.validarYNormalizarCorreo(correo);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtCorreo.requestFocusInWindow();
            return;
        }

        // 5. Regla departamento
        Long deptoId = (deptoSeleccionado != null) ? deptoSeleccionado.id() : null;
        if ("PROPIETARIO".equalsIgnoreCase(tipo)) {
            if (deptoId == null) {
                etiquetaMensajeEstado.setText("Error: Para el tipo PROPIETARIO, la asignación de un departamento es obligatoria.");
                etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                comboDepartamento.requestFocusInWindow();
                return;
            }
            if (fachada != null && fachada.existePropietarioEnDepartamento(deptoId)) {
                etiquetaMensajeEstado.setText("Error: El departamento seleccionado ya cuenta con un propietario registrado.");
                etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                comboDepartamento.requestFocusInWindow();
                return;
            }
        }

        btnRegistrar.setEnabled(false);
        etiquetaMensajeEstado.setText("Registrando residente en el padrón...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        ResidenteDTO dto = ResidenteDTO.builder()
                .dni(dni)
                .nombres(nombres)
                .apellidos(apellidos)
                .telefono(telefono)
                .correoElectronico(correoNormalizado)
                .tipoUsuario(tipo)
                .departamentoId(deptoId)
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.registrarResidente(dto);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    get();
                    String mensajeExito = "Residente " + nombres + " " + apellidos + " (" + tipo + ") registrado exitosamente.";
                    ManejadorErroresUI.mostrarExito(PanelResidentes.this, mensajeExito, "Registro Exitoso");
                    limpiarFormulario();
                    cargarDatosTabla();
                    cargarComboDepartamentos();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelResidentes.this, "No se pudo registrar al residente", causa);
                }
            }
        };
        worker.execute();
    }

    private void ejecutarActualizacionResidente() {
        String dni = txtDni.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();
        OpcionDepartamento deptoSeleccionado = (OpcionDepartamento) comboDepartamento.getSelectedItem();

        try {
            Validaciones.validarNombresApellidos(nombres, "Nombres");
            Validaciones.validarNombresApellidos(apellidos, "Apellidos");
            Validaciones.validarTelefono(telefono);
            correo = Validaciones.validarYNormalizarCorreo(correo);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            return;
        }

        Long deptoId = (deptoSeleccionado != null) ? deptoSeleccionado.id() : null;

        ResidenteDTO dto = ResidenteDTO.builder()
                .dni(dni)
                .nombres(nombres)
                .apellidos(apellidos)
                .telefono(telefono)
                .correoElectronico(correo)
                .tipoUsuario(tipo)
                .departamentoId(deptoId)
                .build();

        btnRegistrar.setEnabled(false);
        etiquetaMensajeEstado.setText("Actualizando datos del residente...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.actualizarResidente(dto);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    get();
                    ManejadorErroresUI.mostrarExito(PanelResidentes.this,
                            "Los datos del residente con DNI " + dni + " fueron actualizados con éxito.",
                            "Actualización Exitosa (CRUD)");
                    salirModoEdicion();
                    cargarDatosTabla();
                    cargarComboDepartamentos();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelResidentes.this, "No se pudo actualizar el residente", causa);
                }
            }
        };
        worker.execute();
    }

    private void activarModoEdicion(Object dniObj, Object nomObj, Object apeObj, Object telObj, Object corObj, Object tipObj, Object depObj) {
        if (!SesionActual.esAdministrador()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Permiso denegado: Solo el Administrador puede editar datos de residentes.");
            return;
        }

        modoEdicion = true;
        if (txtDni != null && dniObj != null) {
            txtDni.setText(dniObj.toString());
            txtDni.setEditable(false);
        }
        if (txtNombres != null && nomObj != null) txtNombres.setText(nomObj.toString());
        if (txtApellidos != null && apeObj != null) txtApellidos.setText(apeObj.toString());
        if (txtTelefono != null && telObj != null) txtTelefono.setText(telObj.toString());
        if (txtCorreo != null && corObj != null) txtCorreo.setText(corObj.toString());
        if (comboTipo != null && tipObj != null) comboTipo.setSelectedItem(tipObj.toString());

        String deptoStr = (depObj != null) ? depObj.toString().trim() : "";
        if (comboDepartamento != null && !deptoStr.isEmpty()) {
            for (int i = 0; i < comboDepartamento.getItemCount(); i++) {
                OpcionDepartamento op = comboDepartamento.getItemAt(i);
                if (op != null && op.etiqueta() != null && op.etiqueta().contains(deptoStr)) {
                    comboDepartamento.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (btnRegistrar != null) btnRegistrar.setText("Actualizar Residente");
        if (btnCancelarEdicion != null) btnCancelarEdicion.setVisible(true);

        etiquetaMensajeEstado.setText("Modo Edición: Modifique los datos y pulse 'Actualizar Residente'.");
        etiquetaMensajeEstado.setForeground(EstilosUI.DORADO_PRINCIPAL);
        if (txtTelefono != null) txtTelefono.requestFocusInWindow();
    }

    private void salirModoEdicion() {
        modoEdicion = false;
        limpiarFormulario();
        if (txtDni != null) txtDni.setEditable(true);
        if (btnRegistrar != null) btnRegistrar.setText("Registrar Residente");
        if (btnCancelarEdicion != null) btnCancelarEdicion.setVisible(false);
        etiquetaMensajeEstado.setText("Listo para registrar nuevos residentes en el padrón.");
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
    }

    private void ejecutarBajaResidente(String dni, String nombreCompleto) {
        if (!SesionActual.esAdministrador()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Permiso denegado: Solo el Administrador puede dar de baja a un residente.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de dar de baja al residente " + nombreCompleto + " (DNI " + dni + ")?\n" +
                "Esta acción liberará la unidad habitacional en el sistema.",
                "Confirmar Baja de Residente (CRUD Delete)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (conf != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.eliminarResidentePorDni(dni);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    ManejadorErroresUI.mostrarExito(PanelResidentes.this,
                            "El residente " + nombreCompleto + " ha sido dado de baja exitosamente del padrón.",
                            "Baja Exitosa (CRUD Delete)");
                    cargarDatosTabla();
                    cargarComboDepartamentos();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelResidentes.this, "No se pudo dar de baja al residente", causa);
                }
            }
        };
        worker.execute();
    }

    // MENÚ CONTEXTUAL DE TABLA Y DESBORDE

    private void mostrarMenuContextualFila(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaResidentes.getRowCount()) return;

        Object dniObj = tablaResidentes.getValueAt(fila, 1);
        Object nomObj = tablaResidentes.getValueAt(fila, 2);
        Object apeObj = tablaResidentes.getValueAt(fila, 3);
        Object telObj = tablaResidentes.getValueAt(fila, 4);
        Object corObj = tablaResidentes.getValueAt(fila, 5);
        Object tipObj = tablaResidentes.getValueAt(fila, 6);
        Object torObj = tablaResidentes.getValueAt(fila, 7);
        Object depObj = tablaResidentes.getValueAt(fila, 8);

        String dni = (dniObj != null) ? dniObj.toString().trim() : "";
        String depto = (depObj != null) ? depObj.toString().trim() : "";
        String nombreCompleto = ((nomObj != null ? nomObj : "") + " " + (apeObj != null ? apeObj : "")).trim();

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_RESIDENTES);

        JMenuItem itemEstado = EstilosUI.crearItemMenuOscuro("Ver estado de cuenta (Depto " + depto + ")", EstilosUI.ACENTO_RESIDENTES, EstilosUI.ACENTO_RESIDENTES);
        itemEstado.addActionListener(e -> {
            if (!depto.isEmpty() && !depto.contains("Sin")) {
                new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto).setVisible(true);
            } else {
                ManejadorErroresUI.mostrarAdvertencia(this, "El residente seleccionado no tiene un departamento asignado.");
            }
        });
        popup.add(itemEstado);

        JMenuItem itemCuotas = EstilosUI.crearItemMenuOscuro("Consultar cuotas de " + nombreCompleto, EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_RESIDENTES);
        itemCuotas.addActionListener(e -> {
            if (!depto.isEmpty() && !depto.contains("Sin")) {
                new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto).setVisible(true);
            } else {
                ManejadorErroresUI.mostrarAdvertencia(this, "El residente no tiene un departamento asignado.");
            }
        });
        popup.add(itemCuotas);

        if (SesionActual.esAdministrador()) {
            EstilosUI.agregarSeparadorOscuro(popup);

            JMenuItem itemEditar = EstilosUI.crearItemMenuOscuro("Modificar datos del residente", EstilosUI.DORADO_PRINCIPAL, EstilosUI.DORADO_PRINCIPAL);
            itemEditar.addActionListener(e -> activarModoEdicion(dniObj, nomObj, apeObj, telObj, corObj, tipObj, depObj));
            popup.add(itemEditar);

            JMenuItem itemBaja = EstilosUI.crearItemMenuOscuro("Dar de baja del padrón residencial", EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
            itemBaja.addActionListener(e -> ejecutarBajaResidente(dni, nombreCompleto));
            popup.add(itemBaja);
        }

        popup.show(tablaResidentes, x, y);
    }

    private void mostrarMenuDesborde(Component invocador, int x, int y) {
        JPopupMenu menu = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_RESIDENTES);

        if (SesionActual.puedeExportarCsv()) {
            JMenuItem itemCsv = EstilosUI.crearItemMenuOscuro("Exportar Padrón Completo CSV", EstilosUI.ACENTO_RESIDENTES, EstilosUI.ACENTO_RESIDENTES);
            itemCsv.addActionListener(e -> exportarDirectorioCsv());
            menu.add(itemCsv);
            EstilosUI.agregarSeparadorOscuro(menu);
        }

        JMenuItem itemRefrescar = EstilosUI.crearItemMenuOscuro("Refrescar Padrón", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_RESIDENTES);
        itemRefrescar.addActionListener(e -> {
            cargarDatosTabla();
            cargarComboDepartamentos();
            cargarComboTorresFiltro();
        });
        menu.add(itemRefrescar);

        menu.show(invocador, x, y);
    }

    private void exportarDirectorioCsv() {
        try {
            com.condominio.service.ExportadorCsvService exp = new com.condominio.service.ExportadorCsvService();
            java.io.File f = exp.exportarTablaCsv(tablaResidentes, "Padron_Residentes");
            ManejadorErroresUI.mostrarExito(this, "Datos de residentes exportados en:\n" + f.getAbsolutePath(), "Exportación Exitosa");
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al exportar CSV", ex);
        }
    }

    public void limpiarFormulario() {
        if (txtDni != null) txtDni.setText("");
        if (txtNombres != null) txtNombres.setText("");
        if (txtApellidos != null) txtApellidos.setText("");
        if (txtTelefono != null) txtTelefono.setText("");
        if (txtCorreo != null) txtCorreo.setText("");
        if (comboDepartamento != null && comboDepartamento.getItemCount() > 0) {
            comboDepartamento.setSelectedIndex(0);
        }
        if (txtDni != null) {
            txtDni.requestFocusInWindow();
        }
    }
}
