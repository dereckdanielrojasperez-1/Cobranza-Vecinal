package com.condominio.patrones.observer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

// Canal de notificación por correo electrónico (Patrón Observer).
@Slf4j
public class NotificadorEmail implements ObservadorCobranza {

    private static final String DIR_NOTIFICACIONES = "./notificaciones";

    @Override
    public void actualizar(String mensaje) {
        CompletableFuture.runAsync(() -> {
            try {
                File dir = new File(DIR_NOTIFICACIONES);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                File archivoEml = new File(dir, "notificacion_pago_" + timestamp + ".eml");

                try (FileWriter writer = new FileWriter(archivoEml, StandardCharsets.UTF_8)) {
                    writer.write("From: cobranza@condominio-vecinal.pe\n");
                    writer.write("To: residente@condominio.pe\n");
                    writer.write("Subject: Confirmación de Pago - Cobranza Vecinal\n");
                    writer.write("Date: " + LocalDateTime.now() + "\n");
                    writer.write("MIME-Version: 1.0\n");
                    writer.write("Content-Type: text/plain; charset=UTF-8\n\n");
                    writer.write("Estimado(a) Propietario / Residente:\n\n");
                    writer.write("Le confirmamos la recepción y procesamiento de su abono:\n");
                    writer.write(mensaje + "\n\n");
                    writer.write("Agradecemos su puntualidad.\n");
                    writer.write("Administración del Condominio\n");
                }

                log.info("[EMAIL ENVIADO] Generado archivo MIME: {}", archivoEml.getAbsolutePath());
            } catch (Exception ex) {
                log.error("Error al generar correo .eml: {}", ex.getMessage());
            }
        });
    }
}
