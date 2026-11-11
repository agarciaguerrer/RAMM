package hoteles.modelo.dao.mysql;

import hoteles.modelo.dao.PersistenciaException;

import java.sql.SQLException;

/**
 * Traduce los errores de MySQL a la lógica del modelo. Los procedimientos almacenados avisan de las
 * reglas de negocio con SIGNAL y un código propio (ver sql/hoteles.sql); los demás errores son técnicos.
 */
final class ErroresSQL {

    /** Error 1062 de MySQL: entrada duplicada en una clave UNIQUE o PRIMARY. */
    private static final int ENTRADA_DUPLICADA = 1062;
    private static final int NO_ENCONTRADO = 30001;
    private static final int NO_DISPONIBLE = 30002;
    private static final int OPERACION_INVALIDA = 30003;

    private ErroresSQL() {
    }

    static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == ENTRADA_DUPLICADA;
    }

    static boolean esNoEncontrado(SQLException e) {
        return e.getErrorCode() == NO_ENCONTRADO;
    }

    static boolean esNoDisponible(SQLException e) {
        return e.getErrorCode() == NO_DISPONIBLE;
    }

    static boolean esOperacionInvalida(SQLException e) {
        return e.getErrorCode() == OPERACION_INVALIDA;
    }

    /** Texto del error propio del procedimiento, sin los prefijos que añaden algunos drivers. */
    static String mensaje(SQLException e) {
        String texto = e.getMessage() == null ? "" : e.getMessage();
        return texto.replaceAll("\\s*\\(conn=\\d+\\)", "").replaceAll("^\\(conn=\\d+\\)\\s*", "").trim();
    }

    static PersistenciaException tecnico(String operacion, SQLException e) {
        return new PersistenciaException("Error de base de datos al " + operacion + ": " + e.getMessage(), e);
    }
}
