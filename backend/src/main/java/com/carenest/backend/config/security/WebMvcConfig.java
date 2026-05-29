package com.carenest.backend.config.security;

import com.carenest.backend.features.family.interceptor.FamilyContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Registers Spring MVC interceptors.
 *
 * FamilyContextInterceptor is applied to all /families/** routes that need
 * the X-Family-Id header validated. Routes that don't send that header (e.g.
 * GET /families/my-list, POST /families) are unaffected â€” the interceptor is
 * a no-op when the header is absent.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FamilyContextInterceptor familyContextInterceptor;

    @Value("${app.media.upload-dir:uploads/media}")
    private String mediaUploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(familyContextInterceptor)
                // Apply to all family-related API routes and health data routes
                .addPathPatterns(
                        "/families/**",
                        "/health-profiles/**",
                        "/medications/**",
                        "/medication-logs/**",
                        "/cabinets/**",
                        "/appointments/**",
                        "/vaccination-records/**",
                        "/growth-records/**",
                        "/chat/**"
                )
                // Exclude the list endpoints that don't require an active family context
                .excludePathPatterns(
                        "/families/my-list",
                        "/families/join-by-code",
                        "/families/join-by-qr",
                        "/auth/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String mediaLocation = Path.of(mediaUploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!mediaLocation.endsWith("/")) {
            mediaLocation = mediaLocation + "/";
        }
        registry.addResourceHandler("/media/files/**")
                .addResourceLocations(mediaLocation);
    }
}
