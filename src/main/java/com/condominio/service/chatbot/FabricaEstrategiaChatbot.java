package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;
import org.springframework.stereotype.Component;

// Factoria del patron Strategy para instanciar la estrategia conversacional adecuada segun el rol del usuario.
@Component
public class FabricaEstrategiaChatbot {

    public static IEstrategiaChatbot obtenerEstrategia(RolUsuario rol) {
        if (rol == null) {
            return new ChatbotPresidenteStrategy();
        }
        switch (rol) {
            case ADMINISTRADOR:
                return new ChatbotAdminStrategy();
            case PRESIDENTE:
                return new ChatbotPresidenteStrategy();
            case GERENTE:
            case CONSERJE:
                return new ChatbotOperativoStrategy();
            case RESIDENTE:
            default:
                return new ChatbotPresidenteStrategy();
        }
    }
}
