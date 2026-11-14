package com.condominio.patrones.facade;

import com.condominio.configuracion.CargadorDatosIniciales;
import com.condominio.patrones.singleton.ConfiguracionCondominio;
import com.condominio.dto.CuotaDTO;
import com.condominio.dto.EdificioDTO;
import com.condominio.dto.InmuebleDTO;
import com.condominio.dto.ResidenteDTO;
import com.condominio.dto.ResultadoBusquedaDTO;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.HistorialPago;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Inquilino;
import com.condominio.modelo.personas.Propietario;
import com.condominio.modelo.personas.Usuario;
import com.condominio.modelo.personas.Visita;
import com.condominio.patrones.decorator.CuotaBase;
import com.condominio.patrones.decorator.DecoradorDescuentoProntoPago;
import com.condominio.patrones.decorator.DecoradorRecargoMora;
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import com.condominio.patrones.prototype.CuotaPrototipo;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.repositorio.CuotaRepositorio;
import com.condominio.repositorio.DepartamentoRepositorio;
import com.condominio.repositorio.EdificioRepositorio;
import com.condominio.repositorio.InquilinoRepositorio;
import com.condominio.repositorio.PagoRepositorio;
import com.condominio.repositorio.PropietarioRepositorio;
import com.condominio.repositorio.ReservaAreaRepositorio;
import com.condominio.repositorio.TorreRepositorio;
import com.condominio.repositorio.UsuarioRepositorio;
import com.condominio.repositorio.UsuarioSistemaRepositorio;
import com.condominio.repositorio.VisitaRepositorio;
import com.condominio.service.IBusquedaGlobalService;
import com.condominio.service.ICobranzaService;
import com.condominio.service.IInmuebleService;
import com.condominio.service.IOperacionService;
import com.condominio.service.IPersonaService;
import com.condominio.service.impl.CobranzaServiceImpl;
import com.condominio.service.impl.InmuebleServiceImpl;
import com.condominio.service.impl.OperacionServiceImpl;
import com.condominio.service.impl.PersonaServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Fachada unificada (Patron Estructural Facade) para la orquestacion de procesos
@Service
@Getter
@Slf4j
public class CobranzaFachada {

    private final ConfiguracionCondominio configuracion;
    private final CuotaRepositorio cuotaRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final EdificioRepositorio edificioRepositorio;
    private final TorreRepositorio torreRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PropietarioRepositorio propietarioRepositorio;
    private final InquilinoRepositorio inquilinoRepositorio;
    private final ICobranzaService cobranzaService;
    private final IInmuebleService inmuebleService;
    private final IPersonaService personaService;
    private final IOperacionService operacionService;
    private final GestorNotificaciones gestorNotificaciones;
    @Autowired(required = false)
    private IBusquedaGlobalService busquedaGlobalService;
    @Autowired(required = false)
    private UsuarioSistemaRepositorio usuarioSistemaRepositorio;
    @Autowired(required = false)
    private VisitaRepositorio visitaRepositorio;
    @Autowired(required = false)
    private ReservaAreaRepositorio reservaAreaRepositorio;
    private final List<Visita> visitasMemoria = new ArrayList<>();

