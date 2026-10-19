package hoteles.modelo;

/** Se lanza al buscar un elemento que no existe. */
public class ElementoNoEncontradoException extends Exception {

    public ElementoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
