package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoMedidorMapper;
import com.mx.uvas.watersystem.model.AvisoMedidorEntity;
import com.mx.uvas.watersystem.model.AvisoMedidorFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoMedidorFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoMedidorRepository;
import com.mx.uvas.watersystem.response.AvisoMedidorFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoMedidorFotoService;
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

// Fotos de respaldo de la entrega de un aviso de instalación de medidor --
// mismo patrón que AvisoBombaFotoService: las fotos NO se guardan en la base
// de datos ni dentro de la carpeta del proyecto -- se guardan en disco, en
// una carpeta aparte (app.uploads.dir, fuera del código) que sobrevive a
// cada recompilación/despliegue. Aquí solo se guarda la referencia (nombre
// de archivo + a qué aviso pertenece).
@Transactional
@Service
@Slf4j
public class AvisoMedidorFotoService implements IAvisoMedidorFotoService {

    private final IAvisoMedidorFotoRepository avisoMedidorFotoRepository;
    private final IAvisoMedidorRepository avisoMedidorRepository;
    private final AvisoMedidorMapper avisoMedidorMapper;
    private final Path uploadsRoot;

    public AvisoMedidorFotoService(IAvisoMedidorFotoRepository avisoMedidorFotoRepository,
                                    IAvisoMedidorRepository avisoMedidorRepository,
                                    AvisoMedidorMapper avisoMedidorMapper,
                                    @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoMedidorFotoRepository = avisoMedidorFotoRepository;
        this.avisoMedidorRepository = avisoMedidorRepository;
        this.avisoMedidorMapper = avisoMedidorMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-medidor");
    }

    @Override
    public ResponseEntity<AvisoMedidorFotoRestResponse> subir(Integer avisoMedidorId, MultipartFile archivo) {
        AvisoMedidorFotoRestResponse response = new AvisoMedidorFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoMedidorEntity> avisoOpt = avisoMedidorRepository.findById(avisoMedidorId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Aviso de medidor no encontrado con id: " + avisoMedidorId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(avisoMedidorId));
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

            AvisoMedidorFotoEntity foto = AvisoMedidorFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .avisoMedidor(avisoOpt.get())
                    .build();
            AvisoMedidorFotoEntity saved = avisoMedidorFotoRepository.save(foto);
            response.setData(List.of(avisoMedidorMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<AvisoMedidorFotoRestResponse> listarPorAviso(Integer avisoMedidorId) {
        AvisoMedidorFotoRestResponse response = new AvisoMedidorFotoRestResponse();
        try {
            List<AvisoMedidorFotoEntity> fotos = avisoMedidorFotoRepository.findByAvisoMedidor_AvisoMedidorIdAndEstatus(avisoMedidorId, 1);
            response.setData(fotos.stream().map(avisoMedidorMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoMedidorFotoRestResponse> eliminar(Integer fotoId) {
        AvisoMedidorFotoRestResponse response = new AvisoMedidorFotoRestResponse();
        try {
            Optional<AvisoMedidorFotoEntity> optional = avisoMedidorFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoMedidorFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoMedidor().getAvisoMedidorId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                // No se pudo borrar el archivo físico -- se sigue borrando el
                // registro de todas formas, para no dejar la foto "fantasma"
                // atorada en la lista si el archivo ya no existía.
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoMedidorFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoMedidorFotoEntity> optional = avisoMedidorFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoMedidorFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoMedidor().getAvisoMedidorId())).resolve(foto.getNombreArchivo());
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
