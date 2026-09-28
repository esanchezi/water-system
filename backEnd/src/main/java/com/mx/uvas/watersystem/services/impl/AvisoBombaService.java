package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AvisoBombaCandidatoDto;
import com.mx.uvas.watersystem.dto.AvisoBombaEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoBombaGenerarRequestDto;
import com.mx.uvas.watersystem.mapping.AvisoBombaMapper;
import com.mx.uvas.watersystem.model.AvisoBombaEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoBombaRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AvisoBombaCandidatoRestResponse;
import com.mx.uvas.watersystem.response.AvisoBombaRestResponse;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Orquesta el flujo de "Aviso por uso indebido de bomba" (Art. 23): a
// diferencia de Cartas de adeudo, NO hay cálculo de deuda -- cualquier
// usuario activo de la calle aplica por igual (el aviso se emite por un
// reporte de uso de bomba, no por adeudo). Folio propio, consecutivo e
// independiente del de avisos de adeudo. No expone nada por sí solo vía
// REST -- eso lo hace AvisoBombaController.
@Service
@AllArgsConstructor
public class AvisoBombaService {

    private final IWaterUserRepository waterUserRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IAvisoBombaRepository avisoBombaRepository;
    private final AvisoBombaMapper avisoBombaMapper;
    private final AvisoBombaPdfService avisoBombaPdfService;
    private final CurrentUserService currentUserService;

