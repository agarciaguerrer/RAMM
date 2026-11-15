package hoteles.vista;

import hoteles.controlador.ClienteControlador;
import hoteles.controlador.HabitacionControlador;
import hoteles.controlador.HotelControlador;
import hoteles.controlador.InicioControlador;
import hoteles.controlador.ReservaControlador;
import hoteles.modelo.Datos;

/** Punto de entrada: crea el Modelo, un controlador por cada área y una vista por cada área. */
public class Main {

    public static void main(String[] args) {
        Datos datos = new Datos();
        InicioControlador inicio = new InicioControlador(datos);
        ClienteControlador clientes = new ClienteControlador(datos);
        HotelControlador hoteles = new HotelControlador(datos);
        HabitacionControlador habitaciones = new HabitacionControlador(datos);
        ReservaControlador reservas = new ReservaControlador(datos);

        Consola consola = new Consola();
        SelectorHotel selector = new SelectorHotel(consola, hoteles);
        ClienteVista vistaClientes = new ClienteVista(consola, clientes);
        HabitacionVista vistaHabitaciones = new HabitacionVista(consola, habitaciones, selector);
        HotelVista vistaHoteles = new HotelVista(consola, hoteles, habitaciones, vistaHabitaciones, selector);
        ReservaVista vistaReservas = new ReservaVista(consola, reservas, clientes, hoteles, habitaciones, selector);

        new MenuVista(consola, inicio, vistaClientes, vistaHoteles, vistaHabitaciones, vistaReservas).iniciar();
    }
}
