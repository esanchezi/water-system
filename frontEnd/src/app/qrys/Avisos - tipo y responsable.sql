-- =====================================================================
-- AVISOS: distinguir Aviso / Nota / Pendiente, y de quién es el pendiente
-- =====================================================================
-- Se agregan dos columnas a agua_usuario_aviso, ambas FK a
-- catalogo_opciones (mismo patrón que aviso_estatus_id que ya existía):
--
-- tipo_id: qué es el registro -> Aviso, Nota o Pendiente.
-- responsable_id: solo aplica cuando tipo = Pendiente -> Comité o Usuario
--                 (de quién es la acción pendiente). Nullable porque un
--                 Aviso o una Nota no necesitan responsable.
-- =====================================================================

ALTER TABLE `agua_usuario_aviso`
  ADD COLUMN `tipo_id` int DEFAULT NULL AFTER `aviso`,
  ADD COLUMN `responsable_id` int DEFAULT NULL AFTER `aviso_estatus_id`,
  ADD CONSTRAINT `FK_aviso_tipo` FOREIGN KEY (`tipo_id`) REFERENCES `catalogo_opciones` (`catalogo_opciones_id`),
  ADD CONSTRAINT `FK_aviso_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `catalogo_opciones` (`catalogo_opciones_id`);

-- Catálogo de tipo de aviso
INSERT INTO catalogo (clave, nombre, descripcion, estatus, date_add)
VALUES ('TIPO_AVISO', 'Tipo de aviso', 'Clasifica si el registro es un aviso, una nota o un pendiente', 1, NOW());

SET @catalogo_tipo_aviso_id = LAST_INSERT_ID();

INSERT INTO catalogo_opciones (catalogo_id, nombre, descripcion, estatus, date_add) VALUES
(@catalogo_tipo_aviso_id, 'Aviso',     'Aviso o notificación al usuario', 1, NOW()),
(@catalogo_tipo_aviso_id, 'Nota',      'Nota interna, sin necesariamente avisar al usuario', 1, NOW()),
(@catalogo_tipo_aviso_id, 'Pendiente', 'Pendiente entre el comité y el usuario', 1, NOW());

-- Catálogo de responsable del pendiente
INSERT INTO catalogo (clave, nombre, descripcion, estatus, date_add)
VALUES ('RESPONSABLE_PENDIENTE', 'Responsable del pendiente', 'De quién es la acción pendiente: comité o usuario', 1, NOW());

SET @catalogo_responsable_id = LAST_INSERT_ID();

INSERT INTO catalogo_opciones (catalogo_id, nombre, descripcion, estatus, date_add) VALUES
(@catalogo_responsable_id, 'Comité',  'El pendiente le corresponde al comité', 1, NOW()),
(@catalogo_responsable_id, 'Usuario', 'El pendiente le corresponde al usuario', 1, NOW());

-- Catálogo de estatus del aviso (esto se nos había quedado pendiente:
-- la columna aviso_estatus_id ya existía desde antes, pero nunca tenía
-- catálogo con opciones cargadas, por eso el combo se veía vacío).
INSERT INTO catalogo (clave, nombre, descripcion, estatus, date_add)
VALUES ('ESTATUS_AVISO', 'Estatus de aviso', 'Seguimiento del aviso/nota/pendiente', 1, NOW());

SET @catalogo_estatus_aviso_id = LAST_INSERT_ID();

INSERT INTO catalogo_opciones (catalogo_id, nombre, descripcion, estatus, date_add) VALUES
(@catalogo_estatus_aviso_id, 'Pendiente', 'Aún no se atiende', 1, NOW()),
(@catalogo_estatus_aviso_id, 'Atendido',  'Ya se atendió/resolvió', 1, NOW()),
(@catalogo_estatus_aviso_id, 'Cerrado',   'Cerrado, sin más seguimiento', 1, NOW());
