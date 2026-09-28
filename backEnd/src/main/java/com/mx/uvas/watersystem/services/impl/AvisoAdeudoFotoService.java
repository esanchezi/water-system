package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoAdeudoMapper;
import com.mx.uvas.watersystem.model.AvisoAdeudoEntity;
import com.mx.uvas.watersystem.model.AvisoAdeudoFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoAdeudoFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoAdeudoRepository;
import com.mx.uvas.watersystem.response.AvisoAdeudoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoAdeudoFotoService;
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

// Fotos de respaldo de la entrega de una carta de adeudo -- típicamente
// cuando no se encontró al usuario (tipoEntrega = NO_ENCONTRADO), como
// evidencia de que sí se buscó. Mismo patrón que ValvulaFotoService: las
// fotos NO se guardan en la base de datos ni dentro de la carpeta del
// proyecto -- se guardan en disco, en una carpeta aparte (app.uploads.dir,
// fuera del código) que sobrevive a cada recompilación/despliegue. Aquí
// solo se guarda la referencia (nombre de archivo + a qué carta pertenece).
@Transactional
@Service
@Slf4j
public class AvisoAdeudoFotoService implements IAvisoAdeudoFotoService {

    private final IAvisoAdeudoFotoRepository avisoAdeudoFotoRepository;
    private final IAvisoAdeudoRepository avisoAdeudoRepository;
    private final AvisoAdeudoMapper avisoAdeudoMapper;
    private final Path uploadsRoot;

    public AvisoAdeudoFotoService(IAvisoAdeudoFotoRepository avisoAdeudoFotoRepository,
                                   IAvisoAdeudoRepository avisoAdeudoRepository,
                                   AvisoAdeudoMapper avisoAdeudoMapper,
                                   @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoAdeudoFotoRepository = avisoAdeudoFotoRepository;
        this.avisoAdeudoRepository = avisoAdeudoRepository;
        this.avisoAdeudoMapper = avisoAdeudoMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-adeudo");
    }

    @Override
    public ResponseEntity<AvisoAdeudoFotoRestResponse> subir(Integer avisoAdeudoId, MultipartFile archivo) {
        AvisoAdeudoFotoRestResponse response = new AvisoAdeudoFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoAdeudoEntity> avisoOpt = avisoAdeudoRepository.findById(avisoAdeudoId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Carta de adeudo no encontrada con id: " + avisoAdeudoId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(avisoAdeudoId));
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

            AvisoAdeudoFotoEntity foto = AvisoAdeudoFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .avisoAdeudo(avisoOpt.get())
                    .build();
            AvisoAdeudoFotoEntity saved = avisoAdeudoFotoRepository.save(foto);
            response.setData(List.of(avisoAdeudoMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<AvisoAdeudoFotoRestResponse> listarPorAviso(Integer avisoAdeudoId) {
        AvisoAdeudoFotoRestResponse response = new AvisoAdeudoFotoRestResponse();
        try {
            List<AvisoAdeudoFotoEntity> fotos = avisoAdeudoFotoRepository.findByAvisoAdeudo_AvisoAdeudoIdAndEstatus(avisoAdeudoId, 1);
            response.setData(fotos.stream().map(avisoAdeudoMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoAdeudoFotoRestResponse> eliminar(Integer fotoId) {
        AvisoAdeudoFotoRestResponse response = new AvisoAdeudoFotoRestResponse();
        try {
            Optional<AvisoAdeudoFotoEntity> optional = avisoAdeudoFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoAdeudoFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoAdeudo().getAvisoAdeudoId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoAdeudoFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoAdeudoFotoEntity> optional = avisoAdeudoFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoAdeudoFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoAdeudo().getAvisoAdeudoId())).resolve(foto.getNombreArchivo());
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
