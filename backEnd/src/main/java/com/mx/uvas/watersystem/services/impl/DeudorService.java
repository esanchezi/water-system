package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.DeudorDto;
import com.mx.uvas.watersystem.model.FeeAmountEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import com.mx.uvas.watersystem.repositories.IFeeAmountRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptPaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserAnnualPaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.DeudorRestResponse;
import com.mx.uvas.watersystem.services.IDeudorService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DeudorService implements IDeudorService {

    private static final Integer CONCEPTO_APORTACION_ID = 6;

    private final IWaterUserRepository waterUserRepository;
    private final IWaterUserAnnualPaymentRepository waterUserAnnualPaymentRepository;
    private final IWaterUserChargeRepository waterUserChargeRepository;
    private final IFeeAmountRepository feeAmountRepository;
    private final IWaterReceiptPaymentRepository waterReceiptPaymentRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<DeudorRestResponse> getDeudores(Integer anio) {
        DeudorRestResponse response = new DeudorRestResponse();
        try {
            int targetAnio = anio != null ? anio : LocalDate.now().getYear();
            int anioActual = LocalDate.now().getYear();

            // Usuarios con noUsuario = 0 son casas sin usuario real asignado
            // (placeholder); no se consideran deudores.
            List<WaterUserEntity> usuarios = waterUserRepository.findAllActiveWithHouse()
                    .stream()
                    .filter(u -> u.getNoUsuario() != null && u.getNoUsuario() != 0)
                    .collect(Collectors.toList());

            // Usuarios que ya tienen el año marcado como pagado (baja lógica de
            // la relación = estatus 1 vigente en agua_usuario_pago_anual).
            // Regla del usuario: si está aquí, ya no se revisa nada más de ese año.
            Set<Integer> usuariosConAnioPagado = waterUserAnnualPaymentRepository
                    .findByAnioAndEstatus(targetAnio, 1)
                    .stream()
                    .map(p -> p.getWaterUser().getAguaUsuarioId())
                    .collect(Collectors.toSet());

            // Si el año NO está marcado como pagado, lo abonado hasta ahora se
            // calcula sumando agua_recibo_pago.monto_aplicado con concepto_id=6
            // (Aportación) para ese año — mismo criterio que se usa en el Excel.
            Map<Integer, Double> montoPagadoPorUsuario = waterReceiptPaymentRepository
                    .findByConceptoAndAnio(CONCEPTO_APORTACION_ID, targetAnio)
                    .stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getWaterReceipt().getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(p -> p.getMontoAplicado() != null ? p.getMontoAplicado() : 0d)
                    ));

            // Monto de cuota vigente para el año, precargado una sola vez por
            // cuotaId (en vez de una consulta por usuario) — así el reporte
            // corre igual de rápido sin importar cuántos usuarios haya.
            Map<Integer, Double> montoCuotaPorCuotaId = feeAmountRepository
                    .findByVigencia(targetAnio)
                    .stream()
                    .filter(fa -> fa.getFee() != null)
                    .collect(Collectors.toMap(
                            fa -> fa.getFee().getCuotaId(),
                            fa -> fa.getCuota() != null ? fa.getCuota().doubleValue() : 0d,
                            (a, b) -> a // si hay duplicados históricos sin depurar, se queda con el primero
                    ));

            // Saldo pendiente de cargos/multas activos, sumado por usuario.
            Map<Integer, Double> cargosPendientesPorUsuario = waterUserChargeRepository
                    .findByEstatus(1)
                    .stream()
                    .filter(c -> c.getSaldo() > 0)
                    .collect(Collectors.groupingBy(
                            c -> c.getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(WaterUserChargeEntity::getSaldo)
                    ));

            List<DeudorDto> deudores = new ArrayList<>();

            for (WaterUserEntity user : usuarios) {
                Integer estatusComiteId = user.getEstatusComite() != null
                        ? user.getEstatusComite().getCatalogoOpcionesId()
                        : null;

                boolean anioMarcadoPagado = usuariosConAnioPagado.contains(user.getAguaUsuarioId());

                double montoCuotaAnio = user.getFee() != null
                        ? montoCuotaPorCuotaId.getOrDefault(user.getFee().getCuotaId(), 0d)
                        : 0d;

                double montoCuotaPagado;
                double montoCuotaPendiente;
                if (anioMarcadoPagado) {
                    montoCuotaPagado = montoCuotaAnio;
                    montoCuotaPendiente = 0d;
                } else {
                    montoCuotaPagado = montoPagadoPorUsuario.getOrDefault(user.getAguaUsuarioId(), 0d);
                    montoCuotaPendiente = Math.max(0d, montoCuotaAnio - montoCuotaPagado);
                }
                boolean cuotaPendiente = montoCuotaPendiente > 0;

                // Los cargos/multas no tienen año propio (son saldo vigente al día de
                // hoy), así que solo se suman al filtrar el año actual — de lo
                // contrario se verían repetidos en cada año histórico consultado.
                double montoCargos = (targetAnio == anioActual)
                        ? cargosPendientesPorUsuario.getOrDefault(user.getAguaUsuarioId(), 0d)
                        : 0d;
                double total = montoCuotaPendiente + montoCargos;

                if (total <= 0) continue;

                WaterHouseEntity casa = user.getWaterHouse();

                DeudorDto dto = new DeudorDto();
                dto.setAguaUsuarioId(user.getAguaUsuarioId());
                dto.setNoUsuario(user.getNoUsuario());
                dto.setNombreCompleto(buildNombreCompleto(user.getPerson()));
                dto.setCasaId(casa != null ? casa.getCasaId() : null);
                dto.setCasaNo(casa != null ? casa.getCasaNo() : null);
                dto.setCalleNombre(casa != null && casa.getCatCalle() != null ? casa.getCatCalle().getNombre() : null);
                dto.setEstatusComiteId(estatusComiteId);
                dto.setEstatusComiteNombre(user.getEstatusComite() != null ? user.getEstatusComite().getNombre() : null);
                dto.setAnio(targetAnio);
                dto.setCuotaPendiente(cuotaPendiente);
                dto.setMontoCuotaAnio(montoCuotaAnio);
                dto.setMontoCuotaPagado(montoCuotaPagado);
                dto.setMontoCuotaPendiente(montoCuotaPendiente);
                dto.setMontoCargosPendiente(montoCargos);
                dto.setMontoTotalPendiente(total);
                deudores.add(dto);
            }

            deudores.sort(
                    Comparator.comparing(DeudorDto::getCalleNombre, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(DeudorDto::getCasaNo, Comparator.nullsLast(Comparator.naturalOrder()))
            );

            response.setData(deudores);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Deudores encontrados");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular deudores", e);
        }
    }

    private String buildNombreCompleto(PersonEntity person) {
        if (person == null) return "";
        return String.join(" ",
                nullToEmpty(person.getNombre()),
                nullToEmpty(person.getNombre2()),
                nullToEmpty(person.getApp()),
                nullToEmpty(person.getApm())
        ).replaceAll("\\s+", " ").trim();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
