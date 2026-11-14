
package com.condominio.patrones;

import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.HistorialPago;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.personas.Propietario;
import com.condominio.patrones.facade.CobranzaFachada;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {

    private final CobranzaFachada fachada =
            new CobranzaFachada();

    // DATOS DE PRUEBA

    private Departamento crearDepartamento() {
        return Departamento.builder()
                .id(101L)
                .numero("101")
                .build();
    }

    private CuotaMantenimiento crearCuota(double monto) {
        return fachada.generarCuotaMensual(
                1001L,
                crearDepartamento(),
                monto,
                LocalDate.now().minusDays(10)
        );
    }

    private Propietario crearPropietario() {
        return Propietario.builder()
                .id(1L)
                .nombre("Carlos")
                .build();
    }

    private AreaComun crearAreaComun() {
        return AreaComun.builder()
                .id(1L)
                .nombre("Salon Social")
                .capacidad(30)
                .build();
    }

    // PRUEBA 1: COBRANZA COMPLETA

    @Test
    @DisplayName("Debe generar cuota, calcular mora y registrar pago")
    public void debeEjecutarFlujoCobranzaFachada() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        assertNotNull(cuota);
        assertEquals(1001L, cuota.getId());
        assertEquals("PENDIENTE", cuota.getEstado());

        double penalidad = fachada.calcularMoraCuota(
                cuota,
                LocalDate.now()
        );

        assertEquals(3.0, penalidad, 0.001);
        assertEquals(203.0, cuota.getMontoTotal(), 0.001);
        assertEquals("VENCIDO", cuota.getEstado());

        HistorialPago historial = fachada.registrarPago(
                501L,
                601L,
                cuota,
                203.0,
                "TRANSFERENCIA"
        );

        assertNotNull(historial);
        assertEquals(601L, historial.getId());
        assertEquals("VENCIDO", historial.getEstadoAnterior());
        assertEquals("PAGADO", historial.getEstadoNuevo());
        assertEquals("PAGADO", cuota.getEstado());
        assertEquals(0.0, cuota.getMontoTotal(), 0.001);

        // Verificar que se guardo el pago.
        assertNotNull(historial.getPago());
        assertEquals(501L, historial.getPago().getId());
        assertEquals(
                203.0,
                historial.getPago().getMonto(),
                0.001
        );
        assertEquals(
                "TRANSFERENCIA",
                historial.getPago().getMetodoPago()
        );
        assertEquals(
                LocalDate.now(),
                historial.getPago().getFechaPago()
        );
        assertEquals(
                cuota.getDepartamento(),
                historial.getPago().getDepartamento()
        );
    }

    // PRUEBA 2: RESERVA Y CAPACIDAD

    @Test
    @DisplayName("Debe confirmar reserva y validar capacidad")
    public void debeGestionarReservasFachada() {

        Propietario propietario = crearPropietario();
        AreaComun area = crearAreaComun();

        LocalDate fecha = LocalDate.now().plusDays(5);

        ReservaArea reserva = fachada.solicitarReserva(
                701L,
                propietario,
                area,
                fecha,
                LocalTime.of(18, 0),
                LocalTime.of(22, 0),
                20
        );

        assertNotNull(reserva);
        assertEquals("CONFIRMADA", reserva.getEstado());

        assertThrows(IllegalStateException.class, () ->
                fachada.solicitarReserva(
                        702L,
                        propietario,
                        area,
                        fecha,
                        LocalTime.of(18, 0),
                        LocalTime.of(22, 0),
                        45
                )
        );
    }

    // PRUEBA 3: PAGO PARCIAL

    @Test
    @DisplayName("Debe registrar un pago parcial")
    public void debeRegistrarPagoParcial() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        HistorialPago historial = fachada.registrarPago(
                502L,
                602L,
                cuota,
                100.0,
                "EFECTIVO"
        );

        assertNotNull(historial);
        assertEquals("PARCIAL", cuota.getEstado());
        assertEquals(100.0, cuota.getMontoTotal(), 0.001);
        assertEquals("PENDIENTE", historial.getEstadoAnterior());
        assertEquals("PARCIAL", historial.getEstadoNuevo());

        // Verificar los datos del pago parcial.
        assertNotNull(historial.getPago());
        assertEquals(502L, historial.getPago().getId());
        assertEquals(
                100.0,
                historial.getPago().getMonto(),
                0.001
        );
        assertEquals(
                "EFECTIVO",
                historial.getPago().getMetodoPago()
        );
    }

    // PRUEBA 4: PAGO EXCESIVO

    @Test
    @DisplayName("Debe rechazar pagos mayores a la deuda")
    public void debeRechazarPagoExcesivo() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        assertThrows(IllegalArgumentException.class, () ->
                fachada.registrarPago(
                        503L,
                        603L,
                        cuota,
                        250.0,
                        "EFECTIVO"
                )
        );

        assertEquals("PENDIENTE", cuota.getEstado());
        assertEquals(200.0, cuota.getMontoTotal(), 0.001);
    }

    // PRUEBA 5: PAGO NEGATIVO

    @Test
    @DisplayName("Debe rechazar pagos negativos")
    public void debeRechazarPagoNegativo() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        assertThrows(IllegalArgumentException.class, () ->
                fachada.registrarPago(
                        504L,
                        604L,
                        cuota,
                        -50.0,
                        "EFECTIVO"
                )
        );
    }

    // PRUEBA 6: PAGO DE CERO

    @Test
    @DisplayName("Debe rechazar pagos de cero soles")
    public void debeRechazarPagoCero() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        assertThrows(IllegalArgumentException.class, () ->
                fachada.registrarPago(
                        505L,
                        605L,
                        cuota,
                        0.0,
                        "EFECTIVO"
                )
        );
    }

    // PRUEBA 7: PAGO DUPLICADO

    @Test
    @DisplayName("No debe permitir pagar una cuota ya pagada")
    public void debeRechazarPagoDuplicado() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        fachada.registrarPago(
                506L,
                606L,
                cuota,
                200.0,
                "TRANSFERENCIA"
        );

        assertEquals("PAGADO", cuota.getEstado());

        assertThrows(IllegalStateException.class, () ->
                fachada.registrarPago(
                        507L,
                        607L,
                        cuota,
                        50.0,
                        "EFECTIVO"
                )
        );
    }

    // PRUEBA 8: MORA DUPLICADA

    @Test
    @DisplayName("No debe aplicar dos veces la mora")
    public void noDebeDuplicarMora() {

        CuotaMantenimiento cuota = crearCuota(200.0);

        double primeraMora = fachada.calcularMoraCuota(
                cuota,
                LocalDate.now()
        );

        double segundaMora = fachada.calcularMoraCuota(
                cuota,
                LocalDate.now()
        );

        assertEquals(3.0, primeraMora, 0.001);
        assertEquals(0.0, segundaMora, 0.001);
        assertEquals(203.0, cuota.getMontoTotal(), 0.001);
        assertEquals("VENCIDO", cuota.getEstado());
    }

    // PRUEBA 9: RESERVA EN FECHA PASADA

    @Test
    @DisplayName("Debe rechazar reservas en fechas pasadas")
    public void debeRechazarReservaFechaPasada() {

        assertThrows(IllegalArgumentException.class, () ->
                fachada.solicitarReserva(
                        703L,
                        crearPropietario(),
                        crearAreaComun(),
                        LocalDate.now().minusDays(1),
                        LocalTime.of(18, 0),
                        LocalTime.of(22, 0),
                        10
                )
        );
    }

    // PRUEBA 10: HORARIO INVALIDO

    @Test
    @DisplayName("Debe rechazar horarios incorrectos")
    public void debeRechazarHorarioInvalido() {

        assertThrows(IllegalArgumentException.class, () ->
                fachada.solicitarReserva(
                        704L,
                        crearPropietario(),
                        crearAreaComun(),
                        LocalDate.now().plusDays(5),
                        LocalTime.of(22, 0),
                        LocalTime.of(18, 0),
                        10
                )
        );
    }

    // PRUEBA 11: RESERVA SIN ASISTENTES

    @Test
    @DisplayName("Debe rechazar reservas sin personas")
    public void debeRechazarReservaSinPersonas() {

        assertThrows(IllegalArgumentException.class, () ->
                fachada.solicitarReserva(
                        705L,
                        crearPropietario(),
                        crearAreaComun(),
                        LocalDate.now().plusDays(5),
                        LocalTime.of(18, 0),
                        LocalTime.of(22, 0),
                        0
                )
        );
    }

    // PRUEBA 12: FINALIZAR RESERVA Y LIBERAR AREA

    @Test
    @DisplayName("Debe finalizar reserva y liberar area para nuevas solicitudes")
    public void debeFinalizarReservaYLiberarAreaParaNuevasSolicitudes() {
        Propietario prop = crearPropietario();
        AreaComun area = crearAreaComun();
        LocalDate fecha = LocalDate.now().plusDays(3);

        ReservaArea res = fachada.solicitarReserva(
                880L,
                prop,
                area,
                fecha,
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                10
        );
        assertNotNull(res);
        assertEquals("CONFIRMADA", res.getEstado());

        boolean ok = fachada.finalizarReserva(880L);
        assertTrue(ok);

        boolean solapado = fachada.existeSolapamientoReserva(area.getNombre(), fecha, LocalTime.of(15, 0), LocalTime.of(17, 0));
        assertFalse(solapado, "El area debe quedar liberada y disponible al finalizar la reserva");
    }
}
