package com.condominio.service;

import com.condominio.dto.ResidenteDTO;
import com.condominio.modelo.personas.Mascota;
import com.condominio.modelo.personas.Vehiculo;

import java.util.List;

public interface IPersonaService {
    List<ResidenteDTO> listarResidentes();
    void registrarResidente(ResidenteDTO dto);
    void actualizarResidente(ResidenteDTO dto);
    void eliminarResidentePorDni(String dni);
    List<Mascota> listarMascotasPorDepartamento(Long departamentoId);
    List<Vehiculo> listarVehiculosPorDepartamento(Long departamentoId);

    // Verifica si ya existe un residente registrado con el DNI indicado.
    boolean existeResidentePorDni(String dni);

    // Verifica si el departamento indicado ya cuenta con un propietario registrado.
    boolean existePropietarioEnDepartamento(Long departamentoId);
}
