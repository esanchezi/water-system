package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.WaterUserRevisionMapper;
import com.mx.uvas.watersystem.model.WaterUserRevisionEntity;
import com.mx.uvas.watersystem.model.WaterUserRevisionFotoEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserRevisionFotoRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRevisionRepository;
import com.mx.uvas.watersystem.response.WaterUserRevisionFotoRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserRevisionFotoService;
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

// Fotos de respaldo de una revisión de usuario -- mismo patrón que
// AvisoAdeudoFotoService: las fotos se guardan en disco, en una carpeta
// aparte (app.uploads.dir, fuera del código) que sobrevive a cada
// recompilación/despliegue. Aquí solo se guarda la referencia (nombre de
// archivo + a qué revisión pertenece).
@Transactional
@Service
@Slf4j
public class WaterUserRevisionFotoService implements IWaterUserRevisionFotoService {

    private final IWaterUserRevisionFotoRepository waterUserRevisionFotoRepository;
    private final IWaterUserRevisionRepository waterUserRevisionRepository;
    private final WaterUserRevisionMapper waterUserRevisionMapper;
    private final Path uploadsRoot;

    public WaterUserRevisionFotoService(IWaterUserRevisionFotoRepository waterUserRevisionFotoRepository,
                                         IWaterUserRevisionRepository waterUserRevisionRepository,
                                         WaterUserRevisionMapper waterUserRevisionMapper,
                                         @Value("${app.uploads.dir}") String uploadsDir) {
        this.waterUserRevisionFotoRepository = waterUserRevisionFotoRepository;
        this.waterUserRevisionRepository = waterUserRevisionRepository;
        this.waterUserRevisionMapper = waterUserRevisionMapper;
        this.uploadsRoot = Path.of(uploadsDir, "revisiones-usuario");
    }

    @Override
    public ResponseEntity<WaterUserRevisionFotoRestResponse> subir(Integer revisionId, MultipartFile archivo) {
        WaterUserRevisionFotoRestResponse response = new WaterUserRevisionFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<WaterUserRevisionEntity> revisionOpt = waterUserRevisionRepository.findById(revisionId);
            if (revisionOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Revisión no encontrada con id: " + revisionId);
            }

            Path carpetaRevision = uploadsRoot.resolve(String.valueOf(revisionId));
            Files.createDirectories(carpetaRevision);

            String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto";
            String extension = "";
            int puntoIdx = nombreOriginal.lastIndexOf('.');
            if (puntoIdx >= 0) {
                extension = nombreOriginal.substring(puntoIdx);
            }
            String nombreArchivo = UUID.randomUUID() + extension;
            Path destino = carpetaRevision.resolve(nombreArchivo);
            archivo.transferTo(destino);

            WaterUserRevisionFotoEntity foto = WaterUserRevisionFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .revision(revisionOpt.get())
                    .build();
            WaterUserRevisionFotoEntity saved = waterUserRevisionFotoRepository.save(foto);
            response.setData(List.of(waterUserRevisionMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<WaterUserRevisionFotoRestResponse> listarPorRevision(Integer revisionId) {
        WaterUserRevisionFotoRestResponse response = new WaterUserRevisionFotoRestResponse();
        try {
            List<WaterUserRevisionFotoEntity> fotos =
                    waterUserRevisionFotoRepository.findByRevision_RevisionIdAndEstatus(revisionId, 1);
            response.setData(fotos.stream().map(waterUserRevisionMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<WaterUserRevisionFotoRestResponse> eliminar(Integer fotoId) {
        WaterUserRevisionFotoRestResponse response = new WaterUserRevisionFotoRestResponse();
        try {
            Optional<WaterUserRevisionFotoEntity> optional = waterUserRevisionFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            WaterUserRevisionFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getRevision().getRevisionId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            waterUserRevisionFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<WaterUserRevisionFotoEntity> optional = waterUserRevisionFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        WaterUserRevisionFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getRevision().getRevisionId())).resolve(foto.getNombreArchivo());
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
