-- =====================================================================
-- USUARIOS SIN CALLE (via casa) -- para saber a quiénes falta capturar
-- =====================================================================
-- Ojo: hay dos "calle" distintas en el sistema:
--  1) agua_direccion.calle  -> texto libre, se captura arriba en el
--     formulario del usuario (Sección/Calle/Número/Referencia).
--  2) agua_casa.calle_id    -> catálogo (dropdown), se asigna en el
--     acordeón "Domicilio / Casa" y es la que alimenta el mapa y el
--     resumen del censo por zona/calle.
-- Este query revisa la #2, que es la que importa para el reporte de censo.
-- =====================================================================

SELECT
    au.agua_usuario_id,
    au.no_usuario,
    CONCAT(p.nombre, ' ', COALESCE(p.nombre2, ''), ' ', p.app, ' ', COALESCE(p.apm, '')) AS usuario,
    au.casa_id,
    c.calle_id,
    co.nombre AS calle_actual
FROM agua_usuario au
JOIN persona p ON p.persona_id = au.persona_id
LEFT JOIN agua_casa c ON c.casa_id = au.casa_id
LEFT JOIN catalogo_opciones co ON co.catalogo_opciones_id = c.calle_id
WHERE au.estatus = 1
  AND (au.casa_id IS NULL OR c.calle_id IS NULL)
ORDER BY au.no_usuario;
