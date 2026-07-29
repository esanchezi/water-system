SELECT agua_egresos_id, no_folio, fecha_pago, monto, concepto_id, descripcion
FROM agua_egresos
WHERE nivel = 1 AND padre_id IS NULL AND estatus = 1
ORDER BY fecha_pago ASC, agua_egresos_id ASC;

R202607225438