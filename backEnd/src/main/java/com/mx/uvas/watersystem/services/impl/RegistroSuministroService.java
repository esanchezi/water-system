package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.DiasPorPozoDto;
import com.mx.uvas.watersystem.dto.DiasPorTramoDto;
import com.mx.uvas.watersystem.dto.RegistroSuministroDto;
import com.mx.uvas.watersystem.dto.RegistroSuministroTramoDto;
import com.mx.uvas.watersystem.mapping.RegistroSuministroMapper;
import com.mx.uvas.watersystem.model.PozoEntity;
import com.mx.uvas.watersystem.model.RegistroSuministroEntity;
import com.mx.uvas.watersystem.model.RegistroSuministroTramoEntity;
import com.mx.uvas.watersystem.model.TramoEntity;
import com.mx.uvas.watersystem.repositories.IPozoRepository;
import com.mx.uvas.watersystem.repositories.IRegistroSuministroRepository;
import com.mx.uvas.watersystem.repositories.ITramoRepository;
import com.mx.uvas.watersystem.response.DiasPorPozoRestResponse;
import com.mx.uvas.watersystem.response.DiasPorTramoRestResponse;
import com.mx.uvas.watersystem.response.RegistroSuministroRestResponse;
import com.mx.uvas.watersystem.services.IRegistroSuministroService;
import com.mx.uvas.watersystem.utils.BaseService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class RegistroSuministroService
        extends BaseService<RegistroSuministroEntity, RegistroSuministroDto, RegistroSuministroRestResponse>
        implements IRegistroSuministroService {

    private final IRegistroSuministroRepository registroSuministroRepository;
    private final IPozoRepository pozoRepository;
    private final ITramoRepository tramoRepository;
    private final RegistroSuministroMapper registroSuministroMapper;

    private static final String FOUND_MESSAGE = "Registros de suministro encontrados";
    private static final String ERROR_MESSAGE = "Error al consultar registros de suministro";

    @Override
    public ResponseEntity<RegistroSuministroRestResponse> findAll() {
        List<RegistroSuministroEntity> entities = registroSuministroRepository.findAllByEstatusOrderByFechaDesc(1);
        return handleFindAll(entities, registroSuministroMapper::entityToDto, RegistroSuministroRestResponse::new, FOUND_MESSAGE, ERROR_MESSAGE);
    }

    @Override
    public ResponseEntity<RegistroSuministroRestResponse> findById(Integer registroId) {
        RegistroSuministroRestResponse response = new RegistroSuministroRestResponse();
        try {
            Optional<RegistroSuministroEntity> optional = registroSuministroRepository.findById(registroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Registro no encontrado con id: " + registroId);
            }
            response.setData(List.of(registroSuministroMapper.entityToDto(optional.get())));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<RegistroSuministroRestResponse> create(RegistroSuministroDto dto) {
        RegistroSuministroRestResponse response = new RegistroSuministroRestResponse();
        try {
            if (dto.getPozoId() == null) {
                return ResponseHandler.handleNotFoundException(response, "Debe indicar el pozo del registro");
            }
            Optional<PozoEntity> pozo = pozoRepository.findById(dto.getPozoId());
            if (pozo.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Pozo no encontrado con id: " + dto.getPozoId());
            }

            RegistroSuministroEntity entity = RegistroSuministroEntity.builder()
                    .fecha(dto.getFecha())
                    .turno(dto.getTurno())
                    .observaciones(dto.getObservaciones())
                    .estatus(1)
                    .pozo(pozo.get())
                    .build();

            List<RegistroSuministroTramoEntity> tramos = new ArrayList<>();
            if (dto.getListTramos() != null) {
                for (RegistroSuministroTramoDto tramoDto : dto.getListTramos()) {
                    Optional<TramoEntity> tramo = tramoRepository.findById(tramoDto.getTramoId());
                    if (tramo.isEmpty()) {
                        continue;
                    }
                    tramos.add(RegistroSuministroTramoEntity.builder()
                            .orden(tramoDto.getOrden())
                            .registro(entity)
                            .tramo(tramo.get())
                            .build());
                }
            }
            entity.setListTramos(tramos);

            RegistroSuministroEntity saved = registroSuministroRepository.save(entity);
            response.setData(List.of(registroSuministroMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Registro de suministro creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear registro de suministro", e);
        }
    }

    @Override
    public ResponseEntity<RegistroSuministroRestResponse> delete(Integer registroId) {
        RegistroSuministroRestResponse response = new RegistroSuministroRestResponse();
        try {
            Optional<RegistroSuministroEntity> optional = registroSuministroRepository.findById(registroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Registro no encontrado con id: " + registroId);
            }
            RegistroSuministroEntity entity = optional.get();
            entity.setEstatus(0);
            registroSuministroRepository.save(entity);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Registro desactivado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar registro", e);
        }
    }

    @Override
    public ResponseEntity<DiasPorPozoRestResponse> diasPorPozo() {
        DiasPorPozoRestResponse response = new DiasPorPozoRestResponse();
        try {
            List<DiasPorPozoDto> dtos = registroSuministroRepository.getDiasPorPozo();
            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen calculado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular días por pozo", e);
        }
    }

    @Override
    public ResponseEntity<DiasPorTramoRestResponse> diasPorTramo() {
        DiasPorTramoRestResponse response = new DiasPorTramoRestResponse();
        try {
            List<DiasPorTramoDto> dtos = registroSuministroRepository.getDiasPorTramo();
            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen calculado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular días por tramo", e);
        }
    }
}
