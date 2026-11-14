package com.condominio.gui.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

// Clase utilitaria centralizada con las reglas de negocio y validación de entrada
public final class Validaciones {

    public static final Pattern REGEX_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern REGEX_DNI = Pattern.compile("^\\d{8}$");
    public static final Pattern REGEX_TELEFONO = Pattern.compile("^[967]\\d{8}$");
    public static final Pattern REGEX_NUMERO_DEPTO = Pattern.compile("^[a-zA-Z0-9\\-]{1,6}$");
    public static final Pattern REGEX_TEXTO_ALFABETICO = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$");

    private Validaciones() {
        // Clase utilitaria no instanciable
    }

    // Valida el número de departamento: 1 a 6 caracteres, alfanumérico con guiones y sin espacios.
    public static void validarNumeroDepartamento(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio.");
        }
        String limpio = numero.trim();
        if (limpio.length() < 1 || limpio.length() > 6 || !REGEX_NUMERO_DEPTO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El número de departamento debe tener entre 1 y 6 caracteres alfanuméricos sin espacios.");
        }
    }

    // Valida el piso del departamento: entero obligatorio en el rango 1 a 30.
    public static int validarPiso(String pisoStr) {
        if (pisoStr == null || pisoStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de piso es obligatorio.");
        }
        int piso;
        try {
            piso = Integer.parseInt(pisoStr.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("El número de piso debe ser un valor entero.");
        }
        if (piso < 1 || piso > 30) {
            throw new IllegalArgumentException("El número de piso debe estar entre 1 y 30.");
        }
        return piso;
    }

    // Valida el metraje del departamento: decimal obligatorio en el rango 10.00 a 1000.00 m².
    public static double validarMetraje(String metrajeStr) {
        if (metrajeStr == null || metrajeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El metraje en m² es obligatorio.");
        }
        double metraje;
        try {
            metraje = Double.parseDouble(metrajeStr.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("El metraje debe ser un valor numérico válido.");
        }
        if (metraje < 10.00 || metraje > 1000.00) {
            throw new IllegalArgumentException("El metraje debe estar comprendido entre 10.00 y 1000.00 m².");
        }
        return metraje;
    }

    // Valida el monto total de gasto común: mayor a 0 y con tope de 999999.99.
    public static double validarMontoGasto(String montoStr) {
        if (montoStr == null || montoStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El monto total del gasto común es obligatorio.");
        }
        double monto;
        try {
            monto = Double.parseDouble(montoStr.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("El monto del gasto común debe ser un valor numérico válido.");
        }
        if (monto <= 0.0 || monto > 999999.99) {
            throw new IllegalArgumentException("El monto del gasto común debe ser mayor a 0 y no superar S/ 999,999.99.");
        }
        return monto;
    }

    // Valida el DNI: exactamente 8 dígitos numéricos.
    public static void validarDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }
        String limpio = dni.trim();
        if (!REGEX_DNI.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos.");
        }
    }

    // Valida nombres o apellidos: alfabético, entre 2 y 50 caracteres.
    public static void validarNombresApellidos(String texto, String nombreCampo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }
        String limpio = texto.trim();
        if (limpio.length() < 2 || limpio.length() > 50) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe tener entre 2 y 50 caracteres.");
        }
        if (!REGEX_TEXTO_ALFABETICO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " solo admite letras y espacios.");
        }
    }

    // Valida teléfono: exactamente 9 dígitos, comenzando por 9, 6 o 7.
    public static void validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        String limpio = telefono.trim();
        if (!REGEX_TELEFONO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos y comenzar con 9, 6 o 7.");
        }
    }

    // Valida correo electrónico con formato estándar y lo retorna en minúsculas.
    public static String validarYNormalizarCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        String limpio = correo.trim().toLowerCase();
        if (!REGEX_CORREO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        return limpio;
    }

    // Valida monto a pagar: mayor a 0 y menor o igual al saldo pendiente de la cuota.
    public static void validarMontoPago(double monto, double saldoPendiente) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }
        // Margen de tolerancia para punto flotante
        if (monto > (saldoPendiente + 0.001)) {
            throw new IllegalArgumentException("El monto excede el saldo pendiente");
        }
    }

    // Valida descripción de incidente: entre 5 y 200 caracteres.
    public static void validarDescripcionIncidente(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del incidente es obligatoria.");
        }
        String limpio = descripcion.trim();
        if (limpio.length() < 5 || limpio.length() > 200) {
            throw new IllegalArgumentException("La descripción del incidente debe tener entre 5 y 200 caracteres.");
        }
    }

    // Valida persona que reporta: entre 2 y 50 caracteres alfabéticos.
    public static void validarReportadoPor(String reportadoPor) {
        validarNombresApellidos(reportadoPor, "Reportado Por");
    }

    // Valida fecha de reserva: no pasada y como máximo 60 días en el futuro.
    public static void validarFechaReserva(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de reserva es obligatoria.");
        }
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            throw new IllegalArgumentException("No se permiten reservas en fechas pasadas.");
        }
        LocalDate limiteMax = hoy.plusDays(60);
        if (fecha.isAfter(limiteMax)) {
            throw new IllegalArgumentException("La fecha de reserva no puede exceder 60 días a partir de hoy.");
        }
    }

    // Valida horario de reserva: hora de inicio estrictamente menor a hora fin.
    public static void validarHorarioReserva(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Los horarios de inicio y fin son obligatorios.");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser estrictamente menor que la hora de fin.");
        }
    }

    // Determina si dos intervalos de horario se solapan en la misma fecha y área.
    public static boolean haySolapamientoHorario(LocalTime ini1, LocalTime fin1, LocalTime ini2, LocalTime fin2) {
        if (ini1 == null || fin1 == null || ini2 == null || fin2 == null) {
            return false;
        }
        return ini1.isBefore(fin2) && fin1.isAfter(ini2);
    }
}
