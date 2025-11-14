package com.pssa.cineminha.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/thumbnails/**")
                .addResourceLocations("file:media/thumbnails/");

        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:media/processed/");
    }
}