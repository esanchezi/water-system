package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AdeudoLuzUsuarioDto;
import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoGenerarRequestDto;
import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoItemRequestDto;
import com.mx.uvas.watersystem.mapping.AvisoInformativoAdeudoMapper;
import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoInformativoAdeudoRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AvisoInformativoAdeudoRestResponse;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Orquesta el "Aviso Informativo de Adeudo" -- notificación PREVIA y más
// suave que las Cartas de adeudo (ver AvisoAdeudoService), pedida por Ely
// para avisar a ciertos usuarios elegidos A MANO antes de iniciar el
// proceso formal de avisos y suspensión. La selección del usuario SÍ es
// manual (mismo criterio que "usuario manual" en Cartas de adeudo, pero
// como flujo PRINCIPAL, no de respaldo) -- pero el adeudo/multa/último
// pago que se imprimen se calculan con el mismo AdeudoLuzService que ya
// usan las Cartas de adeudo (calcularParaUsuarios), NO se capturan a mano
// (pedido explícito de Ely: "acerca del calculo debe ser lo que ya se
// tiene, no que yo lo capture"). Folio propio, consecutivo, totalmente
// independiente del de Cartas de adeudo. No hay cargo automático de $200
// (Art. 20) ni seguimiento de "atendido": es puramente informativo.
@Service
@AllArgsConstructor
public class AvisoInformativoAdeudoService {

    private final IWaterUserRepository waterUserRepository;
    private final IAvisoInformativoAdeudoRepository avisoInformativoAdeudoRepository;
    private final AvisoInformativoAdeudoMapper avisoInformativoAdeudoMapper;
    private final AvisoInformativoAdeudoPdfService avisoInformativoAdeudoPdfService;
    private final AdeudoLuzService adeudoLuzService;
    private final CurrentUserService currentUserService;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public record GenerarAvisosInformativosResultado(byte[] pdfBytes, int totalGeneradas) {
    }

