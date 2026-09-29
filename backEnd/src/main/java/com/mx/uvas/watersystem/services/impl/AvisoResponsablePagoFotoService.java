package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoResponsablePagoMapper;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoEntity;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoResponsablePagoFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoResponsablePagoRepository;
import com.mx.uvas.watersystem.response.AvisoResponsablePagoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoResponsablePagoFotoService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Fotos de respaldo de la entrega de un aviso de responsables de pago --
// mismo patrón que AvisoAdeudoFotoService: las fotos NO se guardan en la
// base de datos ni dentro de la carpeta del proyecto -- se guardan en
// disco, en una carpeta aparte (app.uploads.dir, fuera del código) que
// sobrevive a cada recompilación/despliegue. Aquí solo se guarda la
// referencia (nombre de archivo + a qué aviso pertenece).
@Transactional
@Service
@Slf4j
public class AvisoResponsablePagoFotoService implements IAvisoResponsablePagoFotoService {

    private final IAvisoResponsablePagoFotoRepository avisoResponsablePagoFotoRepository;
    private final IAvisoResponsablePagoRepository avisoResponsablePagoRepository;
    private final AvisoResponsablePagoMapper avisoResponsablePagoMapper;
    private final Path uploadsRoot;

    public AvisoResponsablePagoFotoService(IAvisoResponsablePagoFotoRepository avisoResponsablePagoFotoRepository,
                                            IAvisoResponsablePagoRepository avisoResponsablePagoRepository,
                                            AvisoResponsablePagoMapper avisoResponsablePagoMapper,
                                            @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoResponsablePagoFotoRepository = avisoResponsablePagoFotoRepository;
        this.avisoResponsablePagoRepository = avisoResponsablePagoRepository;
        this.avisoResponsablePagoMapper = avisoResponsablePagoMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-responsable-pago");
    }

    @Override
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> subir(Integer responsablePagoId, MultipartFile archivo) {
        AvisoResponsablePagoFotoRestResponse response = new AvisoResponsablePagoFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoResponsablePagoEntity> avisoOpt = avisoResponsablePagoRepository.findById(responsablePagoId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Aviso de responsables de pago no encontrado con id: " + responsablePagoId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(responsablePagoId));
            Files.createDirectories(carpetaAviso);

            String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto";
            String extension = "";
            int puntoIdx = nombreOriginal.lastIndexOf('.');
            if (puntoIdx >= 0) {
                extension = nombreOriginal.substring(puntoIdx);
            }
            String nombreArchivo = UUID.randomUUID() + extension;
            Path destino = carpetaAviso.resolve(nombreArchivo);
            archivo.transferTo(destino);

            AvisoResponsablePagoFotoEntity foto = AvisoResponsablePagoFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .responsablePago(avisoOpt.get())
                    .build();
            AvisoResponsablePagoFotoEntity saved = avisoResponsablePagoFotoRepository.save(foto);
            response.setData(List.of(avisoResponsablePagoMapper.fotoEntityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto subida correctamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseHandler.handleInternalServerError(response, "No se pudo guardar el archivo en el servidor", e);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al subir la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> listarPorAviso(Integer responsablePagoId) {
        AvisoResponsablePagoFotoRestResponse response = new AvisoResponsablePagoFotoRestResponse();
        try {
            List<AvisoResponsablePagoFotoEntity> fotos = avisoResponsablePagoFotoRepository.findByResponsablePago_ResponsablePagoIdAndEstatus(responsablePagoId, 1);
            response.setData(fotos.stream().map(avisoResponsablePagoMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> eliminar(Integer fotoId) {
        AvisoResponsablePagoFotoRestResponse response = new AvisoResponsablePagoFotoRestResponse();
        try {
            Optional<AvisoResponsablePagoFotoEntity> optional = avisoResponsablePagoFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoResponsablePagoFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getResponsablePago().getResponsablePagoId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoResponsablePagoFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoResponsablePagoFotoEntity> optional = avisoResponsablePagoFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoResponsablePagoFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getResponsablePago().getResponsablePagoId())).resolve(foto.getNombreArchivo());
        if (!Files.exists(archivo)) {
            return null;
        }
        MediaType tipo;
        try {
            tipo = MediaType.parseMediaType(foto.getContentType() != null ? foto.getContentType() : "application/octet-stream");
        } catch (Exception e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }
        return new ArchivoFoto(new FileSystemResource(archivo), tipo);
    }
}
