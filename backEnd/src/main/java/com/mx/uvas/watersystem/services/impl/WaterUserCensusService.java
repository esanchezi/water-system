package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.RangoEdadDto;
import com.mx.uvas.watersystem.dto.WaterUserCensusDto;
import com.mx.uvas.watersystem.dto.WaterUserCensusResumenDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.PreregistroUsuarioEntity;
import com.mx.uvas.watersystem.model.WaterUserCensusEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IPreregistroUsuarioRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserCensusRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.WaterUserCensusResumenRestResponse;
import com.mx.uvas.watersystem.response.WaterUserCensusRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserCensusService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
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
    private final IPreregistroUsuarioRepository preregistroUsuarioRepository;
    private final CurrentUserService currentUserService;

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
                    .userIdAdd(currentUserService.getCurrentUserId())
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
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
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
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
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
            // LinkedHashMap para que las zonas salgan siempre en el mismo
            // orden en que se van encontrando (no importa mucho el orden,
            // pero evita que "brinque" el reporte entre refrescos).
            java.util.Map<String, Integer> conteoPorZona = new java.util.LinkedHashMap<>();
            int sinZona = 0;

            for (WaterUserCensusEntity persona : activos) {
                if (persona.getEdad() == null || persona.getAnioRegistro() == null) {
                    sinClasificar++;
                } else {
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

                // Zona = calle de la casa del usuario al que pertenece esta
                // persona del censo. Un usuario sin casa asignada, o una
                // casa sin calle capturada, cae en "sin zona asignada".
                String zona = obtenerZona(persona);
                if (zona == null) {
                    sinZona++;
                } else {
                    conteoPorZona.merge(zona, 1, Integer::sum);
                }
            }

            List<RangoEdadDto> rangos = new ArrayList<>();
            for (int i = 0; i < RANGOS.length; i++) {
                rangos.add(new RangoEdadDto(RANGOS_LABEL[i], conteos[i]));
            }

            List<RangoEdadDto> porZona = new ArrayList<>();
            conteoPorZona.forEach((nombreZona, cantidad) -> porZona.add(new RangoEdadDto(nombreZona, cantidad)));
            // Orden descendente por cantidad -- las zonas con más personas primero.
            porZona.sort((a, b) -> b.getCantidad().compareTo(a.getCantidad()));

            // Negocios -- no viene de la ficha del censo, se calcula directo
            // de la clasificación de uso (esNegocio + giroNegocio) de cada
            // usuario. Así se puede saber cuántos negocios hay sin necesitar
            // capturar nada extra. También se suman los negocios anotados en
            // preregistro que nunca van a tener su propio usuario (ej.
            // tiendita atendida por el usuario del domicilio) -- se excluyen
            // los ya convertidos ahí para no contarlos dos veces.
            List<WaterUserEntity> negocios = waterUserRepository.findAllNegociosActivos();
            List<PreregistroUsuarioEntity> negociosPreregistro = preregistroUsuarioRepository.findAllNegociosNoConvertidos();
            java.util.Map<String, Integer> conteoPorGiro = new java.util.LinkedHashMap<>();
            int sinGiro = 0;
            for (WaterUserEntity negocio : negocios) {
                if (negocio.getGiroNegocio() != null) {
                    conteoPorGiro.merge(negocio.getGiroNegocio().getNombre(), 1, Integer::sum);
                } else {
                    sinGiro++;
                }
            }
            for (PreregistroUsuarioEntity negocio : negociosPreregistro) {
                if (negocio.getGiroNegocio() != null) {
                    conteoPorGiro.merge(negocio.getGiroNegocio().getNombre(), 1, Integer::sum);
                } else {
                    sinGiro++;
                }
            }
            List<RangoEdadDto> porGiro = new ArrayList<>();
            conteoPorGiro.forEach((nombreGiro, cantidad) -> porGiro.add(new RangoEdadDto(nombreGiro, cantidad)));
            porGiro.sort((a, b) -> b.getCantidad().compareTo(a.getCantidad()));

            WaterUserCensusResumenDto dto = new WaterUserCensusResumenDto();
            dto.setRangos(rangos);
            dto.setSinClasificar(sinClasificar);
            dto.setTotalPersonas(activos.size());
            dto.setPorZona(porZona);
            dto.setSinZonaAsignada(sinZona);
            dto.setTotalNegocios(negocios.size() + negociosPreregistro.size());
            dto.setPorGiro(porGiro);
            dto.setSinGiroAsignado(sinGiro);
            dto.setNegociosConUsuario(negocios.size());
            dto.setNegociosSinUsuario(negociosPreregistro.size());

            response.setData(List.of(dto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Resumen calculado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular el resumen del censo", e);
        }
    }

    // Devuelve la zona del usuario dueño de este registro de censo, o null
    // si no se puede determinar. Si la calle de su casa ya tiene una zona
    // asignada (catálogo Calle -> Sección/Colonia, ej. "La Barca" agrupa
    // Azucena, Jazmín, Orquídea...) se usa esa zona; si la calle todavía no
    // tiene zona asignada, se usa el nombre de la calle como antes
    // (compatible con lo que ya se tenía capturado mientras se van
    // asignando zonas).
    private String obtenerZona(WaterUserCensusEntity persona) {
        if (persona.getWaterUser() == null) return null;
        WaterUserEntity usuario = persona.getWaterUser();
        if (usuario.getWaterHouse() == null) return null;
        CatalogOptionsEntity calle = usuario.getWaterHouse().getCatCalle();
        if (calle == null) return null;
        if (calle.getZona() != null) return calle.getZona().getNombre();
        return calle.getNombre();
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
