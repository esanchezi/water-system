package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.exception.NotFoundException;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.FeeEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WaterUserRepositoryService {

    private final IWaterUserRepository waterUserRepository;
    private final IFeeRepository feeRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;

    public List<WaterUserEntity> findAll() {
        return waterUserRepository.findAll();
    }

    public WaterUserEntity findByNoUser(Integer noUser) {
        return waterUserRepository.findByNoUsuario(noUser);
    }

    public List<WaterUserEntity> findByNombre(String nombre) {
        return waterUserRepository.findByNombre(nombre);
    }

    public WaterUserEntity save(WaterUserEntity waterUser) {
        return waterUserRepository.save(waterUser);
    }

    public FeeEntity getFeeOrThrow(Integer cuotaId) {
        return feeRepository.findById(cuotaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la cuota con el ID: " + cuotaId));
    }

    public CatalogOptionsEntity getCatalogOptionOrThrow(Integer optionId) {
        return catalogOptionsRepository.findById(optionId)
                .orElseThrow(() -> new NotFoundException("No se encontró la opción de pago con el ID: " + optionId));
    }

}
