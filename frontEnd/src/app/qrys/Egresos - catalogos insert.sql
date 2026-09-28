SELECT p.preregistro_id, p.nombre, p.estatus, p.es_negocio, p.giro_negocio_id, gp.nombre AS giro
FROM agua_preregistro p
LEFT JOIN catalogo_opciones gp ON gp.catalogo_opciones_id = p.giro_negocio_id
WHERE p.es_negocio = 1 AND p.estatus <> 2;