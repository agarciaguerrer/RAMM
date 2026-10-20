package hoteles.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

/**
 * Contiene todos los datos de la aplicación y realiza todas las operaciones que los afectan.
 * No lee ni escribe por consola: eso es cosa de la Vista.
 */
public class Datos {

    private final Repositorio<Cliente> clientes = new Repositorio<>("un cliente", Cliente::getIdCliente);
    private final Repositorio<Hotel> hoteles = new Repositorio<>("un hotel", Hotel::getIdHotel);
    // TreeMap: las reservas quedan siempre ordenadas por su id
    private final Repositorio<Reserva> reservas =
            new Repositorio<>("una reserva", Reserva::getIdReserva, new TreeMap<>());

    private int siguienteIdCliente = 1;
    private int siguienteIdHotel = 1;
    private int siguienteIdHabitacion = 1;
    private int siguienteIdReserva = 1;
    private int siguienteIdDetalle = 1;

    // ---------------------------------------------------------------- Clientes

    public Cliente anadirCliente(String nombre, String apellidos, String email, String telefono)
            throws ElementoDuplicadoException {
        String emailNuevo = email == null ? "" : email.trim();
        for (Cliente c : clientes.listar()) {
            if (c.getEmail().equalsIgnoreCase(emailNuevo)) {
                throw new ElementoDuplicadoException("Ya existe un cliente con el email " + emailNuevo);
            }
        }
        Cliente cliente = new Cliente(siguienteIdCliente, nombre, apellidos, email, telefono);
        clientes.anadir(cliente);
        siguienteIdCliente++;
        return cliente;
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public Cliente buscarCliente(int idCliente) throws ElementoNoEncontradoException {
        return clientes.buscar(idCliente);
    }

    /** Busca por coincidencia parcial en nombre o apellidos, sin distinguir mayúsculas. */
    public List<Cliente> buscarClientes(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        return clientes.filtrar(c -> c.getNombre().toLowerCase().contains(t)
                || c.getApellidos().toLowerCase().contains(t));
    }

    public Cliente modificarCliente(int idCliente, String nombre, String apellidos, String email, String telefono)
            throws ElementoNoEncontradoException {
        Cliente cliente = clientes.buscar(idCliente);
        cliente.actualizarDatos(nombre, apellidos, email, telefono);
        return cliente;
    }

    public void eliminarCliente(int idCliente) throws ElementoNoEncontradoException, OperacionInvalidaException {
        clientes.buscar(idCliente);
        for (Reserva r : reservasDeCliente(idCliente)) {
            if (r.getEstado() != EstadoReserva.CANCELADA) {
                throw new OperacionInvalidaException(
                        "No se puede eliminar un cliente con reservas activas (reserva #" + r.getIdReserva() + ")");
            }
        }
        clientes.eliminar(idCliente);
    }

    public List<Reserva> reservasDeCliente(int idCliente) throws ElementoNoEncontradoException {
        clientes.buscar(idCliente);
        return reservas.filtrar(r -> r.getCliente().getIdCliente() == idCliente);
    }

    // ----------------------------------------------------------------- Hoteles

    public Hotel anadirHotel(String nombre, String ciudad, String direccion) throws ElementoDuplicadoException {
        Hotel hotel = new Hotel(siguienteIdHotel, nombre, ciudad, direccion);
        hoteles.anadir(hotel);
        siguienteIdHotel++;
        return hotel;
    }

    public List<Hotel> listarHoteles() {
        return hoteles.listar();
    }

    public Hotel buscarHotel(int idHotel) throws ElementoNoEncontradoException {
        return hoteles.buscar(idHotel);
    }

    public List<Hotel> buscarHotelesPorCiudad(String ciudad) {
        String c = ciudad == null ? "" : ciudad.trim();
        return hoteles.filtrar(h -> h.getCiudad().equalsIgnoreCase(c));
    }

    // ------------------------------------------------------------ Habitaciones

    public Habitacion anadirHabitacion(int idHotel, String numero, String categoria, int capacidad,
                                       double precioNoche)
            throws ElementoNoEncontradoException, ElementoDuplicadoException {
        Hotel hotel = hoteles.buscar(idHotel);
        Habitacion habitacion = new Habitacion(siguienteIdHabitacion, numero, categoria, capacidad, precioNoche);
        for (Habitacion existente : hotel.listarHabitaciones()) {
            if (existente.getNumero().equalsIgnoreCase(habitacion.getNumero())) {
                throw new ElementoDuplicadoException(
                        "El hotel " + hotel.getNombre() + " ya tiene la habitación " + habitacion.getNumero());
            }
        }
        hotel.anadirHabitacion(habitacion);
        siguienteIdHabitacion++;
        return habitacion;
    }

    public List<Habitacion> listarHabitaciones(int idHotel) throws ElementoNoEncontradoException {
        return hoteles.buscar(idHotel).listarHabitaciones();
    }

    public List<Habitacion> buscarHabitacionesPorCategoria(int idHotel, String categoria)
            throws ElementoNoEncontradoException {
        return hoteles.buscar(idHotel).buscarPorCategoria(categoria);
    }

    public List<Habitacion> buscarHabitacionesPorCapacidad(int idHotel, int personas)
            throws ElementoNoEncontradoException {
        List<Habitacion> resultado = new ArrayList<>();
        for (Habitacion h : hoteles.buscar(idHotel).listarHabitaciones()) {
            if (h.validarCapacidad(personas)) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public List<Habitacion> habitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        comprobarRango(entrada, salida);
        List<Reserva> todas = reservas.listar();
        List<Habitacion> resultado = new ArrayList<>();
        for (Habitacion h : hoteles.buscar(idHotel).listarHabitaciones()) {
            if (h.estaDisponible(entrada, salida, todas)) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public long contarHabitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        comprobarRango(entrada, salida);
        return hoteles.buscar(idHotel).contarHabitacionesDisponibles(entrada, salida, reservas.listar());
    }

    // ---------------------------------------------------------------- Reservas

    public Reserva crearReserva(int idCliente, int idHotel, LocalDate entrada, LocalDate salida,
                                int personas, List<Integer> idsHabitaciones)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        Cliente cliente = clientes.buscar(idCliente);
        Hotel hotel = hoteles.buscar(idHotel);
        comprobarRango(entrada, salida);
        comprobarPersonas(personas);
        if (idsHabitaciones == null || idsHabitaciones.isEmpty()) {
            throw new OperacionInvalidaException("Hay que seleccionar al menos una habitación");
        }

        List<Habitacion> elegidas = seleccionarHabitaciones(hotel, new LinkedHashSet<>(idsHabitaciones));
        comprobarDisponibilidadYCapacidad(elegidas, entrada, salida, personas, reservas.listar());

        Reserva reserva = new Reserva(siguienteIdReserva, cliente, hotel, entrada, salida, personas,
                EstadoReserva.PENDIENTE);
        for (Habitacion h : elegidas) {
            reserva.anadirDetalle(new DetalleReserva(siguienteIdDetalle++, h, h.getPrecioNoche()));
        }
        try {
            reservas.anadir(reserva);
        } catch (ElementoDuplicadoException e) {
            // No debería ocurrir porque el id es autoincremental
            throw new OperacionInvalidaException("No se pudo registrar la reserva: " + e.getMessage());
        }
        siguienteIdReserva++;
        return reserva;
    }

    public Reserva buscarReserva(int idReserva) throws ElementoNoEncontradoException {
        return reservas.buscar(idReserva);
    }

    public List<Reserva> listarReservas() {
        return reservas.listar();
    }

    /**
     * Consulta de reservas con filtros opcionales: un filtro a null no se aplica.
     * Las fechas filtran por la fecha exacta de entrada o de salida.
     */
    public List<Reserva> consultarReservas(EstadoReserva estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                                           Integer idCliente, Integer idHotel) {
        return reservas.filtrar(r ->
                (estado == null || r.getEstado() == estado)
                        && (fechaEntrada == null || r.getFechaEntrada().equals(fechaEntrada))
                        && (fechaSalida == null || r.getFechaSalida().equals(fechaSalida))
                        && (idCliente == null || r.getCliente().getIdCliente() == idCliente)
                        && (idHotel == null || r.getHotel().getIdHotel() == idHotel));
    }

    public Reserva confirmarReserva(int idReserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        Reserva reserva = reservas.buscar(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new OperacionInvalidaException("No se puede confirmar una reserva cancelada");
        }
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            throw new OperacionInvalidaException("La reserva ya está confirmada");
        }
        if (reserva.getDetalles().isEmpty()) {
            throw new OperacionInvalidaException("La reserva no tiene habitaciones");
        }
        reserva.confirmar();
        return reserva;
    }

    public Reserva cancelarReserva(int idReserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        Reserva reserva = reservas.buscar(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new OperacionInvalidaException("La reserva ya está cancelada");
        }
        reserva.cancelar();
        return reserva;
    }

    public Reserva modificarReserva(int idReserva, LocalDate entrada, LocalDate salida, int personas)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        Reserva reserva = reservas.buscar(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new OperacionInvalidaException("No se puede modificar una reserva cancelada");
        }
        comprobarRango(entrada, salida);
        comprobarPersonas(personas);

        // La disponibilidad se comprueba contra el resto de reservas (la propia no cuenta)
        List<Reserva> otras = reservas.filtrar(r -> r.getIdReserva() != idReserva);
        List<Habitacion> habitaciones = new ArrayList<>();
        for (DetalleReserva d : reserva.getDetalles()) {
            habitaciones.add(d.getHabitacion());
        }
        comprobarDisponibilidadYCapacidad(habitaciones, entrada, salida, personas, otras);

        reserva.modificar(entrada, salida, personas);
        return reserva;
    }

