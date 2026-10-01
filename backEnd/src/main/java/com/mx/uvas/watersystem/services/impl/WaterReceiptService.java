package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.dto.WaterReceiptPaymentDto;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.helpers.WaterUserHelper;
import com.mx.uvas.watersystem.mapping.WaterReceiptMapper;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserAnnualPaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserChargePaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptService;
import com.mx.uvas.watersystem.helpers.WaterReceiptHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.mx.uvas.watersystem.utils.Constants.*;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterReceiptService implements IWaterReceiptService {

    private final IWaterReceiptRepository waterReceiptRepository;
    private final WaterReceiptHelper waterReceiptHelper;
    private final WaterHelper waterHelper;
    private final WaterUserHelper waterUserHelper;
    private final WaterReceiptMapper waterReceiptMapper;
    private final IWaterUserAnnualPaymentRepository waterUserAnnualPaymentRepository;
    private final IWaterUserChargeRepository waterUserChargeRepository;
    private final IWaterUserChargePaymentRepository waterUserChargePaymentRepository;

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findAllByEstatus() {
        return findWaterReceipts(waterReceiptRepository.findAllByEstatus(), RECIBOS);
    }

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findByNoFolioOrNoUsuario(Integer noFolio) {
        return findWaterReceipts(waterReceiptRepository.findByNoFolioOrNoUsuario(noFolio), RECIBOS);
    }

    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findByNoUsuario(Integer noUser) {
        return findWaterReceipts(waterReceiptRepository.findByNoUsuario(noUser), RECIBOS);
    }

    // findByNoFolio del repositorio ya existía (lo usa AvisoAdeudoService al
    // validar folioReciboVinculado) pero no estaba expuesto por su cuenta --
    // aquí sí importa distinguir "no existe" (lista vacía, se resuelve como
    // 404 en findWaterReceipts) de un folio real, por eso se envuelve como
    // lista de 0 o 1 en vez de usar directo la entidad (puede venir null).
    @Override
    @Transactional
    public ResponseEntity<WaterReceiptRestResponse> findByNoFolioExacto(Integer noFolio) {
        WaterReceiptEntity existente = waterReceiptRepository.findByNoFolio(noFolio);
        List<WaterReceiptEntity> lista = existente != null ? List.of(existente) : List.of();
        return findWaterReceipts(lista, RECIBOS);
    }

    @Override
    public WaterReceiptDto create(WaterReceiptDto request) {
        WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

        WaterReceiptEntity waterReceiptToPersist = waterReceiptHelper.buildWaterReceiptEntity(request, user,concepto);
        List<WaterUserChargePaymentEntity> abonosPendientes = createReceiptPayments(request.getWaterReceiptPayment(), waterReceiptToPersist, user);

        WaterReceiptEntity receiptPersist = waterReceiptRepository.save(waterReceiptToPersist);
        //user = waterUserHelper.updateWaterUserEstatus(request.getWaterUser(),user);
        guardarAniosPagados(request.getAniosPagados(), user);
        guardarAbonosACargos(abonosPendientes, receiptPersist);
        log.info("Receipt saved id:{}",receiptPersist.getAguaReciboId());

        return waterReceiptMapper.entityToDto(receiptPersist);
    }

    // v11 (sept. 2026, pedido explícito de Ely -- "en su caso es elegir en
    // el pago el cargo a liquidar"): cada línea del recibo puede traer
    // opcionalmente un cargoALiquidarId (elegido a mano en la pantalla de
    // captura, ver select "Cargo a liquidar" en new-receipt). Se guarda el
    // abono YA con el recibo persistido (necesita su ID para la FK
    // recibo_id) -- por eso se arma la lista de abonos pendientes durante
    // createReceiptPayments() y se guarda aparte, después del save() del
    // recibo. Reemplaza el intento anterior de adivinar automáticamente por
    // concepto+año (terminamos usando el ID de catálogo equivocado -- 108
    // en vez de 79 -- precisamente el tipo de error que este selector
    // evita).
    private void guardarAbonosACargos(List<WaterUserChargePaymentEntity> abonosPendientes, WaterReceiptEntity receiptPersist) {
        for (WaterUserChargePaymentEntity abono : abonosPendientes) {
            abono.setWaterReceipt(receiptPersist);
            waterUserChargePaymentRepository.save(abono);
            log.info("Abono de {} aplicado al cargo {} desde el recibo {}",
                    abono.getMontoAplicado(), abono.getCargo().getAguaUsuarioCargoId(), receiptPersist.getNoFolio());
        }
    }

    // Si la línea trae cargoALiquidarId, arma (sin guardar todavía) el abono
    // correspondiente -- validando que el cargo exista, sea de este mismo
    // usuario, y todavía tenga saldo. Si algo no cuadra, se ignora
    // silenciosamente (con log) y el recibo se captura igual: nunca debe
    // bloquear el guardado del pago por un cargo mal elegido.
    private WaterUserChargePaymentEntity construirAbonoSiAplica(WaterReceiptPaymentDto paymentDto, WaterUserEntity user) {
        Integer cargoId = paymentDto.getCargoALiquidarId();
        if (cargoId == null) {
            return null;
        }
        Optional<WaterUserChargeEntity> cargoOpt = waterUserChargeRepository.findById(cargoId);
        if (cargoOpt.isEmpty()) {
            log.warn("Cargo a liquidar {} no existe -- se ignora, el recibo se captura igual", cargoId);
            return null;
        }
        WaterUserChargeEntity cargo = cargoOpt.get();
        if (cargo.getWaterUser() == null || !cargo.getWaterUser().getAguaUsuarioId().equals(user.getAguaUsuarioId())) {
            log.warn("Cargo a liquidar {} no pertenece al usuario {} -- se ignora", cargoId, user.getNoUsuario());
            return null;
        }
        double saldo = cargo.getSaldo();
        if (saldo <= 0) {
            log.warn("Cargo a liquidar {} ya no tiene saldo pendiente -- se ignora", cargoId);
            return null;
        }
        double montoAplicado = Math.min(paymentDto.getMontoAplicado() != null ? paymentDto.getMontoAplicado() : 0d, saldo);
        if (montoAplicado <= 0) {
            return null;
        }
        return WaterUserChargePaymentEntity.builder()
                .cargo(cargo)
                .montoAplicado(montoAplicado)
                .fechaPago(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    // Edita un recibo ya existente -- incluye todo lo que trae el formulario
    // de captura (usuario, folio, fecha, concepto, comité, tipo de pago,
    // montos aplicados por año). Los pagos se reemplazan por completo: como
    // la relación tiene orphanRemoval=true, basta con vaciar la colección y
    // volver a construirla -- Hibernate borra los que ya no estén y crea los
    // nuevos al hacer flush.
    @Override
    public ResponseEntity<WaterReceiptRestResponse> update(Integer id, WaterReceiptDto request) {
        WaterReceiptRestResponse response = new WaterReceiptRestResponse();
        try {
            WaterReceiptEntity existente = waterReceiptRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("No se encontró el recibo con el ID: " + id));

            WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
            CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

            waterReceiptHelper.actualizarWaterReceiptEntity(existente, request, user, concepto);

            if (existente.getWaterReceiptPayment() != null) {
                existente.getWaterReceiptPayment().clear();
            } else {
                existente.setWaterReceiptPayment(new HashSet<>());
            }
            // Nota: al EDITAR un recibo no se aplican abonos de "cargo a
            // liquidar" -- si se reutilizara aquí, cada vez que se vuelva a
            // guardar la edición se duplicaría el abono. Esa reconciliación
            // solo corre al capturar un recibo nuevo (create()).
            createReceiptPayments(request.getWaterReceiptPayment(), existente, user);

            WaterReceiptEntity receiptPersist = waterReceiptRepository.save(existente);
            guardarAniosPagados(request.getAniosPagados(), user);
            log.info("Receipt updated id:{}", receiptPersist.getAguaReciboId());

            response.setData(List.of(waterReceiptMapper.entityToDto(receiptPersist)));
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, "Recibo actualizado correctamente");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, "Error al actualizar el recibo");
            log.error("Error al actualizar recibo: {}", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public WaterReceiptDto createCancelled(WaterReceiptDto request) {
        WaterReceiptEntity waterReceiptToPersist = waterReceiptHelper.buildWaterReceiptCancelledEntity(request);
        WaterReceiptEntity receiptPersist = waterReceiptRepository.save(waterReceiptToPersist);

        log.info("Receipt saved id:{}",receiptPersist.getAguaReciboId());

        return waterReceiptMapper.entityToDto(receiptPersist);
    }

    // Devuelve los abonos a cargos pendientes de guardar (todavía sin
    // recibo asignado -- ver guardarAbonosACargos()), armados a partir de
    // cada línea que haya traído cargoALiquidarId.
    private List<WaterUserChargePaymentEntity> createReceiptPayments(List<WaterReceiptPaymentDto> paymentDtos, WaterReceiptEntity waterReceiptToPersist, WaterUserEntity user) {
        List<WaterUserChargePaymentEntity> abonosPendientes = new ArrayList<>();
        for (WaterReceiptPaymentDto paymentDto : paymentDtos) {
            CatalogOptionsEntity comite = waterHelper.getCatalogOptionOrThrow(paymentDto.getComiteId());
            CatalogOptionsEntity tipoPago = waterHelper.getCatalogOptionOrThrow(paymentDto.getTipoPagoId());
            CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(paymentDto.getConceptoId());
            WaterReceiptPaymentEntity payment = waterReceiptHelper.buildWaterReceiptPaymentEntity(paymentDto, waterReceiptToPersist, comite, tipoPago,concepto);
            waterReceiptToPersist.addPayment(payment);
            waterReceiptToPersist.updatePayments();

            WaterUserChargePaymentEntity abono = construirAbonoSiAplica(paymentDto, user);
            if (abono != null) {
                abonosPendientes.add(abono);
            }
        }
        return abonosPendientes;
    }

    private ResponseEntity<WaterReceiptRestResponse> findWaterReceipts(List<WaterReceiptEntity> waterReceipts, String receiptType) {
        WaterReceiptRestResponse response = new WaterReceiptRestResponse();
        try {
            if (waterReceipts.isEmpty()) {
                throw new NoSuchElementException(receiptType + " - " + NO_ENCONTRADOS);
            }
            List<WaterReceiptDto> waterReceiptDtos = waterReceipts.stream()
                    .map(waterReceiptMapper::entityToDto)
                    .toList();

            response.setData(waterReceiptDtos);
            response.addMetadata(OK_RESPONSE_MESSAGE, CODIGO_OO, receiptType + " - " + ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, receiptType + " - " + NO_ENCONTRADOS);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.addMetadata(ERROR_RESPONSE_MESSAGE, CODIGO_MENOS_O1, ERROR_AL_CONSULTAR + " - " + RECIBOS);
            log.error("Error al consultar: {}", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void guardarAniosPagados(List<Integer> anios, WaterUserEntity user) {
        if (anios == null || anios.isEmpty()) return;
        for (Integer anio : anios) {
            boolean existe = waterUserAnnualPaymentRepository.existsByWaterUser_AguaUsuarioIdAndAnio(user.getAguaUsuarioId(), anio);

            if (!existe) {
                WaterUserAnnualPaymentEntity entity =
                        WaterUserAnnualPaymentEntity.builder()
                                .waterUser(user)
                                .anio(anio)
                                .fechaValidacion(LocalDate.now())
                                .estatus(1)
                                .userIdAdd(1)
                                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                                .build();
                waterUserAnnualPaymentRepository.save(entity);
            }
        }
    }
}
