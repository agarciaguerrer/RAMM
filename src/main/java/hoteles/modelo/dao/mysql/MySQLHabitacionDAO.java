package hoteles.modelo.dao.mysql;

import hoteles.modelo.ElementoDuplicadoException;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.Habitacion;
import hoteles.modelo.dao.HabitacionDAO;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** DAO de habitaciones para MySQL (consultas con PreparedStatement, inserciones con procedimiento almacenado). */
final class MySQLHabitacionDAO implements HabitacionDAO {

    @Override
    public void insertar(int idHotel, Habitacion habitacion)
            throws ElementoNoEncontradoException, ElementoDuplicadoException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_habitacion_insertar(?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idHotel);
            cs.setString(2, habitacion.getNumero());
            cs.setString(3, habitacion.getCategoria());
            cs.setInt(4, habitacion.getCapacidad());
            cs.setBigDecimal(5, java.math.BigDecimal.valueOf(habitacion.getPrecioNoche()));
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
            habitacion.setIdHabitacion(cs.getInt(6));
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException("No existe un hotel con id " + idHotel);
            }
            if (ErroresSQL.esDuplicado(e)) {
                throw new ElementoDuplicadoException("El hotel ya tiene la habitación " + habitacion.getNumero());
            }
            throw ErroresSQL.tecnico("insertar la habitación", e);
        }
    }

    @Override
    public boolean existeNumero(int idHotel, String numero) {
        String sql = "SELECT 1 FROM habitacion WHERE id_hotel = ? AND numero = ? LIMIT 1";
        try (PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idHotel);
            ps.setString(2, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("comprobar el número de habitación", e);
        }
    }
}
