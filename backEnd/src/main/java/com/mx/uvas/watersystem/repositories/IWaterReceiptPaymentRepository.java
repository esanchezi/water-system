package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IWaterReceiptPaymentRepository extends JpaRepository <WaterReceiptPaymentEntity,Integer>{

    // Pagos de "Aportación" (concepto_id = 6) aplicados a un año en particular,
    // usados para saber cuánto se ha abonado a la cuota de ese año cuando
    // todavía no está marcado como pagado en agua_usuario_pago_anual. Es
    // para cálculo de ADEUDO (no para corte de caja).
    //
    // v10 (sept. 2026, corrección explícita de Ely -- caso usuario 11, José
    // Hilario Rocha Rodríguez, folio 5906): antes este query también excluía
    // los recibos marcados r.invalido, con el razonamiento de que "un pago
    // invalidado no debe contar como que el usuario ya pagó". Ely aclaró que
    // ese criterio es incorrecto: invalido SOLO debe excluirse del corte de
    // caja y los reportes de dinero (ver findValidosParaTotalesPorAnio /
    // findValidosParaResumenAnual, que sí lo siguen filtrando) -- para todo
    // lo demás, incluyendo el cálculo de adeudo, un recibo con invalido
    // marcado sigue siendo un pago válido del usuario. Se quita el filtro
    // aquí; solo se respeta r.estatus = 1 (recibo cancelado por folio, ver
    // ReceiptCancelledComponent, ese sí no debe contar nunca).
    // No se filtra por p.estatus: igual que en el resto del historial de
    // recibos, ese campo puede venir nulo en pagos históricos.
    @Query("SELECT p FROM WaterReceiptPaymentEntity p " +
            "JOIN FETCH p.waterReceipt r " +
            "JOIN FETCH r.waterUser wu " +
            "WHERE p.catConcepto.catalogoOpcionesId = :conceptoId " +
            "AND p.anio = :anio " +
            "AND r.estatus = 1")
    List<WaterReceiptPaymentEntity> findByConceptoAndAnio(@Param("conceptoId") Integer conceptoId, @Param("anio") Integer anio);

    // Pagos válidos para el reporte de "Totales por año y concepto": recibos
    // no invalidados (agua_recibo.invalido IS NULL), con folio > 4099 y en
    // efectivo (tipo_pago_id = 16) — para poder comparar contra egresos
    // (siempre en efectivo) y sacar el efectivo que debería haber. Se
    // agrupan en el servicio por el año de r.fecha (cuándo se recaudó) y
    // por concepto.
    @Query("SELECT p FROM WaterReceiptPaymentEntity p " +
            "JOIN FETCH p.waterReceipt r " +
            "WHERE r.invalido IS NULL " +
            "AND r.noFolio > 4099 " +
            "AND p.catTiPag.catalogoOpcionesId = 16")
    List<WaterReceiptPaymentEntity> findValidosParaTotalesPorAnio();

    // Igual que findValidosParaTotalesPorAnio pero SIN restringir tipo_pago:
    // el Resumen anual necesita separar Recibos Caja (Caja Popular) de
    // Efectivo, no solo sumar el efectivo real.
    @Query("SELECT p FROM WaterReceiptPaymentEntity p " +
            "JOIN FETCH p.waterReceipt r " +
            "WHERE r.invalido IS NULL " +
            "AND r.noFolio > 4099")
    List<WaterReceiptPaymentEntity> findValidosParaResumenAnual();

    // v10 (sept. 2026, pedido de Ely): ¿este usuario ya dio la cooperación de
    // mantenimiento ("Mtto", concepto_id = 108) para este año, como pago
    // suelto dentro de un recibo normal? Se usa en AvisoAdeudoService para
    // NO generar el cargo formal "Mantenimiento de cajón" si la cajera ya lo
    // cobró por este camino -- antes eran dos vías totalmente desconectadas
    // (ver caso real: folio 7775, "Multa por corte" cobrada como concepto
    // suelto nunca bajaba el saldo del cargo formal de multa; mismo patrón
    // aplicaba a mantenimiento). Mismo criterio que findByConceptoAndAnio:
    // invalido NO excluye (eso es solo para corte de caja/reportes), pero
    // r.estatus = 1 sí (recibo cancelado por folio nunca cuenta).
    @Query("SELECT COUNT(p) > 0 FROM WaterReceiptPaymentEntity p " +
            "JOIN p.waterReceipt r " +
            "WHERE p.catConcepto.catalogoOpcionesId = :conceptoId " +
            "AND p.anio = :anio " +
            "AND r.waterUser.aguaUsuarioId = :usuarioId " +
            "AND r.estatus = 1")
    boolean existsByConceptoAndAnioAndUsuario(@Param("conceptoId") Integer conceptoId, @Param("anio") Integer anio, @Param("usuarioId") Integer usuarioId);
}
