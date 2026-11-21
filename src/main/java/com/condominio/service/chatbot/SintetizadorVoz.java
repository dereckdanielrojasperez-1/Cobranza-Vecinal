package com.condominio.service.chatbot;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Utilidad asincrona de sintesis de voz (Text-to-Speech) para el Asistente Virtual y Modo Instructivo.
@Slf4j
public final class SintetizadorVoz {

    private static final ExecutorService ejecutorAudio = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Hilo-Sintetizador-Voz-Asistente");
        t.setDaemon(true);
        return t;
    });

    private static volatile Process procesoActual = null;
    private static volatile boolean habilitado = false;

    private SintetizadorVoz() {
    }

    public static boolean isHabilitado() {
        return habilitado;
    }

    public static void setHabilitado(boolean estado) {
        habilitado = estado;
        if (!estado) {
            detenerAudio();
        }
    }

    public static void conmutarModoVoz() {
        setHabilitado(!habilitado);
    }

    // Sintetiza el texto de manera asincrona sin bloquear el hilo de la interfaz grafica
    public static void reproducirTextoAsync(String texto) {
        if (!habilitado || texto == null || texto.isBlank()) {
            return;
        }

        ejecutorAudio.submit(() -> {
            try {
                detenerAudio();

                // Sanitizacion estricta del texto para evitar secuencias no deseadas en PowerShell
                String textoLimpio = texto.replaceAll("[\\r\\n]+", " ")
                        .replaceAll("[\"'`$<>|&;]", " ")
                        .replaceAll("S/\\s*", " Soles ")
                        .trim();

                if (textoLimpio.length() > 320) {
                    textoLimpio = textoLimpio.substring(0, 320) + "...";
                }

                if (textoLimpio.isBlank()) {
                    return;
                }

                String comandoPowerShell = String.format(
                        "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Rate = 1; " +
                        "$s.Volume = 90; " +
                        "$s.Speak('%s');",
                        textoLimpio
                );

                ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", comandoPowerShell);
                procesoActual = pb.start();
                procesoActual.waitFor();
            } catch (Exception ex) {
                log.debug("No se pudo sintetizar voz: {}", ex.getMessage());
            } finally {
                procesoActual = null;
            }
        });
    }

    public static void detenerAudio() {
        if (procesoActual != null && procesoActual.isAlive()) {
            try {
                procesoActual.destroyForcibly();
            } catch (Exception ignored) {
            }
            procesoActual = null;
        }
    }
}
