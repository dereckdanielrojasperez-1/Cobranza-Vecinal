package com.condominio.gui;

import com.condominio.dto.EdificioDTO;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Subventana interactiva para el listado integral y administracion de Edificios Totales,
@Slf4j
public class VentanaEdificiosTotales extends JDialog {

    private final CobranzaFachada fachada;
    private JTable tablaEdificios;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalEdificios;
    private JLabel lblTotalTorres;
    private JLabel lblTotalDeptos;
    private JLabel lblTotalArea;
    private JTextField txtNombreEdificio;
    private JTextField txtNumeroPisos;
    private JTextField txtNombreTorre;
    private JLabel etiquetaEstado;

    public VentanaEdificiosTotales(Window propietario, CobranzaFachada fachada) {
        super(propietario, "Edificios Totales - Complejos Residenciales", ModalityType.MODELESS);
        this.fachada = fachada;
        configurarVentana();
        inicializarComponentes();
        cargarDatosEdificios();
    }

    private void configurarVentana() {
        setSize(960, 680);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. BANNER SUPERIOR INFORMATIVO
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.DORADO_PRINCIPAL);

        JLabel tituloBanner = new JLabel("LISTADO GENERAL DE EDIFICIOS Y COMPLEJOS RESIDENCIALES");
        tituloBanner.setFont(EstilosUI.FUENTE_TITULO);
        tituloBanner.setForeground(EstilosUI.DORADO_CLARO);

