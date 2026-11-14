package com.condominio.exception;

public class CuotaNoEncontradaException extends BusinessException {
    public CuotaNoEncontradaException(Long idCuota) {
        super("CUOTA_NO_ENCONTRADA", "No se encontró la cuota de mantenimiento con ID: " + idCuota);
    }

    public CuotaNoEncontradaException(String mensaje) {
        super("CUOTA_NO_ENCONTRADA", mensaje);
    }
}
