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

// Estrategia para el perfil Administrador: auditoria profunda, exportacion formal a PDF/Excel y comandos CRUD.
@Slf4j
public class ChatbotAdminStrategy implements IEstrategiaChatbot {

    @Override
    public RolUsuario getRolObjetivo() {
        return RolUsuario.ADMINISTRADOR;
    }

    @Override
    public String generarMensajeBienvenida(String nombreUsuario) {
        return "Bienvenido Administrador (" + (nombreUsuario != null ? nombreUsuario : "Admin") + "). " +
                "El módulo de supervisión integral está listo. Puede auditar costos, emitir vouchers de pago, " +
                "exportar reportes a PDF o Excel y ejecutar acciones contables directamente.";
    }

    @Override
    public List<String> obtenerSugerenciasIniciales() {
        return Arrays.asList(
                "Auditar recaudación y morosidad",
                "Generar voucher de pago Depto 101",
                "Exportar ingresos y gastos a PDF",
                "Exportar padrón de pagos a Excel",
                "Examinar costos de servicios comunes",
                "Iniciar modo instructivo"
        );
    }

    @Override
    public RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto) {
        String msg = (mensaje != null) ? mensaje.trim().toLowerCase() : "";
        CobranzaFachada fachada = (contexto != null) ? contexto.getFachada() : null;

        // 1. Solicitudes de Tutorial Guiado / Modo Instructivo
        if (msg.contains("tutorial") || msg.contains("instructivo") || msg.contains("guia") || msg.contains("explicar")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Iniciando Modo Instructivo interactivo para Administrador. Resaltaremos en pantalla cada módulo y sus capacidades operativas.")
                    .tipo(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT)
                    .comandoEjecutado("TUTORIAL_SPOTLIGHT")
                    .reproducirAudio(true)
                    .build();
        }

        // 2. Exportación de Vouchers de Pago en PDF
        if ((msg.contains("voucher") || msg.contains("comprobante") || msg.contains("recibo")) &&
                (msg.contains("pdf") || msg.contains("generar") || msg.contains("descargar") || msg.contains("exportar") || msg.contains("emitir"))) {
            return procesarGeneracionVoucher(msg, contexto);
        }

        // 3. Exportación general a PDF (Financiero, Morosidad, Estado de Cuenta)
        if (msg.contains("pdf") || msg.contains("reporte pdf") || msg.contains("exportar pdf")) {
            return procesarReportePdf(msg, contexto);
        }

        // 4. Exportación a Excel / CSV
        if (msg.contains("excel") || msg.contains("csv") || msg.contains("planilla") || msg.contains("exportar excel")) {
            return procesarReporteExcel(msg, contexto);
        }

        // 5. Comandos CRUD conversacionales
        if (msg.startsWith("emitir cuota") || msg.startsWith("generar cuota") || msg.startsWith("crear cuota")) {
            return procesarEmitirCuota(mensaje, fachada);
        }
        if (msg.startsWith("registrar pago") || msg.startsWith("pagar cuota")) {
            return procesarRegistrarPago(mensaje, fachada);
        }
        if (msg.startsWith("registrar incidente") || msg.startsWith("reportar averia")) {
            return procesarRegistrarIncidente(mensaje, fachada);
        }

        // 6. Auditoría Profunda de Costos, Gastos, Recibos y Balances
        if (msg.contains("auditar") || msg.contains("costo") || msg.contains("gasto") || msg.contains("morosidad") ||
                msg.contains("recaudacion") || msg.contains("balance") || msg.contains("resumen") || msg.contains("estado")) {
            return procesarAuditoriaCostos(fachada);
        }

        // 7. Padrón inmobiliario y residentes
        if (msg.contains("inmueble") || msg.contains("departamento") || msg.contains("torre") || msg.contains("residente") || msg.contains("padron")) {
            return procesarConsultaInmuebles(fachada);
        }

        // 8. Respuesta general informativa para Administrador
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("Consulta procesada en módulo administrativo. Puede solicitar: 'auditar recaudación', " +
                        "'generar voucher Depto 101', 'exportar reporte a PDF', 'exportar a Excel', " +
                        "o escribir 'emitir cuota [depto] monto [monto]'.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(obtenerSugerenciasIniciales())
                .build();
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
                        "Puede consultar un departamento específico o exportar el padrón inmobiliario a Excel.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
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
