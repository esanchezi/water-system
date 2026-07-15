-- =====================================================================
-- CARGOS EXTRA / MULTAS POR USUARIO
-- =====================================================================
-- Nombre elegido: "cargo" en vez de "extra" o "multa", porque la lista de
-- conceptos que se pidió agregar (interés, mano de obra, insumos, bastón,
-- llave azul, reparaciones) no son todos multas: son cargos adicionales
-- a la cuota normal. "Multa" queda como UNA opción más dentro del mismo
-- catálogo de conceptos, junto a las demás.
--
-- Sigue el mismo patrón que agua_usuario_aviso: llave a agua_usuario,
-- catálogo genérico (catalogo_opciones) para el tipo, y campos de auditoría
-- (estatus, user_id_add, date_add, user_id_update, date_update).
--
-- El punto de "identificar si ya fue pagado", actualizado: un cargo se
-- puede liquidar en abonos con distintos recibos (confirmado por el
-- usuario), así que NO lleva recibo_id directo. En vez de eso hay una
-- tabla hija `agua_usuario_cargo_pago`, exactamente el mismo patrón que
-- ya usan `agua_recibo` / `agua_recibo_pago`: el cargo es la "deuda" y
-- cada fila de `agua_usuario_cargo_pago` es un abono real ligado a un
-- recibo. El cargo queda pagado cuando SUM(monto_aplicado) >= monto.
-- =====================================================================

-- los_lopez_db.agua_usuario_cargo definition
CREATE TABLE `agua_usuario_cargo` (
  `agua_usuario_cargo_id` int NOT NULL AUTO_INCREMENT,
  `agua_usuario_id` int NOT NULL,
  `concepto_id` int NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `monto` decimal(10,2) NOT NULL,
  `fecha` date NOT NULL,
  `comentario` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `estatus` int DEFAULT '1',
  `user_id_add` int DEFAULT NULL,
  `date_add` datetime(6) DEFAULT NULL,
  `user_id_update` int DEFAULT NULL,
  `date_update` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`agua_usuario_cargo_id`),
  KEY `FK_cargo_concepto` (`concepto_id`),
  KEY `FK_cargo_usuario` (`agua_usuario_id`),
  CONSTRAINT `FK_cargo_concepto` FOREIGN KEY (`concepto_id`) REFERENCES `catalogo_opciones` (`catalogo_opciones_id`),
  CONSTRAINT `FK_cargo_usuario` FOREIGN KEY (`agua_usuario_id`) REFERENCES `agua_usuario` (`agua_usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- los_lopez_db.agua_usuario_cargo_pago definition
-- Cada abono de un cargo, ligado al recibo con el que se cobró.
CREATE TABLE `agua_usuario_cargo_pago` (
  `agua_usuario_cargo_pago_id` int NOT NULL AUTO_INCREMENT,
  `agua_usuario_cargo_id` int NOT NULL,
  `recibo_id` int NOT NULL,
  `monto_aplicado` decimal(10,2) NOT NULL,
  `fecha_pago` datetime(6) DEFAULT NULL,
  `estatus` int DEFAULT '1',
  `user_id_add` int DEFAULT NULL,
  `date_add` datetime(6) DEFAULT NULL,
  `user_id_update` int DEFAULT NULL,
  `date_update` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`agua_usuario_cargo_pago_id`),
  KEY `FK_cargopago_cargo` (`agua_usuario_cargo_id`),
  KEY `FK_cargopago_recibo` (`recibo_id`),
  CONSTRAINT `FK_cargopago_cargo` FOREIGN KEY (`agua_usuario_cargo_id`) REFERENCES `agua_usuario_cargo` (`agua_usuario_cargo_id`),
  CONSTRAINT `FK_cargopago_recibo` FOREIGN KEY (`recibo_id`) REFERENCES `agua_recibo` (`agua_recibo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================================
-- Catálogo de conceptos de cargo (reutiliza catalogo / catalogo_opciones,
-- que ya soporta CRUD completo vía /api/v1/catalog). Esto se puede hacer
-- directamente desde el módulo de Catálogos del frontend en lugar de
-- correr este SQL, pero se deja el script por si se prefiere cargar
-- directo en BD.
-- =====================================================================

-- 1) Crear el catálogo "padre" (ajustar si ya existe uno equivalente)
INSERT INTO catalogo (clave, nombre, descripcion, estatus, date_add)
VALUES ('CONCEPTO_CARGO_EXTRA', 'Conceptos de cargo extra', 'Conceptos para cargos adicionales/multas por usuario', 1, NOW());

-- 2) Agregar las opciones (usa el catalogo_id recién insertado)
SET @catalogo_id = LAST_INSERT_ID();

INSERT INTO catalogo_opciones (catalogo_id, nombre, descripcion, estatus, date_add) VALUES
(@catalogo_id, 'Multa',                         'Multa aplicada al usuario',                          1, NOW()),
(@catalogo_id, 'Interés',                        'Interés moratorio',                                  1, NOW()),
(@catalogo_id, 'Mano de obra corte',             'Mano de obra por corte de toma',                     1, NOW()),
(@catalogo_id, 'Mano de obra reconexión',        'Mano de obra por reconexión de toma',                1, NOW()),
(@catalogo_id, 'Insumos/material corte',         'Materiales usados para el corte',                    1, NOW()),
(@catalogo_id, 'Bastón',                         'Cargo por bastón',                                   1, NOW()),
(@catalogo_id, 'Llave azul',                     'Cargo por llave azul',                               1, NOW()),
(@catalogo_id, 'Reparaciones',                   'Reparaciones varias en la toma del usuario',         1, NOW());
