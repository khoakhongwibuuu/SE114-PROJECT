package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.community.dto.response.AdminReportSummaryResponse;
import com.carenest.backend.features.community.entity.ArticleComment;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final ReportTicketRepository reportTicketRepository;
    private final GroupPostRepository groupPostRepository;
    private final ArticleCommentRepository articleCommentRepository;

    @GetMapping("/reports")
    public ApiResponse<List<AdminReportSummaryResponse>> getReports(
            @RequestParam(name = "status", defaultValue = "PENDING") String status
    ) {
        List<AdminReportSummaryResponse> reports = reportTicketRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ApiResponse.success(reports);
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable("postId") Long postId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        reportTicketRepository.deleteAllByReportedPostId(postId);
        groupPostRepository.delete(post);
        return ApiResponse.success("Đã xóa bài viết vi phạm", null);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("ArticleComment", commentId));
        articleCommentRepository.delete(comment);
        return ApiResponse.success("Đã xóa bình luận vi phạm", null);
    }

    @PatchMapping("/reports/{reportId}/dismiss")
    public ApiResponse<Void> dismissReport(@PathVariable("reportId") Long reportId) {
        ReportTicket report = reportTicketRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportTicket", reportId));
        reportTicketRepository.delete(report);
        return ApiResponse.success("Đã bỏ qua báo cáo", null);
    }

    private AdminReportSummaryResponse toResponse(ReportTicket ticket) {
        GroupPost post = ticket.getReportedPost();
        return AdminReportSummaryResponse.builder()
                .id(ticket.getId())
                .postId(post != null ? post.getId() : null)
                .commentId(null)
                .contentType("POST")
                .reporterId(ticket.getReporter() != null ? ticket.getReporter().getId() : null)
                .reporterName(ticket.getReporter() != null ? ticket.getReporter().getFullName() : null)
                .reporterEmail(ticket.getReporter() != null ? ticket.getReporter().getEmail() : null)
                .reason(ticket.getReason())
                .previewText(post != null ? post.getContent() : null)
                .previewImageUrl(post != null ? post.getImageUrl() : null)
                .contentAuthorName(post != null && post.getAuthor() != null ? post.getAuthor().getFullName() : null)
                .status(statusOrPending(statusFromRequestAwareTicket(ticket)))
                .createdAt(ticket.getCreatedAt())
                .build();
    }

    private String statusFromRequestAwareTicket(ReportTicket ticket) {
        return ticket != null ? "PENDING" : "PENDING";
    }

    private String statusOrPending(String status) {
        return status == null || status.isBlank() ? "PENDING" : status;
    }
}
