package com.alejandro.cv;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CvWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ovro-index6-full/**")
            .addResourceLocations("classpath:/static/ovro-index6-full/")
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
    }
}
