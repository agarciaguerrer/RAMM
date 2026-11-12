package hoteles.modelo.dao.mysql;

import hoteles.modelo.dao.Configuracion;
import hoteles.modelo.dao.PersistenciaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad que controla la conexión con MySQL: la abre cuando hace falta, la reutiliza, la
 * vuelve a abrir si se ha perdido, la cierra al terminar el programa y gestiona las transacciones.
 * <p>
 * Los datos de conexión salen de {@link Configuracion} (bd.url, bd.usuario, bd.password).
 * Es una aplicación de consola de un solo usuario, así que se usa una única conexión compartida por
 * todos los DAO; eso permite que varias operaciones DAO formen parte de la misma transacción.
 * (En una aplicación con varios usuarios simultáneos habría que usar un pool de conexiones.)
 */
public final class ConexionBD {

    private static Connection conexion;
    private static int nivelTransaccion = 0;
    private static boolean soloRevertir = false;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ConexionBD::cerrar, "cierre-conexion-bd"));
    }

    private ConexionBD() {
    }

    /** Devuelve la conexión abierta, abriéndola (o reabriéndola) si hace falta. */
    public static synchronized Connection getConexion() {
        try {
            if (conexion != null && nivelTransaccion == 0 && !conexion.isValid(2)) {
                cerrar();
            }
            if (conexion == null || conexion.isClosed()) {
                conexion = abrir();
            }
            return conexion;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo comprobar la conexión con la base de datos: " + e.getMessage(), e);
        }
    }

    public static synchronized void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                // Al cerrar no hay nada más que hacer con el error.
            }
            conexion = null;
            nivelTransaccion = 0;
            soloRevertir = false;
        }
    }

    private static Connection abrir() {
        String url = Configuracion.get("bd.url", "jdbc:mysql://localhost:3306/RAMM_hoteles");
        String usuario = Configuracion.get("bd.usuario", "root");
        String password = Configuracion.get("bd.password", "");
        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar con la base de datos (" + url + "): " + e.getMessage()
                    + ". Comprueba que MySQL está en marcha, que existe la base de datos (sql/hoteles.sql) "
                    + "y que el usuario y la contraseña de bd.properties son correctos.", e);
        }
    }

    // ------------------------------------------------------------- Transacciones
    // Si se pide una transacción estando ya dentro de otra, se une a ella: solo la más externa
    // hace el COMMIT, y si cualquiera falla, al final se deshace todo.

    static synchronized void iniciarTransaccion() {
        Connection c = getConexion();
        try {
            if (nivelTransaccion == 0) {
                c.setAutoCommit(false);
                soloRevertir = false;
            }
            nivelTransaccion++;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo iniciar la transacción: " + e.getMessage(), e);
        }
    }

    static synchronized void confirmar() {
        if (nivelTransaccion == 0) {
            throw new IllegalStateException("No hay ninguna transacción abierta.");
        }
        nivelTransaccion--;
        if (nivelTransaccion > 0) {
            return;
        }
        try {
            if (soloRevertir) {
                conexion.rollback();
                throw new PersistenciaException("La operación se deshizo porque falló una de sus partes.");
            }
            conexion.commit();
        } catch (SQLException e) {
            deshacerSilencioso();
            throw new PersistenciaException("No se pudieron guardar los cambios: " + e.getMessage(), e);
        } finally {
            soloRevertir = false;
            restaurarAutoCommit();
        }
    }

    static synchronized void revertir() {
        if (nivelTransaccion == 0) {
            return;
        }
        soloRevertir = true;
        nivelTransaccion--;
        if (nivelTransaccion == 0) {
            deshacerSilencioso();
            soloRevertir = false;
            restaurarAutoCommit();
        }
    }

    private static void deshacerSilencioso() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.rollback();
            }
        } catch (SQLException e) {
            // Si ni siquiera se puede hacer el rollback, el servidor lo hará al cerrarse la conexión.
        }
    }

    private static void restaurarAutoCommit() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            cerrar(); // conexión inservible: la siguiente operación abrirá una nueva
        }
    }
}
