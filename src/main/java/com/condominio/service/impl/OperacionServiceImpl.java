package com.condominio.service.impl;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.Proveedor;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Propietario;
import com.condominio.repositorio.ComunicadoRepositorio;
import com.condominio.repositorio.IncidenteRepositorio;
import com.condominio.repositorio.ProveedorRepositorio;
import com.condominio.repositorio.ReservaAreaRepositorio;
import com.condominio.service.IOperacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OperacionServiceImpl implements IOperacionService {

    private final ReservaAreaRepositorio reservaAreaRepositorio;
    private final IncidenteRepositorio incidenteRepositorio;
    private final ComunicadoRepositorio comunicadoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final List<ReservaArea> reservasMemoria = new ArrayList<>();

    @Autowired
    public OperacionServiceImpl(
            ReservaAreaRepositorio reservaAreaRepositorio,
            IncidenteRepositorio incidenteRepositorio,
            ComunicadoRepositorio comunicadoRepositorio,
            ProveedorRepositorio proveedorRepositorio) {
        this.reservaAreaRepositorio = reservaAreaRepositorio;
        this.incidenteRepositorio = incidenteRepositorio;
        this.comunicadoRepositorio = comunicadoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
    }

    public OperacionServiceImpl() {
        this(null, null, null, null);
    }

    @Override
    @Transactional
    public ReservaArea solicitarReserva(
            Long reservaId,
            Propietario propietario,
            AreaComun areaComun,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int cantidadPersonas) {

        if (reservaId == null) {
            reservaId = (long) ((Math.random() * 900000L) + 100000L);
        }
        if (propietario == null || areaComun == null) {
            throw new IllegalArgumentException("El propietario y el area son obligatorios");
        }
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten reservas en fechas pasadas");
        }
        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Debe indicar el horario de reserva");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora final debe ser posterior a la inicial");
        }
        if (fecha.equals(LocalDate.now()) && !horaInicio.isAfter(LocalTime.now())) {
            throw new IllegalArgumentException("No se permiten reservas en horarios pasados");
        }
        if (cantidadPersonas <= 0) {
            throw new IllegalArgumentException("Debe registrar al menos una persona");
        }
        if (areaComun.getCapacidad() != null && cantidadPersonas > areaComun.getCapacidad()) {
            throw new IllegalStateException("La cantidad de personas excede la capacidad maxima (" + areaComun.getCapacidad() + ")");
        }

        if (existeSolapamientoReserva(areaComun.getNombre(), fecha, horaInicio, horaFin)) {
            throw new com.condominio.exception.ReservaSolapadaException(areaComun.getNombre(), fecha, horaInicio, horaFin);
        }

        ReservaArea reserva = ReservaArea.builder()
                .id(reservaId)
                .fechaReserva(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .estado("CONFIRMADA")
                .propietario(propietario)
                .areaComun(areaComun)
                .build();

        reservasMemoria.add(reserva);
        if (reservaAreaRepositorio != null) {
            try {
                reservaAreaRepositorio.save(reserva);
            } catch (Exception ex) {
                log.warn("No se pudo persistir la reserva en BD: {}", ex.getMessage());
            }
        }
        return reserva;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaArea> listarReservas() {
        if (reservaAreaRepositorio != null) {
            List<ReservaArea> list = reservaAreaRepositorio.findAllConRelaciones();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        return new ArrayList<>(reservasMemoria);
    }

    @Override
    @Transactional
    public boolean finalizarReserva(Long reservaId) {
        if (reservaId == null) return false;
        boolean finalizado = false;
        if (reservaAreaRepositorio != null) {
            Optional<ReservaArea> op = reservaAreaRepositorio.findById(reservaId);
            if (op.isPresent()) {
                ReservaArea r = op.get();
                r.setEstado("FINALIZADA");
                reservaAreaRepositorio.save(r);
                finalizado = true;
            }
        }
        for (ReservaArea r : reservasMemoria) {
            if (r.getId() != null && r.getId().equals(reservaId)) {
                r.setEstado("FINALIZADA");
                finalizado = true;
            }
        }
        if (finalizado) {
            log.info("Reserva #{} finalizada exitosamente. Área liberada para nuevas solicitudes.", reservaId);
        }
        return finalizado;
    }

    @Override
    @Transactional
    public List<ReservaArea> verificarYFinalizarReservasVencidas() {
        List<ReservaArea> finalizadas = new ArrayList<>();
        if (reservaAreaRepositorio == null) return finalizadas;
        List<ReservaArea> todas = reservaAreaRepositorio.findAllConRelaciones();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        for (ReservaArea r : todas) {
            if ("CONFIRMADA".equalsIgnoreCase(r.getEstado())) {
                boolean vencida = false;
                if (r.getFechaReserva() != null) {
                    if (r.getFechaReserva().isBefore(hoy)) {
                        vencida = true;
                    } else if (r.getFechaReserva().isEqual(hoy) && r.getHoraFin() != null && r.getHoraFin().isBefore(ahora)) {
                        vencida = true;
                    }
                }
                if (vencida) {
                    r.setEstado("FINALIZADA");
                    reservaAreaRepositorio.save(r);
                    finalizadas.add(r);
                    log.info("Reserva vencida #{} marcada como FINALIZADA automáticamente. Área liberada.", r.getId());
                }
            }
        }
        return finalizadas;
    }

    @Override
    @Transactional
    public Incidente reportarIncidente(Incidente incidente) {
        if (incidente == null) throw new IllegalArgumentException("El incidente no puede ser nulo");
        if (incidenteRepositorio != null) return incidenteRepositorio.save(incidente);
        return incidente;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidente> listarIncidentes() {
        if (incidenteRepositorio == null) return Collections.emptyList();
        return incidenteRepositorio.findAll();
    }

    @Override
    @Transactional
    public Comunicado publicarComunicado(Comunicado comunicado) {
        if (comunicado == null) throw new IllegalArgumentException("El comunicado no puede ser nulo");
        if (comunicadoRepositorio != null) return comunicadoRepositorio.save(comunicado);
        return comunicado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comunicado> listarComunicados() {
        if (comunicadoRepositorio == null) return Collections.emptyList();
        return comunicadoRepositorio.findAllByOrderByFechaPublicacionDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedores() {
        if (proveedorRepositorio == null) return Collections.emptyList();
        return proveedorRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamientoReserva(String nombreArea, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (nombreArea == null || fecha == null || horaInicio == null || horaFin == null) {
            return false;
        }
        List<ReservaArea> reservas = (reservaAreaRepositorio != null)
                ? reservaAreaRepositorio.findByFechaReserva(fecha)
                : reservasMemoria;
        if (reservas == null || reservas.isEmpty()) {
            reservas = reservasMemoria;
        }
        for (ReservaArea r : reservas) {
            if (r.getEstado() != null && !r.getEstado().equalsIgnoreCase("CONFIRMADA")) {
                continue;
            }
            if (r.getFechaReserva() == null || !r.getFechaReserva().equals(fecha)) {
                continue;
            }
            if (r.getAreaComun() == null || r.getAreaComun().getNombre() == null) {
                continue;
            }
            if (!r.getAreaComun().getNombre().trim().equalsIgnoreCase(nombreArea.trim())) {
                continue;
            }
            if (r.getHoraInicio() != null && r.getHoraFin() != null) {
                // Hay cruce si horaInicio < r.getHoraFin() y horaFin > r.getHoraInicio()
                if (horaInicio.isBefore(r.getHoraFin()) && horaFin.isAfter(r.getHoraInicio())) {
                    return true;
                }
            }
        }
        return false;
    }
}
