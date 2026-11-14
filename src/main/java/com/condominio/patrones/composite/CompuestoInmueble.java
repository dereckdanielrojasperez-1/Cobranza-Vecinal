package com.condominio.patrones.composite;

import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Elemento compuesto que agrupa subcomponentes y calcula agregados recursivamente (Semana 9)
@Getter
public class CompuestoInmueble implements ComponenteInmueble {

    private final String nombre;
    private final List<ComponenteInmueble> hijos = new ArrayList<>();

    public CompuestoInmueble(String nombre) {
        this.nombre = nombre;
    }

    public void agregarHijo(ComponenteInmueble hijo) {
        if (hijo != null && !hijos.contains(hijo)) {
            hijos.add(hijo);
        }
    }

    public void removerHijo(ComponenteInmueble hijo) {
        hijos.remove(hijo);
    }

    @Override
    public double calcularAreaTotal() {
        double areaTotal = 0.0;
        for (ComponenteInmueble hijo : hijos) {
            areaTotal += hijo.calcularAreaTotal();
        }
        return areaTotal;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    public List<ComponenteInmueble> getHijos() {
        return Collections.unmodifiableList(hijos);
    }
}
