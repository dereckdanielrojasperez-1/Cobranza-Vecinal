package com.condominio.service.chatbot;

import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.repositorio.PagoRepositorio;
import com.condominio.service.ExportadorCsvService;
import com.condominio.service.ReportePdfService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contexto contenedor de servicios de infraestructura para las estrategias de chatbot.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextoChatbot {

    private CobranzaFachada fachada;
    private ReportePdfService reportePdfService;
    private ExportadorCsvService exportadorCsvService;
    private PagoRepositorio pagoRepositorio;
}
