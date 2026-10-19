package hoteles.vista;

import hoteles.controlador.Controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Única clase que interactúa con el usuario (Scanner y System.out).
 * Solo se comunica con el Controlador; no importa nada del paquete modelo.
 */
public class Vista {

    /** Acción de menú que puede lanzar cualquier excepción del modelo. */
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static final DateTimeFormatter FORMATO_ES = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Controlador controlador;
    private final Scanner scanner = new Scanner(System.in);

    public Vista(Controlador controlador) {
        this.controlador = controlador;
    }

    public void iniciar() {
        System.out.println("=====================================");
        System.out.println("   GESTIÓN DE HOTEL - Grupo RAMM");
        System.out.println("=====================================");
        if (preguntarSiNo("¿Cargar datos de ejemplo?")) {
            controlador.cargarDatosPrueba();
            System.out.println("Datos de ejemplo cargados.");
        }
        try {
            menuPrincipal();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Saliendo...");
        }
    }

    // ------------------------------------------------------------------ Menús

    private void menuPrincipal() {
        int opcion;
        do {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestión de clientes");
            System.out.println("2. Gestión de hoteles");
            System.out.println("3. Gestión de habitaciones");
            System.out.println("4. Gestión de reservas");
            System.out.println("5. Consulta de reservas");
            System.out.println("0. Salir");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> menuClientes();
                case 2 -> menuHoteles();
                case 3 -> menuHabitaciones();
                case 4 -> menuReservas();
                case 5 -> menuConsultaReservas();
                case 0 -> System.out.println("¡Hasta pronto!");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void menuClientes() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE CLIENTES ---");
            System.out.println("1. Listar clientes");
            System.out.println("2. Añadir nuevo cliente");
            System.out.println("3. Eliminar cliente");
            System.out.println("4. Buscar cliente por nombre o apellido");
            System.out.println("5. Modificar datos de un cliente");
            System.out.println("6. Consultar reservas de un cliente");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> mostrar(controlador.listarClientes(), "No hay clientes registrados.");
                case 2 -> ejecutar(this::anadirCliente);
                case 3 -> ejecutar(this::eliminarCliente);
                case 4 -> mostrar(controlador.buscarClientes(leerTexto("Nombre o apellido: ")),
                        "No se encontró ningún cliente.");
                case 5 -> ejecutar(this::modificarCliente);
                case 6 -> ejecutar(this::reservasDeCliente);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void menuHoteles() {
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
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> mostrar(controlador.listarHoteles(), "No hay hoteles registrados.");
                case 2 -> mostrar(controlador.buscarHotelesPorCiudad(leerTexto("Ciudad: ")),
                        "No hay hoteles en esa ciudad.");
                case 3 -> ejecutar(this::contarDisponibles);
                case 4 -> ejecutar(this::habitacionesDisponibles);
                case 5 -> ejecutar(this::habitacionesPorCategoria);
                case 6 -> ejecutar(this::anadirHotel);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void menuHabitaciones() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE HABITACIONES ---");
            System.out.println("1. Listar habitaciones de un hotel");
            System.out.println("2. Buscar habitaciones disponibles según la fecha");
            System.out.println("3. Filtrar habitaciones por número de personas");
            System.out.println("4. Añadir habitación a un hotel");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::listarHabitaciones);
                case 2 -> ejecutar(this::habitacionesDisponibles);
                case 3 -> ejecutar(this::habitacionesPorCapacidad);
                case 4 -> ejecutar(this::anadirHabitacion);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void menuReservas() {
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
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> ejecutar(this::crearReserva);
                case 2 -> ejecutar(this::consultarReserva);
                case 3 -> ejecutar(this::modificarReserva);
                case 4 -> ejecutar(() -> System.out.println(
                        "Reserva confirmada:\n" + controlador.confirmarReserva(leerEntero("Id de la reserva: "))));
                case 5 -> ejecutar(() -> System.out.println(
                        "Reserva cancelada:\n" + controlador.cancelarReserva(leerEntero("Id de la reserva: "))));
                case 6 -> ejecutar(() -> System.out.printf("Importe total: %.2f €%n",
                        controlador.calcularImporte(leerEntero("Id de la reserva: "))));
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void menuConsultaReservas() {
        int opcion;
        do {
            System.out.println("\n--- CONSULTA DE RESERVAS ---");
            System.out.println("1. Consultar todas las reservas existentes");
            System.out.println("2. Consultar por filtros (estado, fechas, cliente, hotel)");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> mostrar(controlador.listarReservas(), "No hay reservas registradas.");
                case 2 -> ejecutar(this::consultarReservasPorFiltro);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // --------------------------------------------------------------- Acciones

    private void anadirCliente() throws Exception {
        String nombre = leerTexto("Nombre: ");
        String apellidos = leerTexto("Apellidos: ");
        String email = leerTexto("Email: ");
        String telefono = leerTexto("Teléfono: ");
        System.out.println("Cliente añadido:\n" + controlador.anadirCliente(nombre, apellidos, email, telefono));
    }

    private void eliminarCliente() throws Exception {
        controlador.eliminarCliente(leerEntero("Id del cliente a eliminar: "));
        System.out.println("Cliente eliminado.");
    }

    private void modificarCliente() throws Exception {
        int id = leerEntero("Id del cliente: ");
        String nombre = leerTexto("Nuevo nombre: ");
        String apellidos = leerTexto("Nuevos apellidos: ");
        String email = leerTexto("Nuevo email: ");
        String telefono = leerTexto("Nuevo teléfono: ");
        System.out.println("Cliente modificado:\n" + controlador.modificarCliente(id, nombre, apellidos, email, telefono));
    }

    private void reservasDeCliente() throws Exception {
        mostrar(controlador.reservasDeCliente(leerEntero("Id del cliente: ")),
                "Este cliente no tiene reservas.");
    }

    private void anadirHotel() throws Exception {
        String nombre = leerTexto("Nombre del hotel: ");
        String ciudad = leerTexto("Ciudad: ");
        String direccion = leerTexto("Dirección: ");
        System.out.println("Hotel añadido:\n" + controlador.anadirHotel(nombre, ciudad, direccion));
    }

    private void anadirHabitacion() throws Exception {
        int idHotel = elegirHotel();
        String numero = leerTexto("Número de habitación: ");
        String categoria = leerTexto("Categoría (Estándar, Familiar, Suite...): ");
        int capacidad = leerEntero("Capacidad (personas): ");
        double precio = leerDecimal("Precio por noche (€): ");
        System.out.println("Habitación añadida:\n"
                + controlador.anadirHabitacion(idHotel, numero, categoria, capacidad, precio));
    }

    private void listarHabitaciones() throws Exception {
        int idHotel = elegirHotel();
        mostrar(controlador.listarHabitaciones(idHotel), "Este hotel no tiene habitaciones.");
    }

    private void habitacionesPorCategoria() throws Exception {
        int idHotel = elegirHotel();
        String categoria = leerTexto("Categoría: ");
        mostrar(controlador.buscarHabitacionesPorCategoria(idHotel, categoria),
                "No hay habitaciones de esa categoría.");
    }

    private void habitacionesPorCapacidad() throws Exception {
        int idHotel = elegirHotel();
        int personas = leerEntero("Número de personas: ");
        mostrar(controlador.buscarHabitacionesPorCapacidad(idHotel, personas),
                "No hay habitaciones con capacidad para esas personas.");
    }

    private void contarDisponibles() throws Exception {
        int idHotel = elegirHotel();
        LocalDate entrada = leerFecha("Fecha de entrada (dd/MM/yyyy o yyyy-MM-dd): ");
        LocalDate salida = leerFecha("Fecha de salida: ");
        System.out.println("Habitaciones disponibles: " + controlador.contarHabitacionesDisponibles(idHotel, entrada, salida));
    }

    private void habitacionesDisponibles() throws Exception {
        int idHotel = elegirHotel();
        LocalDate entrada = leerFecha("Fecha de entrada (dd/MM/yyyy o yyyy-MM-dd): ");
        LocalDate salida = leerFecha("Fecha de salida: ");
        mostrar(controlador.habitacionesDisponibles(idHotel, entrada, salida),
                "No hay habitaciones disponibles en esas fechas.");
    }

    /** Sigue los pasos del caso de uso: cliente, hotel, personas, fechas y habitaciones disponibles. */
    private void crearReserva() throws Exception {
        System.out.println("\nClientes:");
        mostrar(controlador.listarClientes(), "No hay clientes. Añade uno primero.");
        int idCliente = leerEntero("Id del cliente: ");
        int idHotel = elegirHotel();
        int personas = leerEntero("Número de personas: ");
        LocalDate entrada = leerFecha("Fecha de entrada (dd/MM/yyyy o yyyy-MM-dd): ");
        LocalDate salida = leerFecha("Fecha de salida: ");

        System.out.println("\nHabitaciones disponibles:");
        List<String> disponibles = controlador.habitacionesDisponibles(idHotel, entrada, salida);
        if (disponibles.isEmpty()) {
            System.out.println("No hay habitaciones disponibles en esas fechas.");
            return;
        }
        mostrar(disponibles, "");
        System.out.println("(El id de habitación es el de la lista de habitaciones del hotel)");
        List<Integer> ids = leerListaEnteros("Ids de las habitaciones a reservar (separados por comas): ");

        System.out.println("Reserva creada:\n" + controlador.crearReserva(idCliente, idHotel, entrada, salida, personas, ids));
    }

    private void consultarReserva() throws Exception {
        int id = leerEntero("Id de la reserva: ");
        System.out.println(controlador.buscarReserva(id));
        System.out.println("Habitaciones:");
        mostrar(controlador.detallesReserva(id), "");
    }

    private void modificarReserva() throws Exception {
        int id = leerEntero("Id de la reserva: ");
        LocalDate entrada = leerFecha("Nueva fecha de entrada (dd/MM/yyyy o yyyy-MM-dd): ");
        LocalDate salida = leerFecha("Nueva fecha de salida: ");
        int personas = leerEntero("Nuevo número de personas: ");
        System.out.println("Reserva modificada:\n" + controlador.modificarReserva(id, entrada, salida, personas));
    }

    private void consultarReservasPorFiltro() {
        System.out.println("Deja vacío cualquier filtro que no quieras aplicar.");
        String estado = leerTextoOpcional("Estado (PENDIENTE, CONFIRMADA, CANCELADA): ");
        LocalDate entrada = leerFechaOpcional("Fecha de entrada: ");
        LocalDate salida = leerFechaOpcional("Fecha de salida: ");
        Integer idCliente = leerEnteroOpcional("Id de cliente: ");
        Integer idHotel = leerEnteroOpcional("Id de hotel: ");
        mostrar(controlador.consultarReservas(estado, entrada, salida, idCliente, idHotel),
                "No hay reservas que cumplan los filtros.");
    }

    private int elegirHotel() {
        System.out.println("\nHoteles:");
        mostrar(controlador.listarHoteles(), "No hay hoteles registrados.");
        return leerEntero("Id del hotel: ");
    }

    // ------------------------------------------------------------ Utilidades

    /** Ejecuta una acción y, si el modelo lanza una excepción, muestra su mensaje en lugar de romper el programa. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (java.util.NoSuchElementException e) {
            throw e; // fin de la entrada: lo gestiona iniciar()
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void mostrar(List<String> lineas, String mensajeVacio) {
        if (lineas.isEmpty()) {
            if (!mensajeVacio.isEmpty()) {
                System.out.println(mensajeVacio);
            }
            return;
        }
        for (String linea : lineas) {
            System.out.println("  " + linea);
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private String leerTextoOpcional(String mensaje) {
        String texto = leerTexto(mensaje);
        return texto.isEmpty() ? null : texto;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido.");
            }
        }
    }

    private Integer leerEnteroOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido o déjalo vacío.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número válido (por ejemplo 89.90).");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            LocalDate fecha = convertirFecha(leerTexto(mensaje));
            if (fecha != null) {
                return fecha;
            }
            System.out.println("Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd.");
        }
    }

    private LocalDate leerFechaOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            LocalDate fecha = convertirFecha(texto);
            if (fecha != null) {
                return fecha;
            }
            System.out.println("Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd, o déjala vacía.");
        }
    }

    private LocalDate convertirFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(texto, FORMATO_ES);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private List<Integer> leerListaEnteros(String mensaje) {
        while (true) {
            List<Integer> numeros = new ArrayList<>();
            try {
                for (String parte : leerTexto(mensaje).split(",")) {
                    numeros.add(Integer.parseInt(parte.trim()));
                }
                return numeros;
            } catch (NumberFormatException e) {
                System.out.println("Introduce números separados por comas (por ejemplo 1,3).");
            }
        }
    }

    private boolean preguntarSiNo(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje + " (s/n): ").toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) {
                return true;
            }
            if (r.equals("n") || r.equals("no")) {
                return false;
            }
        }
    }
}
