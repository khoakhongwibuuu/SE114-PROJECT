package com.carenest.backend.features.family.interceptor;

import com.carenest.backend.core.api.ApiResponse;
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

/**
 * BOLA/IDOR Protection Interceptor.
 *
 * For any request carrying the X-Family-Id header, this interceptor:
 *   1. Parses the familyId from the header.
 *   2. Resolves the currently authenticated user's email from Spring Security context.
 *   3. Queries family_members to verify the user belongs to that family.
 *   4. If NOT a member → immediately returns 403 Forbidden (stops request processing).
 *   5. If IS a member  → stores (familyId, role) in FamilyRequestContext and continues.
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

        // No header → this endpoint doesn't require family context, pass through
        if (headerValue == null || headerValue.isBlank()) {
            return true;
        }

        // Parse family id
        Long familyId;
        try {
            familyId = Long.parseLong(headerValue.trim());
        } catch (NumberFormatException e) {
            writeForbidden(response, "Giá trị X-Family-Id không hợp lệ");
            return false;
        }

        // Identify current user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            writeForbidden(response, "Bạn chưa đăng nhập");
            return false;
        }

        String email = authentication.getName();

        // Verify membership — BOLA/IDOR check
        FamilyMember membership = familyMemberRepository
                .findByFamilyIdAndUserEmail(familyId, email)
                .orElse(null);

        if (membership == null) {
            log.warn("BOLA/IDOR attempt blocked: user '{}' tried to access familyId={}", email, familyId);
            writeForbidden(response, "Bạn không có quyền truy cập gia đình này");
            return false;
        }

        // All checks passed — store context for downstream service layer
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
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
