package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.EstadoReserva;
import hoteles.modelo.Validaciones;

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

    /** Da de alta el hotel y devuelve su id, para poder añadirle habitaciones a continuación. */
    public int anadirHotel(String nombre, String ciudad, String direccion) throws Exception {
        return datos.anadirHotel(nombre, ciudad, direccion).getIdHotel();
    }

    public String mostrarHotel(int idHotel) throws Exception {
        return datos.buscarHotel(idHotel).toString();
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

    // ------------------------------------------------------------ Validaciones
    // La Vista las llama dato a dato para avisar del error en cuanto se escribe un valor incorrecto.
    // Cada una lanza una excepción con el mensaje a mostrar si el valor no es válido.

    public void validarNombre(String valor) {
        Validaciones.nombre(valor);
    }

    public void validarApellidos(String valor) {
        Validaciones.apellidos(valor);
    }

    /** Formato correcto y no repetido. Al modificar un cliente se pasa su id; al crearlo, null. */
    public void validarEmail(String valor, Integer idClienteExcluido) throws Exception {
        Validaciones.email(valor);
        datos.comprobarEmailDisponible(valor, idClienteExcluido);
    }

    public void validarTelefono(String valor) {
        Validaciones.telefono(valor);
    }

    public void validarNombreHotel(String valor) {
        Validaciones.nombreHotel(valor);
    }

    public void validarCiudad(String valor) {
        Validaciones.ciudad(valor);
    }

    public void validarDireccion(String valor) {
        Validaciones.direccion(valor);
    }

    public void validarNumeroHabitacion(int idHotel, String valor) throws Exception {
        Validaciones.numeroHabitacion(valor);
        datos.comprobarNumeroHabitacionDisponible(idHotel, valor);
    }

    public void validarCategoria(String valor) {
        Validaciones.categoria(valor);
    }

    public void validarCapacidad(int valor) {
        Validaciones.capacidad(valor);
    }

    public void validarPrecio(double valor) {
        Validaciones.precio(valor);
    }

    public void validarPersonas(int valor) {
        Validaciones.personas(valor);
    }

    public void validarCliente(int idCliente) throws Exception {
        datos.buscarCliente(idCliente);
    }

    public void validarHotel(int idHotel) throws Exception {
        datos.buscarHotel(idHotel);
    }

    public void validarReserva(int idReserva) throws Exception {
        datos.buscarReserva(idReserva);
    }

    public void validarReservaModificable(int idReserva) throws Exception {
        datos.comprobarReservaModificable(idReserva);
    }

    /** Vale solo si el texto es uno de los estados existentes. */
    public void validarEstado(String valor) {
        convertirEstado(valor);
    }

    public void validarRango(LocalDate entrada, LocalDate salida) throws Exception {
        datos.comprobarRango(entrada, salida);
    }

    public void validarHabitacionesReserva(int idHotel, LocalDate entrada, LocalDate salida,
                                           int personas, List<Integer> idsHabitaciones) throws Exception {
        datos.comprobarHabitacionesParaReserva(idHotel, entrada, salida, personas, idsHabitaciones);
    }

    public boolean hayClientes() {
        return !datos.listarClientes().isEmpty();
    }

    public boolean hayHoteles() {
        return !datos.listarHoteles().isEmpty();
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
