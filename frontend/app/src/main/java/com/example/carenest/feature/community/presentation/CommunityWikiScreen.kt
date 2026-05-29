package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityWikiScreen() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val repository = application.communityRepository
    var isLoading by remember { mutableStateOf(true) }
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var comments by remember { mutableStateOf<List<ArticleComment>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentDraft by remember { mutableStateOf("") }
    var sendingComment by remember { mutableStateOf(false) }
    val likingMap = remember { mutableStateMapOf<Long, Boolean>() }
    val scope = rememberCoroutineScope()
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    suspend fun loadArticles() {
        isLoading = true
        try {
            articles = withContext(Dispatchers.IO) { repository.getArticles() }
        } catch (_: Exception) {
            articles = emptyList()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadArticles()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)),
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            articles.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Chưa có bài viết", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "N?i dung wiki s? hi?n th? t?i d�y khi b�c si dang b�i.",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text("Cẩm nang sức khỏe", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Bài viết chuyên môn từ bác sĩ và cộng đồng CareNest.",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 20.sp,
                            )
                        }
                    }

                    items(articles, key = { it.id }) { article ->
                        val liking = likingMap[article.id] == true
                        ArticleFeedCard(
                            article = article,
                            liking = liking,
                            onLike = {
                                if (!liking) {
                                    likingMap[article.id] = true
                                    scope.launch {
                                        try {
                                            val result = withContext(Dispatchers.IO) { repository.toggleArticleLike(article.id) }
                                            articles = articles.map {
                                                if (it.id == article.id) it.copy(
                                                    likedByMe = result.likedByMe,
                                                    likeCount = result.likeCount,
                                                ) else it
                                            }
                                        } finally {
                                            likingMap.remove(article.id)
                                        }
                                    }
                                }
                            },
                            onOpenComments = {
                                selectedArticle = article
                                commentDraft = ""
                                scope.launch {
                                    commentsLoading = true
                                    comments = try {
                                        withContext(Dispatchers.IO) { repository.getArticleComments(article.id) }
                                    } catch (_: Exception) {
                                        emptyList()
                                    } finally {
                                        commentsLoading = false
                                    }
                                }
                            },
                        )
                    }

                    item { Spacer(modifier = Modifier.height(92.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tạo bài viết")
        }
    }

    if (selectedArticle != null) {
        val article = selectedArticle ?: return
        ModalBottomSheet(
            onDismissRequest = { selectedArticle = null },
            containerColor = Color.White,
            sheetState = commentSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime)
                    .navigationBarsPadding()
                    .padding(top = 14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Bình luận", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    IconButton(onClick = { selectedArticle = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFF0F172A))
                    }
                }
                Text(
                    text = article.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (commentsLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text("Chưa có bình luận", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "H�y l� ngu?i d?u ti�n d?t c�u h?i ho?c chia s? th�m.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 19.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            items(comments, key = { it.id }) { comment ->
                                CommentRow(comment)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    OutlinedTextField(
                        value = commentDraft,
                        onValueChange = { commentDraft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Viết bình luận...") },
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Button(
                        onClick = {
                            val content = commentDraft.trim()
                            if (content.isNotBlank() && !sendingComment) {
                                sendingComment = true
                                scope.launch {
                                    try {
                                        val created = withContext(Dispatchers.IO) {
                                            repository.createArticleComment(article.id, content)
                                        }
                                        comments = comments + created
                                        articles = articles.map {
                                            if (it.id == article.id) it.copy(commentCount = it.commentCount + 1) else it
                                        }
                                        commentDraft = ""
                                    } finally {
                                        sendingComment = false
                                    }
                                }
                            }
                        },
                        enabled = commentDraft.trim().isNotBlank() && !sendingComment,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    ) {
                        if (sendingComment) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Message, contentDescription = "Gửi", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleFeedCard(
    article: Article,
    liking: Boolean,
    onLike: () -> Unit,
    onOpenComments: () -> Unit,
) {
    val tags = article.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.take(4).orEmpty()
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text((article.authorName ?: "C").take(1), color = PrimaryBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(article.authorName ?: "CareNest Doctor", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        if (article.authorRole == "DOCTOR") {
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(15.dp))
                        }
                    }
                    Text(article.authorSpecialty ?: "Vừa đăng", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                }
            }

            Text(
                article.title,
                modifier = Modifier.padding(horizontal = 14.dp),
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                article.content,
                modifier = Modifier.padding(horizontal = 14.dp),
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 21.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text("#$tag", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (article.likeCount > 0) "${article.likeCount} lượt thích" else "Chưa có lượt thích",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                )
                Text(
                    if (article.commentCount > 0) "${article.commentCount} bình luận" else "Chưa có bình luận",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                )
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(top = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onLike),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (article.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (article.likedByMe) Color(0xFFEF4444) else Color(0xFF64748B),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        if (liking) "Đang thích..." else "Thích",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (article.likedByMe) Color(0xFFEF4444) else Color(0xFF64748B),
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenComments),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Bình luận", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: ArticleComment) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center,
        ) {
            Text((comment.authorName ?: "N").take(1), color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 11.dp, vertical = 9.dp),
        ) {
            Text(comment.authorName ?: "Người dùng CareNest", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.content, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 20.sp)
        }
    }
}
