package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ISistemaUsuarioRepository extends JpaRepository<SistemaUsuarioEntity, Integer> {
    Optional<SistemaUsuarioEntity> findByUsernameAndEstatus(String username, Integer estatus);
}
