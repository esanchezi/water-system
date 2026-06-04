package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.mapping.WaterReceiptHistoryMapper;
import com.mx.uvas.watersystem.mapping.WaterReceiptMapper;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.repositories.IWaterReceiptHistoryRepository;
import com.mx.uvas.watersystem.response.WaterReceiptHistoryRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptHistoryService;
import com.mx.uvas.watersystem.utils.enums.Tables;
import com.mx.uvas.watersystem.exception.IdNotFoundException;
import com.mx.uvas.watersystem.helpers.WaterReceiptHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mx.uvas.watersystem.utils.Constants.*;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterReceiptHistoryService implements IWaterReceiptHistoryService {

    private final IWaterReceiptRepository waterReceiptRepository;
    private final IWaterReceiptHistoryRepository waterReceiptHistoryRepository;
    private final WaterReceiptHelper waterReceiptHelper;
    private final WaterHelper waterHelper;
    private final WaterReceiptHistoryMapper waterReceiptHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterReceiptHistoryRestResponse> findAllByEstatus() {
        WaterReceiptHistoryRestResponse response = new WaterReceiptHistoryRestResponse();
        try {
            List<WaterReceiptHistoryEntity> waterReceipt = waterReceiptHistoryRepository.findAllByEstatus();
            if (waterReceipt.isEmpty()) {
                throw new NoSuchElementException(RECIBOS_HISTORIAL +" - "+ NO_ENCONTRADOS);
            }

            List<WaterReceiptHistoryDto> waterReceiptDtos = waterReceipt.stream()
                    .map(waterReceiptHistoryMapper::entityToDto)
                    .toList();

            response.setData(waterReceiptDtos);
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, RECIBOS_HISTORIAL +" - "+ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, RECIBOS_HISTORIAL +" - "+NO_ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, ERROR_AL_CONSULTAR + " - " + RECIBOS);
            e.getStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterReceiptHistoryRestResponse> findByNoFolioOrNombre(String noFolio) {
        WaterReceiptHistoryRestResponse response = new WaterReceiptHistoryRestResponse();
        try {
            List<WaterReceiptHistoryEntity> waterReceipt = waterReceiptHistoryRepository.findByNoFolioOrNombre(noFolio);
            if (waterReceipt.isEmpty()) {
                throw new NoSuchElementException(RECIBOS_HISTORIAL +" - "+ NO_ENCONTRADOS);
            }

            List<WaterReceiptHistoryDto> waterReceiptDtos = waterReceipt.stream()
                    .map(waterReceiptHistoryMapper::entityToDto)
                    .toList();

            response.setData(waterReceiptDtos);
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, RECIBOS_HISTORIAL +" - "+ ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, RECIBOS_HISTORIAL +" - "+ NO_ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, ERROR_AL_CONSULTAR +" - "+RECIBOS);
            e.getStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public WaterReceiptHistoryDto updateReceiptHistory(WaterReceiptHistoryDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        WaterReceiptHistoryEntity receiptToUpdate = waterReceiptHistoryRepository.findById(request.getAguaReciboHistorialId()).orElseThrow(() -> new IdNotFoundException(Tables.aguaReciboHistorial.name()));
        WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(6);
        CatalogOptionsEntity tipoPago = waterHelper.getCatalogOptionOrThrow(17);
        receiptToUpdate.setFecha(LocalDate.parse(request.getFechaStr()));

        WaterReceiptDto receiptDto = new WaterReceiptDto();
        receiptDto.setNoFolio(Integer.parseInt(receiptToUpdate.getNoFolio()));
        receiptDto.setFecha(receiptToUpdate.getFecha());
        receiptDto.setFechaStr(receiptToUpdate.getFecha().format(formatter2));
        receiptDto.setObservaciones(receiptToUpdate.getConcepto());
        receiptDto.setTotal(receiptToUpdate.getMonto());
        WaterReceiptEntity waterReceiptToPersist = waterReceiptHelper.buildWaterReceiptEntity(receiptDto, user,concepto);

        WaterReceiptPaymentDto paymentDto = new WaterReceiptPaymentDto();
        paymentDto.setFechaPago(receiptToUpdate.getFecha().atStartOfDay());
        paymentDto.setFechaPagoStr(receiptToUpdate.getFecha().atStartOfDay().format(formatter));
        paymentDto.setMontoRecibido(receiptToUpdate.getMonto());
        paymentDto.setMontoAplicado(receiptToUpdate.getMonto());
        paymentDto.setAnio(request.getAnio());
        WaterReceiptPaymentEntity payment = waterReceiptHelper.buildWaterReceiptPaymentEntity(paymentDto, waterReceiptToPersist, receiptToUpdate.getComite(), tipoPago,concepto);
        waterReceiptToPersist.addPayment(payment);
        waterReceiptToPersist.updatePayments();
        WaterReceiptEntity receiptPersist = waterReceiptRepository.save(waterReceiptToPersist);

        receiptToUpdate.setWaterUser(user);
        receiptToUpdate.setEsProcesado(true);
        receiptToUpdate.setUserIdUpdate(1);
        receiptToUpdate.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        var receiptUpdate = waterReceiptHistoryRepository.save(receiptToUpdate);
        return waterReceiptHistoryMapper.entityToDto(receiptUpdate);
    }

}
