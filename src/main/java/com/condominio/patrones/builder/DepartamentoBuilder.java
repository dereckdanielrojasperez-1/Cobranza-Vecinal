package com.condominio.patrones.builder;

import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.modelo.personas.Inquilino;
import com.condominio.modelo.personas.Propietario;

// Constructor paso a paso para entidades Departamento (Patron Builder - Semana 7)
public class DepartamentoBuilder {

    private Long id;
    private String numero;
    private Integer piso;
    private Double metrosCuadrados;
    private Integer numeroHabitantes;
    private Torre torre;
    private Propietario propietario;
    private Inquilino inquilinoActual;

    public DepartamentoBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public DepartamentoBuilder conNumero(String numero) {
        this.numero = numero;
        return this;
    }

    public DepartamentoBuilder conPiso(Integer piso) {
        this.piso = piso;
        return this;
    }

    public DepartamentoBuilder conMetrosCuadrados(Double metrosCuadrados) {
        this.metrosCuadrados = metrosCuadrados;
        return this;
    }

    public DepartamentoBuilder conNumeroHabitantes(Integer numeroHabitantes) {
        this.numeroHabitantes = numeroHabitantes;
        return this;
    }

    public DepartamentoBuilder conTorre(Torre torre) {
        this.torre = torre;
        return this;
    }

    public DepartamentoBuilder conPropietario(Propietario propietario) {
        this.propietario = propietario;
        return this;
    }

    public DepartamentoBuilder conInquilinoActual(Inquilino inquilinoActual) {
        this.inquilinoActual = inquilinoActual;
        return this;
    }

    public Departamento build() {
        return Departamento.builder()
                .id(this.id)
                .numero(this.numero)
                .piso(this.piso)
                .metrosCuadrados(this.metrosCuadrados)
                .numeroHabitantes(this.numeroHabitantes)
                .torre(this.torre)
                .propietario(this.propietario)
                .inquilinoActual(this.inquilinoActual)
                .build();
    }
}
