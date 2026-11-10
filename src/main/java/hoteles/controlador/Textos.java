package hoteles.controlador;

import java.util.ArrayList;
import java.util.List;

/** Utilidad común de los controladores: convierte objetos del modelo en textos para la Vista. */
final class Textos {

    private Textos() {
    }

    static List<String> aTexto(List<?> elementos) {
        List<String> textos = new ArrayList<>();
        for (Object elemento : elementos) {
            textos.add(elemento.toString());
        }
        return textos;
    }
}
