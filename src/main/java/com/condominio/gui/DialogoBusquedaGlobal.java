package com.condominio.gui;

import com.condominio.dto.ResultadoBusquedaDTO;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Diálogo modal centralizado de Búsqueda Global del Sistema.
@Slf4j
public class DialogoBusquedaGlobal extends JDialog {

    private final CobranzaFachada fachada;
    private final VentanaPrincipal ventanaPrincipalRef;
    private final RolUsuario rolUsuario;
    private final String deptoResidente;

    private JTextField txtBuscar;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorterResultados;
    private JLabel lblContador;
    private JTextArea txtDetallePreview;
    private JLabel lblTituloPreview;
    private JLabel lblCategoriaPreview;
    private JLabel lblIdPreview;
    private JButton btnIrModulo;

    private List<ResultadoBusquedaDTO> cacheResultados = new ArrayList<>();
    private String categoriaFiltroActivo = "TODOS";
    private final List<JButton> listaBotonesPildoras = new ArrayList<>();

    public DialogoBusquedaGlobal(Window propietario, CobranzaFachada fachada, String textoInicial) {
        super(propietario, "Búsqueda en el Sistema - Cobranza Vecinal", ModalityType.APPLICATION_MODAL);
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();
        this.ventanaPrincipalRef = (propietario instanceof VentanaPrincipal vp) ? vp : null;
        this.rolUsuario = SesionActual.getRol();
        this.deptoResidente = "101";

        configurarVentana();
        inicializarComponentes(textoInicial);
        ejecutarBusqueda();
    }

    public static void mostrar(Window propietario, CobranzaFachada fachada, String textoInicial) {
        DialogoBusquedaGlobal dlg = new DialogoBusquedaGlobal(propietario, fachada, textoInicial);
        dlg.setVisible(true);
    }

    private void configurarVentana() {
        setSize(980, 620);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes(String textoInicial) {
        JPanel panelContenedor = new JPanel(new BorderLayout(0, 10));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(14, 18, 14, 18));

        // 1. ENCABEZADO SUPERIOR: BANNER + BARRA DE BÚSQUEDA + BADGE ROL

        JPanel panelNorte = new JPanel(new BorderLayout(0, 8));
        panelNorte.setOpaque(false);

        Color acentoRol = SesionActual.esAdministrador() ? EstilosUI.DORADO_PRINCIPAL
                : (SesionActual.esConserje() ? EstilosUI.ACENTO_OPERACIONES : EstilosUI.ACENTO_RESIDENTES);

        JPanel bannerSuperior = EstilosUI.crearPanelBanner(acentoRol);
        JLabel lblTitulo = new JLabel("CENTRO DE BÚSQUEDA Y CONSULTA INSTITUCIONAL");
        lblTitulo.setFont(EstilosUI.FUENTE_BANNER);
        lblTitulo.setForeground(acentoRol);

        String descRol = SesionActual.esAdministrador() ? "Control Total: Acceso irrestricto a finanzas, padrón, cobranzas y operaciones."
                : (SesionActual.esConserje() ? "Perfil Garita: Consulta de visitas, incidentes, contactos de emergencia y avisos de portería."
                : "Perfil Residente (Depto " + deptoResidente + "): Consulta de avisos, gastos comunes de la comunidad, cuotas propias y reservas.");

        JLabel lblSubtitulo = new JLabel(descRol);
        lblSubtitulo.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSubtitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        bannerSuperior.add(lblTitulo, BorderLayout.NORTH);
        bannerSuperior.add(lblSubtitulo, BorderLayout.SOUTH);
        panelNorte.add(bannerSuperior, BorderLayout.NORTH);

        // Barra con campo de texto de búsqueda reactivo
        JPanel panelBarraBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBarraBusqueda.setOpaque(false);

        txtBuscar = EstilosUI.crearCampoTexto(acentoRol);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(450, 36));

        String placeholder = SesionActual.esAdministrador()
                ? "Buscar gastos, cuotas, avisos, residentes, incidentes, visitas, departamentos..."
                : (SesionActual.esConserje()
                ? "Buscar visitas en garita, contactos de portería, incidentes, comunicados..."
                : "Buscar avisos oficiales, gastos de la comunidad, mis cuotas de mantenimiento, reservas...");

