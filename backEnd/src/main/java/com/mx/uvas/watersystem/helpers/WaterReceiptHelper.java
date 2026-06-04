package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.dto.WaterReceiptPaymentDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAdressRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Transactional
@Component
@AllArgsConstructor
public class WaterReceiptHelper {

    private final IPersonRepository personRepository;
    private final IAdressRepository adressRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;

    public WaterReceiptEntity buildWaterReceiptEntity(WaterReceiptDto request, WaterUserEntity user, CatalogOptionsEntity concepto) {
        return WaterReceiptEntity.builder()
                .waterUser(user)
                .catConcepto(concepto)
                .noFolio(request.getNoFolio())
                .fecha(LocalDate.parse(request.getFechaStr()))
                .observaciones(request.getObservaciones())
                .concepto(request.getConcepto())
                .total(request.getTotal())
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public WaterReceiptEntity buildWaterReceiptCancelledEntity(WaterReceiptDto request) {
        return WaterReceiptEntity.builder()
                .noFolio(request.getNoFolio())
                .fecha(LocalDate.parse(request.getFechaStr()))
                .concepto("CANCELADO")
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public WaterReceiptPaymentEntity buildWaterReceiptPaymentEntity(WaterReceiptPaymentDto paymentDto, WaterReceiptEntity waterReceiptToPersist, CatalogOptionsEntity comite, CatalogOptionsEntity tipoPago, CatalogOptionsEntity concepto) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return WaterReceiptPaymentEntity.builder()
                .waterReceipt(waterReceiptToPersist)
                .catComite(comite)
                .catTiPag(tipoPago)
                .catConcepto(concepto)
                .fechaPago(LocalDateTime.parse(paymentDto.getFechaPagoStr(), format))
                .montoRecibido(paymentDto.getMontoRecibido())
                .montoAplicado(paymentDto.getMontoAplicado())
                .anio(paymentDto.getAnio())
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
