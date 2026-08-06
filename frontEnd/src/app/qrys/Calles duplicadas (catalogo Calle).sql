-- =====================================================================
-- CALLES DUPLICADAS EN EL CATALOGO "Calle" (catalogo_id = 15)
-- =====================================================================
-- El selector de calle en "Dar de alta casa" (y en el detalle de usuario)
-- se llena desde catalogo_opciones donde catalogo_id = 15 y estatus = 1.
-- Si aparece una calle repetida en el dropdown, es porque hay más de un
-- renglón activo en catalogo_opciones con el mismo nombre (no es un bug
-- de filtrado -- el query que arma el dropdown ya excluye los inactivos).
-- =====================================================================

-- 1) Ver todas las calles activas, para detectar cuáles se repiten
SELECT catalogo_opciones_id, nombre, estatus
FROM catalogo_opciones
WHERE catalogo_id = 15 AND estatus = 1
ORDER BY nombre, catalogo_opciones_id;

-- 2) Mismo query pero filtrando solo los nombres que sí están duplicados
SELECT nombre, COUNT(*) AS veces, GROUP_CONCAT(catalogo_opciones_id ORDER BY catalogo_opciones_id) AS ids
FROM catalogo_opciones
WHERE catalogo_id = 15 AND estatus = 1
GROUP BY nombre
HAVING COUNT(*) > 1;

-- 3) Antes de dar de baja un duplicado, ver si alguna casa ya lo está
--    usando (para no dejar casas "huérfanas" apuntando a un id inactivo)
-- Reemplaza <ID_DUPLICADO> por cada id que salga en el query 2.
SELECT casa_id, casa_no, nombre, calle_id
FROM agua_casa
WHERE calle_id = <ID_DUPLICADO>;

-- =====================================================================
-- MERGE: una vez identificado cuál id te quedas (<ID_A_CONSERVAR>) y
-- cuál sobra (<ID_DUPLICADO>), corre esto en ese orden:
-- =====================================================================

-- 4) Reasignar las casas que apuntaban al duplicado, al id que se conserva
UPDATE agua_casa
SET calle_id = <ID_A_CONSERVAR>
WHERE calle_id = <ID_DUPLICADO>;

-- 5) Dar de baja el duplicado (baja lógica -- no se borra el renglón)
UPDATE catalogo_opciones
SET estatus = 0
WHERE catalogo_opciones_id = <ID_DUPLICADO>;

-- Repite los pasos 3-5 por cada nombre duplicado que haya salido en el query 2.
