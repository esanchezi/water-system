package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.helpers.PersonHelper;
import com.mx.uvas.watersystem.helpers.WaterUserHelper;
import com.mx.uvas.watersystem.mapping.WaterUserMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.FeeEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.repositories.*;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.response.WaterUserBasicRestResponse;
import com.mx.uvas.watersystem.response.WaterUserDetailsRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserService;
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
import java.util.*;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserService implements IWaterUserService {

    private final IFeeRepository feeRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterUserRepository waterUserRepository;
    private final IWaterHouseRepository waterHouseRepository;
    private final IWaterGroupRepository waterGroupRepository;
    private final PersonHelper personHelper;
    private final WaterUserHelper waterUserHelper;
    private final WaterUserMapper waterUserMapper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserRestResponse> search() {
        return handleFindAll(waterUserRepository.findAll());
    }


    @Override
    @Transactional(readOnly = true)
    public List<AguaUsuarioSearchDTO> searchUsers(String term) {
        return waterUserRepository.searchByNoUsuario(term);
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserBasicRestResponse> getListUsers() {
        return handleFindAllBasic(waterUserRepository.getListUsers());
    }

    @Override
    public ResponseEntity<WaterUserRestResponse> findByNoUser(Integer noUser) {
        return handleFindSingle(waterUserRepository.findByNoUsuario(noUser));
    }

    @Override
    public ResponseEntity<WaterUserDetailsRestResponse> findByIdUserDetails(Integer aguaUsuarioId) {
        WaterUserDetailsDto userDetails = waterUserRepository.getUserDetails(aguaUsuarioId);

        WaterUserDetailsRestResponse response = new WaterUserDetailsRestResponse();

        if (userDetails == null) {
            return ResponseHandler.handleNotFoundException(response, "Usuario no encontrado");
        }

        response.setData(Collections.singletonList(userDetails));
        response.addMetadata("Respuesta ok", "00", "Usuario encontrado");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WaterUserRestResponse> findByPersonaId(Integer personaId) {
        return handleFindAll(waterUserRepository.findByPersonaId(personaId));
    }

    @Override
    public ResponseEntity<WaterUserRestResponse> findByNombre(String nombre) {
        return handleFindAll(waterUserRepository.findByNombre(nombre));
    }

    @Override
    public ResponseEntity<WaterUserRestResponse> findByStreet(String calle) {
        return handleFindAll(waterUserRepository.findByStreet(calle));
    }

    @Override
    public ResponseEntity<WaterUserRestResponse> findByNombreApp(String nombre, String app) {
        return handleFindAll(waterUserRepository.findByNombreApp(nombre,app));
    }

    @Override
    @Transactional
    public WaterUserDto create(WaterUserDto request) {
        FeeEntity fee = getFeeOrThrow(request.getCuotaId());
        CatalogOptionsEntity frecuencia = getCatalogOptionOrThrow(request.getFrecuenciaPagoId());
        CatalogOptionsEntity estatusPago = getCatalogOptionOrThrow(request.getEstatusPagoId());

        WaterUserEntity waterUserToPersist = buildWaterUserEntity(request, fee, frecuencia,estatusPago);
        WaterUserEntity waterUserPersist = waterUserRepository.save(waterUserToPersist);

        return waterUserMapper.entityToDto(waterUserPersist);
    }

    @Override
    @Transactional
    public WaterUserDto updateUserDataOnly(WaterUserDto dto) {
        WaterUserEntity user = waterUserRepository.findById(dto.getAguaUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + dto.getAguaUsuarioId()));

        // Actualizar campos propios del usuario (sin tocar persona ni dirección)
        user.setNoUsuario(dto.getNoUsuario());
        user.setHabitaDomicilio(dto.getHabitaDomicilio());
        user.setTieneToma(dto.getTieneToma());
        user.setInmuebleRenta(dto.getInmuebleRenta());
        user.setInmuebleRenta(dto.getInmuebleRenta());
       // user.setEmail(dto.getEmail());
        user.setObservaciones(dto.getObservaciones());
        user.setUserIdUpdate(1);
        user.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        // Actualizar catálogos si están presentes
        if (dto.getCuotaId() != null) {
            user.setFee(feeRepository.findById(dto.getCuotaId()).orElse(null));
        }

        if (dto.getFrecuenciaPagoId() != null) {
            user.setFrecuenciaPago(catalogOptionsRepository.findById(dto.getFrecuenciaPagoId()).orElse(null));
        }

        if (dto.getEstatusPagoId() != null) {
            user.setEstatusPago(catalogOptionsRepository.findById(dto.getEstatusPagoId()).orElse(null));
        }

        if (dto.getEstatusComiteId() != null) {
            user.setEstatusComite(catalogOptionsRepository.findById(dto.getEstatusComiteId()).orElse(null));
        }

        if (dto.getEstatusTomaId() != null) {
            user.setEstatusToma(catalogOptionsRepository.findById(dto.getEstatusTomaId()).orElse(null));
        }

        if(dto.getCasaNo() != null){
            user.setWaterHouse(waterHouseRepository.findById(dto.getCasaNo()).orElse(null));
        }

        if(dto.getGrupoId() != null){
            user.setWaterGroup(waterGroupRepository.findById(dto.getGrupoId()).orElse(null));
        }

        waterUserRepository.save(user);

        return dto;
    }

    @Override
    @Transactional
    public ResponseEntity<WaterUserRestResponse> assignHouse(Integer aguaUsuarioId, Integer casaId) {
        WaterUserRestResponse response = new WaterUserRestResponse();

        Optional<WaterUserEntity> userOpt = waterUserRepository.findById(aguaUsuarioId);
        if (userOpt.isEmpty()) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        }

        Optional<WaterHouseEntity> houseOpt = waterHouseRepository.findById(casaId);
        if (houseOpt.isEmpty()) {
            return ResponseHandler.handleNotFoundException(response, "Casa no encontrada con el ID: " + casaId);
        }

        WaterUserEntity user = userOpt.get();
        user.setWaterHouse(houseOpt.get());
        user.setUserIdUpdate(1);
        user.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        waterUserRepository.save(user);

        return handleFindSingle(user);
    }

    private ResponseEntity<WaterUserRestResponse> handleFindAll(List<WaterUserEntity> waterUsers) {
        WaterUserRestResponse response = new WaterUserRestResponse();
        try {

            List<WaterUserDto> waterUserDtos = waterUsers.isEmpty()
                    ? Collections.emptyList()
                    : waterUsers.stream()
                    .sorted(Comparator.comparing(WaterUserEntity::getNoUsuario))
                    .map(waterUserMapper::entityToDto)
                    .toList();

            response.setData(waterUserDtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, USUARIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_USUARIOS_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterUserBasicRestResponse> handleFindAllBasic(List<WaterUserBasicDto> waterUsers) {
        WaterUserBasicRestResponse response = new WaterUserBasicRestResponse();
        try {
            response.setData(waterUsers);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, USUARIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_USUARIOS_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterUserRestResponse> handleFindSingle(WaterUserEntity waterUser) {
        WaterUserRestResponse response = new WaterUserRestResponse();
        try {
            if (waterUser == null) {
                throw new NoSuchElementException("Usuario no encontrados");
            }
            List<WaterUserDto> waterUserDtos = Collections.singletonList(waterUserMapper.entityToDto(waterUser));
            response.setData(waterUserDtos);
            response.addMetadata("Respuesta ok", "00", "Usuarios encontrados");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, "Usuarios no encontrados");
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar Usuarios", e);
        }
    }

    private FeeEntity getFeeOrThrow(Integer cuotaId) {
        return feeRepository.findById(cuotaId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la cuota con el ID: " + cuotaId));
    }
    private CatalogOptionsEntity getCatalogOptionOrThrow(Integer optionId) {
        return catalogOptionsRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la opcion de pago con el ID: " + optionId));
    }
    private WaterUserEntity buildWaterUserEntity(WaterUserDto request, FeeEntity fee, CatalogOptionsEntity frecuencia, CatalogOptionsEntity estatusPago) {
        return WaterUserEntity.builder()
                .person(personHelper.createPerson(request.getPerson()))
                .address(waterUserHelper.createAdress(request.getAdress()))
                .fee(fee)
                .frecuenciaPago(frecuencia)
                .estatusPago(estatusPago)
                .noUsuario(request.getNoUsuario())
                .habitaDomicilio(request.getHabitaDomicilio())
                .tieneToma(request.getTieneToma())
                .email(request.getEmail())
                .observaciones(request.getObservaciones())
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
