package hoteles.modelo;

import hoteles.modelo.dao.DAOFactory;
import hoteles.modelo.dao.ReservaDAO;
import hoteles.modelo.dao.Transaccion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de la persistencia en MySQL: que los datos se guardan de verdad, que los ids los genera la base de
 * datos, que las transacciones deshacen lo que no se confirma, que los procedimientos almacenados protegen la
 * coherencia de los datos y que no hay inyección de SQL. Usan la base de datos de pruebas hoteles_test.
 */
class PersistenciaTest {

    private static final LocalDate D10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate D13 = LocalDate.of(2026, 11, 13);

    private DAOFactory fabrica;
    private Datos datos;

    @BeforeAll
    static void comprobarBaseDeDatos() {
        assumeTrue(EntornoBD.preparar(), "No hay un MySQL accesible: se omiten las pruebas de persistencia");
    }

    @BeforeEach
    void preparar() throws Exception {
        EntornoBD.limpiar();
        fabrica = DAOFactory.obtener();
        datos = new Datos(fabrica);
    }

    @Test
    void losDatosSobrevivenAunaNuevaInstanciaDeDatos() throws Exception {
        Cliente cliente = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion hab = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        Reserva reserva = datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D10, D13, 2,
                List.of(hab.getIdHabitacion()));
        datos.confirmarReserva(reserva.getIdReserva());

        Datos otra = new Datos(DAOFactory.obtener()); // como si se hubiera cerrado y abierto el programa
        Reserva leida = otra.buscarReserva(reserva.getIdReserva());

