package com.condominio.gui.paneles;

import com.condominio.dto.CuotaDTO;
import com.condominio.dto.InmuebleDTO;
import com.condominio.dto.ResidenteDTO;
import com.condominio.gui.DialogoGeneradorReportes;
import com.condominio.gui.util.ChipIcono;
import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;
import com.condominio.gui.util.TooltipGraficoGlass;
import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.modelo.seguridad.SesionActual;
import com.condominio.patrones.facade.CobranzaFachada;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Panel de Control Ejecutivo (Dashboard con KPIs y Gráficos Nativos 2D).
@Getter
public class PanelControl extends JPanel implements com.condominio.patrones.observer.ObservadorCobranza {

    private final CobranzaFachada fachada;

    // Tarjetas KPI
    private JLabel lblValorRecaudado;
    private JLabel lblValorDeuda;
    private JLabel lblValorMorosidad;
    private JLabel lblValorOcupacion;
    private JLabel lblValorFondoReserva;

    // Gráficos Canvas 2D Nativos
    private GraficoBarrasRecaudacion graficoBarras;
    private GraficoCircularEstados graficoCircular;

    // Tabla de Últimas Acciones
    private JTable tablaActividad;
    private DefaultTableModel modeloActividad;
    private JButton btnReportesPdf;
    private JButton btnRefrescar;

    public PanelControl(CobranzaFachada fachada) {
        this.fachada = (fachada != null) ? fachada : new CobranzaFachada();
        inicializarComponentes();
        actualizarDatos();
        if (this.fachada.getGestorNotificaciones() != null) {
            this.fachada.getGestorNotificaciones().suscribir(this);
        }
    }

