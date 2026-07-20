package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IWaterReceiptPaymentRepository extends JpaRepository <WaterReceiptPaymentEntity,Integer>{

    // Pagos de "Aportación" (concepto_id = 6) aplicados a un año en particular,
    // usados para saber cuánto se ha abonado a la cuota de ese año cuando
    // todavía no está marcado como pagado en agua_usuario_pago_anual.
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
}
