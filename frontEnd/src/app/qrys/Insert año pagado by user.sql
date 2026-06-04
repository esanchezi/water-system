INSERT INTO agua_usuario_pago_anual 
(agua_usuario_id, anio, fecha_validacion, estatus)
SELECT 
    au.agua_usuario_id,
    anios.anio,
    CURDATE(),
    1
FROM agua_usuario au
JOIN (
 	SELECT 2021 AS anio
    UNION ALL SELECT 2022
    UNION ALL SELECT 2023
    UNION ALL SELECT 2024
    UNION ALL SELECT 2025
    UNION ALL SELECT 2026
) anios
LEFT JOIN agua_usuario_pago_anual ap
    ON ap.agua_usuario_id = au.agua_usuario_id
    AND ap.anio = anios.anio
WHERE au.no_usuario in(192,193) 
AND ap.agua_usuario_id IS NULL;


