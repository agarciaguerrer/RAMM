package hoteles.modelo.dao;

/**
 * Transacción sobre la base de datos. Se usa con try-with-resources:
 *
 * <pre>
 * try (Transaccion tx = factory.iniciarTransaccion()) {
 *     ... varias operaciones DAO ...
 *     tx.confirmar();          // COMMIT
 * }                            // si no se llegó a confirmar (excepción), se hace ROLLBACK
 * </pre>
 *
 * De este modo un fallo en mitad de una operación (por ejemplo, la tercera habitación de una reserva
 * no está libre) deja la base de datos exactamente como estaba antes de empezar.
 * Si ya hay una transacción abierta, la nueva se une a ella (solo la más externa hace el COMMIT).
 */
public interface Transaccion extends AutoCloseable {

    /** Confirma los cambios (COMMIT). */
    void confirmar();

    /** Si la transacción no se confirmó, deshace los cambios (ROLLBACK). Nunca lanza excepciones comprobadas. */
    @Override
    void close();
}
