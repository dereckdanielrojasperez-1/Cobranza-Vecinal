package com.condominio.exception;

public class ReservaSolapadaException extends BusinessException {
    public ReservaSolapadaException(String areaComun, String fecha, String horario) {
        super("RESERVA_SOLAPADA", String.format("Conflicto de reserva en '%s' el %s (%s). Ya existe una reserva confirmada en dicho intervalo.",
                areaComun, fecha, horario));
    }

    public ReservaSolapadaException(String areaComun, java.time.LocalDate fecha, java.time.LocalTime horaInicio, java.time.LocalTime horaFin) {
        this(areaComun, fecha != null ? fecha.toString() : "N/A", (horaInicio != null ? horaInicio.toString() : "") + " - " + (horaFin != null ? horaFin.toString() : ""));
    }

    public ReservaSolapadaException(String mensaje) {
        super("RESERVA_SOLAPADA", mensaje);
    }
}
