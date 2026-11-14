package com.condominio.exception;

// Excepción base de negocio para el dominio de Cobranza Vecinal.
public class BusinessException extends RuntimeException {

    private final String codigoError;

    public BusinessException(String mensaje) {
        super(mensaje);
        this.codigoError = "BUSINESS_ERROR";
    }

    public BusinessException(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public BusinessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = "BUSINESS_ERROR";
    }

    public String getCodigoError() {
        return codigoError;
    }
}
