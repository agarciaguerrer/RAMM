package hoteles.modelo;

public class DetalleReserva {
    private int idDetalle;
    private Habitacion habitacion;
    private double precioNocheAplicado;

    public DetalleReserva(int idDetalle, Habitacion habitacion, double precioNocheAplicado) {
        setIdDetalle(idDetalle); setHabitacion(habitacion); setPrecioNocheAplicado(precioNocheAplicado);
    }
    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        idDetalle = v;
    }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion v) {
        if (v == null) throw new IllegalArgumentException("Debe indicarse una habitación.");
        habitacion = v;
    }
    public double getPrecioNocheAplicado() { return precioNocheAplicado; }
    public void setPrecioNocheAplicado(double v) {
        if (!Double.isFinite(v) || v < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        precioNocheAplicado = v;
    }
    public double calcularSubtotal(int noches) {
        if (noches <= 0) throw new IllegalArgumentException("Las noches deben ser positivas.");
        return precioNocheAplicado * noches;
    }
    @Override public String toString() {
        return "Habitación " + habitacion.getNumero() + " (" + habitacion.getCategoria() + ") | "
               + String.format("%.2f", precioNocheAplicado) + " €/noche";
    }
}
