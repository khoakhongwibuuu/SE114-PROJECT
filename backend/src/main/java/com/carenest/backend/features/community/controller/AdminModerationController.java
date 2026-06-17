package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.chat.entity.GroupChatMessage;
import com.carenest.backend.features.chat.repository.GroupChatMessageRepository;
import com.carenest.backend.features.community.dto.response.AdminReportSummaryResponse;
import com.carenest.backend.features.community.entity.ArticleComment;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.enums.ReportStatus;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import com.carenest.backend.features.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final CommunityKnowledgeService communityKnowledgeService;

    @GetMapping("/reports")
    public ApiResponse<List<AdminReportSummaryResponse>> getReports(
            @RequestParam(name = "status", defaultValue = "PENDING") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        ReportStatus reportStatus = parseStatus(status);
        List<AdminReportSummaryResponse> reports = reportTicketRepository
                .findAllByStatusOrderByCreatedAtDesc(reportStatus, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return ApiResponse.success(reports);
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User currentAdmin) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        reportTicketRepository.findAllByReportedPostId(postId).forEach(report -> {
            report.setStatus(ReportStatus.RESOLVED);
            report.setResolvedBy(currentAdmin);
            report.setResolvedAt(java.time.Instant.now());
            report.setReportedPost(null);
            reportTicketRepository.save(report);
        });
        groupPostRepository.delete(post);
        return ApiResponse.success("Đã xóa bài viết vi phạm", null);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal User currentAdmin) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("ArticleComment", commentId));
        articleCommentRepository.delete(comment);
        return ApiResponse.success("Đã xóa bình luận vi phạm", null);
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable("messageId") Long messageId,
            @AuthenticationPrincipal User currentAdmin) {
        GroupChatMessage message = groupChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupChatMessage", messageId));
        reportTicketRepository.findAllByReportedChatMessageId(messageId).forEach(report -> {
            report.setStatus(ReportStatus.RESOLVED);
            report.setResolvedBy(currentAdmin);
            report.setResolvedAt(java.time.Instant.now());
            report.setReportedChatMessage(null);
            reportTicketRepository.save(report);
        });
        groupChatMessageRepository.delete(message);
        return ApiResponse.success("Da xoa tin nhan vi pham", null);
    }

    @PatchMapping("/reports/{reportId}/dismiss")
    public ApiResponse<Void> dismissReport(
            @PathVariable("reportId") Long reportId,
            @AuthenticationPrincipal User currentAdmin) {
        ReportTicket report = reportTicketRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportTicket", reportId));
        report.setStatus(ReportStatus.DISMISSED);
        report.setResolvedBy(currentAdmin);
        report.setResolvedAt(java.time.Instant.now());
        reportTicketRepository.save(report);
        return ApiResponse.success("Đã bỏ qua báo cáo", null);
    }

    @PostMapping("/groups/{groupId}/freeze")
    public ApiResponse<Void> freezeGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam("reason") String reason) {
        communityKnowledgeService.freezeGroup(groupId, reason);
        return ApiResponse.success("Da tam khoa nhom", null);
    }

    @PostMapping("/groups/{groupId}/unfreeze")
    public ApiResponse<Void> unfreezeGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam("reason") String reason) {
        communityKnowledgeService.unfreezeGroup(groupId, reason);
        return ApiResponse.success("Da mo khoa nhom", null);
    }

    private AdminReportSummaryResponse toResponse(ReportTicket ticket) {
        GroupPost post = ticket.getReportedPost();
        GroupChatMessage message = ticket.getReportedChatMessage();
        return AdminReportSummaryResponse.builder()
                .id(ticket.getId())
                .postId(post != null ? post.getId() : null)
                .messageId(message != null ? message.getId() : null)
                .commentId(null)
                .contentType(message != null ? "MESSAGE" : "POST")
                .reporterId(ticket.getReporter() != null ? ticket.getReporter().getId() : null)
                .reporterName(ticket.getReporter() != null ? ticket.getReporter().getFullName() : null)
                .reporterEmail(ticket.getReporter() != null ? ticket.getReporter().getEmail() : null)
                .reason(ticket.getReason())
                .previewText(message != null ? message.getContent() : (post != null ? post.getContent() : null))
                .previewImageUrl(post != null ? post.getImageUrl() : null)
                .contentAuthorName(resolveContentAuthorName(post, message))
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : ReportStatus.PENDING.name())
                .createdAt(ticket.getCreatedAt())
                .build();
    }

    private String resolveContentAuthorName(GroupPost post, GroupChatMessage message) {
        if (message != null && message.getSender() != null) {
            return message.getSender().getFullName();
        }
        if (post != null && post.getAuthor() != null) {
            return post.getAuthor().getFullName();
        }
        return null;
    }

    private ReportStatus parseStatus(String rawStatus) {
        try {
            return ReportStatus.valueOf(rawStatus == null ? "PENDING" : rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ReportStatus.PENDING;
        }
    }
}
