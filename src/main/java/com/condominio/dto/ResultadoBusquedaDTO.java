package com.condominio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que encapsula un resultado de búsqueda polimórfico en el sistema,
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoBusquedaDTO {
    private String id;
    private String categoria;
    private String titulo;
    private String subtitulo;
    private String fecha;
    private String estado;
    private String detalleCompleto;
    private String moduloDestino;
}
