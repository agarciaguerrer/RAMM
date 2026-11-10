package hoteles.modelo;

import hoteles.modelo.dao.ClienteDAO;
import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.HabitacionDAO;
import hoteles.modelo.dao.HotelDAO;
import hoteles.modelo.dao.PersistenciaException;
import hoteles.modelo.dao.ReservaDAO;
import hoteles.modelo.dao.Transaccion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * - Contiene la lógica de negocio de la aplicación y realiza todas las operaciones sobre los datos.
 * - No lee ni escribe por consola: eso es cosa de la Vista.
 * - Desde el Producto 3 los datos ya no están en colecciones en memoria: se guardan en MySQL a través de
 *   los DAO que crea la {@link DAOFactory}. Esta clase solo conoce las interfaces DAO, no el motor de base
 *   de datos ni el SQL.
 * - Las operaciones que modifican datos se ejecutan dentro de una {@link Transaccion}: o se guardan todos
 *   sus cambios o no se guarda ninguno.
 * - Las excepciones de negocio son las del Producto 2; los fallos técnicos de la base de datos llegan como
 *   {@link PersistenciaException}.
 */
public class Datos {

    private final DAOFactory fabrica;
    private final ClienteDAO clienteDAO;
    private final HotelDAO hotelDAO;
    private final HabitacionDAO habitacionDAO;
    private final ReservaDAO reservaDAO;

    /** Usa el motor de base de datos indicado en la configuración (bd.tipo). */
    public Datos() {
        this(DAOFactory.obtener());
    }

    public Datos(DAOFactory fabrica) {
        this.fabrica = fabrica;
        this.clienteDAO = fabrica.getClienteDAO();
        this.hotelDAO = fabrica.getHotelDAO();
        this.habitacionDAO = fabrica.getHabitacionDAO();
        this.reservaDAO = fabrica.getReservaDAO();
    }

    // ---------------------------------------------------------------- Clientes

