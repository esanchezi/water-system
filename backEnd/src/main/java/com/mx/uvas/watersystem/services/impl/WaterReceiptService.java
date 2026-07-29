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
import java.util.HashSet;
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

    // Edita un recibo ya existente -- incluye todo lo que trae el formulario
    // de captura (usuario, folio, fecha, concepto, comité, tipo de pago,
    // montos aplicados por año). Los pagos se reemplazan por completo: como
    // la relación tiene orphanRemoval=true, basta con vaciar la colección y
    // volver a construirla -- Hibernate borra los que ya no estén y crea los
    // nuevos al hacer flush.
    @Override
    public ResponseEntity<WaterReceiptRestResponse> update(Integer id, WaterReceiptDto request) {
        WaterReceiptRestResponse response = new WaterReceiptRestResponse();
        try {
            WaterReceiptEntity existente = waterReceiptRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró el recibo con el ID: " + id));

            WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
            CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

            waterReceiptHelper.actualizarWaterReceiptEntity(existente, request, user, concepto);

            if (existente.getWaterReceiptPayment() != null) {
                existente.getWaterReceiptPayment().clear();
            } else {
                existente.setWaterReceiptPayment(new HashSet<>());
            }
            createReceiptPayments(request.getWaterReceiptPayment(), existente);

            WaterReceiptEntity receiptPersist = waterReceiptRepository.save(existente);
            guardarAniosPagados(request.getAniosPagados(), user);
            log.info("Receipt updated id:{}", receiptPersist.getAguaReciboId());

            response.setData(List.of(waterReceiptMapper.entityToDto(receiptPersist)));
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, "Recibo actualizado correctamente");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, "Error al actualizar el recibo");
            log.error("Error al actualizar recibo: {}", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
