package com.condominio.patrones.templatemethod;

import com.condominio.modelo.seguridad.UsuarioSistema;

// Clase base abstracta del Patrón de Comportamiento Template Method GoF.
public abstract class PlantillaProcesoCobranza {

    // Método Plantilla (Template Method).
    public final ResultadoCobranza procesarCobranza(Long idCuota, double montoBase, UsuarioSistema usuario) {
        // 1. Paso común invariable: Validación de entrada
        validarParametros(idCuota, montoBase, usuario);

        // 2. Paso primitivo abstracto: Cálculo de ajustes (descuento por pronto pago o recargo)
        double montoFinal = calcularMontoAjustado(montoBase);

        // 3. Paso primitivo abstracto: Emisión y numeración del comprobante oficial
        String codigoComprobante = generarComprobante(idCuota, montoFinal);

        // 4. Paso gancho (Hook): Registro en el libro diario contable
        registrarEnLibroDiario(idCuota, montoFinal, codigoComprobante);

        // 5. Paso gancho (Hook): Notificación al residente
        enviarAvisoResidente(usuario, idCuota, montoFinal, codigoComprobante);

        return new ResultadoCobranza(
                idCuota,
                obtenerTipoCobranza(),
                montoBase,
                montoFinal,
                codigoComprobante,
                "LIQUIDADA_CON_EXITO"
        );
    }

    private void validarParametros(Long idCuota, double montoBase, UsuarioSistema usuario) {
        if (idCuota == null || idCuota <= 0) {
            throw new IllegalArgumentException("El identificador de la cuota debe ser un número positivo");
        }
        if (montoBase <= 0) {
            throw new IllegalArgumentException("El monto base de la cobranza debe ser estrictamente mayor a cero");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Debe indicarse el usuario responsable de procesar la cobranza");
        }
    }

    // Paso primitivo: Nombre descriptivo del tipo de cobranza.
    public abstract String obtenerTipoCobranza();

    // Paso primitivo: Algoritmo especializado para determinar el monto final ajustado.
    protected abstract double calcularMontoAjustado(double montoBase);

    // Paso primitivo: Formato y serie de comprobante (ordinario vs extraordinario).
    protected abstract String generarComprobante(Long idCuota, double montoAjustado);

    // Paso gancho (Hook): Registro contable con implementación por defecto.
    protected void registrarEnLibroDiario(Long idCuota, double montoAjustado, String codigoComprobante) {
        System.out.println("LIBRO DIARIO: Asiento contable registrado para cuota " + idCuota
                + " por S/ " + String.format(java.util.Locale.US, "%.2f", montoAjustado)
                + " con comprobante " + codigoComprobante);
    }

    // Paso gancho (Hook): Notificación al residente.
    protected void enviarAvisoResidente(UsuarioSistema usuario, Long idCuota, double montoAjustado, String codigoComprobante) {
        System.out.println("NOTIFICACIÓN TEMPLATE METHOD: Cuota " + idCuota
                + " liquidada satisfactoriamente para " + usuario.getUsuario());
    }
}
