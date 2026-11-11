package hoteles.modelo.dao.mysql;

import hoteles.modelo.dao.ClienteDAO;
import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.modelo.dao.HotelDAO;
import hoteles.modelo.dao.ReservaDAO;
import hoteles.modelo.dao.Transaccion;

/** Fábrica concreta: crea los DAO que hablan con MySQL. */
public class MySQLDAOFactory extends DAOFactory {

    @Override
    public ClienteDAO getClienteDAO() {
        return new MySQLClienteDAO();
    }

    @Override
    public HotelDAO getHotelDAO() {
        return new MySQLHotelDAO();
    }

    @Override
    public HabitacionDAO getHabitacionDAO() {
        return new MySQLHabitacionDAO();
    }

    @Override
    public ReservaDAO getReservaDAO() {
        return new MySQLReservaDAO();
    }

    @Override
    public Transaccion iniciarTransaccion() {
        return new MySQLTransaccion();
    }
}
