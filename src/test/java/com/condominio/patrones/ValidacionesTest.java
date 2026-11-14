package com.condominio.patrones;

import com.condominio.gui.util.FiltroAlfabetico;
import com.condominio.gui.util.FiltroDecimal;
import com.condominio.gui.util.FiltroNumerico;
import com.condominio.gui.util.Validaciones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas unitarias para validar las clases de filtros de documento,
public class ValidacionesTest {

    // 1. PRUEBAS DE FILTROS DE DOCUMENTO

    @Test
    @DisplayName("FiltroNumerico: debe admitir solo digitos y respetar longitud maxima")
    public void testFiltroNumerico() throws BadLocationException {
        JTextField campo = new JTextField();
        FiltroNumerico.aplicar(campo, 8);
        AbstractDocument doc = (AbstractDocument) campo.getDocument();

        // Inserción válida de dígitos
        doc.insertString(0, "12345678", null);
        assertEquals("12345678", campo.getText());

        // Intentar exceder la longitud máxima de 8
        doc.insertString(8, "9", null);
        assertEquals("12345678", campo.getText());

        // Intentar insertar letras (debe ser bloqueado)
        JTextField campoLetras = new JTextField();
        FiltroNumerico.aplicar(campoLetras, 5);
        AbstractDocument docLetras = (AbstractDocument) campoLetras.getDocument();
        docLetras.insertString(0, "abc12", null);
        assertEquals("", campoLetras.getText());
    }

    @Test
    @DisplayName("FiltroDecimal: debe admitir digitos con hasta 2 decimales y un solo punto")
    public void testFiltroDecimal() throws BadLocationException {
        JTextField campo = new JTextField();
        FiltroDecimal.aplicar(campo, 8);
        AbstractDocument doc = (AbstractDocument) campo.getDocument();

        doc.insertString(0, "150.75", null);
        assertEquals("150.75", campo.getText());

        // Intentar agregar un tercer decimal (debe ser bloqueado)
        doc.insertString(6, "9", null);
        assertEquals("150.75", campo.getText());

        // Intentar agregar un segundo punto decimal (debe ser bloqueado)
        doc.insertString(6, ".", null);
        assertEquals("150.75", campo.getText());

        // Intentar pegar texto con letras (debe ser bloqueado)
        JTextField campoTexto = new JTextField();
        FiltroDecimal.aplicar(campoTexto, 10);
        AbstractDocument docTexto = (AbstractDocument) campoTexto.getDocument();
        docTexto.insertString(0, "100.5A", null);
        assertEquals("", campoTexto.getText());
    }

    @Test
    @DisplayName("FiltroAlfabetico: debe admitir letras, tildes, n y espacios")
    public void testFiltroAlfabetico() throws BadLocationException {
        JTextField campo = new JTextField();
        FiltroAlfabetico.aplicar(campo, 50);
        AbstractDocument doc = (AbstractDocument) campo.getDocument();

        doc.insertString(0, "María Peña Gómez", null);
        assertEquals("María Peña Gómez", campo.getText());

        // Intentar insertar números en campo estrictamente alfabético
        doc.insertString(doc.getLength(), "123", null);
        assertEquals("María Peña Gómez", campo.getText());
    }

    @Test
    @DisplayName("FiltroAlfabetico para Departamento: admite digitos y guiones pero no espacios")
    public void testFiltroDepartamento() throws BadLocationException {
        JTextField campo = new JTextField();
        FiltroAlfabetico.aplicarParaDepartamento(campo, 6);
        AbstractDocument doc = (AbstractDocument) campo.getDocument();

        doc.insertString(0, "A-101", null);
        assertEquals("A-101", campo.getText());

        // Intentar insertar espacio (debe ser rechazado)
        doc.insertString(doc.getLength(), " ", null);
        assertEquals("A-101", campo.getText());
    }

    // 2. PRUEBAS DE VALIDACIONES DE INMUEBLES

