package com.condominio.service.chatbot;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.patrones.facade.CobranzaFachada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatbotServiceTest {

    private CobranzaFachada fachada;
    private IChatbotService chatbotService;

    @BeforeEach
    public void configurarEntorno() {
        fachada = new CobranzaFachada();
        chatbotService = new ChatbotServiceImpl(fachada, null, null, null);
    }

    @Test
    @DisplayName("Administrador debe auditar finanzas y exportar vouchers en PDF y planillas en Excel")
    public void testAdminAuditoriaYExportaciones() {
        // 1. Auditoría de recaudación
        RespuestaChatbotDTO respAuditoria = chatbotService.procesarMensaje(
                "auditar recaudacion y gastos del condominio", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respAuditoria);
        assertEquals(TipoRespuestaChatbot.TEXTO, respAuditoria.getTipo());
        assertTrue(respAuditoria.getTextoRespuesta().contains("AUDITORIA FINANCIERA GENERAL"));

        // 2. Generación de voucher de pago en PDF
        RespuestaChatbotDTO respVoucher = chatbotService.procesarMensaje(
                "generar voucher de pago depto 101 por monto 200 en pdf", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respVoucher);
        assertEquals(TipoRespuestaChatbot.REPORTE_PDF, respVoucher.getTipo());
        assertNotNull(respVoucher.getArchivoGenerado());
        assertTrue(respVoucher.getArchivoGenerado().exists());
        assertTrue(respVoucher.getArchivoGenerado().getName().endsWith(".pdf"));

        // 3. Exportación a Excel / CSV
        RespuestaChatbotDTO respExcel = chatbotService.procesarMensaje(
                "exportar resumen de pagos a excel", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respExcel);
        assertEquals(TipoRespuestaChatbot.REPORTE_EXCEL, respExcel.getTipo());
        assertNotNull(respExcel.getArchivoGenerado());
        assertTrue(respExcel.getArchivoGenerado().exists());
        assertTrue(respExcel.getArchivoGenerado().getName().endsWith(".csv"));
    }

    @Test
    @DisplayName("Administrador debe ejecutar acciones CRUD conversacionales (emitir cuotas y registrar incidentes)")
    public void testAdminComandosCrud() {
        RespuestaChatbotDTO respCuota = chatbotService.procesarMensaje(
                "emitir cuota 102 monto 185", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respCuota);
        assertEquals(TipoRespuestaChatbot.ACCION_CRUD, respCuota.getTipo());
        assertTrue(respCuota.getTextoRespuesta().contains("Acción CRUD completada"));

        RespuestaChatbotDTO respIncidente = chatbotService.procesarMensaje(
                "registrar incidente filtracion en sotano 2", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respIncidente);
        assertEquals(TipoRespuestaChatbot.ACCION_CRUD, respIncidente.getTipo());
        assertTrue(respIncidente.getTextoRespuesta().contains("registrado la novedad"));
    }

    @Test
    @DisplayName("Presidente debe consultar metodos de pago (Yape/Plin/BCP) y desglose de su consumo")
    public void testPresidenteMetodosPagoYConsumo() {
        // Métodos de pago
        RespuestaChatbotDTO respPago = chatbotService.procesarMensaje(
                "¿cómo pagar con Yape o Plin?", RolUsuario.PRESIDENTE, "101"
        );
        assertNotNull(respPago);
        assertEquals(TipoRespuestaChatbot.TEXTO, respPago.getTipo());
        assertTrue(respPago.getTextoRespuesta().contains("Yape / Plin"));
        assertTrue(respPago.getTextoRespuesta().contains("BCP"));

        // Desglose de consumo y piso
        RespuestaChatbotDTO respConsumo = chatbotService.procesarMensaje(
                "consultar mi consumo y detalles de mi piso", RolUsuario.PRESIDENTE, "101"
        );
        assertNotNull(respConsumo);
        assertEquals(TipoRespuestaChatbot.TEXTO, respConsumo.getTipo());
        assertTrue(respConsumo.getTextoRespuesta().contains("Departamento 101"));
        assertTrue(respConsumo.getTextoRespuesta().contains("Piso 1"));
    }

    @Test
    @DisplayName("Presidente debe tener restricción estricta RBAC para exportar PDF o Excel financiero")
    public void testPresidenteRestriccionRbacExportacion() {
        // Intento de exportar PDF
        RespuestaChatbotDTO respPdf = chatbotService.procesarMensaje(
                "exportar reporte financiero a pdf", RolUsuario.PRESIDENTE, "101"
        );
        assertNotNull(respPdf);
        assertEquals(TipoRespuestaChatbot.RESTRICCION_RBAC, respPdf.getTipo());
        assertNull(respPdf.getArchivoGenerado());
        assertTrue(respPdf.getTextoRespuesta().contains("Acceso Restringido"));

        // Intento de descargar Excel
        RespuestaChatbotDTO respExcel = chatbotService.procesarMensaje(
                "descargar padron general en excel", RolUsuario.PRESIDENTE, "101"
        );
        assertNotNull(respExcel);
        assertEquals(TipoRespuestaChatbot.RESTRICCION_RBAC, respExcel.getTipo());
        assertNull(respExcel.getArchivoGenerado());
        assertTrue(respExcel.getTextoRespuesta().contains("Acceso Restringido"));
    }

    @Test
    @DisplayName("Gerente y Limpieza deben consultar estado operativo y ejecutar CRUD de bitacora y visitas")
    public void testGerenteLimpiezaYOperaciones() {
        // Estado de limpieza de áreas comunes
        RespuestaChatbotDTO respLimpieza = chatbotService.procesarMensaje(
                "estado de limpieza de áreas comunes y piscina", RolUsuario.GERENTE, "101"
        );
        assertNotNull(respLimpieza);
        assertEquals(TipoRespuestaChatbot.TEXTO, respLimpieza.getTipo());
        assertTrue(respLimpieza.getTextoRespuesta().contains("BITACORA DE LIMPIEZA"));

        // CRUD de incidencias
        RespuestaChatbotDTO respIncidente = chatbotService.procesarMensaje(
                "registrar incidente bombilla quemada en escalera torre B", RolUsuario.GERENTE, "101"
        );
        assertNotNull(respIncidente);
        assertEquals(TipoRespuestaChatbot.ACCION_CRUD, respIncidente.getTipo());
        assertTrue(respIncidente.getTextoRespuesta().contains("Acción CRUD completada"));

        // CRUD de visitas
        RespuestaChatbotDTO respVisita = chatbotService.procesarMensaje(
                "registrar visita Juan Perez dni 87654321 depto 101 motivo delivery", RolUsuario.GERENTE, "101"
        );
        assertNotNull(respVisita);
        assertEquals(TipoRespuestaChatbot.ACCION_CRUD, respVisita.getTipo());
        assertTrue(respVisita.getTextoRespuesta().contains("Visita registrada formalmente"));
    }

    @Test
    @DisplayName("Gerente y Limpieza deben tener restricción estricta RBAC para exportar PDF/Excel contable")
    public void testGerenteRestriccionRbacFinanciera() {
        RespuestaChatbotDTO respPdf = chatbotService.procesarMensaje(
                "exportar balance financiero pdf", RolUsuario.GERENTE, "101"
        );
        assertNotNull(respPdf);
        assertEquals(TipoRespuestaChatbot.RESTRICCION_RBAC, respPdf.getTipo());
        assertNull(respPdf.getArchivoGenerado());
        assertTrue(respPdf.getTextoRespuesta().contains("Acceso Restringido"));
    }

    @Test
    @DisplayName("Modo Limpieza debe restablecer el historial y contexto conversacional")
    public void testModoLimpiezaConversacion() {
        chatbotService.procesarMensaje("hola asistente", RolUsuario.ADMINISTRADOR, "101");
        chatbotService.procesarMensaje("auditar costos", RolUsuario.ADMINISTRADOR, "101");
        assertTrue(chatbotService.getHistorial().size() >= 2);

        RespuestaChatbotDTO respLimpieza = chatbotService.procesarMensaje(
                "modo limpieza", RolUsuario.ADMINISTRADOR, "101"
        );
        assertNotNull(respLimpieza);
        assertEquals(TipoRespuestaChatbot.LIMPIEZA_CONVERSACION, respLimpieza.getTipo());
        assertTrue(respLimpieza.getTextoRespuesta().contains("Modo Limpieza aplicado"));

        // Historial reiniciado con mensaje de bienvenida limpio
        List<MensajeChatbotDTO> historial = chatbotService.getHistorial();
        assertEquals(1, historial.size());
        assertEquals(MensajeChatbotDTO.Emisor.ASISTENTE, historial.get(0).getEmisor());
    }

    @Test
    @DisplayName("Modo Instructivo debe desencadenar la señal de Spotlight Tutorial")
    public void testModoInstructivoTrigger() {
        RespuestaChatbotDTO respTutorial = chatbotService.procesarMensaje(
                "iniciar modo instructivo tutorial guiado", RolUsuario.PRESIDENTE, "101"
        );
        assertNotNull(respTutorial);
        assertEquals(TipoRespuestaChatbot.TUTORIAL_SPOTLIGHT, respTutorial.getTipo());
        assertTrue(respTutorial.getTextoRespuesta().contains("Modo Instructivo"));
    }

    @Test
    @DisplayName("Conmutacion de Modo Voz debe alternar estado del sintetizador")
    public void testModoVozConmutacion() {
        chatbotService.setModoVozHabilitado(false);
        assertFalse(chatbotService.isModoVozHabilitado());

        RespuestaChatbotDTO respActivar = chatbotService.procesarMensaje(
                "activar voz", RolUsuario.ADMINISTRADOR, "101"
        );
        assertTrue(chatbotService.isModoVozHabilitado());
        assertTrue(respActivar.getTextoRespuesta().contains("Modo Voz activado"));

        RespuestaChatbotDTO respDesactivar = chatbotService.procesarMensaje(
                "desactivar voz", RolUsuario.ADMINISTRADOR, "101"
        );
        assertFalse(chatbotService.isModoVozHabilitado());
        assertTrue(respDesactivar.getTextoRespuesta().contains("Modo Voz desactivado"));
    }
}
