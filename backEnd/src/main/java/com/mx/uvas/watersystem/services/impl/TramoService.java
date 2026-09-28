package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.SegmentoTuberiaDto;
import com.mx.uvas.watersystem.dto.TramoDto;
import com.mx.uvas.watersystem.dto.TramoHorarioDto;
import com.mx.uvas.watersystem.dto.TramoValvulaDto;
import com.mx.uvas.watersystem.mapping.TramoMapper;
import com.mx.uvas.watersystem.model.PozoEntity;
import com.mx.uvas.watersystem.model.SegmentoTuberiaEntity;
import com.mx.uvas.watersystem.model.TramoEntity;
import com.mx.uvas.watersystem.model.TramoHorarioEntity;
import com.mx.uvas.watersystem.model.TramoValvulaEntity;
import com.mx.uvas.watersystem.model.ValvulaEntity;
import com.mx.uvas.watersystem.repositories.IPozoRepository;
import com.mx.uvas.watersystem.repositories.ITramoRepository;
import com.mx.uvas.watersystem.repositories.IValvulaRepository;
import com.mx.uvas.watersystem.response.TramoRestResponse;
import com.mx.uvas.watersystem.services.ITramoService;
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
public class TramoService extends BaseService<TramoEntity, TramoDto, TramoRestResponse> implements ITramoService {

    private final ITramoRepository tramoRepository;
    private final IPozoRepository pozoRepository;
    private final IValvulaRepository valvulaRepository;
    private final TramoMapper tramoMapper;

    private static final String FOUND_MESSAGE = "Tramos encontrados";
    private static final String ERROR_MESSAGE = "Error al consultar tramos";

    @Override
    public ResponseEntity<TramoRestResponse> findAll() {
        List<TramoEntity> entities = tramoRepository.findAllByEstatusOrderByNombreAsc(1);
        return handleFindAll(entities, tramoMapper::entityToDto, TramoRestResponse::new, FOUND_MESSAGE, ERROR_MESSAGE);
    }

