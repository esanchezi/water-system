package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.AssemblyAttendanceDto;
import com.mx.uvas.watersystem.dto.AssemblyDto;
import com.mx.uvas.watersystem.helpers.AssemblyHelper;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.mapping.AssemblyMapper;
import com.mx.uvas.watersystem.model.AssemblyAttendanceEntity;
import com.mx.uvas.watersystem.model.AssemblyEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAssemblyAttendanceRepository;
import com.mx.uvas.watersystem.repositories.IAssemblyRepository;
import com.mx.uvas.watersystem.response.AssemblyRestResponse;
import com.mx.uvas.watersystem.services.IAssemblyAttendanceService;
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
public class AssemblyAttendanceService implements IAssemblyAttendanceService {

    private final IAssemblyAttendanceRepository assemblyAttendanceRepository;
    private final AssemblyMapper assemblyMapper;
    private final AssemblyHelper assemblyHelper;
    private final WaterHelper waterHelper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";


    @Override
    public ResponseEntity<AssemblyRestResponse> findByIdAssembly(Integer idAssembly) {
        return handleFindAll(assemblyAttendanceRepository.findByIdAssembly(idAssembly));
    }

    private ResponseEntity<AssemblyRestResponse> handleFindAll(List<AssemblyAttendanceEntity> assembly) {
        AssemblyRestResponse response = new AssemblyRestResponse();
        try {

            List<AssemblyAttendanceDto> assemblyDto = assembly.isEmpty()
                    ? Collections.emptyList()
                    : assembly.stream()
                    //.sorted(Comparator.comparing(AssemblyAttendanceEntity::getDate))
                    .map(assemblyMapper::attendanceEntityToDto)
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