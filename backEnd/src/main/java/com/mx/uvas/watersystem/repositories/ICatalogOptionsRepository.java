package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICatalogOptionsRepository extends JpaRepository<CatalogOptionsEntity, Integer> {

    List<CatalogOptionsEntity> findByCatalog_CatalogoIdAndEstatus(Integer catalogoId, Integer estatus);

    // Para resolver por nombre una opción dentro de un catálogo -- ej.
    // encontrar la opción "Aviso" del catálogo CONCEPTO_CARGO_EXTRA sin
    // tener que conocer de antemano su ID (ver
    // AvisoAdeudoService.crearCargoAviso()).
    Optional<CatalogOptionsEntity> findByCatalog_ClaveAndNombreAndEstatus(String catalogoClave, String nombre, Integer estatus);
}
