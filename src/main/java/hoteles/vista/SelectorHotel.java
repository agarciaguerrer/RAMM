package hoteles.vista;

import hoteles.controlador.HotelControlador;

/** Pantalla común para elegir un hotel; la usan las vistas de hoteles, habitaciones y reservas. */
class SelectorHotel {

    private final Consola consola;
    private final HotelControlador hoteles;

    SelectorHotel(Consola consola, HotelControlador hoteles) {
        this.consola = consola;
        this.hoteles = hoteles;
    }

    /** Muestra los hoteles y pide el id de uno que exista. */
    int elegirHotel() {
        if (!hoteles.hayHoteles()) {
            throw new IllegalStateException("No hay hoteles registrados. Añade uno primero.");
        }
        System.out.println("\nHoteles:");
        consola.mostrar(hoteles.listarHoteles(), "");
        return consola.pedirEntero("Id del hotel: ", hoteles::validarHotel);
    }
}
