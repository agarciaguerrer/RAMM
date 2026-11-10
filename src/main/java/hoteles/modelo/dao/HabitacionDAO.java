package hoteles.modelo.dao;

import hoteles.modelo.ElementoDuplicadoException;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.Habitacion;

/**
 * Acceso a los datos de las habitaciones. Las habitaciones se leen siempre dentro de su hotel
 * ({@link HotelDAO}) o dentro de los detalles de una reserva ({@link ReservaDAO}).
 */
public interface HabitacionDAO {

    /** Guarda la habitación en el hotel indicado y le asigna el id generado por la base de datos. */
    void insertar(int idHotel, Habitacion habitacion) throws ElementoNoEncontradoException, ElementoDuplicadoException;

    /** Indica si el hotel ya tiene una habitación con ese número (sin distinguir mayúsculas). */
    boolean existeNumero(int idHotel, String numero);
}
