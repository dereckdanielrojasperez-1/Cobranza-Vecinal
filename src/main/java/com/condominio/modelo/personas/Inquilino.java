package com.condominio.modelo.personas;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

// Entidad de dominio que representa a un Inquilino temporal o arrendatario.
@Entity
@Table(name = "inquilino", schema = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Inquilino extends Usuario {

    private LocalDate fechaInicioContrato;
    private LocalDate fechaFinContrato;
}
