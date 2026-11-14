package com.condominio.patrones.abstractfactory;

import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;

// Interfaz fabrica abstracta para creacion de familias de inmuebles (Semana 7)
public interface FabricaInmueble {
    Edificio crearEdificio(Long id, String nombre, Integer numeroPisos, Condominio condominio);
    Departamento crearDepartamento(Long id, String numero, Integer piso, Double metrosCuadrados);
    AreaComun crearAreaComun(Long id, String nombre, Integer capacidad, Double costoReserva, Condominio condominio);
}
