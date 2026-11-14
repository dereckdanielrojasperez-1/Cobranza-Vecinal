package com.condominio.gui;

import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Tablón Oficial de Comunicados y Circulares Condominales.
@Slf4j
public class DialogoComunicados extends JDialog {

    private final CobranzaFachada fachada;
    private JTable tablaComunicados;
    private DefaultTableModel modeloTabla;
    private JTextField txtTitulo;
    private JComboBox<String> comboTipo;
    private JTextArea txtCuerpoMensaje;
    private JTextArea txtDetalleLectura;
    private JLabel lblContador;

    public DialogoComunicados(Window propietario, CobranzaFachada fachada) {
        super(propietario, "Tablón Oficial de Comunicados y Circulares - Cobranza Vecinal", ModalityType.APPLICATION_MODAL);
        this.fachada = fachada != null ? fachada : new CobranzaFachada();
        configurarVentana();
        inicializarComponentes();
        cargarDatosComunicados();
    }

    private void configurarVentana() {
        setSize(960, 680);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. BANNER SUPERIOR INFORMATIVO
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_INMUEBLES);
        JLabel tituloBanner = new JLabel("TABLÓN OFICIAL DE COMUNICADOS Y CIRCULARES A LA COMUNIDAD");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.ACENTO_INMUEBLES);

        JLabel subtituloBanner = new JLabel("Emisión y consulta de avisos formales, convocatorias a asambleas y alertas de mantenimiento.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. PANEL CENTRAL DIVIDIDO: TABLA (IZQUIERDA) Y VISTA PREVIA (DERECHA)
        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCentral.setOpaque(false);
        splitCentral.setBorder(null);
        splitCentral.setResizeWeight(0.55);

        // Lado Izquierdo: Tabla de comunicados
        String[] columnas = {"ID", "Fecha", "Prioridad", "Título del Comunicado", "Autor"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaComunicados = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.ACENTO_INMUEBLES);
        tablaComunicados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaComunicados.getColumnModel().getColumn(0).setPreferredWidth(45);
        tablaComunicados.getColumnModel().getColumn(0).setCellRenderer(renderCentro);
        tablaComunicados.getColumnModel().getColumn(1).setPreferredWidth(85);
        tablaComunicados.getColumnModel().getColumn(1).setCellRenderer(renderCentro);
        tablaComunicados.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaComunicados.getColumnModel().getColumn(2).setCellRenderer(renderCentro);
        tablaComunicados.getColumnModel().getColumn(3).setPreferredWidth(230);
        tablaComunicados.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollTabla = new JScrollPane(tablaComunicados);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));

        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 6));
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.add(scrollTabla, BorderLayout.CENTER);

        lblContador = new JLabel("Cargando comunicados...");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContador.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelIzquierdo.add(lblContador, BorderLayout.SOUTH);

        splitCentral.setLeftComponent(panelIzquierdo);

        // Lado Derecho: Lector de Comunicado en Vidrio
        GlassPanel panelLector = new GlassPanel(20, false, EstilosUI.ACENTO_INMUEBLES);
        panelLector.setLayout(new BorderLayout(0, 8));
        panelLector.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitLector = new JLabel("LECTURA DEL COMUNICADO SELECCIONADO");
        lblTitLector.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitLector.setForeground(EstilosUI.ACENTO_INMUEBLES);
        panelLector.add(lblTitLector, BorderLayout.NORTH);

        txtDetalleLectura = new JTextArea("Seleccione un comunicado de la tabla para leer el texto completo.");
        txtDetalleLectura.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDetalleLectura.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        txtDetalleLectura.setBackground(EstilosUI.FONDO_CAMPO);
        txtDetalleLectura.setLineWrap(true);
        txtDetalleLectura.setWrapStyleWord(true);
        txtDetalleLectura.setEditable(false);
        txtDetalleLectura.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scrollLectura = new JScrollPane(txtDetalleLectura);
        scrollLectura.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1));
        panelLector.add(scrollLectura, BorderLayout.CENTER);

        splitCentral.setRightComponent(panelLector);
        add(splitCentral, BorderLayout.CENTER);

        // 3. SELECCIÓN DE TABLA PARA MOSTRAR TEXTO
        tablaComunicados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaComunicados.getSelectedRow() >= 0) {
                int f = tablaComunicados.getSelectedRow();
                String id = String.valueOf(tablaComunicados.getValueAt(f, 0));
                String fecha = String.valueOf(tablaComunicados.getValueAt(f, 1));
                String prioridad = String.valueOf(tablaComunicados.getValueAt(f, 2));
                String tit = String.valueOf(tablaComunicados.getValueAt(f, 3));
                String autor = String.valueOf(tablaComunicados.getValueAt(f, 4));

                Comunicado c = obtenerComunicadoPorId(Long.valueOf(id));
                String cuerpo = (c != null && c.getMensaje() != null) ? c.getMensaje() : "(Sin contenido registrado)";

                txtDetalleLectura.setText(
                        "ASUNTO: " + tit + "\n" +
                        "FECHA: " + fecha + "   |   TIPO: " + prioridad + "   |   EMITIDO POR: " + autor + "\n\n" +
                        cuerpo + "\n\n" +
                        "Administración del Condominio | Atención de Consultas en Garita y Correo Oficial."
                );
                txtDetalleLectura.setCaretPosition(0);
            }
        });

        // 4. FORMULARIO INFERIOR DE PUBLICACIÓN (VISIBLE PARA ADMINISTRADOR Y CONSERJE)
        JPanel panelInferior = new JPanel(new BorderLayout(0, 8));
        panelInferior.setOpaque(false);

        if (SesionActual.esAdministrador() || SesionActual.esConserje()) {
            GlassPanel panelEmision = new GlassPanel(20, false, EstilosUI.ACENTO_INMUEBLES);
            panelEmision.setLayout(new BorderLayout(10, 8));
            panelEmision.setBorder(new EmptyBorder(12, 14, 12, 14));

            JLabel lblTitEmision = new JLabel("PUBLICAR NUEVO COMUNICADO OFICIAL A LA COMUNIDAD");
            lblTitEmision.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTitEmision.setForeground(EstilosUI.DORADO_PRINCIPAL);
            panelEmision.add(lblTitEmision, BorderLayout.NORTH);

            JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            panelCampos.setOpaque(false);

            panelCampos.add(EstilosUI.crearEtiquetaCampo("Tipo / Prioridad:"));
            comboTipo = EstilosUI.crearCombo();
            comboTipo.addItem("INFORMATIVO");
            comboTipo.addItem("URGENTE");
            comboTipo.addItem("MANTENIMIENTO");
            comboTipo.addItem("ASAMBLEA");
            comboTipo.setPreferredSize(new Dimension(140, 30));
            EstilosUI.estilizarCombo(comboTipo, EstilosUI.ACENTO_INMUEBLES);
            panelCampos.add(comboTipo);

            panelCampos.add(EstilosUI.crearEtiquetaCampo("Título / Asunto:"));
            txtTitulo = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_INMUEBLES);
            txtTitulo.setPreferredSize(new Dimension(380, 30));
            panelCampos.add(txtTitulo);

            panelEmision.add(panelCampos, BorderLayout.WEST);

            txtCuerpoMensaje = new JTextArea(2, 40);
            txtCuerpoMensaje.setFont(EstilosUI.FUENTE_TEXTO);
            txtCuerpoMensaje.setBackground(EstilosUI.FONDO_CAMPO);
            txtCuerpoMensaje.setForeground(EstilosUI.TEXTO_PRINCIPAL);
            txtCuerpoMensaje.setLineWrap(true);
            txtCuerpoMensaje.setWrapStyleWord(true);
            txtCuerpoMensaje.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA, 1),
                    new EmptyBorder(6, 8, 6, 8)
            ));

            JScrollPane scrollCuerpo = new JScrollPane(txtCuerpoMensaje);
            scrollCuerpo.setPreferredSize(new Dimension(500, 46));
            scrollCuerpo.setBorder(null);
            panelEmision.add(scrollCuerpo, BorderLayout.CENTER);

            JPanel panelBotonPublicar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
            panelBotonPublicar.setOpaque(false);
            JButton btnPublicar = new JButton("Publicar y Difundir");
            EstilosUI.estilizarBoton(btnPublicar, EstilosUI.ACENTO_INMUEBLES);
            btnPublicar.addActionListener(e -> ejecutarPublicacion());
            panelBotonPublicar.add(btnPublicar);
            panelEmision.add(panelBotonPublicar, BorderLayout.EAST);

            panelInferior.add(panelEmision, BorderLayout.CENTER);
        }

        // Botones de acción general (Refrescar y Cerrar)
        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesAccion.setOpaque(false);

        JButton btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBotonSecundario(btnRefrescar, EstilosUI.TEXTO_SECUNDARIO);
        btnRefrescar.addActionListener(e -> cargarDatosComunicados());
        panelBotonesAccion.add(btnRefrescar);

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.TEXTO_SECUNDARIO);
        btnCerrar.addActionListener(e -> dispose());
        panelBotonesAccion.add(btnCerrar);

        panelInferior.add(panelBotonesAccion, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatosComunicados() {
        lblContador.setText("Actualizando comunicados...");
        SwingWorker<List<Comunicado>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Comunicado> doInBackground() {
                List<Comunicado> lista = fachada.listarComunicados();
                if (lista == null || lista.isEmpty()) {
                    // Sembrar 3 comunicados iniciales realistas si la base está limpia
                    fachada.publicarComunicado(
                            "Convocatoria a Asamblea General Ordinaria",
                            "Se convoca a todos los propietarios del Condominio a la Asamblea Ordinaria a celebrarse el próximo sábado a las 18:00 hrs en el Salón Social. Orden del día: Aprobación del Balance Anual y Presupuesto de Mantenimiento 2027.",
                            "Junta Directiva"
                    );
                    fachada.publicarComunicado(
                            "Mantenimiento Programado de Bombas y Cisterna",
                            "Por la presente se notifica que el día martes de 09:00 a 13:00 hrs se realizará la limpieza y desinfección semestral de la cisterna principal y revisión de bombas de presión. Se solicita tomar las previsiones necesarias.",
                            "Administración"
                    );
                    fachada.publicarComunicado(
                            "Recordatorio de Convivencia: Horarios de Ruido y Mascotas",
                            "Estimados vecinos, recordamos respetar el horario de descanso (22:00 a 08:00 hrs) y mantener a las mascotas con correa en las áreas comunes y ascensores para una óptima convivencia comunitaria.",
                            "Conserjería"
                    );
                    return fachada.listarComunicados();
                }
                return lista;
            }

            @Override
            protected void done() {
                try {
                    List<Comunicado> lista = get();
                    modeloTabla.setRowCount(0);
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    if (lista != null) {
                        for (Comunicado c : lista) {
                            String fechaStr = c.getFechaPublicacion() != null ? c.getFechaPublicacion().format(fmt) : LocalDate.now().format(fmt);
                            String tipo = "INFORMATIVO";
                            if (c.getTitulo().toUpperCase().contains("ASAMBLEA")) tipo = "ASAMBLEA";
                            else if (c.getTitulo().toUpperCase().contains("MANTENIMIENTO")) tipo = "MANTENIMIENTO";
                            else if (c.getTitulo().toUpperCase().contains("URGENTE")) tipo = "URGENTE";

                            modeloTabla.addRow(new Object[]{
                                    c.getId(),
                                    fechaStr,
                                    tipo,
                                    c.getTitulo(),
                                    c.getAutor() != null ? c.getAutor() : "Administración"
                            });
                        }
                    }
                    lblContador.setText("Mostrando " + (lista != null ? lista.size() : 0) + " comunicados oficiales registrados");
                    if (tablaComunicados.getRowCount() > 0) {
                        tablaComunicados.setRowSelectionInterval(0, 0);
                    }
                } catch (Exception ex) {
                    log.error("Error al cargar comunicados: {}", ex.getMessage());
                    lblContador.setText("Error al cargar comunicados.");
                }
            }
        };
        worker.execute();
    }

    private Comunicado obtenerComunicadoPorId(Long id) {
        if (id == null) return null;
        List<Comunicado> lista = fachada.listarComunicados();
        if (lista != null) {
            for (Comunicado c : lista) {
                if (id.equals(c.getId())) {
                    return c;
                }
            }
        }
        return null;
    }

    private void ejecutarPublicacion() {
        String tit = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
        String msg = txtCuerpoMensaje.getText() != null ? txtCuerpoMensaje.getText().trim() : "";
        String tipo = (String) comboTipo.getSelectedItem();

        if (tit.isBlank() || msg.isBlank()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Debe ingresar el título y el cuerpo del comunicado a emitir.");
            return;
        }

        String autor = SesionActual.getNombreUsuario() != null ? SesionActual.getNombreUsuario() : "Administración";
        String tituloFinal = "[" + tipo + "] " + tit;

        try {
            fachada.publicarComunicado(tituloFinal, msg, autor);
            txtTitulo.setText("");
            txtCuerpoMensaje.setText("");
            ManejadorErroresUI.mostrarExito(this, "Comunicado oficial publicado y notificado correctamente.", "Publicación Exitosa");
            cargarDatosComunicados();
        } catch (Exception ex) {
            ManejadorErroresUI.mostrarError(this, "Error al publicar comunicado: " + ex.getMessage(), ex);
        }
    }
}
