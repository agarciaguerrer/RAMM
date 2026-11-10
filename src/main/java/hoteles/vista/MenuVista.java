package hoteles.vista;

import hoteles.controlador.InicioControlador;

import java.util.NoSuchElementException;

/** Pantalla de bienvenida y menú principal: reparte el trabajo entre las demás vistas. */
public class MenuVista {

    private final Consola consola;
    private final InicioControlador inicio;
    private final ClienteVista clientes;
    private final HotelVista hoteles;
    private final HabitacionVista habitaciones;
    private final ReservaVista reservas;

    MenuVista(Consola consola, InicioControlador inicio, ClienteVista clientes, HotelVista hoteles,
              HabitacionVista habitaciones, ReservaVista reservas) {
        this.consola = consola;
        this.inicio = inicio;
        this.clientes = clientes;
        this.hoteles = hoteles;
        this.habitaciones = habitaciones;
        this.reservas = reservas;
    }

    public void iniciar() {
        System.out.println("=====================================");
        System.out.println("   GESTIÓN DE HOTEL - Grupo RAMM");
        System.out.println("=====================================");
        try {
            if (consola.preguntarSiNo("¿Cargar datos de ejemplo?")) {
                cargarDatosEjemplo();
            }
            menuPrincipal();
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Saliendo...");
        }
    }

    private void cargarDatosEjemplo() {
        try {
            if (inicio.cargarDatosPrueba()) {
                System.out.println("Datos de ejemplo cargados.");
            } else {
                System.out.println("La base de datos ya contiene datos: no se han cargado los de ejemplo.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void menuPrincipal() {
        int opcion;
        do {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestión de clientes");
            System.out.println("2. Gestión de hoteles");
            System.out.println("3. Gestión de habitaciones");
            System.out.println("4. Gestión de reservas");
            System.out.println("5. Consulta de reservas");
            System.out.println("0. Salir");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> clientes.menu();
                case 2 -> hoteles.menu();
                case 3 -> habitaciones.menu();
                case 4 -> reservas.menu();
                case 5 -> reservas.menuConsulta();
                case 0 -> System.out.println("¡Hasta pronto!");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
