package hoteles.modelo.dao.mysql;

import hoteles.modelo.Cliente;
import hoteles.modelo.DetalleReserva;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.Habitacion;
import hoteles.modelo.HabitacionNoDisponibleException;
import hoteles.modelo.Hotel;
import hoteles.modelo.OperacionInvalidaException;
import hoteles.modelo.Reserva;
import hoteles.modelo.dao.ReservaDAO;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de reservas para MySQL. Una reserva se lee con su cliente, su hotel y todos sus detalles en una
 * sola consulta. El hotel que lleva dentro la reserva no trae su lista de habitaciones (no hace falta
 * para trabajar con reservas); las habitaciones reservadas están en los detalles.
 */
final class MySQLReservaDAO implements ReservaDAO {

    private static final String SELECT = """
            SELECT r.id_reserva, r.fecha_entrada, r.fecha_salida, r.numero_personas, r.estado,
                   c.id_cliente, c.nombre AS c_nombre, c.apellidos AS c_apellidos,
                   c.email AS c_email, c.telefono AS c_telefono,
                   h.id_hotel, h.nombre AS h_nombre, h.ciudad AS h_ciudad, h.direccion AS h_direccion,
                   d.id_detalle, d.precio_noche_aplicado,
                   hab.id_habitacion, hab.numero, hab.categoria, hab.capacidad, hab.precio_noche
              FROM reserva r
              JOIN cliente c ON c.id_cliente = r.id_cliente
              JOIN hotel h ON h.id_hotel = r.id_hotel
              LEFT JOIN detalle_reserva d ON d.id_reserva = r.id_reserva
              LEFT JOIN habitacion hab ON hab.id_habitacion = d.id_habitacion
            """;

