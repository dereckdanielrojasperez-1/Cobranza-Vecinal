package com.condominio.gui.paneles;

import com.condominio.dto.CuotaDTO;
import com.condominio.gui.DialogoEstadoCuentaVecino;
import com.condominio.gui.animacion.AnimadorToast;
import com.condominio.gui.animacion.DestelloFila;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroDecimal;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.RenderEstadoCuota;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// Portal del Residente: Experiencia visual dedicada y personalizada para el habitante
@Slf4j
public class PanelPortalResidente extends JPanel {

    private final CobranzaFachada fachada;
    private final String numeroDepartamento;

    // Componentes de Cuotas y Pagos
    private DefaultTableModel modeloCuotas;
    private JTable tablaCuotas;
    private JLabel etiquetaTotalFacturado;
    private JLabel etiquetaTotalPagado;
    private JLabel etiquetaSaldoPendiente;
    private JLabel etiquetaEstadoDistintivo;
    private JTextField campoTextoIdCuotaPago;
    private JTextField campoTextoMontoPago;
    private JComboBox<String> comboMetodoPago;
    private JTextField campoTextoCodigoOperacion;
    private JButton botonPagarCuota;
    private int correlativoOperacion = 101;

    // Componentes de Reservas (100% interactivos por selección y filtros)
    private DefaultTableModel modeloReservas;
    private JTable tablaReservas;
    private TableRowSorter<DefaultTableModel> sorterReservas;
    private JComboBox<String> comboAreaReserva;
    private JComboBox<String> comboFechaReserva;
    private JComboBox<String> comboTurnoReserva;
    private JComboBox<String> comboHoraInicio;
    private JComboBox<String> comboDuracionHoras;
    private JLabel lblHoraFinCalculada;
    private JLabel lblDisponibilidadHorario;
    private JButton botonSolicitarReserva;
    private LocalTime horaFinCalculada;
    private JComboBox<String> comboCantidadInvitados;
    private JComboBox<String> comboFiltroAreaReserva;
    private JComboBox<String> comboFiltroEstadoReserva;
    private JLabel etiquetaEstadoFiltroReservas;

    // Componentes de Incidencias
    private DefaultTableModel modeloIncidencias;
    private JTable tablaIncidencias;
    private JComboBox<String> comboPrioridadIncidencia;
    private JTextField campoTextoDescripcionIncidencia;

    public PanelPortalResidente(CobranzaFachada fachada, String numeroDepartamento) {
        this.fachada = fachada;
        this.numeroDepartamento = (numeroDepartamento != null && !numeroDepartamento.isBlank())
                ? numeroDepartamento : "101";
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    // 1. PESTAÑA: MIS CUOTAS Y PAGOS (PORTAL FINANCIERO DEL VECINO)

    public JPanel crearPanelCuotasYPagos() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        panel.add(construirCabeceraYTarjetasKpi(), BorderLayout.NORTH);
        panel.add(construirTablaCuotasResidente(), BorderLayout.CENTER);
        panel.add(construirFormularioPagoResidente(), BorderLayout.SOUTH);

        cargarDatosCuotas();
        return panel;
    }

    private JPanel construirCabeceraYTarjetasKpi() {
        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);

