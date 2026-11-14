package com.condominio.modelo.personas;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// Entidad de dominio que representa a un Propietario de unidad inmobiliaria.
@Entity
@Table(name = "propietario", schema = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Propietario extends Usuario {

    private String banco;
    private String cuentaBancaria;
}
