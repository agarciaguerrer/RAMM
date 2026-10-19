package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.EstadoReserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Puente entre la Vista y el Modelo. La Vista solo conoce esta clase: recibe tipos básicos
 * (String, int, LocalDate...) y devuelve textos ya preparados para mostrar.
 * Las excepciones del modelo se propagan tal cual y la Vista las captura para informar al usuario.
 */
public class Controlador {

    private final Datos datos;

    public Controlador() {
        this.datos = new Datos();
    }

    public void cargarDatosPrueba() {
        datos.cargarDatosPrueba();
    }

    // ---------------------------------------------------------------- Clientes

    public String anadirCliente(String nombre, String apellidos, String email, String telefono) throws Exception {
        return datos.anadirCliente(nombre, apellidos, email, telefono).toString();
    }

    public List<String> listarClientes() {
        return aTexto(datos.listarClientes());
    }

    public List<String> buscarClientes(String texto) {
        return aTexto(datos.buscarClientes(texto));
    }

    public String modificarCliente(int idCliente, String nombre, String apellidos, String email, String telefono)
            throws Exception {
        return datos.modificarCliente(idCliente, nombre, apellidos, email, telefono).toString();
    }

    public void eliminarCliente(int idCliente) throws Exception {
        datos.eliminarCliente(idCliente);
    }

    public List<String> reservasDeCliente(int idCliente) throws Exception {
        return aTexto(datos.reservasDeCliente(idCliente));
    }

    // ----------------------------------------------------------------- Hoteles

    public String anadirHotel(String nombre, String ciudad, String direccion) throws Exception {
        return datos.anadirHotel(nombre, ciudad, direccion).toString();
    }

    public List<String> listarHoteles() {
        return aTexto(datos.listarHoteles());
    }

    public List<String> buscarHotelesPorCiudad(String ciudad) {
        return aTexto(datos.buscarHotelesPorCiudad(ciudad));
    }

    public long contarHabitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida) throws Exception {
        return datos.contarHabitacionesDisponibles(idHotel, entrada, salida);
    }

    // ------------------------------------------------------------ Habitaciones

    public String anadirHabitacion(int idHotel, String numero, String categoria, int capacidad, double precioNoche)
            throws Exception {
        return datos.anadirHabitacion(idHotel, numero, categoria, capacidad, precioNoche).toString();
    }

    public List<String> listarHabitaciones(int idHotel) throws Exception {
        return aTexto(datos.listarHabitaciones(idHotel));
    }

    public List<String> buscarHabitacionesPorCategoria(int idHotel, String categoria) throws Exception {
        return aTexto(datos.buscarHabitacionesPorCategoria(idHotel, categoria));
    }

    public List<String> buscarHabitacionesPorCapacidad(int idHotel, int personas) throws Exception {
        return aTexto(datos.buscarHabitacionesPorCapacidad(idHotel, personas));
    }

    public List<String> habitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida) throws Exception {
        return aTexto(datos.habitacionesDisponibles(idHotel, entrada, salida));
    }

    // ---------------------------------------------------------------- Reservas

    public String crearReserva(int idCliente, int idHotel, LocalDate entrada, LocalDate salida,
                               int personas, List<Integer> idsHabitaciones) throws Exception {
        return datos.crearReserva(idCliente, idHotel, entrada, salida, personas, idsHabitaciones).toString();
    }

    public String buscarReserva(int idReserva) throws Exception {
        return datos.buscarReserva(idReserva).toString();
    }

    public List<String> detallesReserva(int idReserva) throws Exception {
        return aTexto(datos.buscarReserva(idReserva).getDetalles());
    }

    public List<String> listarReservas() {
        return aTexto(datos.listarReservas());
    }

    /** Filtros opcionales: null (o texto vacío) significa "sin filtrar por ese campo". */
    public List<String> consultarReservas(String estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                                          Integer idCliente, Integer idHotel) {
        return aTexto(datos.consultarReservas(convertirEstado(estado), fechaEntrada, fechaSalida,
                idCliente, idHotel));
    }

    public String confirmarReserva(int idReserva) throws Exception {
        return datos.confirmarReserva(idReserva).toString();
    }

    public String cancelarReserva(int idReserva) throws Exception {
        return datos.cancelarReserva(idReserva).toString();
    }

    public String modificarReserva(int idReserva, LocalDate entrada, LocalDate salida, int personas)
            throws Exception {
        return datos.modificarReserva(idReserva, entrada, salida, personas).toString();
    }

    public double calcularImporte(int idReserva) throws Exception {
        return datos.calcularImporte(idReserva);
    }

    // ----------------------------------------------------------------- Privados

    private EstadoReserva convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoReserva.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado no válido. Usa PENDIENTE, CONFIRMADA o CANCELADA");
        }
    }

    private List<String> aTexto(List<?> elementos) {
        List<String> textos = new ArrayList<>();
        for (Object elemento : elementos) {
            textos.add(elemento.toString());
        }
        return textos;
    }
}
