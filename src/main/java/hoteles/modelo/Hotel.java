package hoteles.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hotel {
    private int idHotel;
    private String nombre, ciudad, direccion;
    private final List<Habitacion> habitaciones = new ArrayList<>();

    public Hotel(int idHotel, String nombre, String ciudad, String direccion) {
        setIdHotel(idHotel); setNombre(nombre); setCiudad(ciudad); setDireccion(direccion);
    }
    public int getIdHotel() { return idHotel; }
    public void setIdHotel(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador del hotel debe ser positivo.");
        idHotel = v;
    }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = obligatorio(v, "El nombre es obligatorio."); }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String v) { ciudad = obligatorio(v, "La ciudad es obligatoria."); }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { direccion = obligatorio(v, "La dirección es obligatoria."); }
    public List<Habitacion> getHabitaciones() { return Collections.unmodifiableList(habitaciones); }
    public void anadirHabitacion(Habitacion h) {
        if (h == null) throw new IllegalArgumentException("La habitación no puede ser nula.");
        if (habitaciones.stream().anyMatch(x -> x.getIdHabitacion() == h.getIdHabitacion()))
            throw new IllegalArgumentException("La habitación ya está añadida.");
        habitaciones.add(h);
    }
    public List<Habitacion> listarHabitaciones() { return getHabitaciones(); }
    public List<Habitacion> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return List.of();
        return habitaciones.stream().filter(h -> h.getCategoria().equalsIgnoreCase(categoria.trim())).toList();
    }
    public long contarHabitacionesDisponibles(LocalDate entrada, LocalDate salida, List<Reserva> reservas) {
        return habitaciones.stream().filter(h -> h.estaDisponible(entrada, salida, reservas)).count();
    }
    private static String obligatorio(String v, String m) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(m);
        return v.trim();
    }
    @Override public String toString() {
        return "Hotel{idHotel=" + idHotel + ", nombre='" + nombre + "', ciudad='" + ciudad +
               "', direccion='" + direccion + "', habitaciones=" + habitaciones.size() + "}";
    }
}
