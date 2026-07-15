-- =====================================================================
-- CONVENIOS DE PAGO (condonación de cargos/multas)
-- =====================================================================
-- Un convenio es el acuerdo entre el comité y el usuario. Puede condonar
-- una o varias líneas de agua_usuario_cargo a la vez (por eso hay una
-- tabla puente agua_convenio_cargo), cada línea con su propio monto
-- condonado. Es el mismo patrón que agua_recibo/agua_recibo_pago y
-- agua_usuario_cargo/agua_usuario_cargo_pago: un encabezado y N líneas.
--
-- El saldo de un cargo pasa a ser:
--   saldo = monto - monto_pagado (abonos) - monto_condonado (convenios)
-- =====================================================================

-- los_lopez_db.agua_convenio definition
CREATE TABLE `agua_convenio` (
  `agua_convenio_id` int NOT NULL AUTO_INCREMENT,
  `agua_usuario_id` int NOT NULL,
  `no_folio` int DEFAULT NULL,
  `fecha` date NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `comentario` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `estatus_convenio_id` int DEFAULT NULL,
  `estatus` int DEFAULT '1',
  `user_id_add` int DEFAULT NULL,
  `date_add` datetime(6) DEFAULT NULL,
  `user_id_update` int DEFAULT NULL,
  `date_update` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`agua_convenio_id`),
  KEY `FK_convenio_usuario` (`agua_usuario_id`),
  KEY `FK_convenio_estatus` (`estatus_convenio_id`),
  CONSTRAINT `FK_convenio_usuario` FOREIGN KEY (`agua_usuario_id`) REFERENCES `agua_usuario` (`agua_usuario_id`),
  CONSTRAINT `FK_convenio_estatus` FOREIGN KEY (`estatus_convenio_id`) REFERENCES `catalogo_opciones` (`catalogo_opciones_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Si ya habías corrido este script antes de que se agregara `no_folio`
-- (sin la columna arriba), agrégala con esto en vez de recrear la tabla:
-- ALTER TABLE `agua_convenio` ADD COLUMN `no_folio` int DEFAULT NULL AFTER `agua_usuario_id`;

-- los_lopez_db.agua_convenio_cargo definition
-- Líneas del convenio: qué cargo se condona y cuánto de ese cargo.
CREATE TABLE `agua_convenio_cargo` (
  `agua_convenio_cargo_id` int NOT NULL AUTO_INCREMENT,
  `agua_convenio_id` int NOT NULL,
  `agua_usuario_cargo_id` int NOT NULL,
  `monto_condonado` decimal(10,2) NOT NULL,
  `estatus` int DEFAULT '1',
  `user_id_add` int DEFAULT NULL,
  `date_add` datetime(6) DEFAULT NULL,
  `user_id_update` int DEFAULT NULL,
  `date_update` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`agua_convenio_cargo_id`),
  KEY `FK_conveniocargo_convenio` (`agua_convenio_id`),
  KEY `FK_conveniocargo_cargo` (`agua_usuario_cargo_id`),
  CONSTRAINT `FK_conveniocargo_convenio` FOREIGN KEY (`agua_convenio_id`) REFERENCES `agua_convenio` (`agua_convenio_id`),
  CONSTRAINT `FK_conveniocargo_cargo` FOREIGN KEY (`agua_usuario_cargo_id`) REFERENCES `agua_usuario_cargo` (`agua_usuario_cargo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Catálogo de estatus del convenio
INSERT INTO catalogo (clave, nombre, descripcion, estatus, date_add)
VALUES ('ESTATUS_CONVENIO', 'Estatus de convenio', 'Seguimiento del convenio de pago', 1, NOW());

SET @catalogo_estatus_convenio_id = LAST_INSERT_ID();

INSERT INTO catalogo_opciones (catalogo_id, nombre, descripcion, estatus, date_add) VALUES
(@catalogo_estatus_convenio_id, 'Activo',    'Convenio vigente', 1, NOW()),
(@catalogo_estatus_convenio_id, 'Cumplido',  'Convenio cumplido en su totalidad', 1, NOW()),
(@catalogo_estatus_convenio_id, 'Cancelado', 'Convenio cancelado', 1, NOW());