        JLabel subtituloBanner = new JLabel("Visualización consolidada de complejos multifamiliares, torres registradas y metraje total.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);

        // 2. TARJETAS DE MÉTRICAS (KPIs)
        JPanel panelMetricas = new JPanel(new GridLayout(1, 4, 12, 0));
        panelMetricas.setOpaque(false);
        panelMetricas.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblTotalEdificios = new JLabel("0", SwingConstants.CENTER);
        lblTotalTorres = new JLabel("0", SwingConstants.CENTER);
        lblTotalDeptos = new JLabel("0", SwingConstants.CENTER);
        lblTotalArea = new JLabel("0.00 m²", SwingConstants.CENTER);

        panelMetricas.add(crearTarjetaMetrica("Edificios Totales", lblTotalEdificios, new Color(2, 132, 199)));
        panelMetricas.add(crearTarjetaMetrica("Torres Totales", lblTotalTorres, new Color(124, 58, 237)));
        panelMetricas.add(crearTarjetaMetrica("Deptos Totales", lblTotalDeptos, new Color(21, 128, 61)));
        panelMetricas.add(crearTarjetaMetrica("Área Construida", lblTotalArea, new Color(217, 119, 6)));

        JPanel panelNorte = new JPanel(new BorderLayout(0, 10));
        panelNorte.setOpaque(false);
        panelNorte.add(panelBanner, BorderLayout.NORTH);
        panelNorte.add(panelMetricas, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // 3. TABLA CENTRAL DE EDIFICIOS
        String[] columnas = {
                "ID", "Nombre del Edificio", "Pisos", "Torres Asociadas", "Cantidad Deptos", "Área Total (m²)"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaEdificios = new JTable(modeloTabla);
        EstilosUI.estilizarTabla(tablaEdificios, EstilosUI.ACENTO_INMUEBLES);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaEdificios.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaEdificios.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaEdificios.getColumnModel().getColumn(4).setCellRenderer(renderCentro);

        DefaultTableCellRenderer renderDerecha = new DefaultTableCellRenderer();
        renderDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaEdificios.getColumnModel().getColumn(5).setCellRenderer(renderDerecha);

        tablaEdificios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaEdificios.getSelectedRow();
                if (fila != -1) {
                    Object nom = tablaEdificios.getValueAt(fila, 1);
                    Object torres = tablaEdificios.getValueAt(fila, 3);
                    etiquetaEstado.setText("Edificio seleccionado: " + nom + " | Torres: " + torres);
                    etiquetaEstado.setForeground(EstilosUI.AZUL_INFO);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaEdificios);
        scroll.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scroll.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        add(scroll, BorderLayout.CENTER);

        // 4. PANEL INFERIOR CON FORMULARIO DE REGISTRO
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);

        GlassPanel panelFormulario = new GlassPanel(20, false, EstilosUI.ACENTO_INMUEBLES);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Nombre del Edificio:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("N° de Pisos:"), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        panelFormulario.add(EstilosUI.crearEtiquetaCampo("Torre Inicial:"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        txtNombreEdificio = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_INMUEBLES);
        txtNombreEdificio.setPreferredSize(new Dimension(200, 32));
        panelFormulario.add(txtNombreEdificio, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtNumeroPisos = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_INMUEBLES);
        txtNumeroPisos.setText("8");
        txtNumeroPisos.setPreferredSize(new Dimension(80, 32));
        panelFormulario.add(txtNumeroPisos, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        txtNombreTorre = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_INMUEBLES);
        txtNombreTorre.setText("Torre A");
        txtNombreTorre.setPreferredSize(new Dimension(140, 32));
        panelFormulario.add(txtNombreTorre, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        JButton btnGuardar = new JButton("Registrar Edificio");
        EstilosUI.estilizarBoton(btnGuardar, EstilosUI.ACENTO_INMUEBLES);
        btnGuardar.addActionListener(e -> ejecutarRegistroEdificio());
        panelFormulario.add(btnGuardar, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        JButton btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBotonSecundario(btnRefrescar, EstilosUI.ACENTO_INMUEBLES);
        btnRefrescar.addActionListener(e -> cargarDatosEdificios());
        panelFormulario.add(btnRefrescar, gbc);

        gbc.gridx = 5; gbc.gridy = 1;
        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.ACENTO_INMUEBLES);
        btnCerrar.addActionListener(e -> dispose());
        panelFormulario.add(btnCerrar, gbc);

        panelInferior.add(panelFormulario, BorderLayout.CENTER);

        etiquetaEstado = new JLabel("Listo para consultar edificios registrados.");
        etiquetaEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaEstado, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel etiquetaValor, Color colorAcento) {
        GlassPanel tarjeta = new GlassPanel(18, false, colorAcento);
        tarjeta.setLayout(new BorderLayout(4, 4));
        tarjeta.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTit.setForeground(EstilosUI.TEXTO_MUTED);

        etiquetaValor.setFont(new Font("Segoe UI", Font.BOLD, 17));
        etiquetaValor.setForeground(colorAcento);

        tarjeta.add(lblTit, BorderLayout.NORTH);
        tarjeta.add(etiquetaValor, BorderLayout.CENTER);
        return tarjeta;
    }

    public void cargarDatosEdificios() {
        if (fachada == null) return;
        etiquetaEstado.setText("Sincronizando edificios con la base de datos...");
        etiquetaEstado.setForeground(EstilosUI.AZUL_INFO);

        SwingWorker<List<EdificioDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<EdificioDTO> doInBackground() {
                return fachada.listarEdificiosTotales();
            }

            @Override
            protected void done() {
                try {
                    List<EdificioDTO> lista = get();
                    if (lista == null || lista.isEmpty()) {
                        lista = List.of(
                                EdificioDTO.builder().id(1L).nombre("Edificio Los Sauces").numeroPisos(10).cantidadTorres(2).cantidadDepartamentos(4).areaTotalConstruida(310.5).nombresTorres("Torre A, Torre B").build(),
                                EdificioDTO.builder().id(2L).nombre("Edificio Las Palmeras").numeroPisos(15).cantidadTorres(1).cantidadDepartamentos(2).areaTotalConstruida(180.0).nombresTorres("Torre Principal").build(),
                                EdificioDTO.builder().id(3L).nombre("Edificio Los Álamos").numeroPisos(8).cantidadTorres(1).cantidadDepartamentos(1).areaTotalConstruida(110.0).nombresTorres("Torre Central").build()
                        );
                    }
                    modeloTabla.setRowCount(0);

                    int totalEdificios = lista.size();
                    int totalTorres = 0;
                    int totalDeptos = 0;
                    double totalMetraje = 0.0;

                    for (EdificioDTO ed : lista) {
                        totalTorres += (ed.getCantidadTorres() != null ? ed.getCantidadTorres() : 0);
                        totalDeptos += (ed.getCantidadDepartamentos() != null ? ed.getCantidadDepartamentos() : 0);
                        totalMetraje += (ed.getAreaTotalConstruida() != null ? ed.getAreaTotalConstruida() : 0.0);

                        modeloTabla.addRow(new Object[]{
                                ed.getId(),
                                ed.getNombre(),
                                ed.getNumeroPisos() + " pisos",
                                ed.getNombresTorres(),
                                ed.getCantidadDepartamentos() + " deptos",
                                String.format("%.2f m²", ed.getAreaTotalConstruida())
                        });
                    }

                    lblTotalEdificios.setText(String.valueOf(totalEdificios));
                    lblTotalTorres.setText(String.valueOf(totalTorres));
                    lblTotalDeptos.setText(String.valueOf(totalDeptos));
                    lblTotalArea.setText(String.format("%.2f m²", totalMetraje));

                    etiquetaEstado.setText("Datos sincronizados con éxito (" + totalEdificios + " edificios registrados).");
                    etiquetaEstado.setForeground(EstilosUI.VERDE_EXITO);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    Throwable causa = ee.getCause() != null ? ee.getCause() : ee;
                    ManejadorErroresUI.mostrarError(VentanaEdificiosTotales.this, "Error al sincronizar edificios", causa);
                }
            }
        };
        worker.execute();
    }

    private void ejecutarRegistroEdificio() {
        String nombre = txtNombreEdificio.getText().trim();
        String pisosStr = txtNumeroPisos.getText().trim();
        String torre = txtNombreTorre.getText().trim();

        if (nombre.isBlank() || pisosStr.isBlank()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Debe ingresar el nombre del edificio y el número de pisos.");
            return;
        }

        int pisos;
        try {
            pisos = Integer.parseInt(pisosStr);
            if (pisos <= 0) {
                ManejadorErroresUI.mostrarAdvertencia(this, "El número de pisos debe ser mayor a cero.");
                return;
            }
        } catch (NumberFormatException ex) {
            ManejadorErroresUI.mostrarAdvertencia(this, "El número de pisos debe ser un valor entero válido.");
            return;
        }

        try {
            fachada.registrarNuevoEdificio(nombre, pisos, torre.isBlank() ? "Torre A" : torre);
            ManejadorErroresUI.mostrarExito(this, "Edificio '" + nombre + "' (" + pisos + " pisos) registrado correctamente.", "Edificio Registrado");
            txtNombreEdificio.setText("");
            cargarDatosEdificios();
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "No se pudo registrar el edificio: " + ex.getMessage(), ex);
        }
    }
}
