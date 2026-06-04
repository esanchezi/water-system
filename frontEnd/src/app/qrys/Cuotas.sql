-- Cuotas
SELECT  c.uso_id ,co.nombre uso, c.usuario_tipo_id, cot.nombre ,cm.cuota_monto_id ,cm.cuota 
FROM los_lopez_db.cuota c 
INNER JOIN los_lopez_db.cuota_monto cm ON cm.cuota_id = c.cuota_id 
INNER JOIN los_lopez_db.catalogo_opciones co ON co.catalogo_opciones_id = c.uso_id 
INNER JOIN los_lopez_db.catalogo_opciones cot ON cot.catalogo_opciones_id = c.usuario_tipo_id 
