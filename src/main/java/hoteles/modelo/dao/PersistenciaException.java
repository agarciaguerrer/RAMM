package hoteles.modelo.dao;

/**
 * Error técnico de acceso a la base de datos (no se puede conectar, la consulta falla...).
 * <p>
 * Es una excepción no comprobada (RuntimeException) a propósito: el usuario no puede hacer nada
 * por recuperarla en el punto donde se produce, así que no obliga a declararla en cada método del
 * modelo y del controlador. La Vista ya captura cualquier excepción y muestra su mensaje, de modo
 * que el programa informa del problema y sigue funcionando (por ejemplo, para reintentar cuando
 * MySQL vuelva a estar en marcha).
 * <p>
 * Los errores de negocio (duplicados, elementos que no existen, habitaciones ocupadas...) no usan
 * esta clase: siguen siendo las excepciones propias del Producto 2.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
