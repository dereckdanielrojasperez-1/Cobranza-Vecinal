package com.condominio.gui;

import com.condominio.dto.CuotaDTO;
import com.condominio.dto.InmuebleDTO;
import com.condominio.dto.ResidenteDTO;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.gui.util.RenderEstadoCuota;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Dialogo modal de visualizacion detallada del Estado de Cuenta individual de cada vecino/departamento,
@Slf4j
public class DialogoEstadoCuentaVecino extends JDialog {

    private final CobranzaFachada fachada;
    private JComboBox<String> comboDepartamentos;
    private JLabel etiquetaNombreResidente;
    private JLabel etiquetaDniResidente;
    private JLabel etiquetaContactoResidente;
    private JLabel etiquetaUbicacionInmueble;

    private JLabel etiquetaTotalFacturado;
    private JLabel etiquetaTotalPagado;
    private JLabel etiquetaSaldoPendiente;

    private JTable tablaCuotasVecino;
    private DefaultTableModel modeloTablaCuotas;

    public DialogoEstadoCuentaVecino(Window propietario, CobranzaFachada fachada, String numeroDepartamentoInicial) {
        super(propietario, "Estado de Cuenta Individual por Vecino", ModalityType.APPLICATION_MODAL);
        this.fachada = fachada;
        configurarVentana();
        inicializarComponentes();
        cargarDepartamentos(numeroDepartamentoInicial);
        cargarEstadoCuenta();
    }

