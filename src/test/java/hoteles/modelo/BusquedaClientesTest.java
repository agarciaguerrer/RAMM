package hoteles.modelo;

import hoteles.modelo.dao.DAOFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Pruebas de la búsqueda de clientes por nombre o apellidos contra MySQL. */
class BusquedaClientesTest {

    private Datos datos;

    @BeforeAll
    static void comprobarBaseDeDatos() {
        assumeTrue(EntornoBD.preparar(), "No hay un MySQL accesible: se omiten las pruebas de persistencia");
    }

    @BeforeEach
    void preparar() throws Exception {
        EntornoBD.limpiar();
        datos = new Datos(DAOFactory.obtener());
        datos.anadirCliente("Laura", "Martín Pérez", "laura@email.com", "600111222");
        datos.anadirCliente("Carlos", "Ruiz", "carlos@email.com", "600333444");
    }

    @Test
    void laBusquedaNoDistingueAcentos() {
        assertEquals(1, datos.buscarClientes("martin").size());
        assertEquals(1, datos.buscarClientes("perez").size());
    }

    @Test
    void laBusquedaNoDistingueMayusculas() {
        assertEquals(1, datos.buscarClientes("MARTÍN").size());
        assertEquals(1, datos.buscarClientes("carlos").size());
    }

    @Test
    void losComodinesDeSqlSeBuscanComoTextoNormal() {
        // Con LIKE sin escapar, "%" y "_" encontrarían a todos los clientes
        assertEquals(0, datos.buscarClientes("%").size());
        assertEquals(0, datos.buscarClientes("_").size());
    }
}
