package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.WaterUserRevisionFotoRestResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IWaterUserRevisionFotoService {

    ResponseEntity<WaterUserRevisionFotoRestResponse> subir(Integer revisionId, MultipartFile archivo);

    ResponseEntity<WaterUserRevisionFotoRestResponse> listarPorRevision(Integer revisionId);

    ResponseEntity<WaterUserRevisionFotoRestResponse> eliminar(Integer fotoId);

    record ArchivoFoto(Resource resource, MediaType contentType) {
    }

    ArchivoFoto obtenerArchivo(Integer fotoId);
}
