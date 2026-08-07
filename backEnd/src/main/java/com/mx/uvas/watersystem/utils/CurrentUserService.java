package com.mx.uvas.watersystem.utils;

import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import com.mx.uvas.watersystem.repositories.ISistemaUsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Devuelve el id del usuario del sistema (login) que está haciendo el
// cambio actual, para llenar userIdAdd/userIdUpdate con quién fue
// realmente -- antes se guardaba fijo en 1 sin importar quién estuviera
// conectado, así que la bitácora no servía para saber quién cambió qué.
@Component
@AllArgsConstructor
public class CurrentUserService {

    private final ISistemaUsuarioRepository sistemaUsuarioRepository;

    // Respaldo por si por algún motivo no hay sesión en el contexto (ej.
    // un proceso interno que corre sin request HTTP) -- así no se rompe
    // el guardado, solo se pierde el detalle de quién fue.
    private static final Integer FALLBACK_USER_ID = 1;

    public Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return FALLBACK_USER_ID;
        }
        return sistemaUsuarioRepository.findByUsernameAndEstatus(auth.getName(), 1)
                .map(SistemaUsuarioEntity::getSistemaUsuarioId)
                .orElse(FALLBACK_USER_ID);
    }
}
