package hoteles.controlador;

import hoteles.modelo.Datos;
import hoteles.modelo.Validaciones;

import java.util.List;

/**
 * Controlador de clientes: recibe de la Vista tipos básicos y devuelve textos ya preparados.
 * Las excepciones del modelo se propagan tal cual y la Vista las captura para informar al usuario.
 */
public class ClienteControlador {

    private final Datos datos;

    public ClienteControlador(Datos datos) {
        this.datos = datos;
    }

    public String anadirCliente(String nombre, String apellidos, String email, String telefono) throws Exception {
        return datos.anadirCliente(nombre, apellidos, email, telefono).toString();
    }

    public List<String> listarClientes() {
        return Textos.aTexto(datos.listarClientes());
    }

    public List<String> buscarClientes(String texto) {
        return Textos.aTexto(datos.buscarClientes(texto));
    }

    public String modificarCliente(int idCliente, String nombre, String apellidos, String email, String telefono)
            throws Exception {
        return datos.modificarCliente(idCliente, nombre, apellidos, email, telefono).toString();
    }

    public void eliminarCliente(int idCliente) throws Exception {
        datos.eliminarCliente(idCliente);
    }

    public List<String> reservasDeCliente(int idCliente) throws Exception {
        return Textos.aTexto(datos.reservasDeCliente(idCliente));
    }

    public boolean hayClientes() {
        return !datos.listarClientes().isEmpty();
    }

    // ------------------------------------------------------------ Validaciones
    // La Vista las llama dato a dato para avisar del error en cuanto se escribe un valor incorrecto.
    // Cada una lanza una excepción con el mensaje a mostrar si el valor no es válido.

    public void validarNombre(String valor) {
        Validaciones.nombre(valor);
    }

    public void validarApellidos(String valor) {
        Validaciones.apellidos(valor);
    }

    /** Formato correcto y no repetido. Al modificar un cliente se pasa su id; al crearlo, null. */
    public void validarEmail(String valor, Integer idClienteExcluido) throws Exception {
        Validaciones.email(valor);
        datos.comprobarEmailDisponible(valor, idClienteExcluido);
    }

    public void validarTelefono(String valor) {
        Validaciones.telefono(valor);
    }

    public void validarCliente(int idCliente) throws Exception {
        datos.buscarCliente(idCliente);
    }
}
