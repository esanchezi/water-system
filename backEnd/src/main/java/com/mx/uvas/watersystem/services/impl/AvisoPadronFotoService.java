package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoPadronMapper;
import com.mx.uvas.watersystem.model.AvisoPadronEntity;
import com.mx.uvas.watersystem.model.AvisoPadronFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoPadronFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoPadronRepository;
import com.mx.uvas.watersystem.response.AvisoPadronFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoPadronFotoService;
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

// Fotos de respaldo de la entrega de un aviso de actualización de padrón --
// mismo patrón que AvisoAdeudoFotoService: las fotos NO se guardan en la
// base de datos ni dentro de la carpeta del proyecto -- se guardan en
// disco, en una carpeta aparte (app.uploads.dir, fuera del código) que
// sobrevive a cada recompilación/despliegue. Aquí solo se guarda la
// referencia (nombre de archivo + a qué aviso pertenece).
@Transactional
@Service
@Slf4j
public class AvisoPadronFotoService implements IAvisoPadronFotoService {

    private final IAvisoPadronFotoRepository avisoPadronFotoRepository;
    private final IAvisoPadronRepository avisoPadronRepository;
    private final AvisoPadronMapper avisoPadronMapper;
    private final Path uploadsRoot;

    public AvisoPadronFotoService(IAvisoPadronFotoRepository avisoPadronFotoRepository,
                                   IAvisoPadronRepository avisoPadronRepository,
                                   AvisoPadronMapper avisoPadronMapper,
                                   @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoPadronFotoRepository = avisoPadronFotoRepository;
        this.avisoPadronRepository = avisoPadronRepository;
        this.avisoPadronMapper = avisoPadronMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-padron");
    }

    @Override
    public ResponseEntity<AvisoPadronFotoRestResponse> subir(Integer avisoPadronId, MultipartFile archivo) {
        AvisoPadronFotoRestResponse response = new AvisoPadronFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoPadronEntity> avisoOpt = avisoPadronRepository.findById(avisoPadronId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Aviso de padrón no encontrado con id: " + avisoPadronId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(avisoPadronId));
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

            AvisoPadronFotoEntity foto = AvisoPadronFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .avisoPadron(avisoOpt.get())
                    .build();
            AvisoPadronFotoEntity saved = avisoPadronFotoRepository.save(foto);
            response.setData(List.of(avisoPadronMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<AvisoPadronFotoRestResponse> listarPorAviso(Integer avisoPadronId) {
        AvisoPadronFotoRestResponse response = new AvisoPadronFotoRestResponse();
        try {
            List<AvisoPadronFotoEntity> fotos = avisoPadronFotoRepository.findByAvisoPadron_AvisoPadronIdAndEstatus(avisoPadronId, 1);
            response.setData(fotos.stream().map(avisoPadronMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoPadronFotoRestResponse> eliminar(Integer fotoId) {
        AvisoPadronFotoRestResponse response = new AvisoPadronFotoRestResponse();
        try {
            Optional<AvisoPadronFotoEntity> optional = avisoPadronFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoPadronFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoPadron().getAvisoPadronId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoPadronFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoPadronFotoEntity> optional = avisoPadronFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoPadronFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoPadron().getAvisoPadronId())).resolve(foto.getNombreArchivo());
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
