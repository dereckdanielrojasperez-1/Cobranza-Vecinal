package com.condominio.service;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.Proveedor;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Propietario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IOperacionService {
    ReservaArea solicitarReserva(Long reservaId, Propietario propietario, AreaComun area, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int cantidadPersonas);
    List<ReservaArea> listarReservas();
    boolean finalizarReserva(Long reservaId);
    List<ReservaArea> verificarYFinalizarReservasVencidas();
    Incidente reportarIncidente(Incidente incidente);
    List<Incidente> listarIncidentes();
    Comunicado publicarComunicado(Comunicado comunicado);
    List<Comunicado> listarComunicados();
    List<Proveedor> listarProveedores();

    // Verifica si existe solapamiento con alguna reserva en estado CONFIRMADA para la misma area comun y fecha.
    boolean existeSolapamientoReserva(String nombreArea, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
}
