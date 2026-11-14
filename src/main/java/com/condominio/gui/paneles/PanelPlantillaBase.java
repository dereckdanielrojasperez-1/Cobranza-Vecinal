package com.condominio.gui.paneles;

import com.condominio.gui.util.EstilosUI;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Plantilla base abstracta (Patrón GoF Template Method) para paneles Swing del sistema.
@Getter
public abstract class PanelPlantillaBase extends JPanel {

    protected JTable tablaDatos;
    protected DefaultTableModel modeloTabla;
    protected JScrollPane scrollTabla;
    protected JLabel lblContadorRegistros;
    protected JPanel panelBanner;
    protected JLabel etiquetaTituloBanner;
    protected JLabel etiquetaSubtituloBanner;
    protected JPanel panelFormulario;
    protected JPanel panelBotones;
    protected JLabel etiquetaEstado;

    public PanelPlantillaBase(String titulo, String subtitulo) {
        inicializarEstructuraBase(titulo, subtitulo);
    }

    private void inicializarEstructuraBase(String titulo, String subtitulo) {
        setLayout(new BorderLayout(14, 14));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. BANNER SUPERIOR INFORMATIVO
        panelBanner = EstilosUI.crearPanelBanner(EstilosUI.ACENTO_OPERACIONES);

        etiquetaTituloBanner = new JLabel(titulo);
        etiquetaTituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        etiquetaTituloBanner.setForeground(EstilosUI.ACENTO_OPERACIONES);

        etiquetaSubtituloBanner = new JLabel(subtitulo);
        etiquetaSubtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        etiquetaSubtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(etiquetaTituloBanner, BorderLayout.NORTH);
        panelBanner.add(etiquetaSubtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. TABLA CENTRAL ESTILIZADA
        String[] columnas = inicializarColumnas();
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDatos = EstilosUI.crearTablaEstilizada(modeloTabla, EstilosUI.ACENTO_OPERACIONES);
        tablaDatos.setPreferredScrollableViewportSize(new Dimension(800, 142));

        scrollTabla = new JScrollPane(tablaDatos);
        scrollTabla.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollTabla.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollTabla.setPreferredSize(new Dimension(800, 178));

        // Panel central con tabla y contador de registros (elimina espacio muerto)
        JPanel panelCentro = new JPanel(new BorderLayout(0, 6));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelContador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelContador.setOpaque(false);
        lblContadorRegistros = new JLabel("Mostrando 0 de 0 registros");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblContadorRegistros.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelContador.add(lblContadorRegistros);
        panelCentro.add(panelContador, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // 3. PANEL INFERIOR (Formulario + Botones + Estado) ANCLADO AL FONDO
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setOpaque(false);

        panelFormulario = EstilosUI.crearPanelGlassmorphic(EstilosUI.ACENTO_OPERACIONES, 14);
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(12, 16, 12, 16));

        panelInferior.add(panelFormulario, BorderLayout.CENTER);

        etiquetaEstado = new JLabel("Listo.");
        etiquetaEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        etiquetaEstado.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(etiquetaEstado, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Hook methods
        inicializarComponentesFormulario(panelFormulario);
        inicializarAcciones();
        cargarDatosTabla();
    }

    // Define los nombres de encabezados para el JTable central.
    protected abstract String[] inicializarColumnas();

    // Construye y añade los controles de entrada (JTextField, JComboBox) al formulario.
    protected abstract void inicializarComponentesFormulario(JPanel panelFormulario);

    // Enlaza los escuchadores (ActionListeners) a los botones y componentes interactivos.
    protected abstract void inicializarAcciones();

    // Realiza la carga y sincronización de datos en la tabla (recomendado con SwingWorker).
    public abstract void cargarDatosTabla();

    // Restablece los campos de captura de datos a su estado vacío o inicial.
    public abstract void limpiarFormulario();

    // Helper para actualizar el mensaje de estado con un color semántico.
    public void establecerEstado(String mensaje, Color color) {
        etiquetaEstado.setText(mensaje);
        etiquetaEstado.setForeground(color);
    }

    // Helper para actualizar el contador de registros visual de la tabla.
    public void actualizarContadorRegistros(int mostrados, int total) {
        if (lblContadorRegistros != null) {
            lblContadorRegistros.setText("Mostrando " + mostrados + " de " + total + " registros");
        }
    }
}
