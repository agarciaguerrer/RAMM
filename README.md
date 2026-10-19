# RAMM - Gestión de hotel (FP447, Producto 2)

Aplicación de consola en Java con patrón MVC, basada en el diagrama de clases del Producto 1.

## Estructura

```
src/main/java/hoteles
├── modelo        Persona, Cliente, Habitacion, Hotel, Reserva, DetalleReserva, EstadoReserva,
│                 Datos, Repositorio<T> (clase genérica) y excepciones propias
├── controlador   Controlador (único puente entre vista y modelo)
└── vista         Vista (menús, Scanner, System.out) y Main
src/test/java/hoteles/modelo   Pruebas JUnit 5
```

## Ejecutar

- Desde el IDE (IntelliJ, NetBeans, Eclipse): abrir como proyecto Maven y ejecutar `hoteles.vista.Main`.
- Con Maven: `mvn test` y `mvn package`, luego `java -jar target/RAMM-1.0.jar`.

Al arrancar pregunta si se quieren cargar datos de ejemplo (3 clientes, 2 hoteles y 2 reservas).
Las fechas se pueden escribir como `dd/MM/yyyy` o `yyyy-MM-dd`.

## Decisiones de diseño

- La Vista no importa nada del paquete modelo; el Controlador le devuelve textos y tipos básicos.
- Colecciones: `LinkedHashMap<Integer, T>` dentro de `Repositorio<T>` (búsqueda por id en O(1) y orden de
  inserción) para clientes, hoteles y reservas; `ArrayList` para las habitaciones de cada hotel y los
  detalles de cada reserva.
- Excepciones propias: `ElementoDuplicadoException`, `ElementoNoEncontradoException`,
  `HabitacionNoDisponibleException` y `OperacionInvalidaException`.
- Una habitación no está disponible si otra reserva no cancelada se solapa con el rango
  [entrada, salida); el día de salida queda libre para una nueva entrada.
