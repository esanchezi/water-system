package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.CatalogMapper;
import com.mx.uvas.watersystem.repositories.ICatalogRepository;
import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.model.CatalogEntity;
import com.mx.uvas.watersystem.response.CatalogRestResponse;
import com.mx.uvas.watersystem.services.ICatalogService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class CatalogService implements ICatalogService {

    private final ICatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;

    private static final String CATALOG_FOUND_MESSAGE = "Catálogo encontrado";
    private static final String CATALOG_NOT_FOUND_MESSAGE = "Catálogo no encontrado";
    private static final String ERROR_SEARCHING_CATALOG_MESSAGE = "Error al consultar Catálogo por ID";

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CatalogRestResponse> searchById(Integer id) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            catalogRepository.findById(id).ifPresentOrElse(
                    catalogEntity -> handleCatalogFound(catalogEntity, response),
                    () -> ResponseHandler.handleNotFoundException(response, CATALOG_NOT_FOUND_MESSAGE  + " [Catalog ID: " + id + "]")
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_CATALOG_MESSAGE ,e);
        }
    }
    private void handleCatalogFound(CatalogEntity catalogEntity, CatalogRestResponse response) {
        CatalogDto catalogDto = catalogMapper.entityToDto(catalogEntity);
        response.setData(Collections.singletonList(catalogDto));
        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CATALOG_FOUND_MESSAGE);
    }
}
