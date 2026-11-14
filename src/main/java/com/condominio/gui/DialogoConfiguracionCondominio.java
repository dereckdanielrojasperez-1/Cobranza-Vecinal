package com.condominio.gui;

import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.ManejadorErroresUI;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.singleton.ConfiguracionCondominio;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Diálogo de Configuración de Políticas y Parámetros Operacionales del Condominio.
@Slf4j
public class DialogoConfiguracionCondominio extends JDialog {

    private JTextField txtTasaMora;
    private JTextField txtDiasGracia;
    private JTextField txtDiasCorte;
    private JTextField txtCuotaBase;
    private JTextField txtCostoParrilla;
    private JTextField txtCostoSalon;
    private JTextField txtNombreCondominio;
    private JTextField txtRuc;
    private JTextField txtTelefonoEmergencia;
    private JTextField txtCorreoTesoreria;
    private final CobranzaFachada fachada;

    public DialogoConfiguracionCondominio(Window propietario) {
        this(propietario, null);
    }

    public DialogoConfiguracionCondominio(Window propietario, CobranzaFachada fachada) {
        super(propietario,
                SesionActual.esAdministrador()
                        ? "Políticas y Parámetros Operacionales del Condominio - Cobranza Vecinal"
                        : "Tarifas Oficiales y Directorio de Contacto - Cobranza Vecinal",
                ModalityType.APPLICATION_MODAL);
        this.fachada = fachada;
        configurarVentana();
        inicializarComponentes();
        cargarValoresActuales();
    }

    private void configurarVentana() {
        setSize(780, 560);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        if (!SesionActual.esAdministrador()) {
            ManejadorErroresUI.mostrarAdvertencia(getOwner(), "Acceso denegado: Se requiere rol de ADMINISTRADOR para acceder a las Políticas y Parámetros del Condominio.");
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        boolean esAdmin = SesionActual.esAdministrador();

        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. BANNER SUPERIOR INFORMATIVO
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.DORADO_PRINCIPAL);
        JLabel tituloBanner = new JLabel(esAdmin
                ? "POLÍTICAS FINANCIERAS Y PARÁMETROS OPERACIONALES"
                : "DIRECTORIO DE TARIFAS Y CONTACTO DE EMERGENCIA");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel subtituloBanner = new JLabel(esAdmin
                ? "Ajuste de tasas de mora, periodos de gracia, cuota base y datos institucionales del condominio."
                : "Modo Consulta Informativa | Parámetros oficiales vigentes fijados por la Administración General.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. PANEL CENTRAL EN DOS TARJETAS DE VIDRIO (FINANZAS Y DATOS INSTITUCIONALES)
        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 14, 0));
        panelCentro.setOpaque(false);

        // Tarjeta 1: Parámetros Financieros y Cobranzas
        GlassPanel tarjetaFinanciera = new GlassPanel(20, false, EstilosUI.DORADO_PRINCIPAL);
        tarjetaFinanciera.setLayout(new BorderLayout(0, 10));
        tarjetaFinanciera.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitFinanzas = new JLabel(esAdmin
                ? "PARÁMETROS DE COBRANZA Y RECUPERACIÓN"
                : "TARIFAS VIGENTES Y COBRANZAS (SOLO LECTURA)");
        lblTitFinanzas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitFinanzas.setForeground(EstilosUI.DORADO_PRINCIPAL);
        tarjetaFinanciera.add(lblTitFinanzas, BorderLayout.NORTH);

        JPanel formFinanzas = new JPanel(new GridLayout(6, 2, 8, 10));
        formFinanzas.setOpaque(false);

