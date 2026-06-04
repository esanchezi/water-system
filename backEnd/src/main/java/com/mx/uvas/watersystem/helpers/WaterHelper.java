package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Component
@AllArgsConstructor
public class WaterHelper {

    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterUserRepository waterUserRepository;

    public CatalogOptionsEntity getCatalogOptionOrThrow(Integer optionId) {
        return catalogOptionsRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la opcion de pago con el ID: " + optionId));
    }

    public WaterUserEntity getWaterUser(Integer noUsuario) {
        return Optional.ofNullable(waterUserRepository.findByNoUsuario(noUsuario))
                .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario con el número de usuario: " + noUsuario));
    }
}
