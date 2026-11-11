package hoteles.vista;

import hoteles.controlador.Controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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

    /** Comprobación de un dato recién escrito: si no es válido, lanza una excepción con el motivo. */
    private interface Comprobacion<T> {
        void comprobar(T valor) throws Exception;
    }

    /** Se lanza cuando el usuario escribe "cancelar" en un formulario. */
    private static class OperacionCancelada extends RuntimeException {
    }

    private static final DateTimeFormatter FORMATO_ES = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String PALABRA_CANCELAR = "cancelar";
    private static final String TEXTO_FECHA = " (dd/MM/yyyy o yyyy-MM-dd): ";

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
        } catch (NoSuchElementException e) {
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
                case 4 -> ejecutar(this::confirmarReserva);
                case 5 -> ejecutar(this::cancelarReserva);
                case 6 -> ejecutar(this::calcularImporte);
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

    // ----------------------------------------------------- Acciones: clientes

    private void anadirCliente() throws Exception {
        avisoCancelar();
        String nombre = pedirTexto("Nombre: ", controlador::validarNombre);
        String apellidos = pedirTexto("Apellidos: ", controlador::validarApellidos);
        String email = pedirTexto("Email: ", v -> controlador.validarEmail(v, null));
        String telefono = pedirTexto("Teléfono: ", controlador::validarTelefono);
        System.out.println("Cliente añadido:\n  " + controlador.anadirCliente(nombre, apellidos, email, telefono));
    }

    private void eliminarCliente() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id del cliente a eliminar: ", controlador::validarCliente);
        controlador.eliminarCliente(id);
        System.out.println("Cliente eliminado.");
    }

    private void modificarCliente() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id del cliente: ", controlador::validarCliente);
        String nombre = pedirTexto("Nuevo nombre: ", controlador::validarNombre);
        String apellidos = pedirTexto("Nuevos apellidos: ", controlador::validarApellidos);
        String email = pedirTexto("Nuevo email: ", v -> controlador.validarEmail(v, id));
        String telefono = pedirTexto("Nuevo teléfono: ", controlador::validarTelefono);
        System.out.println("Cliente modificado:\n  " + controlador.modificarCliente(id, nombre, apellidos, email, telefono));
    }

    private void reservasDeCliente() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id del cliente: ", controlador::validarCliente);
        mostrar(controlador.reservasDeCliente(id), "Este cliente no tiene reservas.");
    }

    // ------------------------------------------------------ Acciones: hoteles

    /** Da de alta el hotel y, a continuación, permite añadirle todas las habitaciones que se quiera. */
    private void anadirHotel() throws Exception {
        avisoCancelar();
        String nombre = pedirTexto("Nombre del hotel: ", controlador::validarNombreHotel);
        String ciudad = pedirTexto("Ciudad: ", controlador::validarCiudad);
        String direccion = pedirTexto("Dirección: ", controlador::validarDireccion);
        int idHotel = controlador.anadirHotel(nombre, ciudad, direccion);
        System.out.println("Hotel añadido:\n  " + controlador.mostrarHotel(idHotel));

        String pregunta = "¿Quieres añadir habitaciones a este hotel ahora?";
        while (preguntarSiNo(pregunta)) {
            try {
                anadirHabitacionAHotel(idHotel);
            } catch (OperacionCancelada e) {
                System.out.println("Habitación no añadida.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            pregunta = "¿Quieres añadir otra habitación?";
        }
        System.out.println("Resultado final:\n  " + controlador.mostrarHotel(idHotel));
        mostrar(controlador.listarHabitaciones(idHotel), "");
    }

    private void contarDisponibles() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        LocalDate entrada = pedirFecha("Fecha de entrada" + TEXTO_FECHA, f -> { });
        LocalDate salida = pedirFecha("Fecha de salida" + TEXTO_FECHA, f -> controlador.validarRango(entrada, f));
        System.out.println("Habitaciones disponibles: "
                + controlador.contarHabitacionesDisponibles(idHotel, entrada, salida));
    }

    private void habitacionesPorCategoria() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        String categoria = pedirTexto("Categoría: ", controlador::validarCategoria);
        mostrar(controlador.buscarHabitacionesPorCategoria(idHotel, categoria),
                "No hay habitaciones de esa categoría.");
    }

    // ------------------------------------------------- Acciones: habitaciones

    private void anadirHabitacion() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        anadirHabitacionAHotel(idHotel);
    }

    private void anadirHabitacionAHotel(int idHotel) throws Exception {
        System.out.println("(Escribe '" + PALABRA_CANCELAR + "' para dejar de añadir esta habitación)");
        String numero = pedirTexto("Número de habitación: ", v -> controlador.validarNumeroHabitacion(idHotel, v));
        String categoria = pedirTexto("Categoría (Estándar, Familiar, Suite...): ", controlador::validarCategoria);
        int capacidad = pedirEntero("Capacidad (personas): ", controlador::validarCapacidad);
        double precio = pedirDecimal("Precio por noche (€): ", controlador::validarPrecio);
        System.out.println("Habitación añadida:\n  "
                + controlador.anadirHabitacion(idHotel, numero, categoria, capacidad, precio));
    }

    private void listarHabitaciones() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        mostrar(controlador.listarHabitaciones(idHotel), "Este hotel no tiene habitaciones.");
    }

    private void habitacionesDisponibles() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        LocalDate entrada = pedirFecha("Fecha de entrada" + TEXTO_FECHA, f -> { });
        LocalDate salida = pedirFecha("Fecha de salida" + TEXTO_FECHA, f -> controlador.validarRango(entrada, f));
        mostrar(controlador.habitacionesDisponibles(idHotel, entrada, salida),
                "No hay habitaciones disponibles en esas fechas.");
    }

    private void habitacionesPorCapacidad() throws Exception {
        avisoCancelar();
        int idHotel = elegirHotel();
        int personas = pedirEntero("Número de personas: ", controlador::validarPersonas);
        mostrar(controlador.buscarHabitacionesPorCapacidad(idHotel, personas),
                "No hay habitaciones con capacidad para esas personas.");
    }

    // ---------------------------------------------------- Acciones: reservas

    /** Sigue los pasos del caso de uso: cliente, hotel, personas, fechas y habitaciones disponibles. */
    private void crearReserva() throws Exception {
        avisoCancelar();
        if (!controlador.hayClientes()) {
            throw new IllegalStateException("No hay clientes registrados. Añade uno primero.");
        }
        System.out.println("\nClientes:");
        mostrar(controlador.listarClientes(), "");
        int idCliente = pedirEntero("Id del cliente: ", controlador::validarCliente);
        int idHotel = elegirHotel();
        int personas = pedirEntero("Número de personas: ", controlador::validarPersonas);
        LocalDate entrada = pedirFecha("Fecha de entrada" + TEXTO_FECHA, f -> { });
        LocalDate salida = pedirFecha("Fecha de salida" + TEXTO_FECHA, f -> controlador.validarRango(entrada, f));

        System.out.println("\nHabitaciones disponibles:");
        List<String> disponibles = controlador.habitacionesDisponibles(idHotel, entrada, salida);
        if (disponibles.isEmpty()) {
            System.out.println("No hay habitaciones disponibles en esas fechas.");
            return;
        }
        mostrar(disponibles, "");
        System.out.println("(El id de cada habitación aparece entre paréntesis)");
        List<Integer> ids = pedirListaEnteros("Ids de las habitaciones a reservar (separados por comas): ",
                lista -> controlador.validarHabitacionesReserva(idHotel, entrada, salida, personas, lista));

        System.out.println("Reserva creada:\n  "
                + controlador.crearReserva(idCliente, idHotel, entrada, salida, personas, ids));
    }

    private void consultarReserva() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println(controlador.buscarReserva(id));
        System.out.println("Habitaciones:");
        mostrar(controlador.detallesReserva(id), "");
    }

    private void modificarReserva() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id de la reserva: ", controlador::validarReservaModificable);
        while (true) {
            LocalDate entrada = pedirFecha("Nueva fecha de entrada" + TEXTO_FECHA, f -> { });
            LocalDate salida = pedirFecha("Nueva fecha de salida" + TEXTO_FECHA,
                    f -> controlador.validarRango(entrada, f));
            int personas = pedirEntero("Nuevo número de personas: ", controlador::validarPersonas);
            try {
                System.out.println("Reserva modificada:\n  " + controlador.modificarReserva(id, entrada, salida, personas));
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
        avisoCancelar();
        int id = pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println("Reserva confirmada:\n  " + controlador.confirmarReserva(id));
    }

    private void cancelarReserva() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.println("Reserva cancelada:\n  " + controlador.cancelarReserva(id));
    }

    private void calcularImporte() throws Exception {
        avisoCancelar();
        int id = pedirEntero("Id de la reserva: ", controlador::validarReserva);
        System.out.printf("Importe total: %.2f €%n", controlador.calcularImporte(id));
    }

    private void consultarReservasPorFiltro() {
        avisoCancelar();
        System.out.println("Deja vacío cualquier filtro que no quieras aplicar.");
        String estado = pedirTextoOpcional("Estado (PENDIENTE, CONFIRMADA, CANCELADA): ", controlador::validarEstado);
        LocalDate entrada = pedirFechaOpcional("Fecha de entrada" + TEXTO_FECHA);
        LocalDate salida = pedirFechaOpcional("Fecha de salida" + TEXTO_FECHA);
        Integer idCliente = pedirEnteroOpcional("Id de cliente: ", controlador::validarCliente);
        Integer idHotel = pedirEnteroOpcional("Id de hotel: ", controlador::validarHotel);
        mostrar(controlador.consultarReservas(estado, entrada, salida, idCliente, idHotel),
                "No hay reservas que cumplan los filtros.");
    }

    /** Muestra los hoteles y pide el id de uno que exista. */
    private int elegirHotel() {
        if (!controlador.hayHoteles()) {
            throw new IllegalStateException("No hay hoteles registrados. Añade uno primero.");
        }
        System.out.println("\nHoteles:");
        mostrar(controlador.listarHoteles(), "");
        return pedirEntero("Id del hotel: ", controlador::validarHotel);
    }

    // ------------------------------------------------------------ Utilidades

    /**
     * Ejecuta una acción y, si el modelo lanza una excepción, muestra su mensaje en lugar de
     * romper el programa.
     */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (OperacionCancelada e) {
            System.out.println("Operación cancelada.");
        } catch (NoSuchElementException e) {
            throw e; // fin de la entrada: lo gestiona iniciar()
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void avisoCancelar() {
        System.out.println("(Escribe '" + PALABRA_CANCELAR + "' en cualquier momento para volver al menú)");
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

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido.");
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

    // Los métodos "pedir..." repiten la pregunta hasta que el dato es correcto.
    // Si el valor es incorrecto se muestra el motivo y se vuelve a preguntar lo mismo,
    // sin avanzar al siguiente dato. Escribir "cancelar" aborta la operación.

    private void comprobarCancelar(String texto) {
        if (texto.equalsIgnoreCase(PALABRA_CANCELAR)) {
            throw new OperacionCancelada();
        }
    }

    private String pedirTexto(String mensaje, Comprobacion<String> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                comprobacion.comprobar(texto);
                return texto;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private String pedirTextoOpcional(String mensaje, Comprobacion<String> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                comprobacion.comprobar(texto);
                return texto;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private int pedirEntero(String mensaje, Comprobacion<Integer> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                int valor = Integer.parseInt(texto);
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número entero válido.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private Integer pedirEnteroOpcional(String mensaje, Comprobacion<Integer> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int valor = Integer.parseInt(texto);
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número entero válido o déjalo vacío.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private double pedirDecimal(String mensaje, Comprobacion<Double> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                double valor = Double.parseDouble(texto.replace(',', '.'));
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número válido (por ejemplo 89.90).");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private LocalDate pedirFecha(String mensaje, Comprobacion<LocalDate> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            LocalDate fecha = convertirFecha(texto);
            if (fecha == null) {
                System.out.println("ERROR: Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd.");
                continue;
            }
            try {
                comprobacion.comprobar(fecha);
                return fecha;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private LocalDate pedirFechaOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            LocalDate fecha = convertirFecha(texto);
            if (fecha != null) {
                return fecha;
            }
            System.out.println("ERROR: Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd, o déjala vacía.");
        }
    }

    private List<Integer> pedirListaEnteros(String mensaje, Comprobacion<List<Integer>> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            List<Integer> numeros = new ArrayList<>();
            try {
                for (String parte : texto.split(",")) {
                    numeros.add(Integer.parseInt(parte.trim()));
                }
                comprobacion.comprobar(numeros);
                return numeros;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce números separados por comas (por ejemplo 1,3).");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
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
}
