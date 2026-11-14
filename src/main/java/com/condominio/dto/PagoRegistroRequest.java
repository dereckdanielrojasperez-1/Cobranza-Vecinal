package com.condominio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro y amortización de pago para una cuota")
public class PagoRegistroRequest {

    @NotNull(message = "El identificador de cuota (idCuota) es obligatorio")
    @Positive(message = "El idCuota debe ser un número positivo")
    @Schema(description = "Identificador de la cuota de mantenimiento", example = "1")
    private Long idCuota;

    @NotNull(message = "El monto monetario es obligatorio")
    @Positive(message = "El monto a abonar debe ser estrictamente mayor a cero")
    @Schema(description = "Monto monetario a abonar (S/)", example = "150.00")
    private Double monto;

    @Schema(description = "Código de operación bancaria o referencia de transferencia", example = "OP-98745")
    private String codigoOperacion;
}