    @Override
    public void insertar(Reserva reserva) throws ElementoNoEncontradoException, HabitacionNoDisponibleException {
        try {
            try (CallableStatement cs = ConexionBD.getConexion()
                    .prepareCall("{CALL sp_reserva_insertar(?, ?, ?, ?, ?, ?, ?)}")) {
                cs.setInt(1, reserva.getCliente().getIdCliente());
                cs.setInt(2, reserva.getHotel().getIdHotel());
                cs.setObject(3, reserva.getFechaEntrada());
                cs.setObject(4, reserva.getFechaSalida());
                cs.setInt(5, reserva.getNumeroPersonas());
                cs.setString(6, reserva.getEstado().name());
                cs.registerOutParameter(7, Types.INTEGER);
                cs.execute();
                reserva.setIdReserva(cs.getInt(7));
            }
            for (DetalleReserva detalle : reserva.getDetalles()) {
                try (CallableStatement cs = ConexionBD.getConexion()
                        .prepareCall("{CALL sp_detalle_insertar(?, ?, ?, ?)}")) {
                    cs.setInt(1, reserva.getIdReserva());
                    cs.setInt(2, detalle.getHabitacion().getIdHabitacion());
                    cs.setBigDecimal(3, BigDecimal.valueOf(detalle.getPrecioNocheAplicado()));
                    cs.registerOutParameter(4, Types.INTEGER);
                    cs.execute();
                    detalle.setIdDetalle(cs.getInt(4));
                }
            }
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException(ErroresSQL.mensaje(e));
            }
            if (ErroresSQL.esNoDisponible(e)) {
                throw new HabitacionNoDisponibleException(ErroresSQL.mensaje(e));
            }
            throw ErroresSQL.tecnico("insertar la reserva", e);
        }
    }

    @Override
    public void actualizar(Reserva reserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_reserva_modificar(?, ?, ?, ?)}")) {
            cs.setInt(1, reserva.getIdReserva());
            cs.setObject(2, reserva.getFechaEntrada());
            cs.setObject(3, reserva.getFechaSalida());
            cs.setInt(4, reserva.getNumeroPersonas());
            cs.execute();
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException("No existe una reserva con id " + reserva.getIdReserva());
            }
            if (ErroresSQL.esOperacionInvalida(e)) {
                throw new OperacionInvalidaException(ErroresSQL.mensaje(e));
            }
            if (ErroresSQL.esNoDisponible(e)) {
                throw new HabitacionNoDisponibleException(ErroresSQL.mensaje(e));
            }
            throw ErroresSQL.tecnico("modificar la reserva", e);
        }
    }

    @Override
    public void cambiarEstado(int idReserva, EstadoReserva estado) throws ElementoNoEncontradoException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_reserva_cambiar_estado(?, ?)}")) {
            cs.setInt(1, idReserva);
            cs.setString(2, estado.name());
            cs.execute();
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException("No existe una reserva con id " + idReserva);
            }
            throw ErroresSQL.tecnico("cambiar el estado de la reserva", e);
        }
    }

    @Override
    public Optional<Reserva> buscar(int idReserva) {
        List<Reserva> encontradas = consultar("WHERE r.id_reserva = ? ", idReserva);
        return encontradas.isEmpty() ? Optional.empty() : Optional.of(encontradas.get(0));
    }

    @Override
    public List<Reserva> listar() {
        return consultar("");
    }

    @Override
    public List<Reserva> filtrar(EstadoReserva estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                                 Integer idCliente, Integer idHotel) {
        // Solo se añaden a la sentencia fragmentos fijos; los valores del usuario van siempre como parámetros (?).
        StringBuilder condicion = new StringBuilder("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (estado != null) {
            condicion.append("AND r.estado = ? ");
            parametros.add(estado.name());
        }
        if (fechaEntrada != null) {
            condicion.append("AND r.fecha_entrada = ? ");
            parametros.add(fechaEntrada);
        }
        if (fechaSalida != null) {
            condicion.append("AND r.fecha_salida = ? ");
            parametros.add(fechaSalida);
        }
        if (idCliente != null) {
            condicion.append("AND r.id_cliente = ? ");
            parametros.add(idCliente);
        }
        if (idHotel != null) {
            condicion.append("AND r.id_hotel = ? ");
            parametros.add(idHotel);
        }
        return consultar(condicion.toString(), parametros.toArray());
    }

    @Override
    public List<Reserva> listarSolapadas(int idHotel, LocalDate entrada, LocalDate salida, Integer idReservaExcluida) {
        StringBuilder condicion = new StringBuilder(
                "WHERE r.id_hotel = ? AND r.estado <> 'CANCELADA' AND r.fecha_entrada < ? AND ? < r.fecha_salida ");
        List<Object> parametros = new ArrayList<>(List.of(idHotel, salida, entrada));
        if (idReservaExcluida != null) {
            condicion.append("AND r.id_reserva <> ? ");
            parametros.add(idReservaExcluida);
        }
        return consultar(condicion.toString(), parametros.toArray());
    }

    private List<Reserva> consultar(String condicion, Object... parametros) {
        String sql = SELECT + condicion + "ORDER BY r.id_reserva, d.id_detalle";
        try (PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql)) {
            Sql.asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reserva> reservas = new ArrayList<>();
                Reserva actual = null;
                while (rs.next()) {
                    int id = rs.getInt("id_reserva");
                    if (actual == null || actual.getIdReserva() != id) {
                        actual = nuevaReserva(rs, id);
                        reservas.add(actual);
                    }
                    Habitacion habitacion = Sql.habitacion(rs);
                    if (habitacion != null) {
                        actual.anadirDetalle(new DetalleReserva(rs.getInt("id_detalle"), habitacion,
                                rs.getDouble("precio_noche_aplicado")));
                    }
                }
                return reservas;
            }
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("consultar las reservas", e);
        }
    }

    private Reserva nuevaReserva(ResultSet rs, int id) throws SQLException {
        Cliente cliente = new Cliente(rs.getInt("id_cliente"), rs.getString("c_nombre"),
                rs.getString("c_apellidos"), rs.getString("c_email"), rs.getString("c_telefono"));
        Hotel hotel = new Hotel(rs.getInt("id_hotel"), rs.getString("h_nombre"),
                rs.getString("h_ciudad"), rs.getString("h_direccion"));
        return new Reserva(id, cliente, hotel, rs.getObject("fecha_entrada", LocalDate.class),
                rs.getObject("fecha_salida", LocalDate.class), rs.getInt("numero_personas"),
                EstadoReserva.valueOf(rs.getString("estado")));
    }
}
