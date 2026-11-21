package com.condominio.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.LocalDateTime;

// DTO que representa un mensaje en el hilo conversacional del Asistente Virtual.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChatbotDTO {

    public enum Emisor {
        USUARIO,
        ASISTENTE
    }

    private Emisor emisor;
    private String texto;
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
    private TipoRespuestaChatbot tipo;
    private File archivoAdjunto;
    private String comandoEjecutado;
}
