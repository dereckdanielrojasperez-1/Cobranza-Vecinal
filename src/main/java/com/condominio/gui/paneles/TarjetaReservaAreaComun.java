package com.condominio.gui.paneles;

import com.condominio.dto.ResidenteDTO;
import com.condominio.gui.util.CampoPlaceholder;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.Validaciones;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.Getter;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// Componente gráfico modular que representa la tarjeta de interfaz para el registro
@Getter
public class TarjetaReservaAreaComun extends JPanel {

    public static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_HORA_UI = DateTimeFormatter.ofPattern("HH:mm");

    private JComboBox<String> comboAreaComun;
    private JComboBox<String> comboPropietario;
    private JFormattedTextField ftfFecha;
    private JFormattedTextField ftfHoraInicio;
    private JFormattedTextField ftfHoraFin;
    private JTextField txtCantidadPersonas;

    public TarjetaReservaAreaComun() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Etiquetas
        gbc.gridx = 0; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Área Común:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Propietario / Residente:"), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Fecha (dd/mm/aaaa):"), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Hora Inicio:"), gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Hora Fin:"), gbc);

        gbc.gridx = 5; gbc.gridy = 0;
        add(EstilosUI.crearEtiquetaCampo("Invitados:"), gbc);

        // Fila 1: Controles de entrada
        // 1. Combo Área Común
        gbc.gridx = 0; gbc.gridy = 1;
        comboAreaComun = new JComboBox<>(new String[]{
                "Zona BBQ / Parrillas",
                "Salón Social Multiusos",
                "Piscina Panorámica"
        });
        comboAreaComun.setEditable(false);
        EstilosUI.estilizarCombo(comboAreaComun, EstilosUI.ACENTO_OPERACIONES);
        EstilosUI.configurarComboPrototipoYTooltip(comboAreaComun, "Salón Social Multiusos");
        comboAreaComun.setPreferredSize(new Dimension(190, 32));
        comboAreaComun.setToolTipText("Seleccione la instalación o área común a reservar");
        add(comboAreaComun, gbc);

        // 2. Combo Propietario / Residente
        gbc.gridx = 1; gbc.gridy = 1;
        comboPropietario = new JComboBox<>(new String[]{
                "Carlos Mendoza Torres",
                "Ana Paredes Ruiz",
                "Luis Ramos Gómez"
        });
        comboPropietario.setEditable(false);
        EstilosUI.estilizarCombo(comboPropietario, EstilosUI.ACENTO_OPERACIONES);
        EstilosUI.configurarComboPrototipoYTooltip(comboPropietario, "Carlos Mendoza Torres (Depto 101)");
        comboPropietario.setPreferredSize(new Dimension(210, 32));
        comboPropietario.setToolTipText("Propietario o residente titular de la reserva");
        add(comboPropietario, gbc);

        // 3. JFormattedTextField Fecha con máscara dd/MM/yyyy
        gbc.gridx = 2; gbc.gridy = 1;
        try {
            MaskFormatter mascaraFecha = new MaskFormatter("##/##/####");
            mascaraFecha.setPlaceholderCharacter('_');
            ftfFecha = new JFormattedTextField(mascaraFecha);
        } catch (ParseException pe) {
            ftfFecha = new JFormattedTextField();
        }
        EstilosUI.estilizarCampo(ftfFecha, EstilosUI.ACENTO_OPERACIONES);
        LocalDate fechaDefault = LocalDate.now().plusDays(2);
        ftfFecha.setText(fechaDefault.format(FORMATO_FECHA_UI));
        ftfFecha.setPreferredSize(new Dimension(130, 32));
        ftfFecha.setToolTipText("Fecha de la reserva (dd/mm/aaaa, no pasada y máximo 60 días)");
        add(ftfFecha, gbc);

        // 4. JFormattedTextField Hora Inicio con máscara HH:mm
        gbc.gridx = 3; gbc.gridy = 1;
        try {
            MaskFormatter mascaraHoraIni = new MaskFormatter("##:##");
            mascaraHoraIni.setPlaceholderCharacter('_');
            ftfHoraInicio = new JFormattedTextField(mascaraHoraIni);
        } catch (ParseException pe) {
            ftfHoraInicio = new JFormattedTextField();
        }
        EstilosUI.estilizarCampo(ftfHoraInicio, EstilosUI.ACENTO_OPERACIONES);
        ftfHoraInicio.setText("10:00");
        ftfHoraInicio.setPreferredSize(new Dimension(80, 32));
        ftfHoraInicio.setToolTipText("Hora de inicio en formato HH:mm (24 horas)");
        add(ftfHoraInicio, gbc);

        // 5. JFormattedTextField Hora Fin con máscara HH:mm
        gbc.gridx = 4; gbc.gridy = 1;
        try {
            MaskFormatter mascaraHoraFin = new MaskFormatter("##:##");
            mascaraHoraFin.setPlaceholderCharacter('_');
            ftfHoraFin = new JFormattedTextField(mascaraHoraFin);
        } catch (ParseException pe) {
            ftfHoraFin = new JFormattedTextField();
        }
        EstilosUI.estilizarCampo(ftfHoraFin, EstilosUI.ACENTO_OPERACIONES);
        ftfHoraFin.setText("14:00");
        ftfHoraFin.setPreferredSize(new Dimension(80, 32));
        ftfHoraFin.setToolTipText("Hora de culminación en formato HH:mm (estrictamente posterior al inicio)");
        add(ftfHoraFin, gbc);

        // 6. Cantidad de Personas
        gbc.gridx = 5; gbc.gridy = 1;
        txtCantidadPersonas = EstilosUI.crearCampoPlaceholder("Ej: 10", EstilosUI.ACENTO_OPERACIONES);
        FiltroNumerico.aplicar(txtCantidadPersonas, 3);
        txtCantidadPersonas.setPreferredSize(new Dimension(80, 32));
        txtCantidadPersonas.setToolTipText("Aforo estimado de asistentes a la reserva");
        add(txtCantidadPersonas, gbc);
    }

    // Carga dinámicamente las áreas comunes y residentes registrados desde la fachada.
    public void cargarDatosServicio(CobranzaFachada fachada) {
        if (fachada == null) return;

        try {
            List<AreaComun> areas = fachada.listarAreasComunes();
            if (areas != null && !areas.isEmpty()) {
                comboAreaComun.removeAllItems();
                for (AreaComun a : areas) {
                    if (a.getNombre() != null && !a.getNombre().isBlank()) {
                        comboAreaComun.addItem(a.getNombre().trim());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            List<ResidenteDTO> residentes = fachada.listarResidentes();
            if (residentes != null && !residentes.isEmpty()) {
                comboPropietario.removeAllItems();
                for (ResidenteDTO r : residentes) {
                    String nombreCompleto = r.getNombres() + " " + r.getApellidos();
                    String depto = (r.getNumeroDepartamento() != null && !r.getNumeroDepartamento().isBlank())
                            ? " (Depto " + r.getNumeroDepartamento() + ")" : "";
                    comboPropietario.addItem(nombreCompleto.trim() + depto);
                }
            }
        } catch (Exception ignored) {
        }

        if (comboAreaComun.getSelectedItem() != null) {
            comboAreaComun.setToolTipText(comboAreaComun.getSelectedItem().toString());
        }
        if (comboPropietario.getSelectedItem() != null) {
            comboPropietario.setToolTipText(comboPropietario.getSelectedItem().toString());
        }
    }

    public void limpiar() {
        if (ftfFecha != null) {
            ftfFecha.setText(LocalDate.now().plusDays(2).format(FORMATO_FECHA_UI));
        }
        if (ftfHoraInicio != null) {
            ftfHoraInicio.setText("10:00");
        }
        if (ftfHoraFin != null) {
            ftfHoraFin.setText("14:00");
        }
        if (txtCantidadPersonas != null) {
            txtCantidadPersonas.setText("");
        }
    }

    public LocalDate obtenerFecha() {
        String texto = ftfFecha.getText().trim();
        if (texto.contains("_") || texto.length() < 10) {
            throw new IllegalArgumentException("Debe ingresar una fecha completa en formato dd/mm/aaaa.");
        }
        try {
            return LocalDate.parse(texto, FORMATO_FECHA_UI);
        } catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException("Formato de fecha inválido. Ingrese una fecha válida dd/mm/aaaa.");
        }
    }

    public LocalTime obtenerHoraInicio() {
        String texto = ftfHoraInicio.getText().trim();
        if (texto.contains("_") || texto.length() < 5) {
            throw new IllegalArgumentException("Debe ingresar una hora de inicio completa en formato HH:mm.");
        }
        try {
            return LocalTime.parse(texto, FORMATO_HORA_UI);
        } catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException("Hora de inicio inválida. Use formato 24 horas (ejemplo: 09:30 o 15:00).");
        }
    }

    public LocalTime obtenerHoraFin() {
        String texto = ftfHoraFin.getText().trim();
        if (texto.contains("_") || texto.length() < 5) {
            throw new IllegalArgumentException("Debe ingresar una hora de fin completa en formato HH:mm.");
        }
        try {
            return LocalTime.parse(texto, FORMATO_HORA_UI);
        } catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException("Hora de fin inválida. Use formato 24 horas (ejemplo: 12:00 o 18:30).");
        }
    }

    public int obtenerCantidadPersonas() {
        String texto = txtCantidadPersonas.getText().trim();
        if (texto.isEmpty()) {
            return 1;
        }
        try {
            int c = Integer.parseInt(texto);
            return Math.max(1, c);
        } catch (NumberFormatException nfe) {
            return 1;
        }
    }
}
