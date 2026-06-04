package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.ProcessDto;
import com.mx.uvas.watersystem.mapping.PersonMapper;
import com.mx.uvas.watersystem.mapping.ProcessMapper;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.ProcessEntity;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import com.mx.uvas.watersystem.repositories.IProcessRepository;
import com.mx.uvas.watersystem.response.FamilyMemberRestResponse;
import com.mx.uvas.watersystem.response.PersonRestResponse;
import com.mx.uvas.watersystem.response.ProcessRestResponse;
import com.mx.uvas.watersystem.services.IPersonService;
import com.mx.uvas.watersystem.services.IProcessService;
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
public class ProcessService implements IProcessService {

    private final IProcessRepository processRepository;
    private final ProcessMapper processMapper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ProcessRestResponse> search() {
        return handleFindAll(processRepository.findAll());
    }

    @Override
    public ResponseEntity<ProcessRestResponse> findByIdPerson(Integer personId) {
        return handleFindAll(processRepository.findByIdPerson(personId));
    }

    private ResponseEntity<ProcessRestResponse> handleFindAll(List<ProcessEntity> processList) {
        ProcessRestResponse response = new ProcessRestResponse();
        try {

            List<ProcessDto> peopleListDtos = processList.isEmpty()
                    ? Collections.emptyList()
                    : processList.stream()
                    //.sorted(Comparator.comparing(ProcessEntity::getApp))
                    .map(processMapper::entityToDto)
                    .toList();

            response.setData(peopleListDtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, USUARIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_USUARIOS_MESSAGE, e);
        }
    }
}
