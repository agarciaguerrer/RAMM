package hoteles.modelo;

/** Se lanza cuando una operación no es válida en el estado actual (fechas incorrectas, reserva ya cancelada...). */
public class OperacionInvalidaException extends Exception {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
