package com.condominio.modelo.financiero;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// Entidad de dominio para gastos comunes derivados del consumo de energia electrica.
@Entity
@Table(name = "gasto_luz", schema = "financiero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GastoLuz extends GastoComun {

    private Double consumoKwh;
    private Double tarifaPorKwh;

    public Double getCostoCalculado() {
        if (consumoKwh != null && tarifaPorKwh != null) {
            return consumoKwh * tarifaPorKwh;
        }
        return 0.0;
    }
}
