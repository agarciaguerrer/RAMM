package hoteles.controlador;

import hoteles.modelo.Datos;

/**
 * Controlador del arranque de la aplicación. Como los demás controladores, es el único puente entre
 * la Vista y el Modelo: la Vista solo conoce los controladores, nunca el paquete modelo.
 */
public class InicioControlador {

    private final Datos datos;

    public InicioControlador(Datos datos) {
        this.datos = datos;
    }

    /** Devuelve false si no se han cargado porque la base de datos ya tenía datos. */
    public boolean cargarDatosPrueba() {
        return datos.cargarDatosPrueba();
    }
}
