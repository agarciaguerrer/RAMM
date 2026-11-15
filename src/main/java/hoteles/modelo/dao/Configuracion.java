package hoteles.modelo.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Datos de configuración de la persistencia (motor, URL, usuario y contraseña), para no escribirlos
 * dentro del código. Se leen, de menor a mayor prioridad, de:
 * <ol>
 *   <li>{@code bd.properties}, incluido dentro del programa (valores por defecto);</li>
 *   <li>{@code bd.local.properties}, un fichero opcional en la carpeta desde la que se ejecuta el
 *       programa, para poner la contraseña propia sin tocar el proyecto (Git lo ignora);</li>
 *   <li>propiedades de la JVM, por ejemplo {@code -Dbd.password=...}.</li>
 * </ol>
 */
public final class Configuracion {

    private static final String RECURSO = "bd.properties";
    private static final String FICHERO_LOCAL = "bd.local.properties";
    private static final Properties PROPIEDADES = cargar();

    private Configuracion() {
    }

    public static String get(String clave, String valorPorDefecto) {
        String valor = System.getProperty(clave);
        if (valor == null) {
            valor = PROPIEDADES.getProperty(clave);
        }
        return valor == null ? valorPorDefecto : valor.trim();
    }

    private static Properties cargar() {
        Properties propiedades = new Properties();
        try (InputStream in = Configuracion.class.getClassLoader().getResourceAsStream(RECURSO)) {
            if (in != null) {
                try (Reader lector = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    propiedades.load(lector);
                }
            }
            Path local = Path.of(FICHERO_LOCAL);
            if (Files.isRegularFile(local)) {
                try (Reader lector = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
                    propiedades.load(lector);
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer la configuración de la base de datos: " + e.getMessage(), e);
        }
        return propiedades;
    }
}
