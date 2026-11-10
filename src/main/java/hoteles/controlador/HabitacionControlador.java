package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.Validaciones;

import java.time.LocalDate;
import java.util.List;

/** Controlador de habitaciones: puente entre la Vista de habitaciones y el Modelo. */
public class HabitacionControlador {

    private final Datos datos;

    public HabitacionControlador(Datos datos) {
        this.datos = datos;
    }

    public String anadirHabitacion(int idHotel, String numero, String categoria, int capacidad, double precioNoche)
            throws Exception {
        return datos.anadirHabitacion(idHotel, numero, categoria, capacidad, precioNoche).toString();
    }

    public List<String> listarHabitaciones(int idHotel) throws Exception {
        return Textos.aTexto(datos.listarHabitaciones(idHotel));
    }

    public List<String> buscarHabitacionesPorCategoria(int idHotel, String categoria) throws Exception {
        return Textos.aTexto(datos.buscarHabitacionesPorCategoria(idHotel, categoria));
    }

    public List<String> buscarHabitacionesPorCapacidad(int idHotel, int personas) throws Exception {
        return Textos.aTexto(datos.buscarHabitacionesPorCapacidad(idHotel, personas));
    }

    public List<String> habitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida) throws Exception {
        return Textos.aTexto(datos.habitacionesDisponibles(idHotel, entrada, salida));
    }

    // ------------------------------------------------------------ Validaciones

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

    /** Fechas de entrada y salida coherentes (también las usan las reservas). */
    public void validarRango(LocalDate entrada, LocalDate salida) throws Exception {
        datos.comprobarRango(entrada, salida);
    }
}
