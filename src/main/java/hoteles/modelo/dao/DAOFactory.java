package hoteles.modelo.dao;

import hoteles.modelo.dao.mysql.MySQLDAOFactory;

/**
 * Patrón Factory (fábrica abstracta) para crear los DAO.
 * <p>
 * El modelo ({@code Datos}) solo conoce esta clase y las interfaces DAO. Qué motor de base de datos
 * hay detrás lo decide {@link #obtener()} leyendo {@code bd.tipo} de la configuración. Para cambiar de
 * motor (PostgreSQL, SQLite...) basta con crear un paquete nuevo con sus DAO y su fábrica, y añadir un
 * valor a {@link TipoBD} y un caso en {@link #obtener(TipoBD)}: ni la lógica de negocio ni el
 * controlador ni la vista cambian.
 */
public abstract class DAOFactory {

    /** Fábrica del motor indicado en la configuración (MYSQL por defecto). */
    public static DAOFactory obtener() {
        String tipo = Configuracion.get("bd.tipo", TipoBD.MYSQL.name());
        try {
            return obtener(TipoBD.valueOf(tipo.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Tipo de base de datos no soportado en bd.tipo: " + tipo, e);
        }
    }

    public static DAOFactory obtener(TipoBD tipo) {
        switch (tipo) {
            case MYSQL:
                return new MySQLDAOFactory();
            default:
                throw new PersistenciaException("Tipo de base de datos no soportado: " + tipo);
        }
    }

    public abstract ClienteDAO getClienteDAO();

    public abstract HotelDAO getHotelDAO();

    public abstract HabitacionDAO getHabitacionDAO();

    public abstract ReservaDAO getReservaDAO();

    /** Abre una transacción (ver {@link Transaccion}). */
    public abstract Transaccion iniciarTransaccion();
}
