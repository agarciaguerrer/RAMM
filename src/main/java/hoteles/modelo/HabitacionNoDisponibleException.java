package hoteles.modelo;

/** Se lanza al reservar una habitación que no está libre o no admite a las personas indicadas. */
public class HabitacionNoDisponibleException extends Exception {

    public HabitacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
