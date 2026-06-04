package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterGroupDto;
import com.mx.uvas.watersystem.response.WaterGroupRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterGroupService {
    ResponseEntity<WaterGroupRestResponse> findAll();

    ResponseEntity<WaterGroupRestResponse> createWaterGroup(WaterGroupDto dto);

}