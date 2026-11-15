-- =============================================================================
--  RAMM - FP447 - Producto 3. Persistencia en base de datos
--  Estructura de la base de datos MySQL 8.0 (tablas, claves, autonuméricos y
--  procedimientos almacenados) a partir del diagrama de clases del Producto 1.
--
--  Cómo ejecutarlo: abrir en MySQL Workbench y ejecutar todo el script
--  (Ctrl+Shift+Enter), o desde consola:   mysql -u root -p < sql/hoteles.sql
--  Se puede ejecutar más de una vez sin perder datos: las tablas solo se crean
--  si no existen y los procedimientos se vuelven a crear.
--
--  Modelo relacional (PK subrayada con *, FK con ->):
--
--     cliente   (*id_cliente, nombre, apellidos, email UNIQUE, telefono)
--     hotel     (*id_hotel, nombre, ciudad, direccion)
--     habitacion(*id_habitacion, id_hotel -> hotel, numero, categoria, capacidad,
--                precio_noche, UNIQUE (id_hotel, numero))
--     reserva   (*id_reserva, id_cliente -> cliente, id_hotel -> hotel,
--                fecha_entrada, fecha_salida, numero_personas, estado)
--     detalle_reserva(*id_detalle, id_reserva -> reserva, id_habitacion -> habitacion,
--                precio_noche_aplicado, UNIQUE (id_reserva, id_habitacion))
--
--  Decisiones de paso de clases a tablas:
--   - Persona es abstracta y Cliente es su única subclase concreta: se crea una sola
--     tabla "cliente" con los atributos heredados (nombre, apellidos, email, telefono).
--   - Hotel 1..* Habitacion  -> FK id_hotel en habitacion (composición: ON DELETE CASCADE).
--   - Cliente 1..* Reserva y Hotel 1..* Reserva -> FK en reserva.
--   - Reserva 1..* DetalleReserva *..1 Habitacion -> detalle_reserva es la tabla
--     intermedia de la relación muchos a muchos Reserva-Habitacion; guarda además el
--     precio aplicado por noche.
--   - EstadoReserva (enum) -> columna ENUM, que se corresponde uno a uno con el enum Java.
--   - Todas las claves primarias son AUTO_INCREMENT: el id lo genera MySQL al insertar.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS RAMM_hoteles
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE RAMM_hoteles;

-- Para empezar de cero (BORRA TODOS LOS DATOS), descomentar:
-- DROP TABLE IF EXISTS detalle_reserva, reserva, habitacion, hotel, cliente;

