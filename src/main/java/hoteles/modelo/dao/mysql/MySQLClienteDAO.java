package hoteles.modelo.dao.mysql;

import hoteles.modelo.Cliente;
import hoteles.modelo.ElementoDuplicadoException;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.OperacionInvalidaException;
import hoteles.modelo.dao.ClienteDAO;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de clientes para MySQL. Las consultas (SELECT) usan PreparedStatement y las modificaciones
 * (INSERT, UPDATE, DELETE) llaman a procedimientos almacenados con CallableStatement. En ningún caso
 * se pega texto del usuario dentro de la sentencia SQL: siempre va como parámetro (?), por lo que
 * no es posible la inyección de SQL.
 */
final class MySQLClienteDAO implements ClienteDAO {

    private static final String COLUMNAS = "SELECT id_cliente, nombre, apellidos, email, telefono FROM cliente ";

    @Override
    public void insertar(Cliente cliente) throws ElementoDuplicadoException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_cliente_insertar(?, ?, ?, ?, ?)}")) {
            cs.setString(1, cliente.getNombre());
            cs.setString(2, cliente.getApellidos());
            cs.setString(3, cliente.getEmail());
            cs.setString(4, cliente.getTelefono());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            cliente.setIdCliente(cs.getInt(5));
        } catch (SQLException e) {
            if (ErroresSQL.esDuplicado(e)) {
                throw new ElementoDuplicadoException("Ya existe un cliente con el email " + cliente.getEmail());
            }
            throw ErroresSQL.tecnico("insertar el cliente", e);
        }
    }

    @Override
    public void modificar(Cliente cliente) throws ElementoNoEncontradoException, ElementoDuplicadoException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_cliente_modificar(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, cliente.getIdCliente());
            cs.setString(2, cliente.getNombre());
            cs.setString(3, cliente.getApellidos());
            cs.setString(4, cliente.getEmail());
            cs.setString(5, cliente.getTelefono());
            cs.execute();
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException("No existe un cliente con id " + cliente.getIdCliente());
            }
            if (ErroresSQL.esDuplicado(e)) {
                throw new ElementoDuplicadoException("Ya existe un cliente con el email " + cliente.getEmail());
            }
            throw ErroresSQL.tecnico("modificar el cliente", e);
        }
    }

    @Override
    public void eliminar(int idCliente) throws ElementoNoEncontradoException, OperacionInvalidaException {
        try (CallableStatement cs = ConexionBD.getConexion().prepareCall("{CALL sp_cliente_eliminar(?)}")) {
            cs.setInt(1, idCliente);
            cs.execute();
        } catch (SQLException e) {
            if (ErroresSQL.esNoEncontrado(e)) {
                throw new ElementoNoEncontradoException("No existe un cliente con id " + idCliente);
            }
            if (ErroresSQL.esOperacionInvalida(e)) {
                throw new OperacionInvalidaException(ErroresSQL.mensaje(e));
            }
            throw ErroresSQL.tecnico("eliminar el cliente", e);
        }
    }

    @Override
    public Optional<Cliente> buscar(int idCliente) {
        List<Cliente> encontrados = consultar("WHERE id_cliente = ? ", idCliente);
        return encontrados.isEmpty() ? Optional.empty() : Optional.of(encontrados.get(0));
    }

    @Override
    public List<Cliente> listar() {
        return consultar("");
    }

    @Override
    public List<Cliente> buscarPorTexto(String texto) {
        // INSTR no trata % ni _ como comodines (a diferencia de LIKE) y, con la collation de las
        // tablas, no distingue mayúsculas ni acentos.
        return consultar("WHERE INSTR(nombre, ?) > 0 OR INSTR(apellidos, ?) > 0 ", texto, texto);
    }

    @Override
    public boolean existeEmail(String email, Integer idClienteExcluido) {
        String sql = "SELECT 1 FROM cliente WHERE email = ? AND id_cliente <> ? LIMIT 1";
        try (PreparedStatement ps = ConexionBD.getConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idClienteExcluido == null ? 0 : idClienteExcluido); // los ids empiezan en 1
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("comprobar el email", e);
        }
    }

    private List<Cliente> consultar(String condicion, Object... parametros) {
        try (PreparedStatement ps = ConexionBD.getConexion().prepareStatement(COLUMNAS + condicion + "ORDER BY id_cliente")) {
            Sql.asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (rs.next()) {
                    clientes.add(new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"),
                            rs.getString("apellidos"), rs.getString("email"), rs.getString("telefono")));
                }
                return clientes;
            }
        } catch (SQLException e) {
            throw ErroresSQL.tecnico("consultar los clientes", e);
        }
    }
}
