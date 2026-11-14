package com.condominio.exception;

public class DepartamentoDuplicadoException extends BusinessException {
    public DepartamentoDuplicadoException(String numero) {
        super("DEPARTAMENTO_DUPLICADO", "Ya existe un departamento registrado con el número: " + numero);
    }

    public DepartamentoDuplicadoException(String numero, String edificio) {
        super("DEPARTAMENTO_DUPLICADO", String.format("El departamento '%s' ya existe registrado en el edificio '%s'.", numero, edificio));
    }

    public DepartamentoDuplicadoException(String numero, String torre, String edificio) {
        super("DEPARTAMENTO_DUPLICADO", String.format("El departamento '%s' ya existe registrado en la %s del edificio '%s'.", numero, torre, edificio));
    }
}
