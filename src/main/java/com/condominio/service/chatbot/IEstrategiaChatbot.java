package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;

import java.util.List;

// Interfaz para el patron Strategy que desacopla la logica conversacional por rol de usuario.
public interface IEstrategiaChatbot {

    RolUsuario getRolObjetivo();

    RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto);

    String generarMensajeBienvenida(String nombreUsuario);

    List<String> obtenerSugerenciasIniciales();
}
