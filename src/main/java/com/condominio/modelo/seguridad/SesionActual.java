package com.condominio.modelo.seguridad;

// Objeto de sesión en memoria para mantener el usuario y rol activos en la ejecución.
public final class SesionActual {

    private static UsuarioSistema usuarioActivo;

    private SesionActual() {
    }

    public static synchronized void iniciarSesion(UsuarioSistema usuario) {
        usuarioActivo = usuario;
    }

    public static synchronized void cerrarSesion() {
        usuarioActivo = null;
    }

    public static synchronized boolean estaAutenticado() {
        return usuarioActivo != null;
    }

    public static synchronized UsuarioSistema getUsuarioActivo() {
        return usuarioActivo;
    }

    public static synchronized String getNombreUsuario() {
        return usuarioActivo != null ? usuarioActivo.getUsuario() : "Invitado";
    }

    public static synchronized RolUsuario getRol() {
        return usuarioActivo != null ? usuarioActivo.getRol() : RolUsuario.RESIDENTE;
    }

    public static synchronized boolean esAdministrador() {
        return usuarioActivo != null && usuarioActivo.getRol() == RolUsuario.ADMINISTRADOR;
    }

    public static synchronized boolean esPresidente() {
        return usuarioActivo != null && usuarioActivo.getRol() == RolUsuario.PRESIDENTE;
    }

    public static synchronized boolean esGerente() {
        return usuarioActivo != null && usuarioActivo.getRol() == RolUsuario.GERENTE;
    }

    public static synchronized boolean esConserje() {
        return usuarioActivo != null && (usuarioActivo.getRol() == RolUsuario.CONSERJE || usuarioActivo.getRol() == RolUsuario.GERENTE);
    }

    public static synchronized boolean esResidente() {
        return usuarioActivo != null && (usuarioActivo.getRol() == RolUsuario.RESIDENTE || usuarioActivo.getRol() == RolUsuario.PRESIDENTE);
    }

    public static synchronized boolean puedeExportarChatbot() {
        return esAdministrador();
    }

    public static synchronized boolean puedeGenerarCuotas() {
        return esAdministrador();
    }

    public static synchronized boolean puedeDeshacerPagos() {
        return esAdministrador();
    }

    public static synchronized boolean puedeGenerarReportesPdf() {
        return esAdministrador();
    }

    public static synchronized boolean puedeExportarCsv() {
        return esAdministrador();
    }

    public static synchronized boolean puedeModificarInmuebles() {
        return esAdministrador();
    }

    public static synchronized boolean puedeRegistrarPagos() {
        return esAdministrador();
    }
}
