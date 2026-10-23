package hoteles.modelo;

/**
 * Reglas de validación de los datos del modelo, en un único sitio.
 * Las usan las propias clases del modelo al construirse o modificarse, y el Controlador
 * para que la Vista pueda comprobar cada dato en el momento en que el usuario lo escribe.
 * Todas devuelven el valor limpio (sin espacios sobrantes) o lanzan IllegalArgumentException.
 */
public final class Validaciones {

    private Validaciones() {
    }

    public static String texto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    public static String nombre(String valor) {
        return texto(valor, "El nombre es obligatorio.");
    }

    public static String apellidos(String valor) {
        return texto(valor, "Los apellidos son obligatorios.");
    }

    public static String email(String valor) {
        if (valor == null || !valor.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El email no tiene un formato válido (por ejemplo nombre@dominio.com).");
        }
        return valor.trim();
    }

    public static String telefono(String valor) {
        return texto(valor, "El teléfono es obligatorio.");
    }

    public static String nombreHotel(String valor) {
        return texto(valor, "El nombre del hotel es obligatorio.");
    }

    public static String ciudad(String valor) {
        return texto(valor, "La ciudad es obligatoria.");
    }

    public static String direccion(String valor) {
        return texto(valor, "La dirección es obligatoria.");
    }

    public static String numeroHabitacion(String valor) {
        return texto(valor, "El número de habitación es obligatorio.");
    }

    public static String categoria(String valor) {
        return texto(valor, "La categoría es obligatoria.");
    }

    public static int capacidad(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser al menos 1 persona.");
        }
        return valor;
    }

    public static double precio(double valor) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor que 0.");
        }
        return valor;
    }

    public static int personas(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El número de personas debe ser al menos 1.");
        }
        return valor;
    }
}
