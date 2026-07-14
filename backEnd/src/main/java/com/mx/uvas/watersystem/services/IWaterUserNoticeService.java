package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.response.WaterUserNoticeRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserNoticeService {

    ResponseEntity<WaterUserNoticeRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterUserNoticeRestResponse> create(WaterUserNoticeDto request);

}
