package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

// Utilidad de procesamiento de lenguaje natural y clasificacion de intenciones conversacionales
// para el Asistente Virtual IA en todos los perfiles de usuario.
public final class AsistenteConversacionalUtil {

    private AsistenteConversacionalUtil() {
    }

    // Normaliza el texto: elimina acentos, convierte a minusculas, retira puntuacion y colapsa espacios.
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    public static boolean esSaludo(String norm) {
        if (norm.isEmpty()) return false;
        return norm.equals("hola") ||
                norm.startsWith("hola ") ||
                norm.contains("que tal") ||
                norm.contains("buenos dias") ||
                norm.contains("buenas tardes") ||
                norm.contains("buenas noches") ||
                norm.equals("buenas") ||
                norm.contains("como estas") ||
                norm.contains("como te va") ||
                norm.equals("saludos") ||
                norm.startsWith("saludos ");
    }

    public static boolean esAyudaManejo(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("no se manejar") ||
                norm.contains("ayudame a manejar") ||
                norm.contains("ayuda a manejar") ||
                norm.contains("quiero que me ayudes a manejarlo") ||
                norm.contains("como se usa") ||
                norm.contains("como funciona el sistema") ||
                norm.contains("como funciona esto") ||
                norm.contains("no entiendo") ||
                norm.contains("ensename a usar") ||
                norm.contains("como empiezo") ||
                norm.contains("por donde empiezo") ||
                norm.contains("guia de uso") ||
                norm.equals("ayuda") ||
                norm.equals("ayudame") ||
                norm.equals("guia") ||
                norm.equals("orientacion") ||
                norm.contains("no se como usar");
    }

    public static boolean esSolicitudTutorial(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("iniciar modo instructivo") ||
                norm.contains("iniciar tutorial") ||
                norm.contains("activar tutorial") ||
                norm.contains("modo tutorial") ||
                norm.contains("spotlight") ||
                norm.equals("tutorial") ||
                norm.equals("modo instructivo") ||
                norm.equals("recorrido guiado") ||
                norm.contains("tutorial guiado") ||
                norm.equals("paso a paso");
    }

    public static boolean esConfirmacionAfirmativa(String norm) {
        if (norm.isEmpty()) return false;
        return norm.equals("si") ||
                norm.equals("si por favor") ||
                norm.equals("si quiero") ||
                norm.equals("claro") ||
                norm.equals("dale") ||
                norm.equals("iniciar") ||
                norm.equals("comenzar") ||
                norm.equals("activalo") ||
                norm.equals("vamos");
    }

    public static boolean esIdentidad(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("quien eres") ||
                norm.contains("que eres") ||
                norm.contains("cual es tu funcion") ||
                norm.contains("que puedes hacer") ||
                norm.contains("para que sirves") ||
                norm.contains("que funciones tienes") ||
                norm.contains("presentate");
    }

    public static boolean esCortesiaOAgradecimiento(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("gracias") ||
                norm.equals("muchas gracias") ||
                norm.equals("ok") ||
                norm.equals("vale") ||
                norm.equals("perfecto") ||
                norm.equals("entendido") ||
                norm.equals("excelente") ||
                norm.equals("genial") ||
                norm.equals("de acuerdo");
    }

    public static boolean esDespedida(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("adios") ||
                norm.contains("hasta luego") ||
                norm.contains("nos vemos") ||
                norm.equals("chau") ||
                norm.equals("hasta pronto");
    }

    public static boolean esPreguntaComoRegistrarPago(String norm) {
        return norm.contains("como registrar pago") ||
                norm.contains("como registro un pago") ||
                norm.contains("como registrar un pago") ||
                norm.contains("como cobrar") ||
                norm.contains("como se anota un pago") ||
                norm.contains("como hago para cobrar") ||
                norm.contains("donde registro el pago");
    }

    public static boolean esPreguntaComoEmitirCuota(String norm) {
        return norm.contains("como emitir cuota") ||
                norm.contains("como emitir una cuota") ||
                norm.contains("como generar cuota") ||
                norm.contains("como crear cuota") ||
                norm.contains("como crear una cuota") ||
                norm.contains("como genero una cuota");
    }