        txtBuscar.setToolTipText(placeholder);
        if (textoInicial != null && !textoInicial.isBlank() && !textoInicial.startsWith("Buscar")) {
            txtBuscar.setText(textoInicial.trim());
        }

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { ejecutarBusqueda(); }
            @Override public void removeUpdate(DocumentEvent e) { ejecutarBusqueda(); }
            @Override public void changedUpdate(DocumentEvent e) { ejecutarBusqueda(); }
        });

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        JButton btnLimpiar = new JButton("Limpiar");
        EstilosUI.estilizarBotonSecundario(btnLimpiar, EstilosUI.TEXTO_SECUNDARIO);
        btnLimpiar.setPreferredSize(new Dimension(90, 36));
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            txtBuscar.requestFocusInWindow();
        });

        panelBarraBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBarraBusqueda.add(btnLimpiar, BorderLayout.EAST);
        panelNorte.add(panelBarraBusqueda, BorderLayout.CENTER);

        // Barra de píldoras de filtrado por categoría (según rol)
        JPanel panelPildoras = crearBarraPildorasCategorias(acentoRol);
        panelNorte.add(panelPildoras, BorderLayout.SOUTH);

        panelContenedor.add(panelNorte, BorderLayout.NORTH);

        // 2. PANEL CENTRAL: SPLIT ENTRE TABLA DE RESULTADOS Y PREVIEW

        String[] columnas = {"ID", "Categoría", "Título / Concepto", "Información Clave", "Fecha", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaResultados = new JTable(modeloTabla);
        sorterResultados = new TableRowSorter<>(modeloTabla);
        tablaResultados.setRowSorter(sorterResultados);
        EstilosUI.estilizarTabla(tablaResultados, acentoRol);

        // Renderizadores
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaResultados.getColumnModel().getColumn(0).setPreferredWidth(85);
        tablaResultados.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaResultados.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaResultados.getColumnModel().getColumn(1).setCellRenderer(new RenderCategoriaBusqueda());
        tablaResultados.getColumnModel().getColumn(2).setPreferredWidth(210);
        tablaResultados.getColumnModel().getColumn(3).setPreferredWidth(240);
        tablaResultados.getColumnModel().getColumn(4).setPreferredWidth(85);
        tablaResultados.getColumnModel().getColumn(4).setCellRenderer(renderCentro);
        tablaResultados.getColumnModel().getColumn(5).setPreferredWidth(95);
        tablaResultados.getColumnModel().getColumn(5).setCellRenderer(new com.condominio.gui.util.RenderTipoOperacion());

        tablaResultados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarFichaPreview();
            }
        });

        tablaResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnIrModulo != null && btnIrModulo.isEnabled()) {
                    btnIrModulo.doClick();
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));

        // Panel lateral derecho: Ficha técnica / Vista previa modal
        JPanel panelPreview = crearPanelFichaPreview(acentoRol);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelPreview);
        splitPane.setOpaque(false);
        splitPane.setDividerLocation(610);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        panelContenedor.add(splitPane, BorderLayout.CENTER);

        // 3. BARRA INFERIOR DE ACCIONES Y ESTADO

        JPanel panelSur = new JPanel(new BorderLayout(10, 0));
        panelSur.setOpaque(false);

        lblContador = new JLabel("Listo para buscar.");
        lblContador.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblContador.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelSur.add(lblContador, BorderLayout.WEST);

        JPanel panelBotonesSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesSur.setOpaque(false);

        if (SesionActual.esAdministrador()) {
            btnIrModulo = new JButton("Ir al Módulo");
            EstilosUI.estilizarBoton(btnIrModulo, acentoRol);
            btnIrModulo.setPreferredSize(new Dimension(130, 32));
            btnIrModulo.setEnabled(false);
            btnIrModulo.setToolTipText("Navega de forma directa al módulo correspondiente en el sistema");
            btnIrModulo.addActionListener(e -> navegarAlModuloSeleccionado());
            panelBotonesSur.add(btnIrModulo);
        }

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.TEXTO_PRINCIPAL);
        btnCerrar.setPreferredSize(new Dimension(100, 32));
        btnCerrar.addActionListener(e -> dispose());
        panelBotonesSur.add(btnCerrar);

        panelSur.add(panelBotonesSur, BorderLayout.EAST);
        panelContenedor.add(panelSur, BorderLayout.SOUTH);

        setContentPane(panelContenedor);
    }

    private JPanel crearBarraPildorasCategorias(Color acento) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.setOpaque(false);

        JLabel lblFiltro = new JLabel("Categoría:");
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFiltro.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panel.add(lblFiltro);

        String[] categorias;
        if (SesionActual.esAdministrador()) {
            categorias = new String[]{"TODOS", "GASTOS", "COMUNICADOS", "CUOTAS", "INCIDENTES", "RESERVAS", "RESIDENTES", "VISITAS"};
        } else if (SesionActual.esConserje()) {
            categorias = new String[]{"TODOS", "VISITAS", "CONTACTOS", "INCIDENTES", "COMUNICADOS"};
        } else {
            categorias = new String[]{"TODOS", "COMUNICADOS", "GASTOS", "CUOTAS", "RESERVAS"};
        }

        for (String cat : categorias) {
            JButton btnPill = new JButton(cat);
            btnPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnPill.setFocusPainted(false);
            btnPill.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnPill.setPreferredSize(new Dimension(cat.length() > 8 ? 100 : 70, 24));

            if ("TODOS".equals(cat)) {
                btnPill.setBackground(acento);
                btnPill.setForeground(Color.BLACK);
                btnPill.setBorder(BorderFactory.createLineBorder(acento, 1));
            } else {
                btnPill.setBackground(new Color(0x16, 0x20, 0x2E));
                btnPill.setForeground(EstilosUI.TEXTO_SECUNDARIO);
                btnPill.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1));
            }

            btnPill.addActionListener(e -> {
                categoriaFiltroActivo = cat;
                for (JButton b : listaBotonesPildoras) {
                    if (b.getText().equals(cat)) {
                        b.setBackground(acento);
                        b.setForeground(Color.BLACK);
                        b.setBorder(BorderFactory.createLineBorder(acento, 1));
                    } else {
                        b.setBackground(new Color(0x16, 0x20, 0x2E));
                        b.setForeground(EstilosUI.TEXTO_SECUNDARIO);
                        b.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1));
                    }
                }
                aplicarFiltroCategoria();
            });

            listaBotonesPildoras.add(btnPill);
            panel.add(btnPill);
        }

        return panel;
    }

    private JPanel crearPanelFichaPreview(Color acento) {
        GlassPanel card = new GlassPanel(14, false, acento);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setPreferredSize(new Dimension(320, 400));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);

        lblIdPreview = new JLabel("SELECCIONE UN REGISTRO");
        lblIdPreview.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblIdPreview.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        lblTituloPreview = new JLabel("Detalle de Consulta");
        lblTituloPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTituloPreview.setForeground(EstilosUI.TEXTO_CLARO);

        lblCategoriaPreview = new JLabel("Vista Previa Rápida");
        lblCategoriaPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCategoriaPreview.setForeground(acento);

        header.add(lblIdPreview, BorderLayout.NORTH);
        header.add(lblTituloPreview, BorderLayout.CENTER);
        header.add(lblCategoriaPreview, BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);

        txtDetallePreview = new JTextArea("Seleccione un resultado de la tabla para visualizar la ficha técnica completa.");
        txtDetallePreview.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtDetallePreview.setForeground(new Color(0xDF, 0xE7, 0xF0));
        txtDetallePreview.setBackground(new Color(0x10, 0x1A, 0x26));
        txtDetallePreview.setLineWrap(true);
        txtDetallePreview.setWrapStyleWord(true);
        txtDetallePreview.setEditable(false);
        txtDetallePreview.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollTxt = new JScrollPane(txtDetallePreview);
        scrollTxt.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        card.add(scrollTxt, BorderLayout.CENTER);

        JLabel lblAvisoSeguridad = new JLabel(
                SesionActual.esResidente() ? "Modo consulta ciudadana - Copropietario"
                        : (SesionActual.esConserje() ? "Control operativo de accesos y portería" : "Administración con privilegios totales")
        );
        lblAvisoSeguridad.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblAvisoSeguridad.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        card.add(lblAvisoSeguridad, BorderLayout.SOUTH);

        return card;
    }

    private void ejecutarBusqueda() {
        String termino = (txtBuscar != null) ? txtBuscar.getText().trim() : "";

        SwingWorker<List<ResultadoBusquedaDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ResultadoBusquedaDTO> doInBackground() {
                return fachada.buscarEnSistema(termino, rolUsuario, deptoResidente);
            }

            @Override
            protected void done() {
                try {
                    cacheResultados = get();
                    poblarTablaResultados();
                } catch (Exception ex) {
                    log.error("Error al ejecutar búsqueda global: {}", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void poblarTablaResultados() {
        if (modeloTabla == null) return;
        modeloTabla.setRowCount(0);

        if (cacheResultados != null) {
            for (ResultadoBusquedaDTO r : cacheResultados) {
                modeloTabla.addRow(new Object[]{
                        r.getId(),
                        r.getCategoria(),
                        r.getTitulo(),
                        r.getSubtitulo(),
                        r.getFecha(),
                        r.getEstado()
                });
            }
        }

        aplicarFiltroCategoria();

        if (tablaResultados.getRowCount() > 0) {
            tablaResultados.setRowSelectionInterval(0, 0);
        } else {
            limpiarFichaPreview();
        }
    }

    private void aplicarFiltroCategoria() {
        if (sorterResultados == null) return;

        if ("TODOS".equalsIgnoreCase(categoriaFiltroActivo)) {
            sorterResultados.setRowFilter(null);
        } else {
            sorterResultados.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(categoriaFiltroActivo.trim()), 1));
        }

        int visibles = tablaResultados.getRowCount();
        int total = modeloTabla.getRowCount();
        lblContador.setText("Mostrando " + visibles + " de " + total + " registros para el criterio ingresado.");
    }

    private void actualizarFichaPreview() {
        int fila = tablaResultados.getSelectedRow();
        if (fila < 0) {
            limpiarFichaPreview();
            return;
        }

        int filaModelo = tablaResultados.convertRowIndexToModel(fila);
        if (filaModelo < 0 || filaModelo >= cacheResultados.size()) {
            return;
        }

        ResultadoBusquedaDTO r = cacheResultados.get(filaModelo);
        lblIdPreview.setText(r.getId());
        lblTituloPreview.setText(r.getTitulo());
        lblCategoriaPreview.setText(r.getCategoria() + "  |  " + r.getEstado());
        txtDetallePreview.setText(r.getDetalleCompleto());
        txtDetallePreview.setCaretPosition(0);

        if (btnIrModulo != null) {
            btnIrModulo.setEnabled(r.getModuloDestino() != null);
        }
    }

    private void limpiarFichaPreview() {
        lblIdPreview.setText("SIN SELECCIÓN");
        lblTituloPreview.setText("Ningún resultado seleccionado");
        lblCategoriaPreview.setText("-");
        txtDetallePreview.setText("No se encontraron coincidencias para el filtro actual.");
        if (btnIrModulo != null) btnIrModulo.setEnabled(false);
    }

    private void navegarAlModuloSeleccionado() {
        int fila = tablaResultados.getSelectedRow();
        if (fila < 0) return;
        int filaModelo = tablaResultados.convertRowIndexToModel(fila);
        if (filaModelo < 0 || filaModelo >= cacheResultados.size()) return;

        ResultadoBusquedaDTO r = cacheResultados.get(filaModelo);
        if (r.getModuloDestino() != null && ventanaPrincipalRef != null) {
            dispose();
            ventanaPrincipalRef.navegarModuloPorId(r.getModuloDestino());
        }
    }

    // Renderizador estilizado para la columna de Categoría con tintes semánticos.
    private static class RenderCategoriaBusqueda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));

            String cat = (value != null) ? value.toString().toUpperCase() : "";
            Color cTexto = EstilosUI.TEXTO_PRINCIPAL;
            if (cat.contains("GASTO")) {
                cTexto = EstilosUI.DORADO_PRINCIPAL;
            } else if (cat.contains("AVISO") || cat.contains("COMUNICADO")) {
                cTexto = EstilosUI.VERDE_EXITO;
            } else if (cat.contains("CUOTA")) {
                cTexto = EstilosUI.AZUL_INFO;
            } else if (cat.contains("VISITA") || cat.contains("GARITA")) {
                cTexto = EstilosUI.ACENTO_OPERACIONES;
            } else if (cat.contains("RESIDENTE") || cat.contains("PADRÓN") || cat.contains("CONTACTO")) {
                cTexto = EstilosUI.ACENTO_RESIDENTES;
            } else if (cat.contains("INCIDENTE") || cat.contains("INCIDENCIA")) {
                cTexto = EstilosUI.AMBAR_AVISO;
            } else if (cat.contains("RESERVA")) {
                cTexto = new Color(0x38, 0xBD, 0xF8);
            }

            lbl.setForeground(isSelected ? Color.WHITE : cTexto);
            return lbl;
        }
    }
}
