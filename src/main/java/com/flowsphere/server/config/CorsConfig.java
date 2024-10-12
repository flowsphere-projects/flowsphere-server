package com.flowsphere.server.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Optional;


@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(CorsProperties corsProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(
                Optional.ofNullable(corsProperties.getPath()).orElse("/**"),
                buildConfig(corsProperties));
        return new CorsFilter(source);
    }

    private CorsConfiguration buildConfig(CorsProperties corsProperties) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        Optional.ofNullable(corsProperties.getAllowOrigins())
                .ifPresent(origins -> origins.forEach(corsConfiguration::addAllowedOrigin));
        Optional.ofNullable(corsProperties.getAllowHeaders())
                .ifPresent(headers -> headers.forEach(corsConfiguration::addAllowedHeader));
        Optional.ofNullable(corsProperties.getAllowMethods())
                .ifPresent(methods -> methods.forEach(corsConfiguration::addAllowedMethod));
        Optional.ofNullable(corsProperties.getAllowExposeHeaders())
                .ifPresent(headers -> headers.forEach(corsConfiguration::addExposedHeader));
        return corsConfiguration;
    }

}
