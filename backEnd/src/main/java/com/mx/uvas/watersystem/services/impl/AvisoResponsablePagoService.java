package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoGenerarRequestDto;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoPersonaInputDto;
import com.mx.uvas.watersystem.dto.CasaUsuarioCuotaDto;
import com.mx.uvas.watersystem.mapping.AvisoResponsablePagoMapper;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoEntity;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoPersonaEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoResponsablePagoRepository;
import com.mx.uvas.watersystem.repositories.IFeeAmountRepository;
import com.mx.uvas.watersystem.repositories.IWaterHouseRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AvisoResponsablePagoRestResponse;
import com.mx.uvas.watersystem.response.CasaUsuarioCuotaRestResponse;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Orquesta el flujo de "Aviso sobre personas responsables de pago del
// domicilio" -- a diferencia de las demás cartas, se arma por Casa (no por
// usuario): no hay lista de "candidatos" que calcular, se busca la casa
// directo (ver AvisoResponsablePagoController/frontend) y se llena el
// formulario con las personas responsables. No expone nada por sí solo vía
// REST -- eso lo hace AvisoResponsablePagoController.
@Service
@AllArgsConstructor
public class AvisoResponsablePagoService {

    private final IWaterHouseRepository waterHouseRepository;
    private final IWaterUserRepository waterUserRepository;
    private final IFeeAmountRepository feeAmountRepository;
    private final IAvisoResponsablePagoRepository avisoResponsablePagoRepository;
    private final AvisoResponsablePagoMapper avisoResponsablePagoMapper;
    private final AvisoResponsablePagoPdfService avisoResponsablePagoPdfService;
    private final CurrentUserService currentUserService;

    // Usuarios ya dados de alta y ligados a esta casa, con su cuota vigente
    // del año actual -- para el selector "agregar usuario existente" del
    // formulario (Ely pidió poder ver la cuota que ya están pagando antes
    // de agregarlos a la resolución).
    @Transactional(readOnly = true)
    public ResponseEntity<CasaUsuarioCuotaRestResponse> usuariosDeLaCasa(Integer casaId) {
        CasaUsuarioCuotaRestResponse response = new CasaUsuarioCuotaRestResponse();
        try {
            Optional<WaterHouseEntity> casaOpt = waterHouseRepository.findById(casaId);
            if (casaOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Casa no encontrada");
                return ResponseEntity.badRequest().body(response);
            }
            WaterHouseEntity casa = casaOpt.get();
            int anioActual = LocalDate.now().getYear();
            Map<Integer, Double> montoCuotaPorCuotaId = feeAmountRepository.findByVigencia(anioActual).stream()
                    .filter(fa -> fa.getFee() != null)
                    .collect(Collectors.toMap(
                            fa -> fa.getFee().getCuotaId(),
                            fa -> fa.getCuota() != null ? fa.getCuota().doubleValue() : 0d,
                            (a, b) -> a
                    ));

            List<WaterUserEntity> usuarios = casa.getListWaterUser() != null
                    ? casa.getListWaterUser().stream()
                            .filter(u -> u.getNoUsuario() != null && u.getNoUsuario() != 0)
                            .toList()
                    : List.of();

            List<CasaUsuarioCuotaDto> lista = usuarios.stream().map(u -> {
                CasaUsuarioCuotaDto dto = new CasaUsuarioCuotaDto();
                dto.setAguaUsuarioId(u.getAguaUsuarioId());
                dto.setNoUsuario(u.getNoUsuario());
                dto.setNombreCompleto(buildNombreCompleto(u.getPerson()));
                dto.setCuotaVigente(u.getFee() != null ? montoCuotaPorCuotaId.get(u.getFee().getCuotaId()) : null);
                return dto;
            }).toList();

            response.setData(lista);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Usuarios encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar los usuarios de la casa", e);
        }
    }

    public record GenerarResponsablePagoResultado(byte[] pdfBytes, boolean generado, String mensajeError) {
    }

