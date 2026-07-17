package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.AguaUsuarioSearchDTO;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.response.WaterUserBasicRestResponse;
import com.mx.uvas.watersystem.response.WaterUserDetailsRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IWaterUserService {
    ResponseEntity<WaterUserRestResponse> search();

    ResponseEntity<WaterUserBasicRestResponse> getListUsers();

    ResponseEntity<WaterUserRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterUserDetailsRestResponse> findByIdUserDetails(Integer aguaUsuarioId);
    ResponseEntity<WaterUserRestResponse> findByPersonaId(Integer personaId);

    ResponseEntity<WaterUserRestResponse> findByNombre(String nombre);

    ResponseEntity<WaterUserRestResponse> findByStreet(String calle);

    ResponseEntity<WaterUserRestResponse> findByNombreApp(String nombre,String app);

    WaterUserDto create(WaterUserDto request);

    WaterUserDto updateUserDataOnly(WaterUserDto dto);

    List<AguaUsuarioSearchDTO> searchUsers(String term);

    ResponseEntity<WaterUserRestResponse> assignHouse(Integer aguaUsuarioId, Integer casaId);
}
