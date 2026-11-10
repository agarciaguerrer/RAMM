package hoteles.modelo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de las excepciones propias y de las reglas de negocio de Datos, ahora sobre MySQL
 * (base de datos de pruebas hoteles_test, ver {@link EntornoBD}). Se omiten si no hay MySQL accesible.
 * Como cada consulta crea objetos nuevos a partir de la base de datos, los resultados se comprueban
 * con lo que devuelve el método o volviendo a leer de la base de datos.
 */
class DatosTest {

    private Datos datos;

    @BeforeAll
    static void comprobarBaseDeDatos() {
        assumeTrue(EntornoBD.preparar(), "No hay un MySQL accesible: se omiten las pruebas de persistencia");
    }

    @BeforeEach
    void preparar() throws Exception {
        EntornoBD.limpiar();
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

        Reserva modificada = datos.modificarReserva(r.getIdReserva(),
                LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 14), 1);

        assertEquals(LocalDate.of(2026, 11, 14), modificada.getFechaSalida());
        assertEquals(1, modificada.getNumeroPersonas());
        // Y el cambio está guardado en la base de datos
        Reserva releida = datos.buscarReserva(r.getIdReserva());
        assertEquals(LocalDate.of(2026, 11, 11), releida.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 11, 14), releida.getFechaSalida());
        assertEquals(1, releida.getNumeroPersonas());
    }

    @Test
    void cancelarDosVecesLanzaOperacionInvalida() throws Exception {
        Reserva r = datos.crearReserva(1, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2, List.of(1));
        datos.cancelarReserva(r.getIdReserva());

        assertEquals(EstadoReserva.CANCELADA, datos.buscarReserva(r.getIdReserva()).getEstado());
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
    void modificarClienteNoPermiteElEmailDeOtroPeroSiElPropio() throws Exception {
        datos.anadirCliente("Carlos", "Ruiz", "carlos@email.com", "611");

        assertThrows(ElementoDuplicadoException.class,
                () -> datos.modificarCliente(2, "Carlos", "Ruiz", "laura@email.com", "611"));
        // Conservar su propio email no es un duplicado
        datos.modificarCliente(2, "Carlos", "Ruiz Soler", "carlos@email.com", "622");
        assertEquals("Ruiz Soler", datos.buscarCliente(2).getApellidos());
    }

    @Test
    void validacionesRechazanDatosIncorrectos() {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.email("sin-arroba"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.nombre("   "));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.precio(0));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.capacidad(0));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.personas(-1));
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
