package com.condominio.patrones.adapter;

import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;
import java.time.LocalDate;

// Adaptador que convierte llamadas del nuevo formato hacia el sistema legado (Semana 8)
public class AdaptadorSistemaLegado implements NuevoFormatoPago {

    private final SistemaLegado sistemaLegado;

    public AdaptadorSistemaLegado(SistemaLegado sistemaLegado) {
        if (sistemaLegado == null) {
            throw new IllegalArgumentException("El sistema legado no puede ser nulo");
        }
        this.sistemaLegado = sistemaLegado;
    }

    @Override
    public Pago procesarPago(Long pagoId, Departamento departamento, Double monto, String metodoPago) {
        String codigoInmueble = (departamento != null && departamento.getNumero() != null)
                ? departamento.getNumero()
                : "DEPTO-DESCONOCIDO";

        String fechaActual = LocalDate.now().toString();

        // Invoca el metodo del sistema antiguo adaptando tipos y estructura
        String respuestaLegado = sistemaLegado.procesarTransaccionAntigua(codigoInmueble, monto, fechaActual);

        // Construye y retorna la entidad Pago del dominio actual
        return Pago.builder()
                .id(pagoId)
                .monto(monto)
                .fechaPago(LocalDate.now())
                .metodoPago(metodoPago + " (ADAPTADO: " + respuestaLegado + ")")
                .departamento(departamento)
                .build();
    }
}
