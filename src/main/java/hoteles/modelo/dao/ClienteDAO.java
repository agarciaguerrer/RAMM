package hoteles.modelo.dao;

import hoteles.modelo.Cliente;
import hoteles.modelo.ElementoDuplicadoException;
import hoteles.modelo.ElementoNoEncontradoException;
import hoteles.modelo.OperacionInvalidaException;

import java.util.List;
import java.util.Optional;

/** Acceso a los datos de los clientes. Solo habla de objetos del modelo: no deja ver nada del SQL ni del motor. */
public interface ClienteDAO {

    /** Guarda el cliente y le asigna el id generado por la base de datos. */
    void insertar(Cliente cliente) throws ElementoDuplicadoException;

    void modificar(Cliente cliente) throws ElementoNoEncontradoException, ElementoDuplicadoException;

    /** Elimina el cliente y, en cascada, sus reservas canceladas. Falla si tiene reservas activas. */
    void eliminar(int idCliente) throws ElementoNoEncontradoException, OperacionInvalidaException;

    Optional<Cliente> buscar(int idCliente);

    /** Todos los clientes ordenados por id. */
    List<Cliente> listar();

    /** Clientes cuyo nombre o apellidos contienen el texto, sin distinguir mayúsculas ni acentos. */
    List<Cliente> buscarPorTexto(String texto);

    /** Indica si otro cliente (distinto de {@code idClienteExcluido}, si no es null) ya usa ese email. */
    boolean existeEmail(String email, Integer idClienteExcluido);
}
