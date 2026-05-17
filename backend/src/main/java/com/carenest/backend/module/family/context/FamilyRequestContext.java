package com.carenest.backend.module.family.context;

import com.carenest.backend.module.family.enums.FamilyRole;

/**
 * Per-request context holder for the active family selected via X-Family-Id header.
 * Uses ThreadLocal so each HTTP request thread has its own isolated copy.
 *
 * IMPORTANT: FamilyContextInterceptor.afterCompletion() MUST call clear()
 * to prevent memory leaks in Tomcat's thread pool (threads are reused!).
 */
public final class FamilyRequestContext {

    private static final ThreadLocal<Long>       FAMILY_ID = new ThreadLocal<>();
    private static final ThreadLocal<FamilyRole> ROLE      = new ThreadLocal<>();

    private FamilyRequestContext() {
        // Utility class — not instantiable
    }

    /** Store the validated familyId and the current user's role in that family. */
    public static void set(Long familyId, FamilyRole role) {
        FAMILY_ID.set(familyId);
        ROLE.set(role);
    }

    /** Returns the active familyId for this request, or null if not set. */
    public static Long getFamilyId() {
        return FAMILY_ID.get();
    }

    /** Returns the current user's role in the active family, or null if not set. */
    public static FamilyRole getRole() {
        return ROLE.get();
    }

    /**
     * MUST be called at the end of every request (in afterCompletion).
     * Tomcat reuses threads, so stale ThreadLocal values from a previous
     * request would be silently leaked to the next request on the same thread.
     */
    public static void clear() {
        FAMILY_ID.remove();
        ROLE.remove();
    }
}
