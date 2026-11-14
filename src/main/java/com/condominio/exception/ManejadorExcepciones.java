package com.condominio.exception;

// Utilidad desacoplada para la traducción y saneamiento de excepciones hacia la interfaz gráfica (Swing)
public final class ManejadorExcepciones {

    private ManejadorExcepciones() {
    }

    // Extrae un mensaje amigable, comprensible y limpio a partir de cualquier Throwable o cadena causal.
    public static String obtenerMensajeAmigable(Throwable ex) {
        if (ex == null) {
            return "Ha ocurrido una operación no válida en el sistema.";
        }

        Throwable actual = ex;
        while (actual.getCause() != null && actual.getCause() != actual) {
            if (actual instanceof BusinessException) {
                break;
            }
            actual = actual.getCause();
        }

        if (actual instanceof BusinessException be) {
            return be.getMessage();
        }

        String mensaje = actual.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return "Error de procesamiento interno en la operación solicitada.";
        }

        // Filtra prefijos técnicos comunes de Java / Spring / JPA
        if (mensaje.contains("could not execute statement") || mensaje.contains("ConstraintViolation")) {
            return "Error de integridad de datos: el registro posee dependencias o restricciones activas.";
        }
        if (mensaje.contains("Connection refused") || mensaje.contains("JDBC")) {
            return "Error de comunicación con la base de datos.";
        }

        return mensaje;
    }
}