    @Test
    @DisplayName("Validar Numero de Departamento: 1 a 6 caracteres sin espacios")
    public void testValidarNumeroDepartamento() {
        assertDoesNotThrow(() -> Validaciones.validarNumeroDepartamento("101"));
        assertDoesNotThrow(() -> Validaciones.validarNumeroDepartamento("A-201"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNumeroDepartamento(""));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNumeroDepartamento("   "));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNumeroDepartamento("101 A"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNumeroDepartamento("DEPTO-101")); // Excede 6 caracteres
    }

    @Test
    @DisplayName("Validar Piso: entero entre 1 y 30")
    public void testValidarPiso() {
        assertEquals(1, Validaciones.validarPiso("1"));
        assertEquals(15, Validaciones.validarPiso("15"));
        assertEquals(30, Validaciones.validarPiso("30"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarPiso("0"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarPiso("31"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarPiso("-5"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarPiso("piso 1"));
    }

    @Test
    @DisplayName("Validar Metraje: decimal entre 10.00 y 1000.00 m2")
    public void testValidarMetraje() {
        assertEquals(10.0, Validaciones.validarMetraje("10.00"));
        assertEquals(85.5, Validaciones.validarMetraje("85.50"));
        assertEquals(1000.0, Validaciones.validarMetraje("1000.00"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMetraje("9.99"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMetraje("1000.01"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMetraje("0"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMetraje("abc"));
    }

    @Test
    @DisplayName("Validar Monto Gasto Comun: mayor a 0 y hasta 999999.99")
    public void testValidarMontoGasto() {
        assertEquals(600.0, Validaciones.validarMontoGasto("600.00"));
        assertEquals(999999.99, Validaciones.validarMontoGasto("999999.99"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMontoGasto("0"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMontoGasto("-10"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMontoGasto("1000000.00"));
    }

    // 3. PRUEBAS DE VALIDACIONES DE RESIDENTES

    @Test
    @DisplayName("Validar DNI: exactamente 8 digitos numericos")
    public void testValidarDni() {
        assertDoesNotThrow(() -> Validaciones.validarDni("72819203"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDni("7281920")); // 7 dígitos
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDni("728192034")); // 9 dígitos
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDni("7281920A"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDni(""));
    }

    @Test
    @DisplayName("Validar Nombres y Apellidos: alfabetico entre 2 y 50 caracteres")
    public void testValidarNombresApellidos() {
        assertDoesNotThrow(() -> Validaciones.validarNombresApellidos("Juan Carlos", "Nombres"));
        assertDoesNotThrow(() -> Validaciones.validarNombresApellidos("Núñez Del Prado", "Apellidos"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNombresApellidos("J", "Nombres"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNombresApellidos("A".repeat(51), "Nombres"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarNombresApellidos("Carlos 2do", "Nombres"));
    }

    @Test
    @DisplayName("Validar Telefono: 9 digitos iniciando en 9, 6 o 7")
    public void testValidarTelefono() {
        assertDoesNotThrow(() -> Validaciones.validarTelefono("987654321"));
        assertDoesNotThrow(() -> Validaciones.validarTelefono("612345678"));
        assertDoesNotThrow(() -> Validaciones.validarTelefono("712345678"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarTelefono("812345678")); // No inicia en 9, 6 o 7
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarTelefono("98765432")); // 8 dígitos
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarTelefono("9876543210")); // 10 dígitos
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarTelefono("98765432A"));
    }

    @Test
    @DisplayName("Validar Correo: regex estandar y normalizacion a minusculas")
    public void testValidarCorreo() {
        assertEquals("vecino.sauces@gmail.com", Validaciones.validarYNormalizarCorreo("Vecino.Sauces@GMAIL.COM"));
        assertEquals("admin@condominio.pe", Validaciones.validarYNormalizarCorreo("admin@condominio.pe"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarYNormalizarCorreo("correo-invalido"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarYNormalizarCorreo("correo@"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarYNormalizarCorreo("@dominio.com"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarYNormalizarCorreo("correo@dominio"));
    }

    // 4. PRUEBAS DE VALIDACIONES DE COBRANZAS

    @Test
    @DisplayName("Validar Monto Pago: debe ser mayor a 0 y no exceder el saldo pendiente")
    public void testValidarMontoPago() {
        assertDoesNotThrow(() -> Validaciones.validarMontoPago(100.0, 250.0));
        assertDoesNotThrow(() -> Validaciones.validarMontoPago(250.0, 250.0));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMontoPago(0.0, 250.0));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarMontoPago(-50.0, 250.0));

        // Regla: si el monto excede el saldo, lanzar excepcion con mensaje exacto
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Validaciones.validarMontoPago(250.01, 250.0));
        assertEquals("El monto excede el saldo pendiente", ex.getMessage());
    }

    // 5. PRUEBAS DE OPERACIONES Y RESERVAS

    @Test
    @DisplayName("Validar Descripcion de Incidente: 5 a 200 caracteres")
    public void testValidarDescripcionIncidente() {
        assertDoesNotThrow(() -> Validaciones.validarDescripcionIncidente("Fuga de agua en cisterna principal"));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDescripcionIncidente("Fuga")); // Menor a 5
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDescripcionIncidente("A".repeat(201))); // Mayor a 200
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarDescripcionIncidente(""));
    }

    @Test
    @DisplayName("Validar Fecha de Reserva: no pasada y maximo 60 dias a futuro")
    public void testValidarFechaReserva() {
        LocalDate hoy = LocalDate.now();
        assertDoesNotThrow(() -> Validaciones.validarFechaReserva(hoy));
        assertDoesNotThrow(() -> Validaciones.validarFechaReserva(hoy.plusDays(30)));
        assertDoesNotThrow(() -> Validaciones.validarFechaReserva(hoy.plusDays(60)));

        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarFechaReserva(hoy.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.validarFechaReserva(hoy.plusDays(61)));
    }

    @Test
    @DisplayName("Validar Horario de Reserva: inicio estrictamente menor a fin")
    public void testValidarHorarioReserva() {
        assertDoesNotThrow(() -> Validaciones.validarHorarioReserva(LocalTime.of(10, 0), LocalTime.of(14, 0)));

        assertThrows(IllegalArgumentException.class,
                () -> Validaciones.validarHorarioReserva(LocalTime.of(14, 0), LocalTime.of(10, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> Validaciones.validarHorarioReserva(LocalTime.of(10, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Solapamiento de Horarios: deteccion precisa de cruces")
    public void testSolapamientoHorario() {
        LocalTime r1Ini = LocalTime.of(10, 0);
        LocalTime r1Fin = LocalTime.of(14, 0);

        // Cruce parcial inicial: 09:00 a 11:00 (solapa)
        assertTrue(Validaciones.haySolapamientoHorario(LocalTime.of(9, 0), LocalTime.of(11, 0), r1Ini, r1Fin));

        // Cruce total contenido: 11:00 a 13:00 (solapa)
        assertTrue(Validaciones.haySolapamientoHorario(LocalTime.of(11, 0), LocalTime.of(13, 0), r1Ini, r1Fin));

        // Cruce envolvente: 08:00 a 16:00 (solapa)
        assertTrue(Validaciones.haySolapamientoHorario(LocalTime.of(8, 0), LocalTime.of(16, 0), r1Ini, r1Fin));

        // Adyacente anterior: 08:00 a 10:00 (no solapa, termina justo cuando empieza)
        assertFalse(Validaciones.haySolapamientoHorario(LocalTime.of(8, 0), LocalTime.of(10, 0), r1Ini, r1Fin));

        // Adyacente posterior: 14:00 a 18:00 (no solapa, empieza justo cuando termina)
        assertFalse(Validaciones.haySolapamientoHorario(LocalTime.of(14, 0), LocalTime.of(18, 0), r1Ini, r1Fin));

        // Disjunto: 16:00 a 20:00 (no solapa)
        assertFalse(Validaciones.haySolapamientoHorario(LocalTime.of(16, 0), LocalTime.of(20, 0), r1Ini, r1Fin));
    }
}
