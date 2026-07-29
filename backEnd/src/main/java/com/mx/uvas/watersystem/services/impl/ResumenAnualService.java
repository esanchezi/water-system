package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ResumenAnualCategoriaMesDto;
import com.mx.uvas.watersystem.dto.ResumenAnualDto;
import com.mx.uvas.watersystem.dto.ResumenAnualFilaDto;
import com.mx.uvas.watersystem.dto.ResumenAnualMesDto;
import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import com.mx.uvas.watersystem.repositories.IWaterEgresoRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptPaymentRepository;
import com.mx.uvas.watersystem.response.ResumenAnualRestResponse;
import com.mx.uvas.watersystem.services.IResumenAnualService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Reproduce la pestaña "Resumen" que la usuaria lleva a mano en Google
// Sheets: por año, un resumen de caja (inicial/recibos caja/efectivo/
// egresos/saldo) y, dentro de ese año, el desglose mes a mes y por
// categoría de egreso (Luz vs Mantenimiento). Pantalla nueva, separada de
// "Totales por año" (que sigue igual, solo cuenta efectivo real).
@Service
@Slf4j
@AllArgsConstructor
public class ResumenAnualService implements IResumenAnualService {

    // tipo_pago_id en agua_recibo_pago: 16 = Efectivo (dinero real que
    // entra), 15 = Recibos Caja / Caja Popular (se cambiaban después, no es
    // efectivo disponible de inmediato). Mismos ids que ya usa "Totales por
    // año" para el 16; pendiente de confirmación explícita para el 15.
    private static final int TIPO_PAGO_EFECTIVO = 16;
    private static final int TIPO_PAGO_CAJA = 15;

    // Saldo de caja con el que arrancó el control (mayo 2023, según la hoja
    // de origen). No hay ningún año anterior registrado en el sistema; si
    // algún día se captura uno, este valor tendría que moverse a ese año.
    private static final double SALDO_INICIAL_HISTORICO = 11101.00;

    private static final String[] NOMBRES_MES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final IWaterReceiptPaymentRepository waterReceiptPaymentRepository;
    private final IWaterEgresoRepository waterEgresoRepository;

    // Acumulador de un mes en particular: cuánto entró (por tipo de pago) y
    // cuánto salió (por categoría de egreso).
    private static class MesAcumulado {
        double recibosCaja = 0d;
        double efectivo = 0d;
        double egresosLuz = 0d;
        double egresosMantenimiento = 0d;

        double egresos() {
            return egresosLuz + egresosMantenimiento;
        }

        double totalMes() {
            return recibosCaja + efectivo;
        }
    }

