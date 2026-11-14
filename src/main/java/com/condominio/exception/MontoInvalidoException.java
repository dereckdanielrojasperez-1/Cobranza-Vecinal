package com.condominio.exception;

public class MontoInvalidoException extends BusinessException {
    public MontoInvalidoException(String mensaje) {
        super("MONTO_INVALIDO", mensaje);
    }

    public MontoInvalidoException(double monto, String motivo) {
        super("MONTO_INVALIDO", String.format("Monto inválido (S/ %.2f): %s", monto, motivo));
    }
}