    private void configurarVentana() {
        setSize(880, 640);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. SUBTÍTULO DISCRETO DE UNA LÍNEA BAJO LA BARRA DE TÍTULO
        JLabel etiquetaSubtituloBanner = new JLabel("Consulte el expediente financiero consolidado, cuotas ordinarias y consumos del vecino.");
        etiquetaSubtituloBanner.setFont(EstilosUI.FUENTE_REGULAR);
        etiquetaSubtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        etiquetaSubtituloBanner.setBorder(new EmptyBorder(0, 4, 8, 4));

        // Selector de Departamento en el norte
        JPanel panelSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSelector.setOpaque(false);
        panelSelector.add(EstilosUI.crearEtiquetaCampo("Seleccionar Departamento / Vecino:"));

        comboDepartamentos = EstilosUI.crearCombo();
        EstilosUI.estilizarCombo(comboDepartamentos, EstilosUI.ACENTO_COBRANZAS);
        comboDepartamentos.setPreferredSize(new Dimension(340, 32));
        comboDepartamentos.addActionListener(evento -> cargarEstadoCuenta());
        panelSelector.add(comboDepartamentos);

        JPanel panelNorte = new JPanel(new BorderLayout(0, 6));
        panelNorte.setOpaque(false);
        panelNorte.add(etiquetaSubtituloBanner, BorderLayout.NORTH);
        panelNorte.add(panelSelector, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // 2. PANEL DE DATOS DEL VECINO Y MÉTRICAS (CENTRO SUPERIOR)
        JPanel panelCentralContenedor = new JPanel(new BorderLayout(0, 10));
        panelCentralContenedor.setOpaque(false);

        // Ficha del vecino con contenedor GlassPanel (Regla 2)
        GlassPanel panelFichaVecino = new GlassPanel(new GridLayout(2, 2, 10, 6), 20, EstilosUI.ACENTO_COBRANZAS, true);
        panelFichaVecino.setBorder(new EmptyBorder(12, 16, 12, 16));

        etiquetaNombreResidente = new JLabel("Residente: Cargando...");
        etiquetaNombreResidente.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaNombreResidente.setForeground(EstilosUI.TEXTO_CLARO);

        etiquetaDniResidente = new JLabel("DNI: -");
        etiquetaDniResidente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaDniResidente.setForeground(EstilosUI.TEXTO_MUTED);

        etiquetaContactoResidente = new JLabel("Contacto: -");
        etiquetaContactoResidente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaContactoResidente.setForeground(EstilosUI.TEXTO_MUTED);

        etiquetaUbicacionInmueble = new JLabel("Inmueble: -");
        etiquetaUbicacionInmueble.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaUbicacionInmueble.setForeground(EstilosUI.DORADO_CLARO);

        panelFichaVecino.add(etiquetaNombreResidente);
        panelFichaVecino.add(etiquetaUbicacionInmueble);
        panelFichaVecino.add(etiquetaDniResidente);
        panelFichaVecino.add(etiquetaContactoResidente);

        // Tarjetas de balance
        JPanel panelTarjetasKpi = new JPanel(new GridLayout(1, 3, 10, 0));
        panelTarjetasKpi.setOpaque(false);

        etiquetaTotalFacturado = new JLabel("S/ 0.00", SwingConstants.CENTER);
        etiquetaTotalPagado = new JLabel("S/ 0.00", SwingConstants.CENTER);
        etiquetaSaldoPendiente = new JLabel("S/ 0.00", SwingConstants.CENTER);

        panelTarjetasKpi.add(crearMiniTarjeta("Total Facturado", etiquetaTotalFacturado, new Color(2, 132, 199)));
        panelTarjetasKpi.add(crearMiniTarjeta("Total Abonado", etiquetaTotalPagado, new Color(21, 128, 61)));
        panelTarjetasKpi.add(crearMiniTarjeta("Saldo Deudor Actual", etiquetaSaldoPendiente, new Color(217, 119, 6)));

        JPanel panelDatosSuperiores = new JPanel(new BorderLayout(0, 8));
        panelDatosSuperiores.setOpaque(false);
        panelDatosSuperiores.add(panelFichaVecino, BorderLayout.NORTH);
        panelDatosSuperiores.add(panelTarjetasKpi, BorderLayout.SOUTH);
        panelCentralContenedor.add(panelDatosSuperiores, BorderLayout.NORTH);

        // 3. TABLA DE CUOTAS DEL VECINO
        String[] columnas = {"ID Cuota", "Monto Base", "Saldo Pendiente", "Estado", "Vencimiento"};
        modeloTablaCuotas = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };

        tablaCuotasVecino = new JTable(modeloTablaCuotas);
        EstilosUI.estilizarTabla(tablaCuotasVecino, EstilosUI.ACENTO_COBRANZAS);

        DefaultTableCellRenderer renderizadorCentro = new DefaultTableCellRenderer();
        renderizadorCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaCuotasVecino.getColumnModel().getColumn(0).setCellRenderer(renderizadorCentro);
        tablaCuotasVecino.getColumnModel().getColumn(3).setCellRenderer(new RenderEstadoCuota());
        tablaCuotasVecino.getColumnModel().getColumn(4).setCellRenderer(renderizadorCentro);

        DefaultTableCellRenderer renderizadorDerecha = new DefaultTableCellRenderer();
        renderizadorDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaCuotasVecino.getColumnModel().getColumn(1).setCellRenderer(renderizadorDerecha);
        tablaCuotasVecino.getColumnModel().getColumn(2).setCellRenderer(renderizadorDerecha);

        JScrollPane panelDesplazable = new JScrollPane(tablaCuotasVecino);
        panelDesplazable.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        panelDesplazable.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        panelCentralContenedor.add(panelDesplazable, BorderLayout.CENTER);

        add(panelCentralContenedor, BorderLayout.CENTER);

        // 4. PANEL DE BOTONES SUR
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelSur.setOpaque(false);

        JButton botonCalcularGasto = new JButton("Calcular Consumo Agua y Luz");
        EstilosUI.estilizarBoton(botonCalcularGasto, EstilosUI.ACENTO_COBRANZAS);
        botonCalcularGasto.addActionListener(evento -> {
            DialogoCalculoConsumo dialogoConsumo = new DialogoCalculoConsumo(this, fachada, this::cargarEstadoCuenta);
            dialogoConsumo.setVisible(true);
        });

        JButton botonReportePdf = new JButton("Reportes PDF");
        EstilosUI.estilizarBotonSecundario(botonReportePdf, EstilosUI.DORADO_PRINCIPAL);
        botonReportePdf.addActionListener(evento -> {
            DialogoGeneradorReportes dialogoReporte = new DialogoGeneradorReportes(this, null, fachada);
            dialogoReporte.setVisible(true);
        });

        JButton botonCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(botonCerrar, EstilosUI.ACENTO_COBRANZAS);
        botonCerrar.addActionListener(evento -> dispose());

        panelSur.add(botonReportePdf);
        panelSur.add(botonCalcularGasto);
        panelSur.add(botonCerrar);
        add(panelSur, BorderLayout.SOUTH);
        com.condominio.gui.animacion.AnimadorDialogo.deslizarEntrada((JComponent) getContentPane());
    }

    private JPanel crearMiniTarjeta(String titulo, JLabel etiquetaValor, Color colorAcento) {
        GlassPanel panelTarjetaMini = new GlassPanel(new BorderLayout(0, 4), 18, colorAcento, true);
        panelTarjetaMini.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel etiquetaTituloTarjeta = new JLabel(titulo, SwingConstants.CENTER);
        etiquetaTituloTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiquetaTituloTarjeta.setForeground(EstilosUI.TEXTO_MUTED);

        etiquetaValor.setFont(new Font("Segoe UI", Font.BOLD, 15));
        etiquetaValor.setForeground(colorAcento);

        panelTarjetaMini.add(etiquetaTituloTarjeta, BorderLayout.NORTH);
        panelTarjetaMini.add(etiquetaValor, BorderLayout.CENTER);
        return panelTarjetaMini;
    }

