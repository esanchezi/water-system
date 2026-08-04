package com.mx.uvas.watersystem.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Todo lo que cuelga de /api/v1/** requiere estar logueada (token JWT
// válido), salvo /api/v1/auth/** (login). Los archivos del frontend
// (index.html, JS, CSS -- servidos por SpaWebConfig) se dejan libres para
// que la pantalla de login pueda cargar antes de autenticarse.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS centralizado aquí en vez de un @CrossOrigin fijo por controlador.
    // El sistema se sirve standalone (frontend + API en el mismo origen), y
    // ese origen cambia todo el tiempo: localhost:4200 en desarrollo, la IP
    // de la compu en la red local, o una URL de túnel (ngrok/Cloudflare)
    // distinta cada vez que se reabre. El navegador manda header "Origin"
    // incluso en peticiones del mismo origen (POST/PUT/DELETE), así que una
    // lista fija de orígenes permitidos se queda desactualizada y todo
    // responde 403 "Invalid CORS request" en cuanto cambia la URL del túnel.
    // Como la autenticación va por token (Authorization: Bearer), no por
    // cookies, permitir cualquier origen aquí no abre riesgo de CSRF.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        // Todo lo demás (index.html, JS, CSS del build de Angular) se
                        // sirve libre -- ahí es donde vive la propia pantalla de login.
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
