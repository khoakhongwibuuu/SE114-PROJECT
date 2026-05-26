package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    long countByArticleId(Long articleId);

    boolean existsByArticleIdAndUserId(Long articleId, Long userId);

    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
