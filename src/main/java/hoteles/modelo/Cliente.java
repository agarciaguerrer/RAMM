package hoteles.modelo;

public class Cliente extends Persona {
    private int idCliente;
    public Cliente(int idCliente, String nombre, String apellidos, String email, String telefono) {
        super(nombre, apellidos, email, telefono); setIdCliente(idCliente);
    }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) {
        if (v <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        idCliente = v;
    }
    public void actualizarDatos(String nombre, String apellidos, String email, String telefono) {
        setNombre(nombre); setApellidos(apellidos); setEmail(email); setTelefono(telefono);
    }
    @Override public String toString() { return "Cliente " + idCliente + ": " + super.toString(); }
}
