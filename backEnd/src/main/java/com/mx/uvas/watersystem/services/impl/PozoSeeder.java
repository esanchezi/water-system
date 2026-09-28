package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.model.PozoEntity;
import com.mx.uvas.watersystem.repositories.IPozoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Los pozos son fijos (solo 2, no cambian) -- en vez de una pantalla para
// darlos de alta, se siembran una sola vez al arrancar el backend, igual
// que AdminUserSeeder. Si ya existe algún pozo, no hace nada.
@Slf4j
@Component
@RequiredArgsConstructor
public class PozoSeeder implements CommandLineRunner {

    private final IPozoRepository pozoRepository;

    @Override
    public void run(String... args) {
        if (pozoRepository.count() > 0) {
            return;
        }

        pozoRepository.save(PozoEntity.builder()
                .nombre("Pozo Los López (tanque elevado)")
                .lat(new BigDecimal("21.0445605"))
                .lng(new BigDecimal("-101.5704643"))
                .observaciones("Motor Altamira serie RT 20hp; 3x460V; amperaje nominal 28.5; " +
                        "factor de servicio 32.8; diámetro nominal 6 pulgadas")
                .estatus(1)
                .dateAdd(LocalDateTime.now())
                .build());

        pozoRepository.save(PozoEntity.builder()
                .nombre("Pozo Buenavista")
                .lat(new BigDecimal("21.046418"))
                .lng(new BigDecimal("-101.580237"))
                .observaciones("Se bombea directo a la red")
                .estatus(1)
                .dateAdd(LocalDateTime.now())
                .build());

        log.info("Pozos fijos sembrados: Pozo Los López (tanque elevado) y Pozo Buenavista.");
    }
}
