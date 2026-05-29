package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    long countByArticleId(Long articleId);

    List<ArticleComment> findAllByArticleIdOrderByCreatedAtAsc(Long articleId);
}
