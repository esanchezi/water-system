package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.RangoEdadDto;
import com.mx.uvas.watersystem.dto.WaterUserCensusDto;
import com.mx.uvas.watersystem.dto.WaterUserCensusResumenDto;
import com.mx.uvas.watersystem.model.WaterUserCensusEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserCensusRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.WaterUserCensusResumenRestResponse;
import com.mx.uvas.watersystem.response.WaterUserCensusRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserCensusService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserCensusService implements IWaterUserCensusService {

    private final IWaterUserCensusRepository waterUserCensusRepository;
    private final IWaterUserRepository waterUserRepository;

    private static final String NOT_FOUND_MESSAGE = "Registro no encontrado";
    private static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado";

    // Rangos de edad para el reporte agregado. Si se quieren ajustar más
    // adelante, basta con cambiar estos dos arreglos -- no está atado a
    // catálogo porque no cambia seguido y así evitamos otra pantalla de
    // mantenimiento para algo tan estable.
    private static final int[][] RANGOS = {
            {0, 5}, {6, 12}, {13, 17}, {18, 29}, {30, 59}, {60, Integer.MAX_VALUE}
    };
    private static final String[] RANGOS_LABEL = {
            "0-5 años", "6-12 años", "13-17 años", "18-29 años", "30-59 años", "60+ años"
    };

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserCensusRestResponse> findByAguaUsuarioId(Integer aguaUsuarioId) {
        WaterUserCensusRestResponse response = new WaterUserCensusRestResponse();
        try {
            List<WaterUserCensusDto> dtos = waterUserCensusRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatus(aguaUsuarioId, 1)
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Censo encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el censo", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserCensusRestResponse> create(Integer aguaUsuarioId, WaterUserCensusDto dto) {
        WaterUserCensusRestResponse response = new WaterUserCensusRestResponse();
        try {
            Optional<WaterUserEntity> userOpt = waterUserRepository.findById(aguaUsuarioId);
            if (userOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, USER_NOT_FOUND_MESSAGE);
            }

            WaterUserCensusEntity entity = WaterUserCensusEntity.builder()
                    .waterUser(userOpt.get())
                    .edad(dto.getEdad())
                    .anioRegistro(dto.getEdad() != null ? Year.now().getValue() : null)
                    .observaciones(dto.getObservaciones())
                    .estatus(1)
                    .userIdAdd(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();

            waterUserCensusRepository.save(entity);

            return findByAguaUsuarioId(aguaUsuarioId);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al agregar persona al censo", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserCensusRestResponse> update(Integer censoId, WaterUserCensusDto dto) {
        WaterUserCensusRestResponse response = new WaterUserCensusRestResponse();
        try {
            Optional<WaterUserCensusEntity> optional = waterUserCensusRepository.findById(censoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            WaterUserCensusEntity entity = optional.get();
            // Si cambió la edad capturada, reiniciamos el año de registro a
            // hoy para que el cálculo de "edad actual" siga siendo correcto
            // en el futuro sin necesidad de un proceso manual anual.
            boolean cambioEdad = !java.util.Objects.equals(entity.getEdad(), dto.getEdad());
            entity.setEdad(dto.getEdad());
            entity.setAnioRegistro(dto.getEdad() != null ? (cambioEdad ? Year.now().getValue() : entity.getAnioRegistro()) : null);
            entity.setObservaciones(dto.getObservaciones());
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            waterUserCensusRepository.save(entity);

            return findByAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar el registro del censo", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserCensusRestResponse> deactivate(Integer censoId) {
        WaterUserCensusRestResponse response = new WaterUserCensusRestResponse();
        try {
            Optional<WaterUserCensusEntity> optional = waterUserCensusRepository.findById(censoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            WaterUserCensusEntity entity = optional.get();
            entity.setEstatus(0);
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            waterUserCensusRepository.save(entity);

            return findByAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al dar de baja el registro del censo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserCensusResumenRestResponse> resumenPorEdad() {
        WaterUserCensusResumenRestResponse response = new WaterUserCensusResumenRestResponse();
        try {
            List<WaterUserCensusEntity> activos = waterUserCensusRepository.findAllActivos(1);
            int anioActual = Year.now().getValue();

            int[] conteos = new int[RANGOS.length];
            int sinClasificar = 0;

            for (WaterUserCensusEntity persona : activos) {
                if (persona.getEdad() == null || persona.getAnioRegistro() == null) {
                    sinClasificar++;
                    continue;
                }
                int edadActual = persona.getEdad() + (anioActual - persona.getAnioRegistro());
                boolean clasificada = false;
                for (int i = 0; i < RANGOS.length; i++) {
                    if (edadActual >= RANGOS[i][0] && edadActual <= RANGOS[i][1]) {
                        conteos[i]++;
                        clasificada = true;
                        break;
                    }
                }
                if (!clasificada) {
                    sinClasificar++;
                }
            }

            List<RangoEdadDto> rangos = new ArrayList<>();
            for (int i = 0; i < RANGOS.length; i++) {
                rangos.add(new RangoEdadDto(RANGOS_LABEL[i], conteos[i]));
            }

            WaterUserCensusResumenDto dto = new WaterUserCensusResumenDto();
            dto.setRangos(rangos);
            dto.setSinClasificar(sinClasificar);
            dto.setTotalPersonas(activos.size());

            response.setData(List.of(dto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen calculado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular el resumen del censo", e);
        }
    }

    private WaterUserCensusDto entityToDto(WaterUserCensusEntity entity) {
        WaterUserCensusDto dto = new WaterUserCensusDto();
        dto.setCensoId(entity.getCensoId());
        dto.setEdad(entity.getEdad());
        dto.setAnioRegistro(entity.getAnioRegistro());
        dto.setObservaciones(entity.getObservaciones());
        dto.setEstatus(entity.getEstatus());
        if (entity.getEdad() != null && entity.getAnioRegistro() != null) {
            dto.setEdadActual(entity.getEdad() + (Year.now().getValue() - entity.getAnioRegistro()));
        }
        return dto;
    }
}
