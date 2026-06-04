package com.mx.uvas.watersystem.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// BaseService
public abstract class BaseService<T, D, R extends BaseRestResponse<D>> {

    protected ResponseEntity<R> handleFindAll(
            List<T> entities,
            Function<T, D> mapper,
            Supplier<R> responseSupplier,
            String successMsg,
            String errorMsg) {

        R response = responseSupplier.get();
        try {
            List<D> dtoList = entities.stream()
                    .map(mapper)
                    .toList();

            response.setData(dtoList);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, successMsg);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, errorMsg, e);
        }
    }
}
