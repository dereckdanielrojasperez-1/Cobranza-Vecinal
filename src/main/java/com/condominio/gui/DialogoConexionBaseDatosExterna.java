package com.condominio.gui;

import com.condominio.gui.animacion.AnimadorToast;
import com.condominio.gui.util.ConstantesUI;
import com.condominio.gui.util.EstilosUI;
import com.condominio.gui.util.GlassPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.sql.Connection;
import java.sql.DriverManager;

// Diálogo interactivo para configuración, diagnóstico y conexión con Bases de Datos Externas.
public class DialogoConexionBaseDatosExterna extends JDialog {

    private final Frame propietario;
    private JTextField txtHost;
    private JTextField txtPuerto;
    private JTextField txtBaseDatos;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> comboMotor;
    private JLabel lblResultadoPrueba;
    private JButton btnProbarConexion;

    public DialogoConexionBaseDatosExterna(Frame parent) {
        super(parent, "Configuración y Conexión de Base de Datos Externa", true);
        this.propietario = parent;
        inicializarUI();
    }

    private void inicializarUI() {
        setSize(780, 620);
        setResizable(false);
        setLocationRelativeTo(propietario);
        getContentPane().setBackground(EstilosUI.FONDO_PRINCIPAL);

        JPanel panelContenedor = new JPanel(new BorderLayout(0, 14));
        panelContenedor.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panelContenedor.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Banner Superior Corporativo
        JPanel banner = EstilosUI.crearPanelBanner(EstilosUI.AZUL_INFO);
        JLabel lblTit = new JLabel("ARQUITECTURA DE PERSISTENCIA Y BASE DE DATOS EXTERNA");
        lblTit.setFont(EstilosUI.FUENTE_BANNER);
        lblTit.setForeground(EstilosUI.AZUL_INFO);

        JLabel lblSub = new JLabel("Conexión desacoplada para PostgreSQL / MySQL con soporte para esquemas modulares y pool HikariCP.");
        lblSub.setFont(EstilosUI.FUENTE_SUBTITULO);
        lblSub.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        banner.add(lblTit, BorderLayout.NORTH);
        banner.add(lblSub, BorderLayout.SOUTH);
        panelContenedor.add(banner, BorderLayout.NORTH);

        // 2. Panel con Pestañas
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pestañas.setBackground(EstilosUI.FONDO_TARJETA);
        pestañas.setForeground(EstilosUI.TEXTO_PRINCIPAL);

        pestañas.addTab("Estado Actual", crearPanelEstadoActual());
        pestañas.addTab("Configurar Motor Externo", crearPanelConfiguracionMotor());
        pestañas.addTab("Scripts DDL y Migración", crearPanelScripts());

        panelContenedor.add(pestañas, BorderLayout.CENTER);

        // 3. Barra Inferior de Botones
        JPanel pnlSur = new JPanel(new BorderLayout());
        pnlSur.setOpaque(false);

        JButton btnCopiarComando = new JButton("Copiar Comando de Inicio");
        EstilosUI.estilizarBoton(btnCopiarComando, EstilosUI.AZUL_INFO);
        btnCopiarComando.setPreferredSize(new Dimension(210, 34));
        btnCopiarComando.addActionListener(e -> copiarComandoArranque());

        JButton btnCerrar = new JButton("Cerrar");
        EstilosUI.estilizarBoton(btnCerrar, EstilosUI.DORADO_PRINCIPAL);
        btnCerrar.setPreferredSize(new Dimension(100, 34));
        btnCerrar.addActionListener(e -> dispose());

        pnlSur.add(btnCopiarComando, BorderLayout.WEST);
        pnlSur.add(btnCerrar, BorderLayout.EAST);
        panelContenedor.add(pnlSur, BorderLayout.SOUTH);

        setContentPane(panelContenedor);
    }

