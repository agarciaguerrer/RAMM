package hoteles.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Clase genérica que almacena elementos identificados por un id entero.
 * Por defecto usa un LinkedHashMap (búsqueda por id en O(1) y listados en orden de inserción),
 * pero se le puede pasar otra implementación de Map en el constructor para elegir la colección
 * más adecuada en cada caso (por ejemplo, un TreeMap para mantener los elementos ordenados por id).
 *
 * @param <T> tipo de elemento almacenado
 */
public class Repositorio<T> {

    private final String nombreElemento;
    private final Function<T, Integer> extractorId;
    private final Map<Integer, T> elementos;

    public Repositorio(String nombreElemento, Function<T, Integer> extractorId) {
        this(nombreElemento, extractorId, new LinkedHashMap<>());
    }

    /** Permite indicar qué implementación de Map se usa para guardar los elementos. */
    public Repositorio(String nombreElemento, Function<T, Integer> extractorId, Map<Integer, T> mapa) {
        if (mapa == null || !mapa.isEmpty()) {
            throw new IllegalArgumentException("El mapa debe existir y estar vacío");
        }
        this.nombreElemento = nombreElemento;
        this.extractorId = extractorId;
        this.elementos = mapa;
    }

    public void anadir(T elemento) throws ElementoDuplicadoException {
        int id = extractorId.apply(elemento);
        if (elementos.containsKey(id)) {
            throw new ElementoDuplicadoException("Ya existe " + nombreElemento + " con id " + id);
        }
        elementos.put(id, elemento);
    }

    public T buscar(int id) throws ElementoNoEncontradoException {
        T elemento = elementos.get(id);
        if (elemento == null) {
            throw new ElementoNoEncontradoException("No existe " + nombreElemento + " con id " + id);
        }
        return elemento;
    }

    public T eliminar(int id) throws ElementoNoEncontradoException {
        T elemento = elementos.remove(id);
        if (elemento == null) {
            throw new ElementoNoEncontradoException("No existe " + nombreElemento + " con id " + id);
        }
        return elemento;
    }

    public List<T> listar() {
        return new ArrayList<>(elementos.values());
    }

    public List<T> filtrar(Predicate<T> condicion) {
        List<T> resultado = new ArrayList<>();
        for (T elemento : elementos.values()) {
            if (condicion.test(elemento)) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    public int tamano() {
        return elementos.size();
    }
}
