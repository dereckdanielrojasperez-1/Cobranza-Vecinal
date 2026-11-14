
package com.condominio.modelo.financiero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialPago {

    private Long id;
    private LocalDateTime fechaModificacion;
    private String estadoAnterior;
    private String estadoNuevo;
    private CuotaMantenimiento cuota;

    // Pago asociado al cambio de estado.
    private Pago pago;
}