    public static boolean esPreguntaMorosidad(String norm) {
        if (norm.isEmpty()) return false;
        if (norm.contains("quienes") || norm.contains("quien") || norm.contains("lista") || norm.contains("cuales")) {
            return false;
        }
        return norm.contains("moros") ||
                norm.contains("no paga") ||
                norm.contains("no cancela") ||
                norm.contains("deudor") ||
                norm.contains("se atrasa") ||
                norm.contains("penalidad") ||
                norm.contains("interes por mora") ||
                norm.contains("mora");
    }

    public static boolean esPreguntaGastosYPresupuesto(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("gastos comunes") ||
                norm.contains("calculan los gastos") ||
                norm.contains("presupuesto") ||
                norm.contains("fondo de reserva") ||
                norm.contains("gasta el dinero") ||
                norm.contains("mantenimiento del edificio");
    }

    public static boolean esPreguntaModulosSistema(String norm) {
        if (norm.isEmpty()) return false;
        return norm.contains("modulo") ||
                norm.contains("pantalla") ||
                norm.contains("seccion") ||
                norm.contains("estructura del sistema") ||
                norm.contains("panel lateral") ||
                norm.contains("que botones");
    }

    public static boolean esPreguntaComoExportar(String norm) {
        return norm.contains("como exportar") ||
                norm.contains("como descargo") ||
                norm.contains("donde se guardan los reportes") ||
                norm.contains("donde estan los reportes") ||
                norm.contains("como imprimo") ||
                norm.contains("como sacar un pdf");
    }

    public static boolean esPreguntaVoz(String norm) {
        return norm.contains("como funciona la voz") ||
                norm.contains("como activar la voz") ||
                norm.contains("como uso la voz") ||
                norm.contains("modo voz");
    }

    // Genera respuesta de saludo concisa y adaptada al perfil
    public static RespuestaChatbotDTO responderSaludo(RolUsuario rol, String nombreUsuario) {
        String nombre = (nombreUsuario != null && !nombreUsuario.isBlank()) ? nombreUsuario : "estimado usuario";
        String saludo;
        List<String> sugerencias;

        if (rol == RolUsuario.ADMINISTRADOR) {
            saludo = "¡Hola! Mucho gusto, " + nombre + ". Estoy listo para apoyarte con la administración y cobranza del condominio. " +
                    "Puedes pedirme auditar la recaudación, emitir cuotas o comprobantes de pago, o decirme 'ayúdame a manejarlo' para guiarte.";
            sugerencias = Arrays.asList("Auditar recaudación y morosidad", "Ayúdame a manejarlo", "Generar voucher Depto 101", "Iniciar modo instructivo");
        } else if (rol == RolUsuario.RESIDENTE) {
            saludo = "¡Hola, vecino! Bienvenido al asistente virtual. " +
                    "Puedo orientarte con las formas de pago (Yape, Plin, BCP), consultar tus cuotas y consumo, o mostrarte las áreas comunes disponibles.";
            sugerencias = Arrays.asList("¿Cómo pagar con Yape o Plin?", "Consultar mi consumo y cuotas", "Ver comunicados oficiales", "Iniciar modo instructivo");
        } else {
            saludo = "¡Hola! Centro de Control Operativo listo. " +
                    "Puedo informarte sobre la bitácora de limpieza, registrar novedades en portería o anotar visitas.";
            sugerencias = Arrays.asList("Estado de limpieza y áreas comunes", "Ver incidencias abiertas", "Registrar visita", "Iniciar modo instructivo");
        }

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(saludo)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(sugerencias)
                .build();
    }

