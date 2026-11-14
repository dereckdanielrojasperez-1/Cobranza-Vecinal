package com.condominio.service;

import com.condominio.configuracion.ConstantesSistema;
import com.condominio.dto.CuotaDTO;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.patrones.facade.CobranzaFachada;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Servicio generador de reportes formales en formato PDF utilizando OpenPDF (Punto 3.1).
@Slf4j
@Service
public class ReportePdfService {

    private final CobranzaFachada fachada;
    private static final String CARPETA_REPORTES = "./reportes";

    public ReportePdfService(CobranzaFachada fachada) {
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();
        asegurarCarpetaExiste();
    }

    private void asegurarCarpetaExiste() {
        File dir = new File(CARPETA_REPORTES);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 1. Estado de cuenta individual por departamento / propietario
    public File generarEstadoCuentaPropietario(String numeroDepto) throws Exception {
        asegurarCarpetaExiste();
        String deptoSanitizado = (numeroDepto != null) ? numeroDepto.replaceAll("[^a-zA-Z0-9_-]", "") : "General";
        if (deptoSanitizado.isBlank()) {
            deptoSanitizado = "General";
        }
        String nombreArchivo = String.format("%s/Estado_Cuenta_Depto_%s.pdf", CARPETA_REPORTES, deptoSanitizado);
        File archivo = new File(nombreArchivo);

        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        doc.open();

        agregarMembrete(doc, "ESTADO DE CUENTA INDIVIDUAL - VECINO");

        Paragraph info = new Paragraph(String.format("Unidad Inmobiliaria: Departamento %s\nFecha de Emisión: %s",
                (numeroDepto != null ? numeroDepto : "101"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY));
        info.setSpacingAfter(14);
        doc.add(info);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{15, 25, 20, 20, 20});

        agregarCeldaEncabezado(tabla, "ID Cuota");
        agregarCeldaEncabezado(tabla, "Vencimiento");
        agregarCeldaEncabezado(tabla, "Monto Base");
        agregarCeldaEncabezado(tabla, "Saldo Actual");
        agregarCeldaEncabezado(tabla, "Estado");

        List<CuotaDTO> cuotas = fachada.listarCuotas();
        double saldoTotal = 0.0;
        if (cuotas != null) {
            for (CuotaDTO c : cuotas) {
                if (numeroDepto == null || numeroDepto.equalsIgnoreCase(c.getNumeroDepartamento())) {
                    tabla.addCell(String.valueOf(c.getIdCuota()));
                    tabla.addCell(c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "-");
                    tabla.addCell(String.format("S/ %.2f", c.getMontoBase()));
                    tabla.addCell(String.format("S/ %.2f", c.getMontoFinal()));
                    tabla.addCell(c.getEstado());
                    saldoTotal += c.getMontoFinal();
                }
            }
        }
        doc.add(tabla);

        Paragraph pieSaldo = new Paragraph(String.format("\nSaldo Total Pendiente de Pago: S/ %.2f", saldoTotal),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(200, 30, 30)));
        pieSaldo.setAlignment(Element.ALIGN_RIGHT);
        doc.add(pieSaldo);

        agregarPiePagina(doc);
        doc.close();
        log.info("Reporte PDF generado exitosamente: {}", archivo.getAbsolutePath());
        return archivo;
    }

    // 2. Cuotas pendientes consolidadas por edificio
    public File generarCuotasPendientesEdificio() throws Exception {
        asegurarCarpetaExiste();
        File archivo = new File(CARPETA_REPORTES + "/Cuotas_Pendientes_Edificio.pdf");
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        doc.open();

        agregarMembrete(doc, "REPORTE DE CUOTAS PENDIENTES POR EDIFICIO");

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{15, 20, 25, 20, 20});

        agregarCeldaEncabezado(tabla, "Depto");
        agregarCeldaEncabezado(tabla, "ID Cuota");
        agregarCeldaEncabezado(tabla, "Vencimiento");
        agregarCeldaEncabezado(tabla, "Saldo Deudor");
        agregarCeldaEncabezado(tabla, "Estado");

        List<CuotaDTO> cuotas = fachada.listarCuotas();
        double deudaAcumulada = 0.0;
        if (cuotas != null) {
            for (CuotaDTO c : cuotas) {
                if (!"PAGADO".equalsIgnoreCase(c.getEstado()) && c.getMontoFinal() > 0) {
                    tabla.addCell(c.getNumeroDepartamento());
                    tabla.addCell(String.valueOf(c.getIdCuota()));
                    tabla.addCell(c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "-");
                    tabla.addCell(String.format("S/ %.2f", c.getMontoFinal()));
                    tabla.addCell(c.getEstado());
                    deudaAcumulada += c.getMontoFinal();
                }
            }
        }
        doc.add(tabla);

