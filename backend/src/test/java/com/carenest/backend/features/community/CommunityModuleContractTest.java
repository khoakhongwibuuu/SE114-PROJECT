package com.carenest.backend.features.community;

import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.community.controller.ArticleController;
import com.carenest.backend.features.community.dto.request.CreateArticleRequest;
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
}
