package com.condominio.patrones;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Prueba de integracion con base de datos externa PostgreSQL 18.
public class PostgreSQLExternalConnectionTest {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/cobranza_vecinal_db";
    private static final String USER = "postgres";
    private static final String PASS = "123";

    // Valida si el servicio local de PostgreSQL responde en el puerto 5432
    private boolean isPostgreSqlAvailable() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS)) {
            return conn != null && conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Debe conectar exitosamente a PostgreSQL 18 en cobranza_vecinal_db")
    public void debeConectarAPostgreSQL() {
        assumeTrue(isPostgreSqlAvailable(),
            "Servicio PostgreSQL 18 local (localhost:5432) no disponible. Se omite la prueba de conexion externa sin bloquear la compilacion.");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS)) {
            assertNotNull(conn, "La conexion a PostgreSQL no debe ser nula");
            assertTrue(conn.isValid(3), "La conexion JDBC a PostgreSQL debe ser valida");
            assertEquals("cobranza_vecinal_db", conn.getCatalog(), "El catalogo conectado debe ser cobranza_vecinal_db");
        } catch (Exception e) {
            fail("Error al conectar a PostgreSQL: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe consultar datos sembrados en los esquemas de PostgreSQL")
    public void debeConsultarDatosSembrados() {
        assumeTrue(isPostgreSqlAvailable(),
            "Servicio PostgreSQL 18 local (localhost:5432) no disponible. Se omite la prueba de esquemas externos sin bloquear la compilacion.");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // Consultar seguridad
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM seguridad.usuario_sistema")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 3, "Deben existir al menos 3 usuarios del sistema");
            }

            // Consultar inmuebles
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM inmuebles.departamento")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 6, "Deben existir departamentos registrados");
            }

            // Consultar financiero
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM financiero.cuota_mantenimiento")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 6, "Deben existir cuotas registradas");
            }

            // Consultar operaciones
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM operaciones.comunicado")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 2, "Deben existir comunicados registrados");
            }

        } catch (Exception e) {
            fail("Error al consultar tablas en PostgreSQL: " + e.getMessage());
        }
    }
}
