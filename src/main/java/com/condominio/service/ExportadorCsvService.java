package com.condominio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.JTable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Servicio utilitario de exportación de datos a formato CSV estándar con BOM UTF-8 (Punto 3.3).
@Slf4j
@Service
public class ExportadorCsvService {

    private static final String CARPETA_EXPORTACIONES = "./exportaciones";

    public ExportadorCsvService() {
        asegurarCarpetaExiste();
    }

    private void asegurarCarpetaExiste() {
        File dir = new File(CARPETA_EXPORTACIONES);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // Exporta el contenido visual de un JTable a un archivo CSV estructurado con BOM UTF-8.
    public File exportarTablaCsv(JTable tabla, String nombreBase) throws Exception {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla a exportar no puede ser nula.");
        }
        asegurarCarpetaExiste();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseLimpia = (nombreBase != null) ? nombreBase.replaceAll("[^a-zA-Z0-9_-]", "") : "exportacion";
        if (baseLimpia.isBlank()) {
            baseLimpia = "exportacion";
        }
        String rutaArchivo = String.format("%s/%s_%s.csv", CARPETA_EXPORTACIONES, baseLimpia, timestamp);
        File archivo = new File(rutaArchivo);

        try (FileOutputStream fos = new FileOutputStream(archivo);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // Escribir Byte Order Mark (BOM) UTF-8 para que Excel reconozca tildes y caracteres especiales
            writer.write('\uFEFF');

            // 1. Cabeceras de columnas
            int colCount = tabla.getColumnCount();
            for (int c = 0; c < colCount; c++) {
                String header = tabla.getColumnName(c);
                writer.write(escaparCsv(header));
                if (c < colCount - 1) {
                    writer.write(";");
                }
            }
            writer.newLine();

            // 2. Filas de datos
            int rowCount = tabla.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    Object val = tabla.getValueAt(r, c);
                    writer.write(escaparCsv(val != null ? val.toString() : ""));
                    if (c < colCount - 1) {
                        writer.write(";");
                    }
                }
                writer.newLine();
            }
            writer.flush();
        }

        log.info("Archivo CSV exportado exitosamente: {}", archivo.getAbsolutePath());
        return archivo;
    }

    private String escaparCsv(String valor) {
        if (valor == null) return "";
        String v = valor.trim();
        // Mitigación de inyección de fórmulas CSV (OWASP Formula Injection)
        if (v.startsWith("=") || v.startsWith("@")) {
            v = "'" + v;
        }
        v = v.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains(",")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
