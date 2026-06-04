package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.FamilyMemberDto;
import com.mx.uvas.watersystem.mapping.FamilyMemberMapper;
import com.mx.uvas.watersystem.model.FamilyMemberEntity;
import com.mx.uvas.watersystem.repositories.IFamilyMemberRepository;
import com.mx.uvas.watersystem.response.FamilyMemberRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IFamilyMemberService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class FamilyMemberService implements IFamilyMemberService {

    private final IFamilyMemberRepository familyMemberRepository;
    private final FamilyMemberMapper familyMemberMapper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<FamilyMemberRestResponse> search() {
        return handleFindAll(familyMemberRepository.findAll());
    }

    @Override
    public ResponseEntity<FamilyMemberRestResponse> findByIdPerson(Integer personId) {
        return handleFindAll(familyMemberRepository.findByIdPerson(personId));
    }

    private ResponseEntity<FamilyMemberRestResponse> handleFindAll(List<FamilyMemberEntity> memberList) {
        FamilyMemberRestResponse response = new FamilyMemberRestResponse();
        try {

            List<FamilyMemberDto> peopleListDtos = memberList.isEmpty()
                    ? Collections.emptyList()
                    : memberList.stream()
                    //.sorted(Comparator.comparing(ProcessEntity::getApp))
                    .map(familyMemberMapper::entityToDto)
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
