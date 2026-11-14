package com.condominio.service;

import com.condominio.dto.ResultadoBusquedaDTO;
import com.condominio.modelo.seguridad.RolUsuario;

import java.util.List;

// Interfaz para el servicio especializado de busqueda global polimorfica y segmentada por RBAC
public interface IBusquedaGlobalService {

    // Ejecuta la busqueda global en el sistema aplicando filtros segun el rol y departamento del usuario
    List<ResultadoBusquedaDTO> buscarEnSistema(String termino, RolUsuario rol, String deptoResidente);

    // Sobrecarga de conveniencia para busqueda con parametros por defecto
    default List<ResultadoBusquedaDTO> buscarEnSistema(String termino) {
        return buscarEnSistema(termino, RolUsuario.ADMINISTRADOR, "101");
    }
}
