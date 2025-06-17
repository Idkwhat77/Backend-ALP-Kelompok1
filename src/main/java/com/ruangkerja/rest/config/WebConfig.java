package com.ruangkerja.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Both profile images and portfolio images use the same directory
        registry.addResourceHandler("/api/v1/images/**")
                .addResourceLocations("file:uploads/images/");
    }
}