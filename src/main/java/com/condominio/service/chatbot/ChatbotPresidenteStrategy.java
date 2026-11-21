package com.condominio.service.chatbot;

import com.condominio.dto.CuotaDTO;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.operaciones.Comunicado;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;

import java.util.Arrays;
import java.util.List;

// Estrategia para el perfil Presidente de la Junta Directiva y Residentes:
// Orientacion de pagos (Yape, Plin, BCP), desglose de consumos personales y estricta restriccion RBAC.
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
                "Consultar mi consumo y cuotas de mi piso",
                "Ver comunicados oficiales de la junta",
                "¿Cuáles son las cuentas del BCP y BBVA?",
                "Iniciar modo instructivo"
        );
    }

    @Override
    public RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto) {
        String msg = (mensaje != null) ? mensaje.trim().toLowerCase() : "";
        CobranzaFachada fachada = (contexto != null) ? contexto.getFachada() : null;
        String depto = (deptoUsuario != null && !deptoUsuario.isBlank()) ? deptoUsuario : "101";

        // 1. RESTRICCIÓN ESTRICTA RBAC: Exportación de reportes financieros a PDF, Excel o Word
        if (msg.contains("pdf") || msg.contains("excel") || msg.contains("csv") || msg.contains("word") ||
                msg.contains("exportar") || msg.contains("descargar reporte") || msg.contains("imprimir voucher")) {
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
        if (msg.contains("tutorial") || msg.contains("instructivo") || msg.contains("guia") ||
                msg.contains("explicar") || msg.contains("como usar") || msg.contains("botones")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Iniciando Modo Instructivo para Presidente de Junta. Se resaltará en pantalla cada botón y carpeta, oscureciendo el fondo para brindarle una guía paso a paso.")
                    .tipo(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT)
                    .comandoEjecutado("TUTORIAL_SPOTLIGHT")
                    .reproducirAudio(true)
                    .build();
        }

        // 3. Métodos de Pago: Yape, Plin, BCP, BBVA, Interbank
        if (msg.contains("como pagar") || msg.contains("metodo") || msg.contains("yape") || msg.contains("plin") ||
                msg.contains("bcp") || msg.contains("bbva") || msg.contains("cuenta") || msg.contains("transferencia")) {
            return procesarMetodosDePago();
        }

        // 4. Consulta de Consumo, Piso, Departamento y Cuotas
        if (msg.contains("consumo") || msg.contains("piso") || msg.contains("depto") || msg.contains("cuota") ||
                msg.contains("deuda") || msg.contains("cuanto debo") || msg.contains("saldo") || msg.contains("luz") || msg.contains("agua")) {
            return procesarConsultaConsumo(depto, fachada);
        }

        // 5. Comunicados Oficiales y Fechas del Condominio
        if (msg.contains("comunicado") || msg.contains("aviso") || msg.contains("asamblea") || msg.contains("fecha") ||
                msg.contains("circular") || msg.contains("reunion") || msg.contains("junta")) {
            return procesarComunicados(fachada);
        }

        // 6. Respuesta General Amigable
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("Estimado Presidente, puedo responder sobre sus opciones de pago (Yape/Plin/Bancos), " +
                        "su consumo mensual en el Depto " + depto + " (Piso 1), avisos oficiales o iniciar el recorrido guiado " +
                        "de la pantalla con 'Modo instructivo'.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(obtenerSugerenciasIniciales())
                .build();
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
                .sugerenciasAccion(Arrays.asList("Consultar mi consumo y cuotas", "Ver comunicados vigentes", "Iniciar modo instructivo"))
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
                .build();
    }
}
