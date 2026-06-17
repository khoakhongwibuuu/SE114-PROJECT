package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.GroupPost

// ---------------------------------------------------------------------------
// Helper: derive a display title if backend didn't send one yet (old posts)
// ---------------------------------------------------------------------------
private fun GroupPost.displayTitle(): String {
    if (!title.isNullOrBlank()) return title
    val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: content
    return if (firstLine.length <= 80) firstLine else firstLine.take(77) + "..."
}

// When title is real (from backend), body = full content.
// When title was derived from the first line, body = content MINUS that first line
// to avoid showing the same sentence twice.
private fun GroupPost.bodyPreview(): String {
    if (!title.isNullOrBlank()) return content // real title — show all of content
    val lines = content.lines()
    val remaining = lines.dropWhile { it.isBlank() }.drop(1).dropWhile { it.isBlank() }
    return remaining.joinToString("\n").trim()
}

// ---------------------------------------------------------------------------
// Helper: format createdAt string to readable date
// ---------------------------------------------------------------------------
private fun String?.toReadableDate(): String {
    if (this == null || length < 10) return ""
    return take(10) // "YYYY-MM-DD"
}

// ---------------------------------------------------------------------------
// Panes
// ---------------------------------------------------------------------------

@Composable
fun ApprovedPostsPane(
    posts: List<GroupPost>,
    isLoading: Boolean,
    error: String?,
    canModerate: Boolean,
    onNavigateToCreatePost: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onDoctorClick: (Long) -> Unit,
    onReportClick: (GroupPost) -> Unit,
    onDeleteClick: (GroupPost) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            !error.isNullOrBlank() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error, color = Color(0xFFEF4444), fontSize = 14.sp)
            }
            posts.isEmpty() -> EmptyState("Chưa có bài viết nào trong nhóm này.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp, ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    StructuredGroupPostCard(
                        post = post,
                        onLikeClick = onLikeClick,
                        onCommentClick = onCommentClick,
                        onDoctorClick = onDoctorClick,
                        actionRow = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (canModerate) {
                                    TextButton(onClick = { onDeleteClick(post) }) {
                                        Text("Gỡ bài", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                TextButton(onClick = { onReportClick(post) }) {
                                    Text("Báo cáo", color = Color(0xFFDC2626))
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        Button(
            onClick = onNavigateToCreatePost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("+ Đăng bài viết", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MyGroupPostsPane(
    posts: List<GroupPost>,
    isLoading: Boolean,
    error: String?,
    onNavigateToCreatePost: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onDoctorClick: (Long) -> Unit,
    onEditClick: (GroupPost) -> Unit,
    onDeleteClick: (GroupPost) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            !error.isNullOrBlank() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error, color = Color(0xFFEF4444), fontSize = 14.sp)
            }
            posts.isEmpty() -> EmptyState("Bạn chưa đăng bài viết nào trong nhóm này.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    StructuredGroupPostCard(
                        post = post, 
                        showStatus = true,
                        onLikeClick = onLikeClick,
                        onCommentClick = onCommentClick,
                        onDoctorClick = onDoctorClick,
                        actionRow = {
                            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(top = 8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEditClick(post) }) {
                                    Text("Chỉnh sửa", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { onDeleteClick(post) }) {
                                    Text("Xóa", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        Button(
            onClick = onNavigateToCreatePost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("+ Đăng bài viết", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModerationQueuePane(
    posts: List<GroupPost>,
    isLoading: Boolean,
    error: String?,
    onApprove: (Long) -> Unit,
    onReject: (Long, String) -> Unit
) {
    var rejectPostId by remember { mutableStateOf<Long?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    if (rejectPostId != null) {
        AlertDialog(
            onDismissRequest = { rejectPostId = null; rejectReason = "" },
            containerColor = Color.White,
            title = {
                Text("Từ chối bài viết", fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text("Vui lòng nhập lý do từ chối để tác giả có thể chỉnh sửa.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Lý do từ chối") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val postId = rejectPostId
                        if (postId != null && rejectReason.isNotBlank()) {
                            onReject(postId, rejectReason)
                            rejectPostId = null
                            rejectReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    enabled = rejectReason.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Từ chối")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectPostId = null; rejectReason = "" }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            !error.isNullOrBlank() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error, color = Color(0xFFEF4444), fontSize = 14.sp)
            }
            posts.isEmpty() -> EmptyState("Không có bài viết nào đang chờ duyệt.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    StructuredGroupPostCard(
                        post = post,
                        showStatus = true,
                        actionRow = {
                            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(top = 12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { rejectPostId = post.id }) {
                                    Text("Từ chối", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onApprove(post.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Duyệt", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Structured Post Card
// ---------------------------------------------------------------------------

@Composable
fun StructuredGroupPostCard(
    post: GroupPost,
    showStatus: Boolean = false,
    onLikeClick: ((Long) -> Unit)? = null,
    onCommentClick: ((Long) -> Unit)? = null,
    onDoctorClick: ((Long) -> Unit)? = null,
    actionRow: (@Composable () -> Unit)? = null
) {
    val displayTitle = post.displayTitle()
    val tags = post.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.take(5).orEmpty()
    val initials = (post.authorName ?: "?").take(1).uppercase()
    val isDoctor = post.authorRole == "DOCTOR"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ---- Header row ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDoctor && post.authorId != null && onDoctorClick != null) {
                            Modifier.clickable { onDoctorClick(post.authorId) }
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = PrimaryBlue, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            post.authorName ?: "Thành viên",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        if (isDoctor) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF0EA5E9),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        post.createdAt.toReadableDate(),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                // Status chip in header (for My Posts and Moderation Queue)
                if (showStatus && post.status != null) {
                    val (chipColor, chipBg, statusLabel) = when (post.status) {
                        "PENDING_APPROVAL" -> Triple(Color(0xFFF59E0B), Color(0xFFFFFBEB), "Chờ duyệt")
                        "APPROVED"         -> Triple(Color(0xFF10B981), Color(0xFFF0FDF4), "Đã duyệt")
                        "REJECTED"         -> Triple(Color(0xFFEF4444), Color(0xFFFEF2F2), "Từ chối")
                        else               -> Triple(Color(0xFF94A3B8), Color(0xFFF1F5F9), post.status)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = chipColor)
                    }
                }
            }

            // ---- Title ----
            Text(
                text = displayTitle,
                modifier = Modifier.padding(horizontal = 14.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ---- Body preview ----
            // When title is real, show full content. When title was derived from first line,
            // strip that first line from the preview to avoid repetition.
            val bodyText = post.bodyPreview()
            if (bodyText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bodyText,
                    modifier = Modifier.padding(horizontal = 14.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ---- Optional image ----
            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = displayTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            // ---- Tags row ----
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("#$tag", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                }
            }

            // ---- Rejection reason (only when rejected) ----
            if (post.status == "REJECTED" && !post.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Lý do từ chối: ${post.rejectionReason}",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        lineHeight = 17.sp
                    )
                }
            }

            // ---- Interaction Stats & Buttons (For Approved Posts Only) ----
            if (post.status.equals("APPROVED", ignoreCase = true) || post.status == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${post.likeCount} lượt thích • ${post.commentCount} bình luận",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { onLikeClick?.invoke(post.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Thích", 
                            color = if (post.likedByMe) PrimaryBlue else Color(0xFF64748B),
                            fontWeight = if (post.likedByMe) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                    TextButton(
                        onClick = { onCommentClick?.invoke(post.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bình luận", color = Color(0xFF64748B))
                    }
                }
            }

            // ---- Action row (moderation approve/reject buttons) ----
            if (actionRow != null) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    actionRow()
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
