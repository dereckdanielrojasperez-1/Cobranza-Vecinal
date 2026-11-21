package com.condominio.patrones.proxy;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;

import java.util.HashMap;
import java.util.Map;

// Patrón Estructural Proxy GoF (Proxy de Protección y Caché).
public class ProxyAccesoCobranza implements ServicioAccesoCobranza {

    private final ServicioAccesoCobranza servicioReal;
    private final Map<String, String> cacheReportes = new HashMap<>();

    public ProxyAccesoCobranza(ServicioAccesoCobranza servicioReal) {
        this.servicioReal = (servicioReal != null) ? servicioReal : new ServicioAccesoCobranzaReal();
    }

    public ProxyAccesoCobranza() {
        this(new ServicioAccesoCobranzaReal());
    }

    @Override
    public String consultarReporteFinanciero(UsuarioSistema usuario, String periodo) {
        validarSesion(usuario);

        // Clave de caché aislada por rol y usuario para evitar filtraciones de datos entre perfiles
        String claveCache = (usuario.getRol() == RolUsuario.RESIDENTE)
                ? periodo + "_RESIDENTE_" + usuario.getUsuario()
                : periodo + "_ADMINISTRATIVO";

        if (cacheReportes.containsKey(claveCache)) {
            return "[DESDE CACHÉ PROXY]\n" + cacheReportes.get(claveCache);
        }

        String resultado;
        // Si el usuario es Residente, se restringe la auditoría a su nivel individual
        if (usuario.getRol() == RolUsuario.RESIDENTE) {
            resultado = "ESTADO DE CUENTA RESIDENTE [" + periodo + "]\n"
                    + "- Residente: " + usuario.getUsuario() + "\n"
                    + "- Cuota Mantenimiento: Al día (S/ 0.00 pendiente)\n"
                    + "- Nota: Para ver el balance global, solicite autorización a Administración.";
        } else {
            // Administrador o Conserje: delega al servicio real
            resultado = servicioReal.consultarReporteFinanciero(usuario, periodo);
        }

        cacheReportes.put(claveCache, resultado);
        return resultado;
    }

    @Override
    public boolean ejecutarCierreMensual(UsuarioSistema usuario, String periodo) {
        validarSesion(usuario);

        if (usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new SecurityException("Acceso denegado: El usuario '" + usuario.getUsuario()
                    + "' con rol " + usuario.getRol() + " no posee privilegios para ejecutar el cierre mensual.");
        }

        // Invalida la caché administrativa del periodo cerrado para obligar a nueva conciliación
        cacheReportes.remove(periodo + "_ADMINISTRATIVO");
        return servicioReal.ejecutarCierreMensual(usuario, periodo);
    }

    @Override
    public int getContadorEjecucionesReales() {
        return servicioReal.getContadorEjecucionesReales();
    }

    public int getCantidadElementosEnCache() {
        return cacheReportes.size();
    }

    public void limpiarCache() {
        cacheReportes.clear();
    }

    private void validarSesion(UsuarioSistema usuario) {
        if (usuario == null) {
            throw new SecurityException("Acceso denegado: No se ha suministrado una identidad de usuario válida en el sistema.");
        }
    }
}
