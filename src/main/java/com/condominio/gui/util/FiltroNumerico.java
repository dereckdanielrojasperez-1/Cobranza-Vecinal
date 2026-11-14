package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import java.awt.*;

// Filtro de documento reutilizable para campos de texto que admiten exclusivamente dígitos numéricos (0-9).
public class FiltroNumerico extends DocumentFilter {

    private final int longitudMaxima;

    // Constructor sin límite de longitud máxima.
    public FiltroNumerico() {
        this(0);
    }

    // Constructor con longitud máxima de caracteres permitidos.
    public FiltroNumerico(int longitudMaxima) {
        this.longitudMaxima = Math.max(0, longitudMaxima);
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null || string.isEmpty()) {
            return;
        }
        if (esNumerico(string) && longitudValida(fb, string.length(), 0)) {
            super.insertString(fb, offset, string, attr);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) {
            super.replace(fb, offset, length, null, attrs);
            return;
        }
        if (text.isEmpty()) {
            super.replace(fb, offset, length, text, attrs);
            return;
        }
        if (esNumerico(text) && longitudValida(fb, text.length(), length)) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean esNumerico(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean longitudValida(FilterBypass fb, int longitudNueva, int longitudReemplazada) {
        if (longitudMaxima <= 0) {
            return true;
        }
        int longitudActual = fb.getDocument().getLength();
        return (longitudActual - longitudReemplazada + longitudNueva) <= longitudMaxima;
    }

    // Helper estático para aplicar el filtro numérico a un JTextField.
    public static void aplicar(JTextField campo) {
        aplicar(campo, 0);
    }

    // Helper estático para aplicar el filtro numérico con límite de longitud a un JTextField.
    public static void aplicar(JTextField campo, int longitudMaxima) {
        if (campo != null && campo.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new FiltroNumerico(longitudMaxima));
        }
    }
}
