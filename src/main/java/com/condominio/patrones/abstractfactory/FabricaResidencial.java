package com.condominio.patrones.abstractfactory;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;

// Fabrica concreta para la familia de inmuebles residenciales (Semana 7)
public class FabricaResidencial implements FabricaInmueble {

    @Override
    public Edificio crearEdificio(Long id, String nombre, Integer numeroPisos, Condominio condominio) {
        return Edificio.builder()
                .id(id)
                .nombre("Torre Residencial " + nombre)
                .numeroPisos(numeroPisos)
                .condominio(condominio)
                .build();
    }

    @Override
    public Departamento crearDepartamento(Long id, String numero, Integer piso, Double metrosCuadrados) {
        return Departamento.builder()
                .id(id)
                .numero(numero)
                .piso(piso)
                .metrosCuadrados(metrosCuadrados)
                .numeroHabitantes(3)
                .build();
    }

    @Override
    public AreaComun crearAreaComun(Long id, String nombre, Integer capacidad, Double costoReserva, Condominio condominio) {
        return AreaComun.builder()
                .id(id)
                .nombre("Zona Social: " + nombre)
                .capacidad(capacidad)
                .costoReserva(costoReserva)
                .condominio(condominio)
                .build();
    }
}
