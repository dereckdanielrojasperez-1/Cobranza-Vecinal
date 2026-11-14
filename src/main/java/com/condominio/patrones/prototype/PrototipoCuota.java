package com.condominio.patrones.prototype;

// Interfaz prototipo para duplicacion de cuotas base (Patron Prototype - Semana 6)
public interface PrototipoCuota extends Cloneable {
    PrototipoCuota clonar();
}
