package com.mx.uvas.watersystem.helpers;

import com.mx.uvas.watersystem.dto.WaterAgreementChargeDto;
import com.mx.uvas.watersystem.dto.WaterAgreementDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterAgreementChargeEntity;
import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

@Transactional
@Component
@AllArgsConstructor
public class WaterAgreementHelper {

    private final IWaterUserChargeRepository waterUserChargeRepository;

    public WaterAgreementEntity buildEntity(WaterAgreementDto request, WaterUserEntity user, CatalogOptionsEntity estatusConvenio) {
        return WaterAgreementEntity.builder()
                .waterUser(user)
                .noFolio(request.getNoFolio())
                .fecha(request.getFecha())
                .motivo(request.getMotivo())
                .comentario(request.getComentario())
                .estatusConvenio(estatusConvenio)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public WaterAgreementChargeEntity buildChargeLine(WaterAgreementChargeDto lineRequest, WaterAgreementEntity convenio) {
        WaterUserChargeEntity cargo = waterUserChargeRepository.findById(lineRequest.getAguaUsuarioCargoId())
                .orElseThrow(() -> new NoSuchElementException("No se encontró el cargo con ID: " + lineRequest.getAguaUsuarioCargoId()));

        return WaterAgreementChargeEntity.builder()
                .convenio(convenio)
                .cargo(cargo)
                .montoCondonado(lineRequest.getMontoCondonado())
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
