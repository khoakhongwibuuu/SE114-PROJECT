package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.community.dto.request.CreateArticleCommentRequest;
import com.carenest.backend.features.community.dto.request.CreateArticleRequest;
import com.carenest.backend.features.community.dto.response.ArticleCommentResponse;
import com.carenest.backend.features.community.dto.response.ArticleLikeResponse;
import com.carenest.backend.features.community.dto.response.ArticleResponse;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        return ApiResponse.success("Đã tạo bài viết", communityKnowledgeService.createArticle(request));
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
    public ApiResponse<ArticleLikeResponse> toggleArticleLike(@PathVariable("id") Long id) {
        return ApiResponse.success("Đã cập nhật lượt thích", communityKnowledgeService.toggleArticleLike(id));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
    public ApiResponse<List<ArticleCommentResponse>> getArticleComments(@PathVariable("id") Long id) {
        return ApiResponse.success(communityKnowledgeService.getArticleComments(id));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
    public ApiResponse<ArticleCommentResponse> createArticleComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateArticleCommentRequest request) {
        return ApiResponse.success("Đã gửi bình luận", communityKnowledgeService.createArticleComment(id, request));
    }
}
