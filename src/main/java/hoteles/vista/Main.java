package hoteles.vista;

import hoteles.controlador.Controlador;

public class Main {

    public static void main(String[] args) {
        Controlador controlador = new Controlador();
        Vista vista = new Vista(controlador);
        vista.iniciar();
    }
}
