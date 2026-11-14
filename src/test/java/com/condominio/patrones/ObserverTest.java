package com.condominio.patrones;

import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObserverTest {

    @Test
    @DisplayName("Debe notificar al residente cuando se emite una notificación de cobranza")
    public void debeNotificarResidenteAlProcesarPago() {
        GestorNotificaciones gestor = new GestorNotificaciones();
        NotificadorResidente notificador = new NotificadorResidente();

        gestor.suscribir(notificador);
        assertEquals(1, gestor.cantidadObservadores());

        String mensaje = "Se procesó un pago de S/ 150.00 para el departamento 101";
        gestor.notificar(mensaje);

        assertEquals(mensaje, notificador.getUltimoMensaje());
    }

    @Test
    @DisplayName("Debe permitir desuscribir un observador para no recibir notificaciones posteriores")
    public void debePermitirDesuscribirObservador() {
        GestorNotificaciones gestor = new GestorNotificaciones();
        NotificadorResidente notificador = new NotificadorResidente();

        gestor.suscribir(notificador);
        assertEquals(1, gestor.cantidadObservadores());

        gestor.desuscribir(notificador);
        assertEquals(0, gestor.cantidadObservadores());

        gestor.notificar("Mensaje de prueba desuscrito");
        assertNull(notificador.getUltimoMensaje());
    }
}