        Paragraph total = new Paragraph(String.format("\nTotal Deuda Acumulada en Cartera: S/ %.2f", deudaAcumulada),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK));
        total.setAlignment(Element.ALIGN_RIGHT);
        doc.add(total);

        agregarPiePagina(doc);
        doc.close();
        return archivo;
    }

    // 3. Resumen mensual de ingresos y gastos
    public File generarResumenIngresosGastos() throws Exception {
        asegurarCarpetaExiste();
        File archivo = new File(CARPETA_REPORTES + "/Resumen_Mensual_Ingresos_Gastos.pdf");
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        doc.open();

        agregarMembrete(doc, "BALANCE MENSUAL DE INGRESOS Y EGRESOS");

        List<CuotaDTO> cuotas = fachada.listarCuotas();
        double ingresos = 0.0;
        double porCobrar = 0.0;
        if (cuotas != null) {
            for (CuotaDTO c : cuotas) {
                ingresos += Math.max(0, c.getMontoBase() - c.getMontoFinal());
                porCobrar += c.getMontoFinal();
            }
        }

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{40, 30, 30});

        agregarCeldaEncabezado(tabla, "Concepto Contable");
        agregarCeldaEncabezado(tabla, "Monto Ejecutado");
        agregarCeldaEncabezado(tabla, "Estado");

        tabla.addCell("Recaudación Cobranzas Ordinarias");
        tabla.addCell(String.format("S/ %.2f", ingresos));
        tabla.addCell("Ingreso Efectivo");

        tabla.addCell("Cuentas por Cobrar Pendientes");
        tabla.addCell(String.format("S/ %.2f", porCobrar));
        tabla.addCell("Por Recaudar");

        tabla.addCell("Presupuesto de Mantenimiento y Servicios");
        tabla.addCell(String.format("S/ %.2f", (ingresos + porCobrar)));
        tabla.addCell("Presupuestado");

        doc.add(tabla);
        agregarPiePagina(doc);
        doc.close();
        return archivo;
    }

    // 4. Registro de Incidentes y Reservas del Mes
    public File generarIncidentesYReservas() throws Exception {
        asegurarCarpetaExiste();
        File archivo = new File(CARPETA_REPORTES + "/Incidentes_Y_Reservas_Mes.pdf");
        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        doc.open();

        agregarMembrete(doc, "CONTROL DE INCIDENTES Y RESERVAS DE ÁREAS COMUNES");

        Paragraph titInc = new Paragraph("1. Incidentes Operativos Registrados", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK));
        titInc.setSpacingAfter(8);
        doc.add(titInc);

        PdfPTable tablaInc = new PdfPTable(4);
        tablaInc.setWidthPercentage(100);
        tablaInc.setWidths(new float[]{15, 45, 20, 20});
        agregarCeldaEncabezado(tablaInc, "ID");
        agregarCeldaEncabezado(tablaInc, "Descripción");
        agregarCeldaEncabezado(tablaInc, "Fecha");
        agregarCeldaEncabezado(tablaInc, "Estado");

        List<Incidente> incidentes = fachada.listarIncidentes();
        if (incidentes != null) {
            for (Incidente i : incidentes) {
                tablaInc.addCell("INC-" + i.getId());
                tablaInc.addCell(i.getDescripcion());
                tablaInc.addCell(i.getFecha() != null ? i.getFecha().toString() : "-");
                tablaInc.addCell(i.getEstado() != null ? i.getEstado() : "ABIERTO");
            }
        }
        doc.add(tablaInc);

        Paragraph titRes = new Paragraph("\n2. Reservas de Áreas Comunes Confirmadas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK));
        titRes.setSpacingAfter(8);
        doc.add(titRes);

        PdfPTable tablaRes = new PdfPTable(4);
        tablaRes.setWidthPercentage(100);
        tablaRes.setWidths(new float[]{15, 35, 25, 25});
        agregarCeldaEncabezado(tablaRes, "ID");
        agregarCeldaEncabezado(tablaRes, "Área Común");
        agregarCeldaEncabezado(tablaRes, "Fecha Reserva");
        agregarCeldaEncabezado(tablaRes, "Horario");

        List<ReservaArea> reservas = fachada.listarReservas();
        if (reservas != null) {
            for (ReservaArea r : reservas) {
                tablaRes.addCell("RES-" + r.getId());
                tablaRes.addCell(r.getAreaComun() != null ? r.getAreaComun().getNombre() : "Área Social");
                tablaRes.addCell(r.getFechaReserva() != null ? r.getFechaReserva().toString() : "-");
                tablaRes.addCell((r.getHoraInicio() != null ? r.getHoraInicio() : "10:00") + " - " + (r.getHoraFin() != null ? r.getHoraFin() : "14:00"));
            }
        }
        doc.add(tablaRes);

        agregarPiePagina(doc);
        doc.close();
        return archivo;
    }

    private void agregarMembrete(Document doc, String tituloReporte) throws DocumentException {
        Paragraph condo = new Paragraph(ConstantesSistema.TITULO_APLICACION.toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(232, 163, 61)));
        condo.setAlignment(Element.ALIGN_CENTER);
        doc.add(condo);

        Paragraph sub = new Paragraph("Sistema Automatizado de Administración y Cobranza Condominal - " + ConstantesSistema.VERSION,
                FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY));
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(10);
        doc.add(sub);

        Paragraph tit = new Paragraph(tituloReporte,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(11, 19, 30)));
        tit.setAlignment(Element.ALIGN_CENTER);
        tit.setSpacingAfter(14);
        doc.add(tit);
    }

    private void agregarPiePagina(Document doc) throws DocumentException {
        Paragraph pie = new Paragraph(String.format("\nGenerado por Cobranza Vecinal %s | Ciclo %s | Documento Oficial de Auditoría Condominal",
                ConstantesSistema.VERSION, ConstantesSistema.CICLO_ACADEMICO),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        celda.setBackgroundColor(new Color(16, 26, 38)); // Fondo oscuro institucional
        celda.setPadding(6);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }
}
