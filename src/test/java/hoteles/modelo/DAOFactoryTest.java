package hoteles.modelo;

import hoteles.modelo.dao.ClienteDAO;
import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.modelo.dao.HotelDAO;
import hoteles.modelo.dao.ReservaDAO;
import hoteles.modelo.dao.TipoBD;
import hoteles.modelo.dao.mysql.MySQLDAOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Pruebas del patrón Factory. No necesitan base de datos: crear los DAO no abre ninguna conexión. */
class DAOFactoryTest {

    @Test
    void laFabricaDeMySQLCreaTodosLosDAO() {
        DAOFactory fabrica = DAOFactory.obtener(TipoBD.MYSQL);

        assertInstanceOf(MySQLDAOFactory.class, fabrica);
        assertInstanceOf(ClienteDAO.class, fabrica.getClienteDAO());
        assertInstanceOf(HotelDAO.class, fabrica.getHotelDAO());
        assertInstanceOf(HabitacionDAO.class, fabrica.getHabitacionDAO());
        assertInstanceOf(ReservaDAO.class, fabrica.getReservaDAO());
    }

    @Test
    void elMotorPorDefectoSeLeeDeLaConfiguracion() {
        assertNotNull(DAOFactory.obtener());
        assertInstanceOf(MySQLDAOFactory.class, DAOFactory.obtener());
    }
}
