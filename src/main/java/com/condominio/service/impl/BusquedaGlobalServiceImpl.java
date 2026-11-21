package com.condominio.service.impl;

import com.condominio.dto.CuotaDTO;
import com.condominio.dto.ResidenteDTO;
import com.condominio.dto.ResultadoBusquedaDTO;
import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Visita;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.service.IBusquedaGlobalService;
import com.condominio.service.ICobranzaService;
import com.condominio.service.IInmuebleService;
import com.condominio.service.IOperacionService;
import com.condominio.service.IPersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Implementacion desacoplada del servicio de busqueda global polimorfica y segmentada por RBAC
@Service
public class BusquedaGlobalServiceImpl implements IBusquedaGlobalService {

    private final ICobranzaService cobranzaService;
    private final IInmuebleService inmuebleService;
    private final IPersonaService personaService;
    private final IOperacionService operacionService;
    @Autowired(required = false)
    private com.condominio.repositorio.VisitaRepositorio visitaRepositorio;

    @Autowired
    public BusquedaGlobalServiceImpl(
            @Autowired(required = false) ICobranzaService cobranzaService,
            @Autowired(required = false) IInmuebleService inmuebleService,
            @Autowired(required = false) IPersonaService personaService,
            @Autowired(required = false) IOperacionService operacionService,
            @Autowired(required = false) com.condominio.repositorio.VisitaRepositorio visitaRepositorio) {
        this.cobranzaService = cobranzaService;
        this.inmuebleService = inmuebleService;
        this.personaService = personaService;
        this.operacionService = operacionService;
        this.visitaRepositorio = visitaRepositorio;
    }

    public BusquedaGlobalServiceImpl(
            ICobranzaService cobranzaService,
            IInmuebleService inmuebleService,
            IPersonaService personaService,
            IOperacionService operacionService) {
        this(cobranzaService, inmuebleService, personaService, operacionService, null);
    }

    public BusquedaGlobalServiceImpl() {
        this(null, null, null, null, null);
    }

    @Override
    public List<ResultadoBusquedaDTO> buscarEnSistema(String termino, RolUsuario rol, String deptoResidente) {
        String query = (termino != null) ? termino.trim().toLowerCase() : "";
        List<ResultadoBusquedaDTO> resultados = new ArrayList<>();

        RolUsuario rolEfectivo = (rol != null) ? rol : SesionActual.getRol();
        boolean esAdmin = (rolEfectivo == RolUsuario.ADMINISTRADOR);
        boolean esConserje = (rolEfectivo == RolUsuario.CONSERJE || rolEfectivo == RolUsuario.GERENTE);
        boolean esResidente = (rolEfectivo == RolUsuario.RESIDENTE || rolEfectivo == RolUsuario.PRESIDENTE);
        String miDepto = (deptoResidente != null && !deptoResidente.isBlank()) ? deptoResidente.trim() : "101";

        buscarComunicados(query, esAdmin, resultados);
        buscarGastosComunes(query, esAdmin, esResidente, resultados);
        buscarCuotas(query, esAdmin, esResidente, miDepto, resultados);
        buscarVisitas(query, esAdmin, esConserje, resultados);
        buscarResidentes(query, esAdmin, esConserje, resultados);
        buscarIncidentes(query, esAdmin, resultados);
        buscarReservas(query, esAdmin, esResidente, resultados);

        return resultados;
    }

