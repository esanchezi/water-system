package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ValvulaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IValvulaRepository extends JpaRepository<ValvulaEntity, Integer> {
}
