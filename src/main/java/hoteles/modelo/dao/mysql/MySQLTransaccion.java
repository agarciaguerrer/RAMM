package hoteles.modelo.dao.mysql;

import hoteles.modelo.dao.Transaccion;

/** Transacción de MySQL: delega en {@link ConexionBD}, que es quien tiene la conexión. */
final class MySQLTransaccion implements Transaccion {

    private boolean terminada = false;

    MySQLTransaccion() {
        ConexionBD.iniciarTransaccion();
    }

    @Override
    public void confirmar() {
        if (terminada) {
            throw new IllegalStateException("La transacción ya ha terminado.");
        }
        terminada = true;
        ConexionBD.confirmar();
    }

    @Override
    public void close() {
        if (!terminada) {
            terminada = true;
            ConexionBD.revertir();
        }
    }
}
