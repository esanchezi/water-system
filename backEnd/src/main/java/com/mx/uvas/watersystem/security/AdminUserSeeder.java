package com.mx.uvas.watersystem.security;

import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import com.mx.uvas.watersystem.repositories.ISistemaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Si todavía no existe NINGUNA cuenta, crea el admin inicial con el
// usuario/contraseña de application.properties (o las variables de entorno
// ADMIN_DEFAULT_USERNAME/ADMIN_DEFAULT_PASSWORD). Es solo para el primer
// arranque -- en cuanto entres una vez, usa "Cambiar contraseña" para
// poner tu contraseña real; a partir de ahí este seeder ya no hace nada
// (la tabla deja de estar vacía).
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final ISistemaUsuarioRepository sistemaUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username}")
    private String defaultUsername;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (sistemaUsuarioRepository.count() > 0) {
            return;
        }

        SistemaUsuarioEntity admin = SistemaUsuarioEntity.builder()
                .username(defaultUsername)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .nombre("Administrador")
                .rol("ADMIN")
                .estatus(1)
                .dateAdd(LocalDateTime.now())
                .build();
        sistemaUsuarioRepository.save(admin);

        log.warn("=================================================================");
        log.warn(" Se creó la cuenta inicial de acceso al sistema:");
        log.warn("   usuario:     {}", defaultUsername);
        log.warn("   contraseña:  {}", defaultPassword);
        log.warn(" Entra y CAMBIA la contraseña de inmediato (Cambiar contraseña).");
        log.warn("=================================================================");
    }
}
