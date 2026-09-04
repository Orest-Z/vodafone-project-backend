package al.vodafone.vodafone_project_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Comma-separated so a second origin (e.g. the dev machine's
                // LAN IP, for testing from a phone on the same Wi-Fi) can be
                // added without a code change — see app.cors.allowed-origin.
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigin.split("\\s*,\\s*"))
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("Content-Type");
            }
        };
    }
}