        JPanel panelFichaVecino = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_COBRANZAS);

        JLabel etiquetaTitulo = new JLabel("PORTAL DEL RESIDENTE - ESTADO DE CUENTA Y PAGOS EN LÍNEA");
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiquetaTitulo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel etiquetaSubtitulo = new JLabel("Bienvenido(a), habitante del Departamento " + numeroDepartamento +
                " (Torre A - Piso 1 | Metraje: 75.00 m2)");
        etiquetaSubtitulo.setFont(EstilosUI.FUENTE_REGULAR);
        etiquetaSubtitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelFichaVecino.add(etiquetaTitulo, BorderLayout.NORTH);
        panelFichaVecino.add(etiquetaSubtitulo, BorderLayout.CENTER);
        panelNorte.add(panelFichaVecino, BorderLayout.NORTH);

        JPanel panelTarjetasKpi = new JPanel(new GridLayout(1, 4, 10, 0));
        panelTarjetasKpi.setOpaque(false);

        etiquetaTotalFacturado = new JLabel("S/ 0.00", SwingConstants.CENTER);
        etiquetaTotalPagado = new JLabel("S/ 0.00", SwingConstants.CENTER);
        etiquetaSaldoPendiente = new JLabel("S/ 0.00", SwingConstants.CENTER);
        etiquetaEstadoDistintivo = new JLabel("AL DÍA", SwingConstants.CENTER);

        panelTarjetasKpi.add(crearTarjetaKpiPersonal("Total Facturado (Mis Cuotas)", etiquetaTotalFacturado, EstilosUI.AZUL_INFO));
        panelTarjetasKpi.add(crearTarjetaKpiPersonal("Total Abonado a la Fecha", etiquetaTotalPagado, EstilosUI.VERDE_EXITO));
        panelTarjetasKpi.add(crearTarjetaKpiPersonal("Saldo Pendiente por Pagar", etiquetaSaldoPendiente, EstilosUI.AMBAR_AVISO));
        panelTarjetasKpi.add(crearTarjetaKpiPersonal("Condición de Pago", etiquetaEstadoDistintivo, EstilosUI.VERDE_EXITO));
        panelNorte.add(panelTarjetasKpi, BorderLayout.SOUTH);

        return panelNorte;
    }

    private JScrollPane construirTablaCuotasResidente() {
        String[] nombresColumnas = {"ID Cuota", "Periodo", "Concepto de Cobro", "Monto Total", "Saldo Pendiente", "Estado", "Vencimiento"};
        modeloCuotas = new DefaultTableModel(nombresColumnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };

        tablaCuotas = new JTable(modeloCuotas);
        EstilosUI.estilizarTabla(tablaCuotas, EstilosUI.ACENTO_COBRANZAS);
        tablaCuotas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer renderizadorCentro = new DefaultTableCellRenderer();
        renderizadorCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaCuotas.getColumnModel().getColumn(0).setCellRenderer(renderizadorCentro);
        tablaCuotas.getColumnModel().getColumn(1).setCellRenderer(renderizadorCentro);
        tablaCuotas.getColumnModel().getColumn(5).setCellRenderer(new RenderEstadoCuota());
        tablaCuotas.getColumnModel().getColumn(6).setCellRenderer(renderizadorCentro);

        DefaultTableCellRenderer renderizadorDerecha = new DefaultTableCellRenderer();
        renderizadorDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaCuotas.getColumnModel().getColumn(3).setCellRenderer(renderizadorDerecha);
        tablaCuotas.getColumnModel().getColumn(4).setCellRenderer(renderizadorDerecha);

        tablaCuotas.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tablaCuotas.getSelectedRow() >= 0) {
                int filaSeleccionada = tablaCuotas.getSelectedRow();
                Object objetoId = tablaCuotas.getValueAt(filaSeleccionada, 0);
                Object objetoSaldo = tablaCuotas.getValueAt(filaSeleccionada, 4);
                if (objetoId != null) {
                    campoTextoIdCuotaPago.setText(objetoId.toString());
                }
                if (objetoSaldo != null) {
                    String saldoTexto = objetoSaldo.toString().replace("S/", "").replace(",", "").trim();
                    try {
                        double saldoDecimal = Double.parseDouble(saldoTexto);
                        campoTextoMontoPago.setText(String.format("%.2f", saldoDecimal));
                    } catch (Exception excepcionIgnorada) {
                        campoTextoMontoPago.setText("0.00");
                    }
                }
            }
        });

        JScrollPane panelDesplazable = new JScrollPane(tablaCuotas);
        panelDesplazable.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        panelDesplazable.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        return panelDesplazable;
    }

    private JPanel construirFormularioPagoResidente() {
        JPanel panelPago = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_COBRANZAS, 14);
        panelPago.setLayout(new BorderLayout(10, 8));
        panelPago.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel etiquetaPagoTitulo = new JLabel("PAGAR CUOTA O GASTO EN LÍNEA (Seleccione una cuota de la tabla superior)");
        etiquetaPagoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaPagoTitulo.setForeground(EstilosUI.DORADO_PRINCIPAL);
        panelPago.add(etiquetaPagoTitulo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelCampos.setOpaque(false);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("ID Cuota:"));
        campoTextoIdCuotaPago = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        campoTextoIdCuotaPago.setPreferredSize(new Dimension(80, 32));
        campoTextoIdCuotaPago.setEditable(false);
        panelCampos.add(campoTextoIdCuotaPago);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("Monto a Pagar (S/):"));
        campoTextoMontoPago = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        campoTextoMontoPago.setPreferredSize(new Dimension(110, 32));
        FiltroDecimal.aplicar(campoTextoMontoPago, 8);
        panelCampos.add(campoTextoMontoPago);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("Método de Pago:"));
        comboMetodoPago = EstilosUI.crearCombo();
        comboMetodoPago.addItem("Billetera Digital Yape / Plin");
        comboMetodoPago.addItem("Transferencia Bancaria BCP");
        comboMetodoPago.addItem("Transferencia Interbancaria BBVA");
        comboMetodoPago.addItem("Tarjeta Débito / Crédito Visa/Mastercard");
        comboMetodoPago.addItem("Efectivo en Recepción Conserjería");
        EstilosUI.estilizarCombo(comboMetodoPago, EstilosUI.ACENTO_COBRANZAS);
        comboMetodoPago.setPreferredSize(new Dimension(230, 32));
        panelCampos.add(comboMetodoPago);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("Operación:"));
        campoTextoCodigoOperacion = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        campoTextoCodigoOperacion.setPreferredSize(new Dimension(120, 32));
        campoTextoCodigoOperacion.setEditable(false);
        campoTextoCodigoOperacion.setText("RES-" + (correlativoOperacion++));
        panelCampos.add(campoTextoCodigoOperacion);

        botonPagarCuota = new JButton("Pagar Mi Cuota");
        EstilosUI.estilizarBoton(botonPagarCuota, EstilosUI.ACENTO_COBRANZAS);
        botonPagarCuota.setPreferredSize(new Dimension(140, 32));
        botonPagarCuota.addActionListener(evento -> ejecutarPagoResidente());
        panelCampos.add(botonPagarCuota);

        JButton botonDetalleEstadoCuenta = new JButton("Ver Estado de Cuenta Detallado");
        EstilosUI.estilizarBotonSecundario(botonDetalleEstadoCuenta, EstilosUI.ACENTO_COBRANZAS);
        botonDetalleEstadoCuenta.setPreferredSize(new Dimension(210, 32));
        botonDetalleEstadoCuenta.addActionListener(evento -> {
            DialogoEstadoCuentaVecino dialogo = new DialogoEstadoCuentaVecino(
                    SwingUtilities.getWindowAncestor(this), fachada, numeroDepartamento);
            dialogo.setVisible(true);
            cargarDatosCuotas();
        });
        panelCampos.add(botonDetalleEstadoCuenta);

        panelPago.add(panelCampos, BorderLayout.CENTER);
        return panelPago;
    }

    private void ejecutarPagoResidente() {
        String idTexto = campoTextoIdCuotaPago.getText().trim();
        String montoTexto = campoTextoMontoPago.getText().trim();

        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione primero una cuota de la tabla para realizar el pago.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoTexto);
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto a pagar debe ser mayor a cero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException excepcionNumero) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long idCuota = Long.parseLong(idTexto);
        String codigoOperacion = campoTextoCodigoOperacion.getText().trim();
        String metodoPago = (String) comboMetodoPago.getSelectedItem();

        int confirmacionPago = JOptionPane.showConfirmDialog(
                this,
                "¿Desea confirmar el pago de S/ " + String.format("%.2f", monto) + " para la Cuota #" + idCuota +
                        "\nmediante " + metodoPago + "?",
                "Confirmación de Pago",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirmacionPago != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean pagoExitoso = fachada.procesarPagoConRepositorio(idCuota, monto, codigoOperacion);
            if (pagoExitoso) {
                AnimadorToast.mostrar("CONFIRMACIÓN DE PAGO", "Pago de S/ " + String.format("%.2f", monto) + " registrado exitosamente.", EstilosUI.VERDE_EXITO);
                campoTextoCodigoOperacion.setText("RES-" + (correlativoOperacion++));
                campoTextoIdCuotaPago.setText("");
                campoTextoMontoPago.setText("");
                cargarDatosCuotas();
                if (tablaCuotas.getRowCount() > 0) {
                    DestelloFila.resaltar(tablaCuotas, 0, EstilosUI.VERDE_EXITO);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el pago. Verifique el saldo de la cuota.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception excepcion) {
            ManejadorErroresUI.mostrarError(this, "Error al registrar pago", excepcion);
        }
    }

    private void cargarDatosCuotas() {
        if (modeloCuotas == null || fachada == null) return;
        modeloCuotas.setRowCount(0);

        List<CuotaDTO> listaCuotas = fachada.listarTodasLasCuotas();
        double totalFacturado = 0.0;
        double saldoPendiente = 0.0;

        for (CuotaDTO cuota : listaCuotas) {
            if (cuota.getNumeroDepartamento() != null && cuota.getNumeroDepartamento().contains(numeroDepartamento)) {
                double montoBase = (cuota.getMontoBase() != null) ? cuota.getMontoBase() : 0.0;
                double montoSaldo = (cuota.getMontoFinal() != null) ? cuota.getMontoFinal() : 0.0;
                totalFacturado += montoBase;
                saldoPendiente += montoSaldo;

                String periodoCuota = (cuota.getFechaVencimiento() != null && cuota.getFechaVencimiento().length() >= 7)
                        ? cuota.getFechaVencimiento().substring(0, 7)
                        : "2026-10";

                modeloCuotas.addRow(new Object[]{
                        cuota.getIdCuota(),
                        periodoCuota,
                        "Cuota Ordinaria Mantenimiento y Servicios",
                        String.format("S/ %.2f", montoBase),
                        String.format("S/ %.2f", montoSaldo),
                        cuota.getEstado(),
                        cuota.getFechaVencimiento()
                });
            }
        }

        double totalAbonado = Math.max(0.0, totalFacturado - saldoPendiente);

        com.condominio.gui.animacion.AnimadorKpi.animar(etiquetaTotalFacturado, totalFacturado, "S/ ", "", 2);
        com.condominio.gui.animacion.AnimadorKpi.animar(etiquetaTotalPagado, totalAbonado, "S/ ", "", 2);
        com.condominio.gui.animacion.AnimadorKpi.animar(etiquetaSaldoPendiente, saldoPendiente, "S/ ", "", 2);

        if (saldoPendiente <= 0.01) {
            etiquetaSaldoPendiente.setForeground(EstilosUI.VERDE_EXITO);
            etiquetaEstadoDistintivo.setText("AL DÍA (SIN DEUDA)");
            etiquetaEstadoDistintivo.setForeground(EstilosUI.VERDE_EXITO);
        } else {
            etiquetaSaldoPendiente.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
            etiquetaEstadoDistintivo.setText("PAGOS PENDIENTES");
            etiquetaEstadoDistintivo.setForeground(EstilosUI.AMBAR_AVISO);
        }
    }

    // 2. PESTAÑA: MIS RESERVAS DE ÁREAS COMUNES

    public JPanel crearPanelReservas() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        Color acentoReserva = new Color(0x38, 0xBD, 0xF8);

        // Banner superior informativo del modulo
        JPanel panelBanner = EstilosUI.crearPanelBanner(acentoReserva);
        JLabel etiquetaTituloBanner = new JLabel("PORTAL DEL RESIDENTE - RESERVAS DE ÁREAS COMUNES");
        etiquetaTituloBanner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiquetaTituloBanner.setForeground(acentoReserva);
        JLabel etiquetaSubtituloBanner = new JLabel("Consulte disponibilidad y reserve parrillas, piscina y salón social mediante selección asistida.");
        etiquetaSubtituloBanner.setFont(EstilosUI.FUENTE_REGULAR);
        etiquetaSubtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelBanner.add(etiquetaTituloBanner, BorderLayout.NORTH);
        panelBanner.add(etiquetaSubtituloBanner, BorderLayout.CENTER);

        // Formulario de solicitud de reserva (100% por selección sin digitación manual)
        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(acentoReserva, 14);
        panelFormulario.setLayout(new BorderLayout(0, 8));
        panelFormulario.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel etiquetaTitulo = new JLabel("SOLICITAR RESERVA DE ÁREA COMÚN (Selección Asistida sin Escritura Manual)");
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaTitulo.setForeground(acentoReserva);
        panelFormulario.add(etiquetaTitulo, BorderLayout.NORTH);

        JPanel panelContenedorCampos = new JPanel();
        panelContenedorCampos.setLayout(new BoxLayout(panelContenedorCampos, BoxLayout.Y_AXIS));
        panelContenedorCampos.setOpaque(false);

        // Fila 1: Instalación, Fecha (dropdown), Turno Rápido
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila1.setOpaque(false);

        fila1.add(EstilosUI.crearEtiquetaCampo("Instalación / Área:"));
        comboAreaReserva = EstilosUI.crearCombo();
        comboAreaReserva.addItem("Zona BBQ / Parrillas (Cap. 15)");
        comboAreaReserva.addItem("Salón Social Multiusos (Cap. 40)");
        comboAreaReserva.addItem("Piscina Panorámica (Cap. 20)");
        comboAreaReserva.addItem("Cancha Polideportiva (Cap. 12)");
        EstilosUI.estilizarCombo(comboAreaReserva, acentoReserva);
        comboAreaReserva.setPreferredSize(new Dimension(210, 32));
        comboAreaReserva.addActionListener(e -> {
            actualizarAforoSegunArea();
            actualizarCalculoHorarioYDisponibilidad();
        });
        fila1.add(comboAreaReserva);

        fila1.add(EstilosUI.crearEtiquetaCampo("Día de Reserva:"));
        comboFechaReserva = EstilosUI.crearCombo();
        LocalDate hoy = LocalDate.now();
        for (int i = 1; i <= 30; i++) {
            LocalDate f = hoy.plusDays(i);
            String prefijo = (i == 1) ? "Mañana - " : ((i == 2) ? "Pasado Mañana - " : "");
            String texto = prefijo + f.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("es", "PE")).toUpperCase()
                    + " " + String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear())
                    + " [" + f + "]";
            comboFechaReserva.addItem(texto);
        }
        EstilosUI.estilizarCombo(comboFechaReserva, acentoReserva);
        comboFechaReserva.setPreferredSize(new Dimension(245, 32));
        comboFechaReserva.setToolTipText("Seleccione el día de su reserva de la lista");
        comboFechaReserva.addActionListener(e -> actualizarCalculoHorarioYDisponibilidad());
        fila1.add(comboFechaReserva);

        fila1.add(EstilosUI.crearEtiquetaCampo("Turno Rápido:"));
        comboTurnoReserva = EstilosUI.crearCombo();
        comboTurnoReserva.addItem("-- Turnos Predefinidos --");
        comboTurnoReserva.addItem("Turno Mañana (09:00 - 4 hrs)");
        comboTurnoReserva.addItem("Turno Almuerzo (12:00 - 3 hrs)");
        comboTurnoReserva.addItem("Turno Tarde (14:00 - 4 hrs)");
        comboTurnoReserva.addItem("Turno Tarde-Noche (16:00 - 4 hrs)");
        comboTurnoReserva.addItem("Turno Noche (18:00 - 4 hrs)");
        comboTurnoReserva.addItem("Personalizado...");
        EstilosUI.estilizarCombo(comboTurnoReserva, acentoReserva);
        comboTurnoReserva.setPreferredSize(new Dimension(200, 32));
        comboTurnoReserva.setToolTipText("Ajusta automáticamente la hora de inicio y duración según el turno");
        comboTurnoReserva.addActionListener(e -> {
            String t = (String) comboTurnoReserva.getSelectedItem();
            if (t == null) return;
            if (t.contains("09:00")) {
                comboHoraInicio.setSelectedItem("09:00 hrs");
                comboDuracionHoras.setSelectedItem("4 horas de reserva");
            } else if (t.contains("12:00")) {
                comboHoraInicio.setSelectedItem("12:00 hrs (Mediodía)");
                comboDuracionHoras.setSelectedItem("3 horas de reserva");
            } else if (t.contains("14:00")) {
                comboHoraInicio.setSelectedItem("14:00 hrs (Tarde)");
                comboDuracionHoras.setSelectedItem("4 horas de reserva");
            } else if (t.contains("16:00")) {
                comboHoraInicio.setSelectedItem("16:00 hrs");
                comboDuracionHoras.setSelectedItem("4 horas de reserva");
            } else if (t.contains("18:00")) {
                comboHoraInicio.setSelectedItem("18:00 hrs");
                comboDuracionHoras.setSelectedItem("4 horas de reserva");
            }
            actualizarCalculoHorarioYDisponibilidad();
        });
        fila1.add(comboTurnoReserva);

        panelContenedorCampos.add(fila1);

        // Fila 2: Hora Inicio (combo), Duración en Horas (combo), Fin Automático (badge), Aforo (combo), Botón Solicitar
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila2.setOpaque(false);

        fila2.add(EstilosUI.crearEtiquetaCampo("Hora Inicio:"));
        comboHoraInicio = EstilosUI.crearCombo();
        String[] horasInicio = {
                "08:00 hrs", "09:00 hrs", "10:00 hrs", "11:00 hrs",
                "12:00 hrs (Mediodía)", "13:00 hrs", "14:00 hrs (Tarde)",
                "15:00 hrs", "16:00 hrs", "17:00 hrs", "18:00 hrs",
                "19:00 hrs (Noche)", "20:00 hrs", "21:00 hrs"
        };
        for (String h : horasInicio) comboHoraInicio.addItem(h);
        comboHoraInicio.setSelectedItem("14:00 hrs (Tarde)");
        EstilosUI.estilizarCombo(comboHoraInicio, acentoReserva);
        comboHoraInicio.setPreferredSize(new Dimension(145, 32));
        comboHoraInicio.addActionListener(e -> actualizarCalculoHorarioYDisponibilidad());
        fila2.add(comboHoraInicio);

        fila2.add(EstilosUI.crearEtiquetaCampo("¿Cuántas Horas?:"));
        comboDuracionHoras = EstilosUI.crearCombo();
        comboDuracionHoras.addItem("1 hora de reserva");
        comboDuracionHoras.addItem("2 horas de reserva (Recomendado)");
        comboDuracionHoras.addItem("3 horas de reserva");
        comboDuracionHoras.addItem("4 horas de reserva");
        comboDuracionHoras.addItem("5 horas de reserva");
        comboDuracionHoras.setSelectedItem("2 horas de reserva (Recomendado)");
        EstilosUI.estilizarCombo(comboDuracionHoras, acentoReserva);
        comboDuracionHoras.setPreferredSize(new Dimension(185, 32));
        comboDuracionHoras.addActionListener(e -> actualizarCalculoHorarioYDisponibilidad());
        fila2.add(comboDuracionHoras);

        fila2.add(EstilosUI.crearEtiquetaCampo("Término Calculado:"));
        lblHoraFinCalculada = new JLabel(" Finaliza: 16:00 hrs ");
        lblHoraFinCalculada.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHoraFinCalculada.setForeground(acentoReserva);
        lblHoraFinCalculada.setOpaque(true);
        lblHoraFinCalculada.setBackground(new Color(0x10, 0x2A, 0x40));
        lblHoraFinCalculada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(acentoReserva, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        lblHoraFinCalculada.setToolTipText("Hora de finalización generada de forma 100% automática a partir de la hora de inicio y la duración");
        fila2.add(lblHoraFinCalculada);

        fila2.add(EstilosUI.crearEtiquetaCampo("Invitados:"));
        comboCantidadInvitados = EstilosUI.crearCombo();
        EstilosUI.estilizarCombo(comboCantidadInvitados, acentoReserva);
        comboCantidadInvitados.setPreferredSize(new Dimension(140, 32));
        actualizarAforoSegunArea();
        fila2.add(comboCantidadInvitados);

        botonSolicitarReserva = new JButton("Solicitar Reserva");
        EstilosUI.estilizarBoton(botonSolicitarReserva, acentoReserva);
        botonSolicitarReserva.setPreferredSize(new Dimension(150, 32));
        botonSolicitarReserva.addActionListener(evento -> ejecutarReservaResidente());
        fila2.add(botonSolicitarReserva);

        panelContenedorCampos.add(fila2);

        // Fila 3: Indicador de Disponibilidad en Tiempo Real
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        fila3.setOpaque(false);
        lblDisponibilidadHorario = new JLabel("Estado: Verificando disponibilidad de horario...");
        lblDisponibilidadHorario.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDisponibilidadHorario.setForeground(EstilosUI.VERDE_EXITO);
        fila3.add(lblDisponibilidadHorario);
        panelContenedorCampos.add(fila3);

        actualizarCalculoHorarioYDisponibilidad();

        panelContenedorCampos.add(fila2);
        panelFormulario.add(panelContenedorCampos, BorderLayout.CENTER);

        // Barra de Filtrado de Reservas por Clic (Cero tipeo)
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelFiltros.setOpaque(false);

        JLabel lblFiltro = new JLabel("Filtros Rápidos:");
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFiltro.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelFiltros.add(lblFiltro);

        comboFiltroAreaReserva = EstilosUI.crearCombo();
        comboFiltroAreaReserva.addItem("Todas las Instalaciones");
        comboFiltroAreaReserva.addItem("Zona BBQ");
        comboFiltroAreaReserva.addItem("Salón Social");
        comboFiltroAreaReserva.addItem("Piscina");
        comboFiltroAreaReserva.addItem("Cancha");
        EstilosUI.estilizarCombo(comboFiltroAreaReserva, acentoReserva);
        comboFiltroAreaReserva.setPreferredSize(new Dimension(165, 28));
        comboFiltroAreaReserva.addActionListener(e -> aplicarFiltrosReservas());
        panelFiltros.add(comboFiltroAreaReserva);

        comboFiltroEstadoReserva = EstilosUI.crearCombo();
        comboFiltroEstadoReserva.addItem("Todos los Estados");
        comboFiltroEstadoReserva.addItem("CONFIRMADA");
        comboFiltroEstadoReserva.addItem("FINALIZADA");
        comboFiltroEstadoReserva.addItem("PENDIENTE");
        comboFiltroEstadoReserva.addItem("CANCELADA");
        EstilosUI.estilizarCombo(comboFiltroEstadoReserva, acentoReserva);
        comboFiltroEstadoReserva.setPreferredSize(new Dimension(145, 28));
        comboFiltroEstadoReserva.addActionListener(e -> aplicarFiltrosReservas());
        panelFiltros.add(comboFiltroEstadoReserva);

        JButton btnLimpiarFiltros = new JButton("Limpiar Filtros");
        EstilosUI.estilizarBotonSecundario(btnLimpiarFiltros, acentoReserva);
        btnLimpiarFiltros.setPreferredSize(new Dimension(110, 28));
        btnLimpiarFiltros.addActionListener(e -> {
            comboFiltroAreaReserva.setSelectedIndex(0);
            comboFiltroEstadoReserva.setSelectedIndex(0);
            aplicarFiltrosReservas();
        });
        panelFiltros.add(btnLimpiarFiltros);

        etiquetaEstadoFiltroReservas = new JLabel("Mostrando todas las reservas");
        etiquetaEstadoFiltroReservas.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaEstadoFiltroReservas.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelFiltros.add(etiquetaEstadoFiltroReservas);

        JPanel panelNorte = new JPanel(new BorderLayout(0, 8));
        panelNorte.setOpaque(false);
        panelNorte.add(panelBanner, BorderLayout.NORTH);
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelFiltros, BorderLayout.SOUTH);
        panel.add(panelNorte, BorderLayout.NORTH);

        // Tabla de reservas del residente
        String[] nombresColumnas = {"ID Reserva", "Área Común", "Fecha", "Hora Inicio", "Hora Fin", "Capacidad", "Estado"};
        modeloReservas = new DefaultTableModel(nombresColumnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };

        tablaReservas = new JTable(modeloReservas);
        sorterReservas = new TableRowSorter<>(modeloReservas);
        tablaReservas.setRowSorter(sorterReservas);
        EstilosUI.estilizarTabla(tablaReservas, acentoReserva);

        DefaultTableCellRenderer renderizadorCentro = new DefaultTableCellRenderer();
        renderizadorCentro.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < nombresColumnas.length - 1; i++) {
            tablaReservas.getColumnModel().getColumn(i).setCellRenderer(renderizadorCentro);
        }
        // Columna Estado con píldora coloreada badge
        tablaReservas.getColumnModel().getColumn(6).setCellRenderer(new com.condominio.gui.util.RenderTipoOperacion());

        // Menú contextual en tabla de reservas (clic derecho)
        tablaReservas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) procesarPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) procesarPopup(e); }
            private void procesarPopup(MouseEvent e) {
                int fila = tablaReservas.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tablaReservas.getRowCount()) {
                    tablaReservas.setRowSelectionInterval(fila, fila);
                    mostrarMenuContextualReserva(e.getX(), e.getY(), fila);
                }
            }
        });

        JScrollPane panelDesplazable = new JScrollPane(tablaReservas);
        panelDesplazable.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        panelDesplazable.setBorder(new LineBorder(new Color(acentoReserva.getRed(), acentoReserva.getGreen(), acentoReserva.getBlue(), 90), 1));
        panel.add(panelDesplazable, BorderLayout.CENTER);

        cargarDatosReservas();

        // Monitoreo periódico en segundo plano: finaliza reservas que vencen y libera las áreas automáticamente
        Timer timerVencimiento = new Timer(35000, ev -> {
            if (isShowing() && fachada != null) {
                try {
                    List<ReservaArea> recien = fachada.verificarYFinalizarReservasVencidas();
                    if (!recien.isEmpty()) {
                        cargarDatosReservas();
                        actualizarCalculoHorarioYDisponibilidad();
                        AnimadorToast.mostrar("ÁREA COMÚN LIBERADA",
                                "Culminó el tiempo de reserva. El área se encuentra disponible. Gerencia notificada.",
                                EstilosUI.AZUL_INFO);
                    }
                } catch (Exception ignored) {}
            }
        });
        timerVencimiento.start();

        return panel;
    }

    private void aplicarFiltrosReservas() {
        if (sorterReservas == null) return;
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();

        if (comboFiltroAreaReserva != null && comboFiltroAreaReserva.getSelectedIndex() > 0) {
            String areaSel = (String) comboFiltroAreaReserva.getSelectedItem();
            if (areaSel != null && !areaSel.startsWith("Todas")) {
                filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(areaSel.trim()), 1));
            }
        }

        if (comboFiltroEstadoReserva != null && comboFiltroEstadoReserva.getSelectedIndex() > 0) {
            String estSel = (String) comboFiltroEstadoReserva.getSelectedItem();
            if (estSel != null && !estSel.startsWith("Todos")) {
                filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(estSel.trim()), 6));
            }
        }

        if (filtros.isEmpty()) {
            sorterReservas.setRowFilter(null);
        } else {
            sorterReservas.setRowFilter(RowFilter.andFilter(filtros));
        }

        int visibles = tablaReservas.getRowCount();
        int total = modeloReservas.getRowCount();
        if (etiquetaEstadoFiltroReservas != null) {
            etiquetaEstadoFiltroReservas.setText("Mostrando " + visibles + " de " + total + " reservas registradas.");
        }
    }

    private void mostrarMenuContextualReserva(int x, int y, int fila) {
        if (fila < 0 || fila >= tablaReservas.getRowCount()) return;
        int filaModelo = tablaReservas.convertRowIndexToModel(fila);
        Object idObj = modeloReservas.getValueAt(filaModelo, 0);
        Object areaObj = modeloReservas.getValueAt(filaModelo, 1);
        Object fechaObj = modeloReservas.getValueAt(filaModelo, 2);
        Object estadoObj = modeloReservas.getValueAt(filaModelo, 6);
        if (idObj == null) return;

        String idStr = idObj.toString();
        String estado = (estadoObj != null) ? estadoObj.toString() : "";

        JPopupMenu popup = EstilosUI.crearMenuDesbordeOscuro(new Color(0x38, 0xBD, 0xF8));

        JMenuItem itemDetalle = EstilosUI.crearItemMenuOscuro("Ver Ficha de Reserva " + idStr, EstilosUI.TEXTO_PRINCIPAL, new Color(0x38, 0xBD, 0xF8));
        itemDetalle.addActionListener(e -> {
            String msg = "FICHA DE RESERVA:\n"
                    + "Código: " + idStr + "\n"
                    + "Área: " + areaObj + "\n"
                    + "Fecha: " + fechaObj + "\n"
                    + "Horario: " + modeloReservas.getValueAt(filaModelo, 3) + " a " + modeloReservas.getValueAt(filaModelo, 4) + "\n"
                    + "Estado: " + estado;
            JOptionPane.showMessageDialog(this, msg, "Detalle de Reserva", JOptionPane.INFORMATION_MESSAGE);
        });
        popup.add(itemDetalle);

        if ("CONFIRMADA".equalsIgnoreCase(estado) || "PENDIENTE".equalsIgnoreCase(estado)) {
            JMenuItem itemFinalizar = EstilosUI.crearItemMenuOscuro("Finalizar y Liberar Área", EstilosUI.AZUL_INFO, EstilosUI.AZUL_INFO);
            itemFinalizar.setToolTipText("Finaliza la reserva, libera el área para otros residentes y notifica a Gerencia y Administración");
            itemFinalizar.addActionListener(e -> {
                Long idNum = null;
                try {
                    idNum = Long.parseLong(idStr.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
                if (idNum != null && fachada != null) {
                    fachada.finalizarReserva(idNum);
                    tablaReservas.setValueAt("FINALIZADA", fila, 6);
                    AnimadorToast.mostrar("ÁREA LIBERADA",
                            "Reserva " + idStr + " finalizada. El área común está libre para nuevos turnos. Gerente y Administrador notificados.",
                            EstilosUI.VERDE_EXITO);
                    cargarDatosReservas();
                    actualizarCalculoHorarioYDisponibilidad();
                }
            });
            popup.add(itemFinalizar);
        }

        if (!"CANCELADA".equalsIgnoreCase(estado) && !"FINALIZADA".equalsIgnoreCase(estado)) {
            JMenuItem itemCancelar = EstilosUI.crearItemMenuOscuro("Cancelar esta reserva", EstilosUI.ROJO_ERROR_TEXTO, EstilosUI.ROJO_ERROR_TEXTO);
            itemCancelar.addActionListener(e -> {
                int resp = JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea cancelar su reserva " + idStr + " para " + areaObj + "?",
                        "Confirmar Cancelación de Reserva",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (resp == JOptionPane.YES_OPTION) {
                    tablaReservas.setValueAt("CANCELADA", fila, 6);
                    AnimadorToast.mostrar("RESERVA CANCELADA", "La reserva " + idStr + " ha sido cancelada.", EstilosUI.AMBAR_AVISO);
                }
            });
            popup.add(itemCancelar);
        }

        popup.show(tablaReservas, x, y);
    }

    private String obtenerNombreAreaLimpio() {
        if (comboAreaReserva == null) return "Zona BBQ / Parrillas";
        String areaRaw = (String) comboAreaReserva.getSelectedItem();
        if (areaRaw == null) return "Zona BBQ / Parrillas";
        if (areaRaw.contains("BBQ") || areaRaw.contains("Parrilla")) return "Zona BBQ / Parrillas";
        if (areaRaw.contains("Salón") || areaRaw.contains("Social")) return "Salón Social Multiusos";
        if (areaRaw.contains("Piscina")) return "Piscina Panorámica";
        if (areaRaw.contains("Cancha")) return "Cancha Polideportiva";
        return areaRaw.split("\\(")[0].trim();
    }

    private LocalDate obtenerFechaReservaSeleccionada() {
        if (comboFechaReserva == null) return LocalDate.now().plusDays(1);
        String fechaSel = (String) comboFechaReserva.getSelectedItem();
        if (fechaSel != null && fechaSel.contains("[") && fechaSel.contains("]")) {
            try {
                String iso = fechaSel.substring(fechaSel.lastIndexOf('[') + 1, fechaSel.lastIndexOf(']')).trim();
                return LocalDate.parse(iso);
            } catch (Exception ignored) {}
        }
        return LocalDate.now().plusDays(1);
    }

    private void actualizarAforoSegunArea() {
        if (comboCantidadInvitados == null) return;
        String area = obtenerNombreAreaLimpio();
        comboCantidadInvitados.removeAllItems();
        if (area.contains("Cancha")) {
            comboCantidadInvitados.addItem("2 personas");
            comboCantidadInvitados.addItem("4 personas");
            comboCantidadInvitados.addItem("6 personas");
            comboCantidadInvitados.addItem("8 personas");
            comboCantidadInvitados.addItem("10 personas");
            comboCantidadInvitados.addItem("12 personas (Aforo máx.)");
            comboCantidadInvitados.setSelectedItem("6 personas");
        } else if (area.contains("BBQ") || area.contains("Parrilla")) {
            comboCantidadInvitados.addItem("2 personas");
            comboCantidadInvitados.addItem("4 personas");
            comboCantidadInvitados.addItem("6 personas");
            comboCantidadInvitados.addItem("8 personas");
            comboCantidadInvitados.addItem("10 personas");
            comboCantidadInvitados.addItem("12 personas");
            comboCantidadInvitados.addItem("15 personas (Aforo máx.)");
            comboCantidadInvitados.setSelectedItem("6 personas");
        } else if (area.contains("Piscina")) {
            comboCantidadInvitados.addItem("2 personas");
            comboCantidadInvitados.addItem("4 personas");
            comboCantidadInvitados.addItem("6 personas");
            comboCantidadInvitados.addItem("8 personas");
            comboCantidadInvitados.addItem("10 personas");
            comboCantidadInvitados.addItem("15 personas");
            comboCantidadInvitados.addItem("20 personas (Aforo máx.)");
            comboCantidadInvitados.setSelectedItem("4 personas");
        } else {
            comboCantidadInvitados.addItem("5 personas");
            comboCantidadInvitados.addItem("10 personas");
            comboCantidadInvitados.addItem("15 personas");
            comboCantidadInvitados.addItem("20 personas");
            comboCantidadInvitados.addItem("25 personas");
            comboCantidadInvitados.addItem("30 personas");
            comboCantidadInvitados.addItem("40 personas (Aforo máx.)");
            comboCantidadInvitados.setSelectedItem("15 personas");
        }
    }

    private void actualizarCalculoHorarioYDisponibilidad() {
        if (comboHoraInicio == null || comboDuracionHoras == null || lblHoraFinCalculada == null) return;
        try {
            String iniStr = (String) comboHoraInicio.getSelectedItem();
            if (iniStr == null) return;
            String horaLimpia = iniStr.split(" ")[0].trim();
            LocalTime ini = LocalTime.parse(horaLimpia);

            int duracion = 2;
            String durStr = (String) comboDuracionHoras.getSelectedItem();
            if (durStr != null) {
                String digits = durStr.replaceAll("[^0-9]", "").trim();
                if (!digits.isEmpty()) duracion = Integer.parseInt(digits);
            }

            LocalTime fin = ini.plusHours(duracion);
            if (fin.isBefore(ini) || (fin.getHour() >= 23 && fin.getMinute() > 0)) {
                fin = LocalTime.of(23, 0);
            }
            horaFinCalculada = fin;
            lblHoraFinCalculada.setText(" Finaliza: " + fin + " hrs ");

            if (fachada != null && lblDisponibilidadHorario != null && comboAreaReserva != null && comboFechaReserva != null) {
                String areaSel = obtenerNombreAreaLimpio();
                LocalDate fechaSel = obtenerFechaReservaSeleccionada();
                boolean ocupado = fachada.existeSolapamientoReserva(areaSel, fechaSel, ini, fin);
                if (ocupado) {
                    lblDisponibilidadHorario.setText("Estado: Horario ocupado por otra reserva. Seleccione otra hora o día.");
                    lblDisponibilidadHorario.setForeground(EstilosUI.ROJO_ERROR);
                    if (botonSolicitarReserva != null) botonSolicitarReserva.setEnabled(false);
                } else {
                    lblDisponibilidadHorario.setText("Estado: Franja (" + ini + " a " + fin + " - " + duracion + " hrs) disponible para reserva inmediata.");
                    lblDisponibilidadHorario.setForeground(EstilosUI.VERDE_EXITO);
                    if (botonSolicitarReserva != null) botonSolicitarReserva.setEnabled(true);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void ejecutarReservaResidente() {
        if (botonSolicitarReserva != null) botonSolicitarReserva.setEnabled(false);
        String areaSeleccionada = obtenerNombreAreaLimpio();
        LocalDate fechaReserva = obtenerFechaReservaSeleccionada();

        LocalTime horaInicio = LocalTime.of(14, 0);
        try {
            String hIniStr = (String) comboHoraInicio.getSelectedItem();
            if (hIniStr != null) {
                horaInicio = LocalTime.parse(hIniStr.split(" ")[0].trim());
            }
        } catch (Exception ex) {
            horaInicio = LocalTime.of(14, 0);
        }

        LocalTime horaFin = (horaFinCalculada != null) ? horaFinCalculada : horaInicio.plusHours(2);
        if (!horaFin.isAfter(horaInicio)) {
            horaFin = horaInicio.plusHours(2);
        }

        int cantidadInvitados = 4;
        String invSel = (String) comboCantidadInvitados.getSelectedItem();
        if (invSel != null) {
            String digits = invSel.replaceAll("[^0-9]", "").trim();
            if (!digits.isEmpty()) cantidadInvitados = Integer.parseInt(digits);
        }

        String titularReserva = "Residente Depto " + numeroDepartamento;
        final LocalTime fHoraInicio = horaInicio;
        final LocalTime fHoraFin = horaFin;
        final int fInvitados = cantidadInvitados;

        SwingWorker<ReservaArea, Void> worker = new SwingWorker<>() {
            @Override
            protected ReservaArea doInBackground() throws Exception {
                return fachada.reservarArea(areaSeleccionada, titularReserva, fechaReserva, fHoraInicio, fHoraFin, fInvitados);
            }

            @Override
            protected void done() {
                if (botonSolicitarReserva != null) botonSolicitarReserva.setEnabled(true);
                try {
                    ReservaArea reserva = get();
                    AnimadorToast.mostrar("RESERVA CONFIRMADA",
                            "Reserva #" + reserva.getId() + " para " + areaSeleccionada + " (" + fHoraInicio + " a " + fHoraFin + ") registrada con éxito.",
                            new Color(0x38, 0xBD, 0xF8));
                    cargarDatosReservas();
                    actualizarCalculoHorarioYDisponibilidad();
                    if (tablaReservas.getRowCount() > 0) {
                        DestelloFila.resaltar(tablaReservas, tablaReservas.getRowCount() - 1, EstilosUI.ACENTO_OPERACIONES);
                    }
                } catch (Exception excepcion) {
                    Throwable cause = excepcion.getCause() != null ? excepcion.getCause() : excepcion;
                    ManejadorErroresUI.mostrarError(PanelPortalResidente.this, "Aviso de Reserva", cause);
                }
            }
        };
        worker.execute();
    }

    private void cargarDatosReservas() {
        if (modeloReservas == null || fachada == null) return;
        try {
            fachada.verificarYFinalizarReservasVencidas();
        } catch (Exception ignored) {}
        modeloReservas.setRowCount(0);

        List<ReservaArea> listaReservas = fachada.listarReservas();
        if (listaReservas != null) {
            for (ReservaArea reserva : listaReservas) {
                String nombreArea = (reserva.getAreaComun() != null && reserva.getAreaComun().getNombre() != null)
                        ? reserva.getAreaComun().getNombre() : "Área Común";
                String fechaFormateada = (reserva.getFechaReserva() != null) ? reserva.getFechaReserva().toString() : "-";
                String horaInicioFormateada = (reserva.getHoraInicio() != null) ? reserva.getHoraInicio().toString() : "14:00";
                String horaFinFormateada = (reserva.getHoraFin() != null) ? reserva.getHoraFin().toString() : "18:00";
                String estadoReserva = (reserva.getEstado() != null) ? reserva.getEstado() : "CONFIRMADA";
                String aforo = (reserva.getAreaComun() != null && reserva.getAreaComun().getCapacidad() != null)
                        ? (reserva.getAreaComun().getCapacidad() + " máx.") : "Estándar";

                modeloReservas.addRow(new Object[]{
                        "RES-" + reserva.getId(),
                        nombreArea,
                        fechaFormateada,
                        horaInicioFormateada,
                        horaFinFormateada,
                        aforo,
                        estadoReserva
                });
            }
        }
        aplicarFiltrosReservas();
    }

    // 3. PESTAÑA: REPORTAR INCIDENCIA A CONSERJERÍA

    public JPanel crearPanelIncidencias() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        Color acentoIncidencias = EstilosUI.AMBAR_AVISO;

        // Banner superior
        JPanel panelBanner = EstilosUI.crearPanelBanner(acentoIncidencias);
        JLabel etiquetaTituloBanner = new JLabel("PORTAL DEL RESIDENTE - REPORTES Y COMUNICACIÓN CON CONSERJERÍA");
        etiquetaTituloBanner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiquetaTituloBanner.setForeground(acentoIncidencias);
        JLabel etiquetaSubtituloBanner = new JLabel("Canalice solicitudes de mantenimiento, averías o novedades para pronta atención.");
        etiquetaSubtituloBanner.setFont(EstilosUI.FUENTE_REGULAR);
        etiquetaSubtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelBanner.add(etiquetaTituloBanner, BorderLayout.NORTH);
        panelBanner.add(etiquetaSubtituloBanner, BorderLayout.CENTER);

        // Formulario
        JPanel panelFormulario = EstilosUI.crearPanelGlassmorphic(acentoIncidencias, 14);
        panelFormulario.setLayout(new BorderLayout(0, 8));
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel etiquetaTitulo = new JLabel("REPORTAR INCIDENCIA O AVERÍA A CONSERJERÍA Y MANTENIMIENTO");
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaTitulo.setForeground(acentoIncidencias);
        panelFormulario.add(etiquetaTitulo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelCampos.setOpaque(false);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("Nivel de Prioridad:"));
        comboPrioridadIncidencia = EstilosUI.crearCombo();
        comboPrioridadIncidencia.addItem("MEDIA");
        comboPrioridadIncidencia.addItem("ALTA");
        comboPrioridadIncidencia.addItem("BAJA");
        EstilosUI.estilizarCombo(comboPrioridadIncidencia, acentoIncidencias);
        comboPrioridadIncidencia.setPreferredSize(new Dimension(110, 32));
        panelCampos.add(comboPrioridadIncidencia);

        panelCampos.add(EstilosUI.crearEtiquetaCampo("Descripción del Problema:"));
        campoTextoDescripcionIncidencia = EstilosUI.crearCampoTexto(acentoIncidencias);
        campoTextoDescripcionIncidencia.setPreferredSize(new Dimension(380, 32));
        campoTextoDescripcionIncidencia.setToolTipText("Describa la avería o solicitud vecinal (ej. fuga de agua, foco quemado)");
        panelCampos.add(campoTextoDescripcionIncidencia);

        JButton botonReportarIncidencia = new JButton("Enviar a Conserjería");
        EstilosUI.estilizarBoton(botonReportarIncidencia, acentoIncidencias);
        botonReportarIncidencia.setPreferredSize(new Dimension(170, 32));
        botonReportarIncidencia.addActionListener(evento -> ejecutarReporteIncidencia());
        panelCampos.add(botonReportarIncidencia);

        panelFormulario.add(panelCampos, BorderLayout.CENTER);

        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);
        panelNorte.add(panelBanner, BorderLayout.NORTH);
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panel.add(panelNorte, BorderLayout.NORTH);

        // Tabla de incidencias del residente
        String[] nombresColumnas = {"ID Incidencia", "Fecha de Emisión", "Prioridad", "Descripción del Reporte", "Estado de Atención"};
        modeloIncidencias = new DefaultTableModel(nombresColumnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };

        tablaIncidencias = new JTable(modeloIncidencias);
        EstilosUI.estilizarTabla(tablaIncidencias, acentoIncidencias);

        DefaultTableCellRenderer renderizadorCentro = new DefaultTableCellRenderer();
        renderizadorCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaIncidencias.getColumnModel().getColumn(0).setCellRenderer(renderizadorCentro);
        tablaIncidencias.getColumnModel().getColumn(1).setCellRenderer(renderizadorCentro);
        // Prioridad con píldora badge coloreada
        tablaIncidencias.getColumnModel().getColumn(2).setCellRenderer(new com.condominio.gui.util.RenderTipoOperacion());
        // Estado con píldora badge coloreada
        tablaIncidencias.getColumnModel().getColumn(4).setCellRenderer(new com.condominio.gui.util.RenderTipoOperacion());

        JScrollPane panelDesplazable = new JScrollPane(tablaIncidencias);
        panelDesplazable.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        panelDesplazable.setBorder(new LineBorder(new Color(acentoIncidencias.getRed(), acentoIncidencias.getGreen(), acentoIncidencias.getBlue(), 90), 1));
        panel.add(panelDesplazable, BorderLayout.CENTER);

        cargarDatosIncidencias();
        return panel;
    }

    private void ejecutarReporteIncidencia() {
        String descripcionProblema = campoTextoDescripcionIncidencia.getText().trim();
        String prioridadProblema = (String) comboPrioridadIncidencia.getSelectedItem();

        if (descripcionProblema.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor describa el problema antes de enviar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Incidente incidente = fachada.reportarIncidente(descripcionProblema, prioridadProblema, "Residente Depto " + numeroDepartamento);
            AnimadorToast.mostrar("REPORTE REGISTRADO", "Incidencia enviada a conserjería con éxito.", EstilosUI.AMBAR_AVISO);
            campoTextoDescripcionIncidencia.setText("");
            cargarDatosIncidencias();
            if (tablaIncidencias.getRowCount() > 0) {
                DestelloFila.resaltar(tablaIncidencias, tablaIncidencias.getRowCount() - 1, EstilosUI.AMBAR_AVISO);
            }
        } catch (Exception excepcion) {
            ManejadorErroresUI.mostrarError(this, "Error al reportar incidencia", excepcion);
        }
    }

    private void cargarDatosIncidencias() {
        if (modeloIncidencias == null || fachada == null) return;
        modeloIncidencias.setRowCount(0);

        List<Incidente> listaIncidentes = fachada.listarIncidentes();
        if (listaIncidentes != null) {
            for (Incidente incidente : listaIncidentes) {
                String fechaFormateada = (incidente.getFecha() != null) ? incidente.getFecha().toString() : "-";
                String estadoIncidente = (incidente.getEstado() != null) ? incidente.getEstado() : "EN PROCESO";
                String descripcion = (incidente.getDescripcion() != null) ? incidente.getDescripcion() : "";

                modeloIncidencias.addRow(new Object[]{
                        "INC-" + incidente.getId(),
                        fechaFormateada,
                        "MEDIA",
                        descripcion,
                        estadoIncidente
                });
            }
        }
    }

    private JPanel crearTarjetaKpiPersonal(String titulo, JLabel etiquetaValor, Color colorAcento) {
        GlassPanel panelTarjetaKpi = new GlassPanel(18, false, colorAcento);
        panelTarjetaKpi.setLayout(new BorderLayout(0, 4));
        panelTarjetaKpi.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel etiquetaTitulo = new JLabel(titulo.toUpperCase(), SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        etiquetaTitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        etiquetaValor.setFont(new Font("Segoe UI", Font.BOLD, 17));
        etiquetaValor.setForeground(colorAcento);

        panelTarjetaKpi.add(etiquetaTitulo, BorderLayout.NORTH);
        panelTarjetaKpi.add(etiquetaValor, BorderLayout.CENTER);
        return panelTarjetaKpi;
    }
}
