package com.condominio.patrones.templatemethod;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

// Resultado estructurado del procesamiento algorítmico definido por el Patrón Template Method.
@Getter
@AllArgsConstructor
@ToString
public class ResultadoCobranza {

    private final Long idCuota;
    private final String tipoCobranza;
    private final double montoBase;
    private final double montoFinalAjustado;
    private final String codigoComprobante;
    private final String estadoOperacion;
}
