package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.AssemblyAttendanceDto;
import com.mx.uvas.watersystem.dto.AssemblyDto;
import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.helpers.AssemblyHelper;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.mapping.AssemblyMapper;
import com.mx.uvas.watersystem.model.*;
import com.mx.uvas.watersystem.repositories.IAssemblyAttendanceRepository;
import com.mx.uvas.watersystem.repositories.IAssemblyRepository;
import com.mx.uvas.watersystem.response.AssemblyRestResponse;
import com.mx.uvas.watersystem.services.IAssemblyService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class AssemblyService implements IAssemblyService {

    private final IAssemblyRepository assemblyRepository;
    private final IAssemblyAttendanceRepository assemblyAttendanceRepository;
    private final AssemblyMapper assemblyMapper;
    private final AssemblyHelper assemblyHelper;
    private final WaterHelper waterHelper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";

    @Override
    public ResponseEntity<AssemblyRestResponse> findAll() {
        return handleFindAll(assemblyRepository.findAll());
    }

    @Override
    public ResponseEntity<AssemblyRestResponse> findByNoUsuario(Integer noUser) {
        return handleFindAll(assemblyRepository.findByNoUsuario(noUser));
    }

    @Override
    public AssemblyAttendanceDto create(AssemblyAttendanceDto request) {
        WaterUserEntity user = waterHelper.getWaterUser(request.getWaterUser().getNoUsuario());
        AssemblyEntity assembly = assemblyHelper.getAssembly(request.getAsambleaId());

        AssemblyAttendanceEntity attendanceToPersist = assemblyHelper.buildWaterReceiptEntity(request, user,assembly);
        assemblyAttendanceRepository.save(attendanceToPersist);

        return assemblyMapper.attendanceEntityToDto(attendanceToPersist);
    }

    private ResponseEntity<AssemblyRestResponse> handleFindAll(List<AssemblyEntity> assembly) {
        AssemblyRestResponse response = new AssemblyRestResponse();
        try {

            List<AssemblyDto> assemblyDto = assembly.isEmpty()
                    ? Collections.emptyList()
                    : assembly.stream()
                    .sorted(Comparator.comparing(AssemblyEntity::getDate))
                    .map(assemblyMapper::entityToDto)
                    .toList();

            response.setData(assemblyDto);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, USUARIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_USUARIOS_MESSAGE, e);
        }
    }

}