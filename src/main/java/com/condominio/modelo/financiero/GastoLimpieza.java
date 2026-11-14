package com.condominio.modelo.financiero;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// Entidad de dominio para gastos comunes de servicios de limpieza y mantenimiento higienico.
@Entity
@Table(name = "gasto_limpieza", schema = "financiero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GastoLimpieza extends GastoComun {

    private String contratoEmpresa;
    private Integer personalAsignado;
}
