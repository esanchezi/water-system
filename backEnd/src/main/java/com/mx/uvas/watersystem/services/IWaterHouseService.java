package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterHouseDto;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.response.WaterHouseRestResponse;
import com.mx.uvas.watersystem.utils.RestResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IWaterHouseService {

    ResponseEntity<WaterHouseRestResponse> findAll();

    ResponseEntity<WaterHouseRestResponse> createWaterHouse(WaterHouseDto dto);
    ResponseEntity<WaterHouseRestResponse> updateWaterHouse(Integer casaId, WaterHouseDto dto);


}