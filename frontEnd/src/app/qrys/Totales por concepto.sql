-- USUARIOS QUE HAN DADO COOPERACION POZO
SELECT au.agua_usuario_id,au.no_usuario,CONCAT( p.nombre, ' ',COALESCE(p.nombre2, ''), ' ', COALESCE(p.app, ''), ' ',COALESCE(p.apm, '') ) AS Usuario, au.observaciones,
    SUM(arp.monto_aplicado) AS efectivo
FROM los_lopez_db.agua_recibo_pago arp
INNER JOIN los_lopez_db.agua_recibo ar 
    ON ar.agua_recibo_id = arp.recibo_id
INNER JOIN los_lopez_db.agua_usuario au 
    ON au.agua_usuario_id = ar.usuario_id
INNER JOIN los_lopez_db.persona p 
    ON p.persona_id = au.persona_id
WHERE 
    arp.estatus = 1 AND arp.comite_id = 11 AND arp.tipo_pago_id = 16 AND arp.concepto_id = 100
    AND ar.invalido IS NULL AND ar.no_folio > 4099 AND ar.fecha BETWEEN '2024-01-01' AND '2025-12-31'
GROUP BY 
    au.agua_usuario_id, 
    au.no_usuario, 
    Usuario, 
    au.observaciones
ORDER BY 
    au.no_usuario;

-- USUARIOS QUE HAN DADO COOPERACION MTTO RED
SELECT au.agua_usuario_id,au.no_usuario,CONCAT( p.nombre, ' ',COALESCE(p.nombre2, ''), ' ', COALESCE(p.app, ''), ' ',COALESCE(p.apm, '') ) AS Usuario, au.observaciones,
    SUM(arp.monto_aplicado) AS efectivo
FROM los_lopez_db.agua_recibo_pago arp
INNER JOIN los_lopez_db.agua_recibo ar 
    ON ar.agua_recibo_id = arp.recibo_id
INNER JOIN los_lopez_db.agua_usuario au 
    ON au.agua_usuario_id = ar.usuario_id
INNER JOIN los_lopez_db.persona p 
    ON p.persona_id = au.persona_id
WHERE 
    arp.estatus = 1 AND arp.comite_id = 11 AND arp.tipo_pago_id = 16 AND arp.concepto_id = 108 -- AND arp.anio =2024
    AND ar.invalido IS NULL AND ar.no_folio > 4099 AND ar.fecha BETWEEN '2024-01-01' AND '2026-12-31'
GROUP BY 
    au.agua_usuario_id, 
    au.no_usuario, 
    Usuario, 
    au.observaciones
ORDER BY 
    au.no_usuario;