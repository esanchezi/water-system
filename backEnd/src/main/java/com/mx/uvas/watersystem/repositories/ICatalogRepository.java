package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.CatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICatalogRepository extends JpaRepository<CatalogEntity, Integer> {

    List<CatalogEntity> findByEstatus(Integer estatus);

    Optional<CatalogEntity> findByClave(String clave);
}
