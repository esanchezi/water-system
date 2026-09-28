package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISistemaUsuarioRepository extends JpaRepository<SistemaUsuarioEntity, Integer> {
    Optional<SistemaUsuarioEntity> findByUsernameAndEstatus(String username, Integer estatus);

    // Sin filtrar por estatus -- para validar unicidad al crear una cuenta
    // (no se debe poder reusar un username aunque la cuenta anterior esté
    // desactivada).
    Optional<SistemaUsuarioEntity> findByUsername(String username);

    List<SistemaUsuarioEntity> findAllByEstatusOrderByUsernameAsc(Integer estatus);
}
