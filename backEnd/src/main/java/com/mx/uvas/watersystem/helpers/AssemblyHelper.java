package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.dto.AssemblyAttendanceDto;
import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.dto.WaterReceiptPaymentDto;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Component
@AllArgsConstructor
public class AssemblyHelper {

    private final IAssemblyRepository assemblyRepository;

    public AssemblyEntity getAssembly(Integer idAssembly) {
        return Optional.ofNullable(assemblyRepository.getById(idAssembly))
                .orElseThrow(() -> new NoSuchElementException("No se encontró registro de asamblea: " + idAssembly));
    }

    public AssemblyAttendanceEntity buildWaterReceiptEntity(AssemblyAttendanceDto request, WaterUserEntity user, AssemblyEntity assembly) {
        return AssemblyAttendanceEntity.builder()
                .waterUser(user)
                .assemblyEntity(assembly)
                .asistencia(request.getAsistencia())
                .observaciones(request.getObservaciones())
                .estatus(1)
                .userIdAdd(1)
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