    // 1. Comunicados oficiales visibles para todos los roles
    private void buscarComunicados(String query, boolean esAdmin, List<ResultadoBusquedaDTO> resultados) {
        List<Comunicado> comunicados = obtenerComunicados();
        if (comunicados == null) return;

        for (Comunicado c : comunicados) {
            String tit = (c.getTitulo() != null) ? c.getTitulo() : "";
            String msg = (c.getMensaje() != null) ? c.getMensaje() : "";
            String aut = (c.getAutor() != null) ? c.getAutor() : "Administración";
            String fec = (c.getFechaPublicacion() != null) ? c.getFechaPublicacion().toString() : "-";

            if (query.isEmpty() || tit.toLowerCase().contains(query) || msg.toLowerCase().contains(query)
                    || aut.toLowerCase().contains(query) || fec.contains(query)
                    || "aviso".contains(query) || "comunicado".contains(query) || "circular".contains(query)) {

                String detalle = "COMUNICADO OFICIAL DEL CONDOMINIO\n\n"
                        + "Título: " + tit + "\n"
                        + "Fecha: " + fec + " | Autor: " + aut + "\n\n"
                        + "Contenido del Comunicado:\n" + msg;

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("AVISO-" + c.getId())
                        .categoria("AVISO / COMUNICADO")
                        .titulo(tit)
                        .subtitulo("Publicado: " + fec + " | Por: " + aut)
                        .fecha(fec)
                        .estado("PUBLICADO")
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_OPERACIONES" : null)
                        .build());
            }
        }
    }

    // 2. Gastos comunes visibles para Administrador y Residente
    private void buscarGastosComunes(String query, boolean esAdmin, boolean esResidente, List<ResultadoBusquedaDTO> resultados) {
        if (!esAdmin && !esResidente) return;

        List<GastoComun> gastos = obtenerGastosComunes();
        if (gastos == null) return;

        for (GastoComun g : gastos) {
            String desc = (g.getDescripcion() != null) ? g.getDescripcion() : "";
            String tipo = "GENERAL";
            if (g.getClass().getSimpleName().contains("Agua")) tipo = "AGUA";
            else if (g.getClass().getSimpleName().contains("Luz")) tipo = "LUZ";
            else if (g.getClass().getSimpleName().contains("Limpieza")) tipo = "LIMPIEZA";

            String fec = (g.getFecha() != null) ? g.getFecha().toString() : "-";
            double monto = (g.getMonto() != null) ? g.getMonto() : 0.0;
            String montoStr = String.format("S/ %.2f", monto);

            if (query.isEmpty() || desc.toLowerCase().contains(query) || tipo.toLowerCase().contains(query)
                    || fec.contains(query) || montoStr.toLowerCase().contains(query)
                    || "gasto".contains(query) || "servicio".contains(query) || "monto".contains(query) || "recibo".contains(query)) {

                String detalle = "DETALLE DE GASTO COMÚN DE LA COMUNIDAD\n\n"
                        + "Concepto: " + desc + "\n"
                        + "Categoría: " + tipo + "\n"
                        + "Monto Total Facturado: " + montoStr + "\n"
                        + "Fecha de Emisión: " + fec + "\n\n"
                        + (esResidente ? "Información de transparencia comunal para residentes." : "Registro contable de egreso administrativo.");

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("GASTO-" + g.getId())
                        .categoria(esResidente ? "GASTO DE LA COMUNIDAD" : "GASTO COMÚN")
                        .titulo(desc)
                        .subtitulo(montoStr + " | Categoría: " + tipo + " | Fecha: " + fec)
                        .fecha(fec)
                        .estado("FACTURADO")
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_COBRANZAS" : null)
                        .build());
            }
        }
    }

    // 3. Cuotas y pagos filtradas segun rol y departamento propio
    private void buscarCuotas(String query, boolean esAdmin, boolean esResidente, String miDepto, List<ResultadoBusquedaDTO> resultados) {
        if (!esAdmin && !esResidente) return;

        List<CuotaDTO> cuotas = obtenerCuotas();
        if (cuotas == null) return;

        for (CuotaDTO c : cuotas) {
            String deptoNum = (c.getNumeroDepartamento() != null) ? c.getNumeroDepartamento().trim() : "";
            if (esResidente && !deptoNum.equalsIgnoreCase(miDepto)) {
                continue;
            }

            String idStr = (c.getIdCuota() != null) ? c.getIdCuota().toString() : "";
            String estado = (c.getEstado() != null) ? c.getEstado() : "PENDIENTE";
            double monto = (c.getMontoBase() != null) ? c.getMontoBase() : 0.0;
            String montoStr = String.format("S/ %.2f", monto);
            double saldo = "PAGADO".equalsIgnoreCase(estado) ? 0.0 : (c.getMontoFinal() != null ? c.getMontoFinal() : monto);
            String saldoStr = String.format("S/ %.2f", saldo);
            String fecVenc = (c.getFechaVencimiento() != null) ? c.getFechaVencimiento() : "-";
            String per = fecVenc;

            if (query.isEmpty() || deptoNum.toLowerCase().contains(query) || estado.toLowerCase().contains(query)
                    || idStr.contains(query) || per.toLowerCase().contains(query) || fecVenc.contains(query)
                    || "cuota".contains(query) || "pago".contains(query) || "deuda".contains(query) || "mora".contains(query)) {

                String detalle = "ESTADO DE CUOTA DEPARTAMENTO " + deptoNum + "\n\n"
                        + "Cuota #" + idStr + " | Vencimiento: " + per + "\n"
                        + "Monto Base: " + montoStr + " | Saldo Pendiente: " + saldoStr + "\n"
                        + "Fecha de Vencimiento: " + fecVenc + "\n"
                        + "Estado Actual: " + estado + "\n\n"
                        + (esResidente ? "Estado de cuenta asignado a su vivienda." : "Registro de cobranza del condominio.");

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("CUOTA-" + idStr)
                        .categoria(esResidente ? "MI CUOTA (DEPTO " + miDepto + ")" : "CUOTA DEPTO " + deptoNum)
                        .titulo("Cuota Depto " + deptoNum + " (" + per + ")")
                        .subtitulo(montoStr + " | Saldo: " + saldoStr + " | Estado: " + estado + " | Vence: " + fecVenc)
                        .fecha(fecVenc)
                        .estado(estado)
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_COBRANZAS" : null)
                        .build());
            }
        }
    }

    // 4. Control de visitas en porteria visible para Administrador y Conserje
    private void buscarVisitas(String query, boolean esAdmin, boolean esConserje, List<ResultadoBusquedaDTO> resultados) {
        if (!esAdmin && !esConserje) return;

        List<Visita> visitas = obtenerVisitas();
        if (visitas == null) return;

        for (Visita v : visitas) {
            String nom = (v.getNombre() != null) ? v.getNombre() : "";
            String dni = (v.getDni() != null) ? v.getDni() : "";
            String mot = (v.getMotivo() != null) ? v.getMotivo() : "";
            String depto = (v.getDepartamento() != null && v.getDepartamento().getNumero() != null) ? v.getDepartamento().getNumero() : "General";
            String estado = (v.getFechaSalida() == null) ? "EN EDIFICIO" : "RETIRADO";
            String fecIng = (v.getFechaIngreso() != null) ? v.getFechaIngreso().toString() : "-";

            if (query.isEmpty() || nom.toLowerCase().contains(query) || dni.contains(query)
                    || mot.toLowerCase().contains(query) || depto.toLowerCase().contains(query)
                    || estado.toLowerCase().contains(query) || "visita".contains(query) || "garita".contains(query) || "porteria".contains(query)) {

                String detalle = "REGISTRO DE ACCESO EN PORTERÍA\n\n"
                        + "Visitante: " + nom + " | DNI: " + dni + "\n"
                        + "Departamento Destino: " + depto + "\n"
                        + "Motivo de Ingreso: " + mot + "\n"
                        + "Fecha y Hora Ingreso: " + fecIng + "\n"
                        + "Estado en Garita: " + estado + "\n\n"
                        + "Bitácora oficial de control de accesos.";

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("VISITA-" + v.getId())
                        .categoria("VISITA EN GARITA")
                        .titulo(nom + " (Destino: Depto " + depto + ")")
                        .subtitulo("DNI: " + dni + " | Motivo: " + mot + " | Estado: " + estado)
                        .fecha(fecIng)
                        .estado(estado)
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_OPERACIONES" : "CARD_VISITAS")
                        .build());
            }
        }
    }

    // 5. Directorio de residentes segmentado segun privilegios
    private void buscarResidentes(String query, boolean esAdmin, boolean esConserje, List<ResultadoBusquedaDTO> resultados) {
        if (!esAdmin && !esConserje) return;

        List<ResidenteDTO> residentes = obtenerResidentes();
        if (residentes == null) return;

        for (ResidenteDTO r : residentes) {
            String nom = (r.getNombreCompleto() != null) ? r.getNombreCompleto() : "";
            String dni = (r.getDni() != null) ? r.getDni() : "";
            String tel = (r.getTelefono() != null) ? r.getTelefono() : "-";
            String depto = (r.getNumeroDepartamento() != null) ? r.getNumeroDepartamento() : "Sin Depto";
            String torre = (r.getNombreTorre() != null) ? r.getNombreTorre() : "Torre A";
            String tipo = (r.getTipoUsuario() != null) ? r.getTipoUsuario() : "RESIDENTE";

            if (query.isEmpty() || nom.toLowerCase().contains(query) || dni.contains(query)
                    || tel.contains(query) || depto.toLowerCase().contains(query) || torre.toLowerCase().contains(query)
                    || "residente".contains(query) || "telefono".contains(query) || "contacto".contains(query)) {

                String detalle = esConserje
                        ? "DIRECTORIO TELEFÓNICO DE PORTERÍA (EMERGENCIAS Y PAQUETERÍA)\n\n"
                                + "Vecino: " + nom + "\n"
                                + "Ubicación: " + torre + " - Depto " + depto + "\n"
                                + "Teléfono de Contacto: " + tel + "\n"
                                + "DNI: " + dni + "\n\n"
                                + "(Uso exclusivo de garita para comunicación directa)."
                        : "PADRÓN ADMINISTRATIVO DE RESIDENTES\n\n"
                                + "ID: " + r.getId() + " | DNI: " + dni + "\n"
                                + "Nombre: " + nom + " | Condición: " + tipo + "\n"
                                + "Ubicación: " + torre + " - Depto " + depto + "\n"
                                + "Teléfono: " + tel + "\n"
                                + "Correo Electrónico: " + (r.getCorreoElectronico() != null ? r.getCorreoElectronico() : "-") + "\n\n"
                                + "Padrón institucional del condominio.";

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("RES-" + r.getId())
                        .categoria(esConserje ? "CONTACTO PORTERÍA" : "PADRÓN RESIDENTE")
                        .titulo(nom + " (" + torre + " - " + depto + ")")
                        .subtitulo("DNI: " + dni + " | Teléfono: " + tel + " | Tipo: " + tipo)
                        .fecha("-")
                        .estado(tipo)
                        .detalleCompleto(detalle)
                        .moduloDestino("CARD_RESIDENTES")
                        .build());
            }
        }
    }

    // 6. Incidentes operativos
    private void buscarIncidentes(String query, boolean esAdmin, List<ResultadoBusquedaDTO> resultados) {
        List<Incidente> incidentes = obtenerIncidentes();
        if (incidentes == null) return;

        for (Incidente inc : incidentes) {
            String desc = (inc.getDescripcion() != null) ? inc.getDescripcion() : "";
            String est = (inc.getEstado() != null) ? inc.getEstado() : "ABIERTO";
            String fec = (inc.getFecha() != null) ? inc.getFecha().toString() : "-";
            String rep = (inc.getReportadoPor() != null) ? inc.getReportadoPor() : "Conserjería";
            String prio = "OPERATIVO";

            if (query.isEmpty() || desc.toLowerCase().contains(query) || est.toLowerCase().contains(query)
                    || fec.contains(query) || rep.toLowerCase().contains(query) || prio.toLowerCase().contains(query)
                    || "incidente".contains(query) || "averia".contains(query) || "daño".contains(query)) {

                String detalle = "REPORTE DE INCIDENCIA OPERATIVA\n\n"
                        + "Incidente #INC-" + inc.getId() + "\n"
                        + "Descripción: " + desc + "\n"
                        + "Prioridad: " + prio + " | Estado: " + est + "\n"
                        + "Reportado por: " + rep + " | Fecha: " + fec + "\n\n"
                        + "Bitácora técnica de mantenimiento.";

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("INC-" + inc.getId())
                        .categoria("INCIDENCIA")
                        .titulo(desc)
                        .subtitulo("Prioridad: " + prio + " | Estado: " + est + " | Fecha: " + fec)
                        .fecha(fec)
                        .estado(est)
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_OPERACIONES" : null)
                        .build());
            }
        }
    }

    // 7. Reservas de areas comunes
    private void buscarReservas(String query, boolean esAdmin, boolean esResidente, List<ResultadoBusquedaDTO> resultados) {
        if (!esAdmin && !esResidente) return;

        List<ReservaArea> reservas = obtenerReservas();
        if (reservas == null) return;

        for (ReservaArea res : reservas) {
            String nomArea = (res.getAreaComun() != null && res.getAreaComun().getNombre() != null)
                    ? res.getAreaComun().getNombre() : "Área Común";
            String prop = "Vecino";
            if (res.getPropietario() != null) {
                String n = (res.getPropietario().getNombre() != null) ? res.getPropietario().getNombre() : "";
                String a = (res.getPropietario().getApellidos() != null) ? res.getPropietario().getApellidos() : "";
                prop = (n + " " + a).trim();
                if (prop.isEmpty()) prop = "Vecino";
            }
            String fec = (res.getFechaReserva() != null) ? res.getFechaReserva().toString() : "-";
            String est = (res.getEstado() != null) ? res.getEstado() : "CONFIRMADA";
            String horario = (res.getHoraInicio() != null ? res.getHoraInicio() : "10:00") + " a " + (res.getHoraFin() != null ? res.getHoraFin() : "14:00");

            if (query.isEmpty() || nomArea.toLowerCase().contains(query) || prop.toLowerCase().contains(query)
                    || fec.contains(query) || est.toLowerCase().contains(query)
                    || "reserva".contains(query) || "social".contains(query) || "piscina".contains(query) || "bbq".contains(query)) {

                String detalle = "RESERVA DE INSTALACIÓN COMÚN\n\n"
                        + "Reserva #RES-" + res.getId() + " | Área: " + nomArea + "\n"
                        + "Fecha: " + fec + " | Horario: " + horario + "\n"
                        + "Estado: " + est + "\n"
                        + "Titular: " + prop + "\n\n"
                        + (esResidente ? "Detalle de su turno programado." : "Control de ocupación de zonas comunes.");

                resultados.add(ResultadoBusquedaDTO.builder()
                        .id("RES-" + res.getId())
                        .categoria(esResidente ? "MI RESERVA" : "RESERVA ÁREA")
                        .titulo("Reserva " + nomArea + " (" + fec + ")")
                        .subtitulo("Horario: " + horario + " | Estado: " + est)
                        .fecha(fec)
                        .estado(est)
                        .detalleCompleto(detalle)
                        .moduloDestino(esAdmin ? "CARD_OPERACIONES" : null)
                        .build());
            }
        }
    }

    // Metodos auxiliares de obtencion con fallback de demostracion
    private List<Comunicado> obtenerComunicados() {
        if (operacionService != null) {
            List<Comunicado> lista = operacionService.listarComunicados();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        List<Comunicado> demo = new ArrayList<>();
        demo.add(Comunicado.builder().id(1L).titulo("Convocatoria a Asamblea General Ordinaria").mensaje("Se convoca a todos los propietarios a la Asamblea Ordinaria en el Salón Social.").autor("Junta Directiva").fechaPublicacion(LocalDate.now().minusDays(2)).build());
        demo.add(Comunicado.builder().id(2L).titulo("Mantenimiento Programado de Cisterna").mensaje("Limpieza semestral de cisterna principal y bombas de presión.").autor("Administración").fechaPublicacion(LocalDate.now().minusDays(5)).build());
        demo.add(Comunicado.builder().id(3L).titulo("Uso Responsable de Zonas Comunes").mensaje("Recordatorio sobre normas de convivencia y reserva de parrillas.").autor("Conserjería").fechaPublicacion(LocalDate.now().minusDays(8)).build());
        return demo;
    }

    private List<GastoComun> obtenerGastosComunes() {
        if (cobranzaService != null) {
            List<GastoComun> lista = cobranzaService.listarGastosComunes();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        List<GastoComun> demo = new ArrayList<>();
        demo.add(GastoAgua.builder().id(1L).descripcion("Consumo de Agua Potable Cisterna y Torres").monto(1250.0).fecha(LocalDate.now().minusDays(10)).lecturaActual(450.0).lecturaAnterior(380.0).build());
        demo.add(GastoLuz.builder().id(2L).descripcion("Energía Eléctrica Áreas Comunes y Ascensores").monto(980.0).fecha(LocalDate.now().minusDays(12)).build());
        demo.add(GastoLimpieza.builder().id(3L).descripcion("Servicio de Limpieza Integral y Desinfección").monto(800.0).fecha(LocalDate.now().minusDays(15)).build());
        return demo;
    }

    private List<CuotaDTO> obtenerCuotas() {
        if (cobranzaService != null) {
            List<CuotaDTO> lista = cobranzaService.listarCuotas();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        List<CuotaDTO> demo = new ArrayList<>();
        demo.add(CuotaDTO.builder().idCuota(1L).numeroDepartamento("101").montoBase(250.0).montoFinal(250.0).estado("PENDIENTE").fechaVencimiento(LocalDate.now().plusDays(10).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(2L).numeroDepartamento("102").montoBase(180.0).montoFinal(180.0).estado("PAGADO").fechaVencimiento(LocalDate.now().minusDays(5).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(3L).numeroDepartamento("201").montoBase(320.0).montoFinal(160.0).estado("PARCIAL").fechaVencimiento(LocalDate.now().plusDays(5).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(4L).numeroDepartamento("202").montoBase(280.0).montoFinal(280.0).estado("PENDIENTE").fechaVencimiento(LocalDate.now().plusDays(12).toString()).build());
        demo.add(CuotaDTO.builder().idCuota(5L).numeroDepartamento("301").montoBase(350.0).montoFinal(350.0).estado("PAGADO").fechaVencimiento(LocalDate.now().minusDays(2).toString()).build());
        return demo;
    }

    private List<Visita> obtenerVisitas() {
        if (visitaRepositorio != null) {
            try {
                List<Visita> visitas = visitaRepositorio.findAllConRelaciones();
                if (visitas != null && !visitas.isEmpty()) return visitas;
            } catch (Exception ignored) {}
        }
        List<Visita> demo = new ArrayList<>();
        demo.add(Visita.builder().id(1L).nombre("Marcos Peña Ramos").dni("44123890").motivo("Visita Familiar").fechaIngreso(java.time.LocalDateTime.now().minusHours(2)).build());
        demo.add(Visita.builder().id(2L).nombre("Sonia Morales Dávila").dni("71298453").motivo("Entrega de Documentos").fechaIngreso(java.time.LocalDateTime.now().minusHours(4)).fechaSalida(java.time.LocalDateTime.now().minusHours(2)).build());
        return demo;
    }

    private List<ResidenteDTO> obtenerResidentes() {
        if (personaService != null) {
            List<ResidenteDTO> lista = personaService.listarResidentes();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        List<ResidenteDTO> demo = new ArrayList<>();
        demo.add(ResidenteDTO.builder().id(1L).dni("10293847").nombres("Carlos").apellidos("Mendoza Torres").telefono("987654321").correoElectronico("carlos.mendoza@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("101").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(2L).dni("84736291").nombres("Ana").apellidos("Rosas Del Campo").telefono("998877665").correoElectronico("ana.rosas@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("102").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(3L).dni("38475629").nombres("Miguel Ángel").apellidos("Benítez Vega").telefono("945612378").correoElectronico("miguel.benitez@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("201").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(4L).dni("62839401").nombres("Roberto").apellidos("Chang Alvarado").telefono("976543210").correoElectronico("roberto.chang@email.com").tipoUsuario("PROPIETARIO").numeroDepartamento("301").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(5L).dni("45678912").nombres("Javier").apellidos("Paredes Quispe").telefono("912345678").correoElectronico("javier.paredes@email.com").tipoUsuario("INQUILINO").numeroDepartamento("101").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(6L).dni("56789023").nombres("Lucía").apellidos("Fernández Vega").telefono("923456789").correoElectronico("lucia.fernandez@email.com").tipoUsuario("INQUILINO").numeroDepartamento("201").nombreTorre("Torre A").build());
        demo.add(ResidenteDTO.builder().id(7L).dni("67890134").nombres("Diego").apellidos("Castro Huamán").telefono("934567890").correoElectronico("diego.castro@email.com").tipoUsuario("INQUILINO").numeroDepartamento("301").nombreTorre("Torre A").build());
        return demo;
    }

    private List<Incidente> obtenerIncidentes() {
        if (operacionService != null) {
            List<Incidente> lista = operacionService.listarIncidentes();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        return Collections.emptyList();
    }

    private List<ReservaArea> obtenerReservas() {
        if (operacionService != null) {
            List<ReservaArea> lista = operacionService.listarReservas();
            if (lista != null && !lista.isEmpty()) return lista;
        }
        return Collections.emptyList();
    }
}
