package com.condominio.gui;

import com.condominio.dto.InmuebleDTO;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

// Dialogo interactivo de calculo automatico de consumo de servicios (Agua y Luz)
@Slf4j
public class DialogoCalculoConsumo extends JDialog {

    private final CobranzaFachada fachada;
    private final Runnable callbackActualizacion;

    private JComboBox<String> comboDepartamentos;
    private JComboBox<String> comboServicio;
    private JTextField txtLecturaAnterior;
    private JTextField txtLecturaActual;
    private JTextField txtTarifaUnitaria;
    private JCheckBox chkImputarCuota;

    // Componentes de la tarjeta de calculo en vivo
    private JLabel lblConsumoCalculado;
    private JLabel lblFormulaCalculo;
    private JLabel lblTotalFacturar;

    public DialogoCalculoConsumo(Window propietario, CobranzaFachada fachada, Runnable callbackActualizacion) {
        super(propietario, "Cálculo y Asignación de Consumo por Departamento", ModalityType.APPLICATION_MODAL);
        this.fachada = fachada;
        this.callbackActualizacion = callbackActualizacion;
        configurarVentana();
        inicializarComponentes();
        cargarDepartamentos();
        recalcularEnVivo();
    }

    private void configurarVentana() {
        setSize(560, 580);
        setMinimumSize(new Dimension(500, 520));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. SUBTÍTULO DISCRETO DE UNA LÍNEA BAJO LA BARRA DE TÍTULO
        JLabel subtituloBanner = new JLabel("Ingrese las lecturas del medidor para calcular en vivo el consumo e importe a facturar.");
        subtituloBanner.setFont(EstilosUI.FUENTE_REGULAR);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        subtituloBanner.setBorder(new EmptyBorder(0, 4, 8, 4));
        add(subtituloBanner, BorderLayout.NORTH);

        // 2. FORMULARIO CENTRAL
        JPanel panelCentro = new JPanel(new BorderLayout(10, 12));
        panelCentro.setOpaque(false);

        GlassPanel panelInputs = new GlassPanel(22, false, null);
        panelInputs.setLayout(new GridLayout(5, 2, 10, 10));
        panelInputs.setBorder(new EmptyBorder(14, 18, 14, 18));

        comboDepartamentos = EstilosUI.crearCombo();
        EstilosUI.estilizarCombo(comboDepartamentos, EstilosUI.ACENTO_COBRANZAS);
        comboServicio = EstilosUI.crearCombo();
        EstilosUI.estilizarCombo(comboServicio, EstilosUI.ACENTO_COBRANZAS);
        comboServicio.addItem("Agua Potable (m³)");
        comboServicio.addItem("Energía Eléctrica (kWh)");

        txtLecturaAnterior = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtLecturaAnterior.setText("120.00");

        txtLecturaActual = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtLecturaActual.setText("145.50");

        txtTarifaUnitaria = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtTarifaUnitaria.setText("3.50");

        comboServicio.addActionListener(e -> {
            boolean esAgua = comboServicio.getSelectedIndex() == 0;
            if (esAgua) {
                txtTarifaUnitaria.setText("3.50");
                txtLecturaAnterior.setText("120.00");
                txtLecturaActual.setText("145.50");
            } else {
                txtTarifaUnitaria.setText("0.65");
                txtLecturaAnterior.setText("450.00");
                txtLecturaActual.setText("580.00");
            }
            recalcularEnVivo();
        });

        DocumentListener listenerRecalculo = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcularEnVivo(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcularEnVivo(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcularEnVivo(); }
        };

        txtLecturaAnterior.getDocument().addDocumentListener(listenerRecalculo);
        txtLecturaActual.getDocument().addDocumentListener(listenerRecalculo);
        txtTarifaUnitaria.getDocument().addDocumentListener(listenerRecalculo);

        panelInputs.add(EstilosUI.crearEtiquetaCampo("Departamento / Vecino:"));
        panelInputs.add(comboDepartamentos);
        panelInputs.add(EstilosUI.crearEtiquetaCampo("Tipo de Servicio:"));
        panelInputs.add(comboServicio);
        panelInputs.add(EstilosUI.crearEtiquetaCampo("Lectura Anterior:"));
        panelInputs.add(txtLecturaAnterior);
        panelInputs.add(EstilosUI.crearEtiquetaCampo("Lectura Actual:"));
        panelInputs.add(txtLecturaActual);
        panelInputs.add(EstilosUI.crearEtiquetaCampo("Tarifa Unitaria (S/):"));
        panelInputs.add(txtTarifaUnitaria);

        panelCentro.add(panelInputs, BorderLayout.NORTH);

        // 3. TARJETA DE CÁLCULO EN VIVO (BoxLayout vertical para separación garantizada)
        GlassPanel panelCalculoEnVivo = new GlassPanel(20, false, EstilosUI.ACENTO_COBRANZAS);
        panelCalculoEnVivo.setLayout(new BoxLayout(panelCalculoEnVivo, BoxLayout.Y_AXIS));
        panelCalculoEnVivo.setBorder(new EmptyBorder(14, 18, 14, 18));

        lblConsumoCalculado = new JLabel("Consumo Calculado: 25.50 m³", SwingConstants.CENTER);
        lblConsumoCalculado.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblConsumoCalculado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConsumoCalculado.setForeground(EstilosUI.AZUL_INFO);

        lblFormulaCalculo = new JLabel("Fórmula: (145.50 - 120.00) * S/ 3.50", SwingConstants.CENTER);
        lblFormulaCalculo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFormulaCalculo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormulaCalculo.setForeground(EstilosUI.TEXTO_MUTED);

        lblTotalFacturar = new JLabel("TOTAL A FACTURAR: S/ 89.25", SwingConstants.CENTER);
        lblTotalFacturar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTotalFacturar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalFacturar.setForeground(EstilosUI.DORADO_CLARO);

        panelCalculoEnVivo.add(Box.createVerticalGlue());
        panelCalculoEnVivo.add(lblConsumoCalculado);
        panelCalculoEnVivo.add(Box.createVerticalStrut(6));
        panelCalculoEnVivo.add(lblFormulaCalculo);
        panelCalculoEnVivo.add(Box.createVerticalStrut(8));
        panelCalculoEnVivo.add(lblTotalFacturar);
        panelCalculoEnVivo.add(Box.createVerticalGlue());

        panelCentro.add(panelCalculoEnVivo, BorderLayout.CENTER);

        // Checkbox para imputar a cuota
        chkImputarCuota = new JCheckBox("Generar cuota/cargo mensual automáticamente para este departamento", true);
        chkImputarCuota.setOpaque(false);
        chkImputarCuota.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkImputarCuota.setForeground(EstilosUI.VERDE_EXITO);
        panelCentro.add(chkImputarCuota, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // 4. BOTONES INFERIORES
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        EstilosUI.estilizarBotonSecundario(btnCancelar, EstilosUI.ACENTO_COBRANZAS);
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGenerar = new JButton("Generar y Guardar Gasto");
        EstilosUI.estilizarBoton(btnGenerar, EstilosUI.ACENTO_COBRANZAS);
        btnGenerar.addActionListener(e -> ejecutarGeneracionGasto());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGenerar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDepartamentos() {
        comboDepartamentos.removeAllItems();
        if (fachada == null) {
            comboDepartamentos.addItem("101");
            return;
        }

        List<InmuebleDTO> lista = fachada.listarInmuebles();
        if (lista != null && !lista.isEmpty()) {
            for (InmuebleDTO d : lista) {
                String label = d.getNumero() + " (" + d.getNombreTorre() + " - " + d.getNombreEdificio() + ")";
                comboDepartamentos.addItem(label);
            }
        } else {
            comboDepartamentos.addItem("101 (Torre A - Los Sauces)");
            comboDepartamentos.addItem("202 (Torre A - Los Sauces)");
            comboDepartamentos.addItem("501 (Torre Principal - Las Palmeras)");
        }
    }

    private void recalcularEnVivo() {
        try {
            double anterior = Double.parseDouble(txtLecturaAnterior.getText().trim());
            double actual = Double.parseDouble(txtLecturaActual.getText().trim());
            double tarifa = Double.parseDouble(txtTarifaUnitaria.getText().trim());

            boolean esAgua = comboServicio.getSelectedIndex() == 0;
            String unidad = esAgua ? "m³" : "kWh";

            double consumo = Math.max(0.0, actual - anterior);
            double total = Math.round((consumo * tarifa) * 100.0) / 100.0;

            lblConsumoCalculado.setText(String.format("Consumo Neto Calculado: %.2f %s", consumo, unidad));
            lblFormulaCalculo.setText(String.format("Fórmula: (%.2f - %.2f) * S/ %.2f", actual, anterior, tarifa));
            lblTotalFacturar.setText(String.format("TOTAL A FACTURAR: S/ %.2f", total));
            lblTotalFacturar.setForeground(EstilosUI.DORADO_CLARO);

        } catch (Exception ex) {
            lblConsumoCalculado.setText("Consumo Neto: Ingrese valores numéricos válidos");
            lblFormulaCalculo.setText("Fórmula: -");
            lblTotalFacturar.setText("TOTAL: S/ 0.00");
            lblTotalFacturar.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
        }
    }

    private void ejecutarGeneracionGasto() {
        String seleccionDepto = (String) comboDepartamentos.getSelectedItem();
        if (seleccionDepto == null || seleccionDepto.isBlank()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Debe seleccionar un departamento.");
            return;
        }

        // Extraer el numero de departamento del texto (ej: "101 (Torre A...)" -> "101")
        String numDepto = seleccionDepto.split(" ")[0].trim();

        double anterior;
        double actual;
        double tarifa;

        try {
            anterior = Double.parseDouble(txtLecturaAnterior.getText().trim());
            actual = Double.parseDouble(txtLecturaActual.getText().trim());
            tarifa = Double.parseDouble(txtTarifaUnitaria.getText().trim());
        } catch (NumberFormatException ex) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Las lecturas y la tarifa deben ser valores numéricos válidos.");
            return;
        }

        if (actual < anterior) {
            ManejadorErroresUI.mostrarAdvertencia(this, "La lectura actual no puede ser menor a la lectura anterior.");
            return;
        }

        if (tarifa <= 0) {
            ManejadorErroresUI.mostrarAdvertencia(this, "La tarifa unitaria debe ser mayor a cero.");
            return;
        }

        boolean esAgua = comboServicio.getSelectedIndex() == 0;
        String tipoServicio = esAgua ? "AGUA" : "LUZ";
        boolean imputarCuota = chkImputarCuota.isSelected();

        double consumo = actual - anterior;
        double total = Math.round((consumo * tarifa) * 100.0) / 100.0;
        String unidad = esAgua ? "m³" : "kWh";

        try {
            fachada.registrarGastoServicioDepartamento(
                    tipoServicio,
                    numDepto,
                    anterior,
                    actual,
                    tarifa,
                    imputarCuota
            );

            String mensajeExito = String.format(
                    "Gasto de consumo registrado exitosamente.\n\n" +
                            "- Servicio: %s\n" +
                            "- Departamento: %s\n" +
                            "- Lectura Anterior: %.2f %s\n" +
                            "- Lectura Actual: %.2f %s\n" +
                            "- Consumo Neto: %.2f %s\n" +
                            "- Tarifa: S/ %.2f por %s\n" +
                            "- Monto Total Calculado: S/ %.2f\n" +
                            "- Imputación a Cobranzas: %s",
                    tipoServicio, numDepto, anterior, unidad, actual, unidad, consumo, unidad, tarifa, unidad, total,
                    imputarCuota ? "Generada nueva cuota en cobranzas" : "Solo registrado en egresos"
            );

            ManejadorErroresUI.mostrarExito(this, mensajeExito, "Cálculo y Registro Exitoso");

            if (callbackActualizacion != null) {
                callbackActualizacion.run();
            }

            dispose();

        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al registrar gasto de consumo: " + ex.getMessage(), ex);
        }
    }
}
