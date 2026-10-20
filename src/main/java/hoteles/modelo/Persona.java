package hoteles.modelo;

public abstract class Persona {
    private String nombre, apellidos, email, telefono;

    public Persona(String nombre, String apellidos, String email, String telefono) {
        setNombre(nombre); setApellidos(apellidos); setEmail(email); setTelefono(telefono);
    }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = obligatorio(v, "El nombre es obligatorio."); }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { apellidos = obligatorio(v, "Los apellidos son obligatorios."); }
    public String getEmail() { return email; }
    public void setEmail(String v) {
        if (v == null || !v.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        email = v.trim();
    }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { telefono = obligatorio(v, "El teléfono es obligatorio."); }
    protected static String obligatorio(String v, String mensaje) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(mensaje);
        return v.trim();
    }
    @Override public String toString() {
        return nombre + " " + apellidos + " | " + email + " | Tel: " + telefono;
    }
}
