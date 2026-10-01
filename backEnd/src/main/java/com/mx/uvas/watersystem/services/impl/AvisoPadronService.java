package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AvisoPadronCandidatoDto;
import com.mx.uvas.watersystem.dto.AvisoPadronEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoPadronGenerarRequestDto;
import com.mx.uvas.watersystem.mapping.AvisoPadronMapper;
import com.mx.uvas.watersystem.model.AvisoPadronEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoPadronRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AvisoPadronCandidatoRestResponse;
import com.mx.uvas.watersystem.response.AvisoPadronRestResponse;
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

// Orquesta el flujo de "Aviso para actualización del padrón de habitantes"
// (Art. 5, 15, 15 Bis y 15 Ter): igual que Aviso de bomba, NO hay cálculo de
// deuda -- cualquier usuario activo de la calle aplica por igual. Folio
// propio, consecutivo e independiente del de avisos de adeudo/bomba. No
// expone nada por sí solo vía REST -- eso lo hace AvisoPadronController.
@Service
@AllArgsConstructor
public class AvisoPadronService {

    private final IWaterUserRepository waterUserRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IAvisoPadronRepository avisoPadronRepository;
    private final AvisoPadronMapper avisoPadronMapper;
    private final AvisoPadronPdfService avisoPadronPdfService;
    private final CurrentUserService currentUserService;

