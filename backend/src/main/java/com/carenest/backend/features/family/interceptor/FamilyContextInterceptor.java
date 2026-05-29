package com.carenest.backend.features.family.interceptor;

import com.carenest.backend.features.family.context.FamilyRequestContext;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * BOLA/IDOR Protection Interceptor.
 *
 * For any request carrying the X-Family-Id header, this interceptor:
 *   1. Parses the familyId from the header.
 *   2. Resolves the currently authenticated user's email from Spring Security context.
 *   3. Queries family_members to verify the user belongs to that family.
 *   4. If NOT a member â†’ immediately returns 403 Forbidden (stops request processing).
 *   5. If IS a member  â†’ stores (familyId, role) in FamilyRequestContext and continues.
 *
 * afterCompletion() always calls FamilyRequestContext.clear() to prevent
 * ThreadLocal memory leaks in Tomcat's thread pool.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FamilyContextInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "X-Family-Id";

    private final FamilyMemberRepository familyMemberRepository;
    private final ObjectMapper            objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String headerValue = request.getHeader(HEADER_NAME);

        // No header â†’ this endpoint doesn't require family context, pass through
        if (headerValue == null || headerValue.isBlank()) {
            return true;
        }

        // Parse family id
        Long familyId;
        try {
            familyId = Long.parseLong(headerValue.trim());
        } catch (NumberFormatException e) {
            writeForbidden(response, "GiÃ¡ trá»‹ X-Family-Id khÃ´ng há»£p lá»‡");
            return false;
        }

        // Identify current user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            writeForbidden(response, "Báº¡n chÆ°a Ä‘Äƒng nháº­p");
            return false;
        }

        String email = authentication.getName();

        // Verify membership â€” BOLA/IDOR check
        FamilyMember membership = familyMemberRepository
                .findByFamilyIdAndUserEmail(familyId, email)
                .orElse(null);

        if (membership == null) {
            log.warn("BOLA/IDOR attempt blocked: user '{}' tried to access familyId={}", email, familyId);
            writeForbidden(response, "Báº¡n khÃ´ng cÃ³ quyá»n truy cáº­p gia Ä‘Ã¬nh nÃ y");
            return false;
        }

        // All checks passed â€” store context for downstream service layer
        FamilyRequestContext.set(familyId, membership.getRole());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // Always clear to prevent ThreadLocal leak in Tomcat thread pool
        FamilyRequestContext.clear();
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                Map.of(
                        "success", false,
                        "message", message,
                        "data", null
                ));
    }
}
