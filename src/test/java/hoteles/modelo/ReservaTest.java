package hoteles.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Prueba 1: lógica de negocio de Reserva (calcularImporte, calcularNoches y validación de fechas). */
class ReservaTest {

    private Cliente cliente;
    private Hotel hotel;
    private Habitacion estandar;
    private Habitacion suite;

    @BeforeEach
    void preparar() {
        cliente = new Cliente(1, "Laura", "Martín", "laura@email.com", "600111222");
        hotel = new Hotel(1, "Hotel Mar", "Mataró", "Passeig 1");
        estandar = new Habitacion(1, "101", "Estándar", 2, 80);
        suite = new Habitacion(2, "301", "Suite", 3, 200);
    }

    private Reserva reserva(LocalDate entrada, LocalDate salida, int personas) {
        return new Reserva(1, cliente, hotel, entrada, salida, personas, EstadoReserva.PENDIENTE);
    }

    @Test
    void calcularImporteSumaTodasLasHabitacionesPorNoche() {
        Reserva reserva = reserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2);
        reserva.anadirDetalle(new DetalleReserva(1, estandar, 80));  // 80 x 3 = 240
        reserva.anadirDetalle(new DetalleReserva(2, suite, 200));    // 200 x 3 = 600

        assertEquals(3, reserva.calcularNoches());
        assertEquals(840.0, reserva.calcularImporte(), 0.001);
    }

    @Test
    void calcularImporteUsaElPrecioAplicadoYNoElActualDeLaHabitacion() {
        Reserva reserva = reserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12), 2);
        reserva.anadirDetalle(new DetalleReserva(1, estandar, 70)); // oferta: 70 en vez de 80

        assertEquals(140.0, reserva.calcularImporte(), 0.001);
    }

    @Test
    void calcularImporteSinHabitacionesEsCero() {
        Reserva reserva = reserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12), 1);

        assertEquals(0.0, reserva.calcularImporte(), 0.001);
    }

    @Test
    void noSePuedeCrearUnaReservaConSalidaAnteriorOIgualALaEntrada() {
        LocalDate dia = LocalDate.of(2026, 11, 10);

        assertThrows(IllegalArgumentException.class, () -> reserva(LocalDate.of(2026, 11, 13), dia, 2));
        assertThrows(IllegalArgumentException.class, () -> reserva(dia, dia, 2));
    }

    @Test
    void noSePuedeAnadirUnaHabitacionSinCapacidadSuficiente() {
        Reserva reserva = reserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 3);

        assertThrows(IllegalArgumentException.class,
                () -> reserva.anadirDetalle(new DetalleReserva(1, estandar, 80))); // capacidad 2 < 3 personas
    }
}
