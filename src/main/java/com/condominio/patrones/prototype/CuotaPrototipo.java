package com.condominio.patrones.prototype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Implementacion concreta del patron Prototype para cuotas base (Semana 6)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaPrototipo implements PrototipoCuota {

    private Long id;
    private Double montoBase;
    private String concepto;
    private String mesPeriodo;
    private String estado;

    @Override
    public CuotaPrototipo clonar() {
        return CuotaPrototipo.builder()
                .id(this.id)
                .montoBase(this.montoBase)
                .concepto(this.concepto)
                .mesPeriodo(this.mesPeriodo)
                .estado(this.estado)
                .build();
    }
}
