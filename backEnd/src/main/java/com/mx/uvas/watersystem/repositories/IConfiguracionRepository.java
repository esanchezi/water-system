package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ConfiguracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IConfiguracionRepository extends JpaRepository<ConfiguracionEntity, Integer> {

    Optional<ConfiguracionEntity> findByClave(String clave);
}
