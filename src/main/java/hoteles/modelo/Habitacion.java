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
    public void setNumero(String v) { numero = obligatorio(v, "El número es obligatorio."); }
    public String getCategoria() { return categoria; }
    public void setCategoria(String v) { categoria = obligatorio(v, "La categoría es obligatoria."); }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int v) {
        if (v <= 0) throw new IllegalArgumentException("La capacidad debe ser positiva.");
        capacidad = v;
    }
    public double getPrecioNoche() { return precioNoche; }
    public void setPrecioNoche(double v) {
        if (!Double.isFinite(v) || v < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        precioNoche = v;
    }
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
    private static String obligatorio(String v, String m) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(m);
        return v.trim();
    }
    @Override public String toString() {
        return "Habitación " + numero + " (id " + idHabitacion + ") | " + categoria + " | capacidad: "
               + capacidad + (capacidad == 1 ? " persona" : " personas")
               + " | " + String.format("%.2f", precioNoche) + " €/noche";
    }
}
