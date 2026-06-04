package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.dto.WaterReceiptPaymentDto;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.helpers.WaterUserHelper;
import com.mx.uvas.watersystem.mapping.WaterReceiptMapper;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserAnnualPaymentRepository;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptService;
import com.mx.uvas.watersystem.helpers.WaterReceiptHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mx.uvas.watersystem.utils.Constants.*;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterReceiptService implements IWaterReceiptService {

    private final IWaterReceiptRepository waterReceiptRepository;
    private final WaterReceiptHelper waterReceiptHelper;
    private final WaterHelper waterHelper;
    private final WaterUserHelper waterUserHelper;
    private final WaterReceiptMapper waterReceiptMapper;
    private final IWaterUserAnnualPaymentRepository waterUserAnnualPaymentRepository;

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findAllByEstatus() {
        return findWaterReceipts(waterReceiptRepository.findAllByEstatus(), RECIBOS);
    }

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findByNoFolioOrNoUsuario(Integer noFolio) {
        return findWaterReceipts(waterReceiptRepository.findByNoFolioOrNoUsuario(noFolio), RECIBOS);
    }

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findByNoUsuario(Integer noUser) {
        return findWaterReceipts(waterReceiptRepository.findByNoUsuario(noUser), RECIBOS);
    }

    @Override
    public WaterReceiptDto create(WaterReceiptDto request) {
        WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

        WaterReceiptEntity waterReceiptToPersist = waterReceiptHelper.buildWaterReceiptEntity(request, user,concepto);
        createReceiptPayments(request.getWaterReceiptPayment(), waterReceiptToPersist);

        WaterReceiptEntity receiptPersist = waterReceiptRepository.save(waterReceiptToPersist);
        //user = waterUserHelper.updateWaterUserEstatus(request.getWaterUser(),user);
        guardarAniosPagados(request.getAniosPagados(), user);
        log.info("Receipt saved id:{}",receiptPersist.getAguaReciboId());

        return waterReceiptMapper.entityToDto(receiptPersist);
    }

    @Override
    public WaterReceiptDto createCancelled(WaterReceiptDto request) {
        WaterReceiptEntity waterReceiptToPersist = waterReceiptHelper.buildWaterReceiptCancelledEntity(request);
        WaterReceiptEntity receiptPersist = waterReceiptRepository.save(waterReceiptToPersist);

        log.info("Receipt saved id:{}",receiptPersist.getAguaReciboId());

        return waterReceiptMapper.entityToDto(receiptPersist);
    }

    private void createReceiptPayments(List<WaterReceiptPaymentDto> paymentDtos, WaterReceiptEntity waterReceiptToPersist) {
        for (WaterReceiptPaymentDto paymentDto : paymentDtos) {
            CatalogOptionsEntity comite = waterHelper.getCatalogOptionOrThrow(paymentDto.getComiteId());
            CatalogOptionsEntity tipoPago = waterHelper.getCatalogOptionOrThrow(paymentDto.getTipoPagoId());
            CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(paymentDto.getConceptoId());
            WaterReceiptPaymentEntity payment = waterReceiptHelper.buildWaterReceiptPaymentEntity(paymentDto, waterReceiptToPersist, comite, tipoPago,concepto);
            waterReceiptToPersist.addPayment(payment);
            waterReceiptToPersist.updatePayments();
        }
    }

    private ResponseEntity<WaterReceiptRestResponse> findWaterReceipts(List<WaterReceiptEntity> waterReceipts, String receiptType) {
        WaterReceiptRestResponse response = new WaterReceiptRestResponse();
        try {
            if (waterReceipts.isEmpty()) {
                throw new NoSuchElementException(receiptType + " - " + NO_ENCONTRADOS);
            }
            List<WaterReceiptDto> waterReceiptDtos = waterReceipts.stream()
                    .map(waterReceiptMapper::entityToDto)
                    .toList();

            response.setData(waterReceiptDtos);
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, receiptType + " - " + ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, receiptType + " - " + NO_ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, ERROR_AL_CONSULTAR + " - " + RECIBOS);
            log.error("Error al consultar: {}", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void guardarAniosPagados(List<Integer> anios, WaterUserEntity user) {
        if (anios == null || anios.isEmpty()) return;
        for (Integer anio : anios) {
            boolean existe = waterUserAnnualPaymentRepository.existsByWaterUser_AguaUsuarioIdAndAnio(user.getAguaUsuarioId(), anio);

            if (!existe) {
                WaterUserAnnualPaymentEntity entity =
                        WaterUserAnnualPaymentEntity.builder()
                                .waterUser(user)
                                .anio(anio)
                                .fechaValidacion(LocalDate.now())
                                .estatus(1)
                                .userIdAdd(1)
                                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                                .build();
                waterUserAnnualPaymentRepository.save(entity);
            }
        }
    }
}
