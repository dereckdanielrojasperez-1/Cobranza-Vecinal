package com.condominio.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// DTO con el resultado de una consulta o accion procesada por el motor conversacional.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaChatbotDTO {

    private String textoRespuesta;
    private TipoRespuestaChatbot tipo;
    private File archivoGenerado;
    private String comandoEjecutado;
    private boolean reproducirAudio;

    @Builder.Default
    private List<String> sugerenciasAccion = new ArrayList<>();
}
