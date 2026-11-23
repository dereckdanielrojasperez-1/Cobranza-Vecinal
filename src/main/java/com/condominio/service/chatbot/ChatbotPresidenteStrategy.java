package com.condominio.service.chatbot;

import com.condominio.dto.CuotaDTO;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;

import java.util.Arrays;
import java.util.List;

// Estrategia para el perfil Presidente de la Junta Directiva y Residentes:
// Orientacion de pagos (Yape, Plin, BCP), desglose de consumos personales,
// atencion a dudas de convivencia y estricta restriccion RBAC financiera.
public class ChatbotPresidenteStrategy implements IEstrategiaChatbot {

    @Override
    public RolUsuario getRolObjetivo() {
        return RolUsuario.RESIDENTE;
    }

    @Override
    public String generarMensajeBienvenida(String nombreUsuario) {
        return "Hola, estimado vecino y Presidente de Junta (" + (nombreUsuario != null ? nombreUsuario : "Presidente") + "). " +
                "Soy su asistente condominal. Puedo orientarlo sobre métodos de pago (Yape, Plin, BCP), " +
                "consultar su consumo de agua y mantenimiento, o verificar las fechas de asambleas y comunicados.";
    }

    @Override
    public List<String> obtenerSugerenciasIniciales() {
        return Arrays.asList(
                "¿Cómo pagar con Yape o Plin?",
                "Ayúdame a manejarlo",
                "Consultar mi consumo y cuotas de mi piso",
                "Ver comunicados oficiales de la junta",
                "¿Cuáles son las cuentas del BCP y BBVA?",
                "Iniciar modo instructivo"
        );
    }

