package com.mx.uvas.watersystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

// Hace que el backend pueda servir también el build de Angular (copiado a
// src/main/resources/static/, ver build-standalone.sh en la raíz del repo),
// para que todo -- API y frontend -- viva en un solo puerto. Así, para usar
// el sistema desde un túnel remoto (ngrok/Cloudflare) o desde otro
// dispositivo en la red, solo hace falta exponer el puerto del backend
// (8080): no hace falta correr "ng serve" aparte ni preocuparse por
// direcciones distintas para frontend y API.
//
// El Angular router usa rutas tipo /dashboard/receipt que no existen como
// archivo real -- si entras directo a esa URL o refrescas la página, sin
// este fallback el backend respondería 404. Este resolver intenta servir el
// archivo real primero (JS, CSS, imágenes, index.html) y, si no existe,
// regresa siempre index.html para que el router de Angular tome el control.
// Las rutas de la API (@RestController) siempre tienen prioridad sobre este
// resource handler, así que esto nunca interfiere con /api/v1/**.
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable()
                                ? requestedResource
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
