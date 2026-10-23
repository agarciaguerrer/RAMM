package hoteles.modelo;

public abstract class Persona {
    private String nombre, apellidos, email, telefono;

    public Persona(String nombre, String apellidos, String email, String telefono) {
        setNombre(nombre); setApellidos(apellidos); setEmail(email); setTelefono(telefono);
    }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = Validaciones.nombre(v); }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { apellidos = Validaciones.apellidos(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = Validaciones.email(v); }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { telefono = Validaciones.telefono(v); }
    protected static String obligatorio(String v, String mensaje) {
        return Validaciones.texto(v, mensaje);
    }
    @Override public String toString() {
        return nombre + " " + apellidos + " | " + email + " | Tel: " + telefono;
    }
}
