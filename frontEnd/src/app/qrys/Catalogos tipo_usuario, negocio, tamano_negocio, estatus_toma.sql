-- Diagnóstico: qué opciones existen hoy en los catálogos que se van a usar
-- para reemplazar familiaCompleta/viudoPadreMadreSoltero (TIPO_USUARIO),
-- negocioGrande (TAMANO_NEGOCIO), arreglar el giro de negocio (NEGOCIO), y
-- decidir si el gating del censo también debe considerar estatusToma.

-- 1. Opciones activas por catálogo (ajusta el/los catalogo_maestro_id si tus
--    ids reales no son 6/7/8/14 -- usa la tabla de abajo para confirmarlos).
SELECT cm.catalogo_id AS catalogo_maestro_id, cm.clave, cm.nombre AS catalogo_nombre,
       co.catalogo_opciones_id, co.nombre AS opcion_nombre, co.estatus
FROM catalogo_opciones co
JOIN catalogo_maestro cm ON cm.catalogo_id = co.catalogo_id
WHERE cm.catalogo_id IN (6, 7, 8, 14)
ORDER BY cm.catalogo_id, co.nombre;

-- 2. Por si los ids no son exactamente 6/7/8/14, confirma las claves reales.
SELECT catalogo_id, clave, nombre, descripcion
FROM catalogo_maestro
WHERE clave IN ('TIPO_USUARIO', 'NEGOCIO', 'TAMANO_NEGOCIO', 'ESTATUS_TOMA');

-- 3. Cuántos usuarios ya tienen datos capturados en los campos que se van a
--    reemplazar -- para dimensionar la migración de familiaCompleta /
--    viudoPadreMadreSoltero / negocioGrande a las nuevas columnas FK.
SELECT
  SUM(CASE WHEN familia_completa = 1 THEN 1 ELSE 0 END)          AS familia_completa,
  SUM(CASE WHEN viudo_padre_madre_soltero = 1 THEN 1 ELSE 0 END) AS viudo_o_solo,
  SUM(CASE WHEN negocio_grande = 1 THEN 1 ELSE 0 END)            AS negocio_grande,
  SUM(CASE WHEN habita_domicilio = 0 THEN 1 ELSE 0 END)          AS casas_sin_habitar,
  COUNT(*)                                                        AS total_usuarios
FROM agua_usuario;
