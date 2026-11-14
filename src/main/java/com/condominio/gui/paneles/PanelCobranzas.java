package com.condominio.gui.paneles;

import com.condominio.dto.CuotaDTO;
import com.condominio.gui.DialogoCalculoConsumo;
import com.condominio.gui.DialogoEstadoCuentaVecino;
import com.condominio.gui.animacion.OverlayProgreso;
import com.condominio.gui.util.CampoPlaceholder;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroDecimal;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.RenderEstadoCuota;
import com.condominio.gui.util.Validaciones;
import com.condominio.patrones.command.ComandoCobranza;
import com.condominio.patrones.command.ComandoRegistrarPago;
import com.condominio.patrones.command.InvocadorCobranza;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Panel interactivo para el módulo de Cobranzas y Recaudación Vecinal.
@Slf4j
public class PanelCobranzas extends JPanel {

    private final CobranzaFachada fachada;
    private final InvocadorCobranza invocador = new InvocadorCobranza();
    private JTable tablaCuotas;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;
    private JLabel lblContadorRegistros;
    private JComboBox<String> comboFiltroDepto;
    private List<CuotaDTO> cuotasCargadas = new ArrayList<>();

    private JTextField txtIdCuota;
    private JTextField txtMontoPagar;
    private JTextField txtCodigoOperacion;
    private JButton btnRegistrarPago;
    private JButton btnDeshacer;
    private JButton btnDesborde;
    private JButton btnGenerarCuotas;
    private JLabel etiquetaMensajeEstado;

    // Pestañas de estado con contador visible
    private JButton tabTodas;
    private JButton tabPendientes;
    private JButton tabParciales;
    private JButton tabPagadas;
    private String estadoFiltroActual = "TODAS";

    private int secuenciaOperacion = 1;

