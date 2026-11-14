package com.condominio.gui.util;

import com.condominio.configuracion.ConstantesSistema;

// Constantes centralizadas para la interfaz de usuario de Cobranza Vecinal.
public final class ConstantesUI {

    private ConstantesUI() {
        // Clase de constantes no instanciable
    }

    // Versión oficial consolidada del sistema para el Segundo Avance.
    public static final String VERSION = ConstantesSistema.VERSION;

    // Ciclo académico universitario.
    public static final String CICLO_ACADEMICO = ConstantesSistema.CICLO_ACADEMICO;

    // Cantidad total real de pruebas automatizadas que componen la suite (70 pruebas JUnit 5).
    public static final int TESTS_COUNT = ConstantesSistema.TESTS_COUNT;

    // Badge legado para trazabilidad de consola interna.
    public static final String TESTS_BADGE = TESTS_COUNT + " Tests JUnit 5";

    // Fondo de reserva legal base por defecto del condominio (S/ 15,000.00 base + 10% recaudación).
    public static final String FONDO_RESERVA_DEFECTO = "S/ 15,000.00";

    // Interruptor global del sistema de motion y transiciones visuales.
    public static boolean ANIMACIONES_ACTIVAS = true;

    // Texto unificado para el badge del encabezado superior.
    public static final String ESTADO_SISTEMA_BADGE = "Sistema Operativo | En Línea";

    // Texto unificado para el indicador de estado en la barra inferior.
    public static final String ESTADO_FOOTER = "Servidor Local | BD Operativa | Sincronización en Tiempo Real";

    // Texto de versión completo para la barra de estado inferior.
    public static final String VERSION_FOOTER = VERSION + " | Edición Corporativa";

    // Título principal corporativo de la aplicación.
    public static final String TITULO_APLICACION = ConstantesSistema.TITULO_APLICACION;

    // Subtítulo descriptivo oficial.
    public static final String SUBTITULO_APLICACION = ConstantesSistema.SUBTITULO_APLICACION;

    // Mensaje consolidado para el diálogo institucional Acerca de.
    public static final String MENSAJE_ACERCA_DE = TITULO_APLICACION + " " + VERSION + "\n\n"
            + SUBTITULO_APLICACION + "\n\n"
            + "Plataforma integral orientada a la administración condominal profesional:\n"
            + "- Control y facturación masiva de cuotas ordinarias y extraordinarias.\n"
            + "- Monitoreo analítico de recaudación, saldos deudores y morosidad vecinal.\n"
            + "- Registro y padrón de propietarios, inquilinos, torres e inmuebles.\n"
            + "- Control de acceso en garita, registro de visitas y contratistas.\n"
            + "- Bitácora de incidencias técnicas y reservas de áreas comunes.\n"
            + "- Tablón de comunicados oficiales y circulares a la comunidad.\n"
            + "- Emisión de estados de cuenta individuales y reportes oficiales en PDF.\n\n"
            + "Arquitectura: Spring Boot + Java Swing (Diseño Glassmorphism de Alta Eficiencia)\n"
            + "Base de Datos: Relacional H2 Local Storage con soporte de auditoría transaccional.";
}
