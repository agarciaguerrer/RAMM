package hoteles.vista;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entrada y salida por consola compartida por todas las vistas (Scanner y System.out):
 * lectura de datos con validación, avisos y listados. No conoce el Modelo ni los controladores.
 */
class Consola {

    /** Acción de menú que puede lanzar cualquier excepción del modelo. */
    interface Accion {
        void ejecutar() throws Exception;
    }

    /** Comprobación de un dato recién escrito: si no es válido, lanza una excepción con el motivo. */
    interface Comprobacion<T> {
        void comprobar(T valor) throws Exception;
    }

    /** Se lanza cuando el usuario escribe "cancelar" en un formulario. */
    static class OperacionCancelada extends RuntimeException {
    }

    static final String PALABRA_CANCELAR = "cancelar";
    static final String TEXTO_FECHA = " (dd/MM/yyyy o yyyy-MM-dd): ";
    private static final DateTimeFormatter FORMATO_ES = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Ejecuta una acción y, si el modelo lanza una excepción, muestra su mensaje en lugar de
     * romper el programa.
     */
    void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (OperacionCancelada e) {
            System.out.println("Operación cancelada.");
        } catch (NoSuchElementException e) {
            throw e; // fin de la entrada: lo gestiona MenuVista.iniciar()
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    void avisoCancelar() {
        System.out.println("(Escribe '" + PALABRA_CANCELAR + "' en cualquier momento para volver al menú)");
    }

    void mostrar(List<String> lineas, String mensajeVacio) {
        if (lineas.isEmpty()) {
            if (!mensajeVacio.isEmpty()) {
                System.out.println(mensajeVacio);
            }
            return;
        }
        for (String linea : lineas) {
            System.out.println("  " + linea);
        }
    }

    String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido.");
            }
        }
    }

    boolean preguntarSiNo(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje + " (s/n): ").toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) {
                return true;
            }
            if (r.equals("n") || r.equals("no")) {
                return false;
            }
        }
    }

    // Los métodos "pedir..." repiten la pregunta hasta que el dato es correcto.
    // Si el valor es incorrecto se muestra el motivo y se vuelve a preguntar lo mismo,
    // sin avanzar al siguiente dato. Escribir "cancelar" aborta la operación.

    private void comprobarCancelar(String texto) {
        if (texto.equalsIgnoreCase(PALABRA_CANCELAR)) {
            throw new OperacionCancelada();
        }
    }

    String pedirTexto(String mensaje, Comprobacion<String> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                comprobacion.comprobar(texto);
                return texto;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    String pedirTextoOpcional(String mensaje, Comprobacion<String> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                comprobacion.comprobar(texto);
                return texto;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    int pedirEntero(String mensaje, Comprobacion<Integer> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                int valor = Integer.parseInt(texto);
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número entero válido.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    Integer pedirEnteroOpcional(String mensaje, Comprobacion<Integer> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int valor = Integer.parseInt(texto);
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número entero válido o déjalo vacío.");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    double pedirDecimal(String mensaje, Comprobacion<Double> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            try {
                double valor = Double.parseDouble(texto.replace(',', '.'));
                comprobacion.comprobar(valor);
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce un número válido (por ejemplo 89.90).");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    LocalDate pedirFecha(String mensaje, Comprobacion<LocalDate> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            LocalDate fecha = convertirFecha(texto);
            if (fecha == null) {
                System.out.println("ERROR: Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd.");
                continue;
            }
            try {
                comprobacion.comprobar(fecha);
                return fecha;
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    LocalDate pedirFechaOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            if (texto.isEmpty()) {
                return null;
            }
            LocalDate fecha = convertirFecha(texto);
            if (fecha != null) {
                return fecha;
            }
            System.out.println("ERROR: Fecha no válida. Usa dd/MM/yyyy o yyyy-MM-dd, o déjala vacía.");
        }
    }

    List<Integer> pedirListaEnteros(String mensaje, Comprobacion<List<Integer>> comprobacion) {
        while (true) {
            String texto = leerTexto(mensaje);
            comprobarCancelar(texto);
            List<Integer> numeros = new ArrayList<>();
            try {
                for (String parte : texto.split(",")) {
                    numeros.add(Integer.parseInt(parte.trim()));
                }
                comprobacion.comprobar(numeros);
                return numeros;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce números separados por comas (por ejemplo 1,3).");
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private LocalDate convertirFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(texto, FORMATO_ES);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