-- -----------------------------------------------------------------------------
--  TABLAS
--  Las longitudes coinciden con las que valida hoteles.modelo.Validaciones.
--  La collation utf8mb4_0900_ai_ci no distingue mayúsculas ni acentos, así que
--  "Laura@Email.com" y "laura@email.com" cuentan como el mismo email (UNIQUE).
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    apellidos   VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    telefono    VARCHAR(30)  NOT NULL,
    CONSTRAINT pk_cliente       PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS hotel (
    id_hotel   INT          NOT NULL AUTO_INCREMENT,
    nombre     VARCHAR(100) NOT NULL,
    ciudad     VARCHAR(80)  NOT NULL,
    direccion  VARCHAR(150) NOT NULL,
    CONSTRAINT pk_hotel PRIMARY KEY (id_hotel),
    INDEX idx_hotel_ciudad (ciudad)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS habitacion (
    id_habitacion INT           NOT NULL AUTO_INCREMENT,
    id_hotel      INT           NOT NULL,
    numero        VARCHAR(10)   NOT NULL,
    categoria     VARCHAR(40)   NOT NULL,
    capacidad     INT           NOT NULL,
    precio_noche  DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_habitacion        PRIMARY KEY (id_habitacion),
    CONSTRAINT uq_habitacion_numero UNIQUE (id_hotel, numero),
    CONSTRAINT fk_habitacion_hotel  FOREIGN KEY (id_hotel) REFERENCES hotel (id_hotel)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_habitacion_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_habitacion_precio    CHECK (precio_noche > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS reserva (
    id_reserva      INT  NOT NULL AUTO_INCREMENT,
    id_cliente      INT  NOT NULL,
    id_hotel        INT  NOT NULL,
    fecha_entrada   DATE NOT NULL,
    fecha_salida    DATE NOT NULL,
    numero_personas INT  NOT NULL,
    estado          ENUM ('PENDIENTE', 'CONFIRMADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
    -- Si se elimina un cliente (solo posible sin reservas activas) se van con él sus reservas canceladas.
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reserva_hotel   FOREIGN KEY (id_hotel)   REFERENCES hotel (id_hotel)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_reserva_fechas    CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT ck_reserva_personas  CHECK (numero_personas > 0),
    -- Acelera la búsqueda de reservas que se solapan con un rango de fechas en un hotel.
    INDEX idx_reserva_hotel_fechas (id_hotel, fecha_entrada, fecha_salida)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS detalle_reserva (
    id_detalle            INT           NOT NULL AUTO_INCREMENT,
    id_reserva            INT           NOT NULL,
    id_habitacion         INT           NOT NULL,
    precio_noche_aplicado DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_detalle_reserva PRIMARY KEY (id_detalle),
    CONSTRAINT uq_detalle_reserva_habitacion UNIQUE (id_reserva, id_habitacion),
    CONSTRAINT fk_detalle_reserva    FOREIGN KEY (id_reserva)    REFERENCES reserva (id_reserva)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion (id_habitacion)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_detalle_precio CHECK (precio_noche_aplicado >= 0)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
--  PROCEDIMIENTOS ALMACENADOS (todas las operaciones DML: INSERT, UPDATE, DELETE)
--
--  Los llama la capa DAO con CallableStatement. Se ejecutan dentro de la transacción
--  que abre el modelo (hoteles.modelo.Datos), de modo que una operación que afecta a
--  varias filas se confirma entera o no se confirma.
--
--  Cuando se incumple una regla de negocio se lanza un error propio con SIGNAL
--  (SQLSTATE 45000) y un código que el DAO traduce a una excepción del modelo:
--      30001  elemento no encontrado        -> ElementoNoEncontradoException
--      30002  habitación no disponible      -> HabitacionNoDisponibleException
--      30003  operación no válida           -> OperacionInvalidaException
--  El modelo Java comprueba estas mismas reglas antes para dar mensajes claros al
--  usuario; aquí se vuelven a comprobar para que la base de datos nunca quede
--  incoherente aunque se llame a los procedimientos desde otro programa.
-- -----------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS sp_cliente_insertar;
DROP PROCEDURE IF EXISTS sp_cliente_modificar;
DROP PROCEDURE IF EXISTS sp_cliente_eliminar;
DROP PROCEDURE IF EXISTS sp_hotel_insertar;
DROP PROCEDURE IF EXISTS sp_habitacion_insertar;
DROP PROCEDURE IF EXISTS sp_reserva_insertar;
DROP PROCEDURE IF EXISTS sp_detalle_insertar;
DROP PROCEDURE IF EXISTS sp_reserva_modificar;
DROP PROCEDURE IF EXISTS sp_reserva_cambiar_estado;

DELIMITER $$

CREATE PROCEDURE sp_cliente_insertar(
    IN  p_nombre     VARCHAR(100),
    IN  p_apellidos  VARCHAR(150),
    IN  p_email      VARCHAR(150),
    IN  p_telefono   VARCHAR(30),
    OUT p_id_cliente INT)
BEGIN
    -- Un email repetido lo rechaza la restricción UNIQUE uq_cliente_email (error 1062).
    INSERT INTO cliente (nombre, apellidos, email, telefono)
    VALUES (p_nombre, p_apellidos, p_email, p_telefono);
    SET p_id_cliente = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_cliente_modificar(
    IN p_id_cliente INT,
    IN p_nombre     VARCHAR(100),
    IN p_apellidos  VARCHAR(150),
    IN p_email      VARCHAR(150),
    IN p_telefono   VARCHAR(30))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = p_id_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe el cliente indicado';
    END IF;
    UPDATE cliente
       SET nombre = p_nombre, apellidos = p_apellidos, email = p_email, telefono = p_telefono
     WHERE id_cliente = p_id_cliente;
END$$

CREATE PROCEDURE sp_cliente_eliminar(IN p_id_cliente INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = p_id_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe el cliente indicado';
    END IF;
    IF EXISTS (SELECT 1 FROM reserva WHERE id_cliente = p_id_cliente AND estado <> 'CANCELADA') THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30003,
            MESSAGE_TEXT = 'No se puede eliminar un cliente con reservas activas';
    END IF;
    -- Las reservas canceladas del cliente (y sus detalles) se borran en cascada.
    DELETE FROM cliente WHERE id_cliente = p_id_cliente;
END$$

CREATE PROCEDURE sp_hotel_insertar(
    IN  p_nombre    VARCHAR(100),
    IN  p_ciudad    VARCHAR(80),
    IN  p_direccion VARCHAR(150),
    OUT p_id_hotel  INT)
BEGIN
    INSERT INTO hotel (nombre, ciudad, direccion) VALUES (p_nombre, p_ciudad, p_direccion);
    SET p_id_hotel = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_habitacion_insertar(
    IN  p_id_hotel      INT,
    IN  p_numero        VARCHAR(10),
    IN  p_categoria     VARCHAR(40),
    IN  p_capacidad     INT,
    IN  p_precio_noche  DECIMAL(10,2),
    OUT p_id_habitacion INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM hotel WHERE id_hotel = p_id_hotel) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe el hotel indicado';
    END IF;
    -- Un número repetido en el mismo hotel lo rechaza UNIQUE uq_habitacion_numero (error 1062).
    INSERT INTO habitacion (id_hotel, numero, categoria, capacidad, precio_noche)
    VALUES (p_id_hotel, p_numero, p_categoria, p_capacidad, p_precio_noche);
    SET p_id_habitacion = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_reserva_insertar(
    IN  p_id_cliente      INT,
    IN  p_id_hotel        INT,
    IN  p_fecha_entrada   DATE,
    IN  p_fecha_salida    DATE,
    IN  p_numero_personas INT,
    IN  p_estado          VARCHAR(10),
    OUT p_id_reserva      INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = p_id_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe el cliente indicado';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM hotel WHERE id_hotel = p_id_hotel) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe el hotel indicado';
    END IF;
    INSERT INTO reserva (id_cliente, id_hotel, fecha_entrada, fecha_salida, numero_personas, estado)
    VALUES (p_id_cliente, p_id_hotel, p_fecha_entrada, p_fecha_salida, p_numero_personas, p_estado);
    SET p_id_reserva = LAST_INSERT_ID();
END$$

-- Añade una habitación a una reserva comprobando que sigue libre en esas fechas.
-- Bloquea la fila de la habitación (FOR UPDATE) para que dos reservas simultáneas
-- de la misma habitación no puedan pasar la comprobación a la vez.
CREATE PROCEDURE sp_detalle_insertar(
    IN  p_id_reserva    INT,
    IN  p_id_habitacion INT,
    IN  p_precio_noche  DECIMAL(10,2),
    OUT p_id_detalle    INT)
BEGIN
    DECLARE v_entrada       DATE DEFAULT NULL;
    DECLARE v_salida        DATE DEFAULT NULL;
    DECLARE v_hotel_reserva INT  DEFAULT NULL;
    DECLARE v_hotel_hab     INT  DEFAULT NULL;
    DECLARE v_capacidad     INT  DEFAULT NULL;
    DECLARE v_personas      INT  DEFAULT NULL;

    SELECT fecha_entrada, fecha_salida, id_hotel, numero_personas
      INTO v_entrada, v_salida, v_hotel_reserva, v_personas
      FROM reserva WHERE id_reserva = p_id_reserva FOR UPDATE;
    IF v_entrada IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe la reserva indicada';
    END IF;

    SELECT id_hotel, capacidad INTO v_hotel_hab, v_capacidad
      FROM habitacion WHERE id_habitacion = p_id_habitacion FOR UPDATE;
    IF v_hotel_hab IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe la habitación indicada';
    END IF;
    IF v_hotel_hab <> v_hotel_reserva THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30003,
            MESSAGE_TEXT = 'La habitación no pertenece al hotel de la reserva';
    END IF;
    IF v_capacidad < v_personas THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30002,
            MESSAGE_TEXT = 'La habitación no tiene capacidad suficiente para las personas de la reserva';
    END IF;

    IF EXISTS (SELECT 1
                 FROM detalle_reserva d
                 JOIN reserva r ON r.id_reserva = d.id_reserva
                WHERE d.id_habitacion = p_id_habitacion
                  AND r.id_reserva <> p_id_reserva
                  AND r.estado <> 'CANCELADA'
                  AND r.fecha_entrada < v_salida
                  AND v_entrada < r.fecha_salida) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30002,
            MESSAGE_TEXT = 'La habitación no está disponible en las fechas de la reserva';
    END IF;

    INSERT INTO detalle_reserva (id_reserva, id_habitacion, precio_noche_aplicado)
    VALUES (p_id_reserva, p_id_habitacion, p_precio_noche);
    SET p_id_detalle = LAST_INSERT_ID();
END$$

-- Cambia fechas y número de personas. Las habitaciones de la reserva deben seguir
-- libres en las nuevas fechas (sin contar la propia reserva) y tener capacidad.
CREATE PROCEDURE sp_reserva_modificar(
    IN p_id_reserva      INT,
    IN p_fecha_entrada   DATE,
    IN p_fecha_salida    DATE,
    IN p_numero_personas INT)
BEGIN
    DECLARE v_estado VARCHAR(10) DEFAULT NULL;
    DECLARE v_bloqueadas INT DEFAULT 0;

    SELECT estado INTO v_estado FROM reserva WHERE id_reserva = p_id_reserva FOR UPDATE;
    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe la reserva indicada';
    END IF;
    IF v_estado = 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30003,
            MESSAGE_TEXT = 'No se puede modificar una reserva cancelada';
    END IF;

    -- Bloquea las habitaciones de la reserva mientras se comprueba su disponibilidad.
    SELECT COUNT(*) INTO v_bloqueadas
      FROM habitacion h
      JOIN detalle_reserva d ON d.id_habitacion = h.id_habitacion
     WHERE d.id_reserva = p_id_reserva
       FOR UPDATE;

    IF EXISTS (SELECT 1
                 FROM detalle_reserva d
                 JOIN habitacion h ON h.id_habitacion = d.id_habitacion
                WHERE d.id_reserva = p_id_reserva AND h.capacidad < p_numero_personas) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30002,
            MESSAGE_TEXT = 'Una habitación de la reserva no tiene capacidad para ese número de personas';
    END IF;

    IF EXISTS (SELECT 1
                 FROM detalle_reserva propio
                 JOIN detalle_reserva otro ON otro.id_habitacion = propio.id_habitacion
                                          AND otro.id_reserva <> propio.id_reserva
                 JOIN reserva r ON r.id_reserva = otro.id_reserva
                WHERE propio.id_reserva = p_id_reserva
                  AND r.estado <> 'CANCELADA'
                  AND r.fecha_entrada < p_fecha_salida
                  AND p_fecha_entrada < r.fecha_salida) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30002,
            MESSAGE_TEXT = 'Una habitación de la reserva no está disponible en las nuevas fechas';
    END IF;

    UPDATE reserva
       SET fecha_entrada = p_fecha_entrada, fecha_salida = p_fecha_salida,
           numero_personas = p_numero_personas
     WHERE id_reserva = p_id_reserva;
END$$

CREATE PROCEDURE sp_reserva_cambiar_estado(
    IN p_id_reserva INT,
    IN p_estado     VARCHAR(10))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM reserva WHERE id_reserva = p_id_reserva) THEN
        SIGNAL SQLSTATE '45000' SET MYSQL_ERRNO = 30001, MESSAGE_TEXT = 'No existe la reserva indicada';
    END IF;
    UPDATE reserva SET estado = p_estado WHERE id_reserva = p_id_reserva;
END$$

DELIMITER ;