    private boolean esLuz(String conceptoNombre) {
        return conceptoNombre != null && conceptoNombre.trim().equalsIgnoreCase("luz");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResumenAnualRestResponse> getResumenAnual() {
        ResumenAnualRestResponse response = new ResumenAnualRestResponse();
        try {
            // anio -> mes -> acumulado. TreeMap para iterar en orden
            // ascendente (necesario para arrastrar el saldo de un año al
            // siguiente).
            Map<Integer, TreeMap<Integer, MesAcumulado>> porAnioMes = new TreeMap<>();

            for (WaterReceiptPaymentEntity pago : waterReceiptPaymentRepository.findValidosParaResumenAnual()) {
                if (pago.getWaterReceipt() == null || pago.getWaterReceipt().getFecha() == null) continue;
                if (pago.getCatTiPag() == null) continue;

                LocalDate fecha = pago.getWaterReceipt().getFecha();
                MesAcumulado acc = porAnioMes
                        .computeIfAbsent(fecha.getYear(), k -> new TreeMap<>())
                        .computeIfAbsent(fecha.getMonthValue(), k -> new MesAcumulado());

                double monto = pago.getMontoAplicado() != null ? pago.getMontoAplicado() : 0d;
                int tipoPagoId = pago.getCatTiPag().getCatalogoOpcionesId();
                if (tipoPagoId == TIPO_PAGO_EFECTIVO) {
                    acc.efectivo += monto;
                } else if (tipoPagoId == TIPO_PAGO_CAJA) {
                    acc.recibosCaja += monto;
                }
            }

            for (WaterEgresoEntity cabecera : waterEgresoRepository.findCabecerasActivas()) {
                if (cabecera.getFechaPago() == null) continue;

                MesAcumulado acc = porAnioMes
                        .computeIfAbsent(cabecera.getFechaPago().getYear(), k -> new TreeMap<>())
                        .computeIfAbsent(cabecera.getFechaPago().getMonthValue(), k -> new MesAcumulado());

                if (cabecera.getLineas() != null && !cabecera.getLineas().isEmpty()) {
                    for (WaterEgresoEntity linea : cabecera.getLineas()) {
                        if (linea.getEstatus() == null || linea.getEstatus() != 1) continue;
                        double monto = linea.getMonto() != null ? linea.getMonto() : 0d;
                        String nombre = linea.getConcepto() != null ? linea.getConcepto().getNombre() : null;
                        if (esLuz(nombre)) {
                            acc.egresosLuz += monto;
                        } else {
                            acc.egresosMantenimiento += monto;
                        }
                    }
                } else {
                    // Vale histórico sin líneas: el concepto va directo en la cabecera.
                    double monto = cabecera.getMonto() != null ? cabecera.getMonto() : 0d;
                    String nombre = cabecera.getConcepto() != null ? cabecera.getConcepto().getNombre() : null;
                    if (esLuz(nombre)) {
                        acc.egresosLuz += monto;
                    } else {
                        acc.egresosMantenimiento += monto;
                    }
                }
            }

            List<ResumenAnualFilaDto> filas = new ArrayList<>();
            double saldoAnterior = SALDO_INICIAL_HISTORICO;

            for (Map.Entry<Integer, TreeMap<Integer, MesAcumulado>> anioEntry : porAnioMes.entrySet()) {
                double inicial = saldoAnterior;
                double totalRecibosCaja = 0d;
                double totalEfectivo = 0d;
                double totalEgresos = 0d;

                List<ResumenAnualMesDto> mesesDto = new ArrayList<>();
                List<ResumenAnualCategoriaMesDto> categoriasDto = new ArrayList<>();

                for (Map.Entry<Integer, MesAcumulado> mesEntry : anioEntry.getValue().entrySet()) {
                    int mes = mesEntry.getKey();
                    MesAcumulado acc = mesEntry.getValue();
                    String mesNombre = NOMBRES_MES[mes - 1];

                    ResumenAnualMesDto mesDto = new ResumenAnualMesDto();
                    mesDto.setMes(mes);
                    mesDto.setMesNombre(mesNombre);
                    mesDto.setRecibosCaja(acc.recibosCaja);
                    mesDto.setEfectivo(acc.efectivo);
                    mesDto.setEgresos(acc.egresos());
                    mesDto.setTotalMes(acc.totalMes());
                    mesesDto.add(mesDto);

                    ResumenAnualCategoriaMesDto catDto = new ResumenAnualCategoriaMesDto();
                    catDto.setMes(mes);
                    catDto.setMesNombre(mesNombre);
                    catDto.setLuz(acc.egresosLuz);
                    catDto.setMantenimiento(acc.egresosMantenimiento);
                    categoriasDto.add(catDto);

                    totalRecibosCaja += acc.recibosCaja;
                    totalEfectivo += acc.efectivo;
                    totalEgresos += acc.egresos();
                }

                double saldoCaja = inicial + totalEfectivo - totalEgresos;

                ResumenAnualFilaDto fila = new ResumenAnualFilaDto();
                fila.setAnio(anioEntry.getKey());
                fila.setInicial(inicial);
                fila.setRecibosCaja(totalRecibosCaja);
                fila.setEfectivo(totalEfectivo);
                fila.setEgresos(totalEgresos);
                fila.setSaldoCaja(saldoCaja);
                fila.setMeses(mesesDto);
                fila.setCategorias(categoriasDto);
                filas.add(fila);

                saldoAnterior = saldoCaja;
            }

            // Año más reciente primero (igual que el resto de los reportes de Egresos).
            Collections.reverse(filas);

            ResumenAnualDto dto = new ResumenAnualDto();
            dto.setFilas(filas);

            response.setData(List.of(dto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen anual calculado");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular el resumen anual", e);
        }
    }
}
