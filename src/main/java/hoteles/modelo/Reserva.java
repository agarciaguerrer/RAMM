package hoteles.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva {
    private int idReserva, numeroPersonas;
    private Cliente cliente;
    private Hotel hotel;
    private LocalDate fechaEntrada, fechaSalida;
    private EstadoReserva estado;
    private final List<DetalleReserva> detalles = new ArrayList<>();

    public Reserva(int idReserva, Cliente cliente, Hotel hotel, LocalDate fechaEntrada,
                   LocalDate fechaSalida, int numeroPersonas, EstadoReserva estado) {
        setIdReserva(idReserva); setCliente(cliente); setHotel(hotel);
        setFechas(fechaEntrada, fechaSalida); setNumeroPersonas(numeroPersonas); setEstado(estado);
    }
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        idReserva = v;
    }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente v) {
        if (v == null) throw new IllegalArgumentException("Debe seleccionarse un cliente.");
        cliente = v;
    }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel v) {
        if (v == null) throw new IllegalArgumentException("Debe seleccionarse un hotel.");
        hotel = v;
    }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null || !salida.isAfter(entrada))
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
        fechaEntrada = entrada; fechaSalida = salida;
    }
    public int getNumeroPersonas() { return numeroPersonas; }
    public void setNumeroPersonas(int v) {
        if (v <= 0) throw new IllegalArgumentException("El número de personas debe ser positivo.");
        numeroPersonas = v;
    }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva v) {
        if (v == null) throw new IllegalArgumentException("El estado es obligatorio.");
        estado = v;
    }
    public List<DetalleReserva> getDetalles() { return Collections.unmodifiableList(detalles); }
    public void anadirDetalle(DetalleReserva d) {
        if (d == null) throw new IllegalArgumentException("El detalle no puede ser nulo.");
        if (!d.getHabitacion().validarCapacidad(numeroPersonas))
            throw new IllegalArgumentException("La habitación no tiene capacidad suficiente.");
        if (detalles.stream().anyMatch(x ->
                x.getHabitacion().getIdHabitacion() == d.getHabitacion().getIdHabitacion()))
            throw new IllegalArgumentException("La habitación ya está incluida en esta reserva.");
        detalles.add(d);
    }
    public long calcularNoches() { return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida); }
    public double calcularImporte() {
        int noches = Math.toIntExact(calcularNoches());
        return detalles.stream().mapToDouble(d -> d.calcularSubtotal(noches)).sum();
    }
    public boolean validarFechas() {
        return fechaEntrada != null && fechaSalida != null && salidaPosterior();
    }
    private boolean salidaPosterior() { return fechaSalida.isAfter(fechaEntrada); }
    public void modificar(LocalDate entrada, LocalDate salida, int personas) {
        setFechas(entrada, salida); setNumeroPersonas(personas);
        for (DetalleReserva d : detalles) {
            if (!d.getHabitacion().validarCapacidad(personas))
                throw new IllegalArgumentException("La habitación no tiene capacidad suficiente.");
        }
    }
    public void confirmar() { estado = EstadoReserva.CONFIRMADA; }
    public void cancelar() { estado = EstadoReserva.CANCELADA; }
    @Override public String toString() {
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "Reserva " + idReserva + " | " + cliente.getNombre() + " " + cliente.getApellidos()
            + " | " + hotel.getNombre() + " | " + fechaEntrada.format(f) + " -> " + fechaSalida.format(f)
            + " (" + calcularNoches() + (calcularNoches() == 1 ? " noche" : " noches") + ")"
            + " | " + numeroPersonas + (numeroPersonas == 1 ? " persona" : " personas")
            + " | " + estado + " | " + detalles.size() + (detalles.size() == 1 ? " habitación" : " habitaciones")
            + " | Total: " + String.format("%.2f", calcularImporte()) + " €";
    }
}
