package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

// Sistema de diseño centralizado para el proyecto "Cobranza Vecinal".
public final class EstilosUI {

    // 1. FONDOS GENERALES (EXACTOS AL PROYECTO ORIGINAL E INFORME)

    public static final Color FONDO_PRINCIPAL    = new Color(11, 19, 30);   // #0B131E Canvas general
    public static final Color FONDO_TARJETA      = new Color(16, 26, 38);   // #101A26 Tarjetas y paneles
    public static final Color FONDO_CABECERA     = new Color(11, 19, 30);   // #0B131E Cabecera
    public static final Color BORDE_LINEA        = new Color(0x2A, 0x3A, 0x4E); // #2A3A4E Borde de paneles y tarjetas (C4)
    public static final Color FONDO_TABLA_CEBRA  = new Color(11, 19, 30);   // #0B131E Fila cebra
    public static final Color FONDO_CAMPO        = new Color(0x10, 0x1A, 0x26); // #101A26 Fondo interior de formularios (Regla 10)
    public static final Color FONDO_BARRA_ESTADO = new Color(9, 14, 23);    // #090E17 Barra inferior

    // 2. TIPOGRAFÍAS Y TEXTOS DE ALTO CONTRASTE

    public static final Color TEXTO_PRINCIPAL  = new Color(223, 231, 240); // #DFE7F0 Slate claro
    public static final Color TEXTO_CLARO      = new Color(255, 255, 255); // #FFFFFF Blanco puro
    public static final Color TEXTO_SECUNDARIO = new Color(143, 163, 184); // #8FA3B8 Slate 400
    public static final Color TEXTO_MUTED      = new Color(100, 116, 139); // #64748B Slate 500

    public static final Font FUENTE_TITULO_GRANDE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FUENTE_BANNER        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_TITULO        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_SUBTITULO     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_TEXTO         = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_REGULAR       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_LABEL         = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FUENTE_BOTON         = new Font("Segoe UI", Font.BOLD, 12);

    // 3. PALETA DE COLORES POR MÓDULO (SECCIÓN C - v2.1.0)

    // Módulo Inmuebles (Menta #58C99A)
    public static final Color INMUEBLES_NORMAL  = new Color(0x58, 0xC9, 0x9A); // #58C99A Borde y Botón
    public static final Color INMUEBLES_BRILLO  = new Color(0x7A, 0xDB, 0xB4); // #7ADBB4 Título y Resplandor
    public static final Color INMUEBLES_PASTEL  = new Color(0xC2, 0xF2, 0xDF); // #C2F2DF Menta Pastel
    public static final Color ACENTO_INMUEBLES  = INMUEBLES_NORMAL;

    // Módulo Residentes (Lavanda #B39DDB)
    public static final Color RESIDENTES_NORMAL  = new Color(0xB3, 0x9D, 0xDB); // #B39DDB Borde y Botón
    public static final Color RESIDENTES_BRILLO  = new Color(0xD1, 0xC4, 0xE9); // #D1C4E9 Título y Resplandor
    public static final Color RESIDENTES_PASTEL  = new Color(0xED, 0xE7, 0xF6); // #EDE7F6 Lavanda Pastel
    public static final Color ACENTO_RESIDENTES  = RESIDENTES_NORMAL;

    // Módulo Cobranzas (Ámbar #FFB454)
    public static final Color COBRANZAS_NORMAL  = new Color(0xFF, 0xB4, 0x54); // #FFB454 Borde y Botón
    public static final Color COBRANZAS_BRILLO  = new Color(0xFF, 0xCA, 0x85); // #FFCA85 Título Dorado
    public static final Color COBRANZAS_PASTEL  = new Color(0xFF, 0xE8, 0xC7); // #FFE8C7 Dorado Pastel
    public static final Color ACENTO_COBRANZAS  = COBRANZAS_NORMAL;

    // Módulo Operaciones (Cielo #6EC1E4)
    public static final Color OPERACIONES_NORMAL = new Color(0x6E, 0xC1, 0xE4); // #6EC1E4 Borde y Botón
    public static final Color OPERACIONES_BRILLO = new Color(0x9B, 0xD7, 0xEE); // #9BD7EE Título y Resplandor
    public static final Color OPERACIONES_PASTEL = new Color(0xDB, 0xF0, 0xF9); // #DBF0F9 Cielo Pastel
    public static final Color ACENTO_OPERACIONES = OPERACIONES_NORMAL;

    // Dorado Corporativo Principal
    public static final Color DORADO_PRINCIPAL = new Color(0xE8, 0xA3, 0x3D); // #E8A33D
    public static final Color DORADO_CLARO     = COBRANZAS_BRILLO;
    public static final Color DORADO_BORDE     = DORADO_PRINCIPAL;

