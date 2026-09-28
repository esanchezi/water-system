package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.ValvulaMapper;
import com.mx.uvas.watersystem.model.ValvulaEntity;
import com.mx.uvas.watersystem.model.ValvulaFotoEntity;
import com.mx.uvas.watersystem.repositories.IValvulaFotoRepository;
import com.mx.uvas.watersystem.repositories.IValvulaRepository;
import com.mx.uvas.watersystem.response.ValvulaFotoRestResponse;
import com.mx.uvas.watersystem.services.IValvulaFotoService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

// Las fotos NO se guardan en la base de datos ni dentro de la carpeta del
// proyecto -- se guardan en disco, en una carpeta aparte (app.uploads.dir,
// fuera del código) que sobrevive a cada recompilación/despliegue. Aquí
// solo se guarda la referencia (nombre de archivo + a qué válvula
// pertenece).
@Transactional
@Service
@Slf4j
public class ValvulaFotoService implements IValvulaFotoService {

    private final IValvulaFotoRepository valvulaFotoRepository;
    private final IValvulaRepository valvulaRepository;
    private final ValvulaMapper valvulaMapper;
    private final Path uploadsRoot;

    public ValvulaFotoService(IValvulaFotoRepository valvulaFotoRepository,
                               IValvulaRepository valvulaRepository,
                               ValvulaMapper valvulaMapper,
                               @Value("${app.uploads.dir}") String uploadsDir) {
        this.valvulaFotoRepository = valvulaFotoRepository;
        this.valvulaRepository = valvulaRepository;
        this.valvulaMapper = valvulaMapper;
        this.uploadsRoot = Path.of(uploadsDir, "valvulas");
    }

    @Override
    public ResponseEntity<ValvulaFotoRestResponse> subir(Integer valvulaId, MultipartFile archivo) {
        ValvulaFotoRestResponse response = new ValvulaFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<ValvulaEntity> valvulaOpt = valvulaRepository.findById(valvulaId);
            if (valvulaOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Válvula no encontrada con id: " + valvulaId);
            }

            Path carpetaValvula = uploadsRoot.resolve(String.valueOf(valvulaId));
            Files.createDirectories(carpetaValvula);

            String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto";
            String extension = "";
            int puntoIdx = nombreOriginal.lastIndexOf('.');
            if (puntoIdx >= 0) {
                extension = nombreOriginal.substring(puntoIdx);
            }
            String nombreArchivo = UUID.randomUUID() + extension;
            Path destino = carpetaValvula.resolve(nombreArchivo);
            archivo.transferTo(destino);

            ValvulaFotoEntity foto = ValvulaFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .valvula(valvulaOpt.get())
                    .build();
            ValvulaFotoEntity saved = valvulaFotoRepository.save(foto);
            response.setData(List.of(valvulaMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<ValvulaFotoRestResponse> listarPorValvula(Integer valvulaId) {
        ValvulaFotoRestResponse response = new ValvulaFotoRestResponse();
        try {
            List<ValvulaFotoEntity> fotos = valvulaFotoRepository.findByValvula_ValvulaIdAndEstatus(valvulaId, 1);
            response.setData(fotos.stream().map(valvulaMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<ValvulaFotoRestResponse> eliminar(Integer fotoId) {
        ValvulaFotoRestResponse response = new ValvulaFotoRestResponse();
        try {
            Optional<ValvulaFotoEntity> optional = valvulaFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            ValvulaFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getValvula().getValvulaId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            valvulaFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<ValvulaFotoEntity> optional = valvulaFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        ValvulaFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getValvula().getValvulaId())).resolve(foto.getNombreArchivo());
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