    // Genera respuesta de induccion paso a paso clara, concisa y accionable
    public static RespuestaChatbotDTO responderAyudaManejo(RolUsuario rol) {
        String guia;
        List<String> sugerencias;

        if (rol == RolUsuario.ADMINISTRADOR) {
            guia = "Con gusto te oriento para manejar el sistema de manera rápida y eficiente:\n\n" +
                    "1. Panel Lateral: En el menú de la izquierda accedes a 'Finanzas' (gestión de cuotas y cobros), 'Inmuebles' (departamentos y residentes) y 'Operaciones' (bitácora y accesos).\n" +
                    "2. Asistente Conversacional: Puedes pedirme directamente tareas como 'auditar recaudación', 'generar voucher Depto 101', 'exportar reporte a PDF' o 'emitir cuota 101 monto 200'.\n" +
                    "3. Modo Instructivo Interactivo: Puedo activar un tutorial visual que resalta en pantalla cada botón y carpeta, oscureciendo el fondo para un aprendizaje paso a paso.\n\n" +
                    "¿Deseas que active el recorrido guiado en pantalla ahora mismo?";
            sugerencias = Arrays.asList("Iniciar modo instructivo", "Auditar recaudación y morosidad", "¿Cómo registrar un pago?", "Exportar reporte a PDF");
        } else if (rol == RolUsuario.RESIDENTE) {
            guia = "Aquí tienes una guía rápida para consultar tus servicios como residente:\n\n" +
                    "1. Mis Cuotas: Consulta tu estado de cuenta mensual, consumo de agua y mantenimiento ordinario.\n" +
                    "2. Formas de Pago: Puedes pagar fácilmente por Yape, Plin o transferencia BCP/BBVA.\n" +
                    "3. Reservas y Novedades: Reserva áreas comunes (parrillas, piscina) o reporta averías directamente.\n\n" +
                    "¿Deseas iniciar el recorrido visual guiado en pantalla?";
            sugerencias = Arrays.asList("Iniciar modo instructivo", "¿Cómo pagar con Yape o Plin?", "Consultar mi consumo y cuotas", "Ver comunicados oficiales");
        } else {
            guia = "Guía operativa para el personal de portería y mantenimiento:\n\n" +
                    "1. Bitácora de Novedades: Revisa y reporta incidencias en las instalaciones del condominio.\n" +
                    "2. Control de Garita: Registra el ingreso y salida de visitantes o personal de delivery.\n" +
                    "3. Áreas Comunes: Monitorea el estado de limpieza y turnos de uso en tiempo real.\n\n" +
                    "¿Deseas activar el tutorial guiado en pantalla?";
            sugerencias = Arrays.asList("Iniciar modo instructivo", "Estado de limpieza y áreas comunes", "Registrar nueva incidencia", "Consultar visitas del día");
        }

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(guia)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(sugerencias)
                .build();
    }

    public static RespuestaChatbotDTO responderIdentidad(RolUsuario rol) {
        String texto = "Soy el Asistente Virtual Inteligente de Cobranza Vecinal. " +
                (rol == RolUsuario.ADMINISTRADOR ?
                        "Estoy diseñado para ayudarte a auditar finanzas, emitir vouchers de pago en PDF, exportar padrones a Excel, registrar cuotas y pagos, y guiarte paso a paso por el sistema." :
                        (rol == RolUsuario.RESIDENTE ?
                                "Estoy diseñado para asistirte en tus consultas de consumo, orientarte con los métodos de pago (Yape/Plin/Bancos) y notificarte los avisos de la junta." :
                                "Estoy diseñado para apoyarte en el registro de bitácora, control de visitas en garita y supervisión del estado operativo del edificio."));

        List<String> sugerencias = (rol == RolUsuario.ADMINISTRADOR) ?
                Arrays.asList("Auditar recaudación y morosidad", "Generar voucher Depto 101", "Iniciar modo instructivo") :
                Arrays.asList("Iniciar modo instructivo", "¿Cómo pagar con Yape o Plin?", "Consultar mi consumo");

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(texto)
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(sugerencias)
                .build();
    }

    public static RespuestaChatbotDTO responderCortesia() {
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("¡De nada! Quedo a tu disposición para cualquier otra consulta o gestión del condominio.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(Arrays.asList("Auditar recaudación y morosidad", "¿Cómo uso el sistema?", "Iniciar modo instructivo"))
                .build();
    }

    public static RespuestaChatbotDTO responderDespedida() {
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("¡Hasta luego! Recuerda que puedes volver a abrir el asistente en cualquier momento desde la barra superior.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .build();
    }
}