    // Peligro / Deshacer (Rojo)
    public static final Color ROJO_NORMAL      = new Color(0xFF, 0x6B, 0x6B); // #FF6B6B
    public static final Color ROJO_BRILLO      = new Color(0xFF, 0x8A, 0x8A); // #FF8A8A
    public static final Color ROJO_PASTEL      = new Color(0xFF, 0xCD, 0xCD); // #FFCDCD
    public static final Color ROJO_PELIGRO     = ROJO_NORMAL;
    public static final Color ROJO_BORDE       = ROJO_NORMAL;
    public static final Color ROJO_ERROR_TEXTO = ROJO_NORMAL;

    // Estados Visuales y Mensajes Unificados (C7)
    public static final Color VERDE_EXITO = new Color(0x3D, 0xDC, 0x84); // Éxito verde #3DDC84
    public static final Color AMBAR_AVISO = new Color(0xE8, 0xC5, 0x47); // Aviso ámbar #E8C547
    public static final Color ROJO_ERROR  = new Color(0xFF, 0x6B, 0x6B); // Error rojo #FF6B6B
    public static final Color AZUL_INFO   = OPERACIONES_NORMAL;

    // Borde de paneles y tarjetas
    public static final Color BORDE_PANEL = new Color(0x2A, 0x3A, 0x4E); // #2A3A4E (C4)

    // Semántica de Estados en Celdas de Tabla y Badges
    public static final Color ESTADO_PENDIENTE_FONDO = new Color(15, 34, 61);   // #0F223D
    public static final Color ESTADO_PENDIENTE_TEXTO = new Color(77, 163, 255);  // #4DA3FF
    public static final Color ESTADO_PAGADO_FONDO    = new Color(13, 46, 31);   // #0D2E1F
    public static final Color ESTADO_PAGADO_TEXTO    = new Color(61, 220, 132);  // #3DDC84
    public static final Color ESTADO_PARCIAL_FONDO   = new Color(56, 32, 10);   // #38200A
    public static final Color ESTADO_PARCIAL_TEXTO   = new Color(232, 163, 61);  // #E8A33D
    public static final Color ESTADO_INCIDENTE_FONDO = new Color(58, 18, 18);   // #3A1212
    public static final Color ESTADO_INCIDENTE_TEXTO = new Color(255, 107, 107); // #FF6B6B
    public static final Color ESTADO_VENCIDO_FONDO   = ESTADO_INCIDENTE_FONDO;
    public static final Color ESTADO_VENCIDO_TEXTO   = ESTADO_INCIDENTE_TEXTO;
    public static final Color ESTADO_RESERVA_FONDO   = new Color(54, 14, 36);   // #360E24
    public static final Color ESTADO_RESERVA_TEXTO   = new Color(255, 107, 157); // #FF6B9D

    private EstilosUI() {
    }

    // 4. MÉTODOS DE RESOLUCIÓN CROMÁTICA

    public static Color obtenerColorNormal(Color acento) {
        if (acento == null) return DORADO_BORDE;
        if (acento.equals(ACENTO_INMUEBLES) || acento.equals(INMUEBLES_NORMAL) || acento.equals(INMUEBLES_PASTEL) || acento.equals(INMUEBLES_BRILLO)) return INMUEBLES_NORMAL;
        if (acento.equals(ACENTO_RESIDENTES) || acento.equals(RESIDENTES_NORMAL) || acento.equals(RESIDENTES_PASTEL) || acento.equals(RESIDENTES_BRILLO)) return RESIDENTES_NORMAL;
        if (acento.equals(ACENTO_COBRANZAS) || acento.equals(COBRANZAS_NORMAL) || acento.equals(COBRANZAS_PASTEL) || acento.equals(COBRANZAS_BRILLO)) return COBRANZAS_NORMAL;
        if (acento.equals(ACENTO_OPERACIONES) || acento.equals(OPERACIONES_NORMAL) || acento.equals(OPERACIONES_PASTEL) || acento.equals(OPERACIONES_BRILLO)) return OPERACIONES_NORMAL;
        if (acento.equals(ROJO_PELIGRO) || acento.equals(ROJO_NORMAL) || acento.equals(ROJO_PASTEL) || acento.equals(ROJO_BORDE)) return ROJO_NORMAL;
        return acento;
    }

    public static Color obtenerColorPastel(Color acento) {
        if (acento == null) return DORADO_CLARO;
        if (acento.equals(ACENTO_INMUEBLES) || acento.equals(INMUEBLES_NORMAL) || acento.equals(INMUEBLES_PASTEL) || acento.equals(INMUEBLES_BRILLO)) return INMUEBLES_PASTEL;
        if (acento.equals(ACENTO_RESIDENTES) || acento.equals(RESIDENTES_NORMAL) || acento.equals(RESIDENTES_PASTEL) || acento.equals(RESIDENTES_BRILLO)) return RESIDENTES_PASTEL;
        if (acento.equals(ACENTO_COBRANZAS) || acento.equals(COBRANZAS_NORMAL) || acento.equals(COBRANZAS_PASTEL) || acento.equals(COBRANZAS_BRILLO)) return COBRANZAS_PASTEL;
        if (acento.equals(ACENTO_OPERACIONES) || acento.equals(OPERACIONES_NORMAL) || acento.equals(OPERACIONES_PASTEL) || acento.equals(OPERACIONES_BRILLO)) return OPERACIONES_PASTEL;
        if (acento.equals(ROJO_PELIGRO) || acento.equals(ROJO_NORMAL) || acento.equals(ROJO_PASTEL) || acento.equals(ROJO_BORDE)) return ROJO_PASTEL;
        return acento;
    }

