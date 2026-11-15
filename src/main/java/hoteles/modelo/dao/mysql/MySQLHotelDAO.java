package hoteles.modelo.dao.mysql;

import hoteles.modelo.Habitacion;
import hoteles.modelo.Hotel;
import hoteles.modelo.dao.HotelDAO;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO de hoteles para MySQL. Cada consulta trae los hoteles con sus habitaciones en una sola consulta (LEFT JOIN). */
final class MySQLHotelDAO implements HotelDAO {

    private static final String SELECT = """
            SELECT h.id_hotel, h.nombre, h.ciudad, h.direccion,
                   hab.id_habitacion, hab.numero, hab.categoria, hab.capacidad, hab.precio_noche
              FROM hotel h
              LEFT JOIN habitacion hab ON hab.id_hotel = h.id_hotel
            """;

    @Override
    public void insertar(Hotel hotel) {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_hotel_insertar(?, ?, ?, ?)}")) {
            cs.setString(1, hotel.getNombre());
            cs.setString(2, hotel.getCiudad());
            cs.setString(3, hotel.getDireccion());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            hotel.setIdHotel(cs.getInt(4));
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("insertar el hotel", e);
        }
    }

    @Override
    public Optional<Hotel> buscar(int idHotel) {
        List<Hotel> encontrados = consultar("WHERE h.id_hotel = ? ", idHotel);
        return encontrados.isEmpty() ? Optional.empty() : Optional.of(encontrados.get(0));
    }

    @Override
    public List<Hotel> listar() {
        return consultar("");
    }

    @Override
    public List<Hotel> buscarPorCiudad(String ciudad) {
        return consultar("WHERE h.ciudad = ? ", ciudad);
    }

    private List<Hotel> consultar(String condicion, Object... parametros) {
        String sql = SELECT + condicion + "ORDER BY h.id_hotel, hab.id_habitacion";
        try (PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql)) {
            Sql.asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<Hotel> hoteles = new ArrayList<>();
                Hotel actual = null;
                while (rs.next()) {
                    int id = rs.getInt("id_hotel");
                    if (actual == null || actual.getIdHotel() != id) {
                        actual = new Hotel(id, rs.getString("nombre"), rs.getString("ciudad"), rs.getString("direccion"));
                        hoteles.add(actual);
                    }
                    Habitacion habitacion = Sql.habitacion(rs);
                    if (habitacion != null) {
                        actual.anadirHabitacion(habitacion);
                    }
                }
                return hoteles;
            }
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("consultar los hoteles", e);
        }
    }
}
