package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ConfiguracionDto;
import com.mx.uvas.watersystem.mapping.ConfiguracionMapper;
import com.mx.uvas.watersystem.model.ConfiguracionEntity;
import com.mx.uvas.watersystem.repositories.IConfiguracionRepository;
import com.mx.uvas.watersystem.response.ConfiguracionRestResponse;
import com.mx.uvas.watersystem.services.IConfiguracionService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class ConfiguracionService implements IConfiguracionService {

    private final IConfiguracionRepository configuracionRepository;
    private final ConfiguracionMapper configuracionMapper;

    // Clave especial que guarda si las configuraciones están bloqueadas.
    // Usa el mismo mecanismo llave/valor que cualquier otro ajuste, así
    // siempre se puede desbloquear sin necesitar un endpoint aparte --
    // updateByClave() deja pasar cambios a esta clave incluso bloqueado.
    public static final String CLAVE_BLOQUEADO = "BLOQUEADO";
    public static final String CLAVE_NOMBRE_COMITE = "NOMBRE_COMITE";

    @Override
    @Transactional
    public ResponseEntity<ConfiguracionRestResponse> findAll() {
        ConfiguracionRestResponse response = new ConfiguracionRestResponse();
        try {
            asegurarValoresPorDefecto();
            List<ConfiguracionEntity> entities = configuracionRepository.findAll();
            response.setData(entities.stream().map(configuracionMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Configuraciones encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar configuraciones", e);
        }
    }

    @Override
    public ResponseEntity<ConfiguracionRestResponse> updateByClave(String clave, ConfiguracionDto dto) {
        ConfiguracionRestResponse response = new ConfiguracionRestResponse();
        try {
            asegurarValoresPorDefecto();

            Optional<ConfiguracionEntity> bloqueadoOpt = configuracionRepository.findByClave(CLAVE_BLOQUEADO);
            boolean bloqueado = bloqueadoOpt.isPresent() && "true".equalsIgnoreCase(bloqueadoOpt.get().getValor());
            if (bloqueado && !CLAVE_BLOQUEADO.equalsIgnoreCase(clave)) {
                return ResponseHandler.handleBadRequest(response,
                        "Las configuraciones están bloqueadas. Desbloquéalas primero para poder cambiar \"" + clave + "\".");
            }

            Optional<ConfiguracionEntity> optional = configuracionRepository.findByClave(clave);
            ConfiguracionEntity entity = optional.orElseGet(() ->
                    ConfiguracionEntity.builder().clave(clave).estatus(1).build());
            entity.setValor(dto.getValor());
            if (dto.getDescripcion() != null) {
                entity.setDescripcion(dto.getDescripcion());
            }
            entity.setUserIdUpdate(1); // TODO: Keycloak
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            ConfiguracionEntity saved = configuracionRepository.save(entity);
            response.setData(List.of(configuracionMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Configuración actualizada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar configuración", e);
        }
    }

    // La primera vez que se usa el módulo (tabla vacía) crea los ajustes
    // base con valores por defecto, así no hace falta insertarlos a mano en
    // la base de datos.
    private void asegurarValoresPorDefecto() {
        if (configuracionRepository.findByClave(CLAVE_NOMBRE_COMITE).isEmpty()) {
            configuracionRepository.save(ConfiguracionEntity.builder()
                    .clave(CLAVE_NOMBRE_COMITE)
                    .valor("Los Lopez")
                    .descripcion("Nombre del comité que aparece en el título de la app")
                    .estatus(1)
                    .build());
        }
        if (configuracionRepository.findByClave(CLAVE_BLOQUEADO).isEmpty()) {
            configuracionRepository.save(ConfiguracionEntity.builder()
                    .clave(CLAVE_BLOQUEADO)
                    .valor("false")
                    .descripcion("Si está en \"true\", ya no se pueden editar las demás configuraciones")
                    .estatus(1)
                    .build());
        }
    }
}
