package com.condominio.patrones.adapter;

// Interfaz del sistema legado con formato antiguo de pagos (Semana 8)
public interface SistemaLegado {
    String procesarTransaccionAntigua(String codigoInmueble, double valorMonetario, String fechaTextoIso);
}