    public static Color obtenerBordeModulo(Color acento) {
        if (acento == null) return DORADO_PRINCIPAL;
        if (acento.equals(ACENTO_INMUEBLES) || acento.equals(INMUEBLES_NORMAL) || acento.equals(INMUEBLES_PASTEL) || acento.equals(INMUEBLES_BRILLO)) return INMUEBLES_BRILLO;
        if (acento.equals(ACENTO_RESIDENTES) || acento.equals(RESIDENTES_NORMAL) || acento.equals(RESIDENTES_PASTEL) || acento.equals(RESIDENTES_BRILLO)) return RESIDENTES_BRILLO;
        if (acento.equals(ACENTO_COBRANZAS) || acento.equals(COBRANZAS_NORMAL) || acento.equals(COBRANZAS_PASTEL) || acento.equals(COBRANZAS_BRILLO)) return COBRANZAS_BRILLO;
        if (acento.equals(ACENTO_OPERACIONES) || acento.equals(OPERACIONES_NORMAL) || acento.equals(OPERACIONES_PASTEL) || acento.equals(OPERACIONES_BRILLO)) return OPERACIONES_BRILLO;
        if (acento.equals(ROJO_PELIGRO) || acento.equals(ROJO_NORMAL) || acento.equals(ROJO_PASTEL) || acento.equals(ROJO_BORDE)) return ROJO_BORDE;
        return acento;
    }

