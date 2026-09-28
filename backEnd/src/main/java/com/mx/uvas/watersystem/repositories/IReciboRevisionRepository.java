package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ReciboRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReciboRevisionRepository extends JpaRepository<ReciboRevisionEntity, Integer> {

    List<ReciboRevisionEntity> findByFoto_FotoIdAndEstatus(Integer fotoId, Integer estatus);
}
