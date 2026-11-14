package com.condominio.gui.paneles;

import com.condominio.gui.util.CampoPlaceholder;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroAlfabetico;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.RenderTipoOperacion;
import com.condominio.gui.util.Validaciones;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Panel interactivo para el Módulo de Operaciones (Incidentes y Reservas de Áreas Comunes).
@Slf4j
public class PanelOperaciones extends PanelPlantillaBase {

    private final CobranzaFachada fachada;

    // Selector general de operación
    private JComboBox<String> comboTipoOperacion;
    private JPanel panelCards;
    private CardLayout cardLayout;

    // Campos para Incidente
    private JTextField txtDescripcionIncidente;
    private JComboBox<String> comboPrioridadIncidente;
    private JTextField txtReportadoPor;

    // Tarjeta modular para Reserva de Área Común
    private TarjetaReservaAreaComun tarjetaReserva;

    // Botones
    private JButton btnRegistrarOperacion;
    private JButton btnRefrescar;
    private JButton btnDesborde;

    public PanelOperaciones(CobranzaFachada fachada) {
        super("MÓDULO INTERACTIVO DE OPERACIONES Y GESTIÓN CONDOMINAL",
                "Registro y seguimiento de incidentes y reservas de áreas comunes.");
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();
        if (tarjetaReserva != null) {
            tarjetaReserva.cargarDatosServicio(this.fachada);
        }
        cargarDatosTabla();
    }

    public PanelOperaciones() {
        this(new CobranzaFachada());
    }

    @Override
    protected String[] inicializarColumnas() {
        return new String[]{
                "ID", "Tipo Operación", "Detalle / Instalación", "Fecha", "Horario / Prioridad", "Estado"
        };
    }

    @Override
    protected void inicializarComponentesFormulario(JPanel panelFormulario) {
        panelFormulario.setLayout(new BorderLayout(10, 10));

        // Selector superior del tipo de operación
        JPanel panelSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panelSelector.setOpaque(false);
        panelSelector.add(EstilosUI.crearEtiquetaCampo("Seleccione la Operación a Realizar:"));

        if (com.condominio.modelo.seguridad.SesionActual.esConserje()) {
            comboTipoOperacion = new JComboBox<>(new String[]{
                    "Reportar Incidente"
            });
        } else {
            comboTipoOperacion = new JComboBox<>(new String[]{
                    "Reportar Incidente",
                    "Reservar Área Común"
            });
        }
        comboTipoOperacion.setEditable(false);
        EstilosUI.estilizarCombo(comboTipoOperacion, EstilosUI.ACENTO_OPERACIONES);
        EstilosUI.configurarComboPrototipoYTooltip(comboTipoOperacion, "Reservar Área Común");
        comboTipoOperacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboTipoOperacion.setPreferredSize(new Dimension(210, 32));
        comboTipoOperacion.setToolTipText("Conmuta el formulario entre reporte de incidentes y reservas de áreas");

        comboTipoOperacion.addActionListener(e -> {
            String seleccion = (String) comboTipoOperacion.getSelectedItem();
            if (cardLayout != null && panelCards != null && seleccion != null) {
                cardLayout.show(panelCards, seleccion);
                // Regla: al cambiar el combo se limpian los campos de la tarjeta entrante
                if ("Reportar Incidente".equals(seleccion)) {
                    limpiarCamposIncidente();
                } else if (tarjetaReserva != null) {
                    tarjetaReserva.limpiar();
                }
            }
        });
        panelSelector.add(comboTipoOperacion);

        panelFormulario.add(panelSelector, BorderLayout.NORTH);

        // Panel con CardLayout para alternar entre Incidente y Reserva
        cardLayout = new CardLayout();
        panelCards = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_OPERACIONES, 12);
        panelCards.setLayout(cardLayout);
        panelCards.setBorder(new EmptyBorder(8, 12, 8, 12));

        // TARJETA 1: REPORTAR INCIDENTE

