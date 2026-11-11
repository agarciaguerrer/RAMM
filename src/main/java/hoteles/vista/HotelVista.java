package hoteles.vista;

import hoteles.controlador.HabitacionControlador;
import hoteles.controlador.HotelControlador;

import java.time.LocalDate;

/** Menú y pantallas de la gestión de hoteles. */
class HotelVista {

    private final Consola consola;
    private final HotelControlador controlador;
    private final HabitacionControlador habitaciones;
    private final HabitacionVista vistaHabitaciones;
    private final SelectorHotel selector;

    HotelVista(Consola consola, HotelControlador controlador, HabitacionControlador habitaciones,
               HabitacionVista vistaHabitaciones, SelectorHotel selector) {
        this.consola = consola;
        this.controlador = controlador;
        this.habitaciones = habitaciones;
        this.vistaHabitaciones = vistaHabitaciones;
        this.selector = selector;
    }

    void menu() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE HOTELES ---");
            System.out.println("1. Mostrar hoteles");
            System.out.println("2. Buscar hoteles por ciudad");
            System.out.println("3. Consultar número de habitaciones disponibles");
            System.out.println("4. Mostrar habitaciones disponibles");
            System.out.println("5. Mostrar habitaciones según categoría");
            System.out.println("6. Añadir hotel");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> consola.ejecutar(() -> consola.mostrar(controlador.listarHoteles(),
                        "No hay hoteles registrados."));
                case 2 -> consola.ejecutar(() -> consola.mostrar(
                        controlador.buscarHotelesPorCiudad(consola.leerTexto("Ciudad: ")),
                        "No hay hoteles en esa ciudad."));
                case 3 -> consola.ejecutar(this::contarDisponibles);
                case 4 -> consola.ejecutar(vistaHabitaciones::habitacionesDisponibles);
                case 5 -> consola.ejecutar(vistaHabitaciones::habitacionesPorCategoria);
                case 6 -> consola.ejecutar(this::anadirHotel);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    /** Da de alta el hotel y, a continuación, permite añadirle todas las habitaciones que se quiera. */
    private void anadirHotel() throws Exception {
        consola.avisoCancelar();
        String nombre = consola.pedirTexto("Nombre del hotel: ", controlador::validarNombreHotel);
        String ciudad = consola.pedirTexto("Ciudad: ", controlador::validarCiudad);
        String direccion = consola.pedirTexto("Dirección: ", controlador::validarDireccion);
        int idHotel = controlador.anadirHotel(nombre, ciudad, direccion);
        System.out.println("Hotel añadido:\n  " + controlador.mostrarHotel(idHotel));

        vistaHabitaciones.anadirVariasAHotel(idHotel);
        System.out.println("Resultado final:\n  " + controlador.mostrarHotel(idHotel));
        consola.mostrar(habitaciones.listarHabitaciones(idHotel), "");
    }

    private void contarDisponibles() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        LocalDate entrada = consola.pedirFecha("Fecha de entrada" + Consola.TEXTO_FECHA, f -> { });
        LocalDate salida = consola.pedirFecha("Fecha de salida" + Consola.TEXTO_FECHA,
                f -> habitaciones.validarRango(entrada, f));
        System.out.println("Habitaciones disponibles: "
                + controlador.contarHabitacionesDisponibles(idHotel, entrada, salida));
    }
}
