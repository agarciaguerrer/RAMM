package hoteles.modelo;

import java.time.LocalDate;
import java.util.List;

public class Habitacion {
    private int idHabitacion, capacidad;
    private String numero, categoria;
    private double precioNoche;

    public Habitacion(int idHabitacion, String numero, String categoria, int capacidad, double precioNoche) {
        setIdHabitacion(idHabitacion); setNumero(numero); setCategoria(categoria);
        setCapacidad(capacidad); setPrecioNoche(precioNoche);
    }
    public int getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        idHabitacion = v;
    }
    public String getNumero() { return numero; }
    public void setNumero(String v) { numero = Validaciones.numeroHabitacion(v); }
    public String getCategoria() { return categoria; }
    public void setCategoria(String v) { categoria = Validaciones.categoria(v); }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int v) { capacidad = Validaciones.capacidad(v); }
    public double getPrecioNoche() { return precioNoche; }
    public void setPrecioNoche(double v) { precioNoche = Validaciones.precio(v); }
    public boolean validarCapacidad(int personas) { return personas > 0 && personas <= capacidad; }

    public boolean estaDisponible(LocalDate entrada, LocalDate salida, List<Reserva> reservas) {
        if (entrada == null || salida == null || !salida.isAfter(entrada))
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
        if (reservas == null) return true;
        return reservas.stream().noneMatch(r -> r != null && r.getEstado() != EstadoReserva.CANCELADA
            && r.getFechaEntrada().isBefore(salida) && entrada.isBefore(r.getFechaSalida())
            && r.getDetalles().stream().anyMatch(d ->
                d.getHabitacion().getIdHabitacion() == idHabitacion));
    }
    @Override public String toString() {
        return "Habitación " + numero + " (id " + idHabitacion + ") | " + categoria + " | capacidad: "
               + capacidad + (capacidad == 1 ? " persona" : " personas")
               + " | " + String.format("%.2f", precioNoche) + " €/noche";
    }
}
