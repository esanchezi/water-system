package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterEgresoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoEmitirDto;
import com.mx.uvas.watersystem.dto.WaterEgresoGastoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoMarcarDto;
import com.mx.uvas.watersystem.helpers.WaterEgresoHelper;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.mapping.WaterEgresoMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import com.mx.uvas.watersystem.repositories.IWaterEgresoRepository;
import com.mx.uvas.watersystem.response.WaterEgresoGastoRestResponse;
import com.mx.uvas.watersystem.response.WaterEgresoRestResponse;
import com.mx.uvas.watersystem.services.IWaterEgresoService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterEgresoService implements IWaterEgresoService {

    private final IWaterEgresoRepository waterEgresoRepository;
    private final WaterEgresoMapper waterEgresoMapper;
    private final WaterEgresoHelper waterEgresoHelper;
    private final WaterHelper waterHelper;

    private static final String EGRESOS_FOUND_MESSAGE = "Egresos encontrados";
    private static final String EGRESO_FOUND_MESSAGE = "Egreso encontrado";
    private static final String EGRESO_NOT_FOUND_MESSAGE = "No se encontró el egreso";
    private static final String ERROR_SEARCHING_EGRESOS_MESSAGE = "Error al consultar egresos";
    private static final String EGRESO_CREATED_MESSAGE = "Egreso registrado correctamente";
    private static final String ERROR_CREATING_EGRESO_MESSAGE = "Error al registrar el egreso";
    private static final String EGRESO_DEACTIVATED_MESSAGE = "Egreso desactivado";
    private static final String EGRESO_MARCADO_MESSAGE = "Vale actualizado";
    private static final String ERROR_MARCANDO_EGRESO_MESSAGE = "Error al actualizar las banderas del vale";
    private static final String LINEAS_REQUIRED_MESSAGE = "Debe capturar al menos una línea de categoría";
    private static final String GASTOS_FOUND_MESSAGE = "Gastos pendientes encontrados";
    private static final String ERROR_SEARCHING_GASTOS_MESSAGE = "Error al consultar gastos pendientes";
    private static final String GASTO_CREATED_MESSAGE = "Gasto registrado correctamente";
    private static final String ERROR_CREATING_GASTO_MESSAGE = "Error al registrar el gasto";
    private static final String GASTO_ACTUALIZADO_MESSAGE = "Gasto actualizado correctamente";
    private static final String ERROR_ACTUALIZANDO_GASTO_MESSAGE = "Error al actualizar el gasto";
    private static final String GASTO_NO_PENDIENTE_MESSAGE = "El gasto no existe o ya no está pendiente (puede que ya se haya incluido en un vale)";
    private static final String GASTOS_REQUIRED_MESSAGE = "Debe seleccionar al menos un gasto pendiente";
    private static final String GASTOS_NOT_FOUND_MESSAGE = "Alguno de los gastos seleccionados ya no está disponible (verifique que sigan pendientes)";
    private static final String VALE_EMITIDO_MESSAGE = "Vale emitido correctamente";
    private static final String ERROR_EMITIENDO_VALE_MESSAGE = "Error al emitir el vale";

    @Override
    public ResponseEntity<WaterEgresoRestResponse> findAll() {
        return handleFindAll(waterEgresoRepository.findCabecerasActivas());
    }

    @Override
    public ResponseEntity<WaterEgresoRestResponse> findById(Integer id) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            return waterEgresoRepository.findCabeceraById(id)
                    .map(entity -> {
                        response.setData(List.of(waterEgresoMapper.entityToDto(entity)));
                        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, EGRESO_FOUND_MESSAGE);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    })
                    .orElseGet(() -> ResponseHandler.handleNotFoundException(response, EGRESO_NOT_FOUND_MESSAGE + " [ID: " + id + "]"));
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_EGRESOS_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoRestResponse> create(WaterEgresoDto request) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            if (request.getLineas() == null || request.getLineas().isEmpty()) {
                return ResponseHandler.handleBadRequest(response, LINEAS_REQUIRED_MESSAGE);
            }

            Double montoTotal = waterEgresoHelper.calcularTotal(request.getLineas());

            CatalogOptionsEntity tipoComprobante = request.getTipoComprobanteId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getTipoComprobanteId())
                    : null;

            WaterEgresoEntity cabecera = waterEgresoHelper.buildCabecera(request, tipoComprobante, montoTotal);

            request.getLineas().forEach(lineaRequest ->
                    cabecera.getLineas().add(waterEgresoHelper.buildLinea(lineaRequest, cabecera)));

            waterEgresoRepository.save(cabecera);

            response.setData(List.of(waterEgresoMapper.entityToDto(cabecera)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, EGRESO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_EGRESO_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoRestResponse> deactivate(Integer id) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            return waterEgresoRepository.findCabeceraById(id)
                    .map(entity -> {
                        entity.setEstatus(0);
                        entity.setUserIdUpdate(1); // TODO: Keycloak
                        entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                        waterEgresoRepository.save(entity);
                        response.setData(Collections.emptyList());
                        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, EGRESO_DEACTIVATED_MESSAGE);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    })
                    .orElseGet(() -> ResponseHandler.handleNotFoundException(response, EGRESO_NOT_FOUND_MESSAGE + " [ID: " + id + "]"));
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar egreso", e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoRestResponse> marcar(Integer id, WaterEgresoMarcarDto request) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            return waterEgresoRepository.findCabeceraById(id)
                    .map(entity -> {
                        if (request.getRevisado() != null) {
                            entity.setRevisado(request.getRevisado());
                        }
                        if (request.getValidadoFisico() != null) {
                            entity.setValidadoFisico(request.getValidadoFisico());
                        }
                        entity.setUserIdUpdate(1); // TODO: Keycloak
                        entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                        waterEgresoRepository.save(entity);
                        response.setData(List.of(waterEgresoMapper.entityToDto(entity)));
                        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, EGRESO_MARCADO_MESSAGE);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    })
                    .orElseGet(() -> ResponseHandler.handleNotFoundException(response, EGRESO_NOT_FOUND_MESSAGE + " [ID: " + id + "]"));
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MARCANDO_EGRESO_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoGastoRestResponse> crearGasto(WaterEgresoGastoDto request) {
        WaterEgresoGastoRestResponse response = new WaterEgresoGastoRestResponse();
        try {
            WaterEgresoEntity gasto = waterEgresoHelper.buildGasto(request);
            waterEgresoRepository.save(gasto);

            response.setData(List.of(waterEgresoMapper.gastoEntityToDto(gasto)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, GASTO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_GASTO_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoGastoRestResponse> actualizarGasto(Integer id, WaterEgresoGastoDto request) {
        WaterEgresoGastoRestResponse response = new WaterEgresoGastoRestResponse();
        try {
            return waterEgresoRepository.findById(id)
                    .filter(g -> g.getNivel() != null && g.getNivel() == 2
                            && g.getEgresoPadre() == null
                            && g.getEstatus() != null && g.getEstatus() == 1)
                    .map(gasto -> {
                        waterEgresoHelper.actualizarGasto(gasto, request);
                        waterEgresoRepository.save(gasto);
                        response.setData(List.of(waterEgresoMapper.gastoEntityToDto(gasto)));
                        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, GASTO_ACTUALIZADO_MESSAGE);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    })
                    .orElseGet(() -> ResponseHandler.handleNotFoundException(response, GASTO_NO_PENDIENTE_MESSAGE + " [ID: " + id + "]"));
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_ACTUALIZANDO_GASTO_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoGastoRestResponse> findPendientes() {
        WaterEgresoGastoRestResponse response = new WaterEgresoGastoRestResponse();
        try {
            List<WaterEgresoEntity> gastos = waterEgresoRepository.findGastosPendientes();
            var dtos = gastos.isEmpty()
                    ? Collections.<WaterEgresoGastoDto>emptyList()
                    : gastos.stream()
                    .map(waterEgresoMapper::gastoEntityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, GASTOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_GASTOS_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterEgresoRestResponse> emitirVale(WaterEgresoEmitirDto request) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            if (request.getGastoIds() == null || request.getGastoIds().isEmpty()) {
                return ResponseHandler.handleBadRequest(response, GASTOS_REQUIRED_MESSAGE);
            }

            List<WaterEgresoEntity> gastos = waterEgresoRepository.findAllById(request.getGastoIds());

            // Verifica que todos existan y que sigan pendientes (nivel = 2,
            // sin cabecera, activos): evita re-emitir un gasto que ya se
            // incluyó en otro vale por una condición de carrera.
            boolean todosValidos = gastos.size() == request.getGastoIds().size()
                    && gastos.stream().allMatch(g ->
                            g.getNivel() != null && g.getNivel() == 2
                                    && g.getEgresoPadre() == null
                                    && g.getEstatus() != null && g.getEstatus() == 1);

            if (!todosValidos) {
                return ResponseHandler.handleBadRequest(response, GASTOS_NOT_FOUND_MESSAGE);
            }

            Double montoTotal = waterEgresoHelper.calcularTotalGastos(gastos);

            CatalogOptionsEntity tipoComprobante = request.getTipoComprobanteId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getTipoComprobanteId())
                    : null;

            WaterEgresoEntity cabecera = waterEgresoHelper.buildCabeceraEmision(request, tipoComprobante, montoTotal);
            cabecera.setLineas(new ArrayList<>(gastos));

            gastos.forEach(g -> g.setEgresoPadre(cabecera));

            waterEgresoRepository.save(cabecera);

            response.setData(List.of(waterEgresoMapper.entityToDto(cabecera)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, VALE_EMITIDO_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_EMITIENDO_VALE_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterEgresoRestResponse> handleFindAll(List<WaterEgresoEntity> egresos) {
        WaterEgresoRestResponse response = new WaterEgresoRestResponse();
        try {
            var dtos = egresos.isEmpty()
                    ? Collections.<WaterEgresoDto>emptyList()
                    : egresos.stream()
                    .map(waterEgresoMapper::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, EGRESOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_EGRESOS_MESSAGE, e);
        }
    }
}