    // Constructor para inyeccion de la capa de servicios desacoplada (SRP) y repositorios por Spring Boot.
    @Autowired
    public CobranzaFachada(
            ICobranzaService cobranzaService,
            IInmuebleService inmuebleService,
            IPersonaService personaService,
            IOperacionService operacionService,
            @Autowired(required = false) CuotaRepositorio cuotaRepositorio,
            @Autowired(required = false) PagoRepositorio pagoRepositorio,
            @Autowired(required = false) DepartamentoRepositorio departamentoRepositorio,
            @Autowired(required = false) EdificioRepositorio edificioRepositorio,
            @Autowired(required = false) TorreRepositorio torreRepositorio,
            @Autowired(required = false) UsuarioRepositorio usuarioRepositorio,
            @Autowired(required = false) PropietarioRepositorio propietarioRepositorio,
            @Autowired(required = false) InquilinoRepositorio inquilinoRepositorio) {
        this.configuracion = ConfiguracionCondominio.obtenerInstancia();
        this.cobranzaService = cobranzaService;
        this.inmuebleService = inmuebleService;
        this.personaService = personaService;
        this.operacionService = operacionService;
        this.cuotaRepositorio = cuotaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.edificioRepositorio = edificioRepositorio;
        this.torreRepositorio = torreRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.propietarioRepositorio = propietarioRepositorio;
        this.inquilinoRepositorio = inquilinoRepositorio;
        this.gestorNotificaciones = new GestorNotificaciones();
        this.gestorNotificaciones.suscribir(new NotificadorResidente());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorEmail());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorSms());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorToast());
        this.busquedaGlobalService = new com.condominio.service.impl.BusquedaGlobalServiceImpl(this.cobranzaService, this.inmuebleService, this.personaService, this.operacionService);
    }

    public CobranzaFachada(
            ICobranzaService cobranzaService,
            IInmuebleService inmuebleService,
            IPersonaService personaService,
            IOperacionService operacionService) {
        this(cobranzaService, inmuebleService, personaService, operacionService,
                null, null, null, null, null, null, null, null);
    }

    // Constructor sobrecargado para inyeccion completa de 8 repositorios.
    public CobranzaFachada(
            CuotaRepositorio cuotaRepositorio,
            PagoRepositorio pagoRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            PropietarioRepositorio propietarioRepositorio,
            InquilinoRepositorio inquilinoRepositorio) {
        this.configuracion = ConfiguracionCondominio.obtenerInstancia();
        this.cuotaRepositorio = cuotaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.edificioRepositorio = edificioRepositorio;
        this.torreRepositorio = torreRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.propietarioRepositorio = propietarioRepositorio;
        this.inquilinoRepositorio = inquilinoRepositorio;
        this.cobranzaService = new CobranzaServiceImpl(cuotaRepositorio, pagoRepositorio, null);
        this.inmuebleService = new InmuebleServiceImpl(departamentoRepositorio, edificioRepositorio, torreRepositorio, null);
        this.personaService = new PersonaServiceImpl(usuarioRepositorio, propietarioRepositorio, inquilinoRepositorio, departamentoRepositorio, null, null);
        this.operacionService = new OperacionServiceImpl(null, null, null, null);
        this.gestorNotificaciones = new GestorNotificaciones();
        this.gestorNotificaciones.suscribir(new NotificadorResidente());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorEmail());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorSms());
        this.gestorNotificaciones.suscribir(new com.condominio.patrones.observer.NotificadorToast());
        this.busquedaGlobalService = new com.condominio.service.impl.BusquedaGlobalServiceImpl(this.cobranzaService, this.inmuebleService, this.personaService, this.operacionService);
    }

    // Constructor sobrecargado de 5 parametros para retrocompatibilidad con controladores o tests de inmuebles.
    public CobranzaFachada(
            CuotaRepositorio cuotaRepositorio,
            PagoRepositorio pagoRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio) {
        this(cuotaRepositorio, pagoRepositorio, departamentoRepositorio, edificioRepositorio, torreRepositorio, null, null, null);
    }

    // Constructor sobrecargado de 2 parametros para retrocompatibilidad con controladores o tests de cobranzas.
    public CobranzaFachada(CuotaRepositorio cuotaRepositorio, PagoRepositorio pagoRepositorio) {
        this(cuotaRepositorio, pagoRepositorio, null, null, null, null, null, null);
    }

    // Constructor por defecto para retrocompatibilidad absoluta con suites de pruebas unitarias (FacadeTest).
    public CobranzaFachada() {
        this(null, null, null, null, null, null, null, null);
    }

    // Autentica credenciales de usuario centralizando el acceso a través de la fachada (SRP / DIP).
    public Optional<UsuarioSistema> autenticarUsuario(String usuario, String clave) {
        if (usuario == null || clave == null || usuario.isBlank() || clave.isBlank()) {
            return Optional.empty();
        }
        if (this.usuarioSistemaRepositorio != null) {
            Optional<UsuarioSistema> opcional = this.usuarioSistemaRepositorio.findByUsuario(usuario.trim());
            if (opcional.isPresent() && clave.equals(opcional.get().getClave()) && opcional.get().isActivo()) {
                return opcional;
            }
        }
        return Optional.empty();
    }

    public void setUsuarioSistemaRepositorio(UsuarioSistemaRepositorio usuarioSistemaRepositorio) {
        this.usuarioSistemaRepositorio = usuarioSistemaRepositorio;
    }

    public UsuarioSistemaRepositorio getUsuarioSistemaRepositorio() {
        return this.usuarioSistemaRepositorio;
    }

    // 1. GENERAR CUOTA MENSUAL

    // Genera una nueva instancia de cuota de mantenimiento mensual con validacion defensiva de atributos.
    public CuotaMantenimiento generarCuotaMensual(
            Long cuotaId,
            Departamento departamento,
            Double montoBase,
            LocalDate fechaVencimiento) {

        if (cuotaId == null) {
            throw new IllegalArgumentException(
                    "El ID de la cuota es obligatorio");
        }

        if (departamento == null) {
            throw new IllegalArgumentException(
                    "El departamento es obligatorio");
        }

        if (montoBase == null
                || !Double.isFinite(montoBase)
                || montoBase <= 0) {
            throw new IllegalArgumentException(
                    "El monto base debe ser mayor a cero");
        }

        if (fechaVencimiento == null) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento es obligatoria");
        }

        return CuotaMantenimiento.builder()
                .id(cuotaId)
                .montoBase(montoBase)
                .montoTotal(montoBase)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(fechaVencimiento)
                .estado("PENDIENTE")
                .departamento(departamento)
                .build();
    }

    // 2. CALCULAR MORA

    // Evalua si una cuota se encuentra fuera de su periodo de gracia y aplica el recargo por mora correspondiente.
    public double calcularMoraCuota(
            CuotaMantenimiento cuota,
            LocalDate fechaEvaluacion) {

        if (cuota == null
                || fechaEvaluacion == null
                || cuota.getFechaVencimiento() == null) {
            return 0.0;
        }

        if (cuota.getMontoBase() == null
                || cuota.getMontoTotal() == null
                || !Double.isFinite(cuota.getMontoBase())
                || !Double.isFinite(cuota.getMontoTotal())
                || cuota.getMontoBase() <= 0) {
            throw new IllegalArgumentException(
                    "Los montos de la cuota son invalidos");
        }

        String estado = cuota.getEstado();

        // Una cuota pagada no genera nuevos recargos.
        if ("PAGADO".equals(estado)) {
            return 0.0;
        }

        // Evita aplicar dos veces la misma mora.
        if ("VENCIDO".equals(estado)) {
            return 0.0;
        }

        // Evita recalcular el importe original sobre una cuota que ya tiene pagos parciales.
        if ("PARCIAL".equals(estado)) {
            return 0.0;
        }

        long diasVencidos = ChronoUnit.DAYS.between(
                cuota.getFechaVencimiento(),
                fechaEvaluacion
        );

        if (diasVencidos <= configuracion.getDiasGracia()) {
            return 0.0;
        }

        double penalidad = cuota.getMontoBase()
                * (configuracion.getTasaInteresMora() / 100.0);

        cuota.setMontoTotal(
                cuota.getMontoBase() + penalidad
        );

        cuota.setEstado("VENCIDO");

        return penalidad;
    }

    // Sobrecarga de calculo de mora evaluada a la fecha actual del sistema.
    public double calcularMoraCuota(
            CuotaMantenimiento cuota) {

        return calcularMoraCuota(
                cuota,
                LocalDate.now()
        );
    }

    // 3. REGISTRAR PAGO

    // Registra un pago en memoria actualizando el saldo, estado de la cuota e instanciando un registro historico.
    public HistorialPago registrarPago(
            Long pagoId,
            Long historialId,
            CuotaMantenimiento cuota,
            Double montoAbonado,
            String metodoPago) {

        if (pagoId == null || historialId == null) {
            throw new IllegalArgumentException(
                    "Los identificadores son obligatorios");
        }

        if (cuota == null) {
            throw new IllegalArgumentException(
                    "La cuota no puede ser nula");
        }

        if (montoAbonado == null
                || !Double.isFinite(montoAbonado)
                || montoAbonado <= 0) {
            throw new IllegalArgumentException(
                    "El monto abonado debe ser mayor a cero");
        }

        if (metodoPago == null
                || metodoPago.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar un metodo de pago");
        }

        if ("PAGADO".equals(cuota.getEstado())) {
            throw new IllegalStateException(
                    "La cuota ya se encuentra pagada");
        }

        Double saldoActual = cuota.getMontoTotal();

        if (saldoActual == null
                || !Double.isFinite(saldoActual)
                || saldoActual <= 0) {
            throw new IllegalStateException(
                    "La cuota no tiene un saldo valido");
        }

        if (montoAbonado > saldoActual) {
            throw new IllegalArgumentException(
                    "El pago excede el saldo pendiente");
        }

        String estadoPrevio = cuota.getEstado();

        // Se crea el objeto Pago con los datos del abono realizado.
        Pago pago = Pago.builder()
                .id(pagoId)
                .monto(montoAbonado)
                .fechaPago(LocalDate.now())
                .metodoPago(metodoPago)
                .departamento(cuota.getDepartamento())
                .build();

        // Calculamos el saldo restante.
        double saldoRestante = saldoActual - montoAbonado;

        String nuevoEstado;

        if (saldoRestante <= 0.000001) {
            nuevoEstado = "PAGADO";
            cuota.setMontoTotal(0.0);
        } else {
            nuevoEstado = "PARCIAL";
            cuota.setMontoTotal(saldoRestante);
        }

        cuota.setEstado(nuevoEstado);

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Se procesó un pago de S/ " + montoAbonado + " para la cuota #" + cuota.getId());
        }

        // El historial conserva el objeto Pago que produjo el cambio de estado.
        return HistorialPago.builder()
                .id(historialId)
                .fechaModificacion(LocalDateTime.now())
                .estadoAnterior(estadoPrevio)
                .estadoNuevo(nuevoEstado)
                .cuota(cuota)
                .pago(pago)
                .build();
    }

    // 4. SOLICITAR RESERVA DE AREA COMUN

    // Valida y registra una solicitud de reserva de instalacion o area comun para un propietario.
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
            throw new IllegalArgumentException(
                    "El propietario y el area son obligatorios");
        }

        if (fecha == null
                || fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se permiten reservas en fechas pasadas");
        }

        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el horario de reserva");
        }

        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "La hora final debe ser posterior a la inicial");
        }

        if (fecha.equals(LocalDate.now())
                && !horaInicio.isAfter(LocalTime.now())) {
            throw new IllegalArgumentException(
                    "No se permiten reservas en horarios pasados");
        }

        if (cantidadPersonas <= 0) {
            throw new IllegalArgumentException(
                    "Debe registrar al menos una persona");
        }

        if (areaComun.getCapacidad() != null
                && cantidadPersonas > areaComun.getCapacidad()) {
            throw new IllegalStateException(
                    "La cantidad de personas excede la capacidad maxima ("
                            + areaComun.getCapacidad() + ")");
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

        if (operacionService != null) {
            try {
                return operacionService.solicitarReserva(reservaId, propietario, areaComun, fecha, horaInicio, horaFin, cantidadPersonas);
            } catch (Exception ex) {
                log.warn("Fallo en servicio de operaciones al solicitar reserva: {}", ex.getMessage());
            }
        }
        return reserva;
    }

    // Finaliza una reserva culminada o vencida, liberando inmediatamente el área común
    @Transactional
    public boolean finalizarReserva(Long reservaId) {
        if (reservaId == null) {
            return false;
        }
        boolean ok = false;
        ReservaArea reserva = null;
        if (operacionService != null) {
            ok = operacionService.finalizarReserva(reservaId);
            List<ReservaArea> list = operacionService.listarReservas();
            for (ReservaArea r : list) {
                if (r.getId() != null && r.getId().equals(reservaId)) {
                    reserva = r;
                    break;
                }
            }
        }
        if (reserva == null && reservaAreaRepositorio != null) {
            reserva = reservaAreaRepositorio.findById(reservaId).orElse(null);
            if (reserva != null) {
                reserva.setEstado("FINALIZADA");
                reservaAreaRepositorio.save(reserva);
                ok = true;
            }
        }

        String nombreArea = (reserva != null && reserva.getAreaComun() != null && reserva.getAreaComun().getNombre() != null)
                ? reserva.getAreaComun().getNombre()
                : "Área Común";

        String msjNotif = "AVISO A GERENTE Y ADMINISTRADOR: La reserva #RES-" + reservaId
                + " de " + nombreArea + " ha concluido. El horario ha finalizado y el área común se encuentra LIBERADA y DISPONIBLE para nuevas reservas.";

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar(msjNotif);
        }

        if (operacionService != null) {
            try {
                operacionService.publicarComunicado(Comunicado.builder()
                        .titulo("Liberación de Área Común: " + nombreArea)
                        .mensaje(msjNotif)
                        .fechaPublicacion(LocalDate.now())
                        .autor("Sistema de Gestión Condominal")
                        .build());
            } catch (Exception ex) {
                log.warn("No se pudo publicar comunicado de liberación: {}", ex.getMessage());
            }
        }

        log.info("[RESERVAS] {}", msjNotif);
        return ok;
    }

    // Revisa todas las reservas y marca como FINALIZADAS aquellas cuyo horario ya transcurrió,
    @Transactional
    public List<ReservaArea> verificarYFinalizarReservasVencidas() {
        List<ReservaArea> finalizadas = new ArrayList<>();
        if (operacionService != null) {
            finalizadas = operacionService.verificarYFinalizarReservasVencidas();
        }
        for (ReservaArea r : finalizadas) {
            String nombreArea = (r.getAreaComun() != null && r.getAreaComun().getNombre() != null)
                    ? r.getAreaComun().getNombre()
                    : "Área Común";
            String msj = "AVISO A GERENTE Y ADMINISTRADOR: La reserva #RES-" + r.getId()
                    + " de " + nombreArea + " culminó su horario establecido. Área LIBERADA y DISPONIBLE para validación y nuevos turnos.";
            if (gestorNotificaciones != null) {
                gestorNotificaciones.notificar(msj);
            }
            if (operacionService != null) {
                try {
                    operacionService.publicarComunicado(Comunicado.builder()
                            .titulo("Área Liberada: " + nombreArea)
                            .mensaje(msj)
                            .fechaPublicacion(LocalDate.now())
                            .autor("Sistema de Gestión Condominal")
                            .build());
                } catch (Exception ignored) {}
            }
            log.info("[RESERVAS VENCIDAS] {}", msj);
        }
        return finalizadas;
    }

    // 5. PROCESAR PAGO CON REPOSITORIO (SPRING DATA JPA)

    // Procesa y persiste un pago transaccional contra la base de datos H2/PostgreSQL.
    @Transactional
    public boolean procesarPagoConRepositorio(Long idCuota, double monto, String codigoOperacion) {
        if (cuotaRepositorio == null || pagoRepositorio == null) {
            throw new IllegalStateException("Los repositorios JPA no han sido inyectados en la fachada");
        }

        if (idCuota == null) {
            throw new IllegalArgumentException("El ID de la cuota es obligatorio");
        }

        if (monto <= 0 || !Double.isFinite(monto)) {
            throw new com.condominio.exception.MontoInvalidoException("El monto a pagar debe ser mayor a cero");
        }

        CuotaMantenimiento cuota = cuotaRepositorio.findById(idCuota)
                .orElseThrow(() -> new com.condominio.exception.CuotaNoEncontradaException("Cuota con ID " + idCuota + " no encontrada"));

        if ("PAGADO".equalsIgnoreCase(cuota.getEstado()) || cuota.getMontoTotal() <= 0.001) {
            throw new com.condominio.exception.CuotaYaPagadaException("La cuota " + idCuota + " ya está completamente pagada");
        }

        if (monto > cuota.getMontoTotal()) {
            throw new com.condominio.exception.MontoInvalidoException(String.format("El monto a pagar (S/ %.2f) excede el saldo pendiente (S/ %.2f)", monto, cuota.getMontoTotal()));
        }

        log.info("Procesando pago: Cuota ID #{} | Monto: S/ {} | Operación: {}", idCuota, monto, codigoOperacion);

        // El patron de comportamiento State ejecuta la logica de amortizacion y transicion de estados
        cuota.pagar(monto);

        Pago pago = Pago.builder()
                .monto(monto)
                .fechaPago(LocalDate.now())
                .metodoPago(codigoOperacion != null && !codigoOperacion.isBlank() ? codigoOperacion : "TRANSFERENCIA")
                .departamento(cuota.getDepartamento())
                .build();
        pagoRepositorio.save(pago);

        cuotaRepositorio.save(cuota);
        log.info("Pago confirmado exitosamente. Saldo restante: S/ {} | Estado: {}", cuota.getMontoTotal(), cuota.getEstado());

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Se procesó un pago de S/ " + monto + " para la cuota #" + idCuota);
        }

        return true;
    }

    // 6. ANULAR PAGO CON REPOSITORIO (PATRON COMMAND UNDO)

    // Revierte un abono financiero previo restaurando el saldo pendiente y el estado logico
    @Transactional
    public boolean anularPagoConRepositorio(Long idCuota, double monto) {
        if (cuotaRepositorio == null) {
            throw new IllegalStateException("Los repositorios JPA no han sido inyectados en la fachada");
        }

        if (idCuota == null) {
            throw new IllegalArgumentException("El ID de la cuota es obligatorio");
        }

        if (monto <= 0 || !Double.isFinite(monto)) {
            throw new IllegalArgumentException("El monto a anular debe ser positivo y finito");
        }

        CuotaMantenimiento cuota = cuotaRepositorio.findById(idCuota)
                .orElseThrow(() -> new IllegalArgumentException("Cuota no encontrada con ID: " + idCuota));

        double saldoActual = cuota.getMontoTotal() != null ? cuota.getMontoTotal() : 0.0;
        double saldoRestaurado = saldoActual + monto;
        double montoBase = cuota.getMontoBase() != null ? cuota.getMontoBase() : saldoRestaurado;

        if (saldoRestaurado >= montoBase) {
            cuota.setMontoTotal(montoBase);
            cuota.setEstado("PENDIENTE");
        } else {
            cuota.setMontoTotal(saldoRestaurado);
            cuota.setEstado("PARCIAL");
        }

        cuotaRepositorio.save(cuota);
        log.info("Pago revertido (Undo) exitosamente: Cuota ID #{} | Saldo restaurado: S/ {} | Estado: {}", idCuota, cuota.getMontoTotal(), cuota.getEstado());

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Se anuló un pago de S/ " + monto + " para la cuota #" + idCuota);
        }

        return true;
    }

    // 7. CONSULTAR TODAS LAS CUOTAS (CON SEED DATA AUTOMATICO)

    // Obtiene el listado consolidado de cuotas en formato DTO para el consumo de vistas Swing o APIs REST.
    @Transactional
    public List<CuotaDTO> listarCuotas() {
        return listarTodasLasCuotas();
    }

    @Transactional
    public List<CuotaDTO> listarTodasLasCuotas() {
        if (cobranzaService != null) {
            List<CuotaDTO> listaServicio = cobranzaService.listarCuotas();
            if (listaServicio != null && !listaServicio.isEmpty()) {
                return listaServicio;
            }
        }

        if (cuotaRepositorio != null) {
            // Delegacion de la siembra inicial desacoplada a CargadorDatosIniciales (SRP)
            CargadorDatosIniciales.sembrarCuotasSiEstaVacio(cuotaRepositorio, departamentoRepositorio);

            List<CuotaMantenimiento> entidades = cuotaRepositorio.findAllConRelaciones();
            if (!entidades.isEmpty()) {
                List<CuotaDTO> resultado = new ArrayList<>();
                for (CuotaMantenimiento c : entidades) {
                    String numDepto = "Depto N/A";
                    if (c.getDepartamento() != null && c.getDepartamento().getNumero() != null) {
                        numDepto = c.getDepartamento().getNumero();
                    } else if (c.getId() != null) {
                        numDepto = "Depto-" + (100 + c.getId());
                    }

                    double saldo = c.getMontoTotal() != null ? c.getMontoTotal() : (c.getMontoBase() != null ? c.getMontoBase() : 0.0);
                    String fechaVenc = c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : LocalDate.now().plusDays(15).toString();

                    resultado.add(CuotaDTO.builder()
                            .idCuota(c.getId())
                            .numeroDepartamento(numDepto)
                            .montoBase(c.getMontoBase())
                            .montoFinal(saldo)
                            .estado(c.getEstado() != null ? c.getEstado() : "PENDIENTE")
                            .fechaVencimiento(fechaVenc)
                            .build());
                }
                return resultado;
            }
        }

        // Fallback en memoria si no hay repositorios inyectados
        List<CuotaDTO> demo = new ArrayList<>();
        demo.add(CuotaDTO.builder().idCuota(1L).numeroDepartamento("101").montoBase(250.0).montoFinal(250.0).estado("PENDIENTE").fechaVencimiento(LocalDate.now().plusDays(10).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(2L).numeroDepartamento("102").montoBase(180.0).montoFinal(180.0).estado("PAGADO").fechaVencimiento(LocalDate.now().minusDays(5).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(3L).numeroDepartamento("201").montoBase(320.0).montoFinal(160.0).estado("PARCIAL").fechaVencimiento(LocalDate.now().plusDays(5).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(4L).numeroDepartamento("202").montoBase(280.0).montoFinal(280.0).estado("PENDIENTE").fechaVencimiento(LocalDate.now().plusDays(12).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(5L).numeroDepartamento("301").montoBase(350.0).montoFinal(350.0).estado("PAGADO").fechaVencimiento(LocalDate.now().minusDays(2).toString()).build());
        return demo;
    }

    // Delega la creacion del lote de demostracion de cuotas al componente {@link CargadorDatosIniciales}.
    @Transactional
    public void sembrarCuotasInicialesDemo() {
        CargadorDatosIniciales.sembrarCuotasDemo(this.cuotaRepositorio, this.departamentoRepositorio);
    }

    // 8. GESTION DE INMUEBLES (MODULO INMUEBLES)

    // Consulta y mapea todos los departamentos a objetos de transferencia {@link InmuebleDTO},
    @Transactional(readOnly = true)
    public List<InmuebleDTO> listarInmuebles() {
        if (departamentoRepositorio == null) {
            return Collections.emptyList();
        }

        List<Departamento> entidades = departamentoRepositorio.findAllConRelaciones();
        List<InmuebleDTO> resultado = new ArrayList<>();

        for (Departamento d : entidades) {
            String nomTorre = "Torre N/A";
            String nomEdificio = "Edificio N/A";

            if (d.getTorre() != null) {
                if (d.getTorre().getNombre() != null) {
                    nomTorre = d.getTorre().getNombre();
                }
                if (d.getTorre().getEdificio() != null && d.getTorre().getEdificio().getNombre() != null) {
                    nomEdificio = d.getTorre().getEdificio().getNombre();
                }
            }

            resultado.add(InmuebleDTO.builder()
                    .id(d.getId())
                    .numero(d.getNumero() != null ? d.getNumero() : "S/N")
                    .piso(d.getPiso() != null ? d.getPiso() : 0)
                    .metrosCuadrados(d.getMetrosCuadrados() != null ? d.getMetrosCuadrados() : 0.0)
                    .nombreTorre(nomTorre)
                    .nombreEdificio(nomEdificio)
                    .build());
        }

        return resultado;
    }

    // Registra un nuevo departamento en la base de datos, resolviendo o persistiendo el Edificio
    @Transactional
    public void registrarDepartamento(InmuebleDTO dto) {
        if (departamentoRepositorio == null || edificioRepositorio == null || torreRepositorio == null) {
            throw new IllegalStateException("Los repositorios JPA de inmuebles no han sido inyectados en la fachada");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Los datos del inmueble no pueden ser nulos");
        }

        if (dto.getNumero() == null || dto.getNumero().isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }

        if (dto.getPiso() == null || dto.getPiso() < 0) {
            throw new IllegalArgumentException("El número de piso no puede ser negativo");
        }

        if (dto.getMetrosCuadrados() == null || dto.getMetrosCuadrados() <= 0) {
            throw new IllegalArgumentException("El metraje en m² debe ser mayor a cero");
        }

        String nomEdificio = (dto.getNombreEdificio() != null && !dto.getNombreEdificio().isBlank())
                ? dto.getNombreEdificio().trim() : "Edificio Principal";
        String nomTorre = (dto.getNombreTorre() != null && !dto.getNombreTorre().isBlank())
                ? dto.getNombreTorre().trim() : "Torre A";

        // Buscar o crear Edificio
        Edificio edificio = edificioRepositorio.findByNombre(nomEdificio)
                .orElseGet(() -> edificioRepositorio.save(Edificio.builder()
                        .nombre(nomEdificio)
                        .numeroPisos(Math.max(dto.getPiso() + 2, 5))
                        .build()));

        // Buscar o crear Torre vinculada específicamente al Edificio encontrado
        Torre torre = torreRepositorio.findByNombreAndEdificioId(nomTorre, edificio.getId())
                .orElseGet(() -> torreRepositorio.save(Torre.builder()
                        .nombre(nomTorre)
                        .numeroPisos(Math.max(dto.getPiso() + 2, 5))
                        .edificio(edificio)
                        .build()));

        if (torre.getEdificio() == null) {
            torre.setEdificio(edificio);
            torreRepositorio.save(torre);
        }

        Departamento nuevoDepto = Departamento.builder()
                .numero(dto.getNumero().trim())
                .piso(dto.getPiso())
                .metrosCuadrados(dto.getMetrosCuadrados())
                .numeroHabitantes(2)
                .torre(torre)
                .build();

        departamentoRepositorio.save(nuevoDepto);
        log.info("Departamento guardado exitosamente: N° {} en {} - {}",
                nuevoDepto.getNumero(), nomTorre, nomEdificio);

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nuevo departamento registrado: N° " + nuevoDepto.getNumero());
        }
    }

    // 16. MODULO DE RESIDENTES (PERSONAS)

    // Consulta y mapea todos los residentes (Propietarios e Inquilinos) registrados en la base de datos,
    @Transactional(readOnly = true)
    public List<ResidenteDTO> listarResidentes() {
        Map<Long, String> deptoPorPropietario = new HashMap<>();
        Map<Long, Long> deptoIdPorPropietario = new HashMap<>();
        Map<Long, String> torrePorPropietario = new HashMap<>();
        Map<Long, String> deptoPorInquilino = new HashMap<>();
        Map<Long, Long> deptoIdPorInquilino = new HashMap<>();
        Map<Long, String> torrePorInquilino = new HashMap<>();

        if (departamentoRepositorio != null) {
            List<Departamento> deptos = departamentoRepositorio.findAllConRelaciones();
            for (Departamento d : deptos) {
                String nombreTorre = "Torre A";
                try {
                    if (d.getTorre() != null && d.getTorre().getNombre() != null && !d.getTorre().getNombre().isBlank()) {
                        nombreTorre = d.getTorre().getNombre().trim();
                    }
                } catch (Exception ignored) {}

                if (d.getPropietario() != null && d.getPropietario().getId() != null) {
                    deptoPorPropietario.put(d.getPropietario().getId(), d.getNumero());
                    deptoIdPorPropietario.put(d.getPropietario().getId(), d.getId());
                    torrePorPropietario.put(d.getPropietario().getId(), nombreTorre);
                }
                if (d.getInquilinoActual() != null && d.getInquilinoActual().getId() != null) {
                    deptoPorInquilino.put(d.getInquilinoActual().getId(), d.getNumero());
                    deptoIdPorInquilino.put(d.getInquilinoActual().getId(), d.getId());
                    torrePorInquilino.put(d.getInquilinoActual().getId(), nombreTorre);
                }
            }
        }

        List<ResidenteDTO> lista = new ArrayList<>();

        // 1. Propietarios
        if (propietarioRepositorio != null) {
            List<Propietario> propietarios = propietarioRepositorio.findAll();
            for (Propietario p : propietarios) {
                lista.add(ResidenteDTO.builder()
                        .id(p.getId())
                        .dni(p.getDni())
                        .nombres(p.getNombre())
                        .apellidos(p.getApellidos() != null ? p.getApellidos() : "")
                        .telefono(p.getTelefono())
                        .correoElectronico(p.getCorreo())
                        .tipoUsuario("PROPIETARIO")
                        .departamentoId(deptoIdPorPropietario.get(p.getId()))
                        .numeroDepartamento(deptoPorPropietario.getOrDefault(p.getId(), "Sin asignar"))
                        .nombreTorre(torrePorPropietario.getOrDefault(p.getId(), "Torre A"))
                        .build());
            }
        }

        // 2. Inquilinos
        if (inquilinoRepositorio != null) {
            List<Inquilino> inquilinos = inquilinoRepositorio.findAll();
            for (Inquilino inq : inquilinos) {
                lista.add(ResidenteDTO.builder()
                        .id(inq.getId())
                        .dni(inq.getDni())
                        .nombres(inq.getNombre())
                        .apellidos(inq.getApellidos() != null ? inq.getApellidos() : "")
                        .telefono(inq.getTelefono())
                        .correoElectronico(inq.getCorreo())
                        .tipoUsuario("INQUILINO")
                        .departamentoId(deptoIdPorInquilino.get(inq.getId()))
                        .numeroDepartamento(deptoPorInquilino.getOrDefault(inq.getId(), "Sin asignar"))
                        .nombreTorre(torrePorInquilino.getOrDefault(inq.getId(), "Torre A"))
                        .build());
            }
        }

        if (lista.isEmpty()) {
            lista.add(ResidenteDTO.builder().id(1L).dni("10293847").nombres("Carlos").apellidos("Mendoza Torres").telefono("987654321").correoElectronico("carlos.mendoza@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("101").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(2L).dni("84736291").nombres("Ana").apellidos("Rosas Del Campo").telefono("998877665").correoElectronico("ana.rosas@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("102").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(3L).dni("38475629").nombres("Miguel Ángel").apellidos("Benítez Vega").telefono("945612378").correoElectronico("miguel.benitez@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("201").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(4L).dni("62839401").nombres("Roberto").apellidos("Chang Alvarado").telefono("976543210").correoElectronico("roberto.chang@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("301").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(5L).dni("45678912").nombres("Javier").apellidos("Paredes Quispe").telefono("912345678").correoElectronico("javier.paredes@email.com").tipoUsuario("INQUILINO").numeroDepartamento("101").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(6L).dni("56789023").nombres("Lucía").apellidos("Fernández Vega").telefono("923456789").correoElectronico("lucia.fernandez@email.com").tipoUsuario("INQUILINO").numeroDepartamento("201").nombreTorre("Torre A").build());
            lista.add(ResidenteDTO.builder().id(7L).dni("67890134").nombres("Diego").apellidos("Castro Huamán").telefono("934567890").correoElectronico("diego.castro@email.com").tipoUsuario("INQUILINO").numeroDepartamento("301").nombreTorre("Torre A").build());
        }

        return lista;
    }

    // Registra un nuevo residente (Propietario o Inquilino) validando duplicidad de DNI
    @Transactional
    public void registrarResidente(ResidenteDTO dto) {
        if (usuarioRepositorio == null) {
            throw new IllegalStateException("Repositorio de usuarios no disponible en la fachada");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del residente no pueden ser nulos");
        }
        if (dto.getDni() == null || dto.getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        if (dto.getNombres() == null || dto.getNombres().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.getApellidos() == null || dto.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        if (dto.getTipoUsuario() == null || dto.getTipoUsuario().isBlank()) {
            throw new IllegalArgumentException("El tipo de usuario es obligatorio (PROPIETARIO o INQUILINO)");
        }

        String dniLimpio = dto.getDni().trim();
        if (usuarioRepositorio.existsByDni(dniLimpio)) {
            throw new IllegalArgumentException("Ya existe un residente registrado con el DNI: " + dniLimpio);
        }

        String tipo = dto.getTipoUsuario().trim().toUpperCase();

        if ("PROPIETARIO".equals(tipo)) {
            Propietario propietario = Propietario.builder()
                    .dni(dniLimpio)
                    .nombre(dto.getNombres().trim())
                    .apellidos(dto.getApellidos().trim())
                    .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                    .correo(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim() : null)
                    .fechaRegistro(LocalDate.now())
                    .build();

            if (propietarioRepositorio != null) {
                propietario = propietarioRepositorio.save(propietario);
            } else {
                propietario = (Propietario) usuarioRepositorio.save(propietario);
            }

            if (dto.getDepartamentoId() != null && departamentoRepositorio != null) {
                Departamento depto = departamentoRepositorio.findById(dto.getDepartamentoId())
                        .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado con ID: " + dto.getDepartamentoId()));
                depto.setPropietario(propietario);
                departamentoRepositorio.save(depto);
                log.info("Propietario {} asignado al departamento N° {}", propietario.getNombre(), depto.getNumero());
            }

            log.info("Propietario registrado exitosamente: DNI={}, Nombre={} {}",
                    propietario.getDni(), propietario.getNombre(), propietario.getApellidos());

        } else if ("INQUILINO".equals(tipo)) {
            Inquilino inquilino = Inquilino.builder()
                    .dni(dniLimpio)
                    .nombre(dto.getNombres().trim())
                    .apellidos(dto.getApellidos().trim())
                    .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                    .correo(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim() : null)
                    .fechaRegistro(LocalDate.now())
                    .fechaInicioContrato(LocalDate.now())
                    .fechaFinContrato(LocalDate.now().plusYears(1))
                    .build();

            if (inquilinoRepositorio != null) {
                inquilino = inquilinoRepositorio.save(inquilino);
            } else {
                inquilino = (Inquilino) usuarioRepositorio.save(inquilino);
            }

            if (dto.getDepartamentoId() != null && departamentoRepositorio != null) {
                Departamento depto = departamentoRepositorio.findById(dto.getDepartamentoId())
                        .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado con ID: " + dto.getDepartamentoId()));
                depto.setInquilinoActual(inquilino);
                departamentoRepositorio.save(depto);
                log.info("Inquilino {} asignado al departamento N° {}", inquilino.getNombre(), depto.getNumero());
            }

            log.info("Inquilino registrado exitosamente: DNI={}, Nombre={} {}",
                    inquilino.getDni(), inquilino.getNombre(), inquilino.getApellidos());

        } else {
            throw new IllegalArgumentException("Tipo de residente no válido. Debe ser PROPIETARIO o INQUILINO.");
        }

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nuevo residente registrado: " + dto.getNombres() + " " + dto.getApellidos());
        }
    }

    @Transactional
    public void actualizarResidente(ResidenteDTO dto) {
        if (this.personaService != null) {
            this.personaService.actualizarResidente(dto);
        } else {
            if (dto == null || dto.getDni() == null) throw new IllegalArgumentException("DNI no puede ser nulo");
            String dni = dto.getDni().trim();
            boolean act = false;
            if (propietarioRepositorio != null) {
                java.util.Optional<Propietario> opt = propietarioRepositorio.findByDni(dni);
                if (opt.isPresent()) {
                    Propietario p = opt.get();
                    if (dto.getNombres() != null) p.setNombre(dto.getNombres().trim());
                    if (dto.getApellidos() != null) p.setApellidos(dto.getApellidos().trim());
                    if (dto.getTelefono() != null) p.setTelefono(dto.getTelefono().trim());
                    if (dto.getCorreoElectronico() != null) p.setCorreo(dto.getCorreoElectronico().trim());
                    propietarioRepositorio.save(p);
                    act = true;
                }
            }
            if (!act && inquilinoRepositorio != null) {
                java.util.Optional<Inquilino> opt = inquilinoRepositorio.findByDni(dni);
                if (opt.isPresent()) {
                    Inquilino inq = opt.get();
                    if (dto.getNombres() != null) inq.setNombre(dto.getNombres().trim());
                    if (dto.getApellidos() != null) inq.setApellidos(dto.getApellidos().trim());
                    if (dto.getTelefono() != null) inq.setTelefono(dto.getTelefono().trim());
                    if (dto.getCorreoElectronico() != null) inq.setCorreo(dto.getCorreoElectronico().trim());
                    inquilinoRepositorio.save(inq);
                    act = true;
                }
            }
            if (!act) {
                throw new IllegalArgumentException("No se encontró ningún residente con el DNI: " + dni);
            }
        }
    }

    @Transactional
    public void eliminarResidentePorDni(String dni) {
        if (this.personaService != null) {
            this.personaService.eliminarResidentePorDni(dni);
        } else {
            if (dni == null || dni.isBlank()) throw new IllegalArgumentException("DNI no puede estar vacío");
            String dniLimpio = dni.trim();
            boolean elim = false;
            if (propietarioRepositorio != null) {
                java.util.Optional<Propietario> opt = propietarioRepositorio.findByDni(dniLimpio);
                if (opt.isPresent()) {
                    propietarioRepositorio.delete(opt.get());
                    elim = true;
                }
            }
            if (!elim && inquilinoRepositorio != null) {
                java.util.Optional<Inquilino> opt = inquilinoRepositorio.findByDni(dniLimpio);
                if (opt.isPresent()) {
                    inquilinoRepositorio.delete(opt.get());
                    elim = true;
                }
            }
            if (!elim) {
                throw new IllegalArgumentException("No se encontró ningún residente con el DNI: " + dniLimpio);
            }
        }
    }

    // 17. METODOS DELEGADOS PARA GASTOS Y OPERACIONES

    @Transactional(readOnly = true)
    public List<GastoComun> listarGastosComunes() {
        if (cobranzaService != null) {
            List<GastoComun> lista = cobranzaService.listarGastosComunes();
            if (lista != null && !lista.isEmpty()) {
                return lista;
            }
        }
        List<GastoComun> demo = new ArrayList<>();
        demo.add(GastoAgua.builder().id(1L).descripcion("Consumo de Agua Potable Cisterna y Torres").monto(1250.0).fecha(LocalDate.now().minusDays(10)).lecturaActual(450.0).lecturaAnterior(380.0).build());
        demo.add(GastoLuz.builder().id(2L).descripcion("Energía Eléctrica Áreas Comunes y Ascensores").monto(980.0).fecha(LocalDate.now().minusDays(12)).build());
        demo.add(GastoLimpieza.builder().id(3L).descripcion("Servicio de Limpieza Integral y Desinfección").monto(800.0).fecha(LocalDate.now().minusDays(15)).build());
        return demo;
    }

    @Transactional
    public GastoComun registrarGastoComun(GastoComun gasto) {
        GastoComun guardado = gasto;
        if (cobranzaService != null) {
            guardado = cobranzaService.registrarGastoComun(gasto);
        }
        if (gestorNotificaciones != null && guardado != null) {
            gestorNotificaciones.notificar("Nuevo gasto común registrado: " + (guardado.getDescripcion() != null ? guardado.getDescripcion() : "Gasto"));
        }
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<ReservaArea> listarReservas() {
        if (operacionService != null) {
            List<ReservaArea> lista = operacionService.listarReservas();
            if (lista != null && !lista.isEmpty()) {
                return lista;
            }
        }
        List<ReservaArea> demo = new ArrayList<>();
        demo.add(ReservaArea.builder().id(1L).areaComun(AreaComun.builder().id(1L).nombre("Piscina Panorámica").build()).propietario(Propietario.builder().id(1L).nombre("Carlos").apellidos("Mendoza Torres").build()).fechaReserva(LocalDate.now().plusDays(2)).horaInicio(LocalTime.of(14, 0)).horaFin(LocalTime.of(18, 0)).estado("CONFIRMADA").build());
        demo.add(ReservaArea.builder().id(2L).areaComun(AreaComun.builder().id(2L).nombre("Zona BBQ").build()).propietario(Propietario.builder().id(2L).nombre("Ana").apellidos("Rosas Del Campo").build()).fechaReserva(LocalDate.now().plusDays(5)).horaInicio(LocalTime.of(12, 0)).horaFin(LocalTime.of(16, 0)).estado("CONFIRMADA").build());
        return demo;
    }

    @Transactional(readOnly = true)
    public List<AreaComun> listarAreasComunes() {
        if (inmuebleService != null) {
            List<AreaComun> lista = inmuebleService.listarAreasComunes();
            if (lista != null && !lista.isEmpty()) {
                return lista;
            }
        }
        List<AreaComun> demo = new ArrayList<>();
        demo.add(AreaComun.builder().id(1L).nombre("Piscina Panorámica").capacidad(25).costoReserva(0.0).build());
        demo.add(AreaComun.builder().id(2L).nombre("Zona BBQ Parrillas").capacidad(15).costoReserva(0.0).build());
        demo.add(AreaComun.builder().id(3L).nombre("Salón Social Multiusos").capacidad(50).costoReserva(0.0).build());
        return demo;
    }

    @Transactional(readOnly = true)
    public List<Incidente> listarIncidentes() {
        if (operacionService != null) {
            List<Incidente> lista = operacionService.listarIncidentes();
            if (lista != null && !lista.isEmpty()) {
                return lista;
            }
        }
        List<Incidente> demo = new ArrayList<>();
        demo.add(Incidente.builder().id(1L).descripcion("Filtración de agua en ducto principal sótano").reportadoPor("Conserjería").fecha(LocalDate.now().minusDays(1)).estado("EN PROCESO").build());
        demo.add(Incidente.builder().id(2L).descripcion("Luminaria parpadeante en pasillo Torre A piso 3").reportadoPor("Depto 301").fecha(LocalDate.now().minusDays(3)).estado("ATENDIDO").build());
        return demo;
    }

    @Transactional
    public Incidente reportarIncidente(String descripcion, String reportadoPor) {
        Incidente inc = Incidente.builder()
                .descripcion(descripcion)
                .reportadoPor(reportadoPor)
                .fecha(LocalDate.now())
                .estado("ABIERTO")
                .build();
        Incidente res = (operacionService != null) ? operacionService.reportarIncidente(inc) : inc;
        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nuevo incidente reportado: " + descripcion);
        }
        return res;
    }

    @Transactional(readOnly = true)
    public List<Comunicado> listarComunicados() {
        if (operacionService != null) {
            List<Comunicado> lista = operacionService.listarComunicados();
            if (lista != null && !lista.isEmpty()) {
                return lista;
            }
        }
        List<Comunicado> demo = new ArrayList<>();
        demo.add(Comunicado.builder().id(1L).titulo("Convocatoria a Asamblea General Ordinaria").mensaje("Se convoca a todos los propietarios a la Asamblea Ordinaria en el Salón Social.").autor("Junta Directiva").fechaPublicacion(LocalDate.now().minusDays(2)).build());
        demo.add(Comunicado.builder().id(2L).titulo("Mantenimiento Programado de Cisterna").mensaje("Limpieza semestral de cisterna principal y bombas de presión.").autor("Administración").fechaPublicacion(LocalDate.now().minusDays(5)).build());
        demo.add(Comunicado.builder().id(3L).titulo("Uso Responsable de Zonas Comunes").mensaje("Recordatorio sobre normas de convivencia y reserva de parrillas.").autor("Conserjería").fechaPublicacion(LocalDate.now().minusDays(8)).build());
        return demo;
    }

    @Transactional
    public Comunicado publicarComunicado(String titulo, String mensaje, String autor) {
        Comunicado com = Comunicado.builder()
                .titulo(titulo)
                .mensaje(mensaje)
                .autor(autor)
                .fechaPublicacion(LocalDate.now())
                .build();
        if (operacionService != null) {
            return operacionService.publicarComunicado(com);
        }
        return com;
    }

    @Transactional
    public Incidente reportarIncidente(String descripcion, String reportadoPor, String prioridad) {
        Incidente inc = Incidente.builder()
                .descripcion(descripcion)
                .reportadoPor((reportadoPor != null && !reportadoPor.isBlank()) ? reportadoPor : "Administración")
                .fecha(LocalDate.now())
                .estado(prioridad != null ? prioridad : "MEDIA")
                .build();
        Incidente res = (operacionService != null) ? operacionService.reportarIncidente(inc) : inc;
        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nuevo incidente reportado (" + (prioridad != null ? prioridad : "MEDIA") + "): " + descripcion);
        }
        return res;
    }

    @Transactional
    public ReservaArea reservarArea(String nombreArea, String nombrePropietario, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int cantidadPersonas) {
        AreaComun area = null;
        if (inmuebleService != null) {
            List<AreaComun> areas = inmuebleService.listarAreasComunes();
            if (areas != null && !areas.isEmpty()) {
                area = areas.stream()
                        .filter(a -> a.getNombre() != null && a.getNombre().equalsIgnoreCase(nombreArea))
                        .findFirst()
                        .orElse(areas.get(0));
            }
        }
        if (area == null) {
            area = AreaComun.builder().id(1L).nombre(nombreArea != null ? nombreArea : "Área Común").capacidad(20).build();
        }

        Propietario prop = null;
        if (propietarioRepositorio != null) {
            List<Propietario> props = propietarioRepositorio.findAll();
            if (!props.isEmpty()) {
                prop = props.stream()
                        .filter(p -> (p.getNombre() + " " + p.getApellidos()).toLowerCase().contains(nombrePropietario != null ? nombrePropietario.toLowerCase() : ""))
                .findFirst()
                .orElse(props.get(0));
            }
        }
        if (prop == null) {
            prop = Propietario.builder().id(1L).nombre(nombrePropietario != null ? nombrePropietario : "Vecino").dni("12345678").build();
        }

        LocalDate fReserva = fecha != null ? fecha : LocalDate.now().plusDays(1);
        LocalTime hIni = horaInicio != null ? horaInicio : LocalTime.of(10, 0);
        LocalTime hFin = horaFin != null ? horaFin : LocalTime.of(14, 0);
        int cant = cantidadPersonas > 0 ? cantidadPersonas : 5;

        Long nuevoId = (long) ((Math.random() * 900000L) + 100000L);
        ReservaArea res;
        if (operacionService != null) {
            res = operacionService.solicitarReserva(nuevoId, prop, area, fReserva, hIni, hFin, cant);
        } else {
            res = ReservaArea.builder()
                    .id(nuevoId)
                    .propietario(prop)
                    .areaComun(area)
                    .fechaReserva(fReserva)
                    .horaInicio(hIni)
                    .horaFin(hFin)
                    .estado("CONFIRMADA")
                    .build();
        }
        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nueva reserva registrada para " + area.getNombre());
        }
        return res;
    }

    // Genera cuotas de mantenimiento masivas para todos los departamentos registrados
    @Transactional
    public List<CuotaDTO> generarCuotasMasivas(String mesPeriodo, Double montoBase) {
        String periodo = (mesPeriodo != null && !mesPeriodo.isBlank()) ? mesPeriodo.trim() : LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
        Double monto = (montoBase != null && montoBase > 0) ? montoBase : 200.0;

        CuotaPrototipo prototipo = CuotaPrototipo.builder()
                .montoBase(monto)
                .concepto("Mantenimiento Ordinario")
                .mesPeriodo(periodo)
                .estado("PENDIENTE")
                .build();

        List<Departamento> departamentos = (departamentoRepositorio != null)
                ? departamentoRepositorio.findAllConRelaciones()
                : Collections.emptyList();

        List<CuotaDTO> resultado = new ArrayList<>();

        if (departamentos.isEmpty()) {
            String[] deptosSimulados = {"101", "102", "201", "202", "301"};
            for (String num : deptosSimulados) {
                CuotaPrototipo clon = prototipo.clonar();
                resultado.add(CuotaDTO.builder()
                        .idCuota((long) (resultado.size() + 100))
                        .numeroDepartamento(num)
                        .montoBase(clon.getMontoBase())
                        .montoFinal(clon.getMontoBase())
                        .estado(clon.getEstado())
                        .fechaVencimiento(LocalDate.now().plusDays(15).toString())
                        .build());
            }
            return resultado;
        }

        LocalDate vencimiento = LocalDate.now().plusDays(15);
        for (Departamento depto : departamentos) {
            // Regla: no duplicar cuotas a departamentos que ya cuentan con una cuota abierta en el periodo
            if (depto.getId() != null && tieneCuotaAbiertaEnPeriodo(depto.getId(), periodo)) {
                log.info("Departamento N° {} ya cuenta con cuota abierta para el periodo {}. Se omite duplicación.", depto.getNumero(), periodo);
                continue;
            }

            CuotaPrototipo clon = prototipo.clonar();

            CuotaMantenimiento cuotaEntidad = CuotaMantenimiento.builder()
                    .montoBase(clon.getMontoBase())
                    .montoTotal(clon.getMontoBase())
                    .fechaEmision(LocalDate.now())
                    .fechaVencimiento(vencimiento)
                    .estado(clon.getEstado())
                    .departamento(depto)
                    .build();

            if (cuotaRepositorio != null) {
                cuotaEntidad = cuotaRepositorio.save(cuotaEntidad);
            }

            resultado.add(CuotaDTO.builder()
                    .idCuota(cuotaEntidad.getId())
                    .numeroDepartamento(depto.getNumero())
                    .montoBase(cuotaEntidad.getMontoBase())
                    .montoFinal(cuotaEntidad.getMontoTotal())
                    .estado(cuotaEntidad.getEstado())
                    .fechaVencimiento(vencimiento.toString())
                    .build());
        }

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Cuotas masivas generadas para el periodo " + periodo);
        }

        return resultado;
    }

    // 18. OPERACIONES EXTENDIDAS INTERACTIVAS

    // Emite una nueva cuota de mantenimiento ordinaria o extraordinaria para un departamento,
    @Transactional
    public CuotaDTO emitirNuevaCuota(String numeroDepartamento, Double montoBase, LocalDate fechaVencimiento) {
        if (cuotaRepositorio == null) {
            throw new IllegalStateException("Repositorio de cuotas no disponible");
        }
        if (numeroDepartamento == null || numeroDepartamento.isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }
        if (montoBase == null || montoBase <= 0) {
            throw new IllegalArgumentException("El monto base debe ser mayor a cero");
        }

        Departamento depto = (departamentoRepositorio != null)
                ? departamentoRepositorio.findByNumero(numeroDepartamento.trim()).orElse(null)
                : null;

        LocalDate venc = (fechaVencimiento != null) ? fechaVencimiento : LocalDate.now().plusDays(15);

        CuotaMantenimiento nueva = CuotaMantenimiento.builder()
                .montoBase(montoBase)
                .montoTotal(montoBase)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(venc)
                .estado("PENDIENTE")
                .departamento(depto)
                .build();

        nueva = cuotaRepositorio.save(nueva);

        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Nueva cuota emitida para el depto " + (depto != null ? depto.getNumero() : numeroDepartamento));
        }

        return CuotaDTO.builder()
                .idCuota(nueva.getId())
                .numeroDepartamento(depto != null ? depto.getNumero() : numeroDepartamento)
                .montoBase(nueva.getMontoBase())
                .montoFinal(nueva.getMontoTotal())
                .estado(nueva.getEstado())
                .fechaVencimiento(nueva.getFechaVencimiento().toString())
                .build();
    }

    // Simula el cálculo dinámico de una cuota aplicando el Patrón Decorator
    public double calcularMontoConDecorador(double montoBase, boolean aplicarMora, int diasMora, boolean aplicarProntoPago) {
        CuotaBase componente = new CuotaBase(montoBase);
        if (aplicarMora && diasMora > 0) {
            double tasaDiaria = configuracion != null ? configuracion.getTasaInteresMora() : 1.5;
            componente = new DecoradorRecargoMora(componente, tasaDiaria * diasMora);
        }
        if (aplicarProntoPago) {
            componente = new DecoradorDescuentoProntoPago(componente, 5.0);
        }
        return componente.calcularMontoTotal();
    }

    // Actualiza las políticas operacionales y tasas contables del condominio.
    public void actualizarParametrosCondominio(double tasaMora, int diasGracia, int diasCorte) {
        if (!SesionActual.esAdministrador()) {
            throw new SecurityException("Acceso denegado: Se requiere rol ADMINISTRADOR en la capa de servicio para modificar las políticas y parámetros contables del condominio.");
        }
        if (tasaMora < 0 || diasGracia < 0 || diasCorte < 0) {
            throw new IllegalArgumentException("Los valores de políticas contables deben ser no negativos.");
        }
        log.info("Parámetros contables actualizados por [{}]: Tasa Mora={}%, Días Gracia={}, Días Corte={}",
                SesionActual.getNombreUsuario(), tasaMora, diasGracia, diasCorte);
    }

    // Lista los edificios totales del complejo residencial con metricas agregadas
    @Transactional
    public List<EdificioDTO> listarEdificiosTotales() {
        if (edificioRepositorio == null) {
            return obtenerEdificiosTotalesDemo();
        }

        List<Edificio> listaEdificios = edificioRepositorio.findAll();
        List<Torre> todasLasTorres = (torreRepositorio != null) ? torreRepositorio.findAllConEdificio() : Collections.emptyList();
        List<Departamento> todosLosDeptos = (departamentoRepositorio != null) ? departamentoRepositorio.findAllConRelaciones() : Collections.emptyList();

        if (listaEdificios.isEmpty()) {
            listaEdificios = sembrarEdificiosYTorresSiVacio(todasLasTorres);
            todasLasTorres = (torreRepositorio != null) ? torreRepositorio.findAllConEdificio() : Collections.emptyList();
        }

        return mapearEdificiosADTO(listaEdificios, todasLasTorres, todosLosDeptos);
    }

    private List<EdificioDTO> obtenerEdificiosTotalesDemo() {
        return List.of(
                EdificioDTO.builder().id(1L).nombre("Edificio Los Sauces").numeroPisos(10).cantidadTorres(2).cantidadDepartamentos(4).areaTotalConstruida(310.5).nombresTorres("Torre A, Torre B").build(),
                EdificioDTO.builder().id(2L).nombre("Edificio Las Palmeras").numeroPisos(15).cantidadTorres(1).cantidadDepartamentos(2).areaTotalConstruida(180.0).nombresTorres("Torre Principal").build(),
                EdificioDTO.builder().id(3L).nombre("Edificio Los Álamos").numeroPisos(8).cantidadTorres(1).cantidadDepartamentos(1).areaTotalConstruida(110.0).nombresTorres("Torre Central").build()
        );
    }

    private List<Edificio> sembrarEdificiosYTorresSiVacio(List<Torre> todasLasTorres) {
        Edificio e1 = edificioRepositorio.save(Edificio.builder().nombre("Edificio Los Sauces").numeroPisos(10).build());
        Edificio e2 = edificioRepositorio.save(Edificio.builder().nombre("Edificio Las Palmeras").numeroPisos(15).build());
        Edificio e3 = edificioRepositorio.save(Edificio.builder().nombre("Edificio Los Álamos").numeroPisos(8).build());

        if (torreRepositorio != null) {
            if (todasLasTorres.isEmpty()) {
                torreRepositorio.save(Torre.builder().nombre("Torre A").numeroPisos(10).edificio(e1).build());
                torreRepositorio.save(Torre.builder().nombre("Torre B").numeroPisos(10).edificio(e1).build());
                torreRepositorio.save(Torre.builder().nombre("Torre Principal").numeroPisos(15).edificio(e2).build());
                torreRepositorio.save(Torre.builder().nombre("Torre Central").numeroPisos(8).edificio(e3).build());
            } else {
                asignarEdificiosATorresExistentes(todasLasTorres, e1, e2, e3);
            }
        }
        return edificioRepositorio.findAll();
    }

    private void asignarEdificiosATorresExistentes(List<Torre> torres, Edificio e1, Edificio e2, Edificio e3) {
        for (Torre t : torres) {
            if (t.getEdificio() == null) {
                if (t.getNombre() != null && (t.getNombre().contains("A") || t.getNombre().contains("B"))) {
                    t.setEdificio(e1);
                } else if (t.getNombre() != null && t.getNombre().contains("Principal")) {
                    t.setEdificio(e2);
                } else {
                    t.setEdificio(e3);
                }
                torreRepositorio.save(t);
            }
        }
    }

    private List<EdificioDTO> mapearEdificiosADTO(List<Edificio> listaEdificios, List<Torre> todasLasTorres, List<Departamento> todosLosDeptos) {
        List<EdificioDTO> resultado = new ArrayList<>();
        for (Edificio ed : listaEdificios) {
            resultado.add(mapearEdificioIndividualADTO(ed, todasLasTorres, todosLosDeptos));
        }
        return resultado;
    }

    private EdificioDTO mapearEdificioIndividualADTO(Edificio ed, List<Torre> todasLasTorres, List<Departamento> todosLosDeptos) {
        List<Torre> torresDelEdificio = todasLasTorres.stream()
                .filter(t -> t.getEdificio() != null && ed.getId().equals(t.getEdificio().getId()))
                .toList();

        List<Long> idTorres = torresDelEdificio.stream().map(Torre::getId).toList();

        List<Departamento> deptosDelEdificio = todosLosDeptos.stream()
                .filter(d -> d.getTorre() != null && idTorres.contains(d.getTorre().getId()))
                .toList();

        double areaTotal = deptosDelEdificio.stream()
                .mapToDouble(d -> d.getMetrosCuadrados() != null ? d.getMetrosCuadrados() : 0.0)
                .sum();

        String nombresTorres = torresDelEdificio.stream()
                .map(Torre::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin torres");

        return EdificioDTO.builder()
                .id(ed.getId())
                .nombre(ed.getNombre())
                .numeroPisos(ed.getNumeroPisos())
                .cantidadTorres(torresDelEdificio.size())
                .cantidadDepartamentos(deptosDelEdificio.size())
                .areaTotalConstruida(areaTotal)
                .nombresTorres(nombresTorres)
                .build();
    }

    // Registra un nuevo edificio completo con su primera torre asociada.
    @Transactional
    public EdificioDTO registrarNuevoEdificio(String nombre, int numeroPisos, String nombreTorreInicial) {
        if (edificioRepositorio == null || torreRepositorio == null) {
            throw new IllegalStateException("Repositorios de inmuebles no disponibles");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del edificio es obligatorio");
        }
        if (numeroPisos <= 0) {
            throw new IllegalArgumentException("El número de pisos debe ser mayor a cero");
        }

        String nomTorre = (nombreTorreInicial != null && !nombreTorreInicial.isBlank()) ? nombreTorreInicial.trim() : "Torre A";

        Edificio nuevoEd = edificioRepositorio.save(Edificio.builder()
                .nombre(nombre.trim())
                .numeroPisos(numeroPisos)
                .build());

        torreRepositorio.save(Torre.builder()
                .nombre(nomTorre)
                .numeroPisos(numeroPisos)
                .edificio(nuevoEd)
                .build());

        return EdificioDTO.builder()
                .id(nuevoEd.getId())
                .nombre(nuevoEd.getNombre())
                .numeroPisos(nuevoEd.getNumeroPisos())
                .cantidadTorres(1)
                .cantidadDepartamentos(0)
                .areaTotalConstruida(0.0)
                .nombresTorres(nomTorre)
                .build();
    }

    // Registra un gasto de consumo individual (Agua o Luz) para un departamento especifico,
    @Transactional
    public GastoComun registrarGastoServicioDepartamento(
            String tipoServicio,
            String numeroDepartamento,
            double lecturaAnterior,
            double lecturaActual,
            double tarifaUnitaria,
            boolean imputarComoCuota) {

        if (tipoServicio == null || tipoServicio.isBlank()) {
            throw new IllegalArgumentException("Debe especificar el tipo de servicio (AGUA o LUZ)");
        }
        if (numeroDepartamento == null || numeroDepartamento.isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }
        if (lecturaActual < lecturaAnterior) {
            throw new IllegalArgumentException("La lectura actual no puede ser menor a la lectura anterior");
        }
        if (tarifaUnitaria <= 0) {
            throw new IllegalArgumentException("La tarifa unitaria debe ser mayor a cero");
        }

        double consumo = Math.max(0.0, lecturaActual - lecturaAnterior);
        double montoTotal = Math.round((consumo * tarifaUnitaria) * 100.0) / 100.0;

        GastoComun nuevoGasto;
        if ("AGUA".equalsIgnoreCase(tipoServicio)) {
            String desc = "Consumo Agua Depto " + numeroDepartamento.trim() + " (" + String.format("%.2f", consumo) + " m³ a S/ " + tarifaUnitaria + "/m³)";
            nuevoGasto = GastoComunFabrica.crearGastoAgua(null, montoTotal, LocalDate.now(), desc, null, lecturaActual, lecturaAnterior);
        } else {
            String desc = "Consumo Luz Depto " + numeroDepartamento.trim() + " (" + String.format("%.2f", consumo) + " kWh a S/ " + tarifaUnitaria + "/kWh)";
            nuevoGasto = GastoComunFabrica.crearGastoLuz(null, montoTotal, LocalDate.now(), desc, null, consumo);
        }

        GastoComun persistido = registrarGastoComun(nuevoGasto);

        if (imputarComoCuota) {
            emitirNuevaCuota(numeroDepartamento.trim(), montoTotal, LocalDate.now().plusDays(15));
        }

        return persistido;
    }

    // 19. MÉTODOS DE VALIDACIÓN Y CONSULTA UI

    @Transactional(readOnly = true)
    public List<Torre> listarTorres() {
        if (inmuebleService != null) return inmuebleService.listarTorres();
        if (torreRepositorio != null) return torreRepositorio.findAllConEdificio();
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public List<Edificio> listarEdificios() {
        if (inmuebleService != null) return inmuebleService.listarEdificios();
        if (edificioRepositorio != null) return edificioRepositorio.findAll();
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public boolean existeDepartamentoPorNumeroTorreYEdificio(String numero, String nombreTorre, String nombreEdificio) {
        if (inmuebleService != null) {
            return inmuebleService.existeDepartamentoPorNumeroTorreYEdificio(numero, nombreTorre, nombreEdificio);
        }
        if (departamentoRepositorio != null && numero != null) {
            String num = numero.trim();
            return departamentoRepositorio.findByNumero(num).isPresent();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean existeResidentePorDni(String dni) {
        if (personaService != null) {
            return personaService.existeResidentePorDni(dni);
        }
        if (dni != null && usuarioRepositorio != null) {
            return usuarioRepositorio.existsByDni(dni.trim());
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean existePropietarioEnDepartamento(Long departamentoId) {
        if (personaService != null) {
            return personaService.existePropietarioEnDepartamento(departamentoId);
        }
        if (departamentoId != null && departamentoRepositorio != null) {
            return departamentoRepositorio.findById(departamentoId)
                    .map(d -> d.getPropietario() != null)
                    .orElse(false);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean existeSolapamientoReserva(String nombreArea, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (operacionService != null) {
            return operacionService.existeSolapamientoReserva(nombreArea, fecha, horaInicio, horaFin);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean tieneCuotaAbiertaEnPeriodo(Long departamentoId, String periodo) {
        if (cobranzaService != null) {
            return cobranzaService.tieneCuotaAbiertaEnPeriodo(departamentoId, periodo);
        }
        if (cuotaRepositorio != null && departamentoId != null && periodo != null) {
            String p = periodo.trim();
            List<CuotaMantenimiento> cuotas = cuotaRepositorio.findByDepartamentoId(departamentoId);
            return cuotas.stream().anyMatch(c ->
                    !"PAGADO".equalsIgnoreCase(c.getEstado())
                            && ((c.getFechaEmision() != null && c.getFechaEmision().toString().startsWith(p))
                            || (c.getFechaVencimiento() != null && c.getFechaVencimiento().toString().startsWith(p)))
            );
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Propietario> listarPropietarios() {
        if (propietarioRepositorio != null) {
            return propietarioRepositorio.findAll();
        }
        return Collections.emptyList();
    }

    @Transactional
    public List<Visita> listarVisitas() {
        if (visitaRepositorio != null) {
            try {
                List<Visita> visitas = visitaRepositorio.findAllConRelaciones();
                if (!visitas.isEmpty()) {
                    return visitas;
                }
                // Si la BD de visitas está vacía, sembrar 6 registros con fechas y horas reales
                sembrarVisitasEnRepositorio();
                visitas = visitaRepositorio.findAllConRelaciones();
                if (!visitas.isEmpty()) {
                    return visitas;
                }
            } catch (Exception ex) {
                log.warn("Error al consultar visitas en BD, recurriendo a memoria: {}", ex.getMessage());
            }
        }
        if (visitasMemoria.isEmpty()) {
            sembrarVisitasMemoria();
        }
        return new ArrayList<>(visitasMemoria);
    }

    private void sembrarVisitasEnRepositorio() {
        if (visitaRepositorio == null) return;
        Departamento d101 = buscarDepartamentoPorNumero("101");
        Departamento d201 = buscarDepartamentoPorNumero("201");
        Departamento d102 = buscarDepartamentoPorNumero("102");
        Departamento d202 = buscarDepartamentoPorNumero("202");
        Departamento d301 = buscarDepartamentoPorNumero("301");

        visitaRepositorio.save(Visita.builder()
                .nombre("Mario Ramos Paredes")
                .dni("45892134")
                .departamento(d101)
                .motivo("Visita Familiar")
                .fechaIngreso(LocalDateTime.now().minusHours(2).minusMinutes(15))
                .fechaSalida(null)
                .build());

        visitaRepositorio.save(Visita.builder()
                .nombre("Courier Express (Luis Vega)")
                .dni("71239845")
                .departamento(d201)
                .motivo("Delivery / Encomienda")
                .fechaIngreso(LocalDateTime.now().minusMinutes(45))
                .fechaSalida(LocalDateTime.now().minusMinutes(35))
                .build());

        visitaRepositorio.save(Visita.builder()
                .nombre("Sodimac Servicios (Jorge Silva)")
                .dni("08765432")
                .departamento(d301)
                .motivo("Mantenimiento Eléctrico")
                .fechaIngreso(LocalDateTime.now().minusHours(3))
                .fechaSalida(null)
                .build());

        visitaRepositorio.save(Visita.builder()
                .nombre("Patricia Delgado Ríos")
                .dni("44332211")
                .departamento(d102)
                .motivo("Visita Amical")
                .fechaIngreso(LocalDateTime.now().minusDays(1).withHour(18).withMinute(30))
                .fechaSalida(LocalDateTime.now().minusDays(1).withHour(21).withMinute(0))
                .build());

        visitaRepositorio.save(Visita.builder()
                .nombre("Cálidda Gas Natural (Raúl Cruz)")
                .dni("10987654")
                .departamento(d202)
                .motivo("Inspección Técnica Periódica")
                .fechaIngreso(LocalDateTime.now().minusMinutes(50))
                .fechaSalida(null)
                .build());

        visitaRepositorio.save(Visita.builder()
                .nombre("Farmacia delivery (Carlos Quispe)")
                .dni("60124589")
                .departamento(d101)
                .motivo("Entrega de Medicamentos")
                .fechaIngreso(LocalDateTime.now().minusMinutes(25))
                .fechaSalida(LocalDateTime.now().minusMinutes(10))
                .build());
    }

    private synchronized void sembrarVisitasMemoria() {
        if (!visitasMemoria.isEmpty()) return;
        Departamento d101 = buscarDepartamentoPorNumero("101");
        Departamento d201 = buscarDepartamentoPorNumero("201");
        Departamento d102 = buscarDepartamentoPorNumero("102");
        Departamento d202 = buscarDepartamentoPorNumero("202");
        Departamento d301 = buscarDepartamentoPorNumero("301");

        visitasMemoria.add(Visita.builder()
                .id(1L)
                .nombre("Mario Ramos Paredes")
                .dni("45892134")
                .departamento(d101)
                .motivo("Visita Familiar")
                .fechaIngreso(LocalDateTime.now().minusHours(2).minusMinutes(15))
                .fechaSalida(null)
                .build());

        visitasMemoria.add(Visita.builder()
                .id(2L)
                .nombre("Courier Express (Luis Vega)")
                .dni("71239845")
                .departamento(d201)
                .motivo("Delivery / Encomienda")
                .fechaIngreso(LocalDateTime.now().minusMinutes(45))
                .fechaSalida(LocalDateTime.now().minusMinutes(35))
                .build());

        visitasMemoria.add(Visita.builder()
                .id(3L)
                .nombre("Sodimac Servicios (Jorge Silva)")
                .dni("08765432")
                .departamento(d301)
                .motivo("Mantenimiento Eléctrico")
                .fechaIngreso(LocalDateTime.now().minusHours(3))
                .fechaSalida(null)
                .build());

        visitasMemoria.add(Visita.builder()
                .id(4L)
                .nombre("Patricia Delgado Ríos")
                .dni("44332211")
                .departamento(d102)
                .motivo("Visita Amical")
                .fechaIngreso(LocalDateTime.now().minusDays(1).withHour(18).withMinute(30))
                .fechaSalida(LocalDateTime.now().minusDays(1).withHour(21).withMinute(0))
                .build());

        visitasMemoria.add(Visita.builder()
                .id(5L)
                .nombre("Cálidda Gas Natural (Raúl Cruz)")
                .dni("10987654")
                .departamento(d202)
                .motivo("Inspección Técnica Periódica")
                .fechaIngreso(LocalDateTime.now().minusMinutes(50))
                .fechaSalida(null)
                .build());

        visitasMemoria.add(Visita.builder()
                .id(6L)
                .nombre("Farmacia delivery (Carlos Quispe)")
                .dni("60124589")
                .departamento(d101)
                .motivo("Entrega de Medicamentos")
                .fechaIngreso(LocalDateTime.now().minusMinutes(25))
                .fechaSalida(LocalDateTime.now().minusMinutes(10))
                .build());
    }

    private Departamento buscarDepartamentoPorNumero(String numero) {
        if (numero == null) return null;
        String numLimpio = numero.replace("Depto", "").trim();
        if (departamentoRepositorio != null) {
            try {
                return departamentoRepositorio.findByNumero(numLimpio).orElse(null);
            } catch (Exception ignored) {}
        }
        return Departamento.builder().numero(numLimpio).build();
    }

    @Transactional
    public Visita registrarIngresoVisita(String nombre, String dni, String deptoNumero, String motivo) {
        Departamento d = buscarDepartamentoPorNumero(deptoNumero);
        Visita v = Visita.builder()
                .nombre(nombre != null ? nombre.trim() : "")
                .dni(dni != null ? dni.trim() : "")
                .departamento(d)
                .motivo(motivo != null ? motivo.trim() : "Visita Regular")
                .fechaIngreso(LocalDateTime.now())
                .fechaSalida(null)
                .build();

        if (visitaRepositorio != null) {
            try {
                Visita guardada = visitaRepositorio.save(v);
                if (gestorNotificaciones != null) {
                    gestorNotificaciones.notificar("Control de Acceso: Ingreso registrado para " + v.getNombre() + " (Depto " + (d != null ? d.getNumero() : deptoNumero) + ")");
                }
                return guardada;
            } catch (Exception ex) {
                log.warn("Error al guardar visita en BD: {}", ex.getMessage());
            }
        }

        if (v.getId() == null) {
            v.setId((long) (visitasMemoria.size() + 1));
        }
        visitasMemoria.add(0, v);
        if (gestorNotificaciones != null) {
            gestorNotificaciones.notificar("Control de Acceso: Ingreso registrado para " + v.getNombre() + " (Depto " + (d != null ? d.getNumero() : deptoNumero) + ")");
        }
        return v;
    }

    @Transactional
    public boolean registrarSalidaVisita(Long visitaId) {
        if (visitaId == null) return false;
        if (visitaRepositorio != null) {
            try {
                Optional<Visita> opt = visitaRepositorio.findById(visitaId);
                if (opt.isPresent()) {
                    Visita v = opt.get();
                    v.setFechaSalida(LocalDateTime.now());
                    visitaRepositorio.save(v);
                    if (gestorNotificaciones != null) {
                        gestorNotificaciones.notificar("Control de Acceso: Salida registrada para " + v.getNombre());
                    }
                    return true;
                }
            } catch (Exception ex) {
                log.warn("Error al registrar salida de visita en BD: {}", ex.getMessage());
            }
        }

        for (Visita v : visitasMemoria) {
            if (visitaId.equals(v.getId())) {
                v.setFechaSalida(LocalDateTime.now());
                if (gestorNotificaciones != null) {
                    gestorNotificaciones.notificar("Control de Acceso: Salida registrada para " + v.getNombre());
                }
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean eliminarVisita(Long visitaId) {
        if (visitaId == null) return false;
        if (visitaRepositorio != null) {
            try {
                if (visitaRepositorio.existsById(visitaId)) {
                    visitaRepositorio.deleteById(visitaId);
                    return true;
                }
            } catch (Exception ex) {
                log.warn("Error al eliminar visita en BD: {}", ex.getMessage());
            }
        }
        return visitasMemoria.removeIf(v -> visitaId.equals(v.getId()));
    }

    @Transactional
    public boolean actualizarDepartamento(String numero, Integer piso, Double metrosCuadrados, Integer numeroHabitantes) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }
        if (departamentoRepositorio != null) {
            Departamento depto = departamentoRepositorio.findByNumero(numero.trim()).orElse(null);
            if (depto != null) {
                if (piso != null && piso > 0) depto.setPiso(piso);
                if (metrosCuadrados != null && metrosCuadrados > 0) depto.setMetrosCuadrados(metrosCuadrados);
                if (numeroHabitantes != null && numeroHabitantes >= 0) depto.setNumeroHabitantes(numeroHabitantes);
                departamentoRepositorio.save(depto);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean eliminarDepartamento(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }
        if (departamentoRepositorio != null) {
            Departamento depto = departamentoRepositorio.findByNumero(numero.trim()).orElse(null);
            if (depto != null) {
                if (cuotaRepositorio != null) {
                    boolean tieneCuotas = cuotaRepositorio.countByDepartamentoId(depto.getId()) > 0;
                    if (tieneCuotas) {
                        throw new IllegalStateException("No se puede eliminar el Departamento " + numero + " porque registra cuotas de mantenimiento asociadas.");
                    }
                }
                depto.setPropietario(null);
                depto.setInquilinoActual(null);
                departamentoRepositorio.delete(depto);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean anularCuota(Long cuotaId) {
        if (cuotaId == null) return false;
        if (cuotaRepositorio != null) {
            Optional<CuotaMantenimiento> opt = cuotaRepositorio.findById(cuotaId);
            if (opt.isPresent()) {
                CuotaMantenimiento c = opt.get();
                if ("PAGADO".equalsIgnoreCase(c.getEstado())) {
                    throw new IllegalStateException("No se puede anular una cuota que ya se encuentra pagada. Utilice la función Deshacer Pago.");
                }
                cuotaRepositorio.delete(c);
                return true;
            }
        }
        return false;
    }

    // Motor de busqueda global polimorfico y desacoplado, filtrado estrictamente segun el rol
    public List<ResultadoBusquedaDTO> buscarEnSistema(String termino, RolUsuario rol, String deptoResidente) {
        if (this.busquedaGlobalService == null) {
            this.busquedaGlobalService = new com.condominio.service.impl.BusquedaGlobalServiceImpl(
                    this.cobranzaService, this.inmuebleService, this.personaService, this.operacionService);
        }
        return this.busquedaGlobalService.buscarEnSistema(termino, rol, deptoResidente);
    }

    public List<ResultadoBusquedaDTO> buscarEnSistema(String termino) {
        return buscarEnSistema(termino, SesionActual.getRol(), "101");
    }
}