        JPanel cardIncidente = new JPanel(new GridBagLayout());
        cardIncidente.setOpaque(false);
        GridBagConstraints gbcInc = new GridBagConstraints();
        gbcInc.insets = new Insets(4, 6, 4, 6);
        gbcInc.fill = GridBagConstraints.HORIZONTAL;

        gbcInc.gridx = 0; gbcInc.gridy = 0;
        cardIncidente.add(EstilosUI.crearEtiquetaCampo("Descripción del Incidente:"), gbcInc);

        gbcInc.gridx = 1; gbcInc.gridy = 0;
        cardIncidente.add(EstilosUI.crearEtiquetaCampo("Prioridad:"), gbcInc);

        gbcInc.gridx = 2; gbcInc.gridy = 0;
        cardIncidente.add(EstilosUI.crearEtiquetaCampo("Reportado Por:"), gbcInc);

        // Inputs Incidente
        gbcInc.gridx = 0; gbcInc.gridy = 1;
        txtDescripcionIncidente = EstilosUI.crearCampoPlaceholder("Ej: Fuga de agua en cisterna", EstilosUI.ACENTO_OPERACIONES);
        txtDescripcionIncidente.setPreferredSize(new Dimension(360, 32));
        txtDescripcionIncidente.setToolTipText("Detalle del desperfecto o eventualidad (5 a 200 caracteres)");
        cardIncidente.add(txtDescripcionIncidente, gbcInc);

        gbcInc.gridx = 1; gbcInc.gridy = 1;
        comboPrioridadIncidente = new JComboBox<>(new String[]{"BAJA", "MEDIA", "ALTA"});
        comboPrioridadIncidente.setEditable(false);
        EstilosUI.estilizarCombo(comboPrioridadIncidente, EstilosUI.ACENTO_OPERACIONES);
        EstilosUI.configurarComboPrototipoYTooltip(comboPrioridadIncidente, "MEDIA");
        comboPrioridadIncidente.setPreferredSize(new Dimension(130, 32));
        comboPrioridadIncidente.setToolTipText("Nivel de criticidad del incidente");
        cardIncidente.add(comboPrioridadIncidente, gbcInc);

        gbcInc.gridx = 2; gbcInc.gridy = 1;
        txtReportadoPor = EstilosUI.crearCampoPlaceholder("Ej: Conserjería", EstilosUI.ACENTO_OPERACIONES);
        FiltroAlfabetico.aplicar(txtReportadoPor, 50);
        txtReportadoPor.setPreferredSize(new Dimension(180, 32));
        txtReportadoPor.setToolTipText("Nombre de quien reporta la novedad (letras y espacios, 2 a 50 caracteres)");
        cardIncidente.add(txtReportadoPor, gbcInc);

        panelCards.add(cardIncidente, "Reportar Incidente");

        // TARJETA 2: RESERVAR ÁREA COMÚN (NUEVA TARJETA MODULAR)

        tarjetaReserva = new TarjetaReservaAreaComun();
        panelCards.add(tarjetaReserva, "Reservar Área Común");

        panelFormulario.add(panelCards, BorderLayout.CENTER);

        // Panel de Botones al sur del formulario (Máximo 3 botones visibles)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelAcciones.setOpaque(false);

        // Botón 1 de 3: Registrar Operación
        btnRegistrarOperacion = new JButton("Registrar Operación");
        EstilosUI.estilizarBoton(btnRegistrarOperacion, EstilosUI.ACENTO_OPERACIONES);
        btnRegistrarOperacion.setToolTipText("Valida la tarjeta activa y persiste el incidente o la reserva");
        panelAcciones.add(btnRegistrarOperacion);

        // Botón 2 de 3: Refrescar
        btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBotonSecundario(btnRefrescar, EstilosUI.ACENTO_OPERACIONES);
        btnRefrescar.setToolTipText("Recarga el listado de operaciones y actualiza opciones");
        panelAcciones.add(btnRefrescar);