    // Todos los usuarios activos de una calle -- mismo criterio de
    // "coincide con la calle" que ya usa AdeudoLuzService.calcularParaCalle
    // (casa del catastro con esa calle asignada, O dirección libre que
    // contenga el nombre de la calle), pero sin ningún cálculo de deuda:
    // aquí aplica cualquier usuario, esté o no al corriente.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoPadronCandidatoRestResponse> candidatosPorCalle(Integer calleId) {
        AvisoPadronCandidatoRestResponse response = new AvisoPadronCandidatoRestResponse();
        try {
            var calle = catalogOptionsRepository.findById(calleId).orElse(null);
            String calleNombreLower = calle != null && calle.getNombre() != null ? calle.getNombre().toLowerCase() : null;

            List<AvisoPadronCandidatoDto> lista = waterUserRepository.findAllActiveWithHouse().stream()
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

    // v9 (sept. 2026): mismo fix que AdeudoLuzService.coincideCalle() (bug
    // reportado por Ely, caso usuario 116) -- si ya tiene casa con calle de
    // catálogo asignada, esa es la única fuente que se usa (se respeta
    // aunque no coincida); el texto libre solo es respaldo cuando NO tiene
    // casa asignada en absoluto. Antes caía al texto libre incluso teniendo
    // casa, causando falsos positivos entre calles/zonas distintas.
    private boolean coincideCalle(WaterUserEntity user, Integer calleId, String calleNombreLower) {
        if (user.getWaterHouse() != null && user.getWaterHouse().getCatCalle() != null) {
            return calleId.equals(user.getWaterHouse().getCatCalle().getCatalogoOpcionesId());
        }
        if (calleNombreLower == null) {
            return false;
        }
        String direccionLibre = user.getAddress() != null ? user.getAddress().getCalle() : null;
        return direccionLibre != null && direccionLibre.toLowerCase().contains(calleNombreLower);
    }

    private AvisoPadronCandidatoDto usuarioACandidatoDto(WaterUserEntity user) {
        WaterHouseEntity casa = user.getWaterHouse();
        AvisoPadronCandidatoDto dto = new AvisoPadronCandidatoDto();
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

    public record GenerarAvisosPadronResultado(byte[] pdfBytes, int totalGeneradas) {
    }

    @Transactional
    public GenerarAvisosPadronResultado generar(AvisoPadronGenerarRequestDto request) throws IOException, DocumentException {
        List<Integer> aguaUsuarioIds = request.getAguaUsuarioIds() != null ? request.getAguaUsuarioIds() : new ArrayList<>();
        if (aguaUsuarioIds.isEmpty()) {
            return new GenerarAvisosPadronResultado(new byte[0], 0);
        }

        List<WaterUserEntity> usuarios = waterUserRepository.findAllById(aguaUsuarioIds);

        List<CartaPadronDatos> cartas = new ArrayList<>();
        List<AvisoPadronEntity> paraGuardar = new ArrayList<>();

        Integer siguienteFolio = Optional.ofNullable(avisoPadronRepository.findMaxFolio()).orElse(0) + 1;
        Integer userIdAdd = currentUserService.getCurrentUserId();
        LocalDateTime ahora = LocalDateTime.now();

        for (WaterUserEntity usuario : usuarios) {
            Integer folio = siguienteFolio++;
            WaterHouseEntity casa = usuario.getWaterHouse();
            String casaNoTexto = buildCasaNoTexto(casa);
            String domicilioToma = buildDomicilio(usuario);
            String nombreConNumero = usuario.getNoUsuario() + " - " + buildNombreCompleto(usuario.getPerson());

            String motivoSolicitud = request.getMotivoSolicitud() != null && !request.getMotivoSolicitud().isBlank()
                    ? request.getMotivoSolicitud().trim() : null;

            cartas.add(new CartaPadronDatos(folio, nombreConNumero, casaNoTexto, domicilioToma, request.getFechaPresentacion(), motivoSolicitud));

            paraGuardar.add(AvisoPadronEntity.builder()
                    .folioNotificacion(folio)
                    .nombreUsuarioTitular(buildNombreCompleto(usuario.getPerson()))
                    .noCasa(casa != null ? casa.getCasaNo() : null)
                    .noCasaTexto(casaNoTexto)
                    .domicilioToma(domicilioToma)
                    .fechaPresentacion(request.getFechaPresentacion())
                    .motivoSolicitud(motivoSolicitud)
                    .estatus(1)
                    .userIdAdd(userIdAdd)
                    .dateAdd(ahora)
                    .waterUser(usuario)
                    .build());
        }

        if (cartas.isEmpty()) {
            return new GenerarAvisosPadronResultado(new byte[0], 0);
        }

        byte[] pdf = avisoPadronPdfService.generarLote(cartas);
        avisoPadronRepository.saveAll(paraGuardar);

        return new GenerarAvisosPadronResultado(pdf, cartas.size());
    }

    // Trae activos Y cancelados -- el frontend los distingue con el campo
    // "cancelado" del DTO y los oculta por default, mismo patrón que
    // avisos de adeudo/bomba.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoPadronRestResponse> historial() {
        AvisoPadronRestResponse response = new AvisoPadronRestResponse();
        try {
            List<AvisoPadronEntity> avisos = avisoPadronRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoPadronMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial", e);
        }
    }

    // Historial completo (activos + cancelados) de un usuario específico --
    // para el acordeón "Cartas generadas" en su ficha (details-user).
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoPadronRestResponse> porUsuario(Integer aguaUsuarioId) {
        AvisoPadronRestResponse response = new AvisoPadronRestResponse();
        try {
            List<AvisoPadronEntity> avisos = avisoPadronRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(aguaUsuarioId, List.of(1, 0));
            response.setData(avisos.stream().map(avisoPadronMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Avisos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar los avisos del usuario", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoPadronRestResponse> marcarEntregada(Integer avisoPadronId, AvisoPadronEntregaRequestDto request) {
        AvisoPadronRestResponse response = new AvisoPadronRestResponse();
        try {
            Optional<AvisoPadronEntity> avisoOpt = avisoPadronRepository.findById(avisoPadronId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoPadronEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            aviso.setComentarioEntrega(request.getComentarioEntrega());
            avisoPadronRepository.save(aviso);

            response.setData(List.of(avisoPadronMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoPadronRestResponse> cancelar(Integer avisoPadronId) {
        AvisoPadronRestResponse response = new AvisoPadronRestResponse();
        try {
            Optional<AvisoPadronEntity> avisoOpt = avisoPadronRepository.findById(avisoPadronId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoPadronEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoPadronRepository.save(aviso);

            response.setData(List.of(avisoPadronMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoPadronRestResponse> reactivar(Integer avisoPadronId) {
        AvisoPadronRestResponse response = new AvisoPadronRestResponse();
        try {
            Optional<AvisoPadronEntity> avisoOpt = avisoPadronRepository.findById(avisoPadronId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoPadronEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoPadronRepository.save(aviso);

            response.setData(List.of(avisoPadronMapper.entityToDto(aviso)));
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
