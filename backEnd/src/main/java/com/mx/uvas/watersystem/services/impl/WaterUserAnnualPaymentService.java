package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterUserAnnualPaymentDto;
import com.mx.uvas.watersystem.model.WaterUserAnnualPaymentEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserAnnualPaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.WaterUserAnnualPaymentRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserAnnualPaymentService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
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
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserAnnualPaymentService implements IWaterUserAnnualPaymentService {

    private final IWaterUserAnnualPaymentRepository waterUserAnnualPaymentRepository;
    private final IWaterUserRepository waterUserRepository;

    private static final String NOT_FOUND_MESSAGE = "Registros no encontrados";
    private static final String ERROR_MESSAGE = "Error al consultar años pagados";

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> findByNoUser(Integer noUser) {
        WaterUserAnnualPaymentRestResponse response = new WaterUserAnnualPaymentRestResponse();
        try {
            List<WaterUserAnnualPaymentDto> dtos = waterUserAnnualPaymentRepository
                    .findByNoUser(noUser)
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Años pagados encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> create(Integer aguaUsuarioId, WaterUserAnnualPaymentDto dto) {
        WaterUserAnnualPaymentRestResponse response = new WaterUserAnnualPaymentRestResponse();
        try {
            Optional<WaterUserEntity> userOpt = waterUserRepository.findById(aguaUsuarioId);
            if (userOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Usuario no encontrado");
            }

            boolean yaActivo = waterUserAnnualPaymentRepository
                    .existsByWaterUser_AguaUsuarioIdAndAnioAndEstatus(aguaUsuarioId, dto.getAnio(), 1);
            if (yaActivo) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                        "Ese año ya está marcado como pagado para este usuario");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            WaterUserAnnualPaymentEntity entity = WaterUserAnnualPaymentEntity.builder()
                    .waterUser(userOpt.get())
                    .anio(dto.getAnio())
                    .fechaValidacion(dto.getFechaValidacion() != null ? dto.getFechaValidacion() : LocalDate.now())
                    .observaciones(dto.getObservaciones())
                    .estatus(1)
                    .userIdAdd(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();

            waterUserAnnualPaymentRepository.save(entity);

            return findByNoUser(userOpt.get().getNoUsuario());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al marcar el año como pagado", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> deactivate(Integer pagoAnualId) {
        WaterUserAnnualPaymentRestResponse response = new WaterUserAnnualPaymentRestResponse();
        try {
            Optional<WaterUserAnnualPaymentEntity> optional = waterUserAnnualPaymentRepository.findById(pagoAnualId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            WaterUserAnnualPaymentEntity entity = optional.get();
            entity.setEstatus(0);
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            waterUserAnnualPaymentRepository.save(entity);

            return findByNoUser(entity.getWaterUser().getNoUsuario());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al dar de baja el registro", e);
        }
    }

    private WaterUserAnnualPaymentDto entityToDto(WaterUserAnnualPaymentEntity entity) {
        WaterUserAnnualPaymentDto dto = new WaterUserAnnualPaymentDto();
        dto.setPagoAnualId(entity.getPagoAnualId());
        dto.setAnio(entity.getAnio());
        dto.setFechaValidacion(entity.getFechaValidacion());
        dto.setObservaciones(entity.getObservaciones());
        dto.setEstatus(entity.getEstatus());
        return dto;
    }
}