    private JPanel crearPanelEstadoActual() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        GlassPanel tarjeta = new GlassPanel(16, false, EstilosUI.AZUL_INFO);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(16, 18, 16, 18));

        String perfilActivo = System.getProperty("spring.profiles.active", "dev (Por defecto)");
        String motorActual = "H2 Database Local en Archivo Persistente (./data/cobranzadb)";
        if ("external".equalsIgnoreCase(perfilActivo) || "prod".equalsIgnoreCase(perfilActivo)) {
            motorActual = "PostgreSQL / Servidor Relacional Externo";
        }

        tarjeta.add(crearFilaDato("Perfil de Ejecución Activo:", perfilActivo, EstilosUI.DORADO_PRINCIPAL));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Motor de Base de Datos:", motorActual, EstilosUI.TEXTO_PRINCIPAL));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Estado del Pool de Conexiones:", "HikariCP Activo (10 conexiones max, timeout 20s)", EstilosUI.VERDE_EXITO));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Esquemas Relacionales Inicializados:", "seguridad, personas, inmuebles, operaciones, financiero", EstilosUI.AZUL_INFO));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Estrategia DDL Hibernate:", "update (Sincronización automática de entidades y tablas)", EstilosUI.TEXTO_SECUNDARIO));
        tarjeta.add(Box.createVerticalStrut(16));

        JTextArea infoText = new JTextArea(
                "NOTA TÉCNICA:\n" +
                "El sistema opera por defecto en modo local de desarrollo utilizando H2 Database persistente en disco.\n" +
                "Para migrar a una base de datos externa (PostgreSQL en pgAdmin 4 o MySQL en servidor corporativo),\n" +
                "simplemente active el perfil 'external' sin modificar ninguna línea de código Java.\n\n" +
                "Variables de entorno admitidas:\n" +
                "  - DB_URL: URL JDBC del servidor (ej: jdbc:postgresql://localhost:5432/cobranza_vecinal_db)\n" +
                "  - DB_USERNAME: Usuario de la base de datos (ej: postgres)\n" +
                "  - DB_PASSWORD: Clave de acceso\n" +
                "  - DB_DRIVER: Clase del driver JDBC (ej: org.postgresql.Driver)"
        );
        infoText.setFont(new Font("Consolas", Font.PLAIN, 12));
        infoText.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        infoText.setBackground(new Color(0x0C, 0x14, 0x1F));
        infoText.setEditable(false);
        infoText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA),
                new EmptyBorder(10, 12, 10, 12)
        ));

        panel.add(tarjeta, BorderLayout.NORTH);
        panel.add(infoText, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelConfiguracionMotor() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        GlassPanel form = new GlassPanel(16, false, EstilosUI.DORADO_PRINCIPAL);
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 18, 16, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Motor
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblMotor = new JLabel("Motor de Base de Datos:");
        lblMotor.setFont(EstilosUI.FUENTE_TEXTO);
        lblMotor.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblMotor, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        comboMotor = new JComboBox<>(new String[]{"PostgreSQL (Recomendado)", "MySQL / MariaDB"});
        comboMotor.setFont(EstilosUI.FUENTE_TEXTO);
        comboMotor.setBackground(EstilosUI.FONDO_CAMPO);
        comboMotor.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        comboMotor.addActionListener(e -> actualizarCamposPorMotor());
        form.add(comboMotor, gbc);

        // Host
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblHost = new JLabel("Servidor Host / IP:");
        lblHost.setFont(EstilosUI.FUENTE_TEXTO);
        lblHost.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblHost, gbc);

        gbc.gridx = 1;
        txtHost = new JTextField("localhost");
        EstilosUI.estilizarTextField(txtHost);
        form.add(txtHost, gbc);

        // Puerto
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(EstilosUI.FUENTE_TEXTO);
        lblPuerto.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblPuerto, gbc);

        gbc.gridx = 1;
        txtPuerto = new JTextField("5432");
        EstilosUI.estilizarTextField(txtPuerto);
        form.add(txtPuerto, gbc);

        // Base de Datos
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblBD = new JLabel("Nombre Base de Datos:");
        lblBD.setFont(EstilosUI.FUENTE_TEXTO);
        lblBD.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblBD, gbc);

        gbc.gridx = 1;
        txtBaseDatos = new JTextField("cobranza_vecinal_db");
        EstilosUI.estilizarTextField(txtBaseDatos);
        form.add(txtBaseDatos, gbc);

        // Usuario
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(EstilosUI.FUENTE_TEXTO);
        lblUser.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField("postgres");
        EstilosUI.estilizarTextField(txtUsuario);
        form.add(txtUsuario, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(EstilosUI.FUENTE_TEXTO);
        lblPass.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        form.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField("123");
        txtPassword.setFont(EstilosUI.FUENTE_TEXTO);
        txtPassword.setBackground(EstilosUI.FONDO_CAMPO);
        txtPassword.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        txtPassword.setCaretColor(EstilosUI.DORADO_PRINCIPAL);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA),
                new EmptyBorder(5, 8, 5, 8)
        ));
        form.add(txtPassword, gbc);

        // Botón Probar Conexión
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel pnlAccionPrueba = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        pnlAccionPrueba.setOpaque(false);

        btnProbarConexion = new JButton("Probar Conexión JDBC");
        EstilosUI.estilizarBoton(btnProbarConexion, EstilosUI.AZUL_INFO);
        btnProbarConexion.setPreferredSize(new Dimension(190, 34));
        btnProbarConexion.addActionListener(e -> probarConexionJDBC());
        pnlAccionPrueba.add(btnProbarConexion);

        lblResultadoPrueba = new JLabel("Haga clic en 'Probar Conexión JDBC' para verificar conectividad con el servidor.");
        lblResultadoPrueba.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblResultadoPrueba.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        pnlAccionPrueba.add(lblResultadoPrueba);

        form.add(pnlAccionPrueba, gbc);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarCamposPorMotor() {
        if (comboMotor.getSelectedIndex() == 0) {
            // PostgreSQL
            txtPuerto.setText("5432");
            txtUsuario.setText("postgres");
        } else {
            // MySQL
            txtPuerto.setText("3306");
            txtUsuario.setText("root");
        }
    }

    private void probarConexionJDBC() {
        String motor = (String) comboMotor.getSelectedItem();
        boolean esPostgres = (motor != null && motor.contains("PostgreSQL"));
        String host = txtHost.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String db = txtBaseDatos.getText().trim();
        String user = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());

        String driverClass = esPostgres ? "org.postgresql.Driver" : "com.mysql.cj.jdbc.Driver";
        String jdbcUrl = esPostgres
                ? "jdbc:postgresql://" + host + ":" + puerto + "/" + db
                : "jdbc:mysql://" + host + ":" + puerto + "/" + db + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        btnProbarConexion.setEnabled(false);
        lblResultadoPrueba.setText("Conectando con el servidor " + host + ":" + puerto + "...");
        lblResultadoPrueba.setForeground(EstilosUI.DORADO_PRINCIPAL);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String mensajeDetalle = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    Class.forName(driverClass);
                    DriverManager.setLoginTimeout(5);
                    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
                        boolean valida = conn.isValid(5);
                        mensajeDetalle = "Conexión exitosa a " + db + " en " + host + " (Catálogo: " + conn.getCatalog() + ")";
                        return valida;
                    }
                } catch (Exception ex) {
                    mensajeDetalle = "Fallo de conexión: " + ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnProbarConexion.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        lblResultadoPrueba.setText(mensajeDetalle);
                        lblResultadoPrueba.setForeground(EstilosUI.VERDE_EXITO);
                        AnimadorToast.mostrar("CONEXIÓN EXITOSA", "Servidor de base de datos validado correctamente.", EstilosUI.VERDE_EXITO);
                    } else {
                        lblResultadoPrueba.setText(mensajeDetalle);
                        lblResultadoPrueba.setForeground(EstilosUI.ROJO_ERROR);
                    }
                } catch (Exception e) {
                    lblResultadoPrueba.setText("Error en prueba: " + e.getMessage());
                    lblResultadoPrueba.setForeground(EstilosUI.ROJO_ERROR);
                }
            }
        };
        worker.execute();
    }

    private JPanel crearPanelScripts() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(EstilosUI.FONDO_PRINCIPAL);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        GlassPanel tarjeta = new GlassPanel(16, false, EstilosUI.AZUL_INFO);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(16, 18, 16, 18));

        tarjeta.add(crearFilaDato("Script DDL PostgreSQL (Multi-Esquema):", "src/main/resources/schema-external-postgresql.sql", EstilosUI.DORADO_PRINCIPAL));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Script DDL MySQL / MariaDB:", "src/main/resources/schema-external-mysql.sql", EstilosUI.TEXTO_PRINCIPAL));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Datos Semilla Iniciales (Data Seed):", "src/main/resources/data-external.sql", EstilosUI.VERDE_EXITO));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearFilaDato("Perfil Externo Spring Boot:", "src/main/resources/application-external.properties", EstilosUI.AZUL_INFO));
        tarjeta.add(Box.createVerticalStrut(16));

        JTextArea txtGuia = new JTextArea(
                "PASOS PARA DESPLEGAR EN POSTGRESQL / PGADMIN 4:\n" +
                "1. Abra pgAdmin 4 y cree una base de datos llamada 'cobranza_vecinal_db'.\n" +
                "2. Abra la herramienta 'Query Tool' y ejecute el script 'schema-external-postgresql.sql'.\n" +
                "3. Opcionalmente ejecute 'data-external.sql' para contar con datos reales iniciales.\n" +
                "4. Inicie el aplicativo con el perfil externo:\n" +
                "     java -jar cobranza-vecinal-1.0.0.jar --spring.profiles.active=external\n" +
                "   o en NetBeans mediante Project Properties -> Run -> VM Options:\n" +
                "     -Dspring.profiles.active=external"
        );
        txtGuia.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtGuia.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        txtGuia.setBackground(new Color(0x0C, 0x14, 0x1F));
        txtGuia.setEditable(false);
        txtGuia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE_LINEA),
                new EmptyBorder(10, 12, 10, 12)
        ));

        panel.add(tarjeta, BorderLayout.NORTH);
        panel.add(txtGuia, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFilaDato(String etiqueta, String valor, Color colorValor) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);

        JLabel lblE = new JLabel(etiqueta);
        lblE.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblE.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        JLabel lblV = new JLabel(valor);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblV.setForeground(colorValor);

        fila.add(lblE, BorderLayout.WEST);
        fila.add(lblV, BorderLayout.CENTER);
        return fila;
    }

    private void copiarComandoArranque() {
        String comando = "java -jar cobranza-vecinal-1.0.0.jar --spring.profiles.active=external";
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(comando), null);
        AnimadorToast.mostrar("COMANDO COPIADO", "Instrucción de inicio copiada al portapapeles.", EstilosUI.AZUL_INFO);
    }

    public static void mostrar(Frame parent) {
        DialogoConexionBaseDatosExterna dlg = new DialogoConexionBaseDatosExterna(parent);
        dlg.setVisible(true);
    }
}
