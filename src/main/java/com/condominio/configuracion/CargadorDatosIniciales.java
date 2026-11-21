package com.condominio.configuracion;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Inquilino;
import com.condominio.modelo.personas.Propietario;
import com.condominio.modelo.personas.Visita;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.repositorio.AreaComunRepositorio;
import com.condominio.repositorio.ComunicadoRepositorio;
import com.condominio.repositorio.CuotaRepositorio;
import com.condominio.repositorio.DepartamentoRepositorio;
import com.condominio.repositorio.EdificioRepositorio;
import com.condominio.repositorio.GastoComunRepositorio;
import com.condominio.repositorio.IncidenteRepositorio;
import com.condominio.repositorio.InquilinoRepositorio;
import com.condominio.repositorio.PagoRepositorio;
import com.condominio.repositorio.PropietarioRepositorio;
import com.condominio.repositorio.ReservaAreaRepositorio;
import com.condominio.repositorio.TorreRepositorio;
import com.condominio.repositorio.UsuarioRepositorio;
import com.condominio.repositorio.UsuarioSistemaRepositorio;
import com.condominio.repositorio.VisitaRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

// Componente de infraestructura responsable de la inicializacion y siembra de datos demo.
@Slf4j
@Component
@Order(1)
public class CargadorDatosIniciales implements CommandLineRunner {

    private final CuotaRepositorio cuotaRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final EdificioRepositorio edificioRepositorio;
    private final TorreRepositorio torreRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PropietarioRepositorio propietarioRepositorio;
    private final InquilinoRepositorio inquilinoRepositorio;
    private final AreaComunRepositorio areaComunRepositorio;
    private final GastoComunRepositorio gastoComunRepositorio;
    private final ReservaAreaRepositorio reservaAreaRepositorio;
    private final IncidenteRepositorio incidenteRepositorio;
    private final ComunicadoRepositorio comunicadoRepositorio;
    private final PagoRepositorio pagoRepositorio;
    @Autowired(required = false)
    private UsuarioSistemaRepositorio usuarioSistemaRepositorio;
    @Autowired(required = false)
    private VisitaRepositorio visitaRepositorio;

    @Autowired
    public CargadorDatosIniciales(
            CuotaRepositorio cuotaRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            PropietarioRepositorio propietarioRepositorio,
            InquilinoRepositorio inquilinoRepositorio,
            AreaComunRepositorio areaComunRepositorio,
            GastoComunRepositorio gastoComunRepositorio,
            ReservaAreaRepositorio reservaAreaRepositorio,
            IncidenteRepositorio incidenteRepositorio,
            ComunicadoRepositorio comunicadoRepositorio,
            @Autowired(required = false) PagoRepositorio pagoRepositorio) {
        this.cuotaRepositorio = cuotaRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.edificioRepositorio = edificioRepositorio;
        this.torreRepositorio = torreRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.propietarioRepositorio = propietarioRepositorio;
        this.inquilinoRepositorio = inquilinoRepositorio;
        this.areaComunRepositorio = areaComunRepositorio;
        this.gastoComunRepositorio = gastoComunRepositorio;
        this.reservaAreaRepositorio = reservaAreaRepositorio;
        this.incidenteRepositorio = incidenteRepositorio;
        this.comunicadoRepositorio = comunicadoRepositorio;
        this.pagoRepositorio = pagoRepositorio;
    }

    public CargadorDatosIniciales(
            CuotaRepositorio cuotaRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            PropietarioRepositorio propietarioRepositorio,
            InquilinoRepositorio inquilinoRepositorio,
            AreaComunRepositorio areaComunRepositorio,
            GastoComunRepositorio gastoComunRepositorio,
            ReservaAreaRepositorio reservaAreaRepositorio,
            IncidenteRepositorio incidenteRepositorio,
            ComunicadoRepositorio comunicadoRepositorio) {
        this(cuotaRepositorio, departamentoRepositorio, edificioRepositorio, torreRepositorio,
                usuarioRepositorio, propietarioRepositorio, inquilinoRepositorio,
                areaComunRepositorio, gastoComunRepositorio, reservaAreaRepositorio,
                incidenteRepositorio, comunicadoRepositorio, null);
    }

