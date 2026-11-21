package com.condominio.service.chatbot;

// Tipos de respuesta que puede emitir el motor de chatbot inteligente segun el rol y comando ejecutado.
public enum TipoRespuestaChatbot {
    TEXTO,
    REPORTE_PDF,
    REPORTE_EXCEL,
    ACCION_CRUD,
    TUTORIAL_SPOTLIGHT,
    RESTRICCION_RBAC,
    LIMPIEZA_CONVERSACION
}
