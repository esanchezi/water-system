package com.mx.uvas.watersystem.utils;

import com.mx.uvas.watersystem.response.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor(access = AccessLevel.NONE)
public class ResponseHandler {

    public static <T> ResponseEntity<T> handleNotFoundException(T response, String errorMessage) {
        addMetadata(response, Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    public static <T> ResponseEntity<T> handleInternalServerError(T response, String errorMessage, Exception exception) {
        log.error(errorMessage, exception);
        addMetadata(response, Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> void addMetadata(T response, String message, String code, String description) {
        if (response instanceof Response) {
            ((Response<?>) response).addMetadata(message, code, description);
        } else {
            log.warn("Cannot add metadata to the response. The response does not implement the Response interface.");
        }
    }
}