    private void cargarDepartamentos(String numeroDepartamentoInicial) {
        comboDepartamentos.removeAllItems();
        if (fachada == null) {
            comboDepartamentos.addItem("101");
            return;
        }

        List<InmuebleDTO> listaInmuebles = fachada.listarInmuebles();
        int indiceInicial = 0;
        int contador = 0;

        for (InmuebleDTO inmueble : listaInmuebles) {
            String etiqueta = inmueble.getNumero() + " (" + inmueble.getNombreTorre() + " - " + inmueble.getNombreEdificio() + ")";
            comboDepartamentos.addItem(etiqueta);
            if (numeroDepartamentoInicial != null && (numeroDepartamentoInicial.trim().equalsIgnoreCase(inmueble.getNumero().trim()) || etiqueta.startsWith(numeroDepartamentoInicial.trim() + " "))) {
                indiceInicial = contador;
            }
            contador++;
        }

        if (comboDepartamentos.getItemCount() > 0) {
            comboDepartamentos.setSelectedIndex(indiceInicial);
        }
    }

    public void cargarEstadoCuenta() {
        String itemSeleccionado = (String) comboDepartamentos.getSelectedItem();
        if (itemSeleccionado == null || fachada == null) return;

        String numeroDepartamento = itemSeleccionado.contains(" (")
                ? itemSeleccionado.substring(0, itemSeleccionado.indexOf(" (")).trim()
                : itemSeleccionado.trim();

        // 1. Buscar residente asociado
        List<ResidenteDTO> listaResidentes = fachada.listarResidentes();
        ResidenteDTO residenteEncontrado = null;
        for (ResidenteDTO residente : listaResidentes) {
            if (numeroDepartamento.equalsIgnoreCase(residente.getNumeroDepartamento())) {
                residenteEncontrado = residente;
                break;
            }
        }

        if (residenteEncontrado != null) {
            etiquetaNombreResidente.setText("Residente: " + residenteEncontrado.getNombres() + " " + residenteEncontrado.getApellidos() + " (" + residenteEncontrado.getTipoUsuario() + ")");
            etiquetaDniResidente.setText("DNI: " + residenteEncontrado.getDni());
            etiquetaContactoResidente.setText("Tel: " + (residenteEncontrado.getTelefono() != null ? residenteEncontrado.getTelefono() : "S/N") + " | Correo: " + (residenteEncontrado.getCorreoElectronico() != null ? residenteEncontrado.getCorreoElectronico() : "S/N"));
        } else {
            etiquetaNombreResidente.setText("Residente: Sin residente registrado actualmente");
            etiquetaDniResidente.setText("DNI: -");
            etiquetaContactoResidente.setText("Contacto: -");
        }

        // 2. Buscar datos del inmueble
        List<InmuebleDTO> listaInmuebles = fachada.listarInmuebles();
        for (InmuebleDTO inmueble : listaInmuebles) {
            if (numeroDepartamento.equalsIgnoreCase(inmueble.getNumero())) {
                etiquetaUbicacionInmueble.setText("Inmueble: Depto " + inmueble.getNumero() + " (" + inmueble.getNombreTorre() + " - " + inmueble.getNombreEdificio() + " | " + inmueble.getMetrosCuadrados() + " m²)");
                break;
            }
        }

        // 3. Buscar cuotas del departamento
        List<CuotaDTO> listaCuotas = fachada.listarTodasLasCuotas();
        modeloTablaCuotas.setRowCount(0);

        double totalFacturado = 0.0;
        double saldoPendiente = 0.0;

        for (CuotaDTO cuota : listaCuotas) {
            if (cuota.getNumeroDepartamento() != null && cuota.getNumeroDepartamento().contains(numeroDepartamento)) {
                double montoBase = cuota.getMontoBase() != null ? cuota.getMontoBase() : 0.0;
                double montoSaldo = cuota.getMontoFinal() != null ? cuota.getMontoFinal() : 0.0;
                totalFacturado += montoBase;
                saldoPendiente += montoSaldo;

                modeloTablaCuotas.addRow(new Object[]{
                        cuota.getIdCuota(),
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

        if (saldoPendiente > 0) {
            etiquetaSaldoPendiente.setForeground(EstilosUI.ROJO_ERROR_TEXTO);
        } else {
            etiquetaSaldoPendiente.setForeground(EstilosUI.VERDE_EXITO);
        }
    }
}
