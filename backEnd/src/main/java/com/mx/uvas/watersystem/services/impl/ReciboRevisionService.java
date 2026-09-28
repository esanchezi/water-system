package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ReciboRevisionCreateDto;
import com.mx.uvas.watersystem.dto.ReciboRevisionUpdateDto;
import com.mx.uvas.watersystem.mapping.ReciboRevisionMapper;
import com.mx.uvas.watersystem.model.ReciboRevisionEntity;
import com.mx.uvas.watersystem.model.ReciboRevisionFotoEntity;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IReciboRevisionFotoRepository;
import com.mx.uvas.watersystem.repositories.IReciboRevisionRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.response.ReciboRevisionFotoRestResponse;
import com.mx.uvas.watersystem.response.ReciboRevisionRestResponse;
import com.mx.uvas.watersystem.services.IReciboRevisionService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Revisión de recibos de papel contra lo capturado en el sistema. A
// diferencia de ValvulaFotoService, aquí SÍ se recomprime la imagen antes
// de guardarla (los recibos se suben desde el celular en alta resolución
// y no hace falta esa calidad para leerlos a mano después) -- se reduce a
// un ancho máximo razonable y se guarda como JPEG, para no llenar la
// carpeta de uploads con fotos pesadas. No hay ningún tipo de lectura
// automática (OCR) del contenido: folio y usuario se capturan a mano
// viendo la foto (ver agregarRevision), que es lo que se decidió después
// de platicar el riesgo/costo de automatizarlo.
@Transactional
@Service
@Slf4j
public class ReciboRevisionService implements IReciboRevisionService {

    private static final int ANCHO_MAXIMO_PX = 1600;
    private static final float CALIDAD_JPEG = 0.75f;
    private static final List<String> RESULTADOS_VALIDOS =
            List.of("PENDIENTE", "COINCIDE", "DISCREPANCIA", "NO_ENCONTRADO");

    private final IReciboRevisionFotoRepository fotoRepository;
    private final IReciboRevisionRepository revisionRepository;
    private final IWaterReceiptRepository waterReceiptRepository;
    private final ReciboRevisionMapper reciboRevisionMapper;
    private final CurrentUserService currentUserService;
    private final Path uploadsRoot;

    public ReciboRevisionService(IReciboRevisionFotoRepository fotoRepository,
                                  IReciboRevisionRepository revisionRepository,
                                  IWaterReceiptRepository waterReceiptRepository,
                                  ReciboRevisionMapper reciboRevisionMapper,
                                  CurrentUserService currentUserService,
                                  @Value("${app.uploads.dir}") String uploadsDir) {
        this.fotoRepository = fotoRepository;
        this.revisionRepository = revisionRepository;
        this.waterReceiptRepository = waterReceiptRepository;
        this.reciboRevisionMapper = reciboRevisionMapper;
        this.currentUserService = currentUserService;
        this.uploadsRoot = Path.of(uploadsDir, "recibos-revision");
    }

