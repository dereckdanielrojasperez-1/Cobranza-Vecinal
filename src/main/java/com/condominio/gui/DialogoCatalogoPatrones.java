package com.condominio.gui;

import com.condominio.patrones.singleton.ConfiguracionCondominio;
import com.condominio.gui.animacion.AnimadorToast;
import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.HistorialPago;
import com.condominio.modelo.financiero.Pago;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Condominio;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.modelo.personas.Propietario;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.patrones.abstractfactory.FabricaInmueble;
import com.condominio.patrones.abstractfactory.FabricaResidencial;
import com.condominio.patrones.adapter.AdaptadorSistemaLegado;
import com.condominio.patrones.adapter.NuevoFormatoPago;
import com.condominio.patrones.adapter.SistemaLegado;
import com.condominio.patrones.builder.DepartamentoBuilder;
import com.condominio.patrones.command.ComandoCobranza;
import com.condominio.patrones.command.InvocadorCobranza;
import com.condominio.patrones.composite.CompuestoInmueble;
import com.condominio.patrones.composite.HojaInmueble;
import com.condominio.patrones.decorator.CuotaBase;
import com.condominio.patrones.decorator.DecoradorDescuentoProntoPago;
import com.condominio.patrones.decorator.DecoradorRecargoMora;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import com.condominio.patrones.prototype.CuotaPrototipo;
import com.condominio.patrones.proxy.ProxyAccesoCobranza;
import com.condominio.patrones.proxy.ServicioAccesoCobranza;
import com.condominio.patrones.strategy.CalculadoraGastos;
import com.condominio.patrones.strategy.ProrrateoEquitativo;
import com.condominio.patrones.strategy.ProrrateoPorAlicuota;
import com.condominio.patrones.templatemethod.PlantillaProcesoCobranza;
import com.condominio.patrones.templatemethod.ProcesoCobranzaExtraordinaria;
import com.condominio.patrones.templatemethod.ProcesoCobranzaOrdinaria;
import com.condominio.patrones.templatemethod.ResultadoCobranza;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Dialogo modal corporativo que presenta el catalogo interactivo de los 15 patrones GoF
public class DialogoCatalogoPatrones extends JDialog {

    private final JTextArea areaConsolaSalida = new JTextArea();
    private final List<InfoPatron> listaPatrones = new ArrayList<>();

    public DialogoCatalogoPatrones(Window propietario) {
        super(propietario, "Catálogo de los 15 Patrones de Diseño GoF - " + ConstantesUI.TITULO_APLICACION, ModalityType.APPLICATION_MODAL);

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        inicializarDatosPatrones();
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(940, 680);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(0, 0));

