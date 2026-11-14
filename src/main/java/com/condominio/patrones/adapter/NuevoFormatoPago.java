package com.condominio.patrones.adapter;

import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.Departamento;

// Interfaz del sistema actual para registro estandarizado de pagos (Semana 8)
public interface NuevoFormatoPago {
    Pago procesarPago(Long pagoId, Departamento departamento, Double monto, String metodoPago);
}
