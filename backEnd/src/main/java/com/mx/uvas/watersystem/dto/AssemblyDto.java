package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AssemblyDto implements Serializable {
    private Integer asambleaId;
    private LocalDate date;
    private String dateS;
    private String purpose;
    private String observaciones;
    private List<AssemblyAttendanceDto> attendance;

}