    // Constructor sobrecargado de 4 parametros para retrocompatibilidad.
    public CargadorDatosIniciales(
            CuotaRepositorio cuotaRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio) {
        this(cuotaRepositorio, departamentoRepositorio, edificioRepositorio, torreRepositorio,
                null, null, null, null, null, null, null, null, null);
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando siembra inicial de datos...");

        // 1. Primero Edificio, Torre y Departamento para evitar errores de llave foránea
        if (this.edificioRepositorio != null && this.torreRepositorio != null && this.departamentoRepositorio != null) {
            if (this.departamentoRepositorio.count() == 0) {
                log.info("departamentoRepositorio.count() == 0: Sembrando inmuebles iniciales...");
                sembrarInmueblesSiEstaVacio(this.edificioRepositorio, this.torreRepositorio, this.departamentoRepositorio);
            }
        }

        // 2. Personas (Propietarios e Inquilinos)
        if (this.propietarioRepositorio != null && this.inquilinoRepositorio != null) {
            if (this.propietarioRepositorio.count() == 0) {
                sembrarPersonasSiEstaVacio(this.propietarioRepositorio, this.inquilinoRepositorio, this.departamentoRepositorio);
            }
        }

        // 3. Cuotas de mantenimiento: Si cuotaRepositorio.count() == 0, sembrar siempre las 5 cuotas demo
        if (this.cuotaRepositorio != null && this.cuotaRepositorio.count() == 0) {
            log.info("cuotaRepositorio.count() == 0: Sembrando 5 cuotas demo vinculadas a departamentos...");
            if (this.departamentoRepositorio != null && this.departamentoRepositorio.count() == 0) {
                sembrarInmueblesSiEstaVacio(this.edificioRepositorio, this.torreRepositorio, this.departamentoRepositorio);
            }
            sembrarCuotasDemo(this.cuotaRepositorio, this.departamentoRepositorio, this.pagoRepositorio);
            log.info("5 cuotas demo sembradas exitosamente.");
        }

        // 4. Áreas comunes
        if (this.areaComunRepositorio != null && this.areaComunRepositorio.count() == 0) {
            sembrarAreasComunesSiEstaVacio(this.areaComunRepositorio);
        }

        // 5. Gastos comunes
        if (this.gastoComunRepositorio != null && this.gastoComunRepositorio.count() == 0) {
            sembrarGastosComunesSiEstaVacio(this.gastoComunRepositorio);
        }

        // 6. Operaciones (Reservas, Incidentes y Comunicados)
        if (this.reservaAreaRepositorio != null && this.incidenteRepositorio != null) {
            if (this.reservaAreaRepositorio.count() == 0 || this.incidenteRepositorio.count() == 0) {
                sembrarOperacionesSiEstaVacio(this.reservaAreaRepositorio, this.incidenteRepositorio, this.comunicadoRepositorio,
                        this.propietarioRepositorio, this.areaComunRepositorio);
            }
        }

        // 7. Visitas y Control de Accesos en Garita / Portería
        if (this.visitaRepositorio != null && this.visitaRepositorio.count() == 0 && this.departamentoRepositorio != null) {
            Departamento d101 = this.departamentoRepositorio.findByNumero("101").orElse(null);
            Departamento d201 = this.departamentoRepositorio.findByNumero("201").orElse(null);
            Departamento d102 = this.departamentoRepositorio.findByNumero("102").orElse(null);
            Departamento d202 = this.departamentoRepositorio.findByNumero("202").orElse(null);
            Departamento d301 = this.departamentoRepositorio.findByNumero("301").orElse(null);

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Mario Ramos Paredes")
                    .dni("45892134")
                    .departamento(d101)
                    .motivo("Visita Familiar")
                    .fechaIngreso(java.time.LocalDateTime.now().minusHours(2).minusMinutes(15))
                    .fechaSalida(null)
                    .build());

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Courier Express (Luis Vega)")
                    .dni("71239845")
                    .departamento(d201)
                    .motivo("Delivery / Encomienda")
                    .fechaIngreso(java.time.LocalDateTime.now().minusMinutes(45))
                    .fechaSalida(java.time.LocalDateTime.now().minusMinutes(35))
                    .build());

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Sodimac Servicios (Jorge Silva)")
                    .dni("08765432")
                    .departamento(d301)
                    .motivo("Mantenimiento Eléctrico")
                    .fechaIngreso(java.time.LocalDateTime.now().minusHours(3))
                    .fechaSalida(null)
                    .build());

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Patricia Delgado Ríos")
                    .dni("44332211")
                    .departamento(d102)
                    .motivo("Visita Amical")
                    .fechaIngreso(java.time.LocalDateTime.now().minusDays(1).withHour(18).withMinute(30))
                    .fechaSalida(java.time.LocalDateTime.now().minusDays(1).withHour(21).withMinute(0))
                    .build());

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Cálidda Gas Natural (Raúl Cruz)")
                    .dni("10987654")
                    .departamento(d202)
                    .motivo("Inspección Técnica Periódica")
                    .fechaIngreso(java.time.LocalDateTime.now().minusMinutes(50))
                    .fechaSalida(null)
                    .build());

            this.visitaRepositorio.save(Visita.builder()
                    .nombre("Farmacia delivery (Carlos Quispe)")
                    .dni("60124589")
                    .departamento(d101)
                    .motivo("Entrega de Medicamentos")
                    .fechaIngreso(java.time.LocalDateTime.now().minusMinutes(25))
                    .fechaSalida(java.time.LocalDateTime.now().minusMinutes(10))
                    .build());

            log.info("Siembra inicial de visitas en garita completada: 6 registros demo con fechas y horarios.");
        }

        // 8. Resumen del seed auditable (4.2)
        if (this.departamentoRepositorio != null && this.cuotaRepositorio != null) {
            List<String> numsDeptos = this.departamentoRepositorio.findAll().stream()
                    .map(Departamento::getNumero)
                    .sorted()
                    .collect(Collectors.toList());
            List<String> cuotasInfo = this.cuotaRepositorio.findAll().stream()
                    .map(c -> String.format("ID:%d-%s:%.2f", c.getId(), c.getEstado(), c.getMontoTotal()))
                    .collect(Collectors.toList());
            log.info("RESUMEN DEL SEED -> Departamentos: {} | Cuotas [ID-Estado-Saldo]: {}", numsDeptos, cuotasInfo);
        }

        // 9. Usuarios de Sistema para Autenticación RBAC (admin, conserje, residente)
        if (this.usuarioSistemaRepositorio != null && this.usuarioSistemaRepositorio.count() == 0) {
            sembrarUsuariosSistemaDemo(this.usuarioSistemaRepositorio);
        }

        log.info("Verificación y siembra integral de datos finalizada.");
    }

    public static void sembrarUsuariosSistemaDemo(UsuarioSistemaRepositorio repo) {
        if (repo == null || repo.count() > 0) return;

        repo.save(UsuarioSistema.builder()
                .usuario("admin")
                .clave("admin123")
                .rol(RolUsuario.ADMINISTRADOR)
                .nombreCompleto("Administrador General del Condominio")
                .activo(true)
                .build());

        repo.save(UsuarioSistema.builder()
                .usuario("presidente")
                .clave("presidente123")
                .rol(RolUsuario.PRESIDENTE)
                .nombreCompleto("Presidente de la Junta Directiva (Depto 101)")
                .activo(true)
                .build());

        repo.save(UsuarioSistema.builder()
                .usuario("gerente")
                .clave("gerente123")
                .rol(RolUsuario.GERENTE)
                .nombreCompleto("Gerente de Operaciones y Mantenimiento")
                .activo(true)
                .build());

        repo.save(UsuarioSistema.builder()
                .usuario("conserje")
                .clave("conserje123")
                .rol(RolUsuario.CONSERJE)
                .nombreCompleto("Conserjería y Recepción Principal")
                .activo(true)
                .build());

        repo.save(UsuarioSistema.builder()
                .usuario("residente")
                .clave("residente123")
                .rol(RolUsuario.RESIDENTE)
                .nombreCompleto("Residente Propietario / Inquilino")
                .activo(true)
                .build());

        log.info("Usuarios de sistema sembrados exitosamente (admin, presidente, gerente, conserje, residente).");
    }

    // Verifica si la base de datos carece de cuotas y siembra los registros de prueba vinculados a departamentos.
    @Transactional
    public static void sembrarCuotasSiEstaVacio(CuotaRepositorio cuotaRepo, DepartamentoRepositorio deptoRepo) {
        sembrarCuotasSiEstaVacio(cuotaRepo, deptoRepo, null);
    }

    @Transactional
    public static void sembrarCuotasSiEstaVacio(CuotaRepositorio cuotaRepo, DepartamentoRepositorio deptoRepo, PagoRepositorio pagoRepo) {
        if (cuotaRepo != null && cuotaRepo.count() == 0) {
            sembrarCuotasDemo(cuotaRepo, deptoRepo, pagoRepo);
        }
    }

    @Transactional
    public static void sembrarCuotasSiEstaVacio(CuotaRepositorio cuotaRepo) {
        sembrarCuotasSiEstaVacio(cuotaRepo, null, null);
    }

    @Transactional
    public static void sembrarCuotasDemo(CuotaRepositorio cuotaRepo) {
        sembrarCuotasDemo(cuotaRepo, null, null);
    }

    @Transactional
    public static void sembrarCuotasDemo(CuotaRepositorio cuotaRepo, DepartamentoRepositorio deptoRepo) {
        sembrarCuotasDemo(cuotaRepo, deptoRepo, null);
    }

    @Transactional
    public static void sembrarCuotasDemo(CuotaRepositorio cuotaRepo, DepartamentoRepositorio deptoRepo, PagoRepositorio pagoRepo) {
        if (cuotaRepo == null) {
            return;
        }

        Departamento depto101 = (deptoRepo != null) ? deptoRepo.findByNumero("101").orElse(null) : null;
        Departamento depto102 = (deptoRepo != null) ? deptoRepo.findByNumero("102").orElse(null) : null;
        Departamento depto201 = (deptoRepo != null) ? deptoRepo.findByNumero("201").orElse(null) : null;
        Departamento depto202 = (deptoRepo != null) ? deptoRepo.findByNumero("202").orElse(null) : null;
        Departamento depto301 = (deptoRepo != null) ? deptoRepo.findByNumero("301").orElse(null) : null;

        if (deptoRepo != null && (depto101 == null || depto102 == null || depto201 == null || depto202 == null || depto301 == null)) {
            List<Departamento> todos = deptoRepo.findAll();
            if (todos.size() >= 5) {
                if (depto101 == null) depto101 = todos.get(0);
                if (depto102 == null) depto102 = todos.get(1);
                if (depto201 == null) depto201 = todos.get(2);
                if (depto202 == null) depto202 = todos.get(3);
                if (depto301 == null) depto301 = todos.get(4);
            } else if (!todos.isEmpty()) {
                if (depto101 == null) depto101 = todos.get(0);
                if (depto102 == null) depto102 = todos.get(0);
                if (depto201 == null) depto201 = todos.get(0);
                if (depto202 == null) depto202 = todos.get(0);
                if (depto301 == null) depto301 = todos.get(0);
            }
        }

        // 5 Cuotas (Mezcla de PENDIENTE, PARCIAL y PAGADO)
        // Regla: Toda cuota PAGADA tiene saldo pendiente 0.00 y su correspondiente registro Pago persistido
        cuotaRepo.save(CuotaMantenimiento.builder()
                .montoBase(250.0)
                .montoTotal(250.0)
                .fechaEmision(LocalDate.now().minusDays(5))
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .estado("PENDIENTE")
                .departamento(depto101)
                .build());

        cuotaRepo.save(CuotaMantenimiento.builder()
                .montoBase(180.0)
                .montoTotal(0.0)
                .fechaEmision(LocalDate.now().minusDays(20))
                .fechaVencimiento(LocalDate.now().minusDays(5))
                .estado("PAGADO")
                .departamento(depto102)
                .build());

        if (pagoRepo != null) {
            pagoRepo.save(Pago.builder()
                    .monto(180.0)
                    .fechaPago(LocalDate.now().minusDays(6))
                    .metodoPago("TRANSFERENCIA")
                    .departamento(depto102)
                    .build());
        }

        cuotaRepo.save(CuotaMantenimiento.builder()
                .montoBase(320.0)
                .montoTotal(160.0)
                .fechaEmision(LocalDate.now().minusDays(8))
                .fechaVencimiento(LocalDate.now().plusDays(5))
                .estado("PARCIAL")
                .departamento(depto201)
                .build());

        if (pagoRepo != null) {
            pagoRepo.save(Pago.builder()
                    .monto(160.0)
                    .fechaPago(LocalDate.now().minusDays(2))
                    .metodoPago("EFECTIVO")
                    .departamento(depto201)
                    .build());
        }

        cuotaRepo.save(CuotaMantenimiento.builder()
                .montoBase(280.0)
                .montoTotal(280.0)
                .fechaEmision(LocalDate.now().minusDays(3))
                .fechaVencimiento(LocalDate.now().plusDays(12))
                .estado("PENDIENTE")
                .departamento(depto202)
                .build());

        cuotaRepo.save(CuotaMantenimiento.builder()
                .montoBase(350.0)
                .montoTotal(0.0)
                .fechaEmision(LocalDate.now().minusDays(15))
                .fechaVencimiento(LocalDate.now().minusDays(2))
                .estado("PAGADO")
                .departamento(depto301)
                .build());

        if (pagoRepo != null) {
            pagoRepo.save(Pago.builder()
                    .monto(350.0)
                    .fechaPago(LocalDate.now().minusDays(3))
                    .metodoPago("DEPOSITO")
                    .departamento(depto301)
                    .build());
        }

        log.info("Siembra inicial de cuotas demo completada con 5 registros (PENDIENTE, PARCIAL, PAGADO) y pagos sincronizados.");
    }

    // Siembra inicial para el modulo de inmuebles: 3 edificios, torres y 5 departamentos.
    @Transactional
    public static void sembrarInmueblesSiEstaVacio(
            EdificioRepositorio edificioRepo,
            TorreRepositorio torreRepo,
            DepartamentoRepositorio deptoRepo) {

        if (edificioRepo == null || torreRepo == null || deptoRepo == null) {
            return;
        }

        Edificio edificio1 = null;
        Edificio edificio2 = null;
        Edificio edificio3 = null;

        if (edificioRepo.count() == 0) {
            // 1. Tres Edificios oficiales
            edificio1 = edificioRepo.save(Edificio.builder()
                    .nombre("Edificio Los Sauces")
                    .numeroPisos(10)
                    .build());

            edificio2 = edificioRepo.save(Edificio.builder()
                    .nombre("Edificio Las Palmeras")
                    .numeroPisos(15)
                    .build());

            edificio3 = edificioRepo.save(Edificio.builder()
                    .nombre("Edificio Los Álamos")
                    .numeroPisos(8)
                    .build());
        } else {
            List<Edificio> eds = edificioRepo.findAll();
            if (eds.size() > 0) edificio1 = eds.get(0);
            if (eds.size() > 1) edificio2 = eds.get(1);
            if (eds.size() > 2) edificio3 = eds.get(2);
        }

        Torre torreA = null;
        Torre torreB = null;
        Torre torrePrincipal = null;
        Torre torreCentral = null;

        if (torreRepo.count() == 0) {
            // 2. Torres
            torreA = torreRepo.save(Torre.builder()
                    .nombre("Torre A")
                    .numeroPisos(10)
                    .edificio(edificio1)
                    .build());

            torreB = torreRepo.save(Torre.builder()
                    .nombre("Torre B")
                    .numeroPisos(10)
                    .edificio(edificio1)
                    .build());

            torrePrincipal = torreRepo.save(Torre.builder()
                    .nombre("Torre Principal")
                    .numeroPisos(15)
                    .edificio(edificio2)
                    .build());

            torreCentral = torreRepo.save(Torre.builder()
                    .nombre("Torre Central")
                    .numeroPisos(8)
                    .edificio(edificio3)
                    .build());
        } else {
            List<Torre> torres = torreRepo.findAll();
            for (Torre t : torres) {
                if (t.getEdificio() == null && edificio1 != null) {
                    if (t.getNombre() != null && (t.getNombre().contains("A") || t.getNombre().contains("B"))) {
                        t.setEdificio(edificio1);
                    } else if (t.getNombre() != null && t.getNombre().contains("Principal") && edificio2 != null) {
                        t.setEdificio(edificio2);
                    } else if (edificio3 != null) {
                        t.setEdificio(edificio3);
                    } else {
                        t.setEdificio(edificio1);
                    }
                    torreRepo.save(t);
                }
                if (t.getNombre() != null && t.getNombre().equals("Torre A")) torreA = t;
                else if (t.getNombre() != null && t.getNombre().equals("Torre B")) torreB = t;
                else if (t.getNombre() != null && t.getNombre().contains("Principal")) torrePrincipal = t;
                else if (t.getNombre() != null && t.getNombre().contains("Central")) torreCentral = t;
            }
            if (torreA == null && !torres.isEmpty()) torreA = torres.get(0);
            if (torreB == null && torres.size() > 1) torreB = torres.get(1);
            if (torrePrincipal == null && torres.size() > 2) torrePrincipal = torres.get(2);
            if (torreCentral == null && torres.size() > 3) torreCentral = torres.get(3);
        }

        if (deptoRepo.count() == 0) {
            // 3. Departamentos Iniciales
            deptoRepo.save(Departamento.builder()
                    .numero("101")
                    .piso(1)
                    .metrosCuadrados(75.5)
                    .numeroHabitantes(3)
                    .torre(torreA)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("102")
                    .piso(1)
                    .metrosCuadrados(80.0)
                    .numeroHabitantes(2)
                    .torre(torreA)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("201")
                    .piso(2)
                    .metrosCuadrados(92.0)
                    .numeroHabitantes(4)
                    .torre(torreB)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("202")
                    .piso(2)
                    .metrosCuadrados(85.0)
                    .numeroHabitantes(3)
                    .torre(torrePrincipal)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("301")
                    .piso(3)
                    .metrosCuadrados(110.0)
                    .numeroHabitantes(3)
                    .torre(torreCentral)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("401")
                    .piso(4)
                    .metrosCuadrados(95.0)
                    .numeroHabitantes(3)
                    .torre(torreA)
                    .build());

            deptoRepo.save(Departamento.builder()
                    .numero("402")
                    .piso(4)
                    .metrosCuadrados(68.0)
                    .numeroHabitantes(2)
                    .torre(torreB)
                    .build());
        }

        // Departamentos adicionales para completar todas las torres de forma balanceada
        if (!deptoRepo.findByNumero("103").isPresent()) {
            deptoRepo.save(Departamento.builder()
                    .numero("103")
                    .piso(1)
                    .metrosCuadrados(78.0)
                    .numeroHabitantes(3)
                    .torre(torreB)
                    .build());
        }
        if (!deptoRepo.findByNumero("203").isPresent()) {
            deptoRepo.save(Departamento.builder()
                    .numero("203")
                    .piso(2)
                    .metrosCuadrados(82.0)
                    .numeroHabitantes(2)
                    .torre(torrePrincipal)
                    .build());
        }
        if (!deptoRepo.findByNumero("501").isPresent()) {
            deptoRepo.save(Departamento.builder()
                    .numero("501")
                    .piso(5)
                    .metrosCuadrados(90.0)
                    .numeroHabitantes(3)
                    .torre(torrePrincipal)
                    .build());
        }
        if (!deptoRepo.findByNumero("502").isPresent()) {
            deptoRepo.save(Departamento.builder()
                    .numero("502")
                    .piso(5)
                    .metrosCuadrados(85.0)
                    .numeroHabitantes(2)
                    .torre(torreCentral)
                    .build());
        }

        log.info("Siembra inicial de inmuebles completada: 3 edificios y {} departamentos.", deptoRepo.count());
    }

    // Siembra inicial para el modulo de residentes: 5 propietarios y 3 inquilinos vinculados a departamentos.
    @Transactional
    public static void sembrarPersonasSiEstaVacio(
            PropietarioRepositorio propRepo,
            InquilinoRepositorio inqRepo,
            DepartamentoRepositorio deptoRepo) {

        if (propRepo == null || inqRepo == null) return;

        Departamento d101 = deptoRepo != null ? deptoRepo.findByNumero("101").orElse(null) : null;
        Departamento d102 = deptoRepo != null ? deptoRepo.findByNumero("102").orElse(null) : null;
        Departamento d103 = deptoRepo != null ? deptoRepo.findByNumero("103").orElse(null) : null;
        Departamento d201 = deptoRepo != null ? deptoRepo.findByNumero("201").orElse(null) : null;
        Departamento d202 = deptoRepo != null ? deptoRepo.findByNumero("202").orElse(null) : null;
        Departamento d203 = deptoRepo != null ? deptoRepo.findByNumero("203").orElse(null) : null;
        Departamento d301 = deptoRepo != null ? deptoRepo.findByNumero("301").orElse(null) : null;
        Departamento d401 = deptoRepo != null ? deptoRepo.findByNumero("401").orElse(null) : null;
        Departamento d402 = deptoRepo != null ? deptoRepo.findByNumero("402").orElse(null) : null;
        Departamento d501 = deptoRepo != null ? deptoRepo.findByNumero("501").orElse(null) : null;
        Departamento d502 = deptoRepo != null ? deptoRepo.findByNumero("502").orElse(null) : null;

        // Propietarios (11 residentes distribuidos en todas las torres y abecedario)
        if (!propRepo.existsByDni("10293847")) {
            Propietario prop1 = propRepo.save(Propietario.builder()
                    .dni("10293847")
                    .nombre("Carlos")
                    .apellidos("Mendoza Torres")
                    .telefono("987654321")
                    .correo("carlos.mendoza@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(6))
                    .build());
            if (d101 != null && deptoRepo != null) { d101.setPropietario(prop1); deptoRepo.save(d101); }
        }

        if (!propRepo.existsByDni("84736291")) {
            Propietario prop2 = propRepo.save(Propietario.builder()
                    .dni("84736291")
                    .nombre("Ana")
                    .apellidos("Rosas Del Campo")
                    .telefono("998877665")
                    .correo("ana.rosas@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(4))
                    .build());
            if (d102 != null && deptoRepo != null) { d102.setPropietario(prop2); deptoRepo.save(d102); }
        }

        if (!propRepo.existsByDni("38475629")) {
            Propietario prop3 = propRepo.save(Propietario.builder()
                    .dni("38475629")
                    .nombre("Miguel Ángel")
                    .apellidos("Benítez Vega")
                    .telefono("945612378")
                    .correo("miguel.benitez@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(5))
                    .build());
            if (d201 != null && deptoRepo != null) { d201.setPropietario(prop3); deptoRepo.save(d201); }
        }

        if (!propRepo.existsByDni("71928374")) {
            Propietario prop4 = propRepo.save(Propietario.builder()
                    .dni("71928374")
                    .nombre("Valeria")
                    .apellidos("Soto Morales")
                    .telefono("934567891")
                    .correo("valeria.soto@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(3))
                    .build());
            if (d202 != null && deptoRepo != null) { d202.setPropietario(prop4); deptoRepo.save(d202); }
        }

        if (!propRepo.existsByDni("62839401")) {
            Propietario prop5 = propRepo.save(Propietario.builder()
                    .dni("62839401")
                    .nombre("Roberto")
                    .apellidos("Chang Alvarado")
                    .telefono("976543210")
                    .correo("roberto.chang@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .build());
            if (d301 != null && deptoRepo != null) { d301.setPropietario(prop5); deptoRepo.save(d301); }
        }

        if (!propRepo.existsByDni("75967704")) {
            Propietario prop6 = propRepo.save(Propietario.builder()
                    .dni("75967704")
                    .nombre("Antonieta")
                    .apellidos("Morales Solís")
                    .telefono("981234567")
                    .correo("antonieta.morales@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(4))
                    .build());
            if (d401 != null && deptoRepo != null) { d401.setPropietario(prop6); deptoRepo.save(d401); }
        }

        if (!propRepo.existsByDni("29485716")) {
            Propietario prop7 = propRepo.save(Propietario.builder()
                    .dni("29485716")
                    .nombre("Beatriz")
                    .apellidos("Alarcón Palacios")
                    .telefono("972345678")
                    .correo("beatriz.alarcon@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(3))
                    .build());
            if (d402 != null && deptoRepo != null) { d402.setPropietario(prop7); deptoRepo.save(d402); }
        }

        if (!propRepo.existsByDni("74839201")) {
            Propietario prop8 = propRepo.save(Propietario.builder()
                    .dni("74839201")
                    .nombre("Antonela")
                    .apellidos("Quispe Vargas")
                    .telefono("963456789")
                    .correo("antonela.quispe@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .build());
            if (d103 != null && deptoRepo != null) { d103.setPropietario(prop8); deptoRepo.save(d103); }
        }

        if (!propRepo.existsByDni("71829304")) {
            Propietario prop9 = propRepo.save(Propietario.builder()
                    .dni("71829304")
                    .nombre("Renato")
                    .apellidos("Medina Castro")
                    .telefono("954321678")
                    .correo("renato.medina@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(3))
                    .build());
            if (d203 != null && deptoRepo != null) { d203.setPropietario(prop9); deptoRepo.save(d203); }
        }

        if (!propRepo.existsByDni("47281930")) {
            Propietario prop10 = propRepo.save(Propietario.builder()
                    .dni("47281930")
                    .nombre("Hugo")
                    .apellidos("Cárdenas Silva")
                    .telefono("943218765")
                    .correo("hugo.cardenas@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(4))
                    .build());
            if (d501 != null && deptoRepo != null) { d501.setPropietario(prop10); deptoRepo.save(d501); }
        }

        if (!propRepo.existsByDni("81920384")) {
            Propietario prop11 = propRepo.save(Propietario.builder()
                    .dni("81920384")
                    .nombre("Álvaro")
                    .apellidos("Guzmán Prado")
                    .telefono("932187654")
                    .correo("alvaro.guzman@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(5))
                    .build());
            if (d502 != null && deptoRepo != null) { d502.setPropietario(prop11); deptoRepo.save(d502); }
        }

        // Inquilinos (11 residentes vinculados a departamentos)
        if (!inqRepo.existsByDni("45678912")) {
            Inquilino inq1 = inqRepo.save(Inquilino.builder()
                    .dni("45678912")
                    .nombre("Javier")
                    .apellidos("Paredes Quispe")
                    .telefono("912345678")
                    .correo("javier.paredes@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .fechaInicioContrato(LocalDate.now().minusMonths(2))
                    .fechaFinContrato(LocalDate.now().plusMonths(10))
                    .build());
            if (d101 != null && deptoRepo != null) { d101.setInquilinoActual(inq1); deptoRepo.save(d101); }
        }

        if (!inqRepo.existsByDni("48291038")) {
            Inquilino inq2 = inqRepo.save(Inquilino.builder()
                    .dni("48291038")
                    .nombre("Daniel")
                    .apellidos("Palacios Rivas")
                    .telefono("921345679")
                    .correo("daniel.palacios@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(3))
                    .fechaInicioContrato(LocalDate.now().minusMonths(3))
                    .fechaFinContrato(LocalDate.now().plusMonths(9))
                    .build());
            if (d102 != null && deptoRepo != null) { d102.setInquilinoActual(inq2); deptoRepo.save(d102); }
        }

        if (!inqRepo.existsByDni("56789023")) {
            Inquilino inq3 = inqRepo.save(Inquilino.builder()
                    .dni("56789023")
                    .nombre("Lucía")
                    .apellidos("Fernández Vega")
                    .telefono("923456789")
                    .correo("lucia.fernandez@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(1))
                    .fechaInicioContrato(LocalDate.now().minusMonths(1))
                    .fechaFinContrato(LocalDate.now().plusMonths(11))
                    .build());
            if (d201 != null && deptoRepo != null) { d201.setInquilinoActual(inq3); deptoRepo.save(d201); }
        }

        if (!inqRepo.existsByDni("43928175")) {
            Inquilino inq4 = inqRepo.save(Inquilino.builder()
                    .dni("43928175")
                    .nombre("Gonzalo")
                    .apellidos("Herrera Ríos")
                    .telefono("931456782")
                    .correo("gonzalo.herrera@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .fechaInicioContrato(LocalDate.now().minusMonths(2))
                    .fechaFinContrato(LocalDate.now().plusMonths(10))
                    .build());
            if (d103 != null && deptoRepo != null) { d103.setInquilinoActual(inq4); deptoRepo.save(d103); }
        }

        if (!inqRepo.existsByDni("60192837")) {
            Inquilino inq5 = inqRepo.save(Inquilino.builder()
                    .dni("60192837")
                    .nombre("Gabriela")
                    .apellidos("Zúñiga Paredes")
                    .telefono("941567893")
                    .correo("gabriela.zuniga@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(1))
                    .fechaInicioContrato(LocalDate.now().minusMonths(1))
                    .fechaFinContrato(LocalDate.now().plusMonths(11))
                    .build());
            if (d402 != null && deptoRepo != null) { d402.setInquilinoActual(inq5); deptoRepo.save(d402); }
        }

        if (!inqRepo.existsByDni("51839204")) {
            Inquilino inq6 = inqRepo.save(Inquilino.builder()
                    .dni("51839204")
                    .nombre("Elena")
                    .apellidos("Fuentes Navarro")
                    .telefono("951678904")
                    .correo("elena.fuentes@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .fechaInicioContrato(LocalDate.now().minusMonths(2))
                    .fechaFinContrato(LocalDate.now().plusMonths(10))
                    .build());
            if (d202 != null && deptoRepo != null) { d202.setInquilinoActual(inq6); deptoRepo.save(d202); }
        }

        if (!inqRepo.existsByDni("42819203")) {
            Inquilino inq7 = inqRepo.save(Inquilino.builder()
                    .dni("42819203")
                    .nombre("Sandra")
                    .apellidos("Vega Flores")
                    .telefono("961789015")
                    .correo("sandra.vega@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(3))
                    .fechaInicioContrato(LocalDate.now().minusMonths(3))
                    .fechaFinContrato(LocalDate.now().plusMonths(9))
                    .build());
            if (d203 != null && deptoRepo != null) { d203.setInquilinoActual(inq7); deptoRepo.save(d203); }
        }

        if (!inqRepo.existsByDni("72839415")) {
            Inquilino inq8 = inqRepo.save(Inquilino.builder()
                    .dni("72839415")
                    .nombre("Patricia")
                    .apellidos("Delgado Ramos")
                    .telefono("971890126")
                    .correo("patricia.delgado@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .fechaInicioContrato(LocalDate.now().minusMonths(2))
                    .fechaFinContrato(LocalDate.now().plusMonths(10))
                    .build());
            if (d501 != null && deptoRepo != null) { d501.setInquilinoActual(inq8); deptoRepo.save(d501); }
        }

        if (!inqRepo.existsByDni("67890134")) {
            Inquilino inq9 = inqRepo.save(Inquilino.builder()
                    .dni("67890134")
                    .nombre("Diego")
                    .apellidos("Castro Huamán")
                    .telefono("934567890")
                    .correo("diego.castro@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(1))
                    .fechaInicioContrato(LocalDate.now().minusMonths(1))
                    .fechaFinContrato(LocalDate.now().plusMonths(11))
                    .build());
            if (d301 != null && deptoRepo != null) { d301.setInquilinoActual(inq9); deptoRepo.save(d301); }
        }

        if (!inqRepo.existsByDni("32918475")) {
            Inquilino inq10 = inqRepo.save(Inquilino.builder()
                    .dni("32918475")
                    .nombre("Mónica")
                    .apellidos("Silva Barreto")
                    .telefono("981901237")
                    .correo("monica.silva@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(1))
                    .fechaInicioContrato(LocalDate.now().minusMonths(1))
                    .fechaFinContrato(LocalDate.now().plusMonths(11))
                    .build());
            if (d502 != null && deptoRepo != null) { d502.setInquilinoActual(inq10); deptoRepo.save(d502); }
        }

        if (!inqRepo.existsByDni("39281746")) {
            Inquilino inq11 = inqRepo.save(Inquilino.builder()
                    .dni("39281746")
                    .nombre("Fernando")
                    .apellidos("Gamboa Salazar")
                    .telefono("991012348")
                    .correo("fernando.gamboa@email.com")
                    .fechaRegistro(LocalDate.now().minusMonths(2))
                    .fechaInicioContrato(LocalDate.now().minusMonths(2))
                    .fechaFinContrato(LocalDate.now().plusMonths(10))
                    .build());
            if (d401 != null && deptoRepo != null) { d401.setInquilinoActual(inq11); deptoRepo.save(d401); }
        }

        log.info("Siembra consolidada de residentes completada: {} propietarios y {} inquilinos en el padrón.",
                propRepo.count(), inqRepo.count());
    }

    // Siembra inicial de areas comunes: Zona BBQ, Salon Social y Piscina.
    @Transactional
    public static void sembrarAreasComunesSiEstaVacio(AreaComunRepositorio areaRepo) {
        if (areaRepo == null || areaRepo.count() > 0) return;

        areaRepo.save(AreaComun.builder()
                .nombre("Zona BBQ / Parrillas")
                .capacidad(20)
                .costoReserva(50.0)
                .build());

        areaRepo.save(AreaComun.builder()
                .nombre("Salón Social Multiusos")
                .capacidad(50)
                .costoReserva(120.0)
                .build());

        areaRepo.save(AreaComun.builder()
                .nombre("Piscina Panorámica")
                .capacidad(30)
                .costoReserva(0.0)
                .build());

        log.info("Siembra inicial de áreas comunes completada: 3 instalaciones registradas.");
    }

    // Siembra inicial de gastos comunes utilizando subclases polimorficas de GastoComun.
    @Transactional
    public static void sembrarGastosComunesSiEstaVacio(GastoComunRepositorio gastoRepo) {
        if (gastoRepo == null || gastoRepo.count() > 0) return;

        gastoRepo.save(GastoAgua.builder()
                .descripcion("Facturación SEDAPAL - Red Matriz y Áreas Comunes")
                .monto(450.50)
                .fecha(LocalDate.now().minusDays(15))
                .lecturaAnterior(1200.0)
                .lecturaActual(1350.0)
                .build());

        gastoRepo.save(GastoLuz.builder()
                .descripcion("Recibo Luz del Sur - Pasillos, Ascensores e Iluminación Externa")
                .monto(820.00)
                .fecha(LocalDate.now().minusDays(12))
                .consumoKwh(1450.0)
                .tarifaPorKwh(0.56)
                .build());

        gastoRepo.save(GastoLimpieza.builder()
                .descripcion("Servicio Quincenal Limpieza Integral y Sanitización")
                .monto(1200.00)
                .fecha(LocalDate.now().minusDays(8))
                .contratoEmpresa("Clean Corp Perú SAC")
                .personalAsignado(4)
                .build());

        log.info("Siembra inicial de gastos comunes completada: Agua, Luz y Limpieza.");
    }

    // Siembra inicial de operaciones: reservas, incidentes y comunicados.
    @Transactional
    public static void sembrarOperacionesSiEstaVacio(
            ReservaAreaRepositorio reservaRepo,
            IncidenteRepositorio incRepo,
            ComunicadoRepositorio comRepo,
            PropietarioRepositorio propRepo,
            AreaComunRepositorio areaRepo) {

        if (incRepo != null && incRepo.count() == 0) {
            incRepo.save(Incidente.builder()
                    .reportadoPor("Conserjería")
                    .fecha(LocalDate.now().minusDays(3))
                    .descripcion("Mantenimiento preventivo programado para el ascensor de la Torre A.")
                    .estado("EN_PROCESO")
                    .build());

            incRepo.save(Incidente.builder()
                    .reportadoPor("Residente Depto 101")
                    .fecha(LocalDate.now().minusDays(1))
                    .descripcion("Reemplazo de luminaria LED en el pasillo del piso 2.")
                    .estado("RESUELTO")
                    .build());
        }

        if (comRepo != null && comRepo.count() == 0) {
            comRepo.save(Comunicado.builder()
                    .titulo("Asamblea General Ordinaria de Propietarios")
                    .autor("Junta Directiva")
                    .fechaPublicacion(LocalDate.now().minusDays(2))
                    .mensaje("Se convoca a todos los copropietarios a la asamblea este sábado para la aprobación del presupuesto anual.")
                    .build());

            comRepo.save(Comunicado.builder()
                    .titulo("Mantenimiento y Desinfección de Cisternas")
                    .autor("Administración")
                    .fechaPublicacion(LocalDate.now().minusDays(1))
                    .mensaje("Se realizará la limpieza de tanques de agua el jueves de 08:00 a 14:00 hrs. Tomar previsiones.")
                    .build());
        }

        if (reservaRepo != null && reservaRepo.count() == 0 && propRepo != null && areaRepo != null) {
            List<Propietario> props = propRepo.findAll();
            List<AreaComun> areas = areaRepo.findAll();

            if (props.size() >= 2 && areas.size() >= 2) {
                reservaRepo.save(ReservaArea.builder()
                        .propietario(props.get(0))
                        .areaComun(areas.get(0))
                        .fechaReserva(LocalDate.now().plusDays(2))
                        .horaInicio(LocalTime.of(15, 0))
                        .horaFin(LocalTime.of(19, 0))
                        .estado("CONFIRMADA")
                        .build());

                reservaRepo.save(ReservaArea.builder()
                        .propietario(props.get(1))
                        .areaComun(areas.get(1))
                        .fechaReserva(LocalDate.now().plusDays(4))
                        .horaInicio(LocalTime.of(18, 0))
                        .horaFin(LocalTime.of(22, 0))
                        .estado("CONFIRMADA")
                        .build());
            } else if (!props.isEmpty() && !areas.isEmpty()) {
                reservaRepo.save(ReservaArea.builder()
                        .propietario(props.get(0))
                        .areaComun(areas.get(0))
                        .fechaReserva(LocalDate.now().plusDays(2))
                        .horaInicio(LocalTime.of(15, 0))
                        .horaFin(LocalTime.of(19, 0))
                        .estado("CONFIRMADA")
                        .build());
            }
        }

        log.info("Siembra inicial de operaciones completada: 2 incidentes y 2 reservas.");
    }
}
