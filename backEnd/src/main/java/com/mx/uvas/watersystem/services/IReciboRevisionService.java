package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.ReciboRevisionCreateDto;
import com.mx.uvas.watersystem.dto.ReciboRevisionUpdateDto;
import com.mx.uvas.watersystem.response.ReciboRevisionFotoRestResponse;
import com.mx.uvas.watersystem.response.ReciboRevisionRestResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IReciboRevisionService {

    ResponseEntity<ReciboRevisionFotoRestResponse> subirFoto(MultipartFile archivo, String observaciones);

    ResponseEntity<ReciboRevisionFotoRestResponse> listarFotos();

    ResponseEntity<ReciboRevisionFotoRestResponse> eliminarFoto(Integer fotoId);

    // Resultado de leer el archivo de una foto: el contenido y su tipo real,
    // igual que ArchivoFoto en IValvulaFotoService.
    record ArchivoFoto(Resource resource, MediaType contentType) {
    }

    ArchivoFoto obtenerArchivo(Integer fotoId);

    ResponseEntity<ReciboRevisionRestResponse> agregarRevision(Integer fotoId, ReciboRevisionCreateDto dto);

    ResponseEntity<ReciboRevisionRestResponse> actualizarRevision(Integer revisionId, ReciboRevisionUpdateDto dto);

    ResponseEntity<ReciboRevisionRestResponse> eliminarRevision(Integer revisionId);
}
