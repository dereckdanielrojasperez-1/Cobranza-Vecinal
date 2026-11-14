package com.condominio.service.impl;

import com.condominio.patrones.singleton.ConfiguracionCondominio;
import com.condominio.dto.CuotaDTO;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.HistorialPago;
import com.condominio.modelo.financiero.Multa;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.repositorio.CuotaRepositorio;
import com.condominio.repositorio.GastoComunRepositorio;
import com.condominio.repositorio.MultaRepositorio;
import com.condominio.repositorio.PagoRepositorio;
import com.condominio.service.ICobranzaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CobranzaServiceImpl implements ICobranzaService {

    private final CuotaRepositorio cuotaRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final MultaRepositorio multaRepositorio;
    private final GastoComunRepositorio gastoComunRepositorio;
    private final ConfiguracionCondominio configuracion;
    private final GestorNotificaciones gestorNotificaciones;

    @Autowired
    public CobranzaServiceImpl(
            CuotaRepositorio cuotaRepositorio,
            PagoRepositorio pagoRepositorio,
            MultaRepositorio multaRepositorio,
            GastoComunRepositorio gastoComunRepositorio) {
        this.cuotaRepositorio = cuotaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.multaRepositorio = multaRepositorio;
        this.gastoComunRepositorio = gastoComunRepositorio;
        this.configuracion = ConfiguracionCondominio.obtenerInstancia();
        this.gestorNotificaciones = new GestorNotificaciones();
        this.gestorNotificaciones.suscribir(new NotificadorResidente());
    }

    public CobranzaServiceImpl(
            CuotaRepositorio cuotaRepositorio,
            PagoRepositorio pagoRepositorio,
            MultaRepositorio multaRepositorio) {
        this(cuotaRepositorio, pagoRepositorio, multaRepositorio, null);
    }

    public CobranzaServiceImpl() {
        this(null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuotaDTO> listarCuotas() {
        if (cuotaRepositorio == null) {
            return Collections.emptyList();
        }
        List<CuotaMantenimiento> cuotas = cuotaRepositorio.findAllConRelaciones();
        List<CuotaDTO> dtos = new ArrayList<>();
        for (CuotaMantenimiento c : cuotas) {
            String deptoNumero = (c.getDepartamento() != null && c.getDepartamento().getNumero() != null)
                    ? c.getDepartamento().getNumero()
                    : "S/N";
            dtos.add(CuotaDTO.builder()
                    .idCuota(c.getId())
                    .numeroDepartamento(deptoNumero)
                    .montoBase(c.getMontoBase())
                    .montoFinal(c.getMontoTotal())
                    .fechaVencimiento(c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "-")
                    .estado(c.getEstado())
                    .build());
        }
        return dtos;
    }

    @Override
    public CuotaMantenimiento generarCuotaMensual(
            Long cuotaId,
            Departamento departamento,
            Double montoBase,
            LocalDate fechaVencimiento) {

        if (cuotaId == null) {
            throw new IllegalArgumentException("El ID de la cuota es obligatorio");
        }
        if (departamento == null) {
            throw new IllegalArgumentException("El departamento es obligatorio");
        }
        if (montoBase == null || !Double.isFinite(montoBase) || montoBase <= 0) {
            throw new IllegalArgumentException("El monto base debe ser mayor a cero");
        }
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }

        CuotaMantenimiento cuota = CuotaMantenimiento.builder()
                .id(cuotaId)
                .montoBase(montoBase)
                .montoTotal(montoBase)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(fechaVencimiento)
                .estado("PENDIENTE")
                .departamento(departamento)
                .build();

        if (cuotaRepositorio != null) {
            try {
                cuotaRepositorio.save(cuota);
            } catch (Exception ex) {
                log.warn("No se pudo persistir la cuota en BD, retornando instancia en memoria: {}", ex.getMessage());
            }
        }
        return cuota;
    }

    @Override
    public double calcularMoraCuota(CuotaMantenimiento cuota, LocalDate fechaEvaluacion) {
        if (cuota == null || fechaEvaluacion == null || cuota.getFechaVencimiento() == null) {
            return 0.0;
        }

        if (cuota.getMontoBase() == null
                || cuota.getMontoTotal() == null
                || !Double.isFinite(cuota.getMontoBase())
                || !Double.isFinite(cuota.getMontoTotal())
                || cuota.getMontoBase() <= 0) {
            throw new IllegalArgumentException("Los montos de la cuota son invalidos");
        }

        String estado = cuota.getEstado();
        if ("PAGADO".equals(estado) || "VENCIDO".equals(estado) || "PARCIAL".equals(estado)) {
            return 0.0;
        }

        long diasVencidos = ChronoUnit.DAYS.between(cuota.getFechaVencimiento(), fechaEvaluacion);
        if (diasVencidos <= configuracion.getDiasGracia()) {
            return 0.0;
        }

        double penalidad = cuota.getMontoBase() * (configuracion.getTasaInteresMora() / 100.0);
        cuota.setMontoTotal(cuota.getMontoBase() + penalidad);
        cuota.setEstado("VENCIDO");
        return penalidad;
    }

    @Override
    public HistorialPago registrarPago(
            Long pagoId,
            Long historialId,
            CuotaMantenimiento cuota,
            Double montoAbonado,
            String metodoPago) {

        if (pagoId == null || historialId == null) {
            throw new IllegalArgumentException("Los identificadores son obligatorios");
        }
        if (cuota == null) {
            throw new IllegalArgumentException("La cuota no puede ser nula");
        }
        if (montoAbonado == null || !Double.isFinite(montoAbonado) || montoAbonado <= 0) {
            throw new IllegalArgumentException("El monto abonado debe ser mayor a cero");
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new IllegalArgumentException("Debe indicar un metodo de pago");
        }
        if ("PAGADO".equals(cuota.getEstado())) {
            throw new IllegalStateException("La cuota ya se encuentra pagada");
        }

        Double saldoActual = cuota.getMontoTotal();
        if (saldoActual == null || montoAbonado > saldoActual) {
            throw new IllegalArgumentException("El monto excede el saldo pendiente");
        }

        double nuevoSaldo = saldoActual - montoAbonado;
        cuota.setMontoTotal(nuevoSaldo);

        String estadoPrevio = cuota.getEstado();
        String nuevoEstado = (nuevoSaldo == 0.0) ? "PAGADO" : "PARCIAL";
        cuota.setEstado(nuevoEstado);

        Pago pago = Pago.builder()
                .id(pagoId)
                .monto(montoAbonado)
                .metodoPago(metodoPago)
                .fechaPago(LocalDate.now())
                .departamento(cuota.getDepartamento())
                .build();

        return HistorialPago.builder()
                .id(historialId)
                .fechaModificacion(LocalDateTime.now())
                .estadoAnterior(estadoPrevio)
                .estadoNuevo(nuevoEstado)
                .cuota(cuota)
                .pago(pago)
                .build();
    }

    @Override
    @Transactional
    public boolean procesarPagoConRepositorio(Long idCuota, double monto, String codigoOperacion) {
        if (cuotaRepositorio == null || pagoRepositorio == null) {
            throw new IllegalStateException("Los repositorios JPA no han sido inyectados en la fachada");
        }
        if (idCuota == null) {
            throw new IllegalArgumentException("El ID de la cuota es obligatorio");
        }
        if (monto <= 0 || !Double.isFinite(monto)) {
            throw new com.condominio.exception.MontoInvalidoException("El monto a pagar debe ser positivo y finito");
        }

        CuotaMantenimiento cuota = cuotaRepositorio.findById(idCuota)
                .orElseThrow(() -> new com.condominio.exception.CuotaNoEncontradaException("Cuota con ID " + idCuota + " no encontrada"));

        if ("PAGADO".equalsIgnoreCase(cuota.getEstado()) || cuota.getMontoTotal() <= 0.001) {
            throw new com.condominio.exception.CuotaYaPagadaException("La cuota " + idCuota + " ya está completamente pagada");
        }

        if (monto > cuota.getMontoTotal()) {
            throw new com.condominio.exception.MontoInvalidoException(String.format("El monto a pagar (S/ %.2f) excede el saldo pendiente (S/ %.2f)", monto, cuota.getMontoTotal()));
        }

        cuota.pagar(monto);

        Pago pago = Pago.builder()
                .monto(monto)
                .fechaPago(LocalDate.now())
                .metodoPago(codigoOperacion != null && !codigoOperacion.isBlank() ? codigoOperacion : "TRANSFERENCIA")
                .departamento(cuota.getDepartamento())
                .build();
        pagoRepositorio.save(pago);
        cuotaRepositorio.save(cuota);

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Se procesó un pago de S/ " + monto + " para la cuota #" + idCuota);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Multa> listarMultas() {
        if (multaRepositorio == null) return Collections.emptyList();
        return multaRepositorio.findAll();
    }

    @Override
    @Transactional
    public Multa registrarMulta(Multa multa) {
        if (multa == null) throw new IllegalArgumentException("La multa no puede ser nula");
        if (multaRepositorio != null) return multaRepositorio.save(multa);
        return multa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoComun> listarGastosComunes() {
        if (gastoComunRepositorio == null) return Collections.emptyList();
        return gastoComunRepositorio.findAll();
    }

    @Override
    @Transactional
    public GastoComun registrarGastoComun(GastoComun gasto) {
        if (gasto == null) throw new IllegalArgumentException("El gasto común no puede ser nulo");
        if (gastoComunRepositorio != null) return gastoComunRepositorio.save(gasto);
        return gasto;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneCuotaAbiertaEnPeriodo(Long departamentoId, String periodo) {
        if (cuotaRepositorio == null || departamentoId == null || periodo == null || periodo.isBlank()) {
            return false;
        }
        String p = periodo.trim();
        List<CuotaMantenimiento> cuotas = cuotaRepositorio.findAllConRelaciones();
        return cuotas.stream().anyMatch(c -> {
            if (c.getDepartamento() == null || !departamentoId.equals(c.getDepartamento().getId())) {
                return false;
            }
            if ("PAGADO".equalsIgnoreCase(c.getEstado())) {
                return false;
            }
            boolean emisionCoincide = c.getFechaEmision() != null && c.getFechaEmision().toString().startsWith(p);
            boolean vencimientoCoincide = c.getFechaVencimiento() != null && c.getFechaVencimiento().toString().startsWith(p);
            return emisionCoincide || vencimientoCoincide;
        });
    }
}
