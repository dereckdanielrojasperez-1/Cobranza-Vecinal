package com.condominio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Objeto de transferencia de datos para la gestion de departamentos e inmuebles.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InmuebleDTO {

    private Long id;

    @NotBlank(message = "El número de departamento es obligatorio")
    private String numero;

    @NotNull(message = "El piso es obligatorio")
    @PositiveOrZero(message = "El piso no puede ser negativo")
    private Integer piso;

    @NotNull(message = "Los metros cuadrados son obligatorios")
    @Positive(message = "Los metros cuadrados deben ser positivos")
    private Double metrosCuadrados;

    @NotBlank(message = "El nombre de la torre es obligatorio")
    private String nombreTorre;

    @NotBlank(message = "El nombre del edificio es obligatorio")
    private String nombreEdificio;
}
