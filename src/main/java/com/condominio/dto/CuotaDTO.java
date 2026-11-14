package com.condominio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Objeto de transferencia de datos (DTO) para la exposicion de cuotas de mantenimiento
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaDTO {

    @NotNull(message = "El ID de la cuota no puede ser nulo")
    private Long idCuota;

    private String numeroDepartamento;

    @NotNull(message = "El monto base es obligatorio")
    @Positive(message = "El monto base debe ser positivo")
    private Double montoBase;

    private Double montoFinal;

    @NotBlank(message = "El estado de la cuota es obligatorio")
    private String estado;

    private String fechaVencimiento;
}
