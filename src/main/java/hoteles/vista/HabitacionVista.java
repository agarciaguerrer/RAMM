package hoteles.vista;

import hoteles.controlador.HabitacionControlador;

import java.time.LocalDate;
import java.util.NoSuchElementException;

/** Menú y pantallas de la gestión de habitaciones. */
class HabitacionVista {

    private final Consola consola;
    private final HabitacionControlador controlador;
    private final SelectorHotel selector;

    HabitacionVista(Consola consola, HabitacionControlador controlador, SelectorHotel selector) {
        this.consola = consola;
        this.controlador = controlador;
        this.selector = selector;
    }

    void menu() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE HABITACIONES ---");
            System.out.println("1. Listar habitaciones de un hotel");
            System.out.println("2. Buscar habitaciones disponibles según la fecha");
            System.out.println("3. Filtrar habitaciones por número de personas");
            System.out.println("4. Añadir habitación a un hotel");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> consola.ejecutar(this::listarHabitaciones);
                case 2 -> consola.ejecutar(this::habitacionesDisponibles);
                case 3 -> consola.ejecutar(this::habitacionesPorCapacidad);
                case 4 -> consola.ejecutar(this::anadirHabitacion);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void anadirHabitacion() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        anadirHabitacionAHotel(idHotel);
    }

    /** Pide los datos de una habitación y la añade al hotel indicado. */
    void anadirHabitacionAHotel(int idHotel) throws Exception {
        System.out.println("(Escribe '" + Consola.PALABRA_CANCELAR + "' para dejar de añadir esta habitación)");
        String numero = consola.pedirTexto("Número de habitación: ",
                v -> controlador.validarNumeroHabitacion(idHotel, v));
        String categoria = consola.pedirTexto("Categoría (Estándar, Familiar, Suite...): ",
                controlador::validarCategoria);
        int capacidad = consola.pedirEntero("Capacidad (personas): ", controlador::validarCapacidad);
        double precio = consola.pedirDecimal("Precio por noche (€): ", controlador::validarPrecio);
        System.out.println("Habitación añadida:\n  "
                + controlador.anadirHabitacion(idHotel, numero, categoria, capacidad, precio));
    }

    /** Pregunta si se quieren añadir habitaciones a un hotel recién creado, tantas como se quiera. */
    void anadirVariasAHotel(int idHotel) {
        String pregunta = "¿Quieres añadir habitaciones a este hotel ahora?";
        while (consola.preguntarSiNo(pregunta)) {
            try {
                anadirHabitacionAHotel(idHotel);
            } catch (Consola.OperacionCancelada e) {
                System.out.println("Habitación no añadida.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            pregunta = "¿Quieres añadir otra habitación?";
        }
    }

    void listarHabitaciones() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        consola.mostrar(controlador.listarHabitaciones(idHotel), "Este hotel no tiene habitaciones.");
    }

    /** Habitaciones de un hotel que no tienen reserva activa en las fechas pedidas. */
    void habitacionesDisponibles() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        LocalDate entrada = consola.pedirFecha("Fecha de entrada" + Consola.TEXTO_FECHA, f -> { });
        LocalDate salida = consola.pedirFecha("Fecha de salida" + Consola.TEXTO_FECHA,
                f -> controlador.validarRango(entrada, f));
        consola.mostrar(controlador.habitacionesDisponibles(idHotel, entrada, salida),
                "No hay habitaciones disponibles en esas fechas.");
    }

    void habitacionesPorCategoria() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        String categoria = consola.pedirTexto("Categoría: ", controlador::validarCategoria);
        consola.mostrar(controlador.buscarHabitacionesPorCategoria(idHotel, categoria),
                "No hay habitaciones de esa categoría.");
    }

    private void habitacionesPorCapacidad() throws Exception {
        consola.avisoCancelar();
        int idHotel = selector.elegirHotel();
        int personas = consola.pedirEntero("Número de personas: ", controlador::validarPersonas);
        consola.mostrar(controlador.buscarHabitacionesPorCapacidad(idHotel, personas),
                "No hay habitaciones con capacidad para esas personas.");
    }
}