    @Transactional
    public GenerarAvisosInformativosResultado generar(AvisoInformativoAdeudoGenerarRequestDto request) throws IOException, DocumentException {
        List<AvisoInformativoAdeudoItemRequestDto> items = request.getUsuarios() != null ? request.getUsuarios() : new ArrayList<>();
        if (items.isEmpty()) {
            return new GenerarAvisosInformativosResultado(new byte[0], 0);
        }

        // El adeudo/multa/último pago se calculan igual que en Cartas de
        // adeudo (AdeudoLuzService), NO se capturan a mano -- se pide en
        // batch para los usuarios elegidos, una sola vez.
        List<Integer> aguaUsuarioIds = items.stream()
                .map(AvisoInformativoAdeudoItemRequestDto::getAguaUsuarioId)
                .filter(id -> id != null)
                .toList();
        Map<Integer, AdeudoLuzUsuarioDto> calculadoPorUsuario = new HashMap<>();
        if (!aguaUsuarioIds.isEmpty()) {
            adeudoLuzService.calcularParaUsuarios(aguaUsuarioIds)
                    .forEach(dto -> calculadoPorUsuario.put(dto.getAguaUsuarioId(), dto));
        }

        List<CartaInformativoAdeudoDatos> avisos = new ArrayList<>();
        List<AvisoInformativoAdeudoEntity> paraGuardar = new ArrayList<>();

        Integer siguienteFolio = Optional.ofNullable(avisoInformativoAdeudoRepository.findMaxFolio()).orElse(0) + 1;
        Integer userIdAdd = currentUserService.getCurrentUserId();
        LocalDateTime ahora = LocalDateTime.now();

        for (AvisoInformativoAdeudoItemRequestDto item : items) {
            if (item.getAguaUsuarioId() == null) continue;
            Optional<WaterUserEntity> usuarioOpt = waterUserRepository.findById(item.getAguaUsuarioId());
            if (usuarioOpt.isEmpty()) continue;
            WaterUserEntity usuario = usuarioOpt.get();

            // Puede no venir en el mapa (ej. usuario sin adeudo de luz
            // pendiente, o en renuncia temporal) -- se genera igual, con
            // ceros/blancos, porque la selección es manual y a propósito
            // (Ely eligió avisarle a este usuario en particular).
            AdeudoLuzUsuarioDto calculado = calculadoPorUsuario.get(item.getAguaUsuarioId());
            String periodosAdeudados = calculado != null ? calculado.getPeriodosAdeudadosTexto() : "";
            Double adeudoTotal = calculado != null ? calculado.getAdeudoTotal() : 0.0;
            Double multaAcumulada = calculado != null ? calculado.getMultaAcumulada() : 0.0;
            Integer noFolioUltimoPago = calculado != null ? calculado.getNoFolioUltimoPago() : null;
            LocalDateTime fechaUltimoPago = calculado != null ? calculado.getFechaUltimoPago() : null;

            // Cooperación extraordinaria de mantenimiento de cajón (Art. 10)
            // ya pendiente -- igual que multaAcumulada, solo se LEE lo que ya
            // se tiene calculado (AdeudoLuzService), no se genera un cargo
            // nuevo aquí (a diferencia de AvisoAdeudoService.generar(), que sí
            // crea el cargo del año en curso porque ese es el flujo formal).
            Double mantenimientoPendiente = calculado != null && calculado.getMantenimientoPendiente() != null
                    ? calculado.getMantenimientoPendiente() : 0.0;
            String mantenimientoPorAnioTexto = "";
            if (calculado != null && calculado.getMantenimientoPorAnio() != null && !calculado.getMantenimientoPorAnio().isEmpty()) {
                mantenimientoPorAnioTexto = new TreeMap<>(calculado.getMantenimientoPorAnio()).entrySet().stream()
                        .map(e -> FORMATO_MONEDA.format(e.getValue()) + " - " + e.getKey())
                        .collect(Collectors.joining(" | "));
            }

            Integer folio = siguienteFolio++;
            WaterHouseEntity casa = usuario.getWaterHouse();
            String casaNoTexto = buildCasaNoTexto(casa);
            String domicilioToma = buildDomicilio(usuario);
            String nombreConNumero = usuario.getNoUsuario() + " - " + buildNombreCompleto(usuario.getPerson());
            String fechaUltimoPagoTexto = fechaUltimoPago != null ? fechaUltimoPago.format(FORMATO_FECHA) : "";

            avisos.add(new CartaInformativoAdeudoDatos(
                    folio,
                    nombreConNumero,
                    casaNoTexto,
                    domicilioToma,
                    periodosAdeudados,
                    multaAcumulada,
                    mantenimientoPendiente,
                    mantenimientoPorAnioTexto,
                    noFolioUltimoPago,
                    fechaUltimoPagoTexto,
                    request.getFechaPresentacion(),
                    item.getObservacion()
            ));

            paraGuardar.add(AvisoInformativoAdeudoEntity.builder()
                    .folioNotificacion(folio)
                    .nombreUsuarioTitular(buildNombreCompleto(usuario.getPerson()))
                    .noCasa(casa != null ? casa.getCasaNo() : null)
                    .noCasaTexto(casaNoTexto)
                    .domicilioToma(domicilioToma)
                    .periodosAdeudados(periodosAdeudados)
                    .adeudoTotal(adeudoTotal)
                    .multaAcumulada(multaAcumulada)
                    .noFolioUltimoPago(noFolioUltimoPago)
                    .fechaUltimoPago(fechaUltimoPago)
                    .fechaPresentacion(request.getFechaPresentacion())
                    .observacion(item.getObservacion())
                    .estatus(1)
                    .userIdAdd(userIdAdd)
                    .dateAdd(ahora)
                    .waterUser(usuario)
                    .build());
        }

        if (avisos.isEmpty()) {
            return new GenerarAvisosInformativosResultado(new byte[0], 0);
        }

        byte[] pdf = avisoInformativoAdeudoPdfService.generarLote(avisos);
        avisoInformativoAdeudoRepository.saveAll(paraGuardar);

        return new GenerarAvisosInformativosResultado(pdf, avisos.size());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> historial() {
        AvisoInformativoAdeudoRestResponse response = new AvisoInformativoAdeudoRestResponse();
        try {
            List<AvisoInformativoAdeudoEntity> avisos = avisoInformativoAdeudoRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoInformativoAdeudoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial", e);
        }
    }

    // Historial completo (activos + cancelados) de un usuario específico --
    // para el acordeón "Cartas generadas" en su ficha (details-user).
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> porUsuario(Integer aguaUsuarioId) {
        AvisoInformativoAdeudoRestResponse response = new AvisoInformativoAdeudoRestResponse();
        try {
            List<AvisoInformativoAdeudoEntity> avisos = avisoInformativoAdeudoRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(aguaUsuarioId, List.of(1, 0));
            response.setData(avisos.stream().map(avisoInformativoAdeudoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Avisos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar los avisos del usuario", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> marcarEntregada(Integer avisoInformativoAdeudoId, AvisoInformativoAdeudoEntregaRequestDto request) {
        AvisoInformativoAdeudoRestResponse response = new AvisoInformativoAdeudoRestResponse();
        try {
            Optional<AvisoInformativoAdeudoEntity> avisoOpt = avisoInformativoAdeudoRepository.findById(avisoInformativoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoInformativoAdeudoEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            aviso.setComentarioEntrega(request.getComentarioEntrega());
            avisoInformativoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoInformativoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> cancelar(Integer avisoInformativoAdeudoId) {
        AvisoInformativoAdeudoRestResponse response = new AvisoInformativoAdeudoRestResponse();
        try {
            Optional<AvisoInformativoAdeudoEntity> avisoOpt = avisoInformativoAdeudoRepository.findById(avisoInformativoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoInformativoAdeudoEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoInformativoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoInformativoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    @Transactional
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> reactivar(Integer avisoInformativoAdeudoId) {
        AvisoInformativoAdeudoRestResponse response = new AvisoInformativoAdeudoRestResponse();
        try {
            Optional<AvisoInformativoAdeudoEntity> avisoOpt = avisoInformativoAdeudoRepository.findById(avisoInformativoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoInformativoAdeudoEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoInformativoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoInformativoAdeudoMapper.entityToDto(aviso)));
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
