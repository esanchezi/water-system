package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.dto.AdressDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.model.AdressEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAdressRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Transactional
@Component
@AllArgsConstructor
public class WaterUserHelper {

    private final IAdressRepository adressRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterUserRepository waterUserRepository;

    private final WaterHelper waterHelper;

    public AdressEntity createAdress(AdressDto adressDto){
        Optional<CatalogOptionsEntity> seccion = catalogOptionsRepository.findById(adressDto.getSeccionId());
        var adressToPersist = AdressEntity.builder()
                .catSeccion(seccion.get())
                .calle(adressDto.getCalle())
                .numero(adressDto.getNumero())
                .referencia(adressDto.getReferencia())
                .entrecalle1(adressDto.getEntrecalle1())
                .estatus(1)
                .userIdAdd(1)
                .dateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();

        return this.adressRepository.save(adressToPersist);
    }

    public WaterUserEntity updateWaterUserEstatus(WaterUserDto waterUser,WaterUserEntity user) {
        CatalogOptionsEntity estatusComite = waterHelper.getCatalogOptionOrThrow(waterUser.getEstatusComiteId());
        CatalogOptionsEntity estatusPago = waterHelper.getCatalogOptionOrThrow(waterUser.getEstatusPagoId());
        user.setEstatusPago(estatusPago);
        user.setEstatusComite(estatusComite);
        user.setUserIdUpdate(1);
        user.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return this.waterUserRepository.save(user);
    }
}
