package com.condominio.patrones.observer;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

// Canal de notificación vía SMS a telefonía móvil (Patrón Observer).
@Slf4j
public class NotificadorSms implements ObservadorCobranza {

    private static final String DESTINATARIO_DEFAULT = "+51 987654321";

    @Override
    public void actualizar(String mensaje) {
        CompletableFuture.runAsync(() -> {
            String texto = String.format("[SMS ENVIADO] A: %s | Mensaje: %s", DESTINATARIO_DEFAULT, mensaje);
            log.info(texto);
            System.out.println(texto);
        });
    }
}
