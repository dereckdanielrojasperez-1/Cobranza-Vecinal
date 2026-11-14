package com.condominio.gui.paneles;

import com.condominio.dto.InmuebleDTO;
import com.condominio.gui.animacion.DestelloFila;
import com.condominio.gui.util.CampoPlaceholder;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroAlfabetico;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.Validaciones;
import com.condominio.modelo.personas.Visita;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

// Panel interactivo para el modulo de Control de Visitas y Accesos a Porteria.
@Slf4j
public class PanelVisitas extends JPanel {

    public static final Color ACENTO_VISITAS = new Color(0x38, 0xBD, 0xF8); // Cyan suave #38BDF8
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CobranzaFachada fachada;
    private JTable tablaVisitas;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollTabla;
    private JLabel lblContadorRegistros;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtNombreVisitante;
    private JTextField txtDni;
    private JComboBox<String> comboDepartamento;
    private JComboBox<String> comboMotivo;
    private JButton btnRegistrarIngreso;
    private JButton btnRegistrarSalida;
    private JButton btnRefrescar;
    private JLabel etiquetaMensajeEstado;

    public PanelVisitas(CobranzaFachada fachada) {
        this.fachada = fachada;
        inicializarComponentes();
        cargarDepartamentos();
        cargarDatosTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. BANNER SUPERIOR INFORMATIVO

        JPanel panelBanner = EstilosUI.crearPanelBanner(ACENTO_VISITAS);

        JLabel tituloBanner = new JLabel("CONTROL DE VISITAS Y ACCESOS A PORTERÍA");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(ACENTO_VISITAS);

        JLabel subtituloBanner = new JLabel("Registro en garita de visitas, deliveries, personal técnico y contratistas temporales.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. TABLA CENTRAL DE VISITAS Y REGISTRO EN GARITA

        String[] columnas = {
                "ID", "Visitante", "DNI", "Departamento", "Motivo", "Fecha / Hora Ingreso", "Fecha / Hora Salida", "Estado"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVisitas = EstilosUI.crearTablaEstilizada(modeloTabla, ACENTO_VISITAS);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaVisitas.setRowSorter(sorter);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaVisitas.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaVisitas.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaVisitas.getColumnModel().getColumn(3).setCellRenderer(renderCentro);
        tablaVisitas.getColumnModel().getColumn(5).setCellRenderer(renderCentro);
        tablaVisitas.getColumnModel().getColumn(6).setCellRenderer(renderCentro);
        tablaVisitas.getColumnModel().getColumn(7).setCellRenderer(new RenderEstadoVisita());

        tablaVisitas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaVisitas.getSelectedRow();
                if (fila != -1) {
                    Object visitante = tablaVisitas.getValueAt(fila, 1);
                    Object depto = tablaVisitas.getValueAt(fila, 3);
                    Object estado = tablaVisitas.getValueAt(fila, 7);
                    etiquetaMensajeEstado.setText("Visitante seleccionado: " + visitante + " (Depto " + depto + ") - Estado: " + estado);
                    etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);
                }
            }
        });

        tablaVisitas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) procesarPopup(e);
            }
            private void procesarPopup(MouseEvent e) {
                int fila = tablaVisitas.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaVisitas.getRowCount()) {
                    tablaVisitas.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualVisita(e.getX(), e.getY(), fila);
                }
            }
        });

        scrollTabla = new JScrollPane(tablaVisitas);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollTabla.setPreferredSize(new Dimension(800, 200));

        JPanel panelCentro = new JPanel(new BorderLayout(0, 6));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelFiltroYContador = new JPanel(new BorderLayout());
        panelFiltroYContador.setOpaque(false);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelBusqueda.setOpaque(false);
        JLabel lblBuscar = EstilosUI.crearEtiquetaCampo("Buscar Visitante / DNI / Depto:");
        JTextField txtBuscar = EstilosUI.crearCampoPlaceholder("Ej: Mario, 45892134, 101...", ACENTO_VISITAS);
        txtBuscar.setPreferredSize(new Dimension(220, 28));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                String txt = txtBuscar.getText();
                if (txt == null || txt.trim().isEmpty() || txt.startsWith("Ej:")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt.trim())));
                }
                actualizarContador();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);

        JPanel panelContador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelContador.setOpaque(false);
        lblContadorRegistros = new JLabel("Mostrando 0 de 0 visitas registradas");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContadorRegistros.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelContador.add(lblContadorRegistros);

        panelFiltroYContador.add(panelBusqueda, BorderLayout.WEST);
        panelFiltroYContador.add(panelContador, BorderLayout.EAST);
        panelCentro.add(panelFiltroYContador, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // 3. FORMULARIO INFERIOR DE REGISTRO DE INGRESO Y SALIDA

        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);

        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(ACENTO_VISITAS, 14);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Etiquetas
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Nombre del Visitante:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("DNI / Documento:"), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Departamento a Visitar:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Motivo de Visita:"), gbc);

        // Fila 1: Inputs
        // 1. Nombre Visitante
        gbc.gridx = 0; gbc.gridy = 1;
        txtNombreVisitante = EstilosUI.crearCampoPlaceholder("Ej: Carlos Rojas / Courier", ACENTO_VISITAS);
        FiltroAlfabetico.aplicar(txtNombreVisitante, 60);
        txtNombreVisitante.setPreferredSize(new Dimension(170, 32));
        txtNombreVisitante.setToolTipText("Nombre y apellido del visitante o empresa de mensajería");
        panelFormulario.add(txtNombreVisitante, gbc);

        // 2. DNI (8 dígitos)
        gbc.gridx = 1; gbc.gridy = 1;
        txtDni = EstilosUI.crearCampoPlaceholder("Ej: 8 dígitos", ACENTO_VISITAS);
        FiltroNumerico.aplicar(txtDni, 8);
        txtDni.setPreferredSize(new Dimension(100, 32));
        txtDni.setToolTipText("Documento Nacional de Identidad del visitante (8 dígitos)");
        panelFormulario.add(txtDni, gbc);

        // 3. Departamento
        gbc.gridx = 2; gbc.gridy = 1;
        comboDepartamento = new JComboBox<>(new String[]{"101", "102", "201", "202", "301", "302"});
        comboDepartamento.setEditable(false);
        EstilosUI.estilizarCombo(comboDepartamento, ACENTO_VISITAS);
        comboDepartamento.setPreferredSize(new Dimension(120, 32));
        comboDepartamento.setToolTipText("Departamento de destino en el condominio");
        panelFormulario.add(comboDepartamento, gbc);

        // 4. Motivo
        gbc.gridx = 3; gbc.gridy = 1;
        comboMotivo = new JComboBox<>(new String[]{
                "Visita Familiar",
                "Delivery / Encomienda",
                "Servicio Técnico / Contratista",
                "Mudanza",
                "Reunión de Residentes",
                "Otro"
        });
        comboMotivo.setEditable(false);
        EstilosUI.estilizarCombo(comboMotivo, ACENTO_VISITAS);
        comboMotivo.setPreferredSize(new Dimension(160, 32));
        comboMotivo.setToolTipText("Motivo del ingreso a las instalaciones");
        panelFormulario.add(comboMotivo, gbc);

        // Botón: Registrar Ingreso
        gbc.gridx = 4; gbc.gridy = 1;
        btnRegistrarIngreso = new JButton("Registrar Ingreso");
        EstilosUI.estilizarBoton(btnRegistrarIngreso, ACENTO_VISITAS);
        btnRegistrarIngreso.setToolTipText("Registra la entrada del visitante en portería con la hora actual");
        btnRegistrarIngreso.addActionListener(e -> ejecutarRegistroIngreso());
        panelFormulario.add(btnRegistrarIngreso, gbc);

        // Botón: Marcar Salida
        gbc.gridx = 5; gbc.gridy = 1;
        btnRegistrarSalida = new JButton("Marcar Salida");
        EstilosUI.estilizarBotonSecundario(btnRegistrarSalida, EstilosUI.DORADO_PRINCIPAL);
        btnRegistrarSalida.setToolTipText("Marca la hora de retiro del visitante seleccionado en la tabla");
        btnRegistrarSalida.addActionListener(e -> ejecutarRegistroSalida());
        panelFormulario.add(btnRegistrarSalida, gbc);

        // Botón: Refrescar
        gbc.gridx = 6; gbc.gridy = 1;
        btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBotonSecundario(btnRefrescar, ACENTO_VISITAS);
        btnRefrescar.setToolTipText("Recarga el registro de visitas desde la base de datos");
        btnRefrescar.addActionListener(e -> cargarDatosTabla());
        panelFormulario.add(btnRefrescar, gbc);

        panelInferior.add(panelFormulario, BorderLayout.CENTER);

        // Mensaje de estado inferior
        etiquetaMensajeEstado = new JLabel("Garita de control de accesos activa. Sincronizado con portería.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void actualizarContador() {
        int visibles = tablaVisitas.getRowCount();
        int total = modeloTabla.getRowCount();
        lblContadorRegistros.setText("Mostrando " + visibles + " de " + total + " visitas registradas");
    }

    private void cargarDepartamentos() {
        if (fachada == null) return;
        SwingWorker<List<InmuebleDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InmuebleDTO> doInBackground() {
                return fachada.listarInmuebles();
            }

            @Override
            protected void done() {
                try {
                    poblarComboDepartamentos(get());
                } catch (Exception ex) {
                    log.warn("No se pudo cargar la lista de departamentos para visitas: {}", ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void poblarComboDepartamentos(List<InmuebleDTO> inms) {
        if (inms == null || inms.isEmpty()) return;
        comboDepartamento.removeAllItems();
        for (InmuebleDTO inm : inms) {
            comboDepartamento.addItem(inm.getNumero());
        }
    }

    public void cargarDatosTabla() {
        if (fachada == null) return;

        etiquetaMensajeEstado.setText("Consultando visitas en portería...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return mapearVisitasAFilas(fachada.listarVisitas());
            }

            @Override
            protected void done() {
                try {
                    poblarFilasTablaVisitas(get());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception ee) {
                    Throwable causa = (ee instanceof ExecutionException && ee.getCause() != null) ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelVisitas.this, "Error al sincronizar visitas de portería", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private List<Object[]> mapearVisitasAFilas(List<Visita> visitas) {
        List<Object[]> filas = new ArrayList<>();
        if (visitas == null) return filas;

        for (Visita v : visitas) {
            String fIngreso = (v.getFechaIngreso() != null) ? v.getFechaIngreso().format(FORMATO_FECHA_HORA) : "-";
            String fSalida = (v.getFechaSalida() != null) ? v.getFechaSalida().format(FORMATO_FECHA_HORA) : "-";
            String estado = (v.getFechaSalida() == null) ? "EN EDIFICIO" : "RETIRADO";
            String depto = "General";
            try {
                if (v.getDepartamento() != null && v.getDepartamento().getNumero() != null) {
                    depto = v.getDepartamento().getNumero();
                }
            } catch (Exception ignored) {}

            filas.add(new Object[]{
                    v.getId(),
                    v.getNombre(),
                    v.getDni() != null ? v.getDni() : "-",
                    depto,
                    v.getMotivo() != null ? v.getMotivo() : "-",
                    fIngreso,
                    fSalida,
                    estado
            });
        }
        return filas;
    }

    private void poblarFilasTablaVisitas(List<Object[]> filas) {
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
        actualizarContador();
        etiquetaMensajeEstado.setText("Garita sincronizada: " + total + " registros de visitas.");
        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
    }

    private void mostrarMenuContextualVisita(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaVisitas.getRowCount()) return;
        int filaModelo = tablaVisitas.convertRowIndexToModel(fila);
        Object idObj = modeloTabla.getValueAt(filaModelo, 0);
        Object nomObj = modeloTabla.getValueAt(filaModelo, 1);
        Object estObj = modeloTabla.getValueAt(filaModelo, 7);
        if (idObj == null) return;
        Long id = Long.valueOf(idObj.toString());
        String nombre = (nomObj != null) ? nomObj.toString() : "";
        String estado = (estObj != null) ? estObj.toString() : "";

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(ACENTO_VISITAS);

        if (!"RETIRADO".equalsIgnoreCase(estado)) {
            JMenuItem itemSalida = EstilosUI.crearItemMenuOscuro("Marcar Salida de Portería", EstilosUI.DORADO_PRINCIPAL, EstilosUI.DORADO_PRINCIPAL);
            itemSalida.addActionListener(e -> ejecutarRegistroSalida());
            popup.add(itemSalida);
        }

        JMenuItem itemEliminar = EstilosUI.crearItemMenuOscuro("Anular / Eliminar Registro de Visita", EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
        itemEliminar.addActionListener(e -> ejecutarEliminarVisita(id, nombre));
        popup.add(itemEliminar);

        popup.show(tablaVisitas, x, y);
    }

    private void ejecutarEliminarVisita(Long id, String nombre) {
        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Desea anular y eliminar el registro de visita #" + id + " (" + nombre + ") de la garita?",
                "Confirmar Eliminación de Visita (CRUD Delete)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (resp == JOptionPane.YES_OPTION) {
            try {
                boolean ok = fachada.eliminarVisita(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registro de visita eliminado correctamente.", "Operación Exitosa (CRUD Delete)", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosTabla();
                } else {
                    ManejadorErroresUI.mostrarAdvertencia(this, "No se pudo eliminar el registro de visita.");
                }
            } catch (Exception ex) {
                ManejadorErroresUI.mostrarError(this, "Error al eliminar visita", ex);
            }
        }
    }

    private void ejecutarRegistroIngreso() {
        String nombre = txtNombreVisitante.getText().trim();
        String dni = txtDni.getText().trim();
        String depto = (String) comboDepartamento.getSelectedItem();
        String motivo = (String) comboMotivo.getSelectedItem();

        if (nombre.isEmpty() || nombre.startsWith("Ej:")) {
            etiquetaMensajeEstado.setText("Error: Ingrese el nombre completo del visitante o repartidor.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtNombreVisitante.requestFocusInWindow();
            return;
        }

        try {
            Validaciones.validarDni(dni);
        } catch (IllegalArgumentException ex) {
            etiquetaMensajeEstado.setText("Error: " + ex.getMessage());
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            txtDni.requestFocusInWindow();
            return;
        }

        btnRegistrarIngreso.setEnabled(false);
        etiquetaMensajeEstado.setText("Registrando acceso en garita...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() {
                return fachada.registrarIngresoVisita(nombre, dni, depto, motivo);
            }

            @Override
            protected void done() {
                btnRegistrarIngreso.setEnabled(true);
                try {
                    Visita registrada = get();
                    ManejadorErroresUI.mostrarExito(
                            PanelVisitas.this,
                            "Ingreso registrado correctamente en portería:\n" +
                                    "Visitante: " + registrada.getNombre() + "\n" +
                                    "DNI: " + registrada.getDni() + "\n" +
                                    "Destino: Depto " + depto + "\n" +
                                    "Motivo: " + motivo,
                            "Ingreso a Condominio"
                    );

                    txtNombreVisitante.setText("");
                    txtDni.setText("");
                    cargarDatosTabla();
                    DestelloFila.resaltar(tablaVisitas, 0, ACENTO_VISITAS);
                    etiquetaMensajeEstado.setText("Acceso registrado: " + nombre + " ingresó al departamento " + depto + ".");
                    etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelVisitas.this, "No se pudo registrar el ingreso de la visita", causa);
                    etiquetaMensajeEstado.setText("Error: " + causa.getMessage());
                    etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
                }
            }
        };
        worker.execute();
    }

    private void ejecutarRegistroSalida() {
        int fila = tablaVisitas.getSelectedRow();
        if (fila == -1) {
            etiquetaMensajeEstado.setText("Seleccione una visita en la tabla para marcar su salida.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            return;
        }

        int filaModelo = tablaVisitas.convertRowIndexToModel(fila);
        Long idVisita = (Long) modeloTabla.getValueAt(filaModelo, 0);
        String visitante = (String) modeloTabla.getValueAt(filaModelo, 1);
        String estado = (String) modeloTabla.getValueAt(filaModelo, 7);

        if ("RETIRADO".equalsIgnoreCase(estado)) {
            etiquetaMensajeEstado.setText("El visitante " + visitante + " ya tiene registrada su salida.");
            etiquetaMensajeEstado.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            return;
        }

        btnRegistrarSalida.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return fachada.registrarSalidaVisita(idVisita);
            }

            @Override
            protected void done() {
                btnRegistrarSalida.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        ManejadorErroresUI.mostrarExito(
                                PanelVisitas.this,
                                "Salida registrada con éxito para: " + visitante,
                                "Salida de Portería"
                        );
                        cargarDatosTabla();
                        etiquetaMensajeEstado.setText("Salida confirmada para el visitante " + visitante + ".");
                        etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    ManejadorErroresUI.mostrarError(PanelVisitas.this, "Error al marcar salida", causa);
                }
            }
        };
        worker.execute();
    }

    // Renderer con diseño tipo píldora para estados de visitas (EN EDIFICIO / RETIRADO).
    private static class RenderEstadoVisita extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String val = (value != null) ? value.toString() : "";
            if ("EN EDIFICIO".equalsIgnoreCase(val)) {
                lbl.setForeground(new Color(0x3D, 0xDC, 0x84)); // Verde Menta #3DDC84
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            } else {
                lbl.setForeground(new Color(0x9E, 0xB0, 0xC4)); // Gris Azulado
                lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
            }
            return lbl;
        }
    }
}
