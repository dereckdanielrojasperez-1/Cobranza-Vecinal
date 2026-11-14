package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.regex.Pattern;

// Filtro de documento reutilizable para campos numéricos decimales.
public class FiltroDecimal extends DocumentFilter {

    private static final Pattern PATRON_DECIMAL = Pattern.compile("^\\d*(\\.\\d{0,2})?$");
    private final int longitudMaxima;

    // Constructor sin límite de longitud total.
    public FiltroDecimal() {
        this(0);
    }

    // Constructor con límite de longitud total de caracteres permitidos.
    public FiltroDecimal(int longitudMaxima) {
        this.longitudMaxima = Math.max(0, longitudMaxima);
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null || string.isEmpty()) {
            return;
        }
        String textoActual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String propuesto = textoActual.substring(0, offset) + string + textoActual.substring(offset);

        if (esValido(propuesto)) {
            super.insertString(fb, offset, string, attr);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String textoActual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String propuesto = textoActual.substring(0, offset) + (text != null ? text : "") + textoActual.substring(offset + length);

        if (esValido(propuesto)) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean esValido(String texto) {
        if (texto.isEmpty()) {
            return true;
        }
        if (longitudMaxima > 0 && texto.length() > longitudMaxima) {
            return false;
        }
        return PATRON_DECIMAL.matcher(texto).matches();
    }

    // Helper estático para aplicar el filtro decimal a un JTextField.
    public static void aplicar(JTextField campo) {
        aplicar(campo, 0);
    }

    // Helper estático para aplicar el filtro decimal con longitud máxima a un JTextField.
    public static void aplicar(JTextField campo, int longitudMaxima) {
        if (campo != null && campo.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new FiltroDecimal(longitudMaxima));
        }
    }
}