        // Botón 3 de 3: Menú de Desborde "..."
        btnDesborde = new JButton("...");
        EstilosUI.estilizarBotonSecundario(btnDesborde, EstilosUI.ACENTO_OPERACIONES);
        btnDesborde.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDesborde.setPreferredSize(new Dimension(38, 32));
        btnDesborde.setToolTipText("Más opciones: Exportar CSV y acciones de bitácora (Desborde)");
        btnDesborde.addActionListener(e -> mostrarMenuDesborde(btnDesborde, 0, btnDesborde.getHeight()));
        panelAcciones.add(btnDesborde);

        panelFormulario.add(panelAcciones, BorderLayout.SOUTH);
    }

    @Override
    protected void inicializarAcciones() {
        btnRegistrarOperacion.addActionListener(e -> ejecutarRegistroOperacion());
        btnRefrescar.addActionListener(e -> {
            if (tarjetaReserva != null) {
                tarjetaReserva.cargarDatosServicio(fachada);
            }
            cargarDatosTabla();
        });

        // Menú contextual en tabla (clic derecho con selección automática) y doble clic
        tablaDatos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }

            private void procesarPopup(MouseEvent e) {
                int fila = tablaDatos.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaDatos.getRowCount()) {
                    tablaDatos.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualFila(e.getX(), e.getY(), fila);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    int fila = tablaDatos.rowAtPoint(e.getPoint());
                    if (fila >= 0 && fila < tablaDatos.getRowCount()) {
                        tablaDatos.setRowSelectionInterval(fila, fila);
                        abrirDetalleOperacion(fila);
                    }
                }
            }
        });

        // Alineación visual y renderizadores de la tabla
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaDatos.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaDatos.getColumnModel().getColumn(1).setCellRenderer(new RenderTipoOperacion());
        tablaDatos.getColumnModel().getColumn(3).setCellRenderer(renderCentro);
        tablaDatos.getColumnModel().getColumn(4).setCellRenderer(renderCentro);
        tablaDatos.getColumnModel().getColumn(5).setCellRenderer(new RenderTipoOperacion());
    }

    @Override
    public void cargarDatosTabla() {
        if (fachada == null) return;

        establecerEstado("Sincronizando operaciones (incidentes y reservas) con la base de datos...", EstilosUI.AZUL_INFO);

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                try {
                    fachada.verificarYFinalizarReservasVencidas();
                } catch (Exception ignored) {}
                List<Object[]> filas = new ArrayList<>();

                // 1. Cargar incidentes con ID prefijado INC-
                List<Incidente> incidentes = fachada.listarIncidentes();
                if (incidentes != null) {
                    for (Incidente inc : incidentes) {
                        String prioridadInc = determinarPrioridadIncidente(inc);
                        String estadoInc = determinarEstadoIncidente(inc);
                        String rep = (inc.getReportadoPor() != null && !inc.getReportadoPor().isBlank()) ? inc.getReportadoPor() : "Conserjería";

                        filas.add(new Object[]{
                                "INC-" + inc.getId(),
                                "INCIDENTE",
                                inc.getDescripcion(),
                                inc.getFecha() != null ? inc.getFecha().toString() : "-",
                                prioridadInc + " (Rep: " + rep + ")",
                                estadoInc
                        });
                    }
                }

                // 2. Cargar reservas con ID prefijado RES-
                List<ReservaArea> reservas = fachada.listarReservas();
                if (reservas != null) {
                    for (ReservaArea res : reservas) {
                        String nomArea = (res.getAreaComun() != null && res.getAreaComun().getNombre() != null)
                                ? res.getAreaComun().getNombre()
                                : "Área Común";
                        String nomProp = (res.getPropietario() != null)
                                ? res.getPropietario().getNombre() + " " + (res.getPropietario().getApellidos() != null ? res.getPropietario().getApellidos() : "")
                                : "Vecino";

                        filas.add(new Object[]{
                                "RES-" + res.getId(),
                                "RESERVA",
                                nomArea + " - Titular: " + nomProp.trim(),
                                res.getFechaReserva() != null ? res.getFechaReserva().toString() : "-",
                                (res.getHoraInicio() != null ? res.getHoraInicio() : "10:00") + " - " + (res.getHoraFin() != null ? res.getHoraFin() : "14:00"),
                                res.getEstado() != null ? res.getEstado() : "CONFIRMADA"
                        });
                    }
                }

                return filas;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> filas = get();
                    modeloTabla.setRowCount(0);
                    int total = (filas != null) ? filas.size() : 0;
                    if (filas != null) {
                        for (Object[] fila : filas) {
                            modeloTabla.addRow(fila);
                        }
                    }
                    if (scrollTabla != null) {
                        scrollTabla.getVerticalScrollBar().setValue(0);
                    }
                    if (tablaDatos != null) {
                        tablaDatos.setPreferredScrollableViewportSize(new Dimension(800, 142));
                    }
                    if (btnRefrescar != null) {
                        btnRefrescar.setEnabled(true);
                    }
                    actualizarContadorRegistros(total, total);
                    establecerEstado("Módulo sincronizado: " + total + " registros.", EstilosUI.VERDE_EXITO);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    ManejadorErroresUI.mostrarError(PanelOperaciones.this, "Operación interrumpida durante la carga", ie);
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelOperaciones.this, "Error al sincronizar datos operacionales", causa);
                    establecerEstado("Error: " + causa.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private String determinarPrioridadIncidente(Incidente inc) {
        if (inc == null || inc.getEstado() == null) return "MEDIA";
        String st = inc.getEstado().trim().toUpperCase();
        if (st.equals("ALTA") || st.equals("MEDIA") || st.equals("BAJA") || st.equals("URGENTE")) {
            return st;
        }
        String desc = (inc.getDescripcion() != null) ? inc.getDescripcion().toLowerCase() : "";
        if (desc.contains("ascensor") || desc.contains("fuga") || desc.contains("urgente") || desc.contains("eléctric")) {
            return "ALTA";
        }
        return "MEDIA";
    }

    private String determinarEstadoIncidente(Incidente inc) {
        if (inc == null || inc.getEstado() == null) return "ABIERTO";
        String st = inc.getEstado().trim().toUpperCase();
        if (st.equals("ALTA") || st.equals("MEDIA") || st.equals("BAJA") || st.equals("URGENTE")) {
            return "ABIERTO";
        }
        return st;
    }

    private void ejecutarRegistroOperacion() {
        String tipoOp = (String) comboTipoOperacion.getSelectedItem();

        if ("Reportar Incidente".equals(tipoOp)) {
            ejecutarRegistroIncidente();
        } else {
            ejecutarRegistroReserva();
        }
    }

    private void ejecutarRegistroIncidente() {
        String descripcionProblema = txtDescripcionIncidente.getText().trim();
        String prioridadProblema = (String) comboPrioridadIncidente.getSelectedItem();
        String reportadoPor = txtReportadoPor.getText().trim();

        // 1. Validación de descripción (5 a 200 caracteres)
        try {
            Validaciones.validarDescripcionIncidente(descripcionProblema);
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            txtDescripcionIncidente.requestFocusInWindow();
            return;
        }

        // 2. Validación de reportado por (2 a 50 caracteres alfabéticos)
        try {
            Validaciones.validarReportadoPor(reportadoPor);
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            txtReportadoPor.requestFocusInWindow();
            return;
        }

        // 3. Persistencia segura mediante SwingWorker
        btnRegistrarOperacion.setEnabled(false);
        establecerEstado("Registrando reporte de incidente...", EstilosUI.AZUL_INFO);

        final String descripcionFinal = descripcionProblema;
        final String prioridadFinal = prioridadProblema != null ? prioridadProblema : "MEDIA";
        final String reportadoPorFinal = reportadoPor;

        SwingWorker<Void, Void> hiloTrabajador = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.reportarIncidente(descripcionFinal, prioridadFinal, reportadoPorFinal);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrarOperacion.setEnabled(true);
                try {
                    get();
                    ManejadorErroresUI.mostrarExito(PanelOperaciones.this,
                            "Incidente registrado correctamente en el sistema.\nPrioridad: " + prioridadFinal + "\nReportado por: " + reportadoPorFinal,
                            "Incidente Registrado");

                    txtDescripcionIncidente.setText("");
                    cargarDatosTabla();
                    com.condominio.gui.animacion.AnimadorToast.mostrar("REPORTE REGISTRADO", "Incidente registrado exitosamente (" + prioridadFinal + ")", EstilosUI.AMBAR_AVISO);
                    com.condominio.gui.animacion.DestelloFila.resaltar(tablaDatos, tablaDatos.getRowCount() - 1, EstilosUI.ACENTO_OPERACIONES);
                    establecerEstado("Incidente reportado exitosamente con prioridad " + prioridadFinal + ".", EstilosUI.VERDE_EXITO);
                } catch (Exception ex) {
                    Throwable causaError = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelOperaciones.this, "No se pudo registrar el incidente", causaError);
                    establecerEstado("Error: " + causaError.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
                    txtDescripcionIncidente.requestFocusInWindow();
                }
            }
        };
        hiloTrabajador.execute();
    }

    private void ejecutarRegistroReserva() {
        if (tarjetaReserva == null) return;

        String areaSeleccionada = (String) tarjetaReserva.getComboAreaComun().getSelectedItem();
        String propietarioSeleccionado = (String) tarjetaReserva.getComboPropietario().getSelectedItem();

        if (areaSeleccionada == null || areaSeleccionada.isBlank()) {
            establecerEstado("Error: Debe seleccionar un área común para la reserva.", EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getComboAreaComun().requestFocusInWindow();
            return;
        }

        if (propietarioSeleccionado == null || propietarioSeleccionado.isBlank()) {
            establecerEstado("Error: Debe seleccionar un residente titular de la reserva.", EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getComboPropietario().requestFocusInWindow();
            return;
        }

        // 1. Validación de Fecha
        LocalDate fechaReserva;
        try {
            fechaReserva = tarjetaReserva.obtenerFecha();
            Validaciones.validarFechaReserva(fechaReserva);
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getFtfFecha().requestFocusInWindow();
            return;
        }

        // 2. Validación de Horas
        LocalTime horaInicio;
        LocalTime horaFin;
        try {
            horaInicio = tarjetaReserva.obtenerHoraInicio();
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getFtfHoraInicio().requestFocusInWindow();
            return;
        }

        try {
            horaFin = tarjetaReserva.obtenerHoraFin();
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getFtfHoraFin().requestFocusInWindow();
            return;
        }

        try {
            Validaciones.validarHorarioReserva(horaInicio, horaFin);
        } catch (IllegalArgumentException ex) {
            establecerEstado("Error: " + ex.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getFtfHoraFin().requestFocusInWindow();
            return;
        }

        // 3. Regla de negocio: Validación de Solapamiento contra reservas CONFIRMADA
        if (fachada != null && fachada.existeSolapamientoReserva(areaSeleccionada, fechaReserva, horaInicio, horaFin)) {
            establecerEstado("Error: Ya existe una reserva confirmada en ese horario", EstilosUI.ROJO_ERROR_TEXTO);
            tarjetaReserva.getFtfHoraInicio().requestFocusInWindow();
            ManejadorErroresUI.mostrarAdvertencia(this, "Ya existe una reserva confirmada en ese horario para " + areaSeleccionada + " el " + fechaReserva + ".");
            return;
        }

        int cantidadInvitados = tarjetaReserva.obtenerCantidadPersonas();

        // 4. Registro seguro con SwingWorker evitando doble clic
        btnRegistrarOperacion.setEnabled(false);
        establecerEstado("Confirmando reserva de " + areaSeleccionada + "...", EstilosUI.AZUL_INFO);

        final String areaFinal = areaSeleccionada;
        final String propietarioFinal = propietarioSeleccionado;
        final LocalDate fechaFinal = fechaReserva;
        final LocalTime horaInicioFinal = horaInicio;
        final LocalTime horaFinFinal = horaFin;
        final int cantidadInvitadosFinal = cantidadInvitados;

        SwingWorker<Void, Void> hiloTrabajador = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                fachada.reservarArea(areaFinal, propietarioFinal, fechaFinal, horaInicioFinal, horaFinFinal, cantidadInvitadosFinal);
                return null;
            }

            @Override
            protected void done() {
                btnRegistrarOperacion.setEnabled(true);
                try {
                    get();
                    ManejadorErroresUI.mostrarExito(PanelOperaciones.this,
                            "Reserva confirmada exitosamente para " + areaFinal + " el " + fechaFinal +
                                    "\nHorario: " + horaInicioFinal + " a " + horaFinFinal + "\nTitular: " + propietarioFinal,
                            "Reserva Exitosa");

                    limpiarFormulario();
                    cargarDatosTabla();
                    com.condominio.gui.animacion.AnimadorToast.mostrar("RESERVA CONFIRMADA", "Reserva confirmada exitosamente: " + areaFinal, new Color(0x38, 0xBD, 0xF8));
                    com.condominio.gui.animacion.DestelloFila.resaltar(tablaDatos, tablaDatos.getRowCount() - 1, EstilosUI.ACENTO_OPERACIONES);
                    establecerEstado("Reserva para " + areaFinal + " confirmada exitosamente.", EstilosUI.VERDE_EXITO);
                } catch (Exception ex) {
                    Throwable causaError = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelOperaciones.this, "No se pudo concretar la reserva", causaError);
                    establecerEstado("Error: " + causaError.getMessage(), EstilosUI.ROJO_ERROR_TEXTO);
                    tarjetaReserva.getFtfHoraInicio().requestFocusInWindow();
                }
            }
        };
        hiloTrabajador.execute();
    }

    private void limpiarCamposIncidente() {
        if (txtDescripcionIncidente != null) txtDescripcionIncidente.setText("");
        if (txtReportadoPor != null) txtReportadoPor.setText("");
    }

    @Override
    public void limpiarFormulario() {
        limpiarCamposIncidente();
        if (tarjetaReserva != null) {
            tarjetaReserva.limpiar();
        }
    }

    private void mostrarMenuContextualFila(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaDatos.getRowCount()) return;
        Object idObj = tablaDatos.getValueAt(fila, 0);
        Object tipoObj = tablaDatos.getValueAt(fila, 1);
        Object detObj = tablaDatos.getValueAt(fila, 2);
        Object fecObj = tablaDatos.getValueAt(fila, 3);
        Object horObj = tablaDatos.getValueAt(fila, 4);
        Object estObj = tablaDatos.getValueAt(fila, 5);

        String idStr = (idObj != null) ? idObj.toString() : "";
        String tipoStr = (tipoObj != null) ? tipoObj.toString().toUpperCase() : "";

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_OPERACIONES);
        boolean esAdmin = com.condominio.modelo.seguridad.SesionActual.esAdministrador();
        boolean esIncidente = idStr.startsWith("INC") || tipoStr.contains("INCIDENTE");

        if (esIncidente) {
            // Incidente: "Cambiar estado" (Solo Administrador) y "Ver detalle"
            if (esAdmin) {
                JMenuItem itemCambiar = EstilosUI.crearItemMenuOscuro("Cambiar estado (Atendido / En Proceso)", EstilosUI.ACENTO_OPERACIONES, EstilosUI.ACENTO_OPERACIONES);
                itemCambiar.setToolTipText("Actualiza el estado operativo del incidente");
                itemCambiar.addActionListener(e -> {
                    String[] opciones = {"ATENDIDO", "EN PROCESO", "ABIERTO"};
                    String nuevoEstado = (String) JOptionPane.showInputDialog(
                            this,
                            "Seleccione el nuevo estado para el incidente " + idStr + ":",
                            "Cambiar Estado de Incidente",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            opciones,
                            "ATENDIDO"
                    );
                    if (nuevoEstado != null && !nuevoEstado.isBlank()) {
                        tablaDatos.setValueAt(nuevoEstado, fila, 5);
                        ManejadorErroresUI.mostrarExito(this, "El incidente " + idStr + " ha cambiado su estado a " + nuevoEstado + ".", "Estado Actualizado");
                        establecerEstado("Incidente " + idStr + " actualizado a " + nuevoEstado + ".", EstilosUI.VERDE_EXITO);
                    }
                });
                popup.add(itemCambiar);
                EstilosUI.agregarSeparadorOscuro(popup);
            }

            JMenuItem itemDetalle = EstilosUI.crearItemMenuOscuro("Ver detalle del incidente", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_OPERACIONES);
            itemDetalle.setToolTipText("Muestra la ficha técnica completa de la novedad");
            itemDetalle.addActionListener(e -> abrirDetalleOperacion(fila));
            popup.add(itemDetalle);

        } else {
            // Reserva: "Confirmar reserva", "Finalizar y Liberar Área", "Cancelar reserva" (Solo Administrador), "Ver detalle"
            if (esAdmin) {
                JMenuItem itemConfirmar = EstilosUI.crearItemMenuOscuro("Confirmar reserva", EstilosUI.VERDE_EXITO, EstilosUI.VERDE_EXITO);
                itemConfirmar.setToolTipText("Establece la reserva en estado CONFIRMADA");
                itemConfirmar.addActionListener(e -> {
                    tablaDatos.setValueAt("CONFIRMADA", fila, 5);
                    ManejadorErroresUI.mostrarExito(this, "La reserva " + idStr + " ha sido CONFIRMADA.", "Reserva Confirmada");
                    establecerEstado("Reserva " + idStr + " confirmada.", EstilosUI.VERDE_EXITO);
                });
                popup.add(itemConfirmar);

                JMenuItem itemFinalizar = EstilosUI.crearItemMenuOscuro("Finalizar Reserva y Liberar Área", EstilosUI.AZUL_INFO, EstilosUI.AZUL_INFO);
                itemFinalizar.setToolTipText("Finaliza la reserva cumplida, libera el área para otros residentes y notifica a Gerencia y Administración");
                itemFinalizar.addActionListener(e -> {
                    Long idNum = null;
                    try {
                        idNum = Long.parseLong(idStr.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                    if (idNum != null && fachada != null) {
                        fachada.finalizarReserva(idNum);
                        tablaDatos.setValueAt("FINALIZADA", fila, 5);
                        com.condominio.gui.animacion.AnimadorToast.mostrar("ÁREA LIBERADA",
                                "Reserva " + idStr + " finalizada. Área común disponible para nuevos turnos. Gerente y Administrador notificados.",
                                EstilosUI.VERDE_EXITO);
                        ManejadorErroresUI.mostrarExito(this,
                                "La reserva " + idStr + " ha sido FINALIZADA exitosamente.\n"
                                        + "El área común se encuentra LIBERADA y DISPONIBLE para que otros residentes puedan seleccionarla.\n"
                                        + "Notificación oficial remitida a Gerencia y Administración.",
                                "Reserva Finalizada y Área Liberada");
                        establecerEstado("Reserva " + idStr + " finalizada. Área común liberada para nuevas reservas.", EstilosUI.VERDE_EXITO);
                        cargarDatosTabla();
                    }
                });
                popup.add(itemFinalizar);

                JMenuItem itemCancelar = EstilosUI.crearItemMenuOscuro("Cancelar reserva", EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
                itemCancelar.setToolTipText("Cancela la reserva seleccionada");
                itemCancelar.addActionListener(e -> {
                    int conf = JOptionPane.showConfirmDialog(
                            this,
                            "¿Está seguro de que desea cancelar la reserva " + idStr + "?",
                            "Cancelar Reserva",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );
                    if (conf == JOptionPane.YES_OPTION) {
                        tablaDatos.setValueAt("CANCELADA", fila, 5);
                        ManejadorErroresUI.mostrarExito(this, "La reserva " + idStr + " ha sido CANCELADA.", "Reserva Cancelada");
                        establecerEstado("Reserva " + idStr + " cancelada.", EstilosUI.AMBAR_AVISO);
                    }
                });
                popup.add(itemCancelar);
                EstilosUI.agregarSeparadorOscuro(popup);
            }

            JMenuItem itemDetalle = EstilosUI.crearItemMenuOscuro("Ver detalle de la reserva", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_OPERACIONES);
            itemDetalle.setToolTipText("Muestra el detalle del área, titular y horario");
            itemDetalle.addActionListener(e -> abrirDetalleOperacion(fila));
            popup.add(itemDetalle);
        }

        popup.show(tablaDatos, x, y);
    }

    private void abrirDetalleOperacion(int fila) {
        if (fila < 0 || fila >= tablaDatos.getRowCount()) return;
        Object idObj = tablaDatos.getValueAt(fila, 0);
        Object tipObj = tablaDatos.getValueAt(fila, 1);
        Object detObj = tablaDatos.getValueAt(fila, 2);
        Object fecObj = tablaDatos.getValueAt(fila, 3);
        Object horObj = tablaDatos.getValueAt(fila, 4);
        Object estObj = tablaDatos.getValueAt(fila, 5);

        StringBuilder sb = new StringBuilder();
        sb.append("DETALLE DE OPERACIÓN CONDOMINAL\n\n");
        sb.append("Identificador: ").append(idObj != null ? idObj : "-").append("\n");
        sb.append("Tipo: ").append(tipObj != null ? tipObj : "-").append("\n");
        sb.append("Detalle / Instalación: ").append(detObj != null ? detObj : "-").append("\n");
        sb.append("Fecha Calendaria: ").append(fecObj != null ? fecObj : "-").append("\n");
        sb.append("Horario / Prioridad: ").append(horObj != null ? horObj : "-").append("\n");
        sb.append("Estado Actual: ").append(estObj != null ? estObj : "-").append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Ficha de Operación - " + idObj, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarMenuDesborde(Component invocador, int x, int y) {
        JPopupMenu menu = EstilosUI.crearMenuDesbordeOscuro(EstilosUI.ACENTO_OPERACIONES);

        if (com.condominio.modelo.seguridad.SesionActual.puedeExportarCsv()) {
            JMenuItem itemCsv = EstilosUI.crearItemMenuOscuro("Exportar Operaciones CSV", EstilosUI.ACENTO_OPERACIONES, EstilosUI.ACENTO_OPERACIONES);
            itemCsv.setToolTipText("Exporta la bitácora completa de operaciones a formato CSV");
            itemCsv.addActionListener(e -> exportarOperacionesCsv());
            menu.add(itemCsv);
            EstilosUI.agregarSeparadorOscuro(menu);
        }

        JMenuItem itemRefrescar = EstilosUI.crearItemMenuOscuro("Refrescar Bitácora", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_OPERACIONES);
        itemRefrescar.setToolTipText("Recarga el listado de operaciones desde la base de datos");
        itemRefrescar.addActionListener(e -> {
            if (tarjetaReserva != null) {
                tarjetaReserva.cargarDatosServicio(fachada);
            }
            cargarDatosTabla();
        });
        menu.add(itemRefrescar);

        int fila = tablaDatos.getSelectedRow();
        if (fila != -1) {
            EstilosUI.agregarSeparadorOscuro(menu);
            JMenuItem itemDet = EstilosUI.crearItemMenuOscuro("Ver detalle de la fila seleccionada", EstilosUI.TEXTO_PRINCIPAL, EstilosUI.ACENTO_OPERACIONES);
            itemDet.addActionListener(e -> abrirDetalleOperacion(fila));
            menu.add(itemDet);
        }

        menu.show(invocador, x, y);
    }

    private void exportarOperacionesCsv() {
        try {
            com.condominio.service.ExportadorCsvService exp = new com.condominio.service.ExportadorCsvService();
            java.io.File f = exp.exportarTablaCsv(tablaDatos, "Operaciones_Incidentes_Reservas");
            ManejadorErroresUI.mostrarExito(this, "Operaciones exportadas en:\n" + f.getAbsolutePath(), "Exportación Exitosa");
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al exportar CSV", ex);
        }
    }
}
