package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.AvisoInformativoAdeudoFotoRestResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IAvisoInformativoAdeudoFotoService {

    ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> subir(Integer avisoInformativoAdeudoId, MultipartFile archivo);

    ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> listarPorAviso(Integer avisoInformativoAdeudoId);

    ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> eliminar(Integer fotoId);

    record ArchivoFoto(Resource resource, MediaType contentType) {
    }

    ArchivoFoto obtenerArchivo(Integer fotoId);
}
