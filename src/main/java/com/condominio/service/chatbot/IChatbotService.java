package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;

import java.util.List;

// Interfaz del servicio central de Chatbot Asistente Virtual para Cobranza Vecinal.
public interface IChatbotService {

    RespuestaChatbotDTO procesarMensaje(String mensaje, RolUsuario rol, String departamento);

    void reiniciarConversacion(RolUsuario rol, String nombreUsuario);

    List<MensajeChatbotDTO> getHistorial();

    boolean isModoVozHabilitado();

    void setModoVozHabilitado(boolean habilitado);

    List<String> obtenerSugerenciasPorRol(RolUsuario rol);
}
