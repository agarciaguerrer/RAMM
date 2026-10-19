package hoteles.modelo;

/** Se lanza al intentar añadir un elemento cuyo identificador ya existe. */
public class ElementoDuplicadoException extends Exception {

    public ElementoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
