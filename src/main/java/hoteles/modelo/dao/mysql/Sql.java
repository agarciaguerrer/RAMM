package hoteles.modelo.dao.mysql;

import hoteles.modelo.Habitacion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Utilidades comunes de los DAO de MySQL. */
final class Sql {

    private Sql() {
    }

    /**
     * Crea la habitación de la fila actual (columnas id_habitacion, numero, categoria, capacidad y
     * precio_noche) o devuelve null si la fila no tiene habitación (resultado de un LEFT JOIN vacío).
     */
    static Habitacion habitacion(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_habitacion");
        if (rs.wasNull()) {
            return null;
        }
        return new Habitacion(id, rs.getString("numero"), rs.getString("categoria"),
                rs.getInt("capacidad"), rs.getDouble("precio_noche"));
    }

    /** Asigna los parámetros (?) de una sentencia, en orden. Admite String, Integer y LocalDate. */
    static void asignar(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            if (valor instanceof LocalDate) {
                ps.setObject(i + 1, valor);
            } else if (valor instanceof Integer) {
                ps.setInt(i + 1, (Integer) valor);
            } else {
                ps.setString(i + 1, (String) valor);
            }
        }
    }
}