    @Override
    public ResponseEntity<ReciboRevisionFotoRestResponse> subirFoto(MultipartFile archivo, String observaciones) {
        ReciboRevisionFotoRestResponse response = new ReciboRevisionFotoRestResponse();
        try {
            if (archivo == null || archivo.isEmpty()) {
                return ResponseHandler.handleBadRequest(response, "No se recibió ningún archivo");
            }
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseHandler.handleBadRequest(response, "Solo se aceptan imágenes");
            }

            Files.createDirectories(uploadsRoot);
            String nombreArchivo = UUID.randomUUID() + ".jpg";
            Path destino = uploadsRoot.resolve(nombreArchivo);
            String tipoGuardado = comprimirYGuardar(archivo, destino);

            ReciboRevisionFotoEntity foto = ReciboRevisionFotoEntity.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto")
                    .contentType(tipoGuardado)
                    .observaciones(observaciones)
                    .estatus(1)
                    .userIdAdd(currentUserService.getCurrentUserId())
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();
            ReciboRevisionFotoEntity saved = fotoRepository.save(foto);
            response.setData(List.of(reciboRevisionMapper.fotoEntityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto subida correctamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseHandler.handleInternalServerError(response, "No se pudo guardar el archivo en el servidor", e);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al subir la foto", e);
        }
    }

    // Reduce la imagen a un ancho máximo y la vuelve a guardar como JPEG con
    // calidad recortada -- si por algún motivo no se puede leer/decodificar
    // (formato raro), se guarda el archivo tal cual llegó, sin comprimir,
    // para no perder la foto.
    private String comprimirYGuardar(MultipartFile archivo, Path destino) throws IOException {
        BufferedImage original;
        try (var in = archivo.getInputStream()) {
            original = ImageIO.read(in);
        }
        if (original == null) {
            archivo.transferTo(destino);
            return archivo.getContentType();
        }

        BufferedImage aEscribir = original;
        if (original.getWidth() > ANCHO_MAXIMO_PX) {
            int nuevoAlto = (int) Math.round(original.getHeight() * (ANCHO_MAXIMO_PX / (double) original.getWidth()));
            BufferedImage redimensionada = new BufferedImage(ANCHO_MAXIMO_PX, nuevoAlto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = redimensionada.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, ANCHO_MAXIMO_PX, nuevoAlto, null);
            g.dispose();
            aEscribir = redimensionada;
        } else if (original.getType() != BufferedImage.TYPE_INT_RGB) {
            // JPEG no soporta canal alfa -- si la imagen lo trae (ej. venía de
            // un PNG), se aplana sobre fondo blanco para poder guardarla.
            BufferedImage plana = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = plana.createGraphics();
            g.drawImage(original, 0, 0, java.awt.Color.WHITE, null);
            g.dispose();
            aEscribir = plana;
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            archivo.transferTo(destino);
            return archivo.getContentType();
        }
        ImageWriter writer = writers.next();
        ImageWriteParam parametros = writer.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(CALIDAD_JPEG);
        try (var salida = new FileImageOutputStream(destino.toFile())) {
            writer.setOutput(salida);
            writer.write(null, new IIOImage(aEscribir, null, null), parametros);
        } finally {
            writer.dispose();
        }
        return MediaType.IMAGE_JPEG_VALUE;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ReciboRevisionFotoRestResponse> listarFotos() {
        ReciboRevisionFotoRestResponse response = new ReciboRevisionFotoRestResponse();
        try {
            List<ReciboRevisionFotoEntity> fotos = fotoRepository.findByEstatusOrderByDateAddDesc(1);
            response.setData(fotos.stream().map(reciboRevisionMapper::fotoEntityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Fotos encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las fotos", e);
        }
    }

    @Override
    public ResponseEntity<ReciboRevisionFotoRestResponse> eliminarFoto(Integer fotoId) {
        ReciboRevisionFotoRestResponse response = new ReciboRevisionFotoRestResponse();
        try {
            Optional<ReciboRevisionFotoEntity> optional = fotoRepository.findById(fotoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            ReciboRevisionFotoEntity foto = optional.get();
            Path archivo = uploadsRoot.resolve(foto.getNombreArchivo());
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException e) {
                log.warn("No se pudo borrar el archivo de la foto {}: {}", fotoId, e.getMessage());
            }
            fotoRepository.delete(foto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Foto eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la foto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoFoto obtenerArchivo(Integer fotoId) {
        Optional<ReciboRevisionFotoEntity> optional = fotoRepository.findById(fotoId);
        if (optional.isEmpty()) {
            return null;
        }
        ReciboRevisionFotoEntity foto = optional.get();
        Path archivo = uploadsRoot.resolve(foto.getNombreArchivo());
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

    @Override
    public ResponseEntity<ReciboRevisionRestResponse> agregarRevision(Integer fotoId, ReciboRevisionCreateDto dto) {
        ReciboRevisionRestResponse response = new ReciboRevisionRestResponse();
        try {
            Optional<ReciboRevisionFotoEntity> fotoOpt = fotoRepository.findById(fotoId);
            if (fotoOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Foto no encontrada con id: " + fotoId);
            }
            if (dto.getNoFolioCapturado() == null) {
                return ResponseHandler.handleBadRequest(response, "El número de folio es obligatorio");
            }

            // Cruce automático SOLO por folio+usuario -- nunca se compara el
            // monto solo, porque el monto en letra a mano es justo lo menos
            // confiable de leer y donde no nos podemos equivocar.
            WaterReceiptEntity recibo = waterReceiptRepository.findByNoFolio(dto.getNoFolioCapturado());
            String resultado;
            if (recibo == null) {
                resultado = "NO_ENCONTRADO";
            } else {
                WaterUserEntity usuarioSistema = recibo.getWaterUser();
                Integer noUsuarioSistema = usuarioSistema != null ? usuarioSistema.getNoUsuario() : null;
                if (dto.getNoUsuarioCapturado() == null
                        || (noUsuarioSistema != null && noUsuarioSistema.equals(dto.getNoUsuarioCapturado()))) {
                    resultado = "COINCIDE";
                } else {
                    resultado = "DISCREPANCIA";
                }
            }

            ReciboRevisionEntity revision = ReciboRevisionEntity.builder()
                    .noFolioCapturado(dto.getNoFolioCapturado())
                    .noUsuarioCapturado(dto.getNoUsuarioCapturado())
                    .montoTexto(dto.getMontoTexto())
                    .observaciones(dto.getObservaciones())
                    .resultado(resultado)
                    .estatus(1)
                    .userIdAdd(currentUserService.getCurrentUserId())
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .foto(fotoOpt.get())
                    .waterReceipt(recibo)
                    .build();
            ReciboRevisionEntity saved = revisionRepository.save(revision);
            response.setData(List.of(reciboRevisionMapper.revisionEntityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisión registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la revisión", e);
        }
    }

    @Override
    public ResponseEntity<ReciboRevisionRestResponse> actualizarRevision(Integer revisionId, ReciboRevisionUpdateDto dto) {
        ReciboRevisionRestResponse response = new ReciboRevisionRestResponse();
        try {
            Optional<ReciboRevisionEntity> optional = revisionRepository.findById(revisionId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Revisión no encontrada con id: " + revisionId);
            }
            if (dto.getResultado() != null && !RESULTADOS_VALIDOS.contains(dto.getResultado())) {
                return ResponseHandler.handleBadRequest(response, "Resultado inválido: " + dto.getResultado());
            }
            ReciboRevisionEntity revision = optional.get();
            if (dto.getResultado() != null) {
                revision.setResultado(dto.getResultado());
            }
            if (dto.getObservaciones() != null) {
                revision.setObservaciones(dto.getObservaciones());
            }
            ReciboRevisionEntity saved = revisionRepository.save(revision);
            response.setData(List.of(reciboRevisionMapper.revisionEntityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisión actualizada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar la revisión", e);
        }
    }

    @Override
    public ResponseEntity<ReciboRevisionRestResponse> eliminarRevision(Integer revisionId) {
        ReciboRevisionRestResponse response = new ReciboRevisionRestResponse();
        try {
            Optional<ReciboRevisionEntity> optional = revisionRepository.findById(revisionId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Revisión no encontrada con id: " + revisionId);
            }
            revisionRepository.delete(optional.get());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisión eliminada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la revisión", e);
        }
    }
}
