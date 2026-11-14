package com.condominio.gui.paneles;

import com.condominio.gui.DialogoCalculoConsumo;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Panel interactivo para el modulo de Gastos Comunes y Servicios Generales (Agua, Luz, Limpieza).
@Slf4j
public class PanelGastosComunes extends JPanel {

    private final CobranzaFachada fachada;
    private JTable tablaGastos;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> comboTipoGasto;
    private JTextField txtDescripcion;
    private JTextField txtMonto;
    private JTextField txtParametro1; // Lectura Actual (Agua) o Consumo kWh (Luz) o Empresa (Limpieza)
    private JTextField txtParametro2; // Lectura Anterior (Agua)
    private JLabel lblParametro1;
    private JLabel lblParametro2;
    private JLabel etiquetaMensajeEstado;

    public PanelGastosComunes(CobranzaFachada fachada) {
        this.fachada = fachada;
        inicializarComponentes();
        cargarDatosTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. BANNER SUPERIOR INFORMATIVO
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.DORADO_PRINCIPAL);

        JLabel tituloBanner = new JLabel("MÓDULO DE GESTIÓN DE GASTOS COMUNES Y SERVICIOS");
        tituloBanner.setFont(EstilosUI.FUENTE_TITULO);
        tituloBanner.setForeground(EstilosUI.DORADO_CLARO);

        JLabel subtituloBanner = new JLabel("Registro centralizado de egresos, servicios básicos y mantenimiento condominal.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. TABLA CENTRAL DE GASTOS
        String[] columnas = {
                "ID", "Tipo de Gasto", "Descripción", "Monto (S/)", "Fecha Registro", "Detalle Técnico"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaGastos = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.DORADO_PRINCIPAL);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaGastos.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaGastos.getColumnModel().getColumn(1).setCellRenderer(renderCentro);
        tablaGastos.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaGastos.getColumnModel().getColumn(3).setCellRenderer(renderDerecha);

        JScrollPane scrollTabla = new JScrollPane(tablaGastos);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_PRINCIPAL);
        scrollTabla.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        add(scrollTabla, BorderLayout.CENTER);

