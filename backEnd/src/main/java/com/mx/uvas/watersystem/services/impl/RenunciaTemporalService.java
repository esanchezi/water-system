package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AdeudoLuzUsuarioDto;
import com.mx.uvas.watersystem.dto.RenunciaTemporalCrearRequestDto;
import com.mx.uvas.watersystem.dto.RenunciaTemporalDto;
import com.mx.uvas.watersystem.dto.RenunciaTemporalReconexionRequestDto;
import com.mx.uvas.watersystem.mapping.RenunciaTemporalMapper;
import com.mx.uvas.watersystem.model.RenunciaTemporalEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IRenunciaTemporalRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.RenunciaTemporalRestResponse;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Orquesta el flujo de "Renuncia temporal al servicio" (Art. 6 Bis del
// reglamento): generar la solicitud (calcula el adeudo a la fecha, asigna
// folio, guarda historial, genera PDF), consultar si un usuario está
// actualmente en renuncia (para excluirlo de candidatos a carta de adeudo,
// ver AdeudoLuzService), y registrar la reconexión cuando el usuario la
// solicita.
@Service
@Slf4j
@AllArgsConstructor
public class RenunciaTemporalService {

    private final AdeudoLuzService adeudoLuzService;
    private final RenunciaTemporalPdfService renunciaTemporalPdfService;
    private final IRenunciaTemporalRepository renunciaTemporalRepository;
    private final IWaterUserRepository waterUserRepository;
    private final RenunciaTemporalMapper renunciaTemporalMapper;
    private final CurrentUserService currentUserService;

    private static final DateTimeFormatter FORMATO_FECHA_LARGA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public record RenunciaGenerada(byte[] pdfBytes, RenunciaTemporalDto dto) {
    }

    @Transactional
    public RenunciaGenerada generar(RenunciaTemporalCrearRequestDto request) throws IOException, DocumentException {
        WaterUserEntity usuario = waterUserRepository.findById(request.getAguaUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario indicado"));

        // Adeudo a la fecha -- reutiliza el mismo cálculo de la carta de
        // adeudo (Luz + interés moratorio automático), como constancia por
        // escrito de cuánto debía al momento de renunciar. No exime nada.
        List<AdeudoLuzUsuarioDto> adeudos = adeudoLuzService.calcularParaUsuarios(List.of(request.getAguaUsuarioId()));
        Double adeudoALaFecha = adeudos.isEmpty() ? 0d : adeudos.get(0).getAdeudoTotal();

        LocalDate fechaRenuncia = request.getFechaRenuncia() != null ? request.getFechaRenuncia() : LocalDate.now();
        Integer folio = Optional.ofNullable(renunciaTemporalRepository.findMaxFolio()).orElse(0) + 1;

        RenunciaTemporalEntity entity = RenunciaTemporalEntity.builder()
                .folio(folio)
                .fechaRenuncia(fechaRenuncia)
                .motivo(request.getMotivo())
                .adeudoALaFecha(adeudoALaFecha)
                .estatus(1)
                .userIdAdd(currentUserService.getCurrentUserId())
                .dateAdd(LocalDateTime.now())
                .waterUser(usuario)
                .build();
        RenunciaTemporalEntity saved = renunciaTemporalRepository.save(entity);

        WaterHouseEntity casa = usuario.getWaterHouse();
        RenunciaTemporalPdfService.RenunciaTemporalDatos datosPdf = new RenunciaTemporalPdfService.RenunciaTemporalDatos(
                folio,
                usuario.getNoUsuario() + " - " + nombreCompleto(usuario),
                casa != null && casa.getCasaNo() != null ? String.valueOf(casa.getCasaNo()) : "",
                domicilio(usuario),
                fechaRenuncia.format(FORMATO_FECHA_LARGA),
                request.getMotivo(),
                adeudoALaFecha,
                "", "", "", ""
        );
        byte[] pdf = renunciaTemporalPdfService.generar(datosPdf);

        return new RenunciaGenerada(pdf, renunciaTemporalMapper.entityToDto(saved));
    }

    // Trae activas Y canceladas -- mismo patrón que el historial de avisos
    // de adeudo (se oculta lo cancelado por default en el frontend).
    @Transactional(readOnly = true)
    public ResponseEntity<RenunciaTemporalRestResponse> historialPorUsuario(Integer aguaUsuarioId) {
        RenunciaTemporalRestResponse response = new RenunciaTemporalRestResponse();
        try {
            List<RenunciaTemporalEntity> renuncias = renunciaTemporalRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatusOrderByDateAddDesc(aguaUsuarioId, 1);
            response.setData(renuncias.stream().map(renunciaTemporalMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Renuncias encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar renuncias temporales", e);
        }
    }

    @Transactional
    public ResponseEntity<RenunciaTemporalRestResponse> reconectar(Integer renunciaTemporalId, RenunciaTemporalReconexionRequestDto request) {
        RenunciaTemporalRestResponse response = new RenunciaTemporalRestResponse();
        try {
            Optional<RenunciaTemporalEntity> opt = renunciaTemporalRepository.findById(renunciaTemporalId);
            if (opt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "No se encontró la renuncia indicada");
            }
            RenunciaTemporalEntity entity = opt.get();
            entity.setFechaSolicitudReconexion(request.getFechaSolicitudReconexion());
            entity.setFechaAsamblea(request.getFechaAsamblea());
            entity.setCondicionesReconexion(request.getCondicionesReconexion());
            entity.setFechaReconexion(LocalDateTime.now());
            entity.setUserIdReconexion(currentUserService.getCurrentUserId());
            renunciaTemporalRepository.save(entity);

            response.setData(List.of(renunciaTemporalMapper.entityToDto(entity)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Reconexión registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la reconexión", e);
        }
    }

    @Transactional
    public ResponseEntity<RenunciaTemporalRestResponse> cancelar(Integer renunciaTemporalId) {
        RenunciaTemporalRestResponse response = new RenunciaTemporalRestResponse();
        try {
            Optional<RenunciaTemporalEntity> opt = renunciaTemporalRepository.findById(renunciaTemporalId);
            if (opt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "No se encontró la renuncia indicada");
            }
            RenunciaTemporalEntity entity = opt.get();
            entity.setEstatus(0);
            entity.setUserIdCancela(currentUserService.getCurrentUserId());
            entity.setDateCancela(LocalDateTime.now());
            renunciaTemporalRepository.save(entity);

            response.setData(List.of(renunciaTemporalMapper.entityToDto(entity)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Renuncia cancelada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar la renuncia", e);
        }
    }

    // true si el usuario tiene una renuncia ACTIVA sin reconectar todavía
    // -- ver AdeudoLuzService (se excluyen de candidatos a carta de adeudo
    // mientras dure) y la ficha del usuario (para mostrar el estatus).
    @Transactional(readOnly = true)
    public boolean estaEnRenunciaTemporal(Integer aguaUsuarioId) {
        return !renunciaTemporalRepository
                .findByWaterUser_AguaUsuarioIdAndEstatusAndFechaReconexionIsNull(aguaUsuarioId, 1)
                .isEmpty();
    }

    private String nombreCompleto(WaterUserEntity usuario) {
        if (usuario.getPerson() == null) return "";
        var p = usuario.getPerson();
        return String.join(" ",
                        nz(p.getNombre()), nz(p.getNombre2()), nz(p.getApp()), nz(p.getApm()))
                .replaceAll("\\s+", " ").trim();
    }

    private String domicilio(WaterUserEntity usuario) {
        if (usuario.getAddress() != null && usuario.getAddress().getCalle() != null && !usuario.getAddress().getCalle().isBlank()) {
            String numero = usuario.getAddress().getNumero();
            return usuario.getAddress().getCalle() + (numero != null && !numero.isBlank() ? " #" + numero : "");
        }
        WaterHouseEntity casa = usuario.getWaterHouse();
        if (casa != null) {
            String calle = casa.getCatCalle() != null ? casa.getCatCalle().getNombre() : "";
            String casaNo = casa.getCasaNo() != null ? " #" + casa.getCasaNo() : "";
            return (calle + casaNo).trim();
        }
        return "";
    }

    private String nz(String value) {
        return value != null ? value : "";
    }
}
