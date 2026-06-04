package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseService<T> {

    public ResponseEntity<RestResponse<T>> createResponse(HttpStatus status, List<T> data, String message) {
        RestResponse<T> response = new RestResponse<>();
        response.setData(data);

        if (HttpStatus.OK.equals(status)) {
            response.addMetadata("Respuesta ok", "00", message);
        } else {
            response.addMetadata("Respuesta nok", "-1", message);
        }

        return new ResponseEntity<>(response, status);
    }

    /*public ResponseEntity<WaterUserResponseRest> createSuccessResponse(List<WaterUserDto> data, String message) {
        WaterUserResponseRest response = new WaterUserResponseRest();
        response.setData(data);
        response.addMetadata("Respuesta ok", "00", message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<WaterUserResponseRest> createNotFoundResponse(String message) {
        WaterUserResponseRest response = new WaterUserResponseRest();
        response.addMetadata("Respuesta nok", "-1", message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<WaterUserResponseRest> createErrorResponse(String message) {
        WaterUserResponseRest response = new WaterUserResponseRest();
        response.addMetadata("Respuesta nok", "-1", message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/
}