    @Override
    public ResponseEntity<TramoRestResponse> findById(Integer tramoId) {
        TramoRestResponse response = new TramoRestResponse();
        try {
            Optional<TramoEntity> optional = tramoRepository.findById(tramoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Tramo no encontrado con id: " + tramoId);
            }
            response.setData(List.of(tramoMapper.entityToDto(optional.get())));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<TramoRestResponse> create(TramoDto dto) {
        TramoRestResponse response = new TramoRestResponse();
        try {
            TramoEntity entity = tramoMapper.dtoToEntity(dto);
            entity.setEstatus(1);
            entity.setHorarios(construirHorarios(entity, dto.getHorarios()));
            entity.setValvulas(construirValvulas(entity, dto.getValvulas()));
            entity.setSegmentos(construirSegmentos(entity, dto.getSegmentos()));
            TramoEntity saved = tramoRepository.save(entity);
            response.setData(List.of(tramoMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Tramo creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear tramo", e);
        }
    }

    @Override
    public ResponseEntity<TramoRestResponse> update(Integer tramoId, TramoDto dto) {
        TramoRestResponse response = new TramoRestResponse();
        try {
            Optional<TramoEntity> optional = tramoRepository.findById(tramoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Tramo no encontrado con id: " + tramoId);
            }
            TramoEntity entity = optional.get();
            entity.setNombre(dto.getNombre());
            entity.setDescripcion(dto.getDescripcion());
            entity.setLado(dto.getLado());
            entity.setTrazoJson(dto.getTrazoJson());
            entity.setMetodoTrazo(dto.getMetodoTrazo());

            // Se reemplaza la lista completa de horarios -- más simple que
            // hacer merge fino, y el front siempre manda la lista completa
            // (agrega/quita localmente y luego guarda todo de una vez).
            if (entity.getHorarios() == null) {
                entity.setHorarios(new ArrayList<>());
            } else {
                entity.getHorarios().clear();
            }
            entity.getHorarios().addAll(construirHorarios(entity, dto.getHorarios()));

            // Mismo criterio para las válvulas: se reemplaza la lista completa.
            if (entity.getValvulas() == null) {
                entity.setValvulas(new ArrayList<>());
            } else {
                entity.getValvulas().clear();
            }
            entity.getValvulas().addAll(construirValvulas(entity, dto.getValvulas()));

            // Mismo criterio para los segmentos de tubería/manguera.
            if (entity.getSegmentos() == null) {
                entity.setSegmentos(new ArrayList<>());
            } else {
                entity.getSegmentos().clear();
            }
            entity.getSegmentos().addAll(construirSegmentos(entity, dto.getSegmentos()));

            TramoEntity saved = tramoRepository.save(entity);
            response.setData(List.of(tramoMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Tramo actualizado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar tramo", e);
        }
    }

    // Construye la lista de TramoHorarioEntity a partir del DTO, resolviendo
    // cada pozoId contra el catálogo de pozos. Los horarios sin pozo válido
    // se ignoran (no debería pasar si el front siempre manda un pozo elegido).
    private List<TramoHorarioEntity> construirHorarios(TramoEntity tramo, List<TramoHorarioDto> horariosDto) {
        List<TramoHorarioEntity> resultado = new ArrayList<>();
        if (horariosDto == null) {
            return resultado;
        }
        for (TramoHorarioDto h : horariosDto) {
            if (h.getPozoId() == null) {
                continue;
            }
            Optional<PozoEntity> pozo = pozoRepository.findById(h.getPozoId());
            if (pozo.isEmpty()) {
                continue;
            }
            resultado.add(TramoHorarioEntity.builder()
                    .diaSemana(h.getDiaSemana())
                    .horaInicio(h.getHoraInicio())
                    .horaFin(h.getHoraFin())
                    .tramo(tramo)
                    .pozo(pozo.get())
                    .build());
        }
        return resultado;
    }

    // Construye la lista de TramoValvulaEntity a partir del DTO, resolviendo
    // cada valvulaId contra el catálogo de válvulas. Las instrucciones sin
    // válvula válida se ignoran.
    private List<TramoValvulaEntity> construirValvulas(TramoEntity tramo, List<TramoValvulaDto> valvulasDto) {
        List<TramoValvulaEntity> resultado = new ArrayList<>();
        if (valvulasDto == null) {
            return resultado;
        }
        for (TramoValvulaDto v : valvulasDto) {
            if (v.getValvulaId() == null) {
                continue;
            }
            Optional<ValvulaEntity> valvula = valvulaRepository.findById(v.getValvulaId());
            if (valvula.isEmpty()) {
                continue;
            }
            resultado.add(TramoValvulaEntity.builder()
                    .estadoRequerido(v.getEstadoRequerido())
                    .tramo(tramo)
                    .valvula(valvula.get())
                    .build());
        }
        return resultado;
    }

    // Construye la lista de SegmentoTuberiaEntity a partir del DTO -- a
    // diferencia de horarios/válvulas, no depende de resolver ningún
    // catálogo (material/diámetro son texto libre), así que solo copia
    // los campos.
    private List<SegmentoTuberiaEntity> construirSegmentos(TramoEntity tramo, List<SegmentoTuberiaDto> segmentosDto) {
        List<SegmentoTuberiaEntity> resultado = new ArrayList<>();
        if (segmentosDto == null) {
            return resultado;
        }
        for (SegmentoTuberiaDto s : segmentosDto) {
            resultado.add(SegmentoTuberiaEntity.builder()
                    .orden(s.getOrden())
                    .material(s.getMaterial())
                    .diametro(s.getDiametro())
                    .longitudMetros(s.getLongitudMetros())
                    .observaciones(s.getObservaciones())
                    .estatus(1)
                    .tramo(tramo)
                    .build());
        }
        return resultado;
    }

    @Override
    public ResponseEntity<TramoRestResponse> delete(Integer tramoId) {
        TramoRestResponse response = new TramoRestResponse();
        try {
            Optional<TramoEntity> optional = tramoRepository.findById(tramoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Tramo no encontrado con id: " + tramoId);
            }
            TramoEntity entity = optional.get();
            entity.setEstatus(0);
            tramoRepository.save(entity);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Tramo desactivado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar tramo", e);
        }
    }
}