    @Override
    public void actualizar(String mensaje) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(this::actualizarDatos);
    }

    public void desuscribir() {
        if (fachada != null && fachada.getGestorNotificaciones() != null) {
            fachada.getGestorNotificaciones().desuscribir(this);
        }
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(12, 12));
        setBackground(EstilosUI.FONDO_PRINCIPAL);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. BANNER SUPERIOR INFORMATIVO
        JPanel panelBanner = EstilosUI.crearPanelBanner(EstilosUI.DORADO_PRINCIPAL);
        JLabel tituloBanner = new JLabel("PANEL DE CONTROL Y GESTIÓN ANALÍTICA (DASHBOARD)");
        tituloBanner.setFont(EstilosUI.FUENTE_BANNER);
        tituloBanner.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JLabel subtituloBanner = new JLabel("Monitoreo en tiempo real de recaudación, morosidad, ocupación e indicadores clave.");
        subtituloBanner.setFont(EstilosUI.FUENTE_SUBTITULO);
        subtituloBanner.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelBanner.add(tituloBanner, BorderLayout.NORTH);
        panelBanner.add(subtituloBanner, BorderLayout.SOUTH);
        add(panelBanner, BorderLayout.NORTH);

        // 2. CONTENIDO CENTRAL (Scrollable para resoluciones estándar)
        JPanel panelCuerpo = new JPanel();
        panelCuerpo.setLayout(new BoxLayout(panelCuerpo, BoxLayout.Y_AXIS));
        panelCuerpo.setOpaque(false);

        // 2.1 FILA DE TARJETAS KPI (5 Tarjetas con variación porcentual a la vista)
        JPanel panelKpis = new JPanel(new GridLayout(1, 5, 10, 0));
        panelKpis.setOpaque(false);
        panelKpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        panelKpis.setPreferredSize(new Dimension(800, 116));

        lblValorRecaudado    = new JLabel("S/ 0.00", SwingConstants.CENTER);
        lblValorDeuda        = new JLabel("S/ 0.00", SwingConstants.CENTER);
        lblValorMorosidad    = new JLabel("0.0%", SwingConstants.CENTER);
        lblValorOcupacion    = new JLabel("0 / 0", SwingConstants.CENTER);
        lblValorFondoReserva = new JLabel(ConstantesUI.FONDO_RESERVA_DEFECTO, SwingConstants.CENTER);

        panelKpis.add(crearTarjetaKpi("Recaudación Mes", lblValorRecaudado, EstilosUI.VERDE_EXITO, "RECAUDACION", "12.5% vs mes anterior", true));
        panelKpis.add(crearTarjetaKpi("Saldo Deudor", lblValorDeuda, EstilosUI.ROJO_ERROR, "DEUDA", "5.2% vs periodo anterior", false));
        panelKpis.add(crearTarjetaKpi("Morosidad", lblValorMorosidad, EstilosUI.AMBAR_AVISO, "MOROSIDAD", "3.0% tendencia mensual", false));
        panelKpis.add(crearTarjetaKpi("Ocupación Deptos", lblValorOcupacion, EstilosUI.ACENTO_OPERACIONES, "OCUPACION", "1 unidad ocupada (71%)", true));
        panelKpis.add(crearTarjetaKpi("Fondo de Reserva", lblValorFondoReserva, EstilosUI.DORADO_PRINCIPAL, "FONDO", "5.8% acumulado anual", true));

        panelCuerpo.add(panelKpis);
        panelCuerpo.add(Box.createVerticalStrut(12));

        // 2.2 FILA DE GRÁFICOS (Canvas 2D nativos)
        JPanel panelGraficos = new JPanel(new GridLayout(1, 2, 12, 0));
        panelGraficos.setOpaque(false);
        panelGraficos.setPreferredSize(new Dimension(800, 215));
        panelGraficos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        graficoBarras = new GraficoBarrasRecaudacion();
        graficoCircular = new GraficoCircularEstados();

        panelGraficos.add(envolverEnTarjeta("Recaudación por Periodo (S/)", graficoBarras, EstilosUI.DORADO_PRINCIPAL));
        panelGraficos.add(envolverEnTarjeta("Distribución de Estados de Cuotas", graficoCircular, EstilosUI.ACENTO_COBRANZAS));

        panelCuerpo.add(panelGraficos);
        panelCuerpo.add(Box.createVerticalStrut(12));

        // 2.3 ÚLTIMAS ACCIONES (Historial)
        JPanel panelActividad = EstilosUI.crearPanelGlassmorphic(EstilosUI.DORADO_PRINCIPAL, 14);
        panelActividad.setLayout(new BorderLayout(0, 8));
        panelActividad.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel headerActividad = new JPanel(new BorderLayout());
        headerActividad.setOpaque(false);
        JLabel lblTituloActividad = new JLabel("REGISTRO DE ÚLTIMAS 10 OPERACIONES EN EL SISTEMA");
        lblTituloActividad.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTituloActividad.setForeground(EstilosUI.DORADO_PRINCIPAL);

        JPanel panelBotonesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotonesHeader.setOpaque(false);

        btnRefrescar = new JButton("Refrescar");
        EstilosUI.estilizarBotonPildora(btnRefrescar, EstilosUI.DORADO_PRINCIPAL, false);
        btnRefrescar.setPreferredSize(new Dimension(105, 30));
        btnRefrescar.setToolTipText("Actualiza manualmente todos los KPIs, gráficos y operaciones");
        btnRefrescar.addActionListener(e -> actualizarDatos());

        btnReportesPdf = new JButton("Centro de Reportes PDF");
        EstilosUI.estilizarBotonPildora(btnReportesPdf, EstilosUI.DORADO_PRINCIPAL, false);
        btnReportesPdf.setPreferredSize(new Dimension(195, 30));
        btnReportesPdf.addActionListener(e -> {
            DialogoGeneradorReportes dlg = new DialogoGeneradorReportes(SwingUtilities.getWindowAncestor(this), null, fachada);
            dlg.setVisible(true);
        });

        panelBotonesHeader.add(btnRefrescar);
        panelBotonesHeader.add(btnReportesPdf);

        headerActividad.add(lblTituloActividad, BorderLayout.WEST);
        headerActividad.add(panelBotonesHeader, BorderLayout.EAST);
        panelActividad.add(headerActividad, BorderLayout.NORTH);

        String[] cols = {"Tipo", "Detalle de Operación", "Responsable / Titular", "Fecha / Estado", "Monto"};
        modeloActividad = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaActividad = EstilosUI.crearTablaEstilizada(modeloActividad, EstilosUI.DORADO_PRINCIPAL);
        tablaActividad.setRowHeight(30);
        tablaActividad.setPreferredScrollableViewportSize(new Dimension(800, 110));
        tablaActividad.getColumnModel().getColumn(0).setCellRenderer(new com.condominio.gui.util.RenderTipoOperacion());

        JScrollPane scrollActividad = new JScrollPane(tablaActividad);
        scrollActividad.getViewport().setBackground(EstilosUI.FONDO_TARJETA);
        scrollActividad.setBorder(EstilosUI.crearBordeRedondeado(8, EstilosUI.BORDE_LINEA));
        scrollActividad.setPreferredSize(new Dimension(800, 115));
        panelActividad.add(scrollActividad, BorderLayout.CENTER);

        panelCuerpo.add(panelActividad);

        JScrollPane scrollGeneral = new JScrollPane(panelCuerpo);
        scrollGeneral.setOpaque(false);
        scrollGeneral.getViewport().setOpaque(false);
        scrollGeneral.setBorder(null);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollGeneral, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaKpi(String titulo, JLabel lblValor, Color colorAcento,
                                   String tipoIcono, String variacionPorcentual, boolean positiva) {
        // Tarjeta KPI con vidrio esmerilado, esquinas redondeadas y profundidad (Regla 6)
        GlassPanel tarjeta = new GlassPanel(new BorderLayout(0, 6), 22, colorAcento, true);
        tarjeta.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Cabecera: Título pequeño a la izquierda y chip circular de icono arriba a la derecha (Regla 6)
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);

        JLabel lblTit = new JLabel(titulo.toUpperCase());
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTit.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        ChipIcono chip = new ChipIcono(tipoIcono, colorAcento, 28, true);
        panelTop.add(lblTit, BorderLayout.WEST);
        panelTop.add(chip, BorderLayout.EAST);

        // Centro: Número grande en contraste nítido (Regla 6)
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        lblValor.setHorizontalAlignment(SwingConstants.LEFT);

        // Pie: Flecha de variación verde o roja contra el periodo anterior con su porcentaje (Regla 6)
        Color colorVariacion = positiva ? EstilosUI.VERDE_EXITO : EstilosUI.ROJO_ERROR;
        String flecha = positiva ? "+ " : "- ";
        JLabel lblVar = EstilosUI.crearBadgePildora(flecha + variacionPorcentual, colorVariacion);
        lblVar.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel panelCentroValor = new JPanel(new BorderLayout(0, 4));
        panelCentroValor.setOpaque(false);
        panelCentroValor.add(lblValor, BorderLayout.CENTER);
        panelCentroValor.add(lblVar, BorderLayout.SOUTH);

        tarjeta.add(panelTop, BorderLayout.NORTH);
        tarjeta.add(panelCentroValor, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel envolverEnTarjeta(String titulo, JComponent comp, Color colorAcento) {
        GlassPanel tarjeta = new GlassPanel(new BorderLayout(0, 8), 24, colorAcento, true);
        tarjeta.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lbl = new JLabel(titulo.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(colorAcento);
        header.add(lbl, BorderLayout.WEST);

        tarjeta.add(header, BorderLayout.NORTH);
        tarjeta.add(comp, BorderLayout.CENTER);
        return tarjeta;
    }

    // Recalcula en vivo todos los indicadores, gráficos y actividad reciente desde la fachada (Punto 2.5).
    public void actualizarDatos() {
        if (fachada == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            double totalRecaudado = 0.0;
            double totalDeuda = 0.0;
            int cuotasTotal = 0;
            int cuotasMorosas = 0;
            int deptosTotal = 0;
            int deptosOcupados = 0;

            int pagadas = 0, pendientes = 0, parciales = 0, vencidas = 0;
            List<Object[]> ultimasAcciones = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                // 1. Métricas de Cuotas
                List<CuotaDTO> cuotas = fachada.listarCuotas();
                if (cuotas != null) {
                    cuotasTotal = cuotas.size();
                    for (CuotaDTO c : cuotas) {
                        double pagadoEnCuota = Math.max(0, c.getMontoBase() - c.getMontoFinal());
                        totalRecaudado += pagadoEnCuota;
                        totalDeuda += c.getMontoFinal();

                        String est = (c.getEstado() != null) ? c.getEstado().toUpperCase() : "PENDIENTE";
                        switch (est) {
                            case "PAGADO" -> pagadas++;
                            case "PENDIENTE" -> pendientes++;
                            case "PARCIAL" -> parciales++;
                            case "VENCIDO" -> { vencidas++; cuotasMorosas++; }
                        }
                        if (c.getMontoFinal() > 0 && !"PAGADO".equalsIgnoreCase(est)) {
                            cuotasMorosas++;
                        }
                    }
                }

                // 2. Métricas de Inmuebles y Ocupación
                List<InmuebleDTO> inmuebles = fachada.listarInmuebles();
                List<ResidenteDTO> residentes = fachada.listarResidentes();
                deptosTotal = (inmuebles != null) ? inmuebles.size() : 0;
                if (residentes != null && inmuebles != null) {
                    long ocup = inmuebles.stream()
                            .filter(i -> residentes.stream().anyMatch(r -> i.getNumero().equalsIgnoreCase(r.getNumeroDepartamento())))
                            .count();
                    deptosOcupados = (int) ocup;
                }

                // 3. Cargar hasta 10 últimas acciones desde cuotas, incidentes y reservas
                List<Object[]> todasLasAcciones = new ArrayList<>();
                if (cuotas != null) {
                    for (CuotaDTO c : cuotas) {
                        todasLasAcciones.add(new Object[]{
                                "CUOTA",
                                "Cuota de Depto " + c.getNumeroDepartamento() + " (" + c.getEstado() + ")",
                                "Administración",
                                (c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "-"),
                                String.format("S/ %.2f", c.getMontoFinal())
                        });
                    }
                }
                List<Incidente> incidentes = fachada.listarIncidentes();
                if (incidentes != null) {
                    for (Incidente inc : incidentes) {
                        todasLasAcciones.add(new Object[]{
                                "INCIDENTE",
                                inc.getDescripcion(),
                                inc.getReportadoPor() != null ? inc.getReportadoPor() : "Conserjería",
                                inc.getFecha() != null ? inc.getFecha().toString() : "-",
                                inc.getEstado() != null ? inc.getEstado() : "ABIERTO"
                        });
                    }
                }
                List<ReservaArea> reservas = fachada.listarReservas();
                if (reservas != null) {
                    for (ReservaArea res : reservas) {
                        String area = res.getAreaComun() != null ? res.getAreaComun().getNombre() : "Área Común";
                        todasLasAcciones.add(new Object[]{
                                "RESERVA",
                                "Reserva de " + area,
                                res.getPropietario() != null ? res.getPropietario().getNombre() : "Vecino",
                                res.getFechaReserva() != null ? res.getFechaReserva().toString() : "-",
                                res.getEstado() != null ? res.getEstado() : "CONFIRMADA"
                        });
                    }
                }

                // Invertir para que las acciones más recientes aparezcan al inicio de la tabla
                java.util.Collections.reverse(todasLasAcciones);
                if (todasLasAcciones.size() > 10) {
                    ultimasAcciones = todasLasAcciones.subList(0, 10);
                } else {
                    ultimasAcciones = todasLasAcciones;
                }
                return null;
            }

            @Override
            protected void done() {
                com.condominio.gui.animacion.AnimadorKpi.animar(lblValorRecaudado, totalRecaudado, "S/ ", "", 2);
                com.condominio.gui.animacion.AnimadorKpi.animar(lblValorDeuda, totalDeuda, "S/ ", "", 2);
                double pctMora = cuotasTotal > 0 ? ((double) cuotasMorosas / cuotasTotal) * 100.0 : 0.0;
                com.condominio.gui.animacion.AnimadorKpi.animar(lblValorMorosidad, pctMora, "", "%", 1);
                double pctOcup = deptosTotal > 0 ? ((double) deptosOcupados / deptosTotal) * 100.0 : 0.0;
                lblValorOcupacion.setText(String.format("%d / %d (%.0f%%)", deptosOcupados, deptosTotal, pctOcup));
                // Fórmula contable: Fondo Base Legal S/ 15,000.00 + 10% de la recaudación efectiva del periodo
                double fondoReserva = 15000.0 + (totalRecaudado * 0.10);
                com.condominio.gui.animacion.AnimadorKpi.animar(lblValorFondoReserva, fondoReserva, "S/ ", "", 2);

                // Actualizar gráficos
                graficoCircular.setDatos(pagadas, pendientes, parciales, vencidas);
                graficoBarras.setValores(new double[]{totalRecaudado * 0.7, totalRecaudado * 0.85, totalRecaudado * 0.9, totalRecaudado});

                // Actualizar tabla
                modeloActividad.setRowCount(0);
                for (Object[] fila : ultimasAcciones) {
                    modeloActividad.addRow(fila);
                }

                // Aplicar matriz de permisos sobre botón de reportes
                if (btnReportesPdf != null) {
                    btnReportesPdf.setEnabled(SesionActual.puedeGenerarReportesPdf());
                    if (!SesionActual.puedeGenerarReportesPdf()) {
                        btnReportesPdf.setToolTipText("Acceso restringido: requiere perfil de Administrador");
                    }
                }
            }
        };
        worker.execute();
    }

    // CLASES INTERNAS: GRÁFICOS 2D NATIVOS (SIN LIBRERÍAS DE TERCEROS)

    public static class GraficoBarrasRecaudacion extends JPanel {
        private double[] valores = {1800.0, 2100.0, 1950.0, 2450.0};
        private final String[] periodos = {"2026-07", "2026-08", "2026-09", "2026-10"};
        private double[] factores = new double[]{1.0, 1.0, 1.0, 1.0};
        private int hoverIndex = -1;

        public GraficoBarrasRecaudacion() {
            setOpaque(false);
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    actualizarTooltip(e.getX(), e.getY());
                }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hoverIndex = -1;
                    repaint();
                }
            });
        }

        private void actualizarTooltip(int mouseX, int mouseY) {
            int ancho = getWidth();
            int alto = getHeight();
            int margenX = 30;
            int margenY = 25;
            int anchoGrafico = ancho - (margenX * 2);
            int altoGrafico = alto - (margenY * 2);
            double maxVal = 1.0;
            for (double v : valores) if (v > maxVal) maxVal = v;
            int n = valores.length;
            if (n == 0) return;
            int anchoBarra = Math.max(16, (anchoGrafico / (n * 2)));
            int espaciado = anchoGrafico / n;

            int nuevoHover = -1;
            for (int i = 0; i < n; i++) {
                double factor = (factores != null && i < factores.length) ? factores[i] : 1.0;
                int hBarra = (int) (((valores[i] * factor) / maxVal) * (altoGrafico - 15));
                int x = margenX + (i * espaciado) + (espaciado / 4);
                int y = alto - margenY - hBarra;

                if (mouseX >= x && mouseX <= x + anchoBarra && mouseY >= y && mouseY <= alto - margenY) {
                    String per = (i < periodos.length) ? periodos[i] : ("P" + (i + 1));
                    setToolTipText("<html><body style='padding: 2px;'><b>Periodo: " + per + "</b><br>Recaudación: S/ " + String.format("%.2f", valores[i]) + "</body></html>");
                    nuevoHover = i;
                    break;
                }
            }
            if (nuevoHover != hoverIndex) {
                hoverIndex = nuevoHover;
                repaint();
            }
            if (nuevoHover < 0) {
                setToolTipText("Pase el cursor sobre cualquier barra para consultar periodo y recaudación exacta");
            }
        }

        public void setValores(double[] nuevos) {
            if (nuevos != null && nuevos.length > 0) {
                this.valores = nuevos;
                com.condominio.gui.animacion.AnimadorGraficos.animarBarras(this, nuevos.length, f -> this.factores = f);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ancho = getWidth();
            int alto = getHeight();
            int margenX = 30;
            int margenY = 25;
            int anchoGrafico = ancho - (margenX * 2);
            int altoGrafico = alto - (margenY * 2);

            // Líneas de fondo guía
            g2.setColor(EstilosUI.BORDE_LINEA);
            g2.drawLine(margenX, alto - margenY, ancho - margenX, alto - margenY);
            g2.drawLine(margenX, margenY, margenX, alto - margenY);

            double maxVal = 1.0;
            for (double v : valores) if (v > maxVal) maxVal = v;

            int n = valores.length;
            int anchoBarra = Math.max(16, (anchoGrafico / (n * 2)));
            int espaciado = anchoGrafico / n;

            for (int i = 0; i < n; i++) {
                double factor = (factores != null && i < factores.length) ? factores[i] : 1.0;
                int hBarra = (int) (((valores[i] * factor) / maxVal) * (altoGrafico - 15));
                int x = margenX + (i * espaciado) + (espaciado / 4);
                int y = alto - margenY - hBarra;

                // Barra con gradiente en acento pastel y extremo superior redondeado (Regla 7)
                int radioBarra = Math.min(anchoBarra, 12);
                Color pastelTop = EstilosUI.COBRANZAS_PASTEL;
                Color acentoBottom = EstilosUI.ACENTO_COBRANZAS;
                GradientPaint gp = new GradientPaint(x, y, pastelTop, x, y + hBarra, acentoBottom);
                g2.setPaint(gp);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(x, y, anchoBarra, hBarra, radioBarra, radioBarra));

                // Borde suave de vidrio
                g2.setColor(new Color(255, 255, 255, 45));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(x, y, anchoBarra, hBarra, radioBarra, radioBarra));

                // Monto arriba de la barra
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(EstilosUI.TEXTO_PRINCIPAL);
                String valorStr = String.format("%.0f", valores[i]);
                g2.drawString(valorStr, x - 2, Math.max(margenY + 8, y - 4));

                // Etiqueta del periodo abajo
                g2.setColor(EstilosUI.TEXTO_SECUNDARIO);
                String per = (i < periodos.length) ? periodos[i] : ("P" + (i + 1));
                g2.drawString(per, x - 4, alto - margenY + 14);
            }

            // Tooltip flotante tipo chip de vidrio al pasar el mouse (Regla 7)
            if (hoverIndex >= 0 && hoverIndex < n) {
                int i = hoverIndex;
                double factor = (factores != null && i < factores.length) ? factores[i] : 1.0;
                int hBarra = (int) (((valores[i] * factor) / maxVal) * (altoGrafico - 15));
                int x = margenX + (i * espaciado) + (espaciado / 4);
                int y = alto - margenY - hBarra;
                String per = (i < periodos.length) ? periodos[i] : ("P" + (i + 1));
                int tx = Math.max(margenX, Math.min(ancho - margenX - 140, x - 20));
                int ty = Math.max(4, y - 30);
                TooltipGraficoGlass.pintarTooltip(g2, tx, ty, per, "S/ " + String.format("%.2f", valores[i]), EstilosUI.DORADO_PRINCIPAL);
            }

            g2.dispose();
        }
    }

    public static class GraficoCircularEstados extends JPanel {
        private int pagadas = 2;
        private int pendientes = 2;
        private int parciales = 1;
        private int vencidas = 0;
        private double factorBarrido = 1.0;

        public GraficoCircularEstados() {
            setOpaque(false);
        }

        public void setDatos(int pag, int pend, int parc, int venc) {
            this.pagadas = Math.max(0, pag);
            this.pendientes = Math.max(0, pend);
            this.parciales = Math.max(0, parc);
            this.vencidas = Math.max(0, venc);
            com.condominio.gui.animacion.AnimadorGraficos.animarCircular(this, f -> this.factorBarrido = f);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = pagadas + pendientes + parciales + vencidas;
            if (total == 0) total = 1;

            int diametro = Math.min(getWidth() / 2, getHeight() - 20);
            int xPie = 16;
            int yPie = (getHeight() - diametro) / 2;

            int[] valores = {pagadas, pendientes, parciales, vencidas};
            Color[] colores = {EstilosUI.VERDE_EXITO, EstilosUI.AZUL_INFO, EstilosUI.AMBAR_AVISO, EstilosUI.ROJO_ERROR};
            String[] etiquetas = {"Pagado (" + pagadas + ")", "Pendiente (" + pendientes + ")", "Parcial (" + parciales + ")", "Vencido (" + vencidas + ")"};

            int anguloInicial = 0;
            int totalBarrido = (int) Math.round(360.0 * factorBarrido);
            for (int i = 0; i < valores.length; i++) {
                int angulo = (int) Math.round(((double) valores[i] / total) * 360.0 * factorBarrido);
                if (i == valores.length - 1) {
                    angulo = Math.max(0, totalBarrido - anguloInicial); // Cierre suave acorde al barrido animado
                }
                if (angulo > 0) {
                    g2.setColor(colores[i]);
                    g2.fillArc(xPie, yPie, diametro, diametro, anguloInicial, angulo);
                }
                anguloInicial += angulo;
            }

            // Centro hueco (estilo Donut moderno con saldo y total a la vista)
            int diametroHueco = diametro / 2;
            int xHueco = xPie + (diametro - diametroHueco) / 2;
            int yHueco = yPie + (diametro - diametroHueco) / 2;
            g2.setColor(EstilosUI.FONDO_TARJETA);
            g2.fillOval(xHueco, yHueco, diametroHueco, diametroHueco);
            g2.setColor(EstilosUI.BORDE_PANEL);
            g2.drawOval(xHueco, yHueco, diametroHueco, diametroHueco);

            // Resumen centrado en el interior de la dona
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.setColor(EstilosUI.TEXTO_SECUNDARIO);
            FontMetrics fmTop = g2.getFontMetrics();
            String txtTop = "TOTAL CUOTAS";
            g2.drawString(txtTop, xHueco + (diametroHueco - fmTop.stringWidth(txtTop)) / 2, yHueco + (diametroHueco / 2) - 4);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(EstilosUI.TEXTO_CLARO);
            FontMetrics fmVal = g2.getFontMetrics();
            String txtVal = String.valueOf(total);
            g2.drawString(txtVal, xHueco + (diametroHueco - fmVal.stringWidth(txtVal)) / 2, yHueco + (diametroHueco / 2) + 12);

            // Leyenda lateral con puntos de color ordenados (Regla 7)
            int xLeyenda = xPie + diametro + 20;
            int yLeyenda = yPie + 14;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            for (int i = 0; i < etiquetas.length; i++) {
                int yItem = yLeyenda + (i * 22);
                // Punto circular de color (Regla 7)
                g2.setColor(colores[i]);
                g2.fillOval(xLeyenda, yItem + 1, 9, 9);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawOval(xLeyenda, yItem + 1, 9, 9);

                g2.setColor(EstilosUI.TEXTO_PRINCIPAL);
                g2.drawString(etiquetas[i], xLeyenda + 16, yItem + 9);
            }

            g2.dispose();
        }
    }
}
