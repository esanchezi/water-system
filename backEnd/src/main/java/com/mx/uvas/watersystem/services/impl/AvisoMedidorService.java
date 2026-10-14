package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AvisoMedidorEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoMedidorGenerarRequestDto;
import com.mx.uvas.watersystem.mapping.AvisoMedidorMapper;
import com.mx.uvas.watersystem.model.AvisoMedidorEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoMedidorRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AvisoMedidorRestResponse;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Orquesta el flujo de "Aviso de Instalación de Medidor" (prueba de
// consumo, Art. 7): a diferencia de Cartas de adeudo, NO hay
// Primero/Segundo aviso, y a diferencia de Aviso de Bomba/Padrón, NO se
// genera en lote -- el Comité elige a mano un único domicilio de prueba y
// captura los datos propios de esa instalación (fecha, número de serie,
// lectura inicial). Folio propio, consecutivo e independiente del resto de
// avisos. No expone nada por sí solo vía REST -- eso lo hace
// AvisoMedidorController.
@Service
@AllArgsConstructor
public class AvisoMedidorService {

    private final IWaterUserRepository waterUserRepository;
    private final IAvisoMedidorRepository avisoMedidorRepository;
    private final AvisoMedidorMapper avisoMedidorMapper;
    private final AvisoMedidorPdfService avisoMedidorPdfService;
    private final CurrentUserService currentUserService;

    public record GenerarAvisoMedidorResultado(byte[] pdfBytes, boolean generado) {
    }

    @Transactional
    public GenerarAvisoMedidorResultado generar(AvisoMedidorGenerarRequestDto request) throws IOException, DocumentException {
        if (request.getAguaUsuarioId() == null) {
            return new GenerarAvisoMedidorResultado(new byte[0], false);
        }
        Optional<WaterUserEntity> usuarioOpt = waterUserRepository.findById(request.getAguaUsuarioId());
        if (usuarioOpt.isEmpty()) {
            return new GenerarAvisoMedidorResultado(new byte[0], false);
        }
        WaterUserEntity usuario = usuarioOpt.get();

        Integer folio = Optional.ofNullable(avisoMedidorRepository.findMaxFolio()).orElse(0) + 1;
        WaterHouseEntity casa = usuario.getWaterHouse();
        String casaNoTexto = buildCasaNoTexto(casa);
        String domicilioToma = buildDomicilio(usuario);
        String nombreConNumero = usuario.getNoUsuario() + " - " + buildNombreCompleto(usuario.getPerson());

        CartaMedidorDatos carta = new CartaMedidorDatos(folio, nombreConNumero, casaNoTexto, domicilioToma,
                request.getFechaInstalacion(), request.getNoSerieMedidor(), request.getLecturaInicialMedidor());

        byte[] pdf = avisoMedidorPdfService.generarLote(List.of(carta));

        AvisoMedidorEntity entidad = AvisoMedidorEntity.builder()
                .folioNotificacion(folio)
                .nombreUsuarioTitular(buildNombreCompleto(usuario.getPerson()))
                .noCasa(casa != null ? casa.getCasaNo() : null)
                .noCasaTexto(casaNoTexto)
                .domicilioToma(domicilioToma)
                .fechaInstalacion(request.getFechaInstalacion())
                .noSerieMedidor(request.getNoSerieMedidor())
                .lecturaInicialMedidor(request.getLecturaInicialMedidor())
                .estatus(1)
                .userIdAdd(currentUserService.getCurrentUserId())
                .dateAdd(LocalDateTime.now())
                .waterUser(usuario)
                .build();
        avisoMedidorRepository.save(entidad);

        return new GenerarAvisoMedidorResultado(pdf, true);
    }

    // Trae activos Y cancelados -- el frontend los distingue con el campo
    // "cancelado" del DTO y los oculta por default, mismo patrón que el
    // resto de avisos.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoMedidorRestResponse> historial() {
        AvisoMedidorRestResponse response = new AvisoMedidorRestResponse();
        try {
            List<AvisoMedidorEntity> avisos = avisoMedidorRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoMedidorMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial", e);
        }
    }

    // Historial completo (activos + cancelados) de un usuario específico --
    // para el acordeón "Cartas generadas" en su ficha (details-user).
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoMedidorRestResponse> porUsuario(Integer aguaUsuarioId) {
        AvisoMedidorRestResponse response = new AvisoMedidorRestResponse();
        try {
            List<AvisoMedidorEntity> avisos = avisoMedidorRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(aguaUsuarioId, List.of(1, 0));
            response.setData(avisos.stream().map(avisoMedidorMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Avisos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar los avisos del usuario", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoMedidorRestResponse> marcarEntregada(Integer avisoMedidorId, AvisoMedidorEntregaRequestDto request) {
        AvisoMedidorRestResponse response = new AvisoMedidorRestResponse();
        try {
            Optional<AvisoMedidorEntity> avisoOpt = avisoMedidorRepository.findById(avisoMedidorId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoMedidorEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            aviso.setComentarioEntrega(request.getComentarioEntrega());
            avisoMedidorRepository.save(aviso);

            response.setData(List.of(avisoMedidorMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoMedidorRestResponse> cancelar(Integer avisoMedidorId) {
        AvisoMedidorRestResponse response = new AvisoMedidorRestResponse();
        try {
            Optional<AvisoMedidorEntity> avisoOpt = avisoMedidorRepository.findById(avisoMedidorId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoMedidorEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoMedidorRepository.save(aviso);

            response.setData(List.of(avisoMedidorMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoMedidorRestResponse> reactivar(Integer avisoMedidorId) {
        AvisoMedidorRestResponse response = new AvisoMedidorRestResponse();
        try {
            Optional<AvisoMedidorEntity> avisoOpt = avisoMedidorRepository.findById(avisoMedidorId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoMedidorEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoMedidorRepository.save(aviso);

            response.setData(List.of(avisoMedidorMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso reactivado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al reactivar el aviso", e);
        }
    }

    private String buildCasaNoTexto(WaterHouseEntity casa) {
        if (casa == null || casa.getCasaNo() == null) {
            return "";
        }
        String lado = casa.getLado();
        return lado != null && !lado.isBlank() ? casa.getCasaNo() + "-" + lado : String.valueOf(casa.getCasaNo());
    }

    private String buildDomicilio(WaterUserEntity user) {
        if (user.getAddress() != null && user.getAddress().getCalle() != null && !user.getAddress().getCalle().isBlank()) {
            String numero = user.getAddress().getNumero();
            return user.getAddress().getCalle() + (numero != null && !numero.isBlank() ? " #" + numero : "");
        }
        WaterHouseEntity casa = user.getWaterHouse();
        if (casa != null) {
            String calle = casa.getCatCalle() != null ? casa.getCatCalle().getNombre() : "";
            String casaNo = casa.getCasaNo() != null ? " #" + casa.getCasaNo() : "";
            return (calle + casaNo).trim();
        }
        return "";
    }

    private String buildNombreCompleto(PersonEntity person) {
        if (person == null) return "";
        return String.join(" ",
                        nullToEmpty(person.getNombre()), nullToEmpty(person.getNombre2()),
                        nullToEmpty(person.getApp()), nullToEmpty(person.getApm()))
                .replaceAll("\\s+", " ").trim();
    }

    private String nullToEmpty(String valor) {
        return valor != null ? valor : "";
    }
}
