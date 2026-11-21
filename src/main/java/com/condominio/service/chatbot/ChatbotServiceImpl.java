package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.repositorio.PagoRepositorio;
import com.condominio.service.ExportadorCsvService;
import com.condominio.service.ReportePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Implementacion del servicio de Chatbot IA basada en el patron Fachada y patron Strategy.
@Slf4j
@Service
public class ChatbotServiceImpl implements IChatbotService {

    private final CobranzaFachada fachada;
    private final ReportePdfService reportePdfService;
    private final ExportadorCsvService exportadorCsvService;
    private final PagoRepositorio pagoRepositorio;

    private final List<MensajeChatbotDTO> historial = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    public ChatbotServiceImpl(
            @Autowired(required = false) CobranzaFachada fachada,
            @Autowired(required = false) ReportePdfService reportePdfService,
            @Autowired(required = false) ExportadorCsvService exportadorCsvService,
            @Autowired(required = false) PagoRepositorio pagoRepositorio) {
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();
        this.reportePdfService = (reportePdfService != null) ? reportePdfService : new ReportePdfService(this.fachada);
        this.exportadorCsvService = (exportadorCsvService != null) ? exportadorCsvService : new ExportadorCsvService();
        this.pagoRepositorio = pagoRepositorio;
    }

    public ChatbotServiceImpl() {
        this(null, null, null, null);
    }

    @Override
    public RespuestaChatbotDTO procesarMensaje(String mensaje, RolUsuario rol, String departamento) {
        if (mensaje == null || mensaje.isBlank()) {
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Por favor, ingrese una consulta o instrucción.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .build();
        }

        RolUsuario rolEfectivo = (rol != null) ? rol : SesionActual.getRol();
        String deptoEfectivo = (departamento != null && !departamento.isBlank()) ? departamento : "101";

        // Registrar mensaje de usuario en el historial
        historial.add(MensajeChatbotDTO.builder()
                .emisor(MensajeChatbotDTO.Emisor.USUARIO)
                .texto(mensaje.trim())
                .fechaHora(LocalDateTime.now())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .build());

        String msgMinuscula = mensaje.trim().toLowerCase();

        // Modo Limpieza de la Conversacion
        if (msgMinuscula.equals("limpiar") || msgMinuscula.equals("reiniciar") ||
                msgMinuscula.contains("modo limpieza") || msgMinuscula.equals("borrar historial") || msgMinuscula.equals("limpiar chat")) {
            reiniciarConversacion(rolEfectivo, SesionActual.getNombreUsuario());
            RespuestaChatbotDTO respuestaLimpieza = RespuestaChatbotDTO.builder()
                    .textoRespuesta("Modo Limpieza aplicado: Se ha restablecido el hilo de la conversación y el contexto de memoria del asistente.")
                    .tipo(TipoRespuestaChatbot.LIMPIEZA_CONVERSACION)
                    .comandoEjecutado("LIMPIAR_CONVERSACION")
                    .reproducirAudio(true)
                    .sugerenciasAccion(obtenerSugerenciasPorRol(rolEfectivo))
                    .build();

            if (isModoVozHabilitado()) {
                SintetizadorVoz.reproducirTextoAsync(respuestaLimpieza.getTextoRespuesta());
            }
            return respuestaLimpieza;
        }

        // Conmutacion de Modo Voz por texto (evaluar primero la desactivacion para evitar colision de subcadena)
        if (msgMinuscula.contains("desactivar voz") || msgMinuscula.contains("silenciar voz") || msgMinuscula.contains("apagar voz")) {
            setModoVozHabilitado(false);
            return RespuestaChatbotDTO.builder()
                    .textoRespuesta("Modo Voz desactivado: El asistente operará exclusivamente en modalidad de texto.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(false)
                    .build();
        } else if (msgMinuscula.contains("activar voz") || msgMinuscula.contains("habilitar voz")) {
            setModoVozHabilitado(true);
            RespuestaChatbotDTO r = RespuestaChatbotDTO.builder()
                    .textoRespuesta("Modo Voz activado: Las respuestas y explicaciones del asistente se reproducirán en audio.")
                    .tipo(TipoRespuestaChatbot.TEXTO)
                    .reproducirAudio(true)
                    .build();
            SintetizadorVoz.reproducirTextoAsync(r.getTextoRespuesta());
            return r;
        }

        // Construir contexto para delegar a la estrategia correspondiente
        ContextoChatbot contexto = ContextoChatbot.builder()
                .fachada(this.fachada)
                .reportePdfService(this.reportePdfService)
                .exportadorCsvService(this.exportadorCsvService)
                .pagoRepositorio(this.pagoRepositorio)
                .build();

        IEstrategiaChatbot estrategia = FabricaEstrategiaChatbot.obtenerEstrategia(rolEfectivo);
        RespuestaChatbotDTO respuesta = estrategia.procesarConsulta(mensaje, deptoEfectivo, contexto);

        // Guardar mensaje del asistente en el historial
        historial.add(MensajeChatbotDTO.builder()
                .emisor(MensajeChatbotDTO.Emisor.ASISTENTE)
                .texto(respuesta.getTextoRespuesta())
                .fechaHora(LocalDateTime.now())
                .tipo(respuesta.getTipo())
                .archivoAdjunto(respuesta.getArchivoGenerado())
                .comandoEjecutado(respuesta.getComandoEjecutado())
                .build());

        // Reproducir audio si el modo voz está activo y la respuesta lo amerita
        if (isModoVozHabilitado() && respuesta.isReproducirAudio()) {
            SintetizadorVoz.reproducirTextoAsync(respuesta.getTextoRespuesta());
        }

        return respuesta;
    }

    @Override
    public void reiniciarConversacion(RolUsuario rol, String nombreUsuario) {
        historial.clear();
        IEstrategiaChatbot estrategia = FabricaEstrategiaChatbot.obtenerEstrategia(rol);
        String bienvenida = estrategia.generarMensajeBienvenida(nombreUsuario);

        historial.add(MensajeChatbotDTO.builder()
                .emisor(MensajeChatbotDTO.Emisor.ASISTENTE)
                .texto(bienvenida)
                .fechaHora(LocalDateTime.now())
                .tipo(TipoRespuestaChatbot.TEXTO)
                .build());
    }

    @Override
    public List<MensajeChatbotDTO> getHistorial() {
        return new ArrayList<>(historial);
    }

    @Override
    public boolean isModoVozHabilitado() {
        return SintetizadorVoz.isHabilitado();
    }

    @Override
    public void setModoVozHabilitado(boolean habilitado) {
        SintetizadorVoz.setHabilitado(habilitado);
    }

    @Override
    public List<String> obtenerSugerenciasPorRol(RolUsuario rol) {
        IEstrategiaChatbot estrategia = FabricaEstrategiaChatbot.obtenerEstrategia(rol);
        return estrategia.obtenerSugerenciasIniciales();
    }
}