    public Cliente anadirCliente(String nombre, String apellidos, String email, String telefono)
            throws ElementoDuplicadoException {
        Cliente cliente = new Cliente(nombre, apellidos, email, telefono);
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            comprobarEmailDisponible(cliente.getEmail(), null);
            clienteDAO.insertar(cliente);
            tx.confirmar();
        }
        return cliente;
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listar();
    }

    public Cliente buscarCliente(int idCliente) throws ElementoNoEncontradoException {
        return clienteDAO.buscar(idCliente)
                .orElseThrow(() -> new ElementoNoEncontradoException("No existe un cliente con id " + idCliente));
    }

    /** Busca por coincidencia parcial en nombre o apellidos, sin distinguir mayúsculas. */
    public List<Cliente> buscarClientes(String texto) {
        return clienteDAO.buscarPorTexto(texto == null ? "" : texto.trim());
    }

    public Cliente modificarCliente(int idCliente, String nombre, String apellidos, String email, String telefono)
            throws ElementoNoEncontradoException, ElementoDuplicadoException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            Cliente cliente = buscarCliente(idCliente);
            cliente.actualizarDatos(nombre, apellidos, email, telefono); // valida todos los datos
            comprobarEmailDisponible(cliente.getEmail(), idCliente);
            clienteDAO.modificar(cliente);
            tx.confirmar();
            return cliente;
        }
    }

    /**
     * Comprueba que ningún otro cliente use ese email. Si se está modificando un cliente,
     * se pasa su id para que su propio email no cuente como repetido.
     */
    public void comprobarEmailDisponible(String email, Integer idClienteExcluido) throws ElementoDuplicadoException {
        String emailNuevo = email == null ? "" : email.trim();
        if (clienteDAO.existeEmail(emailNuevo, idClienteExcluido)) {
            throw new ElementoDuplicadoException("Ya existe un cliente con el email " + emailNuevo);
        }
    }

    public void eliminarCliente(int idCliente) throws ElementoNoEncontradoException, OperacionInvalidaException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            buscarCliente(idCliente);
            for (Reserva r : reservasDeCliente(idCliente)) {
                if (r.getEstado() != EstadoReserva.CANCELADA) {
                    throw new OperacionInvalidaException(
                            "No se puede eliminar un cliente con reservas activas (reserva #" + r.getIdReserva() + ")");
                }
            }
            clienteDAO.eliminar(idCliente); // sus reservas canceladas se borran con él (ON DELETE CASCADE)
            tx.confirmar();
        }
    }

    public List<Reserva> reservasDeCliente(int idCliente) throws ElementoNoEncontradoException {
        buscarCliente(idCliente);
        return reservaDAO.filtrar(null, null, null, idCliente, null);
    }

    // ----------------------------------------------------------------- Hoteles

    public Hotel anadirHotel(String nombre, String ciudad, String direccion) {
        Hotel hotel = new Hotel(nombre, ciudad, direccion);
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            hotelDAO.insertar(hotel);
            tx.confirmar();
        }
        return hotel;
    }

    public List<Hotel> listarHoteles() {
        return hotelDAO.listar();
    }

    public Hotel buscarHotel(int idHotel) throws ElementoNoEncontradoException {
        return hotelDAO.buscar(idHotel)
                .orElseThrow(() -> new ElementoNoEncontradoException("No existe un hotel con id " + idHotel));
    }

    public List<Hotel> buscarHotelesPorCiudad(String ciudad) {
        return hotelDAO.buscarPorCiudad(ciudad == null ? "" : ciudad.trim());
    }

    // ------------------------------------------------------------ Habitaciones

    public Habitacion anadirHabitacion(int idHotel, String numero, String categoria, int capacidad,
                                       double precioNoche)
            throws ElementoNoEncontradoException, ElementoDuplicadoException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            buscarHotel(idHotel);
            Habitacion habitacion = new Habitacion(numero, categoria, capacidad, precioNoche);
            comprobarNumeroHabitacionDisponible(idHotel, habitacion.getNumero());
            habitacionDAO.insertar(idHotel, habitacion);
            tx.confirmar();
            return habitacion;
        }
    }

    /** Comprueba que el hotel no tenga ya una habitación con ese número. */
    public void comprobarNumeroHabitacionDisponible(int idHotel, String numero)
            throws ElementoNoEncontradoException, ElementoDuplicadoException {
        Hotel hotel = buscarHotel(idHotel);
        String buscado = numero == null ? "" : numero.trim();
        if (habitacionDAO.existeNumero(idHotel, buscado)) {
            throw new ElementoDuplicadoException(
                    "El hotel " + hotel.getNombre() + " ya tiene la habitación " + buscado);
        }
    }

    public List<Habitacion> listarHabitaciones(int idHotel) throws ElementoNoEncontradoException {
        return buscarHotel(idHotel).listarHabitaciones();
    }

    public List<Habitacion> buscarHabitacionesPorCategoria(int idHotel, String categoria)
            throws ElementoNoEncontradoException {
        return buscarHotel(idHotel).buscarPorCategoria(categoria);
    }

    public List<Habitacion> buscarHabitacionesPorCapacidad(int idHotel, int personas)
            throws ElementoNoEncontradoException {
        List<Habitacion> resultado = new ArrayList<>();
        for (Habitacion h : buscarHotel(idHotel).listarHabitaciones()) {
            if (h.validarCapacidad(personas)) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public List<Habitacion> habitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        comprobarRango(entrada, salida);
        Hotel hotel = buscarHotel(idHotel);
        List<Reserva> solapadas = reservaDAO.listarSolapadas(idHotel, entrada, salida, null);
        List<Habitacion> resultado = new ArrayList<>();
        for (Habitacion h : hotel.listarHabitaciones()) {
            if (h.estaDisponible(entrada, salida, solapadas)) {
                resultado.add(h);
            }
        }
        return resultado;
    }

    public long contarHabitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        comprobarRango(entrada, salida);
        Hotel hotel = buscarHotel(idHotel);
        return hotel.contarHabitacionesDisponibles(entrada, salida,
                reservaDAO.listarSolapadas(idHotel, entrada, salida, null));
    }

    // ---------------------------------------------------------------- Reservas

    public Reserva crearReserva(int idCliente, int idHotel, LocalDate entrada, LocalDate salida,
                                int personas, List<Integer> idsHabitaciones)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            Cliente cliente = buscarCliente(idCliente);
            Hotel hotel = buscarHotel(idHotel);
            List<Habitacion> elegidas = habitacionesParaReserva(hotel, entrada, salida, personas, idsHabitaciones);

            Reserva reserva = new Reserva(cliente, hotel, entrada, salida, personas, EstadoReserva.PENDIENTE);
            for (Habitacion h : elegidas) {
                reserva.anadirDetalle(new DetalleReserva(h, h.getPrecioNoche()));
            }
            reservaDAO.insertar(reserva); // la reserva y todos sus detalles, en la misma transacción
            tx.confirmar();
            return reserva;
        }
    }

    public Reserva buscarReserva(int idReserva) throws ElementoNoEncontradoException {
        return reservaDAO.buscar(idReserva)
                .orElseThrow(() -> new ElementoNoEncontradoException("No existe una reserva con id " + idReserva));
    }

    public List<Reserva> listarReservas() {
        return reservaDAO.listar();
    }

    /**
     * Consulta de reservas con filtros opcionales: un filtro a null no se aplica.
     * Las fechas filtran por la fecha exacta de entrada o de salida.
     */
    public List<Reserva> consultarReservas(EstadoReserva estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                                           Integer idCliente, Integer idHotel) {
        return reservaDAO.filtrar(estado, fechaEntrada, fechaSalida, idCliente, idHotel);
    }

    public Reserva confirmarReserva(int idReserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            Reserva reserva = buscarReserva(idReserva);
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new OperacionInvalidaException("No se puede confirmar una reserva cancelada");
            }
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                throw new OperacionInvalidaException("La reserva ya está confirmada");
            }
            if (reserva.getDetalles().isEmpty()) {
                throw new OperacionInvalidaException("La reserva no tiene habitaciones");
            }
            reservaDAO.cambiarEstado(idReserva, EstadoReserva.CONFIRMADA);
            reserva.confirmar();
            tx.confirmar();
            return reserva;
        }
    }

    public Reserva cancelarReserva(int idReserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            Reserva reserva = buscarReserva(idReserva);
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new OperacionInvalidaException("La reserva ya está cancelada");
            }
            reservaDAO.cambiarEstado(idReserva, EstadoReserva.CANCELADA);
            reserva.cancelar();
            tx.confirmar();
            return reserva;
        }
    }

    public Reserva modificarReserva(int idReserva, LocalDate entrada, LocalDate salida, int personas)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            Reserva reserva = comprobarModificacion(idReserva, entrada, salida, personas);
            reserva.modificar(entrada, salida, personas);
            reservaDAO.actualizar(reserva);
            tx.confirmar();
            return reserva;
        }
    }

    /** Comprueba que una reserva exista y no esté cancelada (condición para poder modificarla). */
    public Reserva comprobarReservaModificable(int idReserva)
            throws ElementoNoEncontradoException, OperacionInvalidaException {
        Reserva reserva = buscarReserva(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new OperacionInvalidaException("No se puede modificar una reserva cancelada");
        }
        return reserva;
    }

    /** Comprueba, sin cambiar nada, que la reserva admite las nuevas fechas y personas. */
    public Reserva comprobarModificacion(int idReserva, LocalDate entrada, LocalDate salida, int personas)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        Reserva reserva = comprobarReservaModificable(idReserva);
        comprobarRango(entrada, salida);
        comprobarPersonas(personas);

        List<Reserva> otras = reservaDAO.listarSolapadas(reserva.getHotel().getIdHotel(), entrada, salida, idReserva);
        List<Habitacion> habitaciones = new ArrayList<>();
        for (DetalleReserva d : reserva.getDetalles()) {
            habitaciones.add(d.getHabitacion());
        }
        comprobarDisponibilidadYCapacidad(habitaciones, entrada, salida, personas, otras);
        return reserva;
    }

    public void comprobarHabitacionesParaReserva(int idHotel, LocalDate entrada, LocalDate salida,
                                                 int personas, List<Integer> idsHabitaciones)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        habitacionesParaReserva(buscarHotel(idHotel), entrada, salida, personas, idsHabitaciones);
    }

    public double calcularImporte(int idReserva) throws ElementoNoEncontradoException {
        return buscarReserva(idReserva).calcularImporte();
    }

    // ------------------------------------------------------------ Datos de demo

    /**
     * Carga unos clientes, hoteles y reservas de ejemplo, todo en una sola transacción.
     * Solo lo hace si la base de datos está vacía, para no mezclar los datos de ejemplo con datos reales
     * ni repetirlos cada vez que se arranca el programa.
     *
     * @return true si se han cargado; false si no se ha hecho nada porque ya había datos
     */
    public boolean cargarDatosPrueba() {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            if (!clienteDAO.listar().isEmpty() || !hotelDAO.listar().isEmpty()) {
                return false;
            }
            Cliente laura = anadirCliente("Laura", "Martín Pérez", "laura.martin@email.com", "600111222");
            Cliente carlos = anadirCliente("Carlos", "Ruiz Soler", "carlos.ruiz@email.com", "600333444");
            anadirCliente("Marta", "Soler Vidal", "marta.soler@email.com", "600555666");

            Hotel h1 = anadirHotel("Hotel Mar Azul", "Mataró", "Passeig Marítim 12");
            Hotel h2 = anadirHotel("Gran Hotel Centro", "Barcelona", "Gran Via 100");
            Habitacion h101 = anadirHabitacion(h1.getIdHotel(), "101", "Estándar", 2, 80);
            anadirHabitacion(h1.getIdHotel(), "102", "Estándar", 2, 80);
            Habitacion h201 = anadirHabitacion(h1.getIdHotel(), "201", "Familiar", 4, 130);
            anadirHabitacion(h1.getIdHotel(), "301", "Suite", 3, 210);
            anadirHabitacion(h2.getIdHotel(), "10", "Estándar", 2, 110);
            anadirHabitacion(h2.getIdHotel(), "20", "Suite", 4, 290);

            Reserva r1 = crearReserva(laura.getIdCliente(), h1.getIdHotel(), LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 13), 2, List.of(h101.getIdHabitacion()));
            confirmarReserva(r1.getIdReserva());
            crearReserva(carlos.getIdCliente(), h1.getIdHotel(), LocalDate.of(2026, 11, 12),
                    LocalDate.of(2026, 11, 15), 4, List.of(h201.getIdHabitacion()));

            tx.confirmar();
            return true;
        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error al cargar los datos de prueba: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------- Privados

    public void comprobarRango(LocalDate entrada, LocalDate salida) throws OperacionInvalidaException {
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw new OperacionInvalidaException("La fecha de salida debe ser posterior a la de entrada");
        }
    }

    private List<Habitacion> habitacionesParaReserva(Hotel hotel, LocalDate entrada, LocalDate salida,
                                                     int personas, List<Integer> idsHabitaciones)
            throws ElementoNoEncontradoException, OperacionInvalidaException, HabitacionNoDisponibleException {
        comprobarRango(entrada, salida);
        comprobarPersonas(personas);
        if (idsHabitaciones == null || idsHabitaciones.isEmpty()) {
            throw new OperacionInvalidaException("Hay que seleccionar al menos una habitación");
        }
        List<Habitacion> elegidas = seleccionarHabitaciones(hotel, new LinkedHashSet<>(idsHabitaciones));
        List<Reserva> solapadas = reservaDAO.listarSolapadas(hotel.getIdHotel(), entrada, salida, null);
        comprobarDisponibilidadYCapacidad(elegidas, entrada, salida, personas, solapadas);
        return elegidas;
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
