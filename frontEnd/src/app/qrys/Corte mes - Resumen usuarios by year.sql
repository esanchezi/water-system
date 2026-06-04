SELECT 
    au.agua_usuario_id,
    au.no_usuario,
    CONCAT(p.nombre,' ',COALESCE(p.nombre2,''),' ',COALESCE(p.app,''),' ',COALESCE(p.apm,'')) AS Usuario, 
    co.nombre,
    cm.cuota,
    -- au.nombre,
    /* ===== 2021 ===== */
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2021 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2021 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2021,
    /* ===== 2022 ===== */
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2022 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2022 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2022,
    /* ===== 2023 ===== */
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2023 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2023 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2023,
    /* ===== 2024 ===== */
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2024 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2024 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2024,
    /* ===== 2025 ===== */
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2025 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2025 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2025,
    /* ===== 2026 ===== */
    CASE
        WHEN EXISTS (
            SELECT 1 FROM agua_usuario_pago_anual ap 
            WHERE ap.agua_usuario_id = au.agua_usuario_id 
            AND ap.anio = 2026 
            AND ap.estatus = 1
        )
        THEN 'PAGADO'
        ELSE CAST(COALESCE(SUM(CASE WHEN arp.anio = 2026 THEN arp.monto_aplicado END),0) AS CHAR)
    END AS anio_2026
FROM agua_usuario au
INNER JOIN los_lopez_db.persona p ON p.persona_id = au.persona_id 
INNER JOIN los_lopez_db.cuota_monto cm ON cm.cuota_monto_id = au.cuota_id 
INNER JOIN los_lopez_db.cuota c ON c.cuota_id = cm.cuota_id
INNER JOIN los_lopez_db.catalogo_opciones co ON co.catalogo_opciones_id = au.estatus_comites_id 
LEFT JOIN agua_recibo ar 
    ON ar.usuario_id = au.agua_usuario_id
    AND ar.invalido IS NULL
LEFT JOIN agua_recibo_pago arp
    ON arp.recibo_id = ar.agua_recibo_id
    AND arp.concepto_id IN (6,79)
WHERE 
    (au.estatus_comites_id != 102 OR au.estatus_comites_id IS NULL)
     AND au.no_usuario not IN (0)
    -- AND au.no_usuario IN (502,503,70,729,238,761,419,421)
GROUP BY 
    au.agua_usuario_id,
    au.no_usuario,
    p.nombre, p.nombre2, p.app, p.apm
ORDER BY 
    au.no_usuario;