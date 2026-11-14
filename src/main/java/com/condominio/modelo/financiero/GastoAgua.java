package com.condominio.modelo.financiero;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// Entidad de dominio para gastos comunes derivados del consumo de agua potable.
@Entity
@Table(name = "gasto_agua", schema = "financiero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GastoAgua extends GastoComun {

    private Double lecturaActual;
    private Double lecturaAnterior;

    public Double getConsumoTotal() {
        if (lecturaActual != null && lecturaAnterior != null) {
            return Math.max(0.0, lecturaActual - lecturaAnterior);
        }
        return 0.0;
    }
}
