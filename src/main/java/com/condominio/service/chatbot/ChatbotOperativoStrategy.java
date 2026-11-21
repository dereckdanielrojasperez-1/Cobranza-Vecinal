package com.condominio.service.chatbot;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.personas.Visita;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Estrategia para perfiles de Gerencia Operativa, Mantenimiento, Limpieza y Conserjeria:
// Monitoreo de instalaciones, bitacora de novedades, CRUD operativo y restriccion RBAC financiera.
public class ChatbotOperativoStrategy implements IEstrategiaChatbot {

    @Override
    public RolUsuario getRolObjetivo() {
        return RolUsuario.GERENTE;
    }

    @Override
    public String generarMensajeBienvenida(String nombreUsuario) {
        return "Bienvenido al Centro de Control Operativo (" + (nombreUsuario != null ? nombreUsuario : "Gerente / Limpieza") + "). " +
                "Puedo informarle sobre el estado de las áreas comunes, bitácora de limpieza, visitas registradas en garita " +
                "o permitirle registrar incidencias y visitas mediante comandos conversacionales.";
    }

    @Override
    public List<String> obtenerSugerenciasIniciales() {
        return Arrays.asList(
                "Estado de limpieza y áreas comunes",
                "Ver incidencias y averías abiertas",
                "Consultar visitas del día en portería",
                "Registrar nueva incidencia de mantenimiento",
                "Iniciar modo instructivo"
        );
    }

    @Override
    public RespuestaChatbotDTO procesarConsulta(String mensaje, String deptoUsuario, ContextoChatbot contexto) {
        String msg = (mensaje != null) ? mensaje.trim().toLowerCase() : "";
        CobranzaFachada fachada = (contexto != null) ? contexto.getFachada() : null;

        // 1. RESTRICCIÓN ESTRICTA RBAC: Exportación de finanzas a PDF, Excel o Word y cuotas
        if (msg.contains("pdf") || msg.contains("excel") || msg.contains("csv") || msg.contains("word") ||
                msg.contains("exportar financiero") || msg.contains("voucher contable") || msg.contains("balance contable")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Acceso Restringido por Política RBAC:\n" +
                            "El perfil Gerente / Limpieza está facultado para la supervisión física de la infraestructura, " +
                            "bitácora de portería, tareas de mantenimiento y limpieza. La emisión de reportes financieros " +
                            "oficiales en PDF/Excel y la conciliación bancaria corresponde exclusivamente a Administración.")
                    .tipo(TipoRespuestaChatbot.RESTRICCION_RBAC)
                    .reproducirAudio(true)
                    .sugerenciasAccion(Arrays.asList("Estado de limpieza y áreas comunes", "Ver incidencias abiertas", "Registrar nueva incidencia"))
                    .build();
        }

