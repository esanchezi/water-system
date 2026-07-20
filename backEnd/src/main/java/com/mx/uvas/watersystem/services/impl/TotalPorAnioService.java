package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ConceptoResumenDto;
import com.mx.uvas.watersystem.dto.TotalPorAnioDto;
import com.mx.uvas.watersystem.dto.TotalPorAnioFilaDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterEgresoRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptPaymentRepository;
import com.mx.uvas.watersystem.response.TotalPorAnioRestResponse;
import com.mx.uvas.watersystem.services.ITotalPorAnioService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
@Slf4j
@AllArgsConstructor
public class TotalPorAnioService implements ITotalPorAnioService {

    // Catálogo "Conceptos de cobro"
    private static final Integer CATALOGO_CONCEPTOS_COBRO = 2;

    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterReceiptPaymentRepository waterReceiptPaymentRepository;
    private final IWaterEgresoRepository waterEgresoRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<TotalPorAnioRestResponse> getTotalesPorAnio() {
        TotalPorAnioRestResponse response = new TotalPorAnioRestResponse();
        try {
            List<CatalogOptionsEntity> conceptosCatalogo = catalogOptionsRepository
                    .findByCatalog_CatalogoIdAndEstatus(CATALOGO_CONCEPTOS_COBRO, 1);
            conceptosCatalogo.sort(Comparator.comparing(CatalogOptionsEntity::getNombre,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            List<ConceptoResumenDto> conceptos = new ArrayList<>();
            for (CatalogOptionsEntity c : conceptosCatalogo) {
                conceptos.add(new ConceptoResumenDto(c.getCatalogoOpcionesId(), c.getNombre()));
            }

            // Solo recibos válidos (no invalidados), en efectivo y a partir del
            // folio 4100, tal como el usuario los filtra en su reporte de
            // referencia.
            List<WaterReceiptPaymentEntity> pagos = waterReceiptPaymentRepository.findValidosParaTotalesPorAnio();

            // anio -> (conceptoId -> suma monto_aplicado)
            Map<Integer, Map<Integer, Double>> acumulado = new HashMap<>();

            for (WaterReceiptPaymentEntity pago : pagos) {
                if (pago.getWaterReceipt() == null || pago.getWaterReceipt().getFecha() == null) continue;
                if (pago.getCatConcepto() == null) continue;

                // Año en que se recaudó el pago (fecha del recibo), no el año de
                // la cuota que se está saldando (agua_recibo_pago.anio) — lo que
                // se busca aquí es el total efectivamente cobrado por año.
                int anio = pago.getWaterReceipt().getFecha().getYear();
                Integer conceptoId = pago.getCatConcepto().getCatalogoOpcionesId();
                double monto = pago.getMontoAplicado() != null ? pago.getMontoAplicado() : 0d;

                acumulado.computeIfAbsent(anio, k -> new HashMap<>())
                        .merge(conceptoId, monto, Double::sum);
            }

            // Egresos de nivel 1 (agua_egresos, nivel = 1), para sacar el
            // efectivo que debería haber por año: recaudado - egresos.
            Map<Integer, Double> egresosPorAnio = new HashMap<>();
            for (WaterEgresoEntity egreso : waterEgresoRepository.findNivel1Activos()) {
                if (egreso.getFechaPago() == null) continue;
                int anio = egreso.getFechaPago().getYear();
                double monto = egreso.getMonto() != null ? egreso.getMonto() : 0d;
                egresosPorAnio.merge(anio, monto, Double::sum);
            }

            // Un año puede tener egresos sin ingresos (o viceversa); se muestran
            // todos los años que aparezcan en cualquiera de las dos fuentes.
            TreeSet<Integer> anios = new TreeSet<>();
            anios.addAll(acumulado.keySet());
            anios.addAll(egresosPorAnio.keySet());

            List<TotalPorAnioFilaDto> filas = new ArrayList<>();
            Map<Integer, Double> totalesPorConcepto = new HashMap<>();
            for (ConceptoResumenDto c : conceptos) {
                totalesPorConcepto.put(c.getCatalogoOpcionesId(), 0d);
            }
            double granTotal = 0d;
            double granEgresos = 0d;

            for (Integer anio : anios) {
                Map<Integer, Double> montosDelAnio = acumulado.getOrDefault(anio, Map.of());

                TotalPorAnioFilaDto fila = new TotalPorAnioFilaDto();
                fila.setAnio(anio);

                Map<Integer, Double> montosPorConcepto = new HashMap<>();
                double totalAnio = 0d;
                for (ConceptoResumenDto c : conceptos) {
                    double monto = montosDelAnio.getOrDefault(c.getCatalogoOpcionesId(), 0d);
                    montosPorConcepto.put(c.getCatalogoOpcionesId(), monto);
                    totalAnio += monto;
                    totalesPorConcepto.merge(c.getCatalogoOpcionesId(), monto, Double::sum);
                }

                double egresosAnio = egresosPorAnio.getOrDefault(anio, 0d);

                fila.setMontosPorConcepto(montosPorConcepto);
                fila.setTotalAnio(totalAnio);
                fila.setEgresosAnio(egresosAnio);
                fila.setEfectivoEsperado(totalAnio - egresosAnio);
                filas.add(fila);

                granTotal += totalAnio;
                granEgresos += egresosAnio;
            }

            TotalPorAnioDto dto = new TotalPorAnioDto();
            dto.setConceptos(conceptos);
            dto.setFilas(filas);
            dto.setTotalesPorConcepto(totalesPorConcepto);
            dto.setGranTotal(granTotal);
            dto.setGranEgresos(granEgresos);

            response.setData(List.of(dto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Totales calculados");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular totales por año", e);
        }
    }
}
