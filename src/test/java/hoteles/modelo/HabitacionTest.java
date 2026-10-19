package hoteles.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba 2: lógica de negocio de Habitacion.estaDisponible (solapamiento de fechas) y validarCapacidad. */
class HabitacionTest {

    private Habitacion habitacion;
    private List<Reserva> reservas;
    private Reserva existente;

    @BeforeEach
    void preparar() {
        Cliente cliente = new Cliente(1, "Laura", "Martín", "laura@email.com", "600111222");
        Hotel hotel = new Hotel(1, "Hotel Mar", "Mataró", "Passeig 1");
        habitacion = new Habitacion(1, "101", "Estándar", 2, 80);

        // Reserva existente: del 10 al 13 de noviembre
        existente = new Reserva(1, cliente, hotel, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2,
                EstadoReserva.CONFIRMADA);
        existente.anadirDetalle(new DetalleReserva(1, habitacion, 80));
        reservas = new ArrayList<>(List.of(existente));
    }

    @Test
    void noEstaDisponibleSiLasFechasSeSolapan() {
        assertFalse(habitacion.estaDisponible(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 15), reservas));
        assertFalse(habitacion.estaDisponible(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 11), reservas));
        assertFalse(habitacion.estaDisponible(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12), reservas));
        assertFalse(habitacion.estaDisponible(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), reservas));
    }

    @Test
    void estaDisponibleSiEntraElMismoDiaQueSaleOtroCliente() {
        assertTrue(habitacion.estaDisponible(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15), reservas));
        assertTrue(habitacion.estaDisponible(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 10), reservas));
    }

    @Test
    void estaDisponibleSiLaReservaEstaCancelada() {
        existente.cancelar();

        assertTrue(habitacion.estaDisponible(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), reservas));
    }

    @Test
    void fechasIncorrectasLanzanExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> habitacion.estaDisponible(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 10), reservas));
    }

    @Test
    void validarCapacidadRespetaLosLimites() {
        assertTrue(habitacion.validarCapacidad(1));
        assertTrue(habitacion.validarCapacidad(2));
        assertFalse(habitacion.validarCapacidad(3));
        assertFalse(habitacion.validarCapacidad(0));
    }
}
