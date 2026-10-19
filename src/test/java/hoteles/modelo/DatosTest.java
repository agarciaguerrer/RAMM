package hoteles.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de las excepciones propias y de las reglas de negocio de Datos (con la clase genérica Repositorio). */
class DatosTest {

    private Datos datos;

    @BeforeEach
    void preparar() throws Exception {
        datos = new Datos();
        datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        datos.anadirHabitacion(1, "101", "Estándar", 2, 80);
    }

    @Test
    void crearReservaSolapadaLanzaHabitacionNoDisponible() throws Exception {
        datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));

        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.crearReserva(1, 1, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14), 2, List.of(1)));
    }

    @Test
    void crearReservaConDemasiadasPersonasLanzaHabitacionNoDisponible() {
        assertThrows(HabitacionNoDisponibleException.class, () ->
                datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 3, List.of(1)));
    }

    @Test
    void modificarReservaNoChocaConsigoMisma() throws Exception {
        Reserva r = datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));

        datos.modificarReserva(r.getIdReserva(), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 14), 1);

        assertEquals(LocalDate.of(2026, 11, 14), r.getFechaSalida());
        assertEquals(1, r.getNumeroPersonas());
    }

    @Test
    void cancelarDosVecesLanzaOperacionInvalida() throws Exception {
        Reserva r = datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));
        datos.cancelarReserva(r.getIdReserva());

        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertThrows(OperacionInvalidaException.class, () -> datos.cancelarReserva(r.getIdReserva()));
    }

    @Test
    void unaReservaCanceladaLiberaLaHabitacion() throws Exception {
        Reserva r = datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));
        datos.cancelarReserva(r.getIdReserva());

        assertEquals(1, datos.contarHabitacionesDisponibles(1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13)));
    }

    @Test
    void noSePuedeEliminarUnClienteConReservasActivas() throws Exception {
        datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));

        assertThrows(OperacionInvalidaException.class, () -> datos.eliminarCliente(1));
    }

    @Test
    void emailDuplicadoYElementosInexistentesLanzanExcepcionesPropias() {
        assertThrows(ElementoDuplicadoException.class,
                () -> datos.anadirCliente("Otra", "Persona", "LAURA@email.com", "611"));
        assertThrows(ElementoNoEncontradoException.class, () -> datos.buscarCliente(99));
        assertThrows(ElementoNoEncontradoException.class, () -> datos.buscarReserva(99));
        assertThrows(ElementoDuplicadoException.class, () -> datos.anadirHabitacion(1, "101", "Suite", 3, 150));
    }
}
