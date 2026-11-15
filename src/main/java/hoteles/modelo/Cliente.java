package hoteles.modelo;

public class Cliente extends Persona {
    private int idCliente;
    public Cliente(int idCliente, String nombre, String apellidos, String email, String telefono) {
        super(nombre, apellidos, email, telefono); setIdCliente(idCliente);
    }
    /** Cliente todavía no guardado: su id (0 hasta entonces) lo asigna la base de datos al insertarlo. */
    public Cliente(String nombre, String apellidos, String email, String telefono) {
        super(nombre, apellidos, email, telefono);
    }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        idCliente = v;
    }
    public void actualizarDatos(String nombre, String apellidos, String email, String telefono) {
        // Se valida todo antes de cambiar nada: si un dato es incorrecto, el cliente no queda a medias
        String n = Validaciones.nombre(nombre);
        String a = Validaciones.apellidos(apellidos);
        String e = Validaciones.email(email);
        String t = Validaciones.telefono(telefono);
        setNombre(n); setApellidos(a); setEmail(e); setTelefono(t);
    }
    @Override public String toString() { return "Cliente " + idCliente + ": " + super.toString(); }
}
