-- ESTA CONSULTA SE USA PARA OBTENER LOS RECIBOS POR MES
SELECT au.agua_usuario_id ,au.no_usuario,CONCAT( p.nombre,' ',COALESCE(p.nombre2,''),' ',COALESCE(p.app,''),' ',COALESCE(p.apm,'')) Usuario, 
 ar.no_folio,ar.fecha,ar.concepto,ar.total,
(
	SELECT  SUM(arp.monto_aplicado) monto
    FROM los_lopez_db.agua_recibo_pago arp
    WHERE arp.estatus = 1 AND ar.agua_recibo_id = arp.recibo_id AND arp.comite_id= 12 and arp.tipo_pago_id = 15 
) AS caja,
(
	SELECT  SUM(arp.monto_aplicado) monto
    FROM los_lopez_db.agua_recibo_pago arp
    WHERE arp.estatus = 1 AND ar.agua_recibo_id = arp.recibo_id AND arp.comite_id= 11 and arp.tipo_pago_id = 16 
) AS efectivo
FROM los_lopez_db.agua_usuario au 
INNER JOIN los_lopez_db.persona p ON p.persona_id = au.persona_id 
INNER JOIN los_lopez_db.cuota_monto cm on cm.cuota_monto_id = au.cuota_id 
INNER JOIN los_lopez_db.cuota c ON c.cuota_id = cm.cuota_id 
INNER JOIN los_lopez_db.agua_recibo ar ON ar.usuario_id = au.agua_usuario_id
WHERE ar.invalido IS NULL AND ar.no_folio > 4099 AND fecha BETWEEN '2026-02-01' AND '2026-02-28'-- and ar.no_folio <= 4905-- AND no_usuario =1
ORDER BY no_usuario, ar.fecha asc