        // 3. FORMULARIO INFERIOR INTERACTIVO
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);

        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(EstilosUI.DORADO_PRINCIPAL, 14);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Labels
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Tipo de Gasto:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Descripción:"), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Monto Total (S/):"), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        lblParametro1 = EstilosUI.crearEtiquetaCampo("Lectura Actual (m³):");
        panelFormulario.add(lblParametro1, gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        lblParametro2 = EstilosUI.crearEtiquetaCampo("Lectura Anterior (m³):");
        panelFormulario.add(lblParametro2, gbc);

        // Fila 2: Inputs
        gbc.gridx = 0; gbc.gridy = 1;
        comboTipoGasto = new JComboBox<>(new String[]{"AGUA", "LUZ", "LIMPIEZA"});
        comboTipoGasto.setBackground(EstilosUI.FONDO_PRINCIPAL);
        comboTipoGasto.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        comboTipoGasto.setPreferredSize(new Dimension(120, 32));
        comboTipoGasto.addActionListener(e -> actualizarCamposSegunTipo());
        panelFormulario.add(comboTipoGasto, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtDescripcion = EstilosUI.crearCampoTexto();
        txtDescripcion.setText("Consumo mensual");
        txtDescripcion.setPreferredSize(new Dimension(160, 32));
        panelFormulario.add(txtDescripcion, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        txtMonto = EstilosUI.crearCampoTexto();
        txtMonto.setText("350.00");
        txtMonto.setPreferredSize(new Dimension(100, 32));
        panelFormulario.add(txtMonto, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        txtParametro1 = EstilosUI.crearCampoTexto();
        txtParametro1.setText("1420.50");
        txtParametro1.setPreferredSize(new Dimension(120, 32));
        panelFormulario.add(txtParametro1, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        txtParametro2 = EstilosUI.crearCampoTexto();
        txtParametro2.setText("1280.00");
        txtParametro2.setPreferredSize(new Dimension(120, 32));
        panelFormulario.add(txtParametro2, gbc);

        // Boton Registrar Gasto
        gbc.gridx = 5; gbc.gridy = 1;
        JButton btnRegistrar = new JButton("Registrar Gasto");
        EstilosUI.estilizarBoton(btnRegistrar, EstilosUI.DORADO_PRINCIPAL, EstilosUI.DORADO_BORDE);
        btnRegistrar.addActionListener(e -> ejecutarRegistroGasto());
        panelFormulario.add(btnRegistrar, gbc);

        // Boton Asignar Consumo a Departamento
        gbc.gridx = 6; gbc.gridy = 1;
        JButton btnConsumoDepto = new JButton("Consumo por Depto (Agua y Luz)");
        EstilosUI.estilizarBotonSecundario(btnConsumoDepto, EstilosUI.ACENTO_COBRANZAS);
        btnConsumoDepto.addActionListener(e -> {
            DialogoCalculoConsumo dlg = new DialogoCalculoConsumo(SwingUtilities.getWindowAncestor(this), fachada, this::cargarDatosTabla);
            dlg.setVisible(true);
        });
        panelFormulario.add(btnConsumoDepto, gbc);

        // Boton Refrescar
        gbc.gridx = 7; gbc.gridy = 1;
        JButton btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBoton(btnRefrescar, EstilosUI.FONDO_CABECERA, new Color(71, 85, 105));
        btnRefrescar.addActionListener(e -> cargarDatosTabla());
        panelFormulario.add(btnRefrescar, gbc);

        panelInferior.add(panelFormulario, BorderLayout.CENTER);

        // Mensaje de estado inferior
        etiquetaMensajeEstado = new JLabel("Listo para registrar gastos del condominio.");
        etiquetaMensajeEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaMensajeEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaMensajeEstado, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void actualizarCamposSegunTipo() {
        String tipo = (String) comboTipoGasto.getSelectedItem();
        if ("AGUA".equals(tipo)) {
            lblParametro1.setText("Lectura Actual (m³):");
            lblParametro2.setText("Lectura Anterior (m³):");
            txtParametro1.setText("1420.50");
            txtParametro2.setText("1280.00");
            txtParametro2.setEnabled(true);
            txtDescripcion.setText("Recibo de Agua Sedapal");
        } else if ("LUZ".equals(tipo)) {
            lblParametro1.setText("Consumo (kWh):");
            lblParametro2.setText("(No aplica):");
            txtParametro1.setText("750.0");
            txtParametro2.setText("-");
            txtParametro2.setEnabled(false);
            txtDescripcion.setText("Recibo de Luz del Sur");
        } else {
            lblParametro1.setText("Empresa Contratista:");
            lblParametro2.setText("(No aplica):");
            txtParametro1.setText("CleanService SRL");
            txtParametro2.setText("-");
            txtParametro2.setEnabled(false);
            txtDescripcion.setText("Servicio de Limpieza Áreas Comunes");
        }
    }

    public void cargarDatosTabla() {
        if (fachada == null) return;

        etiquetaMensajeEstado.setText("Sincronizando gastos comunes...");
        etiquetaMensajeEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<List<GastoComun>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<GastoComun> doInBackground() {
                return fachada.listarGastosComunes();
            }

            @Override
            protected void done() {
                try {
                    List<GastoComun> gastos = get();
                    modeloTabla.setRowCount(0);
                    double totalGastos = 0.0;
                    for (GastoComun g : gastos) {
                        String tipo = "GENERAL";
                        String detalle = "-";
                        if (g instanceof GastoAgua agua) {
                            tipo = "AGUA";
                            detalle = String.format("Lectura: %.2f m³ - Ant: %.2f m³ (Consumo: %.2f m³)",
                                    agua.getLecturaActual() != null ? agua.getLecturaActual() : 0.0,
                                    agua.getLecturaAnterior() != null ? agua.getLecturaAnterior() : 0.0,
                                    agua.getConsumoTotal());
                        } else if (g instanceof GastoLuz luz) {
                            tipo = "LUZ";
                            detalle = String.format("Consumo registrado: %.2f kWh",
                                    luz.getConsumoKwh() != null ? luz.getConsumoKwh() : 0.0);
                        } else if (g instanceof GastoLimpieza limp) {
                            tipo = "LIMPIEZA";
                            detalle = "Contrato: " + (limp.getContratoEmpresa() != null ? limp.getContratoEmpresa() : "-");
                        }
                        double monto = g.getMonto() != null ? g.getMonto() : 0.0;
                        totalGastos += monto;

                        modeloTabla.addRow(new Object[]{
                                g.getId(),
                                tipo,
                                g.getDescripcion(),
                                String.format("S/ %.2f", monto),
                                g.getFecha() != null ? g.getFecha().toString() : "-",
                                detalle
                        });
                    }
                    etiquetaMensajeEstado.setText(String.format("Poblado con %d egresos registrados. Total acumulado: S/ %.2f",
                            gastos.size(), totalGastos));
                    etiquetaMensajeEstado.setForeground(EstilosUI.VERDE_EXITO);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(PanelGastosComunes.this, "Error al sincronizar gastos", causa);
                }
            }
        };
        worker.execute();
    }

    private void ejecutarRegistroGasto() {
        String tipo = (String) comboTipoGasto.getSelectedItem();
        String desc = txtDescripcion.getText().trim();
        String montoStr = txtMonto.getText().trim();

        if (desc.isBlank() || montoStr.isBlank()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "La descripción y el monto son campos obligatorios.");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException nfe) {
            ManejadorErroresUI.mostrarAdvertencia(this, "El monto debe ser un valor numérico válido.");
            return;
        }

        if (monto <= 0) {
            ManejadorErroresUI.mostrarAdvertencia(this, "El monto debe ser mayor a cero.");
            return;
        }

        try {
            GastoComun nuevoGasto;
            if ("AGUA".equals(tipo)) {
                double lectAct = Double.parseDouble(txtParametro1.getText().trim());
                double lectAnt = Double.parseDouble(txtParametro2.getText().trim());
                nuevoGasto = GastoComunFabrica.crearGastoAgua(null, monto, LocalDate.now(), desc, null, lectAct, lectAnt);
            } else if ("LUZ".equals(tipo)) {
                double kwh = Double.parseDouble(txtParametro1.getText().trim());
                nuevoGasto = GastoComunFabrica.crearGastoLuz(null, monto, LocalDate.now(), desc, null, kwh);
            } else {
                String empresa = txtParametro1.getText().trim();
                nuevoGasto = GastoComunFabrica.crearGastoLimpieza(null, monto, LocalDate.now(), desc, null, empresa);
            }

            fachada.registrarGastoComun(nuevoGasto);
            ManejadorErroresUI.mostrarExito(this,
                    "Gasto de tipo " + tipo + " registrado exitosamente por S/ " + String.format("%.2f", monto),
                    "Registro Exitoso");
            cargarDatosTabla();
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al registrar gasto", ex);
        }
    }
}
