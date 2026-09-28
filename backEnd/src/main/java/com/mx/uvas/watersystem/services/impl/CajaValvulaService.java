package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.CajaValvulaDto;
import com.mx.uvas.watersystem.dto.ResumenValvulasDto;
import com.mx.uvas.watersystem.mapping.CajaValvulaMapper;
import com.mx.uvas.watersystem.model.CajaValvulaEntity;
import com.mx.uvas.watersystem.model.TramoEntity;
import com.mx.uvas.watersystem.model.ValvulaEntity;
import com.mx.uvas.watersystem.repositories.ICajaValvulaRepository;
import com.mx.uvas.watersystem.repositories.ITramoRepository;
import com.mx.uvas.watersystem.response.CajaValvulaRestResponse;
import com.mx.uvas.watersystem.response.ResumenValvulasRestResponse;
import com.mx.uvas.watersystem.services.ICajaValvulaService;
import com.mx.uvas.watersystem.utils.BaseService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class CajaValvulaService extends BaseService<CajaValvulaEntity, CajaValvulaDto, CajaValvulaRestResponse>
        implements ICajaValvulaService {

    private final ICajaValvulaRepository cajaValvulaRepository;
    private final ITramoRepository tramoRepository;
    private final CajaValvulaMapper cajaValvulaMapper;

    private static final String FOUND_MESSAGE = "Cajas de válvulas encontradas";
    private static final String ERROR_MESSAGE = "Error al consultar cajas de válvulas";

    @Override
    public ResponseEntity<CajaValvulaRestResponse> findAll() {
        List<CajaValvulaEntity> entities = cajaValvulaRepository.findAllByEstatusOrderByNombreAsc(1);
        return handleFindAll(entities, cajaValvulaMapper::entityToDto, CajaValvulaRestResponse::new, FOUND_MESSAGE, ERROR_MESSAGE);
    }

    @Override
    public ResponseEntity<CajaValvulaRestResponse> findById(Integer cajaId) {
        CajaValvulaRestResponse response = new CajaValvulaRestResponse();
        try {
            Optional<CajaValvulaEntity> optional = cajaValvulaRepository.findById(cajaId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Caja de válvulas no encontrada con id: " + cajaId);
            }
            response.setData(List.of(cajaValvulaMapper.entityToDto(optional.get())));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<CajaValvulaRestResponse> create(CajaValvulaDto dto) {
        CajaValvulaRestResponse response = new CajaValvulaRestResponse();
        try {
            CajaValvulaEntity entity = cajaValvulaMapper.dtoToEntity(dto);
            entity.setEstatus(1);
            if (entity.getListValvula() != null) {
                entity.getListValvula().forEach(v -> v.setEstatus(1));
            }
            if (dto.getTramoId() != null) {
                Optional<TramoEntity> tramo = tramoRepository.findById(dto.getTramoId());
                tramo.ifPresent(entity::setTramo);
            }
            CajaValvulaEntity saved = cajaValvulaRepository.save(entity);
            response.setData(List.of(cajaValvulaMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Caja de válvulas creada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear caja de válvulas", e);
        }
    }

    @Override
    public ResponseEntity<CajaValvulaRestResponse> update(Integer cajaId, CajaValvulaDto dto) {
        CajaValvulaRestResponse response = new CajaValvulaRestResponse();
        try {
            Optional<CajaValvulaEntity> optional = cajaValvulaRepository.findById(cajaId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Caja de válvulas no encontrada con id: " + cajaId);
            }
            CajaValvulaEntity entity = optional.get();
            entity.setCodigo(dto.getCodigo());
            entity.setNombre(dto.getNombre());
            entity.setLat(dto.getLat());
            entity.setLng(dto.getLng());
            entity.setObservaciones(dto.getObservaciones());
            entity.setLado(dto.getLado());

            if (dto.getTramoId() != null) {
                Optional<TramoEntity> tramo = tramoRepository.findById(dto.getTramoId());
                entity.setTramo(tramo.orElse(null));
            } else {
                entity.setTramo(null);
            }

            if (dto.getListValvula() != null) {
                // OJO: no se puede simplemente hacer clear() + volver a crear
                // todas las válvulas desde cero -- una válvula que ya está
                // ligada a un tramo (agua_tramo_valvula.valvula_id) no se
                // puede borrar sin romper esa relación (llave foránea), y
                // clear() + orphanRemoval en la colección original la borra
                // aunque el usuario solo quería, por ejemplo, agregar OTRA
                // válvula nueva a la misma caja. Por eso aquí se actualizan
                // las que ya existían (mismo valvulaId) en vez de
                // reemplazarlas, y solo se crean como nuevas las que en
                // verdad no tenían id todavía.
                java.util.Map<Integer, ValvulaEntity> existentesPorId = new java.util.HashMap<>();
                if (entity.getListValvula() != null) {
                    for (ValvulaEntity v : entity.getListValvula()) {
                        if (v.getValvulaId() != null) {
                            existentesPorId.put(v.getValvulaId(), v);
                        }
                    }
                }
                java.util.Set<ValvulaEntity> actualizadas = new java.util.HashSet<>();
                for (var vDto : dto.getListValvula()) {
                    ValvulaEntity v = vDto.getValvulaId() != null ? existentesPorId.get(vDto.getValvulaId()) : null;
                    if (v != null) {
                        v.setIdentificador(vDto.getIdentificador());
                        v.setTipo(vDto.getTipo());
                        v.setEstado(vDto.getEstado());
                        v.setObservaciones(vDto.getObservaciones());
                    } else {
                        v = ValvulaEntity.builder()
                                .identificador(vDto.getIdentificador())
                                .tipo(vDto.getTipo())
                                .estado(vDto.getEstado())
                                .observaciones(vDto.getObservaciones())
                                .estatus(1)
                                .caja(entity)
                                .build();
                    }
                    actualizadas.add(v);
                }
                entity.getListValvula().clear();
                entity.getListValvula().addAll(actualizadas);
            }

            CajaValvulaEntity saved = cajaValvulaRepository.save(entity);
            // flush() explícito: sin esto, un borrado de válvula que rompe
            // la llave foránea con agua_tramo_valvula solo se detecta hasta
            // el commit de la transacción -- es decir, DESPUÉS de que este
            // método ya regresó, y el catch de aquí nunca lo alcanzaría.
            cajaValvulaRepository.flush();
            response.setData(List.of(cajaValvulaMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Caja de válvulas actualizada correctamente");
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseHandler.handleInternalServerError(response,
                    "No se pudo guardar: alguna válvula que se quitó está asignada a un tramo. "
                            + "Quítala primero de ese tramo (panel \"Modificar horario y válvulas\") y vuelve a intentar.",
                    e);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar caja de válvulas", e);
        }
    }

    @Override
    public ResponseEntity<CajaValvulaRestResponse> delete(Integer cajaId) {
        CajaValvulaRestResponse response = new CajaValvulaRestResponse();
        try {
            Optional<CajaValvulaEntity> optional = cajaValvulaRepository.findById(cajaId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Caja de válvulas no encontrada con id: " + cajaId);
            }
            CajaValvulaEntity entity = optional.get();
            entity.setEstatus(0);
            cajaValvulaRepository.save(entity);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Caja de válvulas desactivada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar caja de válvulas", e);
        }
    }

    @Override
    public ResponseEntity<ResumenValvulasRestResponse> resumen() {
        ResumenValvulasRestResponse response = new ResumenValvulasRestResponse();
        try {
            ResumenValvulasDto dto = cajaValvulaRepository.getResumen();
            response.setData(List.of(dto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen calculado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular resumen de cajas/válvulas", e);
        }
    }
}
