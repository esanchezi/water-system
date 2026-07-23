package com.mx.uvas.watersystem.helpers;

import com.mx.uvas.watersystem.dto.WaterEgresoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoEmitirDto;
import com.mx.uvas.watersystem.dto.WaterEgresoGastoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoLineaDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Transactional
@Component
@AllArgsConstructor
public class WaterEgresoHelper {

    private final IPersonRepository personRepository;
    private final WaterHelper waterHelper;

    // El monto de la cabecera SIEMPRE se calcula como la suma de las líneas
    // (montoTotal), nunca se toma de lo que mande el cliente en request.getMonto():
    // así no hay forma de que se descuadre.
    public WaterEgresoEntity buildCabecera(WaterEgresoDto request, CatalogOptionsEntity tipoComprobante, Double montoTotal) {
        PersonEntity persona = request.getPersonaId() != null
                ? personRepository.findById(request.getPersonaId())
                        .orElseThrow(() -> new NoSuchElementException("No se encontró la persona con el ID: " + request.getPersonaId()))
                : null;

        return WaterEgresoEntity.builder()
                .valido(true)
                .nivel(1)
                .fechaPago(request.getFechaPago())
                .fechaCompra(request.getFechaCompra())
                .monto(montoTotal)
                .descripcion(request.getDescripcion())
                .proveedor(request.getProveedor())
                .noFolio(request.getNoFolio())
                .justificacion(request.getJustificacion())
                .tipoComprobante(tipoComprobante)
                .persona(persona)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .lineas(new ArrayList<>())
                .build();
    }

    public WaterEgresoEntity buildLinea(WaterEgresoLineaDto lineaRequest, WaterEgresoEntity cabecera) {
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(lineaRequest.getConceptoId());

        return WaterEgresoEntity.builder()
                .valido(true)
                .nivel(2)
                .fechaPago(cabecera.getFechaPago())
                .monto(lineaRequest.getMonto())
                .descripcion(lineaRequest.getDescripcion())
                .concepto(concepto)
                .egresoPadre(cabecera)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    // El monto total del vale es la suma de sus líneas de categoría.
    public Double calcularTotal(Iterable<WaterEgresoLineaDto> lineas) {
        double suma = 0d;
        for (WaterEgresoLineaDto l : lineas) {
            suma += l.getMonto() != null ? l.getMonto() : 0d;
        }
        return suma;
    }

    // Gasto suelto: nivel = 2, sin cabecera todavía (egresoPadre = null).
    // Se captura día a día durante el mes y queda "pendiente" hasta que se
    // incluye en un vale con emitirVale().
    public WaterEgresoEntity buildGasto(WaterEgresoGastoDto request) {
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

        PersonEntity persona = request.getPersonaId() != null
                ? personRepository.findById(request.getPersonaId())
                        .orElseThrow(() -> new NoSuchElementException("No se encontró la persona con el ID: " + request.getPersonaId()))
                : null;

        return WaterEgresoEntity.builder()
                .valido(true)
                .nivel(2)
                .fechaPago(request.getFechaPago())
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .proveedor(request.getProveedor())
                .concepto(concepto)
                .persona(persona)
                .egresoPadre(null)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    // Corrige un gasto que todavía sigue pendiente (el llamador ya validó
    // que nivel = 2, egresoPadre = null y estatus = 1). No se toca
    // egresoPadre/nivel/estatus aquí, solo los datos propios del gasto.
    public void actualizarGasto(WaterEgresoEntity existente, WaterEgresoGastoDto request) {
        CatalogOptionsEntity concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

        PersonEntity persona = request.getPersonaId() != null
                ? personRepository.findById(request.getPersonaId())
                        .orElseThrow(() -> new NoSuchElementException("No se encontró la persona con el ID: " + request.getPersonaId()))
                : null;

        existente.setFechaPago(request.getFechaPago());
        existente.setMonto(request.getMonto());
        existente.setDescripcion(request.getDescripcion());
        existente.setProveedor(request.getProveedor());
        existente.setConcepto(concepto);
        existente.setPersona(persona);
        existente.setUserIdUpdate(1); // TODO: Keycloak
        existente.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    // Cabecera del vale consolidado que se crea al emitir: no trae proveedor
    // ni persona propios (cada gasto ya trae los suyos), su monto es la suma
    // de los gastos que se le re-parentan.
    public WaterEgresoEntity buildCabeceraEmision(WaterEgresoEmitirDto request, CatalogOptionsEntity tipoComprobante, Double montoTotal) {
        return WaterEgresoEntity.builder()
                .valido(true)
                .nivel(1)
                .fechaPago(request.getFechaPago())
                .monto(montoTotal)
                .descripcion(request.getDescripcion())
                .noFolio(request.getNoFolio())
                .justificacion(request.getJustificacion())
                .tipoComprobante(tipoComprobante)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .lineas(new ArrayList<>())
                .build();
    }

    // Suma de los montos de los gastos pendientes que se van a incluir en el
    // vale que se está emitiendo.
    public Double calcularTotalGastos(Iterable<WaterEgresoEntity> gastos) {
        double suma = 0d;
        for (WaterEgresoEntity g : gastos) {
            suma += g.getMonto() != null ? g.getMonto() : 0d;
        }
        return suma;
    }
}
