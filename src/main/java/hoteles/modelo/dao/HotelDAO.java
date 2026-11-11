package hoteles.modelo.dao;

import hoteles.modelo.Hotel;

import java.util.List;
import java.util.Optional;

/** Acceso a los datos de los hoteles. Los hoteles que se devuelven llevan ya cargadas sus habitaciones. */
public interface HotelDAO {

    /** Guarda el hotel (sin habitaciones) y le asigna el id generado por la base de datos. */
    void insertar(Hotel hotel);

    Optional<Hotel> buscar(int idHotel);

    /** Todos los hoteles ordenados por id. */
    List<Hotel> listar();

    /** Hoteles de una ciudad, sin distinguir mayúsculas ni acentos. */
    List<Hotel> buscarPorCiudad(String ciudad);
}
