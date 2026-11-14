package com.condominio;

import com.condominio.patrones.singleton.ConfiguracionCondominio;
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
import com.condominio.patrones.factorymethod.GastoComunFabrica;
import com.condominio.patrones.facade.CobranzaFachada;
import com.condominio.patrones.observer.GestorNotificaciones;
import com.condominio.patrones.observer.NotificadorResidente;
import com.condominio.patrones.prototype.CuotaPrototipo;
import com.condominio.patrones.strategy.CalculadoraGastos;
import com.condominio.patrones.strategy.ProrrateoEquitativo;
import com.condominio.patrones.strategy.ProrrateoPorAlicuota;
import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import com.condominio.patrones.proxy.ProxyAccesoCobranza;
import com.condominio.patrones.proxy.ServicioAccesoCobranza;
import com.condominio.patrones.templatemethod.PlantillaProcesoCobranza;
import com.condominio.patrones.templatemethod.ProcesoCobranzaExtraordinaria;
import com.condominio.patrones.templatemethod.ProcesoCobranzaOrdinaria;
import com.condominio.patrones.templatemethod.ResultadoCobranza;
import com.condominio.gui.VentanaPrincipal;
import com.condominio.gui.util.ConstantesUI;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;

// Punto de entrada de la aplicacion
@SpringBootApplication
public class AplicacionCobranzaVecinal {

    public static void main(String[] argumentos) {
        System.setProperty("java.awt.headless", "false");
        new SpringApplicationBuilder(AplicacionCobranzaVecinal.class)
                .headless(false)
                .web(WebApplicationType.SERVLET)
                .run(argumentos);
    }

    @Bean
    public CommandLineRunner ejecutarDemostracionPatrones(
            @org.springframework.beans.factory.annotation.Autowired(required = false) CobranzaFachada fachadaInyectada) {
        return argumentos -> {
            if (fachadaInyectada == null) {
                return;
            }
            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> {
                    com.condominio.gui.VentanaSplash splash = new com.condominio.gui.VentanaSplash();
                    splash.mostrar();

                    // Flujo secuencial: el Splash se muestra primero con su barra de carga (~1.5s),
                    // luego se desvanece y cierra, y recién entonces se abre el diálogo de Login centrado.
                    int tiempoSplash = com.condominio.gui.util.ConstantesUI.ANIMACIONES_ACTIVAS ? 350 : 0;
                    javax.swing.Timer timerSplash = new javax.swing.Timer(tiempoSplash, evSplash -> {
                        splash.cerrar(() -> {
                            com.condominio.gui.DialogoLogin login = new com.condominio.gui.DialogoLogin(null, fachadaInyectada);
                            login.setVisible(true);

                            if (login.isLoginExitoso()) {
                                VentanaPrincipal ventana = new VentanaPrincipal(fachadaInyectada);
                                com.condominio.gui.GestorBandejaSistema.inicializar(ventana, ventana::abrirEstadoCuentaVecino);
                                ventana.setVisible(true);
                            } else {
                                com.condominio.gui.GestorBandejaSistema.removerTray();
                                System.exit(0);
                            }
                        });
                    });
                    timerSplash.setRepeats(false);
                    timerSplash.start();
                });
            }

