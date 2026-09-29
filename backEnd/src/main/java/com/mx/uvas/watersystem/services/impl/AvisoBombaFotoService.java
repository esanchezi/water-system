package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoBombaMapper;
import com.mx.uvas.watersystem.model.AvisoBombaEntity;
import com.mx.uvas.watersystem.model.AvisoBombaFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoBombaFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoBombaRepository;
import com.mx.uvas.watersystem.response.AvisoBombaFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoBombaFotoService;
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

// Fotos de respaldo de la entrega de un aviso de uso de bomba -- mismo
// patrón que AvisoAdeudoFotoService: las fotos NO se guardan en la base de
// datos ni dentro de la carpeta del proyecto -- se guardan en disco, en una
// carpeta aparte (app.uploads.dir, fuera del código) que sobrevive a cada
// recompilación/despliegue. Aquí solo se guarda la referencia (nombre de
// archivo + a qué aviso pertenece).
@Transactional
@Service
@Slf4j
public class AvisoBombaFotoService implements IAvisoBombaFotoService {

    private final IAvisoBombaFotoRepository avisoBombaFotoRepository;
    private final IAvisoBombaRepository avisoBombaRepository;
    private final AvisoBombaMapper avisoBombaMapper;
    private final Path uploadsRoot;

    public AvisoBombaFotoService(IAvisoBombaFotoRepository avisoBombaFotoRepository,
                                  IAvisoBombaRepository avisoBombaRepository,
                                  AvisoBombaMapper avisoBombaMapper,
                                  @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoBombaFotoRepository = avisoBombaFotoRepository;
        this.avisoBombaRepository = avisoBombaRepository;
        this.avisoBombaMapper = avisoBombaMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-bomba");
    }

    @Override
    public ResponseEntity<AvisoBombaFotoRestResponse> subir(Integer avisoBombaId, MultipartFile archivo) {
        AvisoBombaFotoRestResponse response = new AvisoBombaFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoBombaEntity> avisoOpt = avisoBombaRepository.findById(avisoBombaId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Aviso de bomba no encontrado con id: " + avisoBombaId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(avisoBombaId));
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

            AvisoBombaFotoEntity foto = AvisoBombaFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .avisoBomba(avisoOpt.get())
                    .build();
            AvisoBombaFotoEntity saved = avisoBombaFotoRepository.save(foto);
            response.setData(List.of(avisoBombaMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<AvisoBombaFotoRestResponse> listarPorAviso(Integer avisoBombaId) {
        AvisoBombaFotoRestResponse response = new AvisoBombaFotoRestResponse();
        try {
            List<AvisoBombaFotoEntity> fotos = avisoBombaFotoRepository.findByAvisoBomba_AvisoBombaIdAndEstatus(avisoBombaId, 1);
            response.setData(fotos.stream().map(avisoBombaMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoBombaFotoRestResponse> eliminar(Integer fotoId) {
        AvisoBombaFotoRestResponse response = new AvisoBombaFotoRestResponse();
        try {
            Optional<AvisoBombaFotoEntity> optional = avisoBombaFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoBombaFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoBomba().getAvisoBombaId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoBombaFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoBombaFotoEntity> optional = avisoBombaFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoBombaFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoBomba().getAvisoBombaId())).resolve(foto.getNombreArchivo());
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
