package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterEgresoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoEmitirDto;
import com.mx.uvas.watersystem.dto.WaterEgresoFusionarDto;
import com.mx.uvas.watersystem.dto.WaterEgresoGastoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoMarcarDto;
import com.mx.uvas.watersystem.response.WaterEgresoGastoRestResponse;
import com.mx.uvas.watersystem.response.WaterEgresoRestResponse;
import com.mx.uvas.watersystem.services.IWaterEgresoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterEgreso")
@AllArgsConstructor
public class WaterEgresoController {

    private final IWaterEgresoService waterEgresoService;

    @GetMapping("/")
    public ResponseEntity<WaterEgresoRestResponse> getAll() {
        return waterEgresoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaterEgresoRestResponse> getById(@PathVariable Integer id) {
        return waterEgresoService.findById(id);
    }

    @PostMapping("/")
    public ResponseEntity<WaterEgresoRestResponse> create(@RequestBody WaterEgresoDto request) {
        return waterEgresoService.create(request);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WaterEgresoRestResponse> deactivate(@PathVariable Integer id) {
        return waterEgresoService.deactivate(id);
    }

    // Actualiza (parcial) revisado/validadoFisico de un vale, para la limpieza histórica.
    @PutMapping("/{id}/marcar")
    public ResponseEntity<WaterEgresoRestResponse> marcar(@PathVariable Integer id, @RequestBody WaterEgresoMarcarDto request) {
        return waterEgresoService.marcar(id, request);
    }

    // Captura de un gasto suelto durante el mes (todavía sin vale).
    @PostMapping("/gasto")
    public ResponseEntity<WaterEgresoGastoRestResponse> crearGasto(@RequestBody WaterEgresoGastoDto request) {
        return waterEgresoService.crearGasto(request);
    }

    // Corrige un gasto que sigue pendiente (aún no incluido en un vale).
    @PutMapping("/gasto/{id}")
    public ResponseEntity<WaterEgresoGastoRestResponse> actualizarGasto(@PathVariable Integer id, @RequestBody WaterEgresoGastoDto request) {
        return waterEgresoService.actualizarGasto(id, request);
    }

    // Gastos sueltos capturados en el mes, pendientes de incluirse en un vale.
    @GetMapping("/pendientes")
    public ResponseEntity<WaterEgresoGastoRestResponse> getPendientes() {
        return waterEgresoService.findPendientes();
    }

    // Junta los gastos pendientes seleccionados en un solo vale de fin de mes.
    @PostMapping("/emitir")
    public ResponseEntity<WaterEgresoRestResponse> emitirVale(@RequestBody WaterEgresoEmitirDto request) {
        return waterEgresoService.emitirVale(request);
    }

    // Fusiona varios vales ya emitidos en uno nuevo más grande.
    @PostMapping("/fusionar")
    public ResponseEntity<WaterEgresoRestResponse> fusionarVales(@RequestBody WaterEgresoFusionarDto request) {
        return waterEgresoService.fusionarVales(request);
    }

}
