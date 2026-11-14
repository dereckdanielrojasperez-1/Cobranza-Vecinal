package com.condominio.service;

import com.condominio.dto.InmuebleDTO;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;

import java.util.List;

public interface IInmuebleService {
    List<InmuebleDTO> listarInmuebles();
    void registrarDepartamento(InmuebleDTO dto);
    List<Edificio> listarEdificios();
    List<Torre> listarTorres();
    List<AreaComun> listarAreasComunes();
    AreaComun registrarAreaComun(AreaComun area);

    // Verifica si ya existe un departamento con el mismo numero dentro de la torre y edificio especificados.
    boolean existeDepartamentoPorNumeroTorreYEdificio(String numero, String nombreTorre, String nombreEdificio);
}
