package hoteles.vista;

import hoteles.controlador.ClienteControlador;
import hoteles.controlador.HabitacionControlador;
import hoteles.controlador.HotelControlador;
import hoteles.controlador.ReservaControlador;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** Menús y pantallas de la gestión y consulta de reservas. */
class ReservaVista {

    private final Consola consola;
    private final ReservaControlador controlador;
    private final ClienteControlador clientes;
    private final HotelControlador hoteles;
    private final HabitacionControlador habitaciones;
    private final SelectorHotel selector;

    ReservaVista(Consola consola, ReservaControlador controlador, ClienteControlador clientes,
                 HotelControlador hoteles, HabitacionControlador habitaciones, SelectorHotel selector) {
        this.consola = consola;
        this.controlador = controlador;
        this.clientes = clientes;
        this.hoteles = hoteles;
        this.habitaciones = habitaciones;
        this.selector = selector;
    }

    void menu() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE RESERVAS ---");
            System.out.println("1. Crear reserva");
            System.out.println("2. Consultar una reserva");
            System.out.println("3. Modificar reserva");
            System.out.println("4. Confirmar reserva");
            System.out.println("5. Cancelar reserva");
            System.out.println("6. Calcular importe de una reserva");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> consola.ejecutar(this::crearReserva);
                case 2 -> consola.ejecutar(this::consultarReserva);
                case 3 -> consola.ejecutar(this::modificarReserva);
                case 4 -> consola.ejecutar(this::confirmarReserva);
                case 5 -> consola.ejecutar(this::cancelarReserva);
                case 6 -> consola.ejecutar(this::calcularImporte);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    void menuConsulta() {
        int opcion;
        do {
            System.out.println("\n--- CONSULTA DE RESERVAS ---");
            System.out.println("1. Consultar todas las reservas existentes");
            System.out.println("2. Consultar por filtros (estado, fechas, cliente, hotel)");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> consola.ejecutar(() -> consola.mostrar(controlador.listarReservas(),
                        "No hay reservas registradas."));
                case 2 -> consola.ejecutar(this::consultarReservasPorFiltro);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    /** Sigue los pasos del caso de uso: cliente, hotel, personas, fechas y habitaciones disponibles. */
    private void crearReserva() throws Exception {
        consola.avisoCancelar();
        if (!clientes.hayClientes()) {
            throw new IllegalStateException("No hay clientes registrados. Añade uno primero.");
        }
        System.out.println("\nClientes:");
        consola.mostrar(clientes.listarClientes(), "");
        int idCliente = consola.pedirEntero("Id del cliente: ", clientes::validarCliente);
        int idHotel = selector.elegirHotel();
        int personas = consola.pedirEntero("Número de personas: ", habitaciones::validarPersonas);
        LocalDate entrada = consola.pedirFecha("Fecha de entrada" + Consola.TEXTO_FECHA, f -> { });
        LocalDate salida = consola.pedirFecha("Fecha de salida" + Consola.TEXTO_FECHA,
                f -> habitaciones.validarRango(entrada, f));

        System.out.println("\nHabitaciones disponibles:");
        List<String> disponibles = habitaciones.habitacionesDisponibles(idHotel, entrada, salida);
        if (disponibles.isEmpty()) {
            System.out.println("No hay habitaciones disponibles en esas fechas.");
            return;
        }
        consola.mostrar(disponibles, "");
        System.out.println("(El id de cada habitación aparece entre paréntesis)");
        List<Integer> ids = consola.pedirListaEnteros(
                "Ids de las habitaciones a reservar (separados por comas): ",
                lista -> controlador.validarHabitacionesReserva(idHotel, entrada, salida, personas, lista));

        System.out.println("Reserva creada:\n  "
                + controlador.crearReserva(idCliente, idHotel, entrada, salida, personas, ids));
    }

    private void consultarReserva() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println(controlador.buscarReserva(id));
        System.out.println("Habitaciones:");
        consola.mostrar(controlador.detallesReserva(id), "");
    }

    private void modificarReserva() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id de la reserva: ", controlador::validarReservaModificable);
        while (true) {
            LocalDate entrada = consola.pedirFecha("Nueva fecha de entrada" + Consola.TEXTO_FECHA, f -> { });
            LocalDate salida = consola.pedirFecha("Nueva fecha de salida" + Consola.TEXTO_FECHA,
                    f -> habitaciones.validarRango(entrada, f));
            int personas = consola.pedirEntero("Nuevo número de personas: ", habitaciones::validarPersonas);
            try {
                System.out.println("Reserva modificada:\n  "
                        + controlador.modificarReserva(id, entrada, salida, personas));
                return;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                // Cada dato era válido por separado, pero juntos no son posibles (p. ej. habitación ocupada)
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("Introduce de nuevo las fechas y las personas.");
            }
        }
    }

    private void confirmarReserva() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println("Reserva confirmada:\n  " + controlador.confirmarReserva(id));
    }

    private void cancelarReserva() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println("Reserva cancelada:\n  " + controlador.cancelarReserva(id));
    }

    private void calcularImporte() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.printf("Importe total: %.2f €%n", controlador.calcularImporte(id));
    }

    private void consultarReservasPorFiltro() {
        consola.avisoCancelar();
        System.out.println("Deja vacío cualquier filtro que no quieras aplicar.");
        String estado = consola.pedirTextoOpcional("Estado (PENDIENTE, CONFIRMADA, CANCELADA): ",
                controlador::validarEstado);
        LocalDate entrada = consola.pedirFechaOpcional("Fecha de entrada" + Consola.TEXTO_FECHA);
        LocalDate salida = consola.pedirFechaOpcional("Fecha de salida" + Consola.TEXTO_FECHA);
        Integer idCliente = consola.pedirEnteroOpcional("Id de cliente: ", clientes::validarCliente);
        Integer idHotel = consola.pedirEnteroOpcional("Id de hotel: ", hoteles::validarHotel);
        consola.mostrar(controlador.consultarReservas(estado, entrada, salida, idCliente, idHotel),
                "No hay reservas que cumplan los filtros.");
    }
}
