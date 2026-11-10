package hoteles.modelo;

import hoteles.modelo.dao.Configuracion;
import hoteles.modelo.dao.mysql.ConexionBD;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepara una base de datos de pruebas llamada {@code hoteles_test} (nunca toca la base de datos real
 * {@code hoteles}): la crea desde cero con el mismo script sql/hoteles.sql que usa la aplicación y deja
 * el programa apuntando a ella. Usa el usuario y la contraseña de la configuración (bd.properties,
 * bd.local.properties o -Dbd.usuario / -Dbd.password).
 * <p>
 * Si no hay un MySQL accesible, {@link #preparar()} devuelve false y las pruebas que lo necesitan
 * se omiten (Assumptions) en lugar de fallar.
 */
final class EntornoBD {

    static final String NOMBRE_BD = "hoteles_test";
    private static final String[] TABLAS = {"detalle_reserva", "reserva", "habitacion", "hotel", "cliente"};
    private static Boolean disponible;

    private EntornoBD() {
    }

    static synchronized boolean preparar() {
        if (disponible != null) {
            return disponible;
        }
        String url = Configuracion.get("bd.url", "jdbc:mysql://localhost:3306/hoteles");
        String usuario = Configuracion.get("bd.usuario", "root");
        String password = Configuracion.get("bd.password", "");
        String urlAdmin = url.replaceFirst("/hoteles(?=[?]|$)", "/");
        String urlTest = url.replaceFirst("/hoteles(?=[?]|$)", "/" + NOMBRE_BD);
        try (Connection c = DriverManager.getConnection(urlAdmin, usuario, password);
             Statement st = c.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS " + NOMBRE_BD);
            st.execute("CREATE DATABASE " + NOMBRE_BD + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            st.execute("USE " + NOMBRE_BD);
            String script = Files.readString(Path.of("sql", "hoteles.sql"), StandardCharsets.UTF_8);
            for (String sentencia : sentencias(script)) {
                String mayusculas = sentencia.toUpperCase();
                if (!mayusculas.startsWith("CREATE DATABASE") && !mayusculas.startsWith("USE ")) {
                    st.execute(sentencia);
                }
            }
        } catch (Exception e) {
            System.out.println("[EntornoBD] MySQL no disponible, se omiten las pruebas de persistencia: " + e.getMessage());
            disponible = false;
            return false;
        }
        System.setProperty("bd.url", urlTest);
        disponible = true;
        return true;
    }

    /** Vacía todas las tablas y reinicia los contadores AUTO_INCREMENT (los ids vuelven a empezar en 1). */
    static void limpiar() throws SQLException {
        try (Statement st = ConexionBD.getConexion().createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String tabla : TABLAS) {
                st.execute("TRUNCATE TABLE " + tabla);
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /** Cuenta las filas de una tabla (para comprobar que no quedan restos tras un ROLLBACK o un DELETE). */
    static int contar(String tabla) throws SQLException {
        try (Statement st = ConexionBD.getConexion().createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Divide un script SQL en sentencias respetando las líneas DELIMITER (como hace MySQL Workbench). */
    static List<String> sentencias(String script) {
        List<String> resultado = new ArrayList<>();
        String delimitador = ";";
        StringBuilder actual = new StringBuilder();
        for (String linea : script.split("\\R")) {
            String t = linea.strip();
            if (t.toUpperCase().startsWith("DELIMITER ")) {
                delimitador = t.substring("DELIMITER ".length()).strip();
                continue;
            }
            if (actual.length() == 0 && (t.isEmpty() || t.startsWith("--"))) {
                continue;
            }
            actual.append(linea).append('\n');
            if (t.endsWith(delimitador)) {
                String sentencia = actual.toString();
                sentencia = sentencia.substring(0, sentencia.lastIndexOf(delimitador)).strip();
                if (!sentencia.isEmpty()) {
                    resultado.add(sentencia);
                }
                actual.setLength(0);
            }
        }
        return resultado;
    }
}
