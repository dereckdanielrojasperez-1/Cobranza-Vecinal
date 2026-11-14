package com.condominio.patrones.composite;

import lombok.Getter;

// Elemento hoja que representa una unidad terminal sin subdivisiones (Semana 9)
@Getter
public class HojaInmueble implements ComponenteInmueble {

    private final String nombre;
    private final double metrosCuadrados;

    public HojaInmueble(String nombre, double metrosCuadrados) {
        if (metrosCuadrados < 0) {
            throw new IllegalArgumentException("El area no puede ser negativa");
        }
        this.nombre = nombre;
        this.metrosCuadrados = metrosCuadrados;
    }

    @Override
    public double calcularAreaTotal() {
        return metrosCuadrados;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }
}
