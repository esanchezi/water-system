package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.mapping.AvisoInformativoAdeudoMapper;
import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoEntity;
import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoFotoEntity;
import com.mx.uvas.watersystem.repositories.IAvisoInformativoAdeudoFotoRepository;
import com.mx.uvas.watersystem.repositories.IAvisoInformativoAdeudoRepository;
import com.mx.uvas.watersystem.response.AvisoInformativoAdeudoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoInformativoAdeudoFotoService;
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

// Fotos de respaldo de la entrega de un Aviso Informativo de Adeudo --
// mismo patrón que AvisoBombaFotoService/AvisoAdeudoFotoService: las fotos
// se guardan en disco (app.uploads.dir), no en la base de datos.
@Transactional
@Service
@Slf4j
public class AvisoInformativoAdeudoFotoService implements IAvisoInformativoAdeudoFotoService {

    private final IAvisoInformativoAdeudoFotoRepository avisoInformativoAdeudoFotoRepository;
    private final IAvisoInformativoAdeudoRepository avisoInformativoAdeudoRepository;
    private final AvisoInformativoAdeudoMapper avisoInformativoAdeudoMapper;
    private final Path uploadsRoot;

    public AvisoInformativoAdeudoFotoService(IAvisoInformativoAdeudoFotoRepository avisoInformativoAdeudoFotoRepository,
                                              IAvisoInformativoAdeudoRepository avisoInformativoAdeudoRepository,
                                              AvisoInformativoAdeudoMapper avisoInformativoAdeudoMapper,
                                              @Value("${app.uploads.dir}") String uploadsDir) {
        this.avisoInformativoAdeudoFotoRepository = avisoInformativoAdeudoFotoRepository;
        this.avisoInformativoAdeudoRepository = avisoInformativoAdeudoRepository;
        this.avisoInformativoAdeudoMapper = avisoInformativoAdeudoMapper;
        this.uploadsRoot = Path.of(uploadsDir, "avisos-informativos-adeudo");
    }

    @Override
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> subir(Integer avisoInformativoAdeudoId, MultipartFile archivo) {
        AvisoInformativoAdeudoFotoRestResponse response = new AvisoInformativoAdeudoFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }
            Optional<AvisoInformativoAdeudoEntity> avisoOpt = avisoInformativoAdeudoRepository.findById(avisoInformativoAdeudoId);
            if (avisoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Aviso informativo de adeudo no encontrado con id: " + avisoInformativoAdeudoId);
            }

            Path carpetaAviso = uploadsRoot.resolve(String.valueOf(avisoInformativoAdeudoId));
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

            AvisoInformativoAdeudoFotoEntity foto = AvisoInformativoAdeudoFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(nombreOriginal)
                    .contentType(contentType)
                    .estatus(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .avisoInformativoAdeudo(avisoOpt.get())
                    .build();
            AvisoInformativoAdeudoFotoEntity saved = avisoInformativoAdeudoFotoRepository.save(foto);
            response.setData(List.of(avisoInformativoAdeudoMapper.fotoEntityToDto(saved)));
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
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> listarPorAviso(Integer avisoInformativoAdeudoId) {
        AvisoInformativoAdeudoFotoRestResponse response = new AvisoInformativoAdeudoFotoRestResponse();
        try {
            List<AvisoInformativoAdeudoFotoEntity> fotos = avisoInformativoAdeudoFotoRepository
                    .findByAvisoInformativoAdeudo_AvisoInformativoAdeudoIdAndEstatus(avisoInformativoAdeudoId, 1);
            response.setData(fotos.stream().map(avisoInformativoAdeudoMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> eliminar(Integer fotoId) {
        AvisoInformativoAdeudoFotoRestResponse response = new AvisoInformativoAdeudoFotoRestResponse();
        try {
            Optional<AvisoInformativoAdeudoFotoEntity> optional = avisoInformativoAdeudoFotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            AvisoInformativoAdeudoFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoInformativoAdeudo().getAvisoInformativoAdeudoId())).resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            avisoInformativoAdeudoFotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<AvisoInformativoAdeudoFotoEntity> optional = avisoInformativoAdeudoFotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        AvisoInformativoAdeudoFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(String.valueOf(foto.getAvisoInformativoAdeudo().getAvisoInformativoAdeudoId())).resolve(foto.getNombreArchivo());
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
