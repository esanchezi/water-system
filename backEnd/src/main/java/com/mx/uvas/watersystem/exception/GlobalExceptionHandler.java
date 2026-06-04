package com.mx.uvas.watersystem.exception;

import com.mx.uvas.watersystem.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ControllerAdvice
public class GlobalExceptionHandler<T> {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<RestResponse<T>> handleCatalogNotFoundException(NotFoundException e) {
        RestResponse<T> response = new RestResponse<>();
        response.addMetadata("Respuesta nok", "-1", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestResponse<T>> handleException(Exception e) {
        RestResponse<T> response = new RestResponse<>();
        response.addMetadata("Respuesta nok", "-1", "Global: Error interno al procesar la solicitud");
        System.out.println(e.getCause());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