    @Override
    public RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto) {
        String msgOriginal = (mensaje != null) ? mensaje.trim() : "";
        String norm = AsistenteConversacionalUtil.normalizar(msgOriginal);
        CobranzaFachada fachada = (contexto != null) ? contexto.getFachada() : null;
        String depto = (deptoUsuario != null && !deptoUsuario.isBlank()) ? deptoUsuario : "101";

        // 1. RESTRICCIÓN ESTRICTA RBAC: Exportación de reportes financieros a PDF, Excel o Word
        if (norm.contains("pdf") || norm.contains("excel") || norm.contains("csv") || norm.contains("word") ||
                norm.contains("exportar") || norm.contains("descargar reporte") || norm.contains("imprimir voucher")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Acceso Restringido por Política de Seguridad RBAC:\n" +
                            "Estimado Presidente / Vecino, la exportación y generación de reportes financieros oficiales " +
                            "(en formato PDF, Excel o Word) y la emisión de vouchers contables maestros está reservada " +
                            "exclusivamente para el perfil Administrador. Usted puede visualizar en pantalla todos sus " +
                            "consumos personales, recibos y saldos en cualquier momento.")
                    .tipo(TipoRespuestaChatbot.RESTRICCION_RBAC)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("¿Cómo pagar con Yape o Plin?", "Consultar mi consumo", "Iniciar modo instructivo"))
                    .build();
        }

        // 2. Solicitudes de Tutorial Guiado / Modo Instructivo
        if (AsistenteConversacionalUtil.esSolicitudTutorial(norm) ||
                (AsistenteConversacionalUtil.esConfirmacionAfirmativa(norm) && (norm.contains("tutorial") || norm.contains("instructivo") || norm.equals("si") || norm.equals("dale")))) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Iniciando Modo Instructivo para Presidente de Junta. Se resaltará en pantalla cada botón y carpeta, oscureciendo el fondo para brindarle una guía paso a paso.")
                    .tipo(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT)
                    .comandoEjecutado("TUTORIAL_SPOTLIGHT")
                    .reproducirAudio(true)
                    .build();
        }

        // 3. Ayuda de Manejo e Inducción ("no sé manejar", "ayúdame a usarlo")
        if (AsistenteConversacionalUtil.esAyudaManejo(norm)) {
            return AsistenteConversacionalUtil.responderAyudaManejo(RolUsuario.RESIDENTE);
        }

        // 4. Saludos y Cortesía social
        if (AsistenteConversacionalUtil.esSaludo(norm) && !norm.contains("pago") && !norm.contains("consumo")) {
            return AsistenteConversacionalUtil.responderSaludo(RolUsuario.RESIDENTE, "Presidente / Vecino");
        }

        // 5. Identidad y Capacidades
        if (AsistenteConversacionalUtil.esIdentidad(norm)) {
            return AsistenteConversacionalUtil.responderIdentidad(RolUsuario.RESIDENTE);
        }

        // 6. Cortesía y Agradecimiento
        if (AsistenteConversacionalUtil.esCortesiaOAgradecimiento(norm)) {
            return AsistenteConversacionalUtil.responderCortesia();
        }

        // 7. Despedida
        if (AsistenteConversacionalUtil.esDespedida(norm)) {
            return AsistenteConversacionalUtil.responderDespedida();
        }

        // 8. Métodos de Pago: Yape, Plin, BCP, BBVA, Interbank
        if (norm.contains("como pagar") || norm.contains("metodo") || norm.contains("yape") || norm.contains("plin") ||
                norm.contains("bcp") || norm.contains("bbva") || norm.contains("cuenta") || norm.contains("transferencia")) {
            return procesarMetodosDePago();
        }

        // 9. Consulta de Consumo, Piso, Departamento y Cuotas (evitando colisión de subcadena con 'alícuota')
        if (norm.contains("consumo") || norm.contains("piso") || norm.contains("depto") ||
                (norm.contains("cuota") && !norm.contains("alicuota")) ||
                norm.contains("deuda") || norm.contains("cuanto debo") || norm.contains("saldo") || norm.contains("luz") || norm.contains("agua")) {
            return procesarConsultaConsumo(depto, fachada);
        }

        // 10. Comunicados Oficiales, Fechas y Asambleas
        if (norm.contains("comunicado") || norm.contains("aviso") || norm.contains("asamblea") || norm.contains("fecha") ||
                norm.contains("circular") || norm.contains("reunion") || norm.contains("junta")) {
            return procesarComunicados(fachada);
        }

        // 11. Reserva de Áreas Comunes (Parrillas, Piscina, Sala de Eventos)
        if (norm.contains("reserva") || norm.contains("parrilla") || norm.contains("piscina") || norm.contains("area comun") || norm.contains("alquilar")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Reserva de Áreas Comunes:\n\n" +
                            "- Como residente al día con tus cuotas, puedes solicitar el uso de la Zona de Parrillas y Terraza o la Piscina.\n" +
                            "- Para reservar: Ve a la barra lateral en 'Portal del Residente' -> 'Reservas de Áreas Comunes', escoge la fecha y turno disponible, y envía la solicitud.\n" +
                            "- Recuerda que las reservas están sujetas al aforo y cumplimiento del reglamento de convivencia.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Consultar mi consumo y cuotas", "¿Cómo pagar con Yape o Plin?", "Iniciar modo instructivo"))
                    .build();
        }

        // 12. Motor IA Local Conectado a BD en Tiempo Real (consultas normativas, alícuotas, convivencia y estado en vivo)
        return MotorIALocalCondominio.responderConsultaInteligente(norm, msgOriginal, RolUsuario.RESIDENTE, depto, contexto);
    }

    private RespuestaChatbotDTO procesarMetodosDePago() {
        String texto = "CANALES DE PAGO DISPONIBLES:\n\n" +
                "1. Billeteras Digitales (Sin comisión):\n" +
                "   - Yape / Plin al número institucional: 987-654-321 (Titular: Condominio Las Torres).\n" +
                "   - Indicar en el mensaje de envío su número de departamento (Ej: 'Depto 101').\n\n" +
                "2. Cuentas Corrientes Bancarias:\n" +
                "   - BCP Soles: 191-8849201-0-45 | CCI: 002-19100884920104505\n" +
                "   - BBVA Continental: 0011-0245-0200893321 | CCI: 011-24500020089332156\n\n" +
                "3. Pago Presencial:\n" +
                "   - En oficina de administración de lunes a viernes de 09:00 a 18:00 horas.\n" +
                "   - Se entrega recibo foliado con conciliación inmediata.";

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(texto)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Consultar mi consumo y cuotas", "Ver comunicados oficiales de la junta", "Iniciar modo instructivo"))
                .build();
    }

    private RespuestaChatbotDTO procesarConsultaConsumo(String depto, CobranzaFachada fachada) {
        double montoCuota = 180.0;
        String estadoCuota = "AL DIA";
        String periodo = "Noviembre 2026";

        if (fachada != null) {
            List<CuotaDTO> cuotas = fachada.listarCuotas();
            if (cuotas != null) {
                for (CuotaDTO c : cuotas) {
                    if (depto.equalsIgnoreCase(c.getNumeroDepartamento())) {
                        montoCuota = c.getMontoFinal() != null ? c.getMontoFinal() : (c.getMontoBase() != null ? c.getMontoBase() : 180.0);
                        estadoCuota = c.getEstado() != null ? c.getEstado() : "PENDIENTE";
                        periodo = c.getFechaVencimiento() != null ? c.getFechaVencimiento() : periodo;
                        break;
                    }
                }
            }
        }

        String texto = String.format(
                "INFORMACION DE CONSUMO Y UNIDAD INMOBILIARIA:\n" +
                "- Titular: Presidente de la Junta Directiva\n" +
                "- Unidad Inmobiliaria: Departamento %s (Piso 1 - Torre A)\n" +
                "- Alícuota de Copropiedad: 1.05 %%\n" +
                "- Periodo Facturado: %s\n" +
                "- Cuota Ordinaria de Mantenimiento: S/ %.2f\n" +
                "- Desglose de Servicios Comunes: Agua potable prorrateada, energía de áreas comunes y seguridad 24h.\n" +
                "- Estado de Cuenta: %s\n\n" +
                "Vencimiento: día 10 de cada mes sin recargo por mora.",
                depto, periodo, montoCuota, estadoCuota
        );

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(texto)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("¿Cómo pagar con Yape o Plin?", "Ver comunicados oficiales de la junta", "Iniciar modo instructivo"))
                .build();
    }

    private RespuestaChatbotDTO procesarComunicados(CobranzaFachada fachada) {
        StringBuilder sb = new StringBuilder("COMUNICADOS Y CIRCULARES VIGENTES:\n\n");
        if (fachada != null) {
            List<Comunicado> comunicados = fachada.listarComunicados();
            if (comunicados != null && !comunicados.isEmpty()) {
                int count = 0;
                for (Comunicado c : comunicados) {
                    sb.append("- ").append(c.getTitulo()).append(": ").append(c.getMensaje()).append("\n");
                    count++;
                    if (count >= 3) break;
                }
            } else {
                sb.append("- Mantenimiento preventivo de bombas y cisternas programado para el día 25.\n");
                sb.append("- Asamblea Ordinaria de Propietarios: primer sábado del mes a las 19:00 horas.\n");
            }
        } else {
            sb.append("- Próxima Asamblea General de Propietarios convocada para el sábado 28 de Noviembre.\n");
            sb.append("- Campaña de recolección selectiva y limpieza de ductos en curso.\n");
        }

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(sb.toString().trim())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Consultar mi consumo y cuotas", "¿Cómo pagar con Yape o Plin?", "Iniciar modo instructivo"))
                .build();
    }
}
