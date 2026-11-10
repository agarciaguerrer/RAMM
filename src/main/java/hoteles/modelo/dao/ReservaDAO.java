package hoteles.modelo.dao;

import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.HabitacionNoDisponibleException;
import hoteles.modelo.OperacionInvalidaException;
import hoteles.modelo.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso a los datos de las reservas. Cada reserva se guarda y se lee junto con sus detalles (habitaciones). */
public interface ReservaDAO {

    /**
     * Guarda la reserva y todos sus detalles, asignando los ids generados por la base de datos.
     * Debe llamarse dentro de una {@link Transaccion}: son varias filas que se confirman o se deshacen juntas.
     */
    void insertar(Reserva reserva) throws ElementoNoEncontradoException, HabitacionNoDisponibleException;

    /** Guarda las nuevas fechas y número de personas de la reserva. */
    void actualizar(Reserva reserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException;

    void cambiarEstado(int idReserva, EstadoReserva estado) throws ElementoNoEncontradoException;

    Optional<Reserva> buscar(int idReserva);

    /** Todas las reservas ordenadas por id. */
    List<Reserva> listar();

    /** Filtros opcionales: un filtro a null no se aplica. Las fechas son la fecha exacta de entrada o de salida. */
    List<Reserva> filtrar(EstadoReserva estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                          Integer idCliente, Integer idHotel);

    /**
     * Reservas no canceladas del hotel cuyas fechas se solapan con [entrada, salida): el día de salida
     * queda libre para una nueva entrada. Sirve para comprobar la disponibilidad de las habitaciones
     * sin tener que cargar todas las reservas. Si {@code idReservaExcluida} no es null, esa reserva no cuenta.
     */
    List<Reserva> listarSolapadas(int idHotel, LocalDate entrada, LocalDate salida, Integer idReservaExcluida);
}
