package com.condominio.gui;

import com.condominio.dto.InmuebleDTO;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.service.ReportePdfService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

// Diálogo modal para la selección y generación de reportes oficiales en formato PDF (Punto 3.2).
@Slf4j
public class DialogoGeneradorReportes extends JDialog {

    private final ReportePdfService reportePdfService;
    private final CobranzaFachada fachada;
    private JRadioButton rbEstadoCuenta;
    private JRadioButton rbCuotasPendientes;
    private JRadioButton rbResumenFinanciero;
    private JRadioButton rbIncidentesReservas;
    private JComboBox<String> comboDepto;
    private JLabel lblEstado;

    public DialogoGeneradorReportes(Window padre, ReportePdfService reportePdfService, CobranzaFachada fachada) {
        super(padre, "Centro de Generación de Reportes PDF Oficiales", ModalityType.APPLICATION_MODAL);
        this.reportePdfService = (reportePdfService != null) ? reportePdfService : new ReportePdfService(fachada);
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(520, 390);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(18, 20, 18, 20));

        // Subtítulo
        JLabel lblSub = new JLabel("Seleccione el reporte formal que desea compilar en formato PDF:");
        lblSub.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSub.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        add(lblSub, BorderLayout.NORTH);

        // Opciones
        GlassPanel panelOpciones = new GlassPanel(22, false, EstilosUI.AZUL_INFO);
        panelOpciones.setLayout(new GridLayout(5, 1, 6, 8));
        panelOpciones.setBorder(new EmptyBorder(14, 18, 14, 18));

        ButtonGroup grupo = new ButtonGroup();
        rbEstadoCuenta = crearRadio("1. Estado de Cuenta Individual por Propietario / Depto");
        rbCuotasPendientes = crearRadio("2. Relación de Cuotas Pendientes por Edificio");
        rbResumenFinanciero = crearRadio("3. Balance Mensual de Ingresos y Egresos Ordinarios");
        rbIncidentesReservas = crearRadio("4. Auditoría de Incidentes y Reservas del Periodo");

        rbEstadoCuenta.setSelected(true);
        grupo.add(rbEstadoCuenta);
        grupo.add(rbCuotasPendientes);
        grupo.add(rbResumenFinanciero);
        grupo.add(rbIncidentesReservas);

        JPanel panelDeptoFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelDeptoFila.setOpaque(false);
        panelDeptoFila.add(EstilosUI.crearEtiquetaCampo("Departamento Objetivo:"));
        comboDepto = EstilosUI.crearCombo();
        comboDepto.setPreferredSize(new Dimension(180, 28));
        cargarDepartamentos();
        panelDeptoFila.add(comboDepto);

        rbEstadoCuenta.addActionListener(e -> comboDepto.setEnabled(true));
        rbCuotasPendientes.addActionListener(e -> comboDepto.setEnabled(false));
        rbResumenFinanciero.addActionListener(e -> comboDepto.setEnabled(false));
        rbIncidentesReservas.addActionListener(e -> comboDepto.setEnabled(false));

        panelOpciones.add(rbEstadoCuenta);
        panelOpciones.add(panelDeptoFila);
        panelOpciones.add(rbCuotasPendientes);
        panelOpciones.add(rbResumenFinanciero);
        panelOpciones.add(rbIncidentesReservas);

        add(panelOpciones, BorderLayout.CENTER);

        // Panel Inferior con Estado y Botones
        JPanel panelSur = new JPanel(new BorderLayout(8, 8));
        panelSur.setOpaque(false);

        lblEstado = new JLabel("Listo para emitir documento.", SwingConstants.LEFT);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(EstilosUI.TEXTO_MUTED);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.TEXTO_SECUNDARIO);
        btnCerrar.addActionListener(e -> dispose());

        JButton btnGenerar = new JButton("Generar y Abrir PDF");
        EstilosUI.estilizarBoton(btnGenerar, EstilosUI.DORADO_PRINCIPAL);
        btnGenerar.addActionListener(e -> ejecutarGeneracionPdf());

        panelBotones.add(btnCerrar);
        panelBotones.add(btnGenerar);

        panelSur.add(lblEstado, BorderLayout.NORTH);
        panelSur.add(panelBotones, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);
        com.condominio.gui.animacion.AnimadorDialogo.deslizarEntrada((JComponent) getContentPane());
    }

    private JRadioButton crearRadio(String texto) {
        JRadioButton rb = new JRadioButton(texto);
        rb.setOpaque(false);
        rb.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        rb.setFont(EstilosUI.FUENTE_TEXTO);
        rb.setFocusPainted(false);
        return rb;
    }

    private void cargarDepartamentos() {
        if (comboDepto == null) return;
        comboDepto.removeAllItems();
        List<InmuebleDTO> lista = fachada.listarInmuebles();
        if (lista != null) {
            for (InmuebleDTO inm : lista) {
                comboDepto.addItem(inm.getNumero());
            }
        }
        if (comboDepto.getItemCount() == 0) {
            comboDepto.addItem("101");
        }
    }

    private void ejecutarGeneracionPdf() {
        try {
            lblEstado.setText("Compilando documento PDF...");
            lblEstado.setForeground(EstilosUI.AZUL_INFO);

            File pdfGenerado = null;
            if (rbEstadoCuenta.isSelected()) {
                String num = (String) comboDepto.getSelectedItem();
                pdfGenerado = reportePdfService.generarEstadoCuentaPropietario(num);
            } else if (rbCuotasPendientes.isSelected()) {
                pdfGenerado = reportePdfService.generarCuotasPendientesEdificio();
            } else if (rbResumenFinanciero.isSelected()) {
                pdfGenerado = reportePdfService.generarResumenIngresosGastos();
            } else if (rbIncidentesReservas.isSelected()) {
                pdfGenerado = reportePdfService.generarIncidentesYReservas();
            }

            if (pdfGenerado != null && pdfGenerado.exists()) {
                lblEstado.setText("PDF generado con éxito: " + pdfGenerado.getName());
                lblEstado.setForeground(EstilosUI.VERDE_EXITO);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(pdfGenerado);
                }
            }
        } catch (Exception ex) {
            log.error("Error al generar reporte PDF: {}", ex.getMessage(), ex);
            lblEstado.setText("Error al generar PDF: " + ex.getMessage());
            lblEstado.setForeground(EstilosUI.ROJO_ERROR);
        }
    }
}
