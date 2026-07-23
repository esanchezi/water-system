package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterEgresoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoEmitirDto;
import com.mx.uvas.watersystem.dto.WaterEgresoFusionarDto;
import com.mx.uvas.watersystem.dto.WaterEgresoGastoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoMarcarDto;
import com.mx.uvas.watersystem.response.WaterEgresoGastoRestResponse;
import com.mx.uvas.watersystem.response.WaterEgresoRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterEgresoService {

    ResponseEntity<WaterEgresoRestResponse> findAll();

    ResponseEntity<WaterEgresoRestResponse> findById(Integer id);

    ResponseEntity<WaterEgresoRestResponse> create(WaterEgresoDto request);

    ResponseEntity<WaterEgresoRestResponse> deactivate(Integer id);

    // Actualiza (parcial) las banderas de control revisado/validadoFisico de un vale.
    ResponseEntity<WaterEgresoRestResponse> marcar(Integer id, WaterEgresoMarcarDto request);

    // Captura de un gasto suelto (todavía sin vale).
    ResponseEntity<WaterEgresoGastoRestResponse> crearGasto(WaterEgresoGastoDto request);

    // Corrige un gasto que sigue pendiente (no se ha incluido en ningún vale).
    ResponseEntity<WaterEgresoGastoRestResponse> actualizarGasto(Integer id, WaterEgresoGastoDto request);

    // Gastos sueltos capturados en el mes, aún no incluidos en ningún vale.
    ResponseEntity<WaterEgresoGastoRestResponse> findPendientes();

    // Junta los gastos pendientes seleccionados en un solo vale (cabecera).
    ResponseEntity<WaterEgresoRestResponse> emitirVale(WaterEgresoEmitirDto request);

    // Fusiona varios vales YA EMITIDOS (nivel = 1) en uno nuevo más grande;
    // cada vale seleccionado pasa a ser línea (nivel = 2) del nuevo.
    ResponseEntity<WaterEgresoRestResponse> fusionarVales(WaterEgresoFusionarDto request);
}
