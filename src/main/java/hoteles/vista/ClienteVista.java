package hoteles.vista;

import hoteles.controlador.ClienteControlador;

/** Menú y pantallas de la gestión de clientes. */
class ClienteVista {

    private final Consola consola;
    private final ClienteControlador controlador;

    ClienteVista(Consola consola, ClienteControlador controlador) {
        this.consola = consola;
        this.controlador = controlador;
    }

    void menu() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE CLIENTES ---");
            System.out.println("1. Listar clientes");
            System.out.println("2. Añadir nuevo cliente");
            System.out.println("3. Eliminar cliente");
            System.out.println("4. Buscar cliente por nombre o apellido");
            System.out.println("5. Modificar datos de un cliente");
            System.out.println("6. Consultar reservas de un cliente");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> consola.ejecutar(() -> consola.mostrar(controlador.listarClientes(),
                        "No hay clientes registrados."));
                case 2 -> consola.ejecutar(this::anadirCliente);
                case 3 -> consola.ejecutar(this::eliminarCliente);
                case 4 -> consola.ejecutar(() -> consola.mostrar(
                        controlador.buscarClientes(consola.leerTexto("Nombre o apellido: ")),
                        "No se encontró ningún cliente."));
                case 5 -> consola.ejecutar(this::modificarCliente);
                case 6 -> consola.ejecutar(this::reservasDeCliente);
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void anadirCliente() throws Exception {
        consola.avisoCancelar();
        String nombre = consola.pedirTexto("Nombre: ", controlador::validarNombre);
        String apellidos = consola.pedirTexto("Apellidos: ", controlador::validarApellidos);
        String email = consola.pedirTexto("Email: ", v -> controlador.validarEmail(v, null));
        String telefono = consola.pedirTexto("Teléfono: ", controlador::validarTelefono);
        System.out.println("Cliente añadido:\n  " + controlador.anadirCliente(nombre, apellidos, email, telefono));
    }

    private void eliminarCliente() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id del cliente a eliminar: ", controlador::validarCliente);
        controlador.eliminarCliente(id);
        System.out.println("Cliente eliminado.");
    }

    private void modificarCliente() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id del cliente: ", controlador::validarCliente);
        String nombre = consola.pedirTexto("Nuevo nombre: ", controlador::validarNombre);
        String apellidos = consola.pedirTexto("Nuevos apellidos: ", controlador::validarApellidos);
        String email = consola.pedirTexto("Nuevo email: ", v -> controlador.validarEmail(v, id));
        String telefono = consola.pedirTexto("Nuevo teléfono: ", controlador::validarTelefono);
        System.out.println("Cliente modificado:\n  "
                + controlador.modificarCliente(id, nombre, apellidos, email, telefono));
    }

    private void reservasDeCliente() throws Exception {
        consola.avisoCancelar();
        int id = consola.pedirEntero("Id del cliente: ", controlador::validarCliente);
        consola.mostrar(controlador.reservasDeCliente(id), "Este cliente no tiene reservas.");
    }
}
