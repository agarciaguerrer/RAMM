# RAMM - Gestión de hotel (FP447, Producto 3: persistencia en base de datos)

Aplicación de consola en Java con patrón MVC. Respecto al Producto 2, los datos ya no viven en memoria:
se guardan en una base de datos MySQL mediante JDBC. Solo ha cambiado la capa Modelo (más dos retoques
mínimos en Vista y Controlador).

## Estructura

```
sql/hoteles.sql                      Script: base de datos, tablas, claves y procedimientos almacenados
src/main/java/hoteles
├── modelo        Dominio (Cliente, Hotel, Habitacion, Reserva...), Datos y excepciones propias
│   └── dao       Interfaces DAO, DAOFactory (patrón Factory), Transaccion, PersistenciaException
│       └── mysql Implementaciones MySQL: ConexionBD (utilidad de conexión), MySQL*DAO, ErroresSQL
├── controlador   Controlador (único puente entre vista y modelo)
└── vista         Vista (menús) y Main
src/main/resources/bd.properties     Configuración de conexión por defecto
src/test/java/hoteles/modelo         Pruebas JUnit 5
```

## Puesta en marcha

1. Ejecutar `sql/hoteles.sql` en MySQL 8.0.16 o superior (crea la base `hoteles`; se puede lanzar varias veces):
   `mysql -u root -p < sql/hoteles.sql`
2. Indicar usuario y contraseña. Se puede editar `bd.properties`, o mejor crear un fichero
   `bd.local.properties` junto al proyecto (está en `.gitignore`) con las claves `bd.url`, `bd.usuario`
   y `bd.password`. Orden de prioridad: `-Dbd.xxx` en la JVM > `bd.local.properties` > `bd.properties`.
3. Ejecutar:
   - Desde el IDE: abrir como proyecto Maven y ejecutar `hoteles.vista.Main`.
   - Con Maven: `mvn package` y `java -jar target/RAMM-1.0.jar` (el jar incluye el driver MySQL).

Al arrancar pregunta si se quieren cargar datos de ejemplo; solo se cargan si la base está vacía.
Las fechas se pueden escribir como `dd/MM/yyyy` o `yyyy-MM-dd`.

## Persistencia

- **DAO**: una interfaz por entidad (`ClienteDAO`, `HotelDAO`, `HabitacionDAO`, `ReservaDAO`) y su
  implementación MySQL. `Datos` solo conoce las interfaces.
- **Factory**: `DAOFactory.obtener()` devuelve la fábrica del motor indicado en `bd.tipo`
  (hoy solo `MYSQL`); cambiar de motor no obligaría a tocar `Datos`.
- **Conexión**: `ConexionBD` centraliza la conexión (una sola, perezosa, con reconexión) y las transacciones.
- **Transacciones**: `try (Transaccion tx = fabrica.iniciarTransaccion()) { ...; tx.confirmar(); }`.
  Si no se confirma, al cerrar se hace ROLLBACK. Las transacciones anidadas se unen a la externa.
- **Procedimientos almacenados**: todas las operaciones de inserción, modificación y borrado
  (`sp_cliente_*`, `sp_hotel_insertar`, `sp_habitacion_insertar`, `sp_reserva_*`, `sp_detalle_insertar`)
  se ejecutan con `CallableStatement`. Las consultas usan `PreparedStatement`.
- **Inyección SQL**: ningún valor del usuario se concatena en el SQL; siempre se pasa como parámetro.
- Las reglas de negocio siguen en Java; los procedimientos repiten las críticas (habitación libre,
  existencia) con `SIGNAL`, que `ErroresSQL` traduce a las excepciones propias.
- Los errores técnicos de la base lanzan `PersistenciaException` (sin comprobar), que la Vista muestra
  sin cerrar el programa.

## Pruebas

`mvn test`. Las pruebas de persistencia crean y borran la base `hoteles_test`, así que necesitan un MySQL
accesible con la misma configuración; si no lo hay, se omiten.

## Nota

`Repositorio<T>` (Producto 2) se conserva en el proyecto, pero `Datos` ya no lo utiliza.

## Decisiones de diseño

- La Vista no importa nada del paquete modelo; el Controlador le devuelve textos y tipos básicos.
- Modelo relacional: `cliente`, `hotel`, `habitacion` (1 hotel : N habitaciones), `reserva` y
  `detalle_reserva` (tabla N:M entre reserva y habitación, con el precio aplicado). Todas las claves
  primarias son AUTO_INCREMENT; el código lo asigna la base de datos.
- En memoria, `ArrayList` para las habitaciones de cada hotel y los detalles de cada reserva.
- Excepciones propias: `ElementoDuplicadoException`, `ElementoNoEncontradoException`,
  `HabitacionNoDisponibleException` y `OperacionInvalidaException`.
- Una habitación no está disponible si otra reserva no cancelada se solapa con el rango
  [entrada, salida); el día de salida queda libre para una nueva entrada.
