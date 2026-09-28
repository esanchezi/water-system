package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TramoDto implements Serializable {
    private Integer tramoId;
    private String nombre;
    private String descripcion;
    // IZQUIERDO / DERECHO, opcional -- solo para calles con 2 redes.
    private String lado;
    // JSON de puntos [{"lat":..,"lng":..}, ...], tal cual lo interpreta el front.
    private String trazoJson;
    private String metodoTrazo;
    private Integer estatus;

    // Un tramo puede tener más de un horario (pozo + día + turno).
    private List<TramoHorarioDto> horarios;

    // Qué válvulas hay que abrir/cerrar para que este tramo reciba agua.
    private List<TramoValvulaDto> valvulas;

    // Segmentos de tubería/manguera que forman este tramo, en orden desde
    // el origen -- un mismo tramo puede combinar, ej., tubería de PVC al
    // inicio y manguera después. Opcional: no es obligatorio capturarlo.
    private List<SegmentoTuberiaDto> segmentos;
}