    public static Color obtenerFondoSuaveModulo(Color acento) {
        Color base = obtenerColorNormal(acento);
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), 35);
    }

    public static Color aclarar(Color color, double factor) {
        if (color == null) return new Color(240, 240, 240);
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color oscurecer(Color color, double factor) {
        if (color == null) return new Color(180, 180, 180);
        int r = Math.max(0, (int) (color.getRed() * (1.0 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1.0 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1.0 - factor)));
        return new Color(r, g, b, color.getAlpha());
    }

    // 5. CONFIGURACIÓN DEL TEMA GLOBAL SWING

    public static void aplicarTemaGlobal() {
        UIManager.put("Panel.background", FONDO_PRINCIPAL);
        UIManager.put("Label.font", FUENTE_TEXTO);
        UIManager.put("Label.foreground", TEXTO_PRINCIPAL);
        UIManager.put("Button.font", FUENTE_BOTON);
        UIManager.put("Table.font", FUENTE_TEXTO);
        UIManager.put("Table.background", FONDO_TARJETA);
        UIManager.put("Table.foreground", TEXTO_PRINCIPAL);
        UIManager.put("Table.gridColor", BORDE_LINEA);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("TableHeader.background", FONDO_CABECERA);
        UIManager.put("TableHeader.foreground", DORADO_PRINCIPAL);
        UIManager.put("TextField.font", FUENTE_TEXTO);
        UIManager.put("TextField.background", FONDO_CAMPO);
        UIManager.put("TextField.foreground", TEXTO_PRINCIPAL);
        UIManager.put("TextField.caretForeground", TEXTO_PRINCIPAL);
        UIManager.put("ComboBox.font", FUENTE_TEXTO);
        UIManager.put("ComboBox.background", FONDO_CAMPO);
        UIManager.put("ComboBox.foreground", TEXTO_PRINCIPAL);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.background", FONDO_PRINCIPAL);
        UIManager.put("TabbedPane.foreground", TEXTO_PRINCIPAL);
        UIManager.put("ScrollPane.background", FONDO_PRINCIPAL);
        UIManager.put("Viewport.background", FONDO_TARJETA);
        UIManager.put("OptionPane.background", FONDO_TARJETA);
        UIManager.put("OptionPane.messageForeground", TEXTO_PRINCIPAL);
        UIManager.put("OptionPane.messageArea.background", FONDO_TARJETA);
        UIManager.put("Panel.background", FONDO_TARJETA);

        // ToolTips oscuro corporativo (Corrección 6)
        UIManager.put("ToolTip.background", new Color(0x16, 0x20, 0x2E));
        UIManager.put("ToolTip.foreground", new Color(0xDF, 0xE7, 0xF0));
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(0x2A, 0x3A, 0x4E), 1));
        UIManager.put("ToolTip.font", FUENTE_TEXTO);

        // Menús y Popups oscuros corporativos (elimina fondos blancos nativos de Windows)
        UIManager.put("PopupMenu.background", FONDO_TARJETA);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDE_LINEA, 1));
        UIManager.put("MenuItem.background", FONDO_TARJETA);
        UIManager.put("MenuItem.foreground", TEXTO_PRINCIPAL);
        UIManager.put("MenuItem.selectionBackground", new Color(0x2A, 0x3A, 0x4E));
        UIManager.put("MenuItem.selectionForeground", TEXTO_CLARO);
    }

    // 6. ESTILIZACIÓN DE BOTONES (RÉPLICA EXACTA DEL MODELO INICIAL)

    // Estiliza un botón primario con fondo sólido y contorno redondeado:
    public static void estilizarBoton(JButton boton, Color acento, Color colorBorde) {
        final Color cNormal = obtenerColorNormal(acento);
        final Color cBorde  = (colorBorde != null) ? colorBorde : obtenerBordeModulo(acento);
        final boolean esPeligro = acento.equals(ROJO_PELIGRO) || (colorBorde != null && colorBorde.equals(ROJO_BORDE));
        // Regla 1.1: Texto foreground #0D1520 sobre el acento del módulo (menta, lavanda, ámbar, cielo)
        final Color colorTexto = esPeligro ? Color.WHITE : new Color(0x0D, 0x15, 0x20);

        boton.setFont(FUENTE_BOTON);
        boton.setForeground(colorTexto);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(7, 16, 7, 16));

        // Pulso de botón: en mousePressed suma 10% de luminosidad extra y regresa en 120 ms
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!ConstantesUI.ANIMACIONES_ACTIVAS || GraphicsEnvironment.isHeadless()) return;
                boton.putClientProperty("PULSO_ACTIVO", Boolean.TRUE);
                boton.repaint();
                Timer t = new Timer(120, ev -> {
                    boton.putClientProperty("PULSO_ACTIVO", Boolean.FALSE);
                    boton.repaint();
                });
                t.setRepeats(false);
                t.start();
            }
        });

        boton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fondoActual = cNormal;
                Color bordeActual = cBorde;

                if (!b.isEnabled()) {
                    fondoActual = new Color(30, 42, 56);
                    bordeActual = new Color(50, 65, 82);
                } else if (Boolean.TRUE.equals(b.getClientProperty("PULSO_ACTIVO"))) {
                    fondoActual = aclarar(cNormal, 0.20);
                    bordeActual = Color.WHITE;
                } else if (model.isPressed()) {
                    fondoActual = oscurecer(cNormal, 0.18);
                    bordeActual = oscurecer(cBorde, 0.18);
                } else if (model.isRollover()) {
                    // Hover aclara el fondo un 10% y mantiene el texto oscuro (1.1)
                    fondoActual = aclarar(cNormal, 0.10);
                    bordeActual = Color.WHITE;
                }

                // Relleno sólido redondeado con radio 14 (Regla 11)
                g2.setColor(fondoActual);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 14, 14);

                // Hover de vidrio: shimmer blanco al 4% sutil sin cambiar fondo ni texto
                if (model.isRollover() && b.isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 10)); // +4% alfa
                    g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 14, 14);
                }

                // Borde redondeado definido con radio 14
                g2.setColor(bordeActual);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 14, 14);

                // Texto centrado y nítido
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                Rectangle viewRect = new Rectangle(0, 0, b.getWidth(), b.getHeight());
                Rectangle iconRect = new Rectangle();
                Rectangle textRect = new Rectangle();

                String text = SwingUtilities.layoutCompoundLabel(
                        c, fm, b.getText(), b.getIcon(),
                        b.getVerticalAlignment(), b.getHorizontalAlignment(),
                        b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                        viewRect, iconRect, textRect,
                        b.getText() == null ? 0 : b.getIconTextGap());

                g2.setFont(b.getFont());
                g2.setColor(b.isEnabled() ? colorTexto : new Color(130, 145, 160));
                if (text != null && !text.isEmpty()) {
                    g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                }
                g2.dispose();
            }
        });
    }

    public static void estilizarBoton(JButton boton, Color acento) {
        estilizarBoton(boton, acento, null);
    }

    public static void estilizarBotonPrimario(JButton boton) {
        estilizarBoton(boton, DORADO_PRINCIPAL, null);
    }

    // Estiliza un botón secundario: fondo oscuro de tarjeta (#101A26), contorno en el color
    public static void estilizarBotonSecundario(JButton boton, Color acento) {
        final Color cNormal = obtenerColorNormal(acento);
        final Color cBorde  = obtenerBordeModulo(acento);

        boton.setFont(FUENTE_BOTON);
        boton.setForeground(TEXTO_PRINCIPAL);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(7, 16, 7, 16));

        boton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fondoActual = new Color(0x16, 0x20, 0x2E); // Fondo #16202E (C2)
                Color bordeActual = cBorde;
                Color textoActual = cBorde; // Texto en el color de acento (C2)

                if (!b.isEnabled()) {
                    fondoActual = new Color(0x16, 0x20, 0x2E);
                    bordeActual = BORDE_LINEA;
                    textoActual = TEXTO_MUTED;
                } else if (model.isPressed()) {
                    fondoActual = new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), (int)(255 * 0.20));
                    bordeActual = Color.WHITE;
                    textoActual = Color.WHITE;
                } else if (model.isRollover()) {
                    // Hover teñido al 12 por ciento (C2)
                    fondoActual = new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), (int)(255 * 0.12));
                    bordeActual = cBorde;
                    textoActual = Color.WHITE;
                }

                // Fondo redondeado oscuro con radio 14 (Regla 11)
                g2.setColor(fondoActual);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 14, 14);

                // Hover de vidrio: shimmer blanco 4% sutil
                if (model.isRollover() && b.isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 10)); // +4% alfa
                    g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 14, 14);
                }

                // Borde redondeado con el acento luminoso y radio 14
                g2.setColor(bordeActual);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 14, 14);

                // Texto centrado claro
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                Rectangle viewRect = new Rectangle(0, 0, b.getWidth(), b.getHeight());
                Rectangle iconRect = new Rectangle();
                Rectangle textRect = new Rectangle();

                String text = SwingUtilities.layoutCompoundLabel(
                        c, fm, b.getText(), b.getIcon(),
                        b.getVerticalAlignment(), b.getHorizontalAlignment(),
                        b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                        viewRect, iconRect, textRect,
                        b.getText() == null ? 0 : b.getIconTextGap());

                g2.setFont(b.getFont());
                g2.setColor(textoActual);
                if (text != null && !text.isEmpty()) {
                    g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                }
                g2.dispose();
            }
        });
    }

    // 7. ESTILIZACIÓN DE COMBOS, CAMPOS Y TABLAS

    public static <T> void estilizarCombo(JComboBox<T> combo, Color acento) {
        final Color cNormal = obtenerColorNormal(acento);
        final Color cBorde  = obtenerBordeModulo(acento);
        final Color fondoCombo = new Color(0x10, 0x1A, 0x26); // Fondo #101A26 (1.2)

        combo.setBackground(fondoCombo);
        combo.setForeground(cNormal); // Texto en el color de acento del módulo (1.2)
        combo.setFont(FUENTE_TEXTO);

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(cNormal);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        int[] xPoints = {cx - 4, cx + 4, cx};
                        int[] yPoints = {cy - 2, cy - 2, cy + 3};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.dispose();
                    }
                };
                btn.setBackground(fondoCombo);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 8));
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Elimina relleno sólido de color: Fondo oscuro #101A26 (1.2)
                g2.setColor(fondoCombo);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        // Borde de 1 pixel del acento (1.2)
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cBorde, 1, true),
                new EmptyBorder(3, 8, 3, 4)
        ));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                etiqueta.setFont(FUENTE_TEXTO);
                etiqueta.setBorder(new EmptyBorder(6, 10, 6, 10));

                if (index == -1) {
                    // Item seleccionado cuando el combo está cerrado: fondo #101A26 y texto en acento (1.2)
                    etiqueta.setOpaque(false);
                    etiqueta.setBackground(fondoCombo);
                    etiqueta.setForeground(cNormal);
                } else {
                    // Lista desplegable: fondo oscuro #101A26
                    list.setBackground(fondoCombo);
                    if (isSelected) {
                        // Tinte del acento al 12 por ciento y texto #DFE7F0 (1.2)
                        etiqueta.setOpaque(true);
                        etiqueta.setBackground(new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), (int)(255 * 0.12)));
                        etiqueta.setForeground(new Color(0xDF, 0xE7, 0xF0));
                    } else {
                        etiqueta.setOpaque(true);
                        etiqueta.setBackground(fondoCombo);
                        etiqueta.setForeground(new Color(0xDF, 0xE7, 0xF0));
                    }
                }
                return etiqueta;
            }
        });
    }

    public static <T> JComboBox<T> crearCombo() {
        JComboBox<T> combo = new JComboBox<>();
        estilizarCombo(combo, DORADO_PRINCIPAL);
        return combo;
    }

    public static void estilizarTabla(JTable tabla, Color acento) {
        final Color cNormal = obtenerColorNormal(acento);
        final Color cBorde  = obtenerBordeModulo(acento);
        final Color fondoCabeceraModulo = FONDO_CABECERA;

        tabla.setBackground(FONDO_TARJETA);
        tabla.setForeground(TEXTO_PRINCIPAL);
        tabla.setFont(FUENTE_TEXTO);
        tabla.setRowHeight(28);
        tabla.setGridColor(BORDE_LINEA);
        tabla.setSelectionBackground(new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), 75));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowGrid(true);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(fondoCabeceraModulo);
        header.setForeground(cBorde);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        // Gradiente del acento al 30 por ciento sobre #16202E (1.3)
                        Color cTop = new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), (int)(255 * 0.30));
                        Color cBottom = new Color(0x16, 0x20, 0x2E);
                        GradientPaint gp = new GradientPaint(0, 0, cTop, 0, getHeight(), cBottom);
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        // Línea inferior de 2px en el acento pleno (1.3)
                        g2.setColor(cNormal);
                        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                        // Línea divisoria vertical
                        g2.setColor(new Color(0x2A, 0x3A, 0x4E));
                        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 2);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setOpaque(false);
                lbl.setForeground(new Color(0xDF, 0xE7, 0xF0)); // #DFE7F0 en negrita (1.3)
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x2A, 0x3A, 0x4E)),
                        new EmptyBorder(4, 6, 4, 6)
                ));
                return lbl;
            }
        });

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(cNormal.getRed(), cNormal.getGreen(), cNormal.getBlue(), 80));
                    c.setForeground(Color.WHITE);
                } else {
                    // Filas cebra con alfa 4 por ciento alternado sobre #16202E (Regla 9)
                    c.setBackground((row % 2 == 1) ? new Color(255, 255, 255, 10) : FONDO_TARJETA);
                    c.setForeground(TEXTO_PRINCIPAL);
                }
                return c;
            }
        });
    }

    public static void estilizarTabla(JTable tabla) {
        estilizarTabla(tabla, DORADO_PRINCIPAL);
    }

    // Construye una tabla Swing estilizada que muestra la leyenda 'Sin registros para mostrar'
    public static JTable crearTablaEstilizada(DefaultTableModel modelo, Color acento) {
        JTable tabla = new JTable(modelo) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Destello de fila animado (Grupo 2 Feedback)
                Object alphaProp = getClientProperty("DESTELLO_ALPHA");
                Object filaProp = getClientProperty("DESTELLO_FILA");
                if (alphaProp instanceof Float && filaProp instanceof Integer) {
                    float a = (Float) alphaProp;
                    int f = (Integer) filaProp;
                    if (a > 0.001f && f >= 0 && f < getRowCount()) {
                        Color col = (Color) getClientProperty("DESTELLO_COLOR");
                        if (col == null) col = acento;
                        Graphics2D gFlash = (Graphics2D) g.create();
                        gFlash.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int) (255 * a)));
                        Rectangle r = getCellRect(f, 0, true);
                        gFlash.fillRect(0, r.y, getWidth(), r.height);
                        gFlash.dispose();
                    }
                }

                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.setColor(TEXTO_MUTED);
                    String texto = "Sin registros para mostrar";
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(texto)) / 2;
                    int y = Math.max(35, (getHeight() / 2));
                    g2.drawString(texto, Math.max(10, x), y);
                    g2.dispose();
                }
            }
        };
        estilizarTabla(tabla, acento);
        return tabla;
    }

    public static void estilizarCampo(JTextField campo, Color acento) {
        final Color cBorde = obtenerBordeModulo(acento);
        final Color cBordeInactivo = BORDE_LINEA; // Borde estructural 1px #2A3A4E (Regla 10)

        campo.setBackground(FONDO_CAMPO); // Fondo interior #101A26 (Regla 10)
        campo.setForeground(TEXTO_PRINCIPAL);
        campo.setCaretColor(TEXTO_PRINCIPAL);
        campo.setFont(FUENTE_TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                crearBordeRedondeado(8, cBordeInactivo),
                new EmptyBorder(4, 8, 4, 8)
        ));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Borde de foco en el acento del módulo (Regla 10)
                campo.setBorder(BorderFactory.createCompoundBorder(
                        crearBordeRedondeado(8, cBorde),
                        new EmptyBorder(4, 8, 4, 8)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        crearBordeRedondeado(8, cBordeInactivo),
                        new EmptyBorder(4, 8, 4, 8)
                ));
            }
        });
    }

    public static void estilizarTextField(JTextField campo) {
        estilizarCampo(campo, DORADO_PRINCIPAL);
    }

    public static CampoPlaceholder crearCampoPlaceholder(String placeholder, Color acento) {
        CampoPlaceholder campo = new CampoPlaceholder(placeholder);
        estilizarCampo(campo, acento);
        return campo;
    }

    public static CampoPlaceholder crearCampoPlaceholder(String placeholder) {
        return crearCampoPlaceholder(placeholder, DORADO_PRINCIPAL);
    }

    public static JTextField crearCampoTexto(Color acento) {
        JTextField campo = new JTextField();
        estilizarCampo(campo, acento);
        return campo;
    }

    public static JTextField crearCampoTexto() {
        return crearCampoTexto(DORADO_PRINCIPAL);
    }

    // Configura el valor prototipo de visualización de un JComboBox para prevenir que su texto se trunque
    public static <T> void configurarComboPrototipoYTooltip(JComboBox<T> combo, T valorPrototipo) {
        if (combo == null) return;
        if (valorPrototipo != null) {
            combo.setPrototypeDisplayValue(valorPrototipo);
        }
        Object sel = combo.getSelectedItem();
        if (sel != null) {
            combo.setToolTipText(sel.toString());
        }
        combo.addActionListener(e -> {
            Object nuevoSel = combo.getSelectedItem();
            if (nuevoSel != null) {
                combo.setToolTipText(nuevoSel.toString());
            }
        });
    }

    public static JLabel crearEtiquetaCampo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_LABEL);
        etiqueta.setForeground(TEXTO_PRINCIPAL);
        return etiqueta;
    }

    public static Border crearBordeRedondeado(int radio, Color color) {
        return new RoundedBorder(radio, color);
    }

    public static JPanel crearPanelRedondeado(Color fondo, int radio, Color colorBorde) {
        return new PanelRedondeado(radio, fondo, colorBorde);
    }

    public static JPanel crearPanelRedondeado(Color fondo) {
        return new PanelRedondeado(12, fondo, BORDE_LINEA);
    }

    // Construye un banner superior con gradiente vertical sutil de #16202E a #101A26
    public static JPanel crearPanelBanner() {
        return crearPanelBanner(null);
    }

    public static Border crearBordeModuloSuave(Color acento) {
        Color base = obtenerColorNormal(acento);
        return crearBordeRedondeado(12, new Color(base.getRed(), base.getGreen(), base.getBlue(), 80));
    }

    public static JPanel crearPanelBanner(Color colorBorde) {
        final Color cBorde = (colorBorde != null) ? colorBorde : BORDE_PANEL;
        GlassPanel panel = new GlassPanel(new BorderLayout(0, 4), 24, cBorde, true);
        panel.setBorder(new EmptyBorder(12, 18, 12, 18));
        return panel;
    }

    // Construye un contenedor estilo Frosted Glass (Glassmorphism) con gradiente
    public static JPanel crearPanelGlassmorphic(Color acento, int radio) {
        return new GlassPanel(radio, acento);
    }

    public static JPanel crearPanelGlassmorphic(Color acento) {
        return new GlassPanel(24, acento);
    }

    // Estiliza un botón interactivo o pestaña como píldora / cápsula redondeada
    public static void estilizarBotonPildora(JButton boton, Color acento, boolean activo) {
        if (boton == null) return;
        final Color cAcento = (acento != null) ? acento : DORADO_PRINCIPAL;
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(6, 16, 6, 16));

        boton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int radioPildora = Math.min(b.getHeight(), 28);
                Color fondo;
                Color borde;
                Color texto;

                if (!b.isEnabled()) {
                    fondo = new Color(20, 28, 38, 140);
                    borde = new Color(42, 58, 78, 80);
                    texto = TEXTO_MUTED;
                } else if (activo) {
                    // Píldora activa con resplandor del acento
                    fondo = model.isPressed()
                            ? new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 95)
                            : (model.isRollover()
                                    ? new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 75)
                                    : new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 55));
                    borde = cAcento;
                    texto = (cAcento.getRed() * 0.299 + cAcento.getGreen() * 0.587 + cAcento.getBlue() * 0.114 > 165)
                            ? cAcento : Color.WHITE;
                } else {
                    // Píldora inactiva en Dark Slate traslúcido
                    fondo = model.isRollover()
                            ? new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 35)
                            : new Color(16, 26, 38, 180);
                    borde = model.isRollover()
                            ? new Color(cAcento.getRed(), cAcento.getGreen(), cAcento.getBlue(), 120)
                            : new Color(42, 58, 78, 140);
                    texto = model.isRollover() ? TEXTO_CLARO : TEXTO_SECUNDARIO;
                }

                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, radioPildora, radioPildora);

                // Borde redondeado cápsula
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(activo ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, radioPildora, radioPildora);

                // Texto centrado
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                Rectangle viewRect = new Rectangle(0, 0, b.getWidth(), b.getHeight());
                Rectangle iconRect = new Rectangle();
                Rectangle textRect = new Rectangle();

                String text = SwingUtilities.layoutCompoundLabel(
                        c, fm, b.getText(), b.getIcon(),
                        b.getVerticalAlignment(), b.getHorizontalAlignment(),
                        b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                        viewRect, iconRect, textRect,
                        b.getText() == null ? 0 : b.getIconTextGap());

                g2.setFont(b.getFont());
                g2.setColor(texto);
                if (text != null && !text.isEmpty()) {
                    g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                }
                g2.dispose();
            }
        });
    }

    // Estiliza un JTabbedPane para tema oscuro corporativo de alto contraste, eliminando pestañas blancas del sistema
    public static void estilizarPestanas(JTabbedPane tabbedPane, Color acento) {
        if (tabbedPane == null) return;
        final Color cAcento = (acento != null) ? acento : DORADO_PRINCIPAL;
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(FONDO_PRINCIPAL);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFocusable(false);
        tabbedPane.setOpaque(false);

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = BORDE_LINEA;
                lightHighlight = BORDE_LINEA;
                shadow = FONDO_PRINCIPAL;
                darkShadow = FONDO_PRINCIPAL;
                focus = cAcento;
                tabInsets = new Insets(8, 16, 8, 16);
                selectedTabPadInsets = new Insets(2, 2, 2, 2);
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(FONDO_PRINCIPAL);
                g2.fillRect(0, 0, tabbedPane.getWidth(), tabbedPane.getHeight());
                g2.dispose();
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    // Pestaña activa: fondo azul pizarra oscuro con línea indicadora brillante
                    g2.setColor(new Color(0x18, 0x25, 0x38));
                    g2.fillRect(x, y, w, h);

                    g2.setColor(cAcento);
                    g2.fillRect(x, y + h - 3, w, 3);
                } else {
                    // Pestaña inactiva: tono oscuro corporativo nítido
                    g2.setColor(new Color(0x0E, 0x16, 0x22));
                    g2.fillRect(x, y, w, h);
                }
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);

                if (isSelected) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(0x94, 0xA3, 0xB8)); // Slate-400 visible y de alto contraste
                }
                g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDE_LINEA);
                g2.drawRect(x, y, w, h);
                g2.dispose();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDE_LINEA);
                g2.drawRect(0, 0, tabbedPane.getWidth() - 1, tabbedPane.getHeight() - 1);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Suprimir el rectángulo punteado nativo para estética moderna
            }
        });
    }

    public static void estilizarPestanas(JTabbedPane tabbedPane) {
        estilizarPestanas(tabbedPane, DORADO_PRINCIPAL);
    }

    // Construye una insignia tipo píldora (Badge) compacta con micro-borde y fondo traslúcido
    public static JLabel crearBadgePildora(String texto, Color colorAcento) {
        JLabel badge = new JLabel(texto, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondo = new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 35);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.setColor(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(colorAcento);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setOpaque(false);
        return badge;
    }

    // 8. CLASES INTERNAS DE SOPORTE VISUAL

    // Panel con acabado Glassmorphism: Hereda de GlassPanel cumpliendo las reglas de
    public static class PanelGlassmorphic extends GlassPanel {
        public PanelGlassmorphic(int radio, Color acento) {
            super(radio, acento);
        }

        public PanelGlassmorphic(Color acento) {
            super(24, acento);
        }
    }

    public static class RoundedBorder implements Border {
        private final int radio;
        private final Color color;
        private final int grosor;

        public RoundedBorder(int radio, Color color) {
            this(radio, color, 1);
        }

        public RoundedBorder(int radio, Color color, int grosor) {
            this.radio = radio;
            this.color = color;
            this.grosor = grosor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color != null ? color : BORDE_LINEA);
            g2.setStroke(new BasicStroke(grosor));
            g2.drawRoundRect(x + grosor / 2, y + grosor / 2, width - grosor, height - grosor, radio, radio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radio / 3 + 2, radio / 3 + 4, radio / 3 + 2, radio / 3 + 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public static class PanelRedondeado extends JPanel {
        private final int radio;
        private Color colorFondo;
        private Color colorBorde;

        public PanelRedondeado(int radio, Color fondo, Color borde) {
            this.radio = radio;
            this.colorFondo = fondo;
            this.colorBorde = borde;
            setOpaque(false);
        }

        public PanelRedondeado(Color fondo) {
            this(12, fondo, BORDE_LINEA);
        }

        public void setColorFondo(Color c) {
            this.colorFondo = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorFondo != null ? colorFondo : FONDO_TARJETA);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            if (colorBorde != null) {
                g2.setColor(colorBorde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 13. MENÚS CONTEXTUALES Y DE DESBORDE OSCUROS (ZERO FONDOS BLANCOS)

    // Crea un JPopupMenu corporativo con fondo oscuro sólido (#101A26), borde cromático
    public static JPopupMenu crearMenuDesbordeOscuro(Color acento) {
        final Color cBorde = (acento != null) ? acento : BORDE_LINEA;
        JPopupMenu menu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(FONDO_TARJETA);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        menu.setOpaque(true);
        menu.setBackground(FONDO_TARJETA);
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cBorde, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return menu;
    }

    // Crea un JMenuItem estilizado con BasicMenuItemUI para garantizar fondo oscuro,
    public static JMenuItem crearItemMenuOscuro(String texto, Color colorTexto, Color hoverAcento) {
        JMenuItem item = new JMenuItem(texto);
        final Color cTexto = (colorTexto != null) ? colorTexto : TEXTO_PRINCIPAL;
        final Color cHover = (hoverAcento != null) ? hoverAcento : DORADO_PRINCIPAL;

        item.setUI(new BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color defaultBg) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = menuItem.getModel();
                if (model.isArmed() || model.isRollover() || model.isPressed()) {
                    g2.setColor(new Color(0x2A, 0x3A, 0x4E));
                    g2.fillRoundRect(2, 1, menuItem.getWidth() - 4, menuItem.getHeight() - 2, 4, 4);
                    g2.setColor(cHover);
                    g2.drawRoundRect(2, 1, menuItem.getWidth() - 4, menuItem.getHeight() - 2, 4, 4);
                } else {
                    g2.setColor(FONDO_TARJETA);
                    g2.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                }
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                ButtonModel model = menuItem.getModel();
                g2.setFont(menuItem.getFont());
                if (model.isArmed() || model.isRollover() || model.isPressed()) {
                    g2.setColor(cHover);
                } else {
                    g2.setColor(cTexto);
                }
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                g2.dispose();
            }
        });

        item.setOpaque(false);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setForeground(cTexto);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return item;
    }

    // Agrega un separador con línea oscura (#2A3A4E) al menú.
    public static void agregarSeparadorOscuro(JPopupMenu menu) {
        JSeparator sep = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(BORDE_LINEA);
                g.drawLine(4, getHeight() / 2, getWidth() - 4, getHeight() / 2);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(10, 6));
        menu.add(sep);
    }
}
