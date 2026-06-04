package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.dto.AdressDto;
import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.model.AdressEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.repositories.IAdressRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Transactional
@Component
@AllArgsConstructor
public class AdressHelper {

    private final IAdressRepository adressRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;

    public AdressEntity saveOrUpdateAdress(AdressDto dto) {
        CatalogOptionsEntity seccion = catalogOptionsRepository.findById(dto.getSeccionId())
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada con ID: " + dto.getSeccionId()));

        if (dto.getDireccionId() != null && adressRepository.existsById(dto.getDireccionId())) {
            AdressEntity existing = adressRepository.findById(dto.getDireccionId())
                    .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada con ID: " + dto.getDireccionId()));

            fillAddressData(existing, dto, seccion);
            existing.setUserIdUpdate(1);
            existing.setDateUpdate(currentTimestamp());

            return adressRepository.save(existing);
        } else {
            AdressEntity newAdress = buildNewAddress(dto, seccion);
            return adressRepository.save(newAdress);
        }
    }

    private AdressEntity buildNewAddress(AdressDto dto, CatalogOptionsEntity seccion) {
        AdressEntity entity = new AdressEntity();
        fillAddressData(entity, dto, seccion);
        entity.setEstatus(1);
        entity.setUserIdAdd(1);
        entity.setDateAdd(currentTimestamp());
        return entity;
    }

    private void fillAddressData(AdressEntity entity, AdressDto dto, CatalogOptionsEntity seccion) {
        if (seccion != null) {
            entity.setCatSeccion(seccion);
        }

        if (dto.getCalle() != null && !dto.getCalle().trim().isEmpty()) {
            entity.setCalle(dto.getCalle());
        }

        if (dto.getNumero() != null && !dto.getNumero().trim().isEmpty()) {
            entity.setNumero(dto.getNumero());
        }

        if (dto.getReferencia() != null && !dto.getReferencia().trim().isEmpty()) {
            entity.setReferencia(dto.getReferencia());
        }

        if (dto.getEntrecalle1() != null && !dto.getEntrecalle1().trim().isEmpty()) {
            entity.setEntrecalle1(dto.getEntrecalle1());
        }

        if (dto.getEntrecalle2() != null && !dto.getEntrecalle2().trim().isEmpty()) {
            entity.setEntrecalle2(dto.getEntrecalle2());
        }

        if (dto.getNoCasa() != null) {
            entity.setNoCasa(dto.getNoCasa());
        }

        if (dto.getCoordenadas() != null && !dto.getCoordenadas().trim().isEmpty()) {
            entity.setCoordenadas(dto.getCoordenadas());
        }
    }

    private LocalDateTime currentTimestamp() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}