    public double calcularImporte(int idReserva) throws ElementoNoEncontradoException {
        return reservas.buscar(idReserva).calcularImporte();
    }

    // ------------------------------------------------------------ Datos de demo

    /** Carga unos datos de ejemplo para poder probar la aplicación sin teclear todo. */
    public void cargarDatosPrueba() {
        try {
            anadirCliente("Laura", "Martín Pérez", "laura.martin@email.com", "600111222");
            anadirCliente("Carlos", "Ruiz Soler", "carlos.ruiz@email.com", "600333444");
            anadirCliente("Marta", "Soler Vidal", "marta.soler@email.com", "600555666");

            Hotel h1 = anadirHotel("Hotel Mar Azul", "Mataró", "Passeig Marítim 12");
            Hotel h2 = anadirHotel("Gran Hotel Centro", "Barcelona", "Gran Via 100");
            anadirHabitacion(h1.getIdHotel(), "101", "Estándar", 2, 80);
            anadirHabitacion(h1.getIdHotel(), "102", "Estándar", 2, 80);
            anadirHabitacion(h1.getIdHotel(), "201", "Familiar", 4, 130);
            anadirHabitacion(h1.getIdHotel(), "301", "Suite", 3, 210);
            anadirHabitacion(h2.getIdHotel(), "10", "Estándar", 2, 110);
            anadirHabitacion(h2.getIdHotel(), "20", "Suite", 4, 290);

            confirmarReserva(crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2,
                    List.of(1)).getIdReserva());
            crearReserva(2, 1, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 15), 4, List.of(3));
        } catch (Exception e) {
            throw new IllegalStateException("Error al cargar los datos de prueba: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------- Privados

    private void comprobarRango(LocalDate entrada, LocalDate salida) throws OperacionInvalidaException {
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new OperacionInvalidaException("La fecha de salida debe ser posterior a la de entrada");
        }
    }

    private void comprobarPersonas(int personas) throws OperacionInvalidaException {
        if (personas < 1) {
            throw new OperacionInvalidaException("El número de personas debe ser al menos 1");
        }
    }

    private List<Habitacion> seleccionarHabitaciones(Hotel hotel, LinkedHashSet<Integer> ids)
            throws ElementoNoEncontradoException {
        List<Habitacion> elegidas = new ArrayList<>();
        for (int id : ids) {
            Habitacion encontrada = null;
            for (Habitacion h : hotel.listarHabitaciones()) {
                if (h.getIdHabitacion() == id) {
                    encontrada = h;
                }
            }
            if (encontrada == null) {
                throw new ElementoNoEncontradoException(
                        "El hotel " + hotel.getNombre() + " no tiene ninguna habitación con id " + id);
            }
            elegidas.add(encontrada);
        }
        return elegidas;
    }

    /** Cada habitación elegida debe estar libre y tener capacidad para todas las personas de la reserva. */
    private void comprobarDisponibilidadYCapacidad(List<Habitacion> habitaciones, LocalDate entrada,
                                                   LocalDate salida, int personas, List<Reserva> otrasReservas)
            throws HabitacionNoDisponibleException {
        for (Habitacion h : habitaciones) {
            if (!h.estaDisponible(entrada, salida, otrasReservas)) {
                throw new HabitacionNoDisponibleException(
                        "La habitación " + h.getNumero() + " no está disponible del " + entrada + " al " + salida);
            }
            if (!h.validarCapacidad(personas)) {
                throw new HabitacionNoDisponibleException(
                        "La habitación " + h.getNumero() + " no tiene capacidad para " + personas
                                + " personas (máximo " + h.getCapacidad() + ")");
            }
        }
    }
}
