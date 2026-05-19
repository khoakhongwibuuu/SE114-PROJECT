package com.carenest.backend.module.community.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.community.dto.request.CreateArticleRequest;
import com.carenest.backend.module.community.dto.response.ArticleResponse;
import com.carenest.backend.module.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final CommunityKnowledgeService communityKnowledgeService;

    @GetMapping
    public ApiResponse<List<ArticleResponse>> getArticles() {
        return ApiResponse.success(communityKnowledgeService.getArticles());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ApiResponse<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        return ApiResponse.success("Article created successfully", communityKnowledgeService.createArticle(request));
    }
}
