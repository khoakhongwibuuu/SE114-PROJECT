package com.carenest.backend.features.community;

import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.community.controller.AdminModerationController;
import com.carenest.backend.features.community.controller.ArticleController;
import com.carenest.backend.features.community.controller.GroupCreationRequestController;
import com.carenest.backend.features.community.dto.request.CreateArticleRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupCreationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityModuleContractTest {

    @Test
    void systemRoles_includeDoctor() {
        assertNotNull(Role.valueOf("DOCTOR"));
    }

    @Test
    void createArticle_isRestrictedToDoctorOrAdmin() throws NoSuchMethodException {
        Method method = ArticleController.class.getMethod("createArticle", CreateArticleRequest.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertTrue(preAuthorize.value().contains("DOCTOR"));
        assertTrue(preAuthorize.value().contains("ADMIN"));
    }

    @Test
    void createGroupRequest_isRestrictedToDoctor() throws NoSuchMethodException {
        Method method = GroupCreationRequestController.class.getMethod("createGroupRequest", CreateGroupCreationRequest.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertTrue(preAuthorize.value().contains("DOCTOR"));
    }

    @Test
    void myGroupRequestsEndpoint_isRestrictedToDoctor() throws NoSuchMethodException {
        Method method = GroupCreationRequestController.class.getMethod("getMyGroupRequests");
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertTrue(preAuthorize.value().contains("DOCTOR"));
    }

    @Test
    void adminGroupRequestReviewEndpoints_requireAdmin() throws NoSuchMethodException {
        Method approveMethod = GroupCreationRequestController.class.getMethod("approveGroupRequest", Long.class);
        Method rejectMethod = GroupCreationRequestController.class.getMethod("rejectGroupRequest", Long.class, String.class);

        PreAuthorize approveAuthorize = approveMethod.getAnnotation(PreAuthorize.class);
        PreAuthorize rejectAuthorize = rejectMethod.getAnnotation(PreAuthorize.class);

        assertNotNull(approveAuthorize);
        assertNotNull(rejectAuthorize);
        assertTrue(approveAuthorize.value().contains("ADMIN"));
        assertTrue(rejectAuthorize.value().contains("ADMIN"));
    }

    @Test
    void adminModerationController_isRestrictedToAdmin() {
        PreAuthorize preAuthorize = AdminModerationController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertTrue(preAuthorize.value().contains("ADMIN"));
    }
}
