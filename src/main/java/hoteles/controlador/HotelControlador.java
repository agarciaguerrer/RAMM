package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.Validaciones;

import java.time.LocalDate;
import java.util.List;

/** Controlador de hoteles: puente entre la Vista de hoteles y el Modelo. */
public class HotelControlador {

    private final Datos datos;

    public HotelControlador(Datos datos) {
        this.datos = datos;
    }

    /** Da de alta el hotel y devuelve su id, para poder añadirle habitaciones a continuación. */
    public int anadirHotel(String nombre, String ciudad, String direccion) throws Exception {
        return datos.anadirHotel(nombre, ciudad, direccion).getIdHotel();
    }

    public String mostrarHotel(int idHotel) throws Exception {
        return datos.buscarHotel(idHotel).toString();
    }

    public List<String> listarHoteles() {
        return Textos.aTexto(datos.listarHoteles());
    }

    public List<String> buscarHotelesPorCiudad(String ciudad) {
        return Textos.aTexto(datos.buscarHotelesPorCiudad(ciudad));
    }

    public long contarHabitacionesDisponibles(int idHotel, LocalDate entrada, LocalDate salida) throws Exception {
        return datos.contarHabitacionesDisponibles(idHotel, entrada, salida);
    }

    public boolean hayHoteles() {
        return !datos.listarHoteles().isEmpty();
    }

    // ------------------------------------------------------------ Validaciones

    public void validarNombreHotel(String valor) {
        Validaciones.nombreHotel(valor);
    }

    public void validarCiudad(String valor) {
        Validaciones.ciudad(valor);
    }

    public void validarDireccion(String valor) {
        Validaciones.direccion(valor);
    }

    public void validarHotel(int idHotel) throws Exception {
        datos.buscarHotel(idHotel);
    }
}
