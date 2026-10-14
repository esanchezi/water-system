package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.AvisoMedidorFotoRestResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IAvisoMedidorFotoService {

    ResponseEntity<AvisoMedidorFotoRestResponse> subir(Integer avisoMedidorId, MultipartFile archivo);

    ResponseEntity<AvisoMedidorFotoRestResponse> listarPorAviso(Integer avisoMedidorId);

    ResponseEntity<AvisoMedidorFotoRestResponse> eliminar(Integer fotoId);

    // Resultado de leer el archivo de una foto: el contenido y su tipo real
    // (image/jpeg, image/png...), para que el navegador lo muestre en vez
    // de forzar una descarga.
    record ArchivoFoto(Resource resource, MediaType contentType) {
    }

    ArchivoFoto obtenerArchivo(Integer fotoId);
}
