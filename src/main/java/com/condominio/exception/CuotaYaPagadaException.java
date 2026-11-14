package com.condominio.exception;

public class CuotaYaPagadaException extends BusinessException {
    public CuotaYaPagadaException(Long idCuota) {
        super("CUOTA_YA_PAGADA", "La cuota con ID " + idCuota + " ya se encuentra totalmente cancelada (saldo 0.00).");
    }

    public CuotaYaPagadaException(String mensaje) {
        super("CUOTA_YA_PAGADA", mensaje);
    }
}
