package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ValorGeneralDto;
import com.mx.uvas.watersystem.mapping.ValorGeneralMapper;
import com.mx.uvas.watersystem.model.ValorGeneralEntity;
import com.mx.uvas.watersystem.repositories.IValorGeneralRepository;
import com.mx.uvas.watersystem.response.ValorGeneralRestResponse;
import com.mx.uvas.watersystem.services.IValorGeneralService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class ValorGeneralService implements IValorGeneralService {

    private final IValorGeneralRepository valorGeneralRepository;
    private final ValorGeneralMapper valorGeneralMapper;

    private static final String VALORES_FOUND_MESSAGE = "Valores generales encontrados";
    private static final String ERROR_SEARCHING_MESSAGE = "Error al consultar valores generales";
    private static final String VALOR_CREATED_MESSAGE = "Valor general registrado correctamente";
    private static final String ERROR_CREATING_MESSAGE = "Error al registrar el valor general";
    private static final String VALOR_UPDATED_MESSAGE = "Valor general actualizado correctamente";
    private static final String ERROR_UPDATING_MESSAGE = "Error al actualizar el valor general";
    private static final String VALOR_NOT_FOUND_MESSAGE = "No se encontró el valor general indicado";
    private static final String VALOR_DEACTIVATED_MESSAGE = "Valor general dado de baja correctamente";
    private static final String ERROR_DEACTIVATING_MESSAGE = "Error al dar de baja el valor general";

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ValorGeneralRestResponse> findAll() {
        ValorGeneralRestResponse response = new ValorGeneralRestResponse();
        try {
            List<ValorGeneralEntity> entidades = valorGeneralRepository.findByEstatusOrderByClaveAscVigenciaDesc(1);
            response.setData(entidades.stream().map(valorGeneralMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, VALORES_FOUND_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<ValorGeneralRestResponse> create(ValorGeneralDto dto) {
        ValorGeneralRestResponse response = new ValorGeneralRestResponse();
        try {
            ValorGeneralEntity entity = valorGeneralMapper.dtoToEntity(dto);
            entity.setUserIdAdd(1);
            entity.setDateAdd(LocalDateTime.now());
            ValorGeneralEntity saved = valorGeneralRepository.save(entity);

            response.setData(List.of(valorGeneralMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, VALOR_CREATED_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<ValorGeneralRestResponse> update(Integer valorGeneralId, ValorGeneralDto dto) {
        ValorGeneralRestResponse response = new ValorGeneralRestResponse();
        try {
            Optional<ValorGeneralEntity> optional = valorGeneralRepository.findById(valorGeneralId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, VALOR_NOT_FOUND_MESSAGE);
            }
            ValorGeneralEntity existing = optional.get();
            existing.setClave(dto.getClave());
            existing.setNombre(dto.getNombre());
            existing.setVigencia(dto.getVigencia());
            existing.setMonto(dto.getMonto());
            existing.setObservaciones(dto.getObservaciones());
            existing.setUserIdUpdate(1);
            existing.setDateUpdate(LocalDateTime.now());

            ValorGeneralEntity updated = valorGeneralRepository.save(existing);
            response.setData(List.of(valorGeneralMapper.entityToDto(updated)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, VALOR_UPDATED_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_UPDATING_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<ValorGeneralRestResponse> deactivate(Integer valorGeneralId) {
        ValorGeneralRestResponse response = new ValorGeneralRestResponse();
        try {
            Optional<ValorGeneralEntity> optional = valorGeneralRepository.findById(valorGeneralId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, VALOR_NOT_FOUND_MESSAGE);
            }
            ValorGeneralEntity existing = optional.get();
            existing.setEstatus(0);
            existing.setUserIdUpdate(1);
            existing.setDateUpdate(LocalDateTime.now());
            valorGeneralRepository.save(existing);

            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, VALOR_DEACTIVATED_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_DEACTIVATING_MESSAGE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getMontoVigente(String clave, Integer anio) {
        if (clave == null || anio == null) {
            return 0d;
        }
        Optional<ValorGeneralEntity> exacto = valorGeneralRepository.findByClaveAndVigenciaAndEstatus(clave, anio, 1);
        if (exacto.isPresent()) {
            return exacto.get().getMonto() != null ? exacto.get().getMonto() : 0d;
        }
        // Sin renglón exacto para el año -- se toma el más reciente de un
        // año anterior (para no quedar en $0.00 solo porque todavía no se
        // ha capturado el valor del año en curso).
        return valorGeneralRepository.findByClaveAndEstatusOrderByVigenciaDesc(clave, 1).stream()
                .filter(v -> v.getVigencia() != null && v.getVigencia() <= anio)
                .findFirst()
                .map(v -> v.getMonto() != null ? v.getMonto() : 0d)
                .orElse(0d);
    }
}
