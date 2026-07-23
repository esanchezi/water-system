-- Los catálogos CONCEPTOS_EGRESO (9) y TIPO_COMPROBANTE_EGRESO (10) ya
-- existen desde 2023, con estas opciones:
--   CONCEPTOS_EGRESO: Luz, Nomina, Mantenimiento, Papeleria, Viaticos, Varios
--   TIPO_COMPROBANTE_EGRESO: Factura, Nota, Vale caja, Recibo nomina, Remisión
-- Solo falta agregar Obras y Cortes a CONCEPTOS_EGRESO.

INSERT INTO catalogo_opciones (nombre, descripcion, estatus, catalogo_id, user_id_add, date_add)
VALUES
  ('Obras', 'Identificar egresos', 1, 9, 1, NOW()),
  ('Cortes', 'Identificar egresos', 1, 9, 1, NOW());

-- Verificar
SELECT catalogo_opciones_id, nombre FROM catalogo_opciones WHERE catalogo_id = 9 ORDER BY nombre;

-- La columna legacy `id` de agua_egresos viene vacía/0 en todos los
-- registros existentes (confirmado). Se puede eliminar con confianza:
ALTER TABLE agua_egresos DROP COLUMN id;
