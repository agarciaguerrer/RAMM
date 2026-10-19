package hoteles.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositorioTest {

    private Repositorio<Cliente> nuevoRepositorio() {
        return new Repositorio<>("un cliente", Cliente::getIdCliente);
    }

    @Test
    void anadirYBuscarPorId() throws Exception {
        Repositorio<Cliente> repo = nuevoRepositorio();
        Cliente c = new Cliente(7, "Ana", "Gil", "ana@email.com", "611");
        repo.anadir(c);

        assertEquals(c, repo.buscar(7));
        assertEquals(1, repo.tamano());
    }

    @Test
    void anadirIdRepetidoLanzaExcepcionPropia() throws Exception {
        Repositorio<Cliente> repo = nuevoRepositorio();
        repo.anadir(new Cliente(1, "Ana", "Gil", "ana@email.com", "611"));

        assertThrows(ElementoDuplicadoException.class,
                () -> repo.anadir(new Cliente(1, "Otro", "Cliente", "otro@email.com", "622")));
    }

    @Test
    void buscarYEliminarInexistenteLanzaExcepcionPropia() {
        Repositorio<Cliente> repo = nuevoRepositorio();

        assertThrows(ElementoNoEncontradoException.class, () -> repo.buscar(99));
        assertThrows(ElementoNoEncontradoException.class, () -> repo.eliminar(99));
    }
}
