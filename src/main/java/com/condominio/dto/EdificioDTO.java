package com.condominio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Objeto de transferencia de datos para presentar la información agregada
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdificioDTO {
    private Long id;
    private String nombre;
    private Integer numeroPisos;
    private Integer cantidadTorres;
    private Integer cantidadDepartamentos;
    private Double areaTotalConstruida;
    private String nombresTorres;
}
