package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.DeudorRestResponse;
import org.springframework.http.ResponseEntity;

public interface IDeudorService {

    // Calcula, para el año indicado, quién debe: cuota anual no marcada como
    // pagada (agua_usuario_pago_anual) + saldo de cargos/multas activos.
    ResponseEntity<DeudorRestResponse> getDeudores(Integer anio);
}