    @Transactional
    public GenerarResponsablePagoResultado generar(AvisoResponsablePagoGenerarRequestDto request) throws IOException, DocumentException {
        if (request.getCasaId() == null) {
            return new GenerarResponsablePagoResultado(new byte[0], false, "Indica la casa");
        }
        Optional<WaterHouseEntity> casaOpt = waterHouseRepository.findById(request.getCasaId());
        if (casaOpt.isEmpty()) {
            return new GenerarResponsablePagoResultado(new byte[0], false, "Casa no encontrada");
        }
        WaterHouseEntity casa = casaOpt.get();

        List<AvisoResponsablePagoPersonaInputDto> personasInput = request.getPersonas() != null ? request.getPersonas() : new ArrayList<>();

        int anioActual = LocalDate.now().getYear();
        Map<Integer, Double> montoCuotaPorCuotaId = feeAmountRepository.findByVigencia(anioActual).stream()
                .filter(fa -> fa.getFee() != null)
                .collect(Collectors.toMap(
                        fa -> fa.getFee().getCuotaId(),
                        fa -> fa.getCuota() != null ? fa.getCuota().doubleValue() : 0d,
                        (a, b) -> a
                ));

        Integer folio = Optional.ofNullable(avisoResponsablePagoRepository.findMaxFolio()).orElse(0) + 1;
        Integer userIdAdd = currentUserService.getCurrentUserId();
        LocalDateTime ahora = LocalDateTime.now();
        String casaNoTexto = buildCasaNoTexto(casa);
        String domicilioToma = buildDomicilio(casa);

        AvisoResponsablePagoEntity entity = AvisoResponsablePagoEntity.builder()
                .folioNotificacion(folio)
                .noCasa(casa.getCasaNo())
                .noCasaTexto(casaNoTexto)
                .domicilioToma(domicilioToma)
                .motivoSolicitud(request.getMotivoSolicitud())
                .fechaSolicitud(request.getFechaSolicitud())
                .observacionesComite(request.getObservacionesComite())
                .estatus(1)
                .userIdAdd(userIdAdd)
                .dateAdd(ahora)
                .casa(casa)
                .build();

        List<CartaResponsablePagoDatos.CartaResponsablePagoPersona> personasParaPdf = new ArrayList<>();
        int orden = 1;
        for (AvisoResponsablePagoPersonaInputDto input : personasInput) {
            if (input == null) {
                continue;
            }
            String nombreCompleto = input.getNombreCompleto();
            WaterUserEntity usuarioLigado = null;
            Double cuotaSnapshot = null;

            if (input.getAguaUsuarioId() != null) {
                usuarioLigado = waterUserRepository.findById(input.getAguaUsuarioId()).orElse(null);
                if (usuarioLigado != null) {
                    if (nombreCompleto == null || nombreCompleto.isBlank()) {
                        nombreCompleto = buildNombreCompleto(usuarioLigado.getPerson());
                    }
                    if (usuarioLigado.getFee() != null) {
                        cuotaSnapshot = montoCuotaPorCuotaId.get(usuarioLigado.getFee().getCuotaId());
                    }
                }
            }

            if (nombreCompleto == null || nombreCompleto.isBlank()) {
                continue;
            }

            entity.addPersona(AvisoResponsablePagoPersonaEntity.builder()
                    .orden(orden)
                    .nombreCompleto(nombreCompleto)
                    .parentesco(input.getParentesco())
                    .familiaCuota(input.getFamiliaCuota())
                    .waterUser(usuarioLigado)
                    .cuotaVigenteSnapshot(cuotaSnapshot)
                    .build());

            personasParaPdf.add(new CartaResponsablePagoDatos.CartaResponsablePagoPersona(
                    orden, nombreCompleto, input.getParentesco(), input.getFamiliaCuota()));
            orden++;
        }

        CartaResponsablePagoDatos carta = new CartaResponsablePagoDatos(
                folio, casaNoTexto, domicilioToma, request.getMotivoSolicitud(), request.getFechaSolicitud(),
                personasParaPdf, request.getObservacionesComite());

        byte[] pdf = avisoResponsablePagoPdfService.generar(carta);
        avisoResponsablePagoRepository.save(entity);

        return new GenerarResponsablePagoResultado(pdf, true, null);
    }

    // Trae activos Y cancelados -- el frontend los distingue con el campo
    // "cancelado" del DTO y los oculta por default, mismo patrón que las
    // demás cartas.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoResponsablePagoRestResponse> historial() {
        AvisoResponsablePagoRestResponse response = new AvisoResponsablePagoRestResponse();
        try {
            List<AvisoResponsablePagoEntity> avisos = avisoResponsablePagoRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoResponsablePagoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial", e);
        }
    }

    // Historial de una casa en particular -- para el acordeón "Responsables
    // de pago" dentro de la ficha de la casa.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoResponsablePagoRestResponse> historialPorCasa(Integer casaId) {
        AvisoResponsablePagoRestResponse response = new AvisoResponsablePagoRestResponse();
        try {
            List<AvisoResponsablePagoEntity> avisos = avisoResponsablePagoRepository
                    .findByCasa_CasaIdAndEstatusInOrderByFolioNotificacionDesc(casaId, List.of(1, 0));
            response.setData(avisos.stream().map(avisoResponsablePagoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial de la casa", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoResponsablePagoRestResponse> marcarEntregada(Integer responsablePagoId, AvisoResponsablePagoEntregaRequestDto request) {
        AvisoResponsablePagoRestResponse response = new AvisoResponsablePagoRestResponse();
        try {
            Optional<AvisoResponsablePagoEntity> avisoOpt = avisoResponsablePagoRepository.findById(responsablePagoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoResponsablePagoEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            aviso.setComentarioEntrega(request.getComentarioEntrega());
            avisoResponsablePagoRepository.save(aviso);

            response.setData(List.of(avisoResponsablePagoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoResponsablePagoRestResponse> cancelar(Integer responsablePagoId) {
        AvisoResponsablePagoRestResponse response = new AvisoResponsablePagoRestResponse();
        try {
            Optional<AvisoResponsablePagoEntity> avisoOpt = avisoResponsablePagoRepository.findById(responsablePagoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoResponsablePagoEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoResponsablePagoRepository.save(aviso);

            response.setData(List.of(avisoResponsablePagoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoResponsablePagoRestResponse> reactivar(Integer responsablePagoId) {
        AvisoResponsablePagoRestResponse response = new AvisoResponsablePagoRestResponse();
        try {
            Optional<AvisoResponsablePagoEntity> avisoOpt = avisoResponsablePagoRepository.findById(responsablePagoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoResponsablePagoEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoResponsablePagoRepository.save(aviso);

            response.setData(List.of(avisoResponsablePagoMapper.entityToDto(aviso)));
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

    private String buildDomicilio(WaterHouseEntity casa) {
        if (casa == null) {
            return "";
        }
        if (casa.getCatCalle() != null && casa.getCatCalle().getNombre() != null) {
            String casaNo = casa.getCasaNo() != null ? " #" + casa.getCasaNo() : "";
            return (casa.getCatCalle().getNombre() + casaNo).trim();
        }
        return casa.getNombre() != null ? casa.getNombre() : "";
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
