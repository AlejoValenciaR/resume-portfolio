package com.alejandro.photography;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

@Configuration
public class PhotographyWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor noCacheInterceptor = new WebContentInterceptor();
        noCacheInterceptor.setCacheControl(CacheControl.noStore().mustRevalidate());

        registry.addInterceptor(noCacheInterceptor)
                .addPathPatterns(
                        "/photography",
                        "/photography/",
                        "/photography/**",
                        "/fotografia",
                        "/fotografia/",
                        "/fotografia/**");
    }
}
