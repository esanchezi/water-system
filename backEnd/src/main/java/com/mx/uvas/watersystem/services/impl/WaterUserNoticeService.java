package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.dto.WaterReceiptPaymentDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.helpers.WaterReceiptHelper;
import com.mx.uvas.watersystem.mapping.WaterReceiptMapper;
import com.mx.uvas.watersystem.mapping.WaterUserMapper;
import com.mx.uvas.watersystem.mapping.WaterUserNoticeMapper;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserNoticeRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import com.mx.uvas.watersystem.response.WaterUserNoticeRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptService;
import com.mx.uvas.watersystem.services.IWaterUserNoticeService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mx.uvas.watersystem.utils.Constants.*;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserNoticeService implements IWaterUserNoticeService {

    private final IWaterUserNoticeRepository waterUserNoticeRepository;
    private final WaterUserNoticeMapper waterUserNoticeMapper;

    private static final String AVISOS_FOUND_MESSAGE = "Avisos encontrados";
    private static final String AVISOS_NOT_FOUND_MESSAGE = "Avisos no encontrados";
    private static final String ERROR_SEARCHING_AVISOS_MESSAGE = "Error al consultar avisos por ID";

    @Override
    public ResponseEntity<WaterUserNoticeRestResponse> findByNoUser(Integer noUser) {
        return handleFindAll(waterUserNoticeRepository.findByNoUser(noUser));
    }

    private ResponseEntity<WaterUserNoticeRestResponse> handleFindAll(List<WaterUserNoticeEntity> waterUsersNotice) {
        WaterUserNoticeRestResponse response = new WaterUserNoticeRestResponse();
        try {
            List<WaterUserNoticeDto> waterUserDtos = waterUsersNotice.isEmpty()
                    ? Collections.emptyList()
                    : waterUsersNotice.stream()
                    .map(waterUserNoticeMapper::entityToDto)
                    .toList();

            response.setData(waterUserDtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, AVISOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, AVISOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_AVISOS_MESSAGE, e);
        }
    }

}
