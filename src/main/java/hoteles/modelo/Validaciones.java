package hoteles.modelo;

/**
 * Reglas de validación de los datos del modelo, en un único sitio.
 * Las usan las propias clases del modelo al construirse o modificarse, y el Controlador
 * para que la Vista pueda comprobar cada dato en el momento en que el usuario lo escribe.
 * Todas devuelven el valor limpio (sin espacios sobrantes) o lanzan IllegalArgumentException.
 * <p>
 * Las longitudes máximas coinciden con el tamaño de las columnas de la base de datos
 * (sql/hoteles.sql): así el usuario recibe un aviso claro en lugar de un error técnico de MySQL.
 */
public final class Validaciones {

    public static final int MAX_NOMBRE = 100;
    public static final int MAX_APELLIDOS = 150;
    public static final int MAX_EMAIL = 150;
    public static final int MAX_TELEFONO = 30;
    public static final int MAX_NOMBRE_HOTEL = 100;
    public static final int MAX_CIUDAD = 80;
    public static final int MAX_DIRECCION = 150;
    public static final int MAX_NUMERO_HABITACION = 10;
    public static final int MAX_CATEGORIA = 40;
    /** Mayor precio que cabe en DECIMAL(10,2). */
    public static final double MAX_PRECIO = 99_999_999.99;

    private Validaciones() {
    }

    public static String texto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private static String texto(String valor, String mensaje, String campo, int maximo) {
        String limpio = texto(valor, mensaje);
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(campo + " no puede tener más de " + maximo + " caracteres.");
        }
        return limpio;
    }

    public static String nombre(String valor) {
        return texto(valor, "El nombre es obligatorio.", "El nombre", MAX_NOMBRE);
    }

    public static String apellidos(String valor) {
        return texto(valor, "Los apellidos son obligatorios.", "Los apellidos", MAX_APELLIDOS);
    }

    public static String email(String valor) {
        if (valor == null || !valor.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El email no tiene un formato válido (por ejemplo nombre@dominio.com).");
        }
        if (valor.trim().length() > MAX_EMAIL) {
            throw new IllegalArgumentException("El email no puede tener más de " + MAX_EMAIL + " caracteres.");
        }
        return valor.trim();
    }

    public static String telefono(String valor) {
        return texto(valor, "El teléfono es obligatorio.", "El teléfono", MAX_TELEFONO);
    }

    public static String nombreHotel(String valor) {
        return texto(valor, "El nombre del hotel es obligatorio.", "El nombre del hotel", MAX_NOMBRE_HOTEL);
    }

    public static String ciudad(String valor) {
        return texto(valor, "La ciudad es obligatoria.", "La ciudad", MAX_CIUDAD);
    }

    public static String direccion(String valor) {
        return texto(valor, "La dirección es obligatoria.", "La dirección", MAX_DIRECCION);
    }

    public static String numeroHabitacion(String valor) {
        return texto(valor, "El número de habitación es obligatorio.", "El número de habitación", MAX_NUMERO_HABITACION);
    }

    public static String categoria(String valor) {
        return texto(valor, "La categoría es obligatoria.", "La categoría", MAX_CATEGORIA);
    }

    public static int capacidad(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser al menos 1 persona.");
        }
        return valor;
    }

    /** El precio se guarda con dos decimales (DECIMAL(10,2)), así que se redondea a céntimos. */
    public static double precio(double valor) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor que 0.");
        }
        double redondeado = Math.round(valor * 100) / 100.0;
        if (redondeado <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser al menos 0,01 €.");
        }
        if (redondeado > MAX_PRECIO) {
            throw new IllegalArgumentException("El precio por noche no puede superar 99.999.999,99 €.");
        }
        return redondeado;
    }

    public static int personas(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El número de personas debe ser al menos 1.");
        }
        return valor;
    }
}