    // Todos los usuarios activos de una calle -- mismo criterio de
    // "coincide con la calle" que ya usa AdeudoLuzService.calcularParaCalle
    // (casa del catastro con esa calle asignada, O dirección libre que
    // contenga el nombre de la calle), pero sin ningún cálculo de deuda:
    // aquí aplica cualquier usuario, esté o no al corriente.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoBombaCandidatoRestResponse> candidatosPorCalle(Integer calleId) {
        AvisoBombaCandidatoRestResponse response = new AvisoBombaCandidatoRestResponse();
        try {
            var calle = catalogOptionsRepository.findById(calleId).orElse(null);
            String calleNombreLower = calle != null && calle.getNombre() != null ? calle.getNombre().toLowerCase() : null;

            List<AvisoBombaCandidatoDto> lista = waterUserRepository.findAllActiveWithHouse().stream()
                    .filter(u -> u.getNoUsuario() != null && u.getNoUsuario() != 0)
                    .filter(u -> coincideCalle(u, calleId, calleNombreLower))
                    .map(this::usuarioACandidatoDto)
                    .toList();

            response.setData(lista);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Candidatos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular los candidatos", e);
        }
    }

    private boolean coincideCalle(WaterUserEntity user, Integer calleId, String calleNombreLower) {
        boolean porCasa = user.getWaterHouse() != null
                && user.getWaterHouse().getCatCalle() != null
                && calleId.equals(user.getWaterHouse().getCatCalle().getCatalogoOpcionesId());
        if (porCasa) {
            return true;
        }
        if (calleNombreLower == null) {
            return false;
        }
        String direccionLibre = user.getAddress() != null ? user.getAddress().getCalle() : null;
        return direccionLibre != null && direccionLibre.toLowerCase().contains(calleNombreLower);
    }

    private AvisoBombaCandidatoDto usuarioACandidatoDto(WaterUserEntity user) {
        WaterHouseEntity casa = user.getWaterHouse();
        AvisoBombaCandidatoDto dto = new AvisoBombaCandidatoDto();
        dto.setAguaUsuarioId(user.getAguaUsuarioId());
        dto.setNoUsuario(user.getNoUsuario());
        dto.setNombreCompleto(buildNombreCompleto(user.getPerson()));
        dto.setCasaNo(casa != null ? casa.getCasaNo() : null);
        dto.setCasaNoTexto(buildCasaNoTexto(casa));
        dto.setCalleNombre(casa != null && casa.getCatCalle() != null
                ? casa.getCatCalle().getNombre()
                : (user.getAddress() != null ? user.getAddress().getCalle() : null));
        dto.setDomicilioToma(buildDomicilio(user));
        dto.setEstatusComiteId(user.getEstatusComite() != null ? user.getEstatusComite().getCatalogoOpcionesId() : null);
        dto.setEstatusComiteNombre(user.getEstatusComite() != null ? user.getEstatusComite().getNombre() : null);
        return dto;
    }

    public record GenerarAvisosBombaResultado(byte[] pdfBytes, int totalGeneradas) {
    }

    @Transactional
    public GenerarAvisosBombaResultado generar(AvisoBombaGenerarRequestDto request) throws IOException, DocumentException {
        List<Integer> aguaUsuarioIds = request.getAguaUsuarioIds() != null ? request.getAguaUsuarioIds() : new ArrayList<>();
        if (aguaUsuarioIds.isEmpty()) {
            return new GenerarAvisosBombaResultado(new byte[0], 0);
        }

        List<WaterUserEntity> usuarios = waterUserRepository.findAllById(aguaUsuarioIds);

        List<CartaBombaDatos> cartas = new ArrayList<>();
        List<AvisoBombaEntity> paraGuardar = new ArrayList<>();

        Integer siguienteFolio = Optional.ofNullable(avisoBombaRepository.findMaxFolio()).orElse(0) + 1;
        Integer userIdAdd = currentUserService.getCurrentUserId();
        LocalDateTime ahora = LocalDateTime.now();

        for (WaterUserEntity usuario : usuarios) {
            Integer folio = siguienteFolio++;
            WaterHouseEntity casa = usuario.getWaterHouse();
            String casaNoTexto = buildCasaNoTexto(casa);
            String domicilioToma = buildDomicilio(usuario);
            String nombreConNumero = usuario.getNoUsuario() + " - " + buildNombreCompleto(usuario.getPerson());

            cartas.add(new CartaBombaDatos(folio, nombreConNumero, casaNoTexto, domicilioToma, request.getFechaReporte()));

            paraGuardar.add(AvisoBombaEntity.builder()
                    .folioNotificacion(folio)
                    .nombreUsuarioTitular(buildNombreCompleto(usuario.getPerson()))
                    .noCasa(casa != null ? casa.getCasaNo() : null)
                    .noCasaTexto(casaNoTexto)
                    .domicilioToma(domicilioToma)
                    .fechaReporte(request.getFechaReporte())
                    .estatus(1)
                    .userIdAdd(userIdAdd)
                    .dateAdd(ahora)
                    .waterUser(usuario)
                    .build());
        }

        if (cartas.isEmpty()) {
            return new GenerarAvisosBombaResultado(new byte[0], 0);
        }

        byte[] pdf = avisoBombaPdfService.generarLote(cartas);
        avisoBombaRepository.saveAll(paraGuardar);

        return new GenerarAvisosBombaResultado(pdf, cartas.size());
    }

    // Trae activos Y cancelados -- el frontend los distingue con el campo
    // "cancelado" del DTO y los oculta por default, mismo patrón que
    // avisos de adeudo.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoBombaRestResponse> historial() {
        AvisoBombaRestResponse response = new AvisoBombaRestResponse();
        try {
            List<AvisoBombaEntity> avisos = avisoBombaRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoBombaMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoBombaRestResponse> marcarEntregada(Integer avisoBombaId, AvisoBombaEntregaRequestDto request) {
        AvisoBombaRestResponse response = new AvisoBombaRestResponse();
        try {
            Optional<AvisoBombaEntity> avisoOpt = avisoBombaRepository.findById(avisoBombaId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoBombaEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            avisoBombaRepository.save(aviso);

            response.setData(List.of(avisoBombaMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoBombaRestResponse> cancelar(Integer avisoBombaId) {
        AvisoBombaRestResponse response = new AvisoBombaRestResponse();
        try {
            Optional<AvisoBombaEntity> avisoOpt = avisoBombaRepository.findById(avisoBombaId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoBombaEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoBombaRepository.save(aviso);

            response.setData(List.of(avisoBombaMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoBombaRestResponse> reactivar(Integer avisoBombaId) {
        AvisoBombaRestResponse response = new AvisoBombaRestResponse();
        try {
            Optional<AvisoBombaEntity> avisoOpt = avisoBombaRepository.findById(avisoBombaId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoBombaEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoBombaRepository.save(aviso);

            response.setData(List.of(avisoBombaMapper.entityToDto(aviso)));
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
