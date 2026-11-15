package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.EstadoReserva;

import java.time.LocalDate;
import java.util.List;

/** Controlador de reservas: puente entre las Vistas de reservas y el Modelo. */
public class ReservaControlador {

    private final Datos datos;

    public ReservaControlador(Datos datos) {
        this.datos = datos;
    }

    public String crearReserva(int idCliente, int idHotel, LocalDate entrada, LocalDate salida,
                               int personas, List<Integer> idsHabitaciones) throws Exception {
        return datos.crearReserva(idCliente, idHotel, entrada, salida, personas, idsHabitaciones).toString();
    }

    public String buscarReserva(int idReserva) throws Exception {
        return datos.buscarReserva(idReserva).toString();
    }

    public List<String> detallesReserva(int idReserva) throws Exception {
        return Textos.aTexto(datos.buscarReserva(idReserva).getDetalles());
    }

    public List<String> listarReservas() {
        return Textos.aTexto(datos.listarReservas());
    }

    /** Filtros opcionales: null (o texto vacío) significa "sin filtrar por ese campo". */
    public List<String> consultarReservas(String estado, LocalDate fechaEntrada, LocalDate fechaSalida,
                                          Integer idCliente, Integer idHotel) {
        return Textos.aTexto(datos.consultarReservas(convertirEstado(estado), fechaEntrada, fechaSalida,
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

    public void validarHabitacionesReserva(int idHotel, LocalDate entrada, LocalDate salida,
                                           int personas, List<Integer> idsHabitaciones) throws Exception {
        datos.comprobarHabitacionesParaReserva(idHotel, entrada, salida, personas, idsHabitaciones);
    }

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
}