        txtTasaMora = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtDiasGracia = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtDiasCorte = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtCuotaBase = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtCostoParrilla = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);
        txtCostoSalon = EstilosUI.crearCampoTexto(EstilosUI.DORADO_PRINCIPAL);

        if (!esAdmin) {
            bloquearCampo(txtTasaMora);
            bloquearCampo(txtDiasGracia);
            bloquearCampo(txtDiasCorte);
            bloquearCampo(txtCuotaBase);
            bloquearCampo(txtCostoParrilla);
            bloquearCampo(txtCostoSalon);
        }

        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Tasa Mora Mensual (%):"));
        formFinanzas.add(txtTasaMora);
        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Días de Gracia sin Mora:"));
        formFinanzas.add(txtDiasGracia);
        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Días Notificación Corte:"));
        formFinanzas.add(txtDiasCorte);
        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Cuota Base Ordinaria (S/):"));
        formFinanzas.add(txtCuotaBase);
        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Tarifa Reserva BBQ (S/):"));
        formFinanzas.add(txtCostoParrilla);
        formFinanzas.add(EstilosUI.crearEtiquetaCampo("Tarifa Salón Social (S/):"));
        formFinanzas.add(txtCostoSalon);

        tarjetaFinanciera.add(formFinanzas, BorderLayout.CENTER);
        panelCentro.add(tarjetaFinanciera);

        // Tarjeta 2: Datos Institucionales y Contacto de Emergencia
        GlassPanel tarjetaInstitucional = new GlassPanel(20, false, EstilosUI.ACENTO_COBRANZAS);
        tarjetaInstitucional.setLayout(new BorderLayout(0, 10));
        tarjetaInstitucional.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitInst = new JLabel(esAdmin
                ? "IDENTIFICACIÓN DEL COMPLEJO Y EMERGENCIAS"
                : "CONTACTO DE EMERGENCIA Y GARITA (OFICIAL)");
        lblTitInst.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitInst.setForeground(EstilosUI.ACENTO_COBRANZAS);
        tarjetaInstitucional.add(lblTitInst, BorderLayout.NORTH);

        JPanel formInst = new JPanel(new GridLayout(4, 2, 8, 12));
        formInst.setOpaque(false);

        txtNombreCondominio = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtRuc = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtTelefonoEmergencia = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);
        txtCorreoTesoreria = EstilosUI.crearCampoTexto(EstilosUI.ACENTO_COBRANZAS);

        if (!esAdmin) {
            bloquearCampo(txtNombreCondominio);
            bloquearCampo(txtRuc);
            bloquearCampo(txtTelefonoEmergencia);
            bloquearCampo(txtCorreoTesoreria);
        }

        formInst.add(EstilosUI.crearEtiquetaCampo("Nombre del Complejo:"));
        formInst.add(txtNombreCondominio);
        formInst.add(EstilosUI.crearEtiquetaCampo("RUC de la Junta:"));
        formInst.add(txtRuc);
        formInst.add(EstilosUI.crearEtiquetaCampo("Teléfono Garita / Emergencia:"));
        formInst.add(txtTelefonoEmergencia);
        formInst.add(EstilosUI.crearEtiquetaCampo("Correo de Tesorería:"));
        formInst.add(txtCorreoTesoreria);

        tarjetaInstitucional.add(formInst, BorderLayout.CENTER);
        panelCentro.add(tarjetaInstitucional);

        add(panelCentro, BorderLayout.CENTER);

        // 3. BARRA INFERIOR DE ACCIONES
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setOpaque(false);

        JLabel lblNota = new JLabel(esAdmin
                ? "Los cambios en las tasas se aplican a las nuevas cuotas y cobranzas mensuales generadas."
                : "Modo de solo lectura para residentes. Para consultar o solicitar gestiones, contacte a Administración.");
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        panelInferior.add(lblNota, BorderLayout.WEST);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        if (esAdmin) {
            JButton btnGuardar = new JButton("Guardar Parámetros");
            EstilosUI.estilizarBoton(btnGuardar, EstilosUI.DORADO_PRINCIPAL);
            btnGuardar.addActionListener(e -> ejecutarGuardadoParametros());
            panelBotones.add(btnGuardar);
        }

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(btnCerrar, EstilosUI.TEXTO_SECUNDARIO);
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);

        panelInferior.add(panelBotones, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void bloquearCampo(JTextField campo) {
        campo.setEditable(false);
        campo.setFocusable(true);
        campo.setCaretColor(new Color(0, 0, 0, 0));
        campo.setForeground(new Color(0xCF, 0xD8, 0xDC));
        campo.setBackground(new Color(0x12, 0x1A, 0x24));
    }

    private void cargarValoresActuales() {
        ConfiguracionCondominio config = ConfiguracionCondominio.obtenerInstancia();
        txtTasaMora.setText(String.format("%.1f", config.getTasaInteresMora()));
        txtDiasGracia.setText(String.valueOf(config.getDiasGracia()));
        txtDiasCorte.setText(String.valueOf(config.getDiasParaMora()));
        txtCuotaBase.setText("200.00");
        txtCostoParrilla.setText("50.00");
        txtCostoSalon.setText("100.00");

        txtNombreCondominio.setText("Condominio Residencial Los Sauces");
        txtRuc.setText("20608974512");
        txtTelefonoEmergencia.setText("987-654-321");
        txtCorreoTesoreria.setText("cobranza@condominio-vecinal.pe");
    }

    private void ejecutarGuardadoParametros() {
        if (!SesionActual.esAdministrador()) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Permiso denegado: Solo el Administrador puede modificar los parámetros del condominio.");
            return;
        }

        try {
            double tasa = Double.parseDouble(txtTasaMora.getText().trim());
            int dias = Integer.parseInt(txtDiasGracia.getText().trim());
            int corte = Integer.parseInt(txtDiasCorte.getText().trim());
            double cuota = Double.parseDouble(txtCuotaBase.getText().trim());

            if (tasa < 0 || dias < 0 || corte < 0 || cuota <= 0) {
                ManejadorErroresUI.mostrarAdvertencia(this, "Los valores numéricos deben ser positivos y mayores a cero.");
                return;
            }

            // Defensa en profundidad: Invocación protegida en capa de servicio/fachada
            if (fachada != null) {
                fachada.actualizarParametrosCondominio(tasa, dias, corte);
            }

            ManejadorErroresUI.mostrarExito(
                    this,
                    "Políticas operacionales y parámetros del condominio actualizados con éxito.\n" +
                    "Tasa de mora: " + tasa + "% | Días de gracia: " + dias + " | Cuota ordinaria base: S/ " + String.format("%.2f", cuota),
                    "Parámetros Actualizados"
            );
            dispose();
        } catch (SecurityException secEx) {
            ManejadorErroresUI.mostrarError(this, "Violación de Seguridad RBAC", secEx);
        } catch (NumberFormatException ex) {
            ManejadorErroresUI.mostrarAdvertencia(this, "Verifique que los campos numéricos contengan valores válidos.");
        }
    }
}
