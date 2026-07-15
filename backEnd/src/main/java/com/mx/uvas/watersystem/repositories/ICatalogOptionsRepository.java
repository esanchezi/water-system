package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICatalogOptionsRepository extends JpaRepository<CatalogOptionsEntity, Integer> {

    List<CatalogOptionsEntity> findByCatalog_CatalogoIdAndEstatus(Integer catalogoId, Integer estatus);
}