            // Ejecucion asincrona no bloqueante de la verificacion de patrones en consola para arranque ultrarrapido
            java.util.concurrent.CompletableFuture.runAsync(AplicacionCobranzaVecinal::ejecutarDemostracionPatronesConsola);
        };
    }

    public static void ejecutarDemostracionPatronesConsola() {
        try {
            System.out.println("[INFO] Demostración de patrones GoF iniciada.");
            System.out.println("[INFO] " + ConstantesUI.TITULO_APLICACION + " " + ConstantesUI.VERSION + " - " + ConstantesUI.SUBTITULO_APLICACION);
            System.out.println("[INFO] Suite de Pruebas: " + ConstantesUI.TESTS_BADGE + " | Periodo: " + ConstantesUI.CICLO_ACADEMICO);

            // Patron Singleton
            System.out.println("[PATRON 1/15] Singleton (ConfiguracionCondominio): Instancia global verificada.");
            ConfiguracionCondominio configuracion = ConfiguracionCondominio.obtenerInstancia();
            System.out.println("Instancia global obtenida exitosamente.");
            System.out.println("Tasa de interes por mora: " + configuracion.getTasaInteresMora() + "%");
            System.out.println("Dias de gracia permitidos: " + configuracion.getDiasGracia() + " dias");
            System.out.println("Dias para declarar mora: " + configuracion.getDiasParaMora() + " dias");

            // Patron Prototype
            System.out.println("[PATRON 2/15] Prototype (CuotaPrototipo): Clonacion de plantilla de cuota.");
            CuotaPrototipo cuotaOriginal = CuotaPrototipo.builder()
                    .id(101L)
                    .montoBase(180.0)
                    .concepto("Mantenimiento Ordinario")
                    .mesPeriodo("2026-09")
                    .estado("PENDIENTE")
                    .build();
            CuotaPrototipo cuotaClonada = cuotaOriginal.clonar();
            cuotaClonada.setId(102L);
            cuotaClonada.setMesPeriodo("2026-10");
            System.out.println("Original: Id " + cuotaOriginal.getId() + " | Periodo: " + cuotaOriginal.getMesPeriodo() + " | S/ " + cuotaOriginal.getMontoBase());
            System.out.println("Clon:     Id " + cuotaClonada.getId() + " | Periodo: " + cuotaClonada.getMesPeriodo() + " | S/ " + cuotaClonada.getMontoBase());

            // Patron Factory Method
            System.out.println("[PATRON 3/15] Factory Method (GastoComunFabrica): Creacion de gasto comun.");
            Condominio condominio = Condominio.builder().id(1L).nombre("Condominio Las Palmeras").direccion("Av. Primavera 123").build();
            GastoAgua gastoAgua = GastoComunFabrica.crearGastoAgua(
                    501L, 450.0, LocalDate.now(), "Consumo de agua medidor central",
                    condominio, 1320.0, 1180.0);
            System.out.println("Gasto creado: " + gastoAgua.getClass().getSimpleName() + " (Id: " + gastoAgua.getId() + ")");
            System.out.println("Monto: S/ " + gastoAgua.getMonto() + " | Consumo: " + (gastoAgua.getLecturaActual() - gastoAgua.getLecturaAnterior()) + " m3");

            // Patron Abstract Factory
            System.out.println("[PATRON 4/15] Abstract Factory (FabricaInmueble): Familia residencial creada.");
            FabricaInmueble fabricaResidencial = new FabricaResidencial();
            Edificio torreResidencial = fabricaResidencial.crearEdificio(10L, "Los Robles", 12, condominio);
            Departamento deptoResidencial = fabricaResidencial.crearDepartamento(101L, "R-101", 1, 85.0);
            AreaComun areaResidencial = fabricaResidencial.crearAreaComun(201L, "Parrillas y Terraza", 30, 80.0, condominio);
            System.out.println("Familia Residencial creada:");
            System.out.println("- Edificio: " + torreResidencial.getNombre() + " (" + torreResidencial.getNumeroPisos() + " pisos)");
            System.out.println("- Departamento: " + deptoResidencial.getNumero() + " (" + deptoResidencial.getMetrosCuadrados() + " m2)");
            System.out.println("- Area Comun: " + areaResidencial.getNombre() + " (Capacidad: " + areaResidencial.getCapacidad() + " personas)");

            // Patron Builder
            System.out.println("[PATRON 5/15] Builder (DepartamentoBuilder): Construccion fluida de departamento.");
            Propietario propietarioCarlos = Propietario.builder()
                    .id(1L)
                    .nombre("Carlos Mendoza")
                    .dni("12345678")
                    .correo("carlos.mendoza@email.com")
                    .banco("BCP")
                    .cuentaBancaria("191-12345678-0-01")
                    .build();
            Torre torrePrincipal = Torre.builder().id(1L).nombre("Torre Principal").numeroPisos(10).build();

            Departamento deptoConstruido = new DepartamentoBuilder()
                    .conId(202L)
                    .conNumero("A-202")
                    .conPiso(2)
                    .conMetrosCuadrados(92.5)
                    .conNumeroHabitantes(4)
                    .conTorre(torrePrincipal)
                    .conPropietario(propietarioCarlos)
                    .build();
            System.out.println("Departamento construido con Builder:");
            System.out.println("Numero: " + deptoConstruido.getNumero() + " | Piso: " + deptoConstruido.getPiso()
                    + " | Area: " + deptoConstruido.getMetrosCuadrados() + " m2 | Habitantes: " + deptoConstruido.getNumeroHabitantes());
            System.out.println("Propietario: " + deptoConstruido.getPropietario().getNombre() + " (DNI: " + deptoConstruido.getPropietario().getDni() + ")");

            // Patron Adapter
            System.out.println("[PATRON 6/15] Adapter (AdaptadorSistemaLegado): Integracion de formato de pagos.");
            SistemaLegado sistemaAntiguo = (codigo, valor, fecha) -> "TRANS-LEGACY-" + codigo + "-OK";
            NuevoFormatoPago adaptador = new AdaptadorSistemaLegado(sistemaAntiguo);
            Pago pagoAdaptado = adaptador.procesarPago(7001L, deptoConstruido, 200.0, "TRANSFERENCIA");
            System.out.println("Pago procesado mediante Adapter:");
            System.out.println("Id Pago: " + pagoAdaptado.getId() + " | Monto: S/ " + pagoAdaptado.getMonto() + " | Metodo: " + pagoAdaptado.getMetodoPago());

            // Patron Facade
            System.out.println("[PATRON 7/15] Facade (CobranzaFachada): Coordinacion de subsistemas contables.");
            CobranzaFachada fachada = new CobranzaFachada();
            LocalDate fechaVencida = LocalDate.now().minusDays(8);
            CuotaMantenimiento cuotaFachada = fachada.generarCuotaMensual(3001L, deptoConstruido, 200.0, fechaVencida);
            double moraCalculada = fachada.calcularMoraCuota(cuotaFachada, LocalDate.now());
            HistorialPago historial = fachada.registrarPago(8001L, 9001L, cuotaFachada, cuotaFachada.getMontoTotal(), "DEPOSITO");
            System.out.println("Operacion coordinada con CobranzaFachada:");
            System.out.println("Cuota emitida: S/ " + cuotaFachada.getMontoBase() + " | Mora aplicada: S/ " + moraCalculada + " | Total: S/ " + cuotaFachada.getMontoTotal());
            System.out.println("Auditoria: Estado anterior: " + historial.getEstadoAnterior() + " a Estado nuevo: " + historial.getEstadoNuevo());

            // Patron Decorator
            System.out.println("[PATRON 8/15] Decorator (DecoradorCuota): Aplicacion dinamica de recargos y descuentos.");
            CuotaBase cuotaBase = new CuotaBase(200.0, "Cuota Octubre");
            CuotaBase cuotaConMora = new DecoradorRecargoMora(cuotaBase, configuracion.getTasaInteresMora());
            CuotaBase cuotaConDescuento = new DecoradorDescuentoProntoPago(cuotaBase, 10.0);
            System.out.println("Base:          " + cuotaBase.obtenerDescripcion() + " : S/ " + cuotaBase.calcularMontoTotal());
            System.out.println("Con Mora:      " + cuotaConMora.obtenerDescripcion() + " : S/ " + cuotaConMora.calcularMontoTotal());
            System.out.println("Con Descuento: " + cuotaConDescuento.obtenerDescripcion() + " : S/ " + cuotaConDescuento.calcularMontoTotal());

            // Patron Composite
            System.out.println("[PATRON 9/15] Composite (CompuestoInmueble): Estructura jerarquica y calculo de areas.");
            CompuestoInmueble raizCondominio = new CompuestoInmueble("Condominio Las Palmeras");
            CompuestoInmueble edificioA = new CompuestoInmueble("Edificio A");
            CompuestoInmueble edificioB = new CompuestoInmueble("Edificio B");

            edificioA.agregarHijo(new HojaInmueble("Depto A-101", 80.0));
            edificioA.agregarHijo(new HojaInmueble("Depto A-102", 90.0));
            edificioB.agregarHijo(new HojaInmueble("Depto B-101", 110.0));

            raizCondominio.agregarHijo(edificioA);
            raizCondominio.agregarHijo(edificioB);

            System.out.println("Calculo de area total mediante Composite:");
            System.out.println("Area " + edificioA.obtenerNombre() + ": " + edificioA.calcularAreaTotal() + " m2");
            System.out.println("Area " + edificioB.obtenerNombre() + ": " + edificioB.calcularAreaTotal() + " m2");
            System.out.println("Area Total " + raizCondominio.obtenerNombre() + ": " + raizCondominio.calcularAreaTotal() + " m2");

            // Patron Command
            System.out.println("[PATRON 10/15] Command (InvocadorCobranza): Ejecucion y reversion transaccional.");
            InvocadorCobranza invocadorCobranza = new InvocadorCobranza();
            CuotaMantenimiento cuotaComando = CuotaMantenimiento.builder()
                    .id(901L)
                    .montoBase(300.0)
                    .montoTotal(300.0)
                    .estado("PENDIENTE")
                    .fechaEmision(LocalDate.now())
                    .fechaVencimiento(LocalDate.now().plusDays(15))
                    .departamento(deptoConstruido)
                    .build();

            ComandoCobranza comandoCobro = new ComandoCobranza() {
                private final double montoOperacion = 150.0;
                private boolean transaccionEjecutada = false;

                @Override
                public void ejecutar() {
                    cuotaComando.pagar(montoOperacion);
                    transaccionEjecutada = true;
                }

                @Override
                public void deshacer() {
                    if (transaccionEjecutada) {
                        cuotaComando.setMontoTotal(cuotaComando.getMontoTotal() + montoOperacion);
                        cuotaComando.setEstado("PENDIENTE");
                        transaccionEjecutada = false;
                    }
                }
            };

            invocadorCobranza.ejecutarComando(comandoCobro);
            System.out.println("Comando registrar pago ejecutado. Saldo restante: S/ " + cuotaComando.getMontoTotal()
                    + " | Estado: " + cuotaComando.getEstado() + " | Historial apilado: " + invocadorCobranza.cantidadComandos());
            invocadorCobranza.deshacerUltimoComando();
            System.out.println("Comando revertido (Undo). Saldo restaurado: S/ " + cuotaComando.getMontoTotal()
                    + " | Estado: " + cuotaComando.getEstado() + " | Historial tras deshacer: " + invocadorCobranza.cantidadComandos());

            // Patron Observer
            System.out.println("[PATRON 11/15] Observer (GestorNotificaciones): Despacho reactivo de eventos.");
            GestorNotificaciones gestorNotificaciones = new GestorNotificaciones();
            NotificadorResidente notificadorResidente = new NotificadorResidente();
            gestorNotificaciones.suscribir(notificadorResidente);
            System.out.println("Observador suscrito exitosamente al canal de eventos. Suscriptores activos: " + gestorNotificaciones.cantidadObservadores());
            String mensajeEvento = "Notificacion automatica a Carlos Mendoza: Pago verificado de S/ 200.00 para Depto A-202";
            gestorNotificaciones.notificar(mensajeEvento);
            System.out.println("Canal notificado (Observer despachado): " + notificadorResidente.getUltimoMensaje());

            // Patron State
            System.out.println("[PATRON 12/15] State (EstadoCuota): Transicion dinamica de estados de cobranza.");
            CuotaMantenimiento cuotaEstado = CuotaMantenimiento.builder()
                    .id(902L)
                    .montoBase(200.0)
                    .montoTotal(200.0)
                    .fechaEmision(LocalDate.now().minusDays(2))
                    .fechaVencimiento(LocalDate.now().plusDays(10))
                    .estado("PENDIENTE")
                    .build();
            System.out.println("Estado inicial: " + cuotaEstado.getEstado() + " (" + cuotaEstado.getEstadoLogico().getNombreEstado() + ")");
            cuotaEstado.pagar(80.0);
            System.out.println("Transicion tras amortizacion de S/ 80.00: " + cuotaEstado.getEstado()
                    + " (" + cuotaEstado.getEstadoLogico().getNombreEstado() + ") - Saldo: S/ " + cuotaEstado.getMontoTotal());
            cuotaEstado.pagar(120.0);
            System.out.println("Transicion tras liquidacion total de S/ 120.00: " + cuotaEstado.getEstado()
                    + " (" + cuotaEstado.getEstadoLogico().getNombreEstado() + ") - Saldo: S/ " + cuotaEstado.getMontoTotal());

            // Patron Strategy
            System.out.println("[PATRON 13/15] Strategy (CalculadoraGastos): Algoritmos intercambiables de prorrateo.");
            CalculadoraGastos calculadoraGastos = new CalculadoraGastos(new ProrrateoPorAlicuota());
            double totalGastoComun = 1200.0;
            double areaDepartamento = 92.5;
            double areaTotalCondominio = 280.0;
            double cuotaPorAlicuota = calculadoraGastos.ejecutarCalculo(totalGastoComun, areaDepartamento, areaTotalCondominio);
            System.out.println("Estrategia 1 (Prorrateo por Alicuota - m2): S/ " + String.format(java.util.Locale.US, "%.2f", cuotaPorAlicuota)
                    + " (Area: " + areaDepartamento + " m2 sobre total " + areaTotalCondominio + " m2)");

            calculadoraGastos.setEstrategia(new ProrrateoEquitativo());
            double cuotaEquitativa = calculadoraGastos.ejecutarCalculo(totalGastoComun, 1.0, 3.0);
            System.out.println("Estrategia 2 (Prorrateo Equitativo 1/N):    S/ " + String.format(java.util.Locale.US, "%.2f", cuotaEquitativa)
                    + " (Division simetrica entre 3 unidades residenciales)");

            // Patron Proxy
            System.out.println("[PATRON 14/15] Proxy (ProxyAccesoCobranza): Control de acceso por roles y auditoria.");
            ServicioAccesoCobranza proxyCobranza = new ProxyAccesoCobranza();
            UsuarioSistema usuarioAdministrador = UsuarioSistema.builder()
                    .id(1L).usuario("admin").clave("admin123").rol(RolUsuario.ADMINISTRADOR).activo(true).build();
            UsuarioSistema usuarioResidente = UsuarioSistema.builder()
                    .id(2L).usuario("residente").clave("residente123").rol(RolUsuario.RESIDENTE).activo(true).build();

            String balanceAdmin = proxyCobranza.consultarReporteFinanciero(usuarioAdministrador, "2026-10");
            System.out.println("Consulta permitida a Administrador:\n" + balanceAdmin);
            boolean cierrePermitido = proxyCobranza.ejecutarCierreMensual(usuarioAdministrador, "2026-10");
            System.out.println("Cierre mensual ejecutado por Administrador: " + (cierrePermitido ? "AUTORIZADO" : "DENEGADO"));

            try {
                proxyCobranza.ejecutarCierreMensual(usuarioResidente, "2026-10");
            } catch (SecurityException ex) {
                System.out.println("Intercepcion de seguridad del Proxy: " + ex.getMessage());
            }

            // Patron Template Method
            System.out.println("[PATRON 15/15] Template Method (PlantillaProcesoCobranza): Flujo estandarizado con variantes.");
            PlantillaProcesoCobranza procesoOrdinario = new ProcesoCobranzaOrdinaria(5.0);
            PlantillaProcesoCobranza procesoExtraordinario = new ProcesoCobranzaExtraordinaria(10.0);

            ResultadoCobranza resOrdinario = procesoOrdinario.procesarCobranza(101L, 200.0, usuarioAdministrador);
            System.out.println("Cobranza 1 (Ordinaria - 5% Pronto Pago): " + resOrdinario.getCodigoComprobante()
                    + " | Monto Base: S/ " + resOrdinario.getMontoBase()
                    + " a Final: S/ " + resOrdinario.getMontoFinalAjustado());

            ResultadoCobranza resExtraordinario = procesoExtraordinario.procesarCobranza(202L, 500.0, usuarioAdministrador);
            System.out.println("Cobranza 2 (Extraordinaria - 10% Reserva): " + resExtraordinario.getCodigoComprobante()
                    + " | Monto Base: S/ " + resExtraordinario.getMontoBase()
                    + " a Final: S/ " + resExtraordinario.getMontoFinalAjustado());

            System.out.println("[INFO] Ejecución completada exitosamente: 15 patrones GoF verificados.");
        } catch (Exception ex) {
            System.err.println("Demostracion de patrones en consola finalizada: " + ex.getMessage());
        }
    }
}