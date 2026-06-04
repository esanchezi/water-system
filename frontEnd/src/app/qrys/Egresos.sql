
-- TOTAL MES EGRESOS
SET lc_time_names = 'es_ES';
SELECT MONTHNAME(e.fecha_pago) mes,SUM(e.monto) egreso
FROM los_lopez_db.agua_egresos e
WHERE e.estatus = 1  and e.fk_id_egresos =0
GROUP BY MONTHNAME (e.fecha_pago);

-- EGRESOS POR CATEGORIA 
SELECT con.nombre categoria,MONTHNAME(e.fecha_pago) mes,SUM(e.monto) egreso
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
WHERE e.estatus = 1 
GROUP BY con.nombre, MONTHNAME (e.fecha_pago);

-- EGRESOS POR CATEGORIA Y MES
SELECT con.nombre,SUM(e.monto) egreso
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
WHERE e.estatus = 1 AND e.fecha_pago  BETWEEN '2023-05-01' AND '2023-06-30'
GROUP BY con.nombre;

-- EGRESOS POR CATEGORIA,DETALLE Y MES
SELECT con.nombre,e.fk_id_persona,e.fk_tipo_comprobante ,SUM(e.monto) egreso
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
WHERE e.estatus = 1 AND e.fecha_pago  BETWEEN '2023-05-01' AND '2023-06-30'
GROUP BY con.nombre,e.fk_id_persona,e.fk_tipo_comprobante 



-- EGRESOS POR VALE
SELECT con.nombre tipo,CONCAT(p.nombre,' ',p.nombre2 ,' ',p.app) Persona,com.nombre comprobante,e.no_folio,e.monto egreso
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
INNER JOIN los_lopez_db.persona p ON p.id = e.fk_id_persona 
INNER JOIN los_lopez_db.catalogo_opciones com ON com.id  = e.fk_tipo_comprobante 
WHERE e.estatus = 1 AND e.id in (20,24)	
GROUP BY con.nombre,e.fk_id_persona,e.fk_tipo_comprobante 

-- EGRESOS POR DETALLE VALE
SELECT con.nombre tipo,CONCAT(p.nombre,' ',p.nombre2 ,' ',p.app) Persona,com.nombre comprobante,e.no_folio,e.monto egreso,
d.monto,d.descripcion 
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
INNER JOIN los_lopez_db.persona p ON p.id = e.fk_id_persona 
INNER JOIN los_lopez_db.catalogo_opciones com ON com.id  = e.fk_tipo_comprobante 
INNER JOIN los_lopez_db.agua_egresos d ON d.fk_id_egresos = e.id 
WHERE e.estatus = 1 AND e.id in (25)	
GROUP BY con.nombre ,p.nombre,p.nombre2 ,p.app,com.nombre,e.no_folio,d.monto,d.descripcion,e.monto


-- 
SELECT con.nombre categoria,e.fk_id_egresos ,e.descripcion ,MONTHNAME(e.fecha_pago) mes,e.monto egreso
FROM los_lopez_db.agua_egresos e
INNER JOIN los_lopez_db.catalogo_opciones con ON con.id  = e.fk_id_concepto 
WHERE e.estatus = 1 




-- usuarios por calle
Select+




-- los_lopez_db.agua_usuario definition

CREATE TABLE `agua_usuario` (
  `agua_usuario_year_id` int NOT NULL AUTO_INCREMENT,
  `agua_usuario_id` int NOT NULL,
  `2025` int NOT NULL,

  `estatus` int DEFAULT '1',
  `user_id_add` int DEFAULT NULL,
  `date_add` datetime(6) DEFAULT NULL,
  `user_id_update` int DEFAULT NULL,
  `date_update` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`agua_usuario_id`),
) ENGINE=InnoDB AUTO_INCREMENT=735 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


