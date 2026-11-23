package com.condominio.service.chatbot;

import com.condominio.dto.CuotaDTO;
import com.condominio.dto.InmuebleDTO;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Estrategia para el perfil Administrador: auditoria profunda, exportacion formal a PDF/Excel,
// comandos CRUD y motor de dialogo conversacional interactivo.
@Slf4j
public class ChatbotAdminStrategy implements IEstrategiaChatbot {

    @Override
    public RolUsuario getRolObjetivo() {
        return RolUsuario.ADMINISTRADOR;
    }

    @Override
    public String generarMensajeBienvenida(String nombreUsuario) {
        return "Bienvenido Administrador (" + (nombreUsuario != null ? nombreUsuario : "admin") + "). " +
                "El módulo de supervisión integral está listo. Puede auditar costos, emitir vouchers de pago, " +
                "exportar reportes a PDF o Excel, o preguntarme cualquier duda sobre cómo manejar el sistema.";
    }

    @Override
    public List<String> obtenerSugerenciasIniciales() {
        return Arrays.asList(
                "Auditar recaudación y morosidad",
                "Ayúdame a manejarlo",
                "Generar voucher de pago Depto 101",
                "Exportar ingresos y gastos a PDF",
                "Exportar padrón de pagos a Excel",
                "Iniciar modo instructivo"
        );
    }

