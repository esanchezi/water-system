package com.mx.uvas.watersystem.helpers;

import com.mx.uvas.watersystem.dto.WaterUserChargeDto;
import com.mx.uvas.watersystem.dto.WaterUserChargePaymentDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserChargePaymentEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Component
@AllArgsConstructor
public class WaterUserChargeHelper {

    private final IWaterReceiptRepository waterReceiptRepository;

    public WaterUserChargeEntity buildEntity(WaterUserChargeDto request, WaterUserEntity user, CatalogOptionsEntity concepto) {
        return WaterUserChargeEntity.builder()
                .waterUser(user)
                .concepto(concepto)
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .fecha(request.getFecha())
                .comentario(request.getComentario())
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    // Un abono siempre va ligado a un recibo real: es la evidencia de que
    // entró dinero. Si el folio no existe, no se puede registrar el abono.
    public WaterUserChargePaymentEntity buildPayment(WaterUserChargePaymentDto request, WaterUserChargeEntity cargo) {
        WaterReceiptEntity receipt = Optional.ofNullable(waterReceiptRepository.findByNoFolio(request.getNoFolio()))
                .orElseThrow(() -> new NoSuchElementException("No se encontró el recibo con folio: " + request.getNoFolio()));

        return WaterUserChargePaymentEntity.builder()
                .cargo(cargo)
                .waterReceipt(receipt)
                .montoAplicado(request.getMontoAplicado())
                .fechaPago(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
