package com.example.carenest.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Giả lập Model Bài viết
data class Article(
    val id: String,
    val authorName: String,
    val authorRole: String, // "DOCTOR", "MEMBER", "OWNER"
    val time: Long,
    val title: String,
    val content: String,
    val tags: List<String>,
    var likeCount: Int,
    var commentCount: Int,
    var isLikedByMe: Boolean
)

// Giả lập Comment Dạng Cây (Nested List)
data class CommentNode(
    val id: String,
    val authorName: String,
    val content: String,
    val replies: List<CommentNode> = emptyList()
)

val mockComments = listOf(
    CommentNode(
        id = "c1",
        authorName = "Nguyễn Văn C",
        content = "Bài viết rất hữu ích, cảm ơn bác sĩ!",
        replies = listOf(
            CommentNode(
                id = "c1-r1",
                authorName = "BS. Nguyễn Văn A",
                content = "Cảm ơn bạn đã quan tâm. Chúc gia đình nhiều sức khỏe!"
            )
        )
    ),
    CommentNode(
        id = "c2",
        authorName = "Trần Thị D",
        content = "Cho mình hỏi thêm về liều lượng thuốc hạ sốt cho trẻ 3 tuổi với ạ?"
    )
)

val dummyArticles = mutableStateListOf(
    Article("1", "BS. Nguyễn Văn A", "DOCTOR", System.currentTimeMillis() - 3600000, "Cách phòng tránh sốt xuất huyết", "Sốt xuất huyết là bệnh truyền nhiễm cấp tính, do vi rút Dengue gây ra...", listOf("Sức khỏe", "Phòng bệnh"), 150, 23, false),
    Article("2", "CareNest Admin", "OWNER", System.currentTimeMillis() - 7200000, "Cập nhật tính năng mới: Hồ sơ sức khỏe", "Từ hôm nay, bạn đã có thể tạo nhiều hồ sơ sức khỏe cho gia đình mình.", listOf("Thông báo", "Tính năng"), 89, 5, true),
    Article("3", "Trần Thị B", "MEMBER", System.currentTimeMillis() - 86400000, "Hỏi về lịch tiêm phòng cho bé 6 tháng", "Các bác sĩ cho em hỏi bé nhà em 6 tháng thì cần tiêm những mũi gì ạ?", listOf("Hỏi đáp", "Tiêm chủng"), 12, 10, false)
)

// Giả lập User Session Role
val currentUserRole = "OWNER" // Thay đổi để test logic RoleGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    var showCommentModal by remember { mutableStateOf(false) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    Scaffold(
        containerColor = Color(0xFFF0F4F8),
        topBar = {
            TopAppBar(
                title = { Text("Cộng đồng y tế", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F4F8))
            )
        },
        floatingActionButton = {
            if (currentUserRole == "DOCTOR" || currentUserRole == "OWNER") {
                FloatingActionButton(
                    onClick = { /* TODO: Đăng bài */ },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo bài viết")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Cẩm nang sức khỏe & Thảo luận",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (dummyArticles.isEmpty()) {
                // Shimmer Loading Skeleton
                items(3) {
                    ArticleSkeletonCard()
                }
            } else {
                items(dummyArticles) { article ->
                    ArticleCard(
                        article = article,
                        onToggleLike = {
                            article.isLikedByMe = !article.isLikedByMe
                            article.likeCount += if (article.isLikedByMe) 1 else -1
                            // Kích hoạt re-compose trong thực tế dùng ViewModel
                        },
                        onComment = {
                            selectedArticle = article
                            showCommentModal = true
                        },
                        onDelete = {
                            dummyArticles.remove(article)
                        }
                    )
                }
            }
        }

        if (showCommentModal && selectedArticle != null) {
            CommentModal(
                article = selectedArticle!!,
                onDismiss = { showCommentModal = false }
            )
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onToggleLike: () -> Unit,
    onComment: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(article.authorName.take(1), fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                    if (article.authorRole == "DOCTOR") {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF0EA5E9),
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(article.authorName, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    }
                    Text(
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(article.time)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                }

                // RBAC: Xóa bài viết
                if (currentUserRole == "OWNER" || currentUserRole == "ADMIN" || article.authorRole == currentUserRole) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content
            Text(article.title, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), lineHeight = 25.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(article.content, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 21.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                article.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("#$tag", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (article.likeCount > 0) "${article.likeCount} lượt thích" else "Chưa có lượt thích", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Text(if (article.commentCount > 0) "${article.commentCount} bình luận" else "Chưa có bình luận", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

            // Actions
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onToggleLike)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (article.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (article.isLikedByMe) Color(0xFFEF4444) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Thích", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (article.isLikedByMe) Color(0xFFEF4444) else Color(0xFF64748B))
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onComment)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Bình luận", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun CommentItemNode(node: CommentNode, depth: Int = 0) {
    Column(modifier = Modifier.padding(start = (depth * 24).dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Text(node.authorName.take(1), fontSize = 13.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 11.dp, vertical = 9.dp)
            ) {
                Text(node.authorName, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(4.dp))
                Text(node.content, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Trả lời", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, modifier = Modifier.clickable { /* TODO: Reply to this node */ })
            }
        }
        
        // Đệ quy hiển thị các reply
        if (node.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            node.replies.forEach { reply ->
                CommentItemNode(reply, depth + 1)
            }
        }
    }
}

@Composable
fun ArticleSkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Box(modifier = Modifier.width(120.dp).height(14.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.width(80.dp).height(10.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().height(18.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Box(modifier = Modifier.width(60.dp).height(24.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(60.dp).height(24.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)))
            }
        }
    }
}

@Composable
fun CommentModal(article: Article, onDismiss: () -> Unit) {
    // Giả lập giao diện Modal Bình luận giống React Native
    // Ở Compose ta có thể dùng ModalBottomSheet
    // Vì không import material3.ModalBottomSheet trong file này để giữ nhẹ nhàng, ta dùng một màn hình overlay giả
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0x7A0F172A))
        .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(Color.White)
                .clickable(enabled = false, onClick = {}) // Ngăn click xuyên qua
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bình luận", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                IconButton(onClick = onDismiss, modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape).size(34.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0F172A), modifier = Modifier.size(20.dp))
                }
            }
            Text(article.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Body
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                if (mockComments.isEmpty()) {
                    item {
                        Text("Chưa có bình luận", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), modifier = Modifier.padding(top = 34.dp).align(Alignment.CenterHorizontally))
                        Text("Hãy là người đầu tiên đặt câu hỏi hoặc chia sẻ thêm.", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally))
                    }
                } else {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    items(mockComments) { comment ->
                        CommentItemNode(comment)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Input
            Divider(color = Color(0xFFE2E8F0))
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Viết bình luận...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f).heightIn(min = 42.dp, max = 104.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(42.dp).background(PrimaryBlue, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