    @Override
    public RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto) {
        String msgOriginal = (mensaje != null) ? mensaje.trim() : "";
        String norm = AsistenteConversacionalUtil.normalizar(msgOriginal);
        CobranzaFachada fachada = (contexto != null) ? contexto.getFachada() : null;

        // 1. Solicitudes de Tutorial Guiado / Modo Instructivo (o confirmación afirmativa para iniciar)
        if (AsistenteConversacionalUtil.esSolicitudTutorial(norm) ||
                (AsistenteConversacionalUtil.esConfirmacionAfirmativa(norm) && (norm.contains("tutorial") || norm.contains("instructivo") || norm.equals("si") || norm.equals("dale")))) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Iniciando Modo Instructivo interactivo para Administrador. Resaltaremos en pantalla cada módulo y sus capacidades operativas.")
                    .tipo(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT)
                    .comandoEjecutado("TUTORIAL_SPOTLIGHT")
                    .reproducirAudio(true)
                    .build();
        }

        // 2. Ayuda de Manejo, Inducción y Orientación ("no sé manejar", "ayúdame a manejarlo", etc.)
        if (AsistenteConversacionalUtil.esAyudaManejo(norm)) {
            return AsistenteConversacionalUtil.responderAyudaManejo(RolUsuario.ADMINISTRADOR);
        }

        // 3. Saludos y Cortesía social ("hola", "que tal", "buenos días")
        if (AsistenteConversacionalUtil.esSaludo(norm) && !norm.contains("pago") && !norm.contains("cuota") && !norm.contains("voucher")) {
            return AsistenteConversacionalUtil.responderSaludo(RolUsuario.ADMINISTRADOR, "Administrador");
        }

        // 4. Identidad y Capacidades del Asistente
        if (AsistenteConversacionalUtil.esIdentidad(norm)) {
            return AsistenteConversacionalUtil.responderIdentidad(RolUsuario.ADMINISTRADOR);
        }

        // 5. Cortesía y Agradecimiento
        if (AsistenteConversacionalUtil.esCortesiaOAgradecimiento(norm)) {
            return AsistenteConversacionalUtil.responderCortesia();
        }

        // 6. Despedida
        if (AsistenteConversacionalUtil.esDespedida(norm)) {
            return AsistenteConversacionalUtil.responderDespedida();
        }

        // 7. Preguntas de Dominio Frecuentes:
        // A) ¿Cómo registrar un pago?
        if (AsistenteConversacionalUtil.esPreguntaComoRegistrarPago(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Para registrar un pago de cuota:\n\n" +
                            "1. Directo desde el chat: Escribe 'registrar pago depto [número] monto [monto]' (ejemplo: 'registrar pago depto 101 monto 200').\n" +
                            "2. Desde la interfaz gráfica: Ve a la barra lateral izquierda en 'Finanzas' -> 'Registro de Pagos', selecciona el departamento, la cuota pendiente, el método de pago y haz clic en 'Confirmar Pago'.\n" +
                            "3. Luego de registrarlo puedes pedirme: 'generar voucher Depto 101' para emitir su recibo en PDF.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Registrar pago depto 101 monto 200", "Generar voucher Depto 101", "Auditar recaudación y morosidad"))
                    .build();
        }

        // B) ¿Cómo emitir una cuota?
        if (AsistenteConversacionalUtil.esPreguntaComoEmitirCuota(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Para emitir una cuota ordinaria o extraordinaria:\n\n" +
                            "1. Desde el chat: Escribe 'emitir cuota [número] monto [monto]' (ejemplo: 'emitir cuota 102 monto 250').\n" +
                            "2. Desde la interfaz: En el menú lateral 'Finanzas' -> 'Generación de Cuotas', define el periodo, monto base, fecha de vencimiento y genera las cuotas masivas para toda la torre o para un departamento específico.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Emitir cuota 102 monto 200", "Auditar recaudación y morosidad", "Exportar padrón a Excel"))
                    .build();
        }

        // C) Morosidad, intereses y penalidades
        if (AsistenteConversacionalUtil.esPreguntaMorosidad(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Gestión de Morosidad y Deudas del Condominio:\n\n" +
                            "1. Vencimiento Automático: Cuando una cuota sobrepasa su fecha de vencimiento sin ser cancelada, el sistema calcula un recargo porcentual por mora y cambia el estado a 'MOROSO'.\n" +
                            "2. Restricciones Operativas: Los residentes con mora quedan automáticamente inhabilitados para reservar áreas comunes (parrillas, piscina, sala de eventos).\n" +
                            "3. Seguimiento: Puedes revisar el monto total pendiente diciendo 'auditar morosidad' o descargar la lista oficial con 'exportar reporte a PDF'.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Auditar recaudación y morosidad", "Exportar reporte a PDF", "Ver departamentos"))
                    .build();
        }

        // D) Gastos comunes y presupuesto
        if (AsistenteConversacionalUtil.esPreguntaGastosYPresupuesto(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Gastos Comunes y Presupuesto Operativo:\n\n" +
                            "- Los gastos comunes corresponden a los egresos del condominio: energía eléctrica de áreas comunes, agua de bombas/cisterna, seguridad 24/7, personal de limpieza y mantenimiento de ascensores.\n" +
                            "- Se distribuyen equitativamente o por alícuota entre los departamentos para formar la cuota de mantenimiento mensual.\n" +
                            "- Puedes examinar el detalle de los egresos registrados diciendo 'examinar costos de servicios comunes' o 'exportar ingresos y gastos a PDF'.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Examinar costos de servicios comunes", "Auditar recaudación y morosidad", "Exportar ingresos y gastos a PDF"))
                    .build();
        }

        // E) Módulos y navegación del sistema
        if (AsistenteConversacionalUtil.esPreguntaModulosSistema(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Estructura de Módulos del Sistema Cobranza Vecinal:\n\n" +
                            "- Dashboard: Visión global de ingresos, cuotas pagadas y estado del condominio.\n" +
                            "- Finanzas: Emisión de cuotas, cobro de mantenimiento, mora, vouchers PDF y conciliación.\n" +
                            "- Inmuebles: Padrón de torres, departamentos, metros cuadrados, propietarios e inquilinos.\n" +
                            "- Operaciones: Bitácora de incidencias físicas, garita de portería y turnos de áreas comunes.\n" +
                            "- Reportes: Generación de balances en PDF y planillas Excel.\n\n" +
                            "Puedes solicitar 'iniciar modo instructivo' para que te resalte cada uno en vivo.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Iniciar modo instructivo", "Auditar recaudación", "Exportar padrón de pagos a Excel"))
                    .build();
        }

        // F) Cómo exportar reportes
        if (AsistenteConversacionalUtil.esPreguntaComoExportar(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Generación y Descarga de Reportes:\n\n" +
                            "1. Formato PDF: Puedes solicitar 'exportar reporte a PDF' (para el balance financiero) o 'generar voucher Depto 101' (para un recibo específico).\n" +
                            "2. Formato Excel: Solicita 'exportar padrón a Excel' para descargar la base de datos completa de cuotas y cobros.\n" +
                            "3. Ubicación: Los archivos se guardan en la carpeta oficial ./exportaciones y el chat te mostrará un botón directo para abrirlos.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Exportar reporte a PDF", "Exportar padrón de pagos a Excel", "Generar voucher Depto 101"))
                    .build();
        }

        // G) Modo Voz y Atajos
        if (AsistenteConversacionalUtil.esPreguntaVoz(norm)) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Funcionalidad de Voz y Atajos:\n\n" +
                            "- Para encender la voz di 'activar voz' o pulsa el botón 'Voz: OFF' en la cabecera. El asistente hablará en español sintetizado.\n" +
                            "- Para apagarla di 'desactivar voz'.\n" +
                            "- Para vaciar el historial di 'limpiar chat' o pulsa el botón 'Limpiar Chat'.\n" +
                            "- Presiona Enter en el teclado para enviar consultas rápidamente.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Activar voz", "Limpiar chat", "Iniciar modo instructivo"))
                    .build();
        }

        // 8. Exportación de Vouchers de Pago en PDF
        if ((norm.contains("voucher") || norm.contains("comprobante") || norm.contains("recibo")) &&
                (norm.contains("pdf") || norm.contains("generar") || norm.contains("descargar") || norm.contains("exportar") || norm.contains("emitir") || norm.contains("depto") || norm.contains("101"))) {
            return procesarGeneracionVoucher(norm, contexto);
        }

        // 9. Exportación general y generación de Informes a PDF en tiempo real con conexión a la Base de Datos
        if (MotorIALocalCondominio.esPeticionInformePdf(norm) || norm.contains("pdf") || norm.contains("reporte pdf") || norm.contains("exportar pdf") || norm.contains("resumen ingresos")) {
            return MotorIALocalCondominio.generarInformePdfEnTiempoReal(norm, msgOriginal, contexto);
        }

        // 10. Exportación de datos y planillas a Microsoft Excel en tiempo real con conexión a la Base de Datos
        if (MotorIALocalCondominio.esPeticionExcel(norm) || norm.contains("excel") || norm.contains("csv") || norm.contains("planilla") || norm.contains("exportar excel")) {
            return MotorIALocalCondominio.generarExcelEnTiempoReal(norm, msgOriginal, contexto);
        }

        // 11. Comandos CRUD conversacionales
        if (norm.startsWith("emitir cuota") || norm.startsWith("generar cuota") || norm.startsWith("crear cuota")) {
            return procesarEmitirCuota(msgOriginal, fachada);
        }
        if (norm.startsWith("registrar pago") || norm.startsWith("pagar cuota")) {
            return procesarRegistrarPago(msgOriginal, fachada);
        }
        if (norm.startsWith("registrar incidente") || norm.startsWith("reportar averia")) {
            return procesarRegistrarIncidente(msgOriginal, fachada);
        }

        // 12. Auditoría Profunda de Costos, Gastos, Recibos y Balances
        if (norm.contains("auditar") || norm.contains("costo") || norm.contains("gasto") || norm.contains("morosidad") ||
                norm.contains("recaudacion") || norm.contains("balance") || norm.contains("resumen financiero") || norm.contains("estado financiero")) {
            return procesarAuditoriaCostos(fachada);
        }

        // 13. Padrón inmobiliario y residentes
        if (norm.contains("inmueble") || norm.contains("departamento") || norm.contains("torre") || norm.contains("residente") || norm.contains("padron")) {
            return procesarConsultaInmuebles(fachada);
        }

        // 14. Motor IA Local Conectado a BD en Tiempo Real: responde cualquier duda técnica, financiera, legal o de dominio
        return MotorIALocalCondominio.responderConsultaInteligente(norm, msgOriginal, RolUsuario.ADMINISTRADOR, deptoUsuario, contexto);
    }

    private RespuestaChatbotDTO procesarGeneracionVoucher(String msg, ContextoChatbot contexto) {
        if (contexto == null || contexto.getReportePdfService() == null) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("El servicio de generación de reportes PDF no está disponible.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }

        String depto = extraerNumeroDepartamento(msg, "101");
        double monto = extraerMonto(msg, 200.0);

        try {
            File pdfVoucher = contexto.getReportePdfService().generarVoucherPago(
                    null, depto, monto, "Transferencia Bancaria BCP", LocalDate.now()
            );

            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Voucher oficial emitido con éxito para el Departamento " + depto +
                            " por el monto de S/ " + String.format("%.2f", monto) +
                            ". El archivo PDF ha sido guardado en la carpeta oficial de reportes y biblioteca contable.")
                    .tipo(TipoRespuestaChatbot.REPORTE_PDF)
                    .archivoGenerado(pdfVoucher)
                    .comandoEjecutado("GENERAR_VOUCHER_PDF")
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Auditar recaudación y morosidad", "Exportar reporte a PDF", "Generar voucher Depto 102"))
                    .build();
        } catch (Exception ex) {
            log.error("Error al generar voucher PDF: {}", ex.getMessage());
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Error al emitir voucher PDF: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private RespuestaChatbotDTO procesarReportePdf(String msg, ContextoChatbot contexto) {
        if (contexto == null || contexto.getReportePdfService() == null) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Servicio de exportación PDF no inicializado.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }

        try {
            File archivo;
            String descripcion;

            if (msg.contains("pendiente") || msg.contains("morosidad") || msg.contains("deuda")) {
                archivo = contexto.getReportePdfService().generarCuotasPendientesEdificio();
                descripcion = "Reporte analítico de cuotas pendientes y morosidad del condominio generado en PDF.";
            } else if (msg.contains("incidente") || msg.contains("reserva") || msg.contains("area")) {
                archivo = contexto.getReportePdfService().generarIncidentesYReservas();
                descripcion = "Reporte de incidentes y reservas comunitarias generado en PDF.";
            } else if (msg.contains("individual") || msg.contains("vecino") || msg.contains("depto")) {
                String depto = extraerNumeroDepartamento(msg, "101");
                archivo = contexto.getReportePdfService().generarEstadoCuentaPropietario(depto);
                descripcion = "Estado de cuenta formal del Departamento " + depto + " generado en PDF.";
            } else {
                archivo = contexto.getReportePdfService().generarResumenIngresosGastos();
                descripcion = "Resumen financiero general de ingresos, egresos y cobranzas generado en PDF.";
            }

            return RespuestaChatbotDTO.builder()
                    .textoRespuesta(descripcion + " Ubicado en: " + archivo.getAbsolutePath())
                    .tipo(TipoRespuestaChatbot.REPORTE_PDF)
                    .archivoGenerado(archivo)
                    .comandoEjecutado("EXPORTAR_PDF")
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Exportar padrón de pagos a Excel", "Auditar recaudación y morosidad", "Generar voucher Depto 101"))
                    .build();
        } catch (Exception ex) {
            log.error("Fallo al exportar reporte PDF: {}", ex.getMessage());
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("No se pudo completar la exportación a PDF: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private RespuestaChatbotDTO procesarReporteExcel(String msg, ContextoChatbot contexto) {
        if (contexto == null || contexto.getExportadorCsvService() == null || contexto.getFachada() == null) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Servicio de exportación a Excel / CSV no disponible.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }

        try {
            CobranzaFachada fachada = contexto.getFachada();
            List<String> encabezados;
            List<List<String>> filas = new ArrayList<>();
            String baseNombre;

            if (msg.contains("gasto") || msg.contains("costo")) {
                baseNombre = "Auditoria_Gastos_Comunes";
                encabezados = Arrays.asList("ID", "Concepto", "Categoría", "Monto (S/)", "Fecha Registro", "Estado");
                List<GastoComun> gastos = fachada.listarGastosComunes();
                if (gastos != null) {
                    for (GastoComun g : gastos) {
                        String categoriaGasto = g.getClass().getSimpleName().replace("Gasto", "Gasto de ");
                        filas.add(Arrays.asList(
                                String.valueOf(g.getId()),
                                g.getDescripcion() != null ? g.getDescripcion() : "Gasto General",
                                categoriaGasto,
                                String.format("%.2f", g.getMonto() != null ? g.getMonto() : 0.0),
                                g.getFecha() != null ? g.getFecha().toString() : LocalDate.now().toString(),
                                "REGISTRADO"
                        ));
                    }
                }
            } else {
                baseNombre = "Padron_Cobranzas_Y_Pagos";
                encabezados = Arrays.asList("Cuota ID", "Departamento", "Periodo", "Monto Base (S/)", "Mora (S/)", "Total (S/)", "Estado");
                List<CuotaDTO> cuotas = fachada.listarCuotas();
                if (cuotas != null) {
                    for (CuotaDTO c : cuotas) {
                        double mBase = c.getMontoBase() != null ? c.getMontoBase() : 0.0;
                        double mFinal = c.getMontoFinal() != null ? c.getMontoFinal() : mBase;
                        double mora = (mFinal > mBase) ? (mFinal - mBase) : 0.0;
                        String venc = c.getFechaVencimiento() != null ? c.getFechaVencimiento() : "2026-11";
                        filas.add(Arrays.asList(
                                String.valueOf(c.getIdCuota()),
                                c.getNumeroDepartamento() != null ? c.getNumeroDepartamento() : "-",
                                venc,
                                String.format("%.2f", mBase),
                                String.format("%.2f", mora),
                                String.format("%.2f", mFinal),
                                c.getEstado() != null ? c.getEstado() : "PENDIENTE"
                        ));
                    }
                }
            }

            File archivoExcel = contexto.getExportadorCsvService().exportarMatrizCsv(baseNombre, encabezados, filas);

            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Planilla de datos para Microsoft Excel generada satisfactoriamente con codificación UTF-8 BOM. " +
                            "Archivo: " + archivoExcel.getName() + " guardado en carpeta ./exportaciones.")
                    .tipo(TipoRespuestaChatbot.REPORTE_EXCEL)
                    .archivoGenerado(archivoExcel)
                    .comandoEjecutado("EXPORTAR_EXCEL")
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Exportar reporte a PDF", "Auditar recaudación y morosidad", "Generar voucher Depto 101"))
                    .build();
        } catch (Exception ex) {
            log.error("Fallo al exportar planilla Excel: {}", ex.getMessage());
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Error al exportar archivo para Excel: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private RespuestaChatbotDTO procesarAuditoriaCostos(CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Fachada del sistema no inicializada para auditoría.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }

        List<CuotaDTO> cuotas = fachada.listarCuotas();
        List<GastoComun> gastos = fachada.listarGastosComunes();

        double totalRecaudado = 0.0;
        double totalPorCobrar = 0.0;
        int pagadas = 0;
        int pendientes = 0;

        if (cuotas != null) {
            for (CuotaDTO c : cuotas) {
                double montoCuota = c.getMontoFinal() != null ? c.getMontoFinal() : (c.getMontoBase() != null ? c.getMontoBase() : 0.0);
                if ("PAGADO".equalsIgnoreCase(c.getEstado())) {
                    totalRecaudado += montoCuota;
                    pagadas++;
                } else {
                    totalPorCobrar += montoCuota;
                    pendientes++;
                }
            }
        }

        double totalGastos = 0.0;
        if (gastos != null) {
            for (GastoComun g : gastos) {
                totalGastos += (g.getMonto() != null) ? g.getMonto() : 0.0;
            }
        }

        double balanceNeto = totalRecaudado - totalGastos;

        String reporteTexto = String.format(
                "AUDITORIA FINANCIERA GENERAL - COBRANZA VECINAL\n" +
                "- Recaudación Efectiva (Pagado): S/ %.2f (%d cuotas conciliadas)\n" +
                "- Cartera Pendiente / Morosidad: S/ %.2f (%d cuotas abiertas)\n" +
                "- Total Costos y Gastos Operativos: S/ %.2f (%d registros de egresos)\n" +
                "- Balance Neto del Periodo: S/ %.2f\n\n" +
                "Para emitir el reporte oficial en documento físico, solicite: 'exportar a PDF' o 'exportar a Excel'.",
                totalRecaudado, pagadas, totalPorCobrar, pendientes, totalGastos, (gastos != null ? gastos.size() : 0), balanceNeto
        );

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(reporteTexto)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .comandoEjecutado("AUDITORIA_FINANCIERA")
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Exportar reporte a PDF", "Exportar padrón a Excel", "Generar voucher Depto 101"))
                .build();
    }

    private RespuestaChatbotDTO procesarConsultaInmuebles(CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }
        List<InmuebleDTO> inmuebles = fachada.listarInmuebles();
        int total = (inmuebles != null) ? inmuebles.size() : 0;
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("El condominio cuenta con " + total + " departamentos registrados en el padrón oficial. " +
                        "Puede consultar un departamento específico, emitir cuotas o exportar el padrón inmobiliario a Excel.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Exportar padrón de pagos a Excel", "Auditar recaudación y morosidad", "Generar voucher Depto 101"))
                .build();
    }

    private RespuestaChatbotDTO procesarEmitirCuota(String comando, CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }
        String depto = extraerNumeroDepartamento(comando, "102");
        double monto = extraerMonto(comando, 200.0);

        try {
            CuotaDTO nuevaCuota = fachada.emitirNuevaCuota(depto, monto, LocalDate.now().plusDays(15));
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Acción CRUD completada: Cuota #" + (nuevaCuota != null ? nuevaCuota.getIdCuota() : "Generada") +
                            " emitida satisfactoriamente para el Departamento " + depto + " por S/ " + String.format("%.2f", monto) +
                            " con fecha de vencimiento a 15 días.")
                    .tipo(TipoRespuestaChatbot.ACCION_CRUD)
                    .comandoEjecutado("EMITIR_CUOTA")
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Auditar recaudación y morosidad", "Generar voucher Depto " + depto, "Exportar padrón a Excel"))
                    .build();
        } catch (Exception ex) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("No se pudo emitir la cuota: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private RespuestaChatbotDTO procesarRegistrarPago(String comando, CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }
        double monto = extraerMonto(comando, 150.0);
        Long idCuota = extraerId(comando, 1001L);

        try {
            boolean exito = fachada.procesarPagoConRepositorio(idCuota, monto, "CHAT-BOT-" + System.currentTimeMillis() % 10000);
            if (exito) {
                return RespuestaChatbotDTO.builder()
                        .textoRespuesta("Acción CRUD completada: Pago de S/ " + String.format("%.2f", monto) +
                                " registrado y conciliado con éxito para la cuota #" + idCuota + ". " +
                                "Puede solicitar: 'generar voucher de pago'.")
                        .tipo(TipoRespuestaChatbot.ACCION_CRUD)
                        .comandoEjecutado("REGISTRAR_PAGO")
                        .reproducirAudio(true)
                        .sugerenciasAccion(Arrays.asList("Generar voucher de pago Depto 101", "Auditar recaudación y morosidad", "Exportar reporte a PDF"))
                        .build();
            } else {
                return RespuestaChatbotDTO.builder()
                        .textoRespuesta("La cuota #" + idCuota + " ya se encuentra pagada o no pudo procesarse.")
                        .tipo(TipoRespuestaChatbot.TEXTO)
                        .build();
            }
        } catch (Exception ex) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Error al registrar el pago: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private RespuestaChatbotDTO procesarRegistrarIncidente(String comando, CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }
        String desc = comando.replaceFirst("(?i)registrar incidente", "")
                .replaceFirst("(?i)reportar averia", "")
                .trim();
        if (desc.isBlank()) desc = "Avería reportada desde el Asistente Virtual";

        fachada.reportarIncidente(desc, "Administrador");
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("Acción CRUD completada: Se ha registrado la novedad en la bitácora operativa: '" + desc + "'.")
                .tipo(TipoRespuestaChatbot.ACCION_CRUD)
                .comandoEjecutado("REGISTRAR_INCIDENTE")
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Auditar recaudación y morosidad", "Ver incidencias abiertas", "Iniciar modo instructivo"))
                .build();
    }

    private String extraerNumeroDepartamento(String texto, String porDefecto) {
        Matcher m = Pattern.compile("(?:depto|departamento|dpto|unidad)\\s*([a-zA-Z0-9]+)").matcher(texto);
        if (m.find()) {
            return m.group(1).toUpperCase();
        }
        Matcher m2 = Pattern.compile("\\b(\\d{3,4}[a-zA-Z]?)\\b").matcher(texto);
        if (m2.find()) {
            return m2.group(1).toUpperCase();
        }
        return porDefecto;
    }

    private double extraerMonto(String texto, double porDefecto) {
        Matcher m = Pattern.compile("(?:monto|s/|soles|por)?\\s*(\\d+(?:\\.\\d{1,2})?)").matcher(texto);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception ignored) {
            }
        }
        return porDefecto;
    }

    private Long extraerId(String texto, Long porDefecto) {
        Matcher m = Pattern.compile("(?:cuota|id|#)\\s*(\\d+)").matcher(texto);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (Exception ignored) {
            }
        }
        return porDefecto;
    }
}
