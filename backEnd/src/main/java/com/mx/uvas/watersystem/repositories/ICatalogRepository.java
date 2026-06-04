package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.CatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICatalogRepository extends JpaRepository <CatalogEntity,Integer>{
}