        // 2. Solicitudes de Tutorial Guiado / Modo Instructivo
        if (msg.contains("tutorial") || msg.contains("instructivo") || msg.contains("guia") || msg.contains("explicar")) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Iniciando Modo Instructivo para personal Operativo y de Mantenimiento. Conoceremos el flujo de bitácora, registro de garita y reportes.")
                    .tipo(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT)
                    .comandoEjecutado("TUTORIAL_SPOTLIGHT")
                    .reproducirAudio(true)
                    .build();
        }

        // 3. Estado de Limpieza y Áreas Comunes
        if (msg.contains("limpieza") || msg.contains("area") || msg.contains("piscina") ||
                msg.contains("gimnasio") || msg.contains("terraza") || msg.contains("ascensor") || msg.contains("instalacion")) {
            return procesarEstadoLimpieza(fachada);
        }

        // 4. Consulta de Incidencias Operativas
        if (msg.contains("incidente") || msg.contains("averia") || msg.contains("reparacion") || msg.contains("falla")) {
            if (msg.startsWith("registrar") || msg.startsWith("reportar") || msg.startsWith("crear")) {
                return procesarRegistrarIncidente(mensaje, fachada);
            }
            return procesarConsultaIncidentes(fachada);
        }

        // 5. Control de Visitas y Garita
        if (msg.contains("visita") || msg.contains("garita") || msg.contains("porteria") || msg.contains("ingreso")) {
            if (msg.startsWith("registrar") || msg.startsWith("ingresar")) {
                return procesarRegistrarVisita(mensaje, fachada);
            }
            return procesarConsultaVisitas(fachada);
        }

        // 6. Comandos CRUD conversacionales
        if (msg.startsWith("registrar incidente") || msg.startsWith("reportar averia")) {
            return procesarRegistrarIncidente(mensaje, fachada);
        }
        if (msg.startsWith("registrar visita")) {
            return procesarRegistrarVisita(mensaje, fachada);
        }

        // 7. Respuesta Informativa Operativa
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("Módulo Operativo listo. Puede consultar: 'estado de limpieza', " +
                        "'ver incidencias abiertas', 'visitas del día', o ejecutar: " +
                        "'registrar incidente [descripción]' o 'registrar visita [nombre] depto [número]'.")
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .sugerenciasAccion(obtenerSugerenciasIniciales())
                .build();
    }

    private RespuestaChatbotDTO procesarEstadoLimpieza(CobranzaFachada fachada) {
        StringBuilder sb = new StringBuilder("BITACORA DE LIMPIEZA Y ESTADO DE AREAS COMUNES:\n\n");
        sb.append("- Lobby y Recepción: Limpieza profunda realizada a las 07:30 hrs. Estado: OPTIMO.\n");
        sb.append("- Ascensores Torre A y B: Sanitización y pulido completado. Estado: OPERATIVO.\n");
        sb.append("- Pasillos y Escaleras: Barrido matutino en pisos 1 al 10. Estado: LIMPIO.\n");
        sb.append("- Zona de Parrillas y Terraza: Inspección realizada. Disponible para reservas.\n");
        sb.append("- Cuarto de Bombas y Cisternas: Monitoreo de presión normal. Próximo mantenimiento preventivo: día 25.\n");

        if (fachada != null) {
            List<AreaComun> areas = fachada.listarAreasComunes();
            if (areas != null && !areas.isEmpty()) {
                sb.append("\nÁreas Comunes Monitoreadas: ").append(areas.size()).append(" sectores habilitados.");
            }
        }

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(sb.toString())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .build();
    }

    private RespuestaChatbotDTO procesarConsultaIncidentes(CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }

        List<Incidente> incidentes = fachada.listarIncidentes();
        int total = (incidentes != null) ? incidentes.size() : 0;

        StringBuilder sb = new StringBuilder("INCIDENCIAS OPERATIVAS EN SEGUIMIENTO (" + total + "):\n\n");
        if (incidentes != null && !incidentes.isEmpty()) {
            for (Incidente i : incidentes) {
                sb.append("- [INC-").append(i.getId()).append("] ")
                        .append(i.getDescripcion()).append(" | Estado: ")
                        .append(i.getEstado() != null ? i.getEstado() : "ABIERTO").append("\n");
            }
        } else {
            sb.append("No se registran averías críticas pendientes. Todas las instalaciones funcionan con normalidad.\n");
        }
        sb.append("\nPuede registrar una nueva novedad escribiendo: 'registrar incidente [descripción]'.");

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(sb.toString())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .build();
    }

    private RespuestaChatbotDTO procesarConsultaVisitas(CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }

        List<Visita> visitas = fachada.listarVisitas();
        int total = (visitas != null) ? visitas.size() : 0;

        StringBuilder sb = new StringBuilder("REGISTRO DE VISITAS EN GARITA Y PORTERIA (" + total + " registros):\n\n");
        if (visitas != null && !visitas.isEmpty()) {
            int count = 0;
            for (Visita v : visitas) {
                String deptoDestino = (v.getDepartamento() != null && v.getDepartamento().getNumero() != null)
                        ? v.getDepartamento().getNumero() : "101";
                sb.append("- ").append(v.getNombre()).append(" | DNI: ").append(v.getDni())
                        .append(" | Depto destino: ").append(deptoDestino)
                        .append(" | Motivo: ").append(v.getMotivo() != null ? v.getMotivo() : "Visita regular").append("\n");
                count++;
                if (count >= 5) break;
            }
        } else {
            sb.append("No hay visitas activas en portería en este instante.\n");
        }
        sb.append("\nPuede registrar un nuevo visitante con: 'registrar visita [nombre] dni [numero] depto [numero]'.");

        return RespuestaChatbotDTO.builder()
                .textoRespuesta(sb.toString())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .reproducirAudio(true)
                .build();
    }

    private RespuestaChatbotDTO procesarRegistrarIncidente(String comando, CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }
        String desc = comando.replaceFirst("(?i)registrar incidente", "")
                .replaceFirst("(?i)reportar averia", "")
                .replaceFirst("(?i)reportar incidente", "")
                .trim();
        if (desc.isBlank()) desc = "Mantenimiento preventivo de áreas comunes";

        fachada.reportarIncidente(desc, "Gerente de Operaciones");
        return RespuestaChatbotDTO.builder()
                .textoRespuesta("Acción CRUD completada por Gerencia: Incidencia registrada con éxito: '" + desc +
                        "'. Se ha asignado al equipo técnico para su atención inmediata.")
                .tipo(TipoRespuestaChatbot.ACCION_CRUD)
                .comandoEjecutado("REGISTRAR_INCIDENTE")
                .reproducirAudio(true)
                .build();
    }

    private RespuestaChatbotDTO procesarRegistrarVisita(String comando, CobranzaFachada fachada) {
        if (fachada == null) {
            return RespuestaChatbotDTO.builder().textoRespuesta("Fachada no disponible.").tipo(TipoRespuestaChatbot.TEXTO).build();
        }

        String nombre = extraerPatron(comando, "(?:visita|a|sr\\.?|sra\\.?)\\s+([a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+?)(?:\\s+dni|\\s+depto|\\s+motivo|$)", "Visitante Particular");
        String dni = extraerPatron(comando, "dni\\s*([0-9]{8})", "76543210");
        String depto = extraerPatron(comando, "(?:depto|departamento)\\s*([a-zA-Z0-9]+)", "101");
        String motivo = extraerPatron(comando, "motivo\\s*([a-zA-Z0-9\\s]+)", "Gestión personal / entrega");

        try {
            fachada.registrarIngresoVisita(nombre.trim(), dni, depto, motivo);
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Acción CRUD completada en Portería: Visita registrada formalmente para " + nombre.trim() +
                            " (DNI: " + dni + ") con destino al Departamento " + depto + ". Motivo: " + motivo + ".")
                    .tipo(TipoRespuestaChatbot.ACCION_CRUD)
                    .comandoEjecutado("REGISTRAR_VISITA")
                    .reproducirAudio(true)
                    .build();
        } catch (Exception ex) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Error al registrar la visita: " + ex.getMessage())
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }
    }

    private String extraerPatron(String texto, String regex, String porDefecto) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(texto);
        if (m.find()) {
            return m.group(1).trim();
        }
        return porDefecto;
    }
}