    public PanelCobranzas(CobranzaFachada fachada) {
        this.fachada = fachada;
        inicializarComponentes();
        cargarDatosTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel panelNorte = new JPanel(new BorderLayout(0, 8));
        panelNorte.setOpaque(false);
        panelNorte.add(construirBannerInformativo(), BorderLayout.NORTH);
        panelNorte.add(construirBarraControlFiltros(), BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        add(construirTablaCentro(), BorderLayout.CENTER);
        add(construirPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel construirBannerInformativo() {
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_COBRANZAS);

        JLabel tituloBanner = new JLabel("MÓDULO INTERACTIVO DE COBRANZAS Y RECAUDACIÓN VECINAL");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.ACENTO_COBRANZAS);

        JLabel subtituloBanner = new JLabel("Gestión integral de cuotas, abonos y facturación masiva mensual.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        return panelBanner;
    }

    private JPanel construirBarraControlFiltros() {
        JPanel panelControlFiltros = new JPanel(new BorderLayout(10, 0));
        panelControlFiltros.setOpaque(false);

        JPanel panelPestanas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panelPestanas.setOpaque(false);

        tabTodas = new JButton("Todas (0)");
        tabPendientes = new JButton("Pendientes (0)");
        tabParciales = new JButton("Parciales (0)");
        tabPagadas = new JButton("Pagadas (0)");

        tabTodas.setToolTipText("Filtrar: muestra todas las cuotas sin distinción de estado (1 clic)");
        tabPendientes.setToolTipText("Filtrar: muestra solo cuotas pendientes o vencidas (1 clic)");
        tabParciales.setToolTipText("Filtrar: muestra cuotas con abonos parciales registrados (1 clic)");
        tabPagadas.setToolTipText("Filtrar: muestra únicamente cuotas totalmente saldadas (1 clic)");

        tabTodas.setFocusPainted(false);
        tabPendientes.setFocusPainted(false);
        tabParciales.setFocusPainted(false);
        tabPagadas.setFocusPainted(false);

        tabTodas.addActionListener(e -> { estadoFiltroActual = "TODAS"; actualizarEstiloPestanas(); aplicarFiltroTabla(); });
        tabPendientes.addActionListener(e -> { estadoFiltroActual = "PENDIENTE"; actualizarEstiloPestanas(); aplicarFiltroTabla(); });
        tabParciales.addActionListener(e -> { estadoFiltroActual = "PARCIAL"; actualizarEstiloPestanas(); aplicarFiltroTabla(); });
        tabPagadas.addActionListener(e -> { estadoFiltroActual = "PAGADO"; actualizarEstiloPestanas(); aplicarFiltroTabla(); });

        actualizarEstiloPestanas();

        panelPestanas.add(tabTodas);
        panelPestanas.add(tabPendientes);
        panelPestanas.add(tabParciales);
        panelPestanas.add(tabPagadas);
        panelControlFiltros.add(panelPestanas, BorderLayout.WEST);

        JPanel panelAccionesDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelAccionesDerecha.setOpaque(false);

        comboFiltroDepto = new JComboBox<>();
        comboFiltroDepto.setEditable(false);
        EstilosUI.estilizarCombo(comboFiltroDepto, EstilosUI.ACENTO_COBRANZAS);
        EstilosUI.configurarComboPrototipoYTooltip(comboFiltroDepto, "-- Todos los Deptos --");
        comboFiltroDepto.setPreferredSize(new Dimension(190, 32));
        comboFiltroDepto.addItem("-- Todos los Departamentos --");
        comboFiltroDepto.setToolTipText("Filtra el listado de cuotas por unidad inmobiliaria seleccionada");
        comboFiltroDepto.addActionListener(e -> aplicarFiltroTabla());
        panelAccionesDerecha.add(comboFiltroDepto);

        btnDesborde = new JButton("...");
        EstilosUI.estilizarBotonSecundario(btnDesborde, EstilosUI.ACENTO_COBRANZAS);
        btnDesborde.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDesborde.setPreferredSize(new Dimension(38, 32));
        btnDesborde.setToolTipText("Más acciones administrativas y reportes del módulo (Desborde)");
        btnDesborde.addActionListener(e -> mostrarMenuDesborde(btnDesborde, 0, btnDesborde.getHeight()));
        panelAccionesDerecha.add(btnDesborde);

        btnGenerarCuotas = new JButton("Generar Cuotas Masivas");

        panelControlFiltros.add(panelAccionesDerecha, BorderLayout.EAST);
        return panelControlFiltros;
    }

    private JPanel construirTablaCentro() {
        String[] columnas = {
                "ID Cuota", "Departamento", "Monto Base", "Saldo Pendiente", "Estado", "Vencimiento"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCuotas = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.ACENTO_COBRANZAS);
        aplicarRenderizadoresTabla();
        enlazarEventosTabla();

        tablaCuotas.setPreferredScrollableViewportSize(new Dimension(800, 142));
        scrollTabla = new JScrollPane(tablaCuotas);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollTabla.setPreferredSize(new Dimension(800, 178));

        JPanel panelCentro = new JPanel(new BorderLayout(0, 6));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelContador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelContador.setOpaque(false);
        lblContadorRegistros = new JLabel("Mostrando 0 de 0 cuotas registradas");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContadorRegistros.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelContador.add(lblContadorRegistros);
        panelCentro.add(panelContador, BorderLayout.SOUTH);

        return panelCentro;
    }

    private void aplicarRenderizadoresTabla() {
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaCuotas.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaCuotas.getColumnModel().getColumn(1).setCellRenderer(renderCentro);
        tablaCuotas.getColumnModel().getColumn(4).setCellRenderer(new RenderEstadoCuota());
        tablaCuotas.getColumnModel().getColumn(5).setCellRenderer(renderCentro);

        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaCuotas.getColumnModel().getColumn(2).setCellRenderer(renderDerecha);
        tablaCuotas.getColumnModel().getColumn(3).setCellRenderer(renderDerecha);
    }

    private void enlazarEventosTabla() {
        tablaCuotas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaCuotas.getSelectedRow();
                if (fila != -1) {
                    Object idObj = tablaCuotas.getValueAt(fila, 0);
                    Object saldoObj = tablaCuotas.getValueAt(fila, 3);
                    Object estadoObj = tablaCuotas.getValueAt(fila, 4);

                    if (idObj != null) {
                        txtIdCuota.setText(idObj.toString());
                    }
                    if (saldoObj != null) {
                        String saldoStr = saldoObj.toString().replace("S/ ", "").replace(",", "").trim();
                        txtMontoPagar.setText(saldoStr);
                    }
                    if (estadoObj != null && "PAGADO".equalsIgnoreCase(estadoObj.toString())) {
                        etiquetaMensajeEstado.setText("Aviso: La cuota seleccionada #" + idObj + " ya se encuentra PAGADA.");
                        etiquetaMensajeEstado.setForeground(EstilosUI.DORADO_CLARO);
                    } else {
                        etiquetaMensajeEstado.setText("Cuota seleccionada: ID #" + idObj);
                        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);
                    }
                }
            }
        });

        tablaCuotas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            private void procesarPopup(MouseEvent e) {
                int fila = tablaCuotas.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaCuotas.getRowCount()) {
                    tablaCuotas.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualFila(e.getX(), e.getY(), fila);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    int fila = tablaCuotas.rowAtPoint(e.getPoint());
                    if (fila >= 0 && fila < tablaCuotas.getRowCount()) {
                        tablaCuotas.setRowSelectionInterval(fila, fila);
                        Object deptoObj = tablaCuotas.getValueAt(fila, 1);
                        if (deptoObj != null) {
                            DialogoEstadoCuentaVecino dlg = new DialogoEstadoCuentaVecino(
                                    SwingUtilities.getWindowAncestor(PanelCobranzas.this),
                                    fachada,
                                    deptoObj.toString()
                            );
                            dlg.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    private JPanel construirFormularioInferiorPago() {
        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_COBRANZAS, 14);
        panelFormulario.setLayout(new BorderLayout(16, 0));
        panelFormulario.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelCampos.setOpaque(false);

        // Campo 1: ID Cuota
        JPanel colId = new JPanel(new BorderLayout(0, 4));
        colId.setOpaque(false);
        colId.add(EstilosUI.crearEtiquetaCampo("ID Cuota:"), BorderLayout.NORTH);
        txtIdCuota = EstilosUI.crearCampoPlaceholder("Ej: 1", EstilosUI.ACENTO_COBRANZAS);
        FiltroNumerico.aplicar(txtIdCuota, 8);
        txtIdCuota.setPreferredSize(new Dimension(85, 32));
        txtIdCuota.setToolTipText("ID de la cuota a cancelar (solo números, se autocompleta al seleccionar fila)");
        colId.add(txtIdCuota, BorderLayout.CENTER);
        panelCampos.add(colId);

        // Campo 2: Monto a Pagar
        JPanel colMonto = new JPanel(new BorderLayout(0, 4));
        colMonto.setOpaque(false);
        colMonto.add(EstilosUI.crearEtiquetaCampo("Monto a Pagar (S/):"), BorderLayout.NORTH);
        txtMontoPagar = EstilosUI.crearCampoPlaceholder("Ej: 250.00", EstilosUI.ACENTO_COBRANZAS);
        FiltroDecimal.aplicar(txtMontoPagar, 9);
        txtMontoPagar.setPreferredSize(new Dimension(130, 32));
        txtMontoPagar.setToolTipText("Monto abonado (no puede exceder el saldo pendiente)");
        colMonto.add(txtMontoPagar, BorderLayout.CENTER);
        panelCampos.add(colMonto);

        // Campo 3: Código Operación (Solo Lectura)
        JPanel colCod = new JPanel(new BorderLayout(0, 4));
        colCod.setOpaque(false);
        colCod.add(EstilosUI.crearEtiquetaCampo("Código Operación:"), BorderLayout.NORTH);
        txtCodigoOperacion = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtCodigoOperacion.setEditable(false);
        txtCodigoOperacion.setText(String.format("PAGO-GUI-%03d", secuenciaOperacion));
        txtCodigoOperacion.setPreferredSize(new Dimension(140, 32));
        txtCodigoOperacion.setToolTipText("Identificador secuencial autogenerado de la transacción (solo lectura)");
        colCod.add(txtCodigoOperacion, BorderLayout.CENTER);
        panelCampos.add(colCod);

        panelFormulario.add(panelCampos, BorderLayout.WEST);

        // Subpanel Derecho: Solo 2 Botones Visibles (Registrar y Deshacer)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        panelBotones.setOpaque(false);

        btnRegistrarPago = new JButton("Registrar Pago");
        EstilosUI.estilizarBoton(btnRegistrarPago, EstilosUI.ACENTO_COBRANZAS);
        btnRegistrarPago.setPreferredSize(new Dimension(140, 32));
        btnRegistrarPago.setToolTipText("Procesa el pago contable y notifica a los residentes");
        btnRegistrarPago.addActionListener(e -> ejecutarRegistroPago());
        panelBotones.add(btnRegistrarPago);

        btnDeshacer = new JButton("Deshacer Último Pago");
        EstilosUI.estilizarBoton(btnDeshacer, EstilosUI.ROJO_PELIGRO, EstilosUI.ROJO_BORDE);
        btnDeshacer.setPreferredSize(new Dimension(170, 32));
        btnDeshacer.setEnabled(false);
        btnDeshacer.setVisible(com.condominio.modelo.seguridad.SesionActual.puedeDeshacerPagos());
        btnDeshacer.setToolTipText("Revierte el último comando de pago registrado en esta sesión");
        btnDeshacer.addActionListener(e -> ejecutarDeshacerPago());
        panelBotones.add(btnDeshacer);

        panelFormulario.add(panelBotones, BorderLayout.EAST);
        return panelFormulario;
    }

    private JPanel construirPanelInferior() {
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);
        panelInferior.add(construirFormularioInferiorPago(), BorderLayout.CENTER);

        etiquetaMensajeEstado = new JLabel("Listo para procesar transacciones.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);

        return panelInferior;
    }

    // Carga asíncrona de las cuotas de mantenimiento utilizando SwingWorker.
    public void cargarDatosTabla() {
        if (fachada == null) return;

        etiquetaMensajeEstado.setText("Sincronizando cuotas con la base de datos...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<List<CuotaDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CuotaDTO> doInBackground() {
                return fachada.listarTodasLasCuotas();
            }

            @Override
            protected void done() {
                try {
                    actualizarFiltrosYContadoresCuotas(get());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    ManejadorErroresUI.mostrarError(PanelCobranzas.this, "Operación interrumpida durante la carga de datos", ie);
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelCobranzas.this, "Error al sincronizar datos desde la base de datos", causa);
                    etiquetaMensajeEstado.setText("Error al cargar datos: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private void actualizarFiltrosYContadoresCuotas(List<CuotaDTO> cuotas) {
        cuotasCargadas = cuotas != null ? cuotas : new ArrayList<>();

        // Actualizar combo de filtro
        String seleccionPrevia = (String) comboFiltroDepto.getSelectedItem();
        comboFiltroDepto.removeAllItems();
        comboFiltroDepto.addItem("-- Todos los Departamentos --");

        java.util.Set<String> deptosUnicos = new java.util.TreeSet<>();
        for (CuotaDTO c : cuotasCargadas) {
            if (c.getNumeroDepartamento() != null) {
                deptosUnicos.add(c.getNumeroDepartamento());
            }
        }
        for (String d : deptosUnicos) {
            comboFiltroDepto.addItem(d);
        }
        if (seleccionPrevia != null) {
            comboFiltroDepto.setSelectedItem(seleccionPrevia);
        }
        if (comboFiltroDepto.getSelectedItem() != null) {
            comboFiltroDepto.setToolTipText(comboFiltroDepto.getSelectedItem().toString());
        }

        if (scrollTabla != null) {
            scrollTabla.getVerticalScrollBar().setValue(0);
        }
        if (tablaCuotas != null) {
            tablaCuotas.setPreferredScrollableViewportSize(new Dimension(800, 142));
        }

        int cntTotal = cuotasCargadas.size();
        int cntPend = 0;
        int cntParc = 0;
        int cntPag = 0;
        for (CuotaDTO c : cuotasCargadas) {
            String est = (c.getEstado() != null) ? c.getEstado().toUpperCase() : "";
            if ("PENDIENTE".equals(est) || "VENCIDO".equals(est)) cntPend++;
            else if ("PARCIAL".equals(est)) cntParc++;
            else if ("PAGADO".equals(est)) cntPag++;
        }
        if (tabTodas != null) tabTodas.setText("Todas (" + cntTotal + ")");
        if (tabPendientes != null) tabPendientes.setText("Pendientes (" + cntPend + ")");
        if (tabParciales != null) tabParciales.setText("Parciales (" + cntParc + ")");
        if (tabPagadas != null) tabPagadas.setText("Pagadas (" + cntPag + ")");

        aplicarFiltroTabla();
        actualizarEstadoBotonDeshacer();

        etiquetaMensajeEstado.setText("Módulo sincronizado: " + cuotasCargadas.size() + " registros.");
        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
    }

    private void aplicarFiltroTabla() {
        String seleccion = (comboFiltroDepto != null) ? (String) comboFiltroDepto.getSelectedItem() : null;
        modeloTabla.setRowCount(0);

        boolean filtrarDepto = seleccion != null && !seleccion.startsWith("--");
        int mostrados = 0;

        for (CuotaDTO c : cuotasCargadas) {
            if (filtrarDepto && (c.getNumeroDepartamento() == null || !c.getNumeroDepartamento().equalsIgnoreCase(seleccion))) {
                continue;
            }

            String est = (c.getEstado() != null) ? c.getEstado().toUpperCase() : "PENDIENTE";
            boolean pasaEstado = true;
            if ("PENDIENTE".equals(estadoFiltroActual)) {
                pasaEstado = "PENDIENTE".equals(est) || "VENCIDO".equals(est);
            } else if ("PARCIAL".equals(estadoFiltroActual)) {
                pasaEstado = "PARCIAL".equals(est);
            } else if ("PAGADO".equals(estadoFiltroActual)) {
                pasaEstado = "PAGADO".equals(est);
            }

            if (pasaEstado) {
                modeloTabla.addRow(new Object[]{
                        c.getIdCuota(),
                        c.getNumeroDepartamento(),
                        String.format("S/ %.2f", c.getMontoBase()),
                        String.format("S/ %.2f", c.getMontoFinal()),
                        c.getEstado(),
                        c.getFechaVencimiento()
                });
                mostrados++;
            }
        }
        if (lblContadorRegistros != null) {
            lblContadorRegistros.setText("Mostrando " + mostrados + " de " + cuotasCargadas.size() + " cuotas registradas");
        }
        if (scrollTabla != null) {
            scrollTabla.getVerticalScrollBar().setValue(0);
        }
        if (tablaCuotas != null) {
            tablaCuotas.setPreferredScrollableViewportSize(new Dimension(800, 142));
        }
    }

    private void actualizarEstiloPestanas() {
        EstilosUI.estilizarBotonPildora(tabTodas, EstilosUI.ACENTO_COBRANZAS, "TODAS".equals(estadoFiltroActual));
        EstilosUI.estilizarBotonPildora(tabPendientes, EstilosUI.ACENTO_COBRANZAS, "PENDIENTE".equals(estadoFiltroActual));
        EstilosUI.estilizarBotonPildora(tabParciales, EstilosUI.ACENTO_COBRANZAS, "PARCIAL".equals(estadoFiltroActual));
        EstilosUI.estilizarBotonPildora(tabPagadas, EstilosUI.ACENTO_COBRANZAS, "PAGADO".equals(estadoFiltroActual));
    }

    private void mostrarMenuContextualFila(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaCuotas.getRowCount()) return;
        Object idObj = tablaCuotas.getValueAt(fila, 0);
        Object deptoObj = tablaCuotas.getValueAt(fila, 1);
        Object saldoObj = tablaCuotas.getValueAt(fila, 3);
        String depto = (deptoObj != null) ? deptoObj.toString() : "";
        String idStr = (idObj != null) ? idObj.toString() : "";

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_COBRANZAS);

        JMenuItem itemPago = EstilosUI.crearItemMenuOscuro("Registrar Pago para Depto " + depto + " (ID #" + idStr + ")", EstilosUI.ACENTO_COBRANZAS, EstilosUI.ACENTO_COBRANZAS);
        itemPago.setToolTipText("Autocompleta el formulario inferior con los datos de esta cuota");
        itemPago.addActionListener(e -> {
            txtIdCuota.setText(idStr);
            if (saldoObj != null) {
                String s = saldoObj.toString().replace("S/ ", "").replace(",", "").trim();
                txtMontoPagar.setText(s);
            }
            txtMontoPagar.requestFocusInWindow();
        });
        popup.add(itemPago);

        JMenuItem itemEstado = EstilosUI.crearItemMenuOscuro("Estado de Cuenta (Depto " + depto + ")", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
        itemEstado.setToolTipText("Abre directamente el estado de cuenta de la unidad " + depto);
        itemEstado.addActionListener(e -> {
            DialogoEstadoCuentaVecino dlg = new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto);
            dlg.setVisible(true);
        });
        popup.add(itemEstado);

        if (com.condominio.modelo.seguridad.SesionActual.esAdministrador()) {
            JMenuItem itemSimular = EstilosUI.crearItemMenuOscuro("Simular Mora y Descuento", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
            itemSimular.setToolTipText("Simulación financiera con intereses y descuentos");
            itemSimular.addActionListener(e -> abrirDialogoSimularDecorator());
            popup.add(itemSimular);
        }

        JMenuItem itemConsumo = EstilosUI.crearItemMenuOscuro("Consumo Agua y Luz", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
        itemConsumo.setToolTipText("Reporte de gastos y consumos del periodo");
        itemConsumo.addActionListener(e -> {
            DialogoCalculoConsumo dlg = new DialogoCalculoConsumo(SwingUtilities.getWindowAncestor(this), fachada, this::cargarDatosTabla);
            dlg.setVisible(true);
        });
        popup.add(itemConsumo);

        if (com.condominio.modelo.seguridad.SesionActual.esAdministrador()) {
            JMenuItem itemAnular = EstilosUI.crearItemMenuOscuro("Anular Cuota #" + idStr, EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
            itemAnular.setToolTipText("Elimina la cuota seleccionada siempre que no esté pagada");
            itemAnular.addActionListener(e -> {
                try {
                    ejecutarAnularCuota(Long.valueOf(idStr), depto);
                } catch (Exception ex) {
                    ManejadorErroresUI.mostrarError(this, "ID de cuota inválido", ex);
                }
            });
            popup.add(itemAnular);
        }

        popup.show(tablaCuotas, x, y);
    }

    private void ejecutarAnularCuota(Long idCuota, String depto) {
        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de anular la cuota #" + idCuota + " del Depto " + depto + "?\nEsta acción eliminará el registro financiero.",
                "Confirmar Anulación de Cuota (CRUD Delete)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (resp == JOptionPane.YES_OPTION) {
            try {
                boolean ok = fachada.anularCuota(idCuota);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cuota #" + idCuota + " anulada exitosamente.", "Anulación Exitosa (CRUD Delete)", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosTabla();
                } else {
                    ManejadorErroresUI.mostrarAdvertencia(this, "No se pudo anular la cuota seleccionada.");
                }
            } catch (Exception ex) {
                ManejadorErroresUI.mostrarError(this, "Error al anular cuota: " + ex.getMessage(), ex);
            }
        }
    }

    private void mostrarMenuDesborde(Component invocador, int x, int y) {
        JPopupMenu menu = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_COBRANZAS);

        if (com.condominio.modelo.seguridad.SesionActual.esAdministrador()) {
            JMenuItem itemGen = EstilosUI.crearItemMenuOscuro("Generación Masiva de Cuotas", EstilosUI.ACENTO_COBRANZAS, EstilosUI.ACENTO_COBRANZAS);
            itemGen.setToolTipText("Crea masivamente cuotas sin duplicar departamentos con cuota abierta");
            itemGen.addActionListener(e -> ejecutarGenerarCuotasMes());
            menu.add(itemGen);

            JMenuItem itemNotif = EstilosUI.crearItemMenuOscuro("Enviar Notificaciones a Morosos", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
            itemNotif.setToolTipText("Emite recordatorios a residentes con saldos pendientes");
            itemNotif.addActionListener(e -> ejecutarNotificarMorosos());
            menu.add(itemNotif);

            JMenuItem itemCsv = EstilosUI.crearItemMenuOscuro("Exportar Auditoría CSV", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
            itemCsv.setToolTipText("Exporta el listado actual de cuotas a formato CSV con BOM UTF-8");
            itemCsv.addActionListener(e -> exportarCsvCuotas());
            menu.add(itemCsv);

            EstilosUI.agregarSeparadorOscuro(menu);
        }

        JMenuItem itemEdoCta = EstilosUI.crearItemMenuOscuro("Estado de Cuenta...", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
        itemEdoCta.setToolTipText("Reporte de solo lectura del estado de cuenta por departamento");
        itemEdoCta.addActionListener(e -> abrirEstadoCuentaSeleccionado());
        menu.add(itemEdoCta);

        JMenuItem itemCons = EstilosUI.crearItemMenuOscuro("Consumo Agua y Luz...", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
        itemCons.setToolTipText("Reporte de gastos y consumos del periodo");
        itemCons.addActionListener(e -> {
            DialogoCalculoConsumo dlg = new DialogoCalculoConsumo(SwingUtilities.getWindowAncestor(this), fachada, this::cargarDatosTabla);
            dlg.setVisible(true);
        });
        menu.add(itemCons);

        if (com.condominio.modelo.seguridad.SesionActual.esAdministrador()) {
            JMenuItem itemSim = EstilosUI.crearItemMenuOscuro("Simular Mora y Descuento...", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
            itemSim.setToolTipText("Simulación analítica con cálculo de intereses y bonificaciones");
            itemSim.addActionListener(e -> abrirDialogoSimularDecorator());
            menu.add(itemSim);
        }

        EstilosUI.agregarSeparadorOscuro(menu);

        JMenuItem itemRefrescar = EstilosUI.crearItemMenuOscuro("Refrescar Listado", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_COBRANZAS);
        itemRefrescar.setToolTipText("Recarga el listado completo de cuotas y actualiza contadores");
        itemRefrescar.addActionListener(e -> cargarDatosTabla());
        menu.add(itemRefrescar);

        menu.show(invocador, x, y);
    }

    private void exportarCsvCuotas() {
        try {
            com.condominio.service.ExportadorCsvService exp = new com.condominio.service.ExportadorCsvService();
            java.io.File f = exp.exportarTablaCsv(tablaCuotas, "Cobranzas_Cuotas");
            mostrarAvisoOscuro("Exportación CSV Exitosa", "Datos exportados correctamente en:\n" + f.getAbsolutePath(), EstilosUI.VERDE_EXITO);
        } catch (Exception ex) {
            mostrarAvisoOscuro("Error al Exportar", ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
        }
    }

    private void abrirEstadoCuentaSeleccionado() {
        String seleccion = (String) comboFiltroDepto.getSelectedItem();
        String depto = null;

        if (seleccion != null && !seleccion.startsWith("--")) {
            depto = seleccion;
        } else {
            int fila = tablaCuotas.getSelectedRow();
            if (fila != -1) {
                Object val = tablaCuotas.getValueAt(fila, 1);
                if (val != null) depto = val.toString();
            }
        }

        if (depto == null || depto.isBlank()) {
            etiquetaMensajeEstado.setText("Error: Seleccione un departamento en el filtro o en la tabla para ver su estado de cuenta.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            comboFiltroDepto.requestFocusInWindow();
            ManejadorErroresUI.mostrarAdvertencia(this, "Debe seleccionar un departamento en el filtro o en la tabla para consultar su estado de cuenta.");
            return;
        }

        DialogoEstadoCuentaVecino dialogo = new DialogoEstadoCuentaVecino(SwingUtilities.getWindowAncestor(this), fachada, depto);
        dialogo.setVisible(true);
    }

    private void ejecutarRegistroPago() {
        String idStr = txtIdCuota.getText().trim();
        String montoStr = txtMontoPagar.getText().trim();
        String codOperacion = txtCodigoOperacion.getText().trim();

        // 1. Validación de ID Cuota
        if (idStr.isEmpty()) {
            etiquetaMensajeEstado.setText("Error: Ingrese o seleccione el ID de la cuota a pagar.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtIdCuota.requestFocusInWindow();
            return;
        }

        Long idCuota;
        try {
            idCuota = Long.parseLong(idStr);
        } catch (NumberFormatException nfe) {
            etiquetaMensajeEstado.setText("Error: El ID de la cuota debe ser un número entero.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtIdCuota.requestFocusInWindow();
            return;
        }

        // 2. Buscar la cuota en el listado cargado
        CuotaDTO cuotaSeleccionada = cuotasCargadas.stream()
                .filter(c -> idCuota.equals(c.getIdCuota()))
                .findFirst()
                .orElse(null);

        if (cuotaSeleccionada == null) {
            etiquetaMensajeEstado.setText("Error: El ID de cuota #" + idCuota + " no existe en el sistema.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtIdCuota.requestFocusInWindow();
            return;
        }

        // 3. Regla de negocio: Cuota en estado PAGADO no acepta nuevos pagos
        if ("PAGADO".equalsIgnoreCase(cuotaSeleccionada.getEstado())) {
            etiquetaMensajeEstado.setText("Error: La cuota #" + idCuota + " ya se encuentra en estado PAGADO.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtIdCuota.requestFocusInWindow();
            return;
        }

        // 4. Validación de Monto
        if (montoStr.isEmpty()) {
            etiquetaMensajeEstado.setText("Error: Debe ingresar el monto a pagar.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtMontoPagar.requestFocusInWindow();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException nfe) {
            etiquetaMensajeEstado.setText("Error: El monto a pagar debe ser un número decimal válido.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtMontoPagar.requestFocusInWindow();
            return;
        }

        double saldoPendiente = cuotaSeleccionada.getMontoFinal() != null ? cuotaSeleccionada.getMontoFinal() : 0.0;
        try {
            Validaciones.validarMontoPago(monto, saldoPendiente);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtMontoPagar.requestFocusInWindow();
            return;
        }

        // 5. Ejecución del pago con Patrón Command en segundo plano
        btnRegistrarPago.setEnabled(false);
        etiquetaMensajeEstado.setText("Procesando pago de cuota #" + idCuota + "...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        final double montoFinal = monto;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ComandoCobranza comandoPago = new ComandoRegistrarPago(fachada, idCuota, montoFinal, codOperacion);
                invocador.ejecutarComando(comandoPago);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrarPago.setEnabled(true);
                try {
                    get();
                    secuenciaOperacion++;
                    txtCodigoOperacion.setText(String.format("PAGO-GUI-%03d", secuenciaOperacion));

                    ManejadorErroresUI.mostrarExito(PanelCobranzas.this,
                            "Pago procesado exitosamente.\nNotificación emitida a residentes.\nCuota ID: " + idCuota + " | Monto: S/ " + String.format("%.2f", montoFinal),
                            "Transacción Aprobada");

                    cargarDatosTabla();
                    etiquetaMensajeEstado.setText("Pago de S/ " + String.format("%.2f", montoFinal) + " registrado para cuota #" + idCuota + ".");
                    int filaResaltar = tablaCuotas.getSelectedRow() >= 0 ? tablaCuotas.getSelectedRow() : 0;
                    com.condominio.gui.animacion.DestelloFila.resaltar(tablaCuotas, filaResaltar, EstilosUI.ACENTO_COBRANZAS);
                    etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                    txtMontoPagar.setText("");
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelCobranzas.this, "No se pudo procesar el pago", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                    txtMontoPagar.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void ejecutarDeshacerPago() {
        if (!invocador.tieneComandosPorDeshacer()) {
            etiquetaMensajeEstado.setText("Error: No hay transacciones registradas en la sesión para revertir.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            actualizarEstadoBotonDeshacer();
            return;
        }

        // Regla: Confirmación previa en diálogo oscuro corporativo antes de revertir
        JDialog dialogoConf = new JDialog(SwingUtilities.getWindowAncestor(this), "Confirmación de Deshacer Pago (Command Undo)", Dialog.ModalityType.APPLICATION_MODAL);
        dialogoConf.setSize(460, 200);
        dialogoConf.setLocationRelativeTo(this);
        dialogoConf.setLayout(new BorderLayout(10, 10));
        dialogoConf.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelConf = new JPanel(new BorderLayout(8, 8));
        panelConf.setBackground(EstilosUI.FONDO_TARJETA);
        panelConf.setBorder(BorderFactory.createCompoundBorder(
                EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lblConf = new JLabel("<html><body style='width: 360px;'>¿Está seguro de revertir y anular la última transacción de pago realizada?<br>El saldo y estado de la cuota serán restablecidos en la base de datos.</body></html>");
        lblConf.setFont(EstilosUI.FUENTE_REGULAR);
        lblConf.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        panelConf.add(lblConf, BorderLayout.CENTER);

        JPanel panelBtnConf = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBtnConf.setOpaque(false);
        JButton btnNo = new JButton("No");
        EstilosUI.estilizarBotonSecundario(btnNo, EstilosUI.ACENTO_COBRANZAS);
        JButton btnSi = new JButton("Sí, Revertir");
        EstilosUI.estilizarBoton(btnSi, EstilosUI.ACENTO_COBRANZAS);

        final boolean[] revertir = {false};
        btnNo.addActionListener(e -> dialogoConf.dispose());
        btnSi.addActionListener(e -> {
            revertir[0] = true;
            dialogoConf.dispose();
        });

        panelBtnConf.add(btnNo);
        panelBtnConf.add(btnSi);

        dialogoConf.add(panelConf, BorderLayout.CENTER);
        dialogoConf.add(panelBtnConf, BorderLayout.SOUTH);
        dialogoConf.setVisible(true);

        if (!revertir[0]) {
            etiquetaMensajeEstado.setText("Operación de reversión cancelada por el usuario.");
            etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
            return;
        }

        btnDeshacer.setEnabled(false);
        btnDeshacer.setVisible(com.condominio.modelo.seguridad.SesionActual.puedeDeshacerPagos());
        etiquetaMensajeEstado.setText("Revertiendo última transacción...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return invocador.deshacerUltimoComando();
            }

            @Override
            protected void done() {
                try {
                    boolean revertido = get();
                    if (revertido) {
                        mostrarAvisoOscuro("Transacción Revertida",
                                "Último pago anulado exitosamente.\nEl saldo y estado de la cuota fueron restaurados en la base de datos.",
                                EstilosUI.VERDE_EXITO);
                        cargarDatosTabla();
                        etiquetaMensajeEstado.setText("Último pago revertido con éxito.");
                        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                    } else {
                        etiquetaMensajeEstado.setText("No se pudo revertir la transacción.");
                        etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelCobranzas.this, "Error al revertir el comando de pago", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                } finally {
                    actualizarEstadoBotonDeshacer();
                }
            }
        };
        worker.execute();
    }

    private void actualizarEstadoBotonDeshacer() {
        if (btnDeshacer != null) {
            btnDeshacer.setEnabled(invocador.tieneComandosPorDeshacer());
        }
    }

    private void abrirDialogoSimularDecorator() {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Simulación Financiera - Intereses y Descuentos", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(480, 360);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(12, 12));
        dialogo.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelContenido = new JPanel(new GridLayout(6, 2, 8, 8));
        panelContenido.setBackground(EstilosUI.FONDO_TARJETA);
        panelContenido.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Obtener monto de cuota seleccionada si existe
        String montoInicial = "250.00";
        int fila = tablaCuotas.getSelectedRow();
        if (fila != -1) {
            Object objMonto = tablaCuotas.getValueAt(fila, 2);
            if (objMonto != null) {
                montoInicial = objMonto.toString().replace("S/ ", "").replace(",", "").trim();
            }
        }

        JTextField txtMontoSim = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        FiltroDecimal.aplicar(txtMontoSim, 9);
        txtMontoSim.setText(montoInicial);

        JCheckBox chkMora = new JCheckBox("Mora Ilustrativa (+10%)");
        chkMora.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        chkMora.setOpaque(false);
        JCheckBox chkProntoPago = new JCheckBox("Descuento Ilustrativo (-5%)");
        chkProntoPago.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        chkProntoPago.setOpaque(false);
        JCheckBox chkFondoReserva = new JCheckBox("Fondo de Reserva (+S/ 20)");
        chkFondoReserva.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        chkFondoReserva.setOpaque(false);

        JLabel lblResultado = new JLabel("Total Simulado: S/ " + montoInicial);
        lblResultado.setFont(EstilosUI.FUENTE_TITULO);
        lblResultado.setForeground(EstilosUI.DORADO_CLARO);

        JButton btnCalcular = new JButton("Aplicar Decoradores");
        EstilosUI.estilizarBoton(btnCalcular, EstilosUI.ACENTO_COBRANZAS);
        btnCalcular.addActionListener(e -> {
            try {
                double base = Double.parseDouble(txtMontoSim.getText().trim());
                double res = base;
                if (chkMora.isSelected()) res += (base * 0.10);
                if (chkProntoPago.isSelected()) res -= (base * 0.05);
                if (chkFondoReserva.isSelected()) res += 20.0;
                lblResultado.setText(String.format("Total Simulado: S/ %.2f", res));
            } catch (Exception ex) {
                lblResultado.setText("Monto base inválido");
            }
        });

        panelContenido.add(EstilosUI.crearEtiquetaCampo("Monto Base (S/):"));
        panelContenido.add(txtMontoSim);
        panelContenido.add(chkMora);
        panelContenido.add(new JLabel(""));
        panelContenido.add(chkProntoPago);
        panelContenido.add(new JLabel(""));
        panelContenido.add(chkFondoReserva);
        panelContenido.add(new JLabel(""));
        panelContenido.add(btnCalcular);
        panelContenido.add(lblResultado);

        dialogo.add(panelContenido, BorderLayout.CENTER);
        dialogo.setVisible(true);
    }

    private void ejecutarGenerarCuotasMes() {
        String periodoSugerido = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());

        // Diálogo modal con tema oscuro propio para confirmación e ingreso de datos
        JDialog dialogoProto = new JDialog(SwingUtilities.getWindowAncestor(this), "Confirmación de Facturación Masiva de Cuotas", Dialog.ModalityType.APPLICATION_MODAL);
        dialogoProto.setSize(480, 260);
        dialogoProto.setLocationRelativeTo(this);
        dialogoProto.setLayout(new BorderLayout(10, 10));
        dialogoProto.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelCentro = new JPanel(new GridLayout(3, 2, 8, 8));
        panelCentro.setBackground(EstilosUI.FONDO_TARJETA);
        panelCentro.setBorder(BorderFactory.createCompoundBorder(
                EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JTextField txtPeriodo = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtPeriodo.setText(periodoSugerido);
        JTextField txtMontoBase = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        FiltroDecimal.aplicar(txtMontoBase, 8);
        txtMontoBase.setText("250.00");

        JLabel lblInfo = new JLabel("Generación masiva y emisión de cuotas para unidades sin cuota abierta.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelCentro.add(EstilosUI.crearEtiquetaCampo("Periodo (YYYY-MM):"));
        panelCentro.add(txtPeriodo);
        panelCentro.add(EstilosUI.crearEtiquetaCampo("Monto Base (S/):"));
        panelCentro.add(txtMontoBase);
        panelCentro.add(lblInfo);
        panelCentro.add(new JLabel(""));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        EstilosUI.estilizarBotonSecundario(btnCancelar, EstilosUI.ACENTO_COBRANZAS);
        JButton btnConfirmar = new JButton("Generar Cuotas Masivas");
        EstilosUI.estilizarBoton(btnConfirmar, EstilosUI.ACENTO_COBRANZAS);

        final boolean[] confirmado = {false};
        btnCancelar.addActionListener(e -> dialogoProto.dispose());
        btnConfirmar.addActionListener(e -> {
            confirmado[0] = true;
            dialogoProto.dispose();
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);

        dialogoProto.add(panelCentro, BorderLayout.CENTER);
        dialogoProto.add(panelBotones, BorderLayout.SOUTH);
        dialogoProto.setVisible(true);

        if (!confirmado[0]) {
            etiquetaMensajeEstado.setText("Generación masiva cancelada por el usuario.");
            etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
            return;
        }

        String periodo = txtPeriodo.getText().trim();
        String montoStr = txtMontoBase.getText().trim();
        if (periodo.isBlank() || montoStr.isBlank()) {
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                mostrarAvisoOscuro("Validación de Datos", "El monto base debe ser mayor a cero.", EstilosUI.DORADO_CLARO);
                return;
            }
        } catch (NumberFormatException nfe) {
            mostrarAvisoOscuro("Error de Entrada", "Monto numérico no válido.", EstilosUI.ROJO_ERROR_TEXTO);
            return;
        }

        btnGenerarCuotas.setEnabled(false);
        etiquetaMensajeEstado.setText("Generando cuotas para departamentos sin cuota abierta en " + periodo + "...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        final String periodoFinal = periodo;
        final double montoFinal = monto;
        final OverlayProgreso overlay = OverlayProgreso.mostrar(this, "Generando Facturación Masiva de Cuotas...", EstilosUI.ACENTO_COBRANZAS);

        SwingWorker<List<CuotaDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CuotaDTO> doInBackground() {
                return fachada.generarCuotasMasivas(periodoFinal, montoFinal);
            }

            @Override
            protected void done() {
                if (overlay != null) {
                    overlay.ocultar(null);
                }
                btnGenerarCuotas.setEnabled(true);
                try {
                    List<CuotaDTO> generadas = get();
                    cargarDatosTabla();
                    int totalCreadas = (generadas != null) ? generadas.size() : 0;

                    if (totalCreadas == 0) {
                        mostrarAvisoOscuro("Generación Masiva de Cuotas",
                                "No se generaron cuotas nuevas. Todos los departamentos ya contaban con una cuota abierta para el periodo " + periodoFinal + ".",
                                EstilosUI.DORADO_CLARO);
                        etiquetaMensajeEstado.setText("Facturación completada: 0 cuotas duplicadas en " + periodoFinal + ".");
                        etiquetaMensajeEstado.setForeground(EstilosUI.DORADO_CLARO);
                    } else {
                        mostrarAvisoOscuro("Generación Masiva de Cuotas",
                                "Se generaron exitosamente " + totalCreadas + " cuotas nuevas para el periodo " + periodoFinal +
                                        "\nregistradas y sincronizadas en la contabilidad general del condominio.",
                                EstilosUI.VERDE_EXITO);
                        etiquetaMensajeEstado.setText("Se crearon " + totalCreadas + " nuevas cuotas para el periodo " + periodoFinal + ".");
                        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelCobranzas.this, "Error al generar cuotas masivas", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private void ejecutarNotificarMorosos() {
        OverlayProgreso overlay = OverlayProgreso.mostrar(this, "Enviando Notificaciones a Morosos...", EstilosUI.ACENTO_COBRANZAS);
        try {
            GestorNotificaciones gestor = new GestorNotificaciones();
            gestor.suscribir(new NotificadorResidente());

            int morososCount = 0;
            for (CuotaDTO c : cuotasCargadas) {
                if ("PENDIENTE".equalsIgnoreCase(c.getEstado()) || "PARCIAL".equalsIgnoreCase(c.getEstado()) || "VENCIDO".equalsIgnoreCase(c.getEstado())) {
                    gestor.notificar("Recordatorio de pago pendiente para el departamento: " + c.getNumeroDepartamento() + " por S/ " + c.getMontoFinal());
                    morososCount++;
                }
            }

            if (overlay != null) {
                overlay.ocultar(null);
            }
            mostrarAvisoOscuro("Notificaciones Enviadas",
                    "Se emitieron notificaciones electrónicas a " + morososCount + " residentes con saldo pendiente.\n(Operación de cobranza vecinal: se registraron en bitácora).",
                    EstilosUI.VERDE_EXITO);
            etiquetaMensajeEstado.setText("Recordatorios emitidos a " + morososCount + " residentes morosos.");
            etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
        } catch (Exception ex) {
            if (overlay != null) {
                overlay.ocultar(null);
            }
            ManejadorErroresUI.mostrarError(this, "Error al enviar notificaciones a morosos", ex);
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
        }
    }

    private void mostrarAvisoOscuro(String titulo, String mensaje, Color colorAcento) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(460, 200);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelMsg = new JPanel(new BorderLayout(8, 8));
        panelMsg.setBackground(EstilosUI.FONDO_TARJETA);
        panelMsg.setBorder(BorderFactory.createCompoundBorder(
                EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA),
                new EmptyBorder(16, 18, 16, 18)
        ));

        String msgSanitizado = (mensaje != null) ? mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") : "";
        JLabel lbl = new JLabel("<html><body style='width: 380px;'>" + msgSanitizado + "</body></html>");
        lbl.setFont(EstilosUI.FUENTE_REGULAR);
        lbl.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        panelMsg.add(lbl, BorderLayout.CENTER);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panelBtn.setOpaque(false);
        JButton btnOk = new JButton("Aceptar");
        EstilosUI.estilizarBoton(btnOk, colorAcento != null ? colorAcento : EstilosUI.ACENTO_COBRANZAS);
        btnOk.addActionListener(e -> dialogo.dispose());
        panelBtn.add(btnOk);

        dialogo.add(panelMsg, BorderLayout.CENTER);
        dialogo.add(panelBtn, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }
}
