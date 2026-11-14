package com.condominio.gui.util;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.regex.Pattern;

// Filtro de documento reutilizable para campos alfabéticos y de nomenclatura textual.
public class FiltroAlfabetico extends DocumentFilter {

    private final int longitudMaxima;
    private final boolean permitirEspacios;
    private final boolean permitirDigitos;
    private final boolean permitirGuiones;
    private final Pattern patronPermitido;

    // Constructor por defecto: letras, espacios, tildes y ñ sin límite de longitud.
    public FiltroAlfabetico() {
        this(0, true, false, false);
    }

    // Constructor con longitud máxima de caracteres (letras, espacios, tildes y ñ).
    public FiltroAlfabetico(int longitudMaxima) {
        this(longitudMaxima, true, false, false);
    }

    // Constructor parametrizable para diferentes tipos de campos alfabéticos.
    public FiltroAlfabetico(int longitudMaxima, boolean permitirEspacios, boolean permitirDigitos, boolean permitirGuiones) {
        this.longitudMaxima = Math.max(0, longitudMaxima);
        this.permitirEspacios = permitirEspacios;
        this.permitirDigitos = permitirDigitos;
        this.permitirGuiones = permitirGuiones;

        StringBuilder regex = new StringBuilder("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ");
        if (permitirDigitos) {
            regex.append("0-9");
        }
        if (permitirGuiones) {
            regex.append("\\-");
        }
        if (permitirEspacios) {
            regex.append(" ");
        }
        regex.append("]*$");
        this.patronPermitido = Pattern.compile(regex.toString());
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
        return patronPermitido.matcher(texto).matches();
    }

    // Aplica el filtro alfabético tradicional a un JTextField.
    public static void aplicar(JTextField campo) {
        aplicar(campo, 0);
    }

    // Aplica el filtro alfabético tradicional con límite de caracteres.
    public static void aplicar(JTextField campo, int longitudMaxima) {
        if (campo != null && campo.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new FiltroAlfabetico(longitudMaxima));
        }
    }

    // Aplica un filtro alfanumérico con guiones y sin espacios (adecuado para número de departamentos).
    public static void aplicarParaDepartamento(JTextField campo, int longitudMaxima) {
        if (campo != null && campo.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new FiltroAlfabetico(longitudMaxima, false, true, true));
        }
    }
}
