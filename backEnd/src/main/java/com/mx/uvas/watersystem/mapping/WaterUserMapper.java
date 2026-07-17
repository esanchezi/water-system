package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterUserMapper {

    public WaterUserDto entityToDto(WaterUserEntity entity) {
        WaterUserDto response = new WaterUserDto();
        BeanUtils.copyProperties(entity, response);

        // Catálogo: Estatus de Pago
        if (entity.getEstatusPago() != null) {
            CatalogOptionsDto estatusPago = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getEstatusPago(), estatusPago);
            response.setEstatusPago(estatusPago);
        }

        // Catálogo: Estatus de toma
        if (entity.getEstatusToma() != null) {
            CatalogOptionsDto estatusToma = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getEstatusToma(), estatusToma);
            response.setEstatusToma(estatusToma);
        }

        // Catálogo: Estatus del Comité
        if (entity.getEstatusComite() != null) {
            CatalogOptionsDto estatusComite = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getEstatusComite(), estatusComite);
            response.setEstatusComite(estatusComite);
        }

        // Catálogo: Frecuencia de Pago
        if (entity.getFrecuenciaPago() != null) {
            CatalogOptionsDto frecuenciaPago = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getFrecuenciaPago(), frecuenciaPago);
            response.setFrecuenciaPago(frecuenciaPago);
        }

        // Catálogo: Cuota (categoría) — el monto vigente se resuelve por año, no aquí
        if (entity.getFee() != null) {
            FeeDto fee = new FeeDto();
            BeanUtils.copyProperties(entity.getFee(), fee);
            response.setFee(fee);
        }

        // Persona
        if (entity.getPerson() != null) {
            PersonDto person = new PersonDto();
            BeanUtils.copyProperties(entity.getPerson(), person);
            response.setPerson(person);
        }

        // Dirección
        if (entity.getAddress() != null) {
            AdressDto adress = new AdressDto();
            BeanUtils.copyProperties(entity.getAddress(), adress);

            // Catálogo: Sección de la dirección
            if (entity.getAddress().getCatSeccion() != null) {
                CatalogOptionsDto seccion = new CatalogOptionsDto();
                BeanUtils.copyProperties(entity.getAddress().getCatSeccion(), seccion);
                adress.setSeccion(seccion);
            }
            response.setAdress(adress);
        }
        return response;
    }
}