        assertEquals("Laura", leida.getCliente().getNombre());
        assertEquals("Hotel Mar", leida.getHotel().getNombre());
        assertEquals(D10, leida.getFechaEntrada());
        assertEquals(D13, leida.getFechaSalida());
        assertEquals(EstadoReserva.CONFIRMADA, leida.getEstado());
        assertEquals(1, leida.getDetalles().size());
        assertEquals(240.0, leida.calcularImporte(), 0.001); // 80 € x 3 noches
        assertEquals(1, otra.listarHoteles().get(0).getHabitaciones().size());
    }

    @Test
    void losIdsLosGeneraLaBaseDeDatosEnOrden() throws Exception {
        Cliente a = datos.anadirCliente("Ana", "Gil", "ana@email.com", "611");
        Cliente b = datos.anadirCliente("Beto", "Gil", "beto@email.com", "622");
        Hotel h = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");

        assertEquals(1, a.getIdCliente());
        assertEquals(2, b.getIdCliente());
        assertEquals(1, h.getIdHotel());
        assertEquals(List.of(1, 2), datos.listarClientes().stream().map(Cliente::getIdCliente).toList());
    }

    @Test
    void unTextoMalintencionadoSeGuardaTalCualYNoAlteraLaBaseDeDatos() throws Exception {
        String ataque = "Robert'); DROP TABLE cliente; --";
        datos.anadirCliente(ataque, "Tables", "bobby@email.com", "600");
        datos.anadirCliente("Ana", "Gil", "ana@email.com", "611");

        assertEquals(2, datos.listarClientes().size());
        assertEquals(ataque, datos.buscarCliente(1).getNombre());
        // Una búsqueda con comillas no devuelve todos los clientes, como pasaría con SQL concatenado
        assertTrue(datos.buscarClientes("' OR '1'='1").isEmpty());
        assertTrue(datos.buscarHotelesPorCiudad("x' OR '1'='1").isEmpty());
    }

    @Test
    void siUnaOperacionNoSeConfirmaSeDeshaceEntera() throws Exception {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
            datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
            // sin tx.confirmar(): al cerrar se hace ROLLBACK
        }

        assertTrue(datos.listarClientes().isEmpty());
        assertTrue(datos.listarHoteles().isEmpty());
    }

    @Test
    void siSeConfirmaLaTransaccionExternaSeGuardaTodo() throws Exception {
        try (Transaccion tx = fabrica.iniciarTransaccion()) {
            datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222"); // transacción anidada
            datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
            tx.confirmar();
        }

        assertEquals(1, datos.listarClientes().size());
        assertEquals(1, datos.listarHoteles().size());
    }

    @Test
    void unaReservaFallidaEnMitadDeLaTransaccionNoDejaFilasHuerfanas() throws Exception {
        Cliente cliente = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h101 = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        Habitacion h102 = datos.anadirHabitacion(hotel.getIdHotel(), "102", "Estándar", 2, 80);
        // La 102 ya está ocupada del 10 al 13
        datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D10, D13, 2, List.of(h102.getIdHabitacion()));

        // Se salta las comprobaciones del modelo y va directo al DAO: es el procedimiento almacenado el que protege
        Reserva nueva = new Reserva(cliente, hotel, D10, D13, 2, EstadoReserva.PENDIENTE);
        nueva.anadirDetalle(new DetalleReserva(h101, 80));  // esta se insertaría bien...
        nueva.anadirDetalle(new DetalleReserva(h102, 80));  // ...pero esta está ocupada
        ReservaDAO dao = fabrica.getReservaDAO();

        assertThrows(HabitacionNoDisponibleException.class, () -> {
            try (Transaccion tx = fabrica.iniciarTransaccion()) {
                dao.insertar(nueva);
                tx.confirmar();
            }
        });

        assertEquals(1, datos.listarReservas().size());       // solo la reserva original
        assertEquals(1, EntornoBD.contar("detalle_reserva")); // y ningún detalle suelto de la fallida
    }

    @Test
    void elProcedimientoRechazaCambiarFechasQueChocanConOtraReserva() throws Exception {
        Cliente cliente = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D10, D13, 2, List.of(h.getIdHabitacion()));
        Reserva segunda = datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D13,
                LocalDate.of(2026, 11, 15), 2, List.of(h.getIdHabitacion()));

        // Se salta el modelo: solapa con la primera reserva
        segunda.setFechas(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 15));

        assertThrows(HabitacionNoDisponibleException.class, () -> fabrica.getReservaDAO().actualizar(segunda));
        assertEquals(D13, datos.buscarReserva(segunda.getIdReserva()).getFechaEntrada()); // sin cambios
    }

    @Test
    void salirElDiaQueEntraOtroClienteNoEsUnChoque() throws Exception {
        Cliente cliente = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D10, D13, 2, List.of(h.getIdHabitacion()));

        assertEquals(1, datos.contarHabitacionesDisponibles(hotel.getIdHotel(), D13, LocalDate.of(2026, 11, 15)));
        assertEquals(0, datos.contarHabitacionesDisponibles(hotel.getIdHotel(), LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 14)));
        datos.crearReserva(cliente.getIdCliente(), hotel.getIdHotel(), D13, LocalDate.of(2026, 11, 15), 2,
                List.of(h.getIdHabitacion())); // no lanza excepción
    }

    @Test
    void eliminarUnClienteBorraSusReservasCanceladasPeroNoLasDeOtros() throws Exception {
        Cliente laura = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Cliente carlos = datos.anadirCliente("Carlos", "Ruiz", "carlos@email.com", "600333444");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h101 = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        Habitacion h102 = datos.anadirHabitacion(hotel.getIdHotel(), "102", "Estándar", 2, 80);
        Reserva deLaura = datos.crearReserva(laura.getIdCliente(), hotel.getIdHotel(), D10, D13, 2,
                List.of(h101.getIdHabitacion()));
        datos.crearReserva(carlos.getIdCliente(), hotel.getIdHotel(), D10, D13, 2, List.of(h102.getIdHabitacion()));
        datos.cancelarReserva(deLaura.getIdReserva());

        datos.eliminarCliente(laura.getIdCliente());

        assertThrows(ElementoNoEncontradoException.class, () -> datos.buscarCliente(laura.getIdCliente()));
        assertEquals(1, datos.listarReservas().size());
        assertEquals(1, EntornoBD.contar("detalle_reserva"));
    }

    @Test
    void consultarReservasAplicaLosFiltros() throws Exception {
        Cliente laura = datos.anadirCliente("Laura", "Martín", "laura@email.com", "600111222");
        Cliente carlos = datos.anadirCliente("Carlos", "Ruiz", "carlos@email.com", "600333444");
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h101 = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80);
        Habitacion h102 = datos.anadirHabitacion(hotel.getIdHotel(), "102", "Estándar", 2, 80);
        Reserva r1 = datos.crearReserva(laura.getIdCliente(), hotel.getIdHotel(), D10, D13, 2,
                List.of(h101.getIdHabitacion()));
        datos.crearReserva(carlos.getIdCliente(), hotel.getIdHotel(), D10, D13, 2, List.of(h102.getIdHabitacion()));
        datos.confirmarReserva(r1.getIdReserva());

        assertEquals(2, datos.consultarReservas(null, null, null, null, null).size());
        assertEquals(1, datos.consultarReservas(EstadoReserva.CONFIRMADA, null, null, null, null).size());
        assertEquals(1, datos.consultarReservas(null, D10, D13, carlos.getIdCliente(), null).size());
        assertTrue(datos.consultarReservas(null, LocalDate.of(2026, 12, 1), null, null, null).isEmpty());
        assertEquals(1, datos.reservasDeCliente(laura.getIdCliente()).size());
    }

    @Test
    void losDatosDeEjemploSoloSeCarganEnUnaBaseDeDatosVacia() throws Exception {
        assertTrue(datos.cargarDatosPrueba());
        assertEquals(3, datos.listarClientes().size());
        assertEquals(2, datos.listarHoteles().size());
        assertEquals(2, datos.listarReservas().size());

        assertFalse(datos.cargarDatosPrueba()); // segunda vez: no duplica nada
        assertEquals(3, datos.listarClientes().size());
        assertEquals(2, datos.listarReservas().size());
    }

    @Test
    void losTextosDemasiadoLargosSeRechazanConUnMensajeClaro() {
        assertThrows(IllegalArgumentException.class,
                () -> datos.anadirCliente("x".repeat(Validaciones.MAX_NOMBRE + 1), "Gil", "ana@email.com", "611"));
        assertThrows(IllegalArgumentException.class,
                () -> datos.anadirHotel("Hotel", "Mataró", "d".repeat(Validaciones.MAX_DIRECCION + 1)));
    }

    @Test
    void elPrecioSeGuardaConDosDecimales() throws Exception {
        Hotel hotel = datos.anadirHotel("Hotel Mar", "Mataró", "Passeig 1");
        Habitacion h = datos.anadirHabitacion(hotel.getIdHotel(), "101", "Estándar", 2, 80.504);

        assertEquals(80.50, h.getPrecioNoche(), 0.0001);
        assertEquals(80.50, datos.buscarHotel(hotel.getIdHotel()).getHabitaciones().get(0).getPrecioNoche(), 0.0001);
    }
}
