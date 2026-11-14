package com.condominio.patrones.abstractfactory;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;

// Fabrica concreta para la familia de inmuebles comerciales o mixtos (Semana 7)
public class FabricaComercial implements FabricaInmueble {

    @Override
    public Edificio crearEdificio(Long id, String nombre, Integer numeroPisos, Condominio condominio) {
        return Edificio.builder()
                .id(id)
                .nombre("Centro Empresarial " + nombre)
                .numeroPisos(numeroPisos)
                .condominio(condominio)
                .build();
    }

    @Override
    public Departamento crearDepartamento(Long id, String numero, Integer piso, Double metrosCuadrados) {
        return Departamento.builder()
                .id(id)
                .numero("OFICINA-" + numero)
                .piso(piso)
                .metrosCuadrados(metrosCuadrados)
                .numeroHabitantes(0)
                .build();
    }

    @Override
    public AreaComun crearAreaComun(Long id, String nombre, Integer capacidad, Double costoReserva, Condominio condominio) {
        return AreaComun.builder()
                .id(id)
                .nombre("Sala de Conferencias: " + nombre)
                .capacidad(capacidad)
                .costoReserva(costoReserva)
                .condominio(condominio)
                .build();
    }
}