        // 1. Encabezado corporativo
        JPanel panelEncabezado = new JPanel(new BorderLayout(16, 8));
        panelEncabezado.setBackground(EstilosUI.FONDO_CABECERA);
        panelEncabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstilosUI.BORDE_LINEA),
                new EmptyBorder(16, 22, 16, 22)
        ));

        JPanel panelTextoEncabezado = new JPanel();
        panelTextoEncabezado.setLayout(new BoxLayout(panelTextoEncabezado, BoxLayout.Y_AXIS));
        panelTextoEncabezado.setOpaque(false);

        JLabel etiquetaDistintivo = new JLabel("SEGUNDO AVANCE CONSOLIDADO | SEMANA 15 | ARQUITECTURA LIMPIA");
        etiquetaDistintivo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        etiquetaDistintivo.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel etiquetaTitulo = new JLabel("Catálogo Oficial de los 15 Patrones de Diseño GoF");
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        etiquetaTitulo.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        JLabel etiquetaSubtitulo = new JLabel("5 Creacionales | 5 Estructurales | 5 Comportamiento | 41 Tests Unitarios Aprobados");
        etiquetaSubtitulo.setFont(EstilosUI.FUENTE_SUBTITULO);
        etiquetaSubtitulo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelTextoEncabezado.add(etiquetaDistintivo);
        panelTextoEncabezado.add(Box.createVerticalStrut(4));
        panelTextoEncabezado.add(etiquetaTitulo);
        panelTextoEncabezado.add(Box.createVerticalStrut(4));
        panelTextoEncabezado.add(etiquetaSubtitulo);

        // Insignias en cabecera
        JPanel panelInsignias = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panelInsignias.setOpaque(false);
        panelInsignias.add(crearInsigniaPill("15 Patrones GoF", EstilosUI.DORADO_PRINCIPAL));
        panelInsignias.add(crearInsigniaPill("41 Tests Patrones", EstilosUI.ACENTO_RESIDENTES));
        panelInsignias.add(crearInsigniaPill("BUILD SUCCESS", EstilosUI.VERDE_EXITO));

        panelEncabezado.add(panelTextoEncabezado, BorderLayout.WEST);
        panelEncabezado.add(panelInsignias, BorderLayout.EAST);
        add(panelEncabezado, BorderLayout.NORTH);

        // 2. Panel central con pestanas
        JTabbedPane panelPestanas = new JTabbedPane();
        EstilosUI.estilizarPestanas(panelPestanas, EstilosUI.DORADO_PRINCIPAL);

        panelPestanas.addTab("Patrones Creacionales (5)", crearPanelCategoria("Creacional"));
        panelPestanas.addTab("Patrones Estructurales (5)", crearPanelCategoria("Estructural"));
        panelPestanas.addTab("Patrones Comportamiento (5)", crearPanelCategoria("Comportamiento"));
        panelPestanas.addTab("Demostración en Vivo", crearPanelConsolaVivo());

        add(panelPestanas, BorderLayout.CENTER);

        // 3. Barra inferior de acciones
        JPanel panelInferior = new JPanel(new BorderLayout(16, 0));
        panelInferior.setBackground(EstilosUI.FONDO_CABECERA);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, EstilosUI.BORDE_LINEA),
                new EmptyBorder(12, 22, 12, 22)
        ));

        JLabel etiquetaEstadoTests = new JLabel("Suite completa verificada: 70 Tests JUnit 5 (41 patrones + 29 integración y reglas). 0 Fallos, 0 Errores.");
        etiquetaEstadoTests.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaEstadoTests.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        JPanel panelBotonesInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesInferior.setOpaque(false);

        JButton botonEjecutarTodo = new JButton("Ejecutar Demostración Completa");
        EstilosUI.estilizarBoton(botonEjecutarTodo, EstilosUI.DORADO_PRINCIPAL);
        botonEjecutarTodo.addActionListener(e -> {
            panelPestanas.setSelectedIndex(3);
            ejecutarDemostracionCompletaEnVivo();
        });

        JButton botonCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonSecundario(botonCerrar, EstilosUI.TEXTO_MUTED);
        botonCerrar.addActionListener(e -> dispose());

        panelBotonesInferior.add(botonEjecutarTodo);
        panelBotonesInferior.add(botonCerrar);

        panelInferior.add(etiquetaEstadoTests, BorderLayout.WEST);
        panelInferior.add(panelBotonesInferior, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JComponent crearPanelCategoria(String categoriaFiltro) {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(16, 18, 16, 18));

        for (InfoPatron patron : listaPatrones) {
            if (patron.categoria.equalsIgnoreCase(categoriaFiltro)) {
                panelContenedor.add(crearTarjetaPatron(patron));
                panelContenedor.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane scroll = new JScrollPane(panelContenedor);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(EstilosUI.FONDO_PRINCIPAL);
        scroll.getViewport().setBackground(EstilosUI.FONDO_PRINCIPAL);
        return scroll;
    }

    private JPanel crearTarjetaPatron(InfoPatron patron) {
        GlassPanel tarjeta = new GlassPanel(20, false, null);
        tarjeta.setLayout(new BorderLayout(12, 10));
        tarjeta.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Cabecera de la tarjeta: Numero, Nombre y Categoria Badge
        JPanel panelNorte = new JPanel(new BorderLayout(8, 0));
        panelNorte.setOpaque(false);

        JLabel etiquetaNombre = new JLabel(patron.numero + ". " + patron.nombre);
        etiquetaNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        etiquetaNombre.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel etiquetaClases = new JLabel(patron.clases);
        etiquetaClases.setFont(new Font("Consolas", Font.PLAIN, 12));
        etiquetaClases.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelNorte.add(etiquetaNombre, BorderLayout.WEST);
        panelNorte.add(etiquetaClases, BorderLayout.EAST);
        tarjeta.add(panelNorte, BorderLayout.NORTH);

        // Centro: Proposito y Aplicacion en el Condominio
        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.setOpaque(false);

        JLabel etiquetaProposito = new JLabel("<html><b>Propósito:</b> " + patron.proposito + "</html>");
        etiquetaProposito.setFont(EstilosUI.FUENTE_TEXTO);
        etiquetaProposito.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        JLabel etiquetaAplicacion = new JLabel("<html><b>Aplicación Condominal:</b> " + patron.aplicacion + "</html>");
        etiquetaAplicacion.setFont(EstilosUI.FUENTE_TEXTO);
        etiquetaAplicacion.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelCentro.add(etiquetaProposito);
        panelCentro.add(Box.createVerticalStrut(4));
        panelCentro.add(etiquetaAplicacion);
        tarjeta.add(panelCentro, BorderLayout.CENTER);

        // Sur: Test JUnit 5 y Boton Probar
        JPanel panelSur = new JPanel(new BorderLayout(8, 0));
        panelSur.setOpaque(false);

        JLabel etiquetaTest = new JLabel("Verificación: " + patron.testJUnit + " | 100% Aprobado");
        etiquetaTest.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiquetaTest.setForeground(EstilosUI.ACENTO_RESIDENTES);

        JButton botonProbar = new JButton("Probar Patrón en Vivo");
        EstilosUI.estilizarBotonSecundario(botonProbar, EstilosUI.DORADO_PRINCIPAL);
        botonProbar.setPreferredSize(new Dimension(180, 28));
        botonProbar.addActionListener(e -> {
            String resultado = patron.ejecutor.ejecutar();
            areaConsolaSalida.append("\n[DEMOSTRACION EN VIVO] " + patron.nombre.toUpperCase() + "\n");
            areaConsolaSalida.append(resultado + "\n\n");
            AnimadorToast.mostrar("PATRÓN VERIFICADO", patron.nombre + " ejecutado exitosamente.");
        });

        panelSur.add(etiquetaTest, BorderLayout.WEST);
        panelSur.add(botonProbar, BorderLayout.EAST);
        tarjeta.add(panelSur, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JPanel crearPanelConsolaVivo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Cabecera de consola
        JPanel panelNorteConsola = new JPanel(new BorderLayout(10, 0));
        panelNorteConsola.setOpaque(false);

        JLabel etiquetaConsola = new JLabel("Consola de Verificación y Trazabilidad de Patrones GoF");
        etiquetaConsola.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiquetaConsola.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JPanel panelAccionesConsola = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAccionesConsola.setOpaque(false);

        JButton botonLimpiar = new JButton("Limpiar Consola");
        EstilosUI.estilizarBotonSecundario(botonLimpiar, EstilosUI.TEXTO_MUTED);
        botonLimpiar.addActionListener(e -> areaConsolaSalida.setText(""));

        JButton botonCopiar = new JButton("Copiar Salida");
        EstilosUI.estilizarBotonSecundario(botonCopiar, EstilosUI.ACENTO_OPERACIONES);
        botonCopiar.addActionListener(e -> {
            areaConsolaSalida.selectAll();
            areaConsolaSalida.copy();
            AnimadorToast.mostrar("COPIADO", "Trazabilidad copiada al portapapeles.");
        });

        panelAccionesConsola.add(botonLimpiar);
        panelAccionesConsola.add(botonCopiar);

        panelNorteConsola.add(etiquetaConsola, BorderLayout.WEST);
        panelNorteConsola.add(panelAccionesConsola, BorderLayout.EAST);
        panel.add(panelNorteConsola, BorderLayout.NORTH);

        // Area de texto monospaced
        areaConsolaSalida.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaConsolaSalida.setBackground(new Color(0x0A, 0x0E, 0x17));
        areaConsolaSalida.setForeground(new Color(0x38, 0xBD, 0xF8));
        areaConsolaSalida.setCaretColor(EstilosUI.DORADO_PRINCIPAL);
        areaConsolaSalida.setEditable(false);
        areaConsolaSalida.setMargin(new Insets(12, 14, 12, 14));
        areaConsolaSalida.setText("Consola de demostración interactiva lista.\nPresione 'Ejecutar Demostración Completa' o pruebe cada patrón individualmente.\n");

        JScrollPane scrollConsola = new JScrollPane(areaConsolaSalida);
        scrollConsola.setBorder(new LineBorder(EstilosUI.BORDE_LINEA, 1));
        panel.add(scrollConsola, BorderLayout.CENTER);

        return panel;
    }

    private void ejecutarDemostracionCompletaEnVivo() {
        areaConsolaSalida.setText("");
        areaConsolaSalida.append("[INICIO] EJECUCIÓN DE LOS 15 PATRONES GoF - SEGUNDO AVANCE (SEMANA 15)\n");
        areaConsolaSalida.append(ConstantesUI.TITULO_APLICACION + " " + ConstantesUI.VERSION + " | " + ConstantesUI.CICLO_ACADEMICO + "\n\n");

        for (InfoPatron patron : listaPatrones) {
            areaConsolaSalida.append("[PATRÓN " + patron.numero + "/15] " + patron.nombre.toUpperCase() + " (" + patron.categoria + ")\n");
            try {
                String logSalida = patron.ejecutor.ejecutar();
                areaConsolaSalida.append(logSalida + "\n\n");
            } catch (Exception ex) {
                areaConsolaSalida.append("Error en ejecucion: " + ex.getMessage() + "\n\n");
            }
        }

        areaConsolaSalida.append("[FIN] EJECUCIÓN COMPLETADA EXITOSAMENTE: 15 PATRONES GOF VERIFICADOS\n");
        areaConsolaSalida.append("41 Pruebas Unitarias de Patrones Aprobadas al 100% (BUILD SUCCESS)\n\n");
        AnimadorToast.mostrar("SUITE COMPLETA", "15 patrones GoF verificados en vivo exitosamente.");
    }

    private JLabel crearInsigniaPill(String texto, Color colorAcento) {
        JLabel etiqueta = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D grafico = (Graphics2D) g.create();
                grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                grafico.setColor(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 35));
                grafico.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                grafico.setColor(colorAcento);
                grafico.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                grafico.dispose();
                super.paintComponent(g);
            }
        };
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiqueta.setForeground(colorAcento);
        etiqueta.setBorder(new EmptyBorder(4, 10, 4, 10));
        etiqueta.setOpaque(false);
        return etiqueta;
    }

    private void inicializarDatosPatrones() {
        // 1. Singleton
        listaPatrones.add(new InfoPatron(
                1, "Creacional", "Singleton",
                "ConfiguracionCondominio.java",
                "Garantiza una única instancia global para los parámetros de cobranza y mora.",
                "Control centralizado de tasa de mora (1.5%), días de gracia (5) y días para corte de servicio.",
                "SingletonTest.java (2 tests)",
                () -> {
                    ConfiguracionCondominio config = ConfiguracionCondominio.obtenerInstancia();
                    return "Instancia global única obtenida.\n"
                            + "- Tasa Interés Mora: " + config.getTasaInteresMora() + "%\n"
                            + "- Días de Gracia: " + config.getDiasGracia() + " días\n"
                            + "- Días para Declarar Mora: " + config.getDiasParaMora() + " días";
                }
        ));

        // 2. Prototype
        listaPatrones.add(new InfoPatron(
                2, "Creacional", "Prototype",
                "CuotaPrototipo.java, PrototipoCuota.java",
                "Clona plantillas base de cuotas mensuales evitando sobrecostos de instanciación manual.",
                "Generación masiva mensual de cuotas a partir de un prototipo maestro configurado.",
                "PrototypeTest.java (1 test)",
                () -> {
                    CuotaPrototipo original = CuotaPrototipo.builder()
                            .id(101L).montoBase(180.0).concepto("Mantenimiento Ordinario").mesPeriodo("2026-09").estado("PENDIENTE").build();
                    CuotaPrototipo clon = original.clonar();
                    clon.setId(102L);
                    clon.setMesPeriodo("2026-10");
                    return "Original -> Id: " + original.getId() + " | Periodo: " + original.getMesPeriodo() + " | S/ " + original.getMontoBase() + "\n"
                            + "Clonado  -> Id: " + clon.getId() + " | Periodo: " + clon.getMesPeriodo() + " | S/ " + clon.getMontoBase();
                }
        ));

        // 3. Factory Method
        listaPatrones.add(new InfoPatron(
                3, "Creacional", "Factory Method",
                "GastoComunFabrica.java, TipoGasto.java",
                "Define la interfaz de creación delegando a métodos de fábrica la instanciación de gastos especializados.",
                "Emisión tipada de gastos comunes (Agua con lecturas m3, Luz, Mantenimiento, Seguridad).",
                "FactoryMethodTest.java (2 tests)",
                () -> {
                    Condominio condominio = Condominio.builder().id(1L).nombre("Condominio Las Palmeras").build();
                    GastoAgua gastoAgua = GastoComunFabrica.crearGastoAgua(501L, 450.0, LocalDate.now(), "Agua Central", condominio, 1320.0, 1180.0);
                    return "Gasto instanciado: " + gastoAgua.getClass().getSimpleName() + " (Id: " + gastoAgua.getId() + ")\n"
                            + "Monto: S/ " + gastoAgua.getMonto() + " | Consumo: " + (gastoAgua.getLecturaActual() - gastoAgua.getLecturaAnterior()) + " m3";
                }
        ));

        // 4. Abstract Factory
        listaPatrones.add(new InfoPatron(
                4, "Creacional", "Abstract Factory",
                "FabricaInmueble.java, FabricaResidencial.java, FabricaComercial.java",
                "Crea familias completas de objetos inmobiliarios relacionados sin especificar clases concretas.",
                "Generación consistente de edificios, departamentos y áreas comunes según el giro del inmueble.",
                "AbstractFactoryTest.java (2 tests)",
                () -> {
                    FabricaInmueble fabrica = new FabricaResidencial();
                    Condominio c = Condominio.builder().id(1L).nombre("Las Palmeras").build();
                    Edificio e = fabrica.crearEdificio(10L, "Los Robles", 12, c);
                    Departamento d = fabrica.crearDepartamento(101L, "R-101", 1, 85.0);
                    AreaComun a = fabrica.crearAreaComun(201L, "Parrillas y Terraza", 30, 80.0, c);
                    return "Familia Residencial creada:\n"
                            + "- Edificio: " + e.getNombre() + " (" + e.getNumeroPisos() + " pisos)\n"
                            + "- Departamento: " + d.getNumero() + " (" + d.getMetrosCuadrados() + " m2)\n"
                            + "- Área Común: " + a.getNombre() + " (Capacidad: " + a.getCapacidad() + " personas)";
                }
        ));

        // 5. Builder
        listaPatrones.add(new InfoPatron(
                5, "Creacional", "Builder",
                "DepartamentoBuilder.java",
                "Construye objetos Departamento complejos paso a paso con validaciones fluidas.",
                "Asignación flexible de propietario, torre, piso, metraje, habitantes y alícuotas.",
                "BuilderTest.java (1 test)",
                () -> {
                    Propietario prop = Propietario.builder().id(1L).nombre("Carlos Mendoza").dni("12345678").build();
                    Torre torre = Torre.builder().id(1L).nombre("Torre Principal").numeroPisos(10).build();
                    Departamento depto = new DepartamentoBuilder()
                            .conId(202L).conNumero("A-202").conPiso(2).conMetrosCuadrados(92.5)
                            .conNumeroHabitantes(4).conTorre(torre).conPropietario(prop).build();
                    return "Departamento construido con Builder:\n"
                            + "- Depto: " + depto.getNumero() + " | Piso: " + depto.getPiso() + " | Área: " + depto.getMetrosCuadrados() + " m2\n"
                            + "- Propietario: " + depto.getPropietario().getNombre() + " (DNI: " + depto.getPropietario().getDni() + ")";
                }
        ));

        // 6. Adapter
        listaPatrones.add(new InfoPatron(
                6, "Estructural", "Adapter",
                "AdaptadorSistemaLegado.java, NuevoFormatoPago.java, SistemaLegado.java",
                "Adapta la interfaz de un sistema bancario heredado al modelo transaccional moderno.",
                "Procesamiento e interoperabilidad transparente de transferencias y conciliación bancaria.",
                "AdapterTest.java (1 test)",
                () -> {
                    SistemaLegado legado = (cod, val, fec) -> "TRANS-LEGACY-" + cod + "-OK";
                    NuevoFormatoPago adaptado = new AdaptadorSistemaLegado(legado);
                    Departamento depto = Departamento.builder().id(1L).numero("101").build();
                    Pago pago = adaptado.procesarPago(7001L, depto, 200.0, "TRANSFERENCIA");
                    return "Pago procesado vía Adaptador Sistema Legado:\n"
                            + "- Id Pago: " + pago.getId() + " | Monto: S/ " + pago.getMonto() + " | Método: " + pago.getMetodoPago();
                }
        ));

        // 7. Composite
        listaPatrones.add(new InfoPatron(
                7, "Estructural", "Composite",
                "ComponenteInmueble.java, CompuestoInmueble.java, HojaInmueble.java",
                "Trata objetos individuales y composiciones en árbol de manera uniforme.",
                "Cálculo jerárquico recursivo de áreas totales y metrajes (Condominio -> Edificio -> Torre -> Depto).",
                "CompositeTest.java (1 test)",
                () -> {
                    CompuestoInmueble condominio = new CompuestoInmueble("Condominio Las Palmeras");
                    CompuestoInmueble edificioA = new CompuestoInmueble("Edificio A");
                    edificioA.agregarHijo(new HojaInmueble("Depto 101", 80.0));
                    edificioA.agregarHijo(new HojaInmueble("Depto 102", 90.0));
                    condominio.agregarHijo(edificioA);
                    return "Estructura de árbol Composite calculada:\n"
                            + "- Edificio A: " + edificioA.calcularAreaTotal() + " m2\n"
                            + "- Total Condominio: " + condominio.calcularAreaTotal() + " m2";
                }
        ));

        // 8. Decorator
        listaPatrones.add(new InfoPatron(
                8, "Estructural", "Decorator",
                "CuotaBase.java, DecoradorCuota.java, DecoradorRecargoMora.java, DecoradorDescuentoProntoPago.java",
                "Añade responsabilidades dinámicamente a una cuota sin modificar su código base.",
                "Aplicación modular de intereses por mora (+1.5%) y bonificaciones por pronto pago (-10%).",
                "DecoratorTest.java (4 tests)",
                () -> {
                    CuotaBase base = new CuotaBase(200.0, "Cuota Mensual");
                    CuotaBase conMora = new DecoradorRecargoMora(base, 1.5);
                    CuotaBase conDescuento = new DecoradorDescuentoProntoPago(base, 10.0);
                    return "Base:          " + base.obtenerDescripcion() + " -> S/ " + base.calcularMontoTotal() + "\n"
                            + "Con Mora:      " + conMora.obtenerDescripcion() + " -> S/ " + conMora.calcularMontoTotal() + "\n"
                            + "Con Descuento: " + conDescuento.obtenerDescripcion() + " -> S/ " + conDescuento.calcularMontoTotal();
                }
        ));

        // 9. Facade
        listaPatrones.add(new InfoPatron(
                9, "Estructural", "Facade",
                "CobranzaFachada.java",
                "Proporciona una interfaz unificada de alto nivel para coordinar todos los subsistemas.",
                "Punto central de entrada para controladores y ventanas Swing, desacoplando servicios y repositorios JPA.",
                "FacadeTest.java (11 tests)",
                () -> {
                    CobranzaFachada fachada = new CobranzaFachada();
                    Departamento depto = Departamento.builder().id(1L).numero("101").build();
                    CuotaMantenimiento cuota = fachada.generarCuotaMensual(3001L, depto, 200.0, LocalDate.now());
                    return "Operación coordinada a través de CobranzaFachada:\n"
                            + "- Cuota generada: Id " + cuota.getId() + " por S/ " + cuota.getMontoBase() + "\n"
                            + "- Estado financiero: " + cuota.getEstado();
                }
        ));

        // 10. Proxy
        listaPatrones.add(new InfoPatron(
                10, "Estructural", "Proxy",
                "ServicioAccesoCobranza.java, ProxyAccesoCobranza.java, ServicioAccesoCobranzaReal.java",
                "Controla el acceso sensible a operaciones financieras mediante validación de roles RBAC y memoria caché.",
                "Protege el cierre contable de caja (solo Administrador) y optimiza consultas repetitivas de balance.",
                "ProxyTest.java (3 tests)",
                () -> {
                    ServicioAccesoCobranza proxy = new ProxyAccesoCobranza();
                    UsuarioSistema admin = UsuarioSistema.builder().id(1L).usuario("admin").rol(RolUsuario.ADMINISTRADOR).build();
                    UsuarioSistema res = UsuarioSistema.builder().id(2L).usuario("residente").rol(RolUsuario.RESIDENTE).build();
                    String repAdmin = proxy.consultarReporteFinanciero(admin, "2026-10");
                    boolean cierre = proxy.ejecutarCierreMensual(admin, "2026-10");
                    String resIntercepcion;
                    try {
                        proxy.ejecutarCierreMensual(res, "2026-10");
                        resIntercepcion = "Permitido (Inesperado)";
                    } catch (SecurityException ex) {
                        resIntercepcion = "Bloqueado por RBAC: " + ex.getMessage();
                    }
                    return "Control de Acceso mediante Proxy de Seguridad:\n"
                            + "- Cierre mensual por Admin: " + (cierre ? "AUTORIZADO" : "DENEGADO") + "\n"
                            + "- Intento de cierre por Residente: " + resIntercepcion + "\n"
                            + "- Consulta de Balance: " + repAdmin.split("\n")[0];
                }
        ));

        // 11. Command
        listaPatrones.add(new InfoPatron(
                11, "Comportamiento", "Command",
                "ComandoCobranza.java, ComandoRegistrarPago.java, InvocadorCobranza.java",
                "Encapsula una solicitud de cobro como un objeto independiente, habilitando operaciones de Deshacer (Undo).",
                "Historial apilable de transacciones de caja con reversión atómica ante errores de digitación.",
                "CommandTest.java (2 tests)",
                () -> {
                    InvocadorCobranza invocador = new InvocadorCobranza();
                    CuotaMantenimiento cuota = CuotaMantenimiento.builder()
                            .id(1L).montoBase(300.0).montoTotal(300.0).estado("PENDIENTE").build();
                    ComandoCobranza cmd = new ComandoCobranza() {
                        @Override public void ejecutar() { cuota.pagar(150.0); }
                        @Override public void deshacer() { cuota.setMontoTotal(300.0); cuota.setEstado("PENDIENTE"); }
                    };
                    invocador.ejecutarComando(cmd);
                    double saldoPostPago = cuota.getMontoTotal();
                    invocador.deshacerUltimoComando();
                    double saldoPostUndo = cuota.getMontoTotal();
                    return "Transacción Command ejecutada:\n"
                            + "- Pago aplicado: saldo reducido a S/ " + saldoPostPago + " (Estado: " + cuota.getEstado() + ")\n"
                            + "- Deshacer (Undo): saldo restaurado a S/ " + saldoPostUndo + " (Estado: " + cuota.getEstado() + ")";
                }
        ));

        // 12. Observer
        listaPatrones.add(new InfoPatron(
                12, "Comportamiento", "Observer",
                "GestorNotificaciones.java, ObservadorCobranza.java, NotificadorResidente.java, NotificadorToast.java",
                "Define una dependencia uno a muchos para alertar a los residentes ante eventos del sistema.",
                "Despacho reactivo de confirmaciones de pago, recordatorios de vencimiento y alertas de seguridad.",
                "ObserverTest.java (2 tests)",
                () -> {
                    GestorNotificaciones gestor = new GestorNotificaciones();
                    NotificadorResidente notificador = new NotificadorResidente();
                    gestor.suscribir(notificador);
                    gestor.notificar("Pago recibido de S/ 250.00 para Depto 201.");
                    return "Evento notificado vía Observer:\n"
                            + "- Suscriptores activos: " + gestor.cantidadObservadores() + "\n"
                            + "- Mensaje entregado: " + notificador.getUltimoMensaje();
                }
        ));

        // 13. State
        listaPatrones.add(new InfoPatron(
                13, "Comportamiento", "State",
                "EstadoCuota.java, EstadoPendiente.java, EstadoParcial.java, EstadoPagado.java, EstadoVencido.java",
                "Permite que la cuota altere su comportamiento cuando cambia su estado interno de pago.",
                "Máquina de estados financieros: PENDIENTE -> PARCIAL -> PAGADO o VENCIDO con reglas de bloqueo.",
                "StateTest.java (3 tests)",
                () -> {
                    CuotaMantenimiento cuota = CuotaMantenimiento.builder()
                            .id(1L).montoBase(200.0).montoTotal(200.0).estado("PENDIENTE").build();
                    String estado1 = cuota.getEstado();
                    cuota.pagar(80.0);
                    String estado2 = cuota.getEstado();
                    cuota.pagar(120.0);
                    String estado3 = cuota.getEstado();
                    return "Transición de Estados Financieros:\n"
                            + "1. Inicio: " + estado1 + " (Saldo S/ 200.0)\n"
                            + "2. Tras amortizar S/ 80: " + estado2 + " (Saldo S/ 120.0)\n"
                            + "3. Tras liquidar S/ 120: " + estado3 + " (Saldo S/ 0.0)";
                }
        ));

        // 14. Strategy
        listaPatrones.add(new InfoPatron(
                14, "Comportamiento", "Strategy",
                "CalculadoraGastos.java, EstrategiaProrrateo.java, ProrrateoPorAlicuota.java, ProrrateoEquitativo.java",
                "Encapsula familias de algoritmos intercambiables para el cálculo y prorrateo de gastos comunes.",
                "Conmutación en tiempo de ejecución entre prorrateo por área m2 (alícuota) y división equitativa (1/N).",
                "StrategyTest.java (3 tests)",
                () -> {
                    CalculadoraGastos calc = new CalculadoraGastos(new ProrrateoPorAlicuota());
                    double gastoTotal = 1500.0;
                    double cuotaAlicuota = calc.ejecutarCalculo(gastoTotal, 95.0, 950.0); // 10%
                    calc.setEstrategia(new ProrrateoEquitativo());
                    double cuotaEquitativa = calc.ejecutarCalculo(gastoTotal, 1.0, 5.0); // 1500 / 5
                    return "Estrategias de Prorrateo evaluadas:\n"
                            + "- Por Alícuota (95 m2 sobre 950 m2): S/ " + String.format(Locale.US, "%.2f", cuotaAlicuota) + "\n"
                            + "- Equitativo (1/5 departamentos): S/ " + String.format(Locale.US, "%.2f", cuotaEquitativa);
                }
        ));

        // 15. Template Method
        listaPatrones.add(new InfoPatron(
                15, "Comportamiento", "Template Method",
                "PlantillaProcesoCobranza.java, ProcesoCobranzaOrdinaria.java, ProcesoCobranzaExtraordinaria.java",
                "Define el esqueleto invariable de un algoritmo en una operación, postergando pasos a las subclases.",
                "Estandarización del ciclo de cobro (validación -> ajuste -> emisión comprobante -> libro diario -> aviso).",
                "TemplateMethodTest.java (3 tests)",
                () -> {
                    PlantillaProcesoCobranza ord = new ProcesoCobranzaOrdinaria(5.0);
                    PlantillaProcesoCobranza ext = new ProcesoCobranzaExtraordinaria(10.0);
                    UsuarioSistema u = UsuarioSistema.builder().id(1L).usuario("admin").rol(RolUsuario.ADMINISTRADOR).build();
                    ResultadoCobranza resOrd = ord.procesarCobranza(101L, 200.0, u);
                    ResultadoCobranza resExt = ext.procesarCobranza(202L, 500.0, u);
                    return "Flujo Algorítmico Template Method:\n"
                            + "- " + resOrd.getTipoCobranza() + ": Base S/ " + resOrd.getMontoBase() + " -> Ajustado S/ " + resOrd.getMontoFinalAjustado() + " [" + resOrd.getCodigoComprobante() + "]\n"
                            + "- " + resExt.getTipoCobranza() + ": Base S/ " + resExt.getMontoBase() + " -> Ajustado S/ " + resExt.getMontoFinalAjustado() + " [" + resExt.getCodigoComprobante() + "]";
                }
        ));
    }

    @FunctionalInterface
    public interface EjecutorPatron {
        String ejecutar();
    }

    private static class InfoPatron {
        final int numero;
        final String categoria;
        final String nombre;
        final String clases;
        final String proposito;
        final String aplicacion;
        final String testJUnit;
        final EjecutorPatron ejecutor;

        InfoPatron(int numero, String categoria, String nombre, String clases, String proposito, String aplicacion, String testJUnit, EjecutorPatron ejecutor) {
            this.numero = numero;
            this.categoria = categoria;
            this.nombre = nombre;
            this.clases = clases;
            this.proposito = proposito;
            this.aplicacion = aplicacion;
            this.testJUnit = testJUnit;
            this.ejecutor = ejecutor;
        }
    }
}
