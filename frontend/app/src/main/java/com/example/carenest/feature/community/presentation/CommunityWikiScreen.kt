package com.example.carenest.feature.community.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.Article
import com.example.carenest.feature.community.domain.model.ArticleComment
import com.example.carenest.feature.community.domain.model.CommunityGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityWikiScreen(
    canCreateArticle: Boolean = false,
    refreshTrigger: Int = 0,
    onOpenGroup: (CommunityGroup) -> Unit = {},
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val repository = application.communityRepository
    val scope = rememberCoroutineScope()
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLoading by remember { mutableStateOf(true) }
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var comments by remember { mutableStateOf<List<ArticleComment>>(emptyList()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var commentDraft by remember { mutableStateOf("") }
    var sendingComment by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var uploadingImage by remember { mutableStateOf(false) }
    var savingArticle by remember { mutableStateOf(false) }
    val likingMap = remember { mutableStateMapOf<Long, Boolean>() }
    var selectedDoctorArticle by remember { mutableStateOf<Article?>(null) }
    var groupActionLoadingId by remember { mutableStateOf<Long?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploadingImage = true
                try {
                    imageUrl = withContext(Dispatchers.IO) { repository.uploadArticleImage(context, uri) }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, e.localizedMessage ?: "Không thể tải ảnh lên", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    uploadingImage = false
                }
            }
        }
    }

    suspend fun loadArticles() {
        isLoading = true
        try {
            articles = withContext(Dispatchers.IO) { repository.getArticles() }
        } catch (e: Exception) {
            articles = emptyList()
            android.widget.Toast.makeText(context, e.localizedMessage ?: "Không thể tải bài viết", android.widget.Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    fun resetCreateForm() {
        title = ""
        tags = ""
        content = ""
        imageUrl = ""
        uploadingImage = false
        savingArticle = false
    }

    LaunchedEffect(refreshTrigger) {
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
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Chưa có bài viết", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Nội dung wiki sẽ hiển thị tại đây khi bác sĩ đăng bài.",
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
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
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, e.localizedMessage ?: "Không thể thích bài viết", android.widget.Toast.LENGTH_SHORT).show()
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
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, e.localizedMessage ?: "Không thể tải bình luận", android.widget.Toast.LENGTH_SHORT).show()
                                        emptyList()
                                    } finally {
                                        commentsLoading = false
                                    }
                                }
                            },
                            onDoctorClick = { selectedDoctorArticle = it }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(92.dp)) }
                }
            }
        }

        if (canCreateArticle) {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
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
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
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
                                        "Hãy là người đầu tiên đặt câu hỏi hoặc chia sẻ thêm.",
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
                            val commentContent = commentDraft.trim()
                            if (commentContent.isNotBlank() && !sendingComment) {
                                sendingComment = true
                                scope.launch {
                                    try {
                                        val created = withContext(Dispatchers.IO) {
                                            repository.createArticleComment(article.id, commentContent)
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
                        contentPadding = PaddingValues(0.dp),
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

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCreateSheet = false
                resetCreateForm()
            },
            containerColor = Color.White,
            sheetState = createSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.ime)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text("Tạo bài viết", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tiêu đề") },
                    placeholder = { Text("Ví dụ: Chăm sóc trẻ sốt tại nhà") },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category / tags") },
                    placeholder = { Text("Nhi khoa, sốt, dinh dưỡng") },
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !uploadingImage && !savingArticle,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = PrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imageUrl.isBlank()) "Chọn ảnh minh họa" else "Đổi ảnh minh họa")
                }
                if (imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Ảnh minh họa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = { Text("Nội dung") },
                    placeholder = { Text("Nhập nội dung bài viết...") },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 10,
                )

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        val finalTitle = title.trim()
                        val finalContent = content.trim()
                        if (finalTitle.isNotBlank() && finalContent.isNotBlank() && !savingArticle && !uploadingImage) {
                            savingArticle = true
                            scope.launch {
                                try {
                                    val created = withContext(Dispatchers.IO) {
                                        repository.createArticle(
                                            title = finalTitle,
                                            content = finalContent,
                                            tags = tags.trim(),
                                            imageUrl = imageUrl.trim()
                                        )
                                    }
                                    articles = listOf(created) + articles
                                    showCreateSheet = false
                                    resetCreateForm()
                                } finally {
                                    savingArticle = false
                                }
                            }
                        }
                    },
                    enabled = title.trim().isNotBlank() && content.trim().isNotBlank() && !savingArticle && !uploadingImage,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                ) {
                    if (savingArticle) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Đăng bài", color = Color.White)
                    }
                }
            }
        }
    }

    if (selectedDoctorArticle != null) {
        val doctorArticle = selectedDoctorArticle ?: return
        val doctorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedDoctorArticle = null },
            containerColor = Color.White,
            sheetState = doctorSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFCBD5E1)),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                (doctorArticle.authorName ?: "B").take(1),
                                color = PrimaryBlue,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    doctorArticle.authorName ?: "Bác sĩ CareNest",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF0EA5E9),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                doctorArticle.authorSpecialty ?: "Chuyên khoa",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            if (!doctorArticle.authorHospitalName.isNullOrBlank()) {
                                Text(
                                    doctorArticle.authorHospitalName,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { selectedDoctorArticle = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFF64748B))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Dịch vụ hỗ trợ trực tuyến",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                val privateId = doctorArticle.authorPrivateGroupId
                val hasPrivate = privateId != null
                DoctorActionBlock(
                    title = "Phòng tư vấn riêng",
                    description = "Nhắn tin và trao đổi trực tiếp 1-1 với bác sĩ về tình trạng của gia đình.",
                    enabled = hasPrivate,
                    loading = groupActionLoadingId == privateId && privateId != null,
                    onClick = {
                        if (privateId != null) {
                            groupActionLoadingId = privateId
                            scope.launch {
                                try {
                                    val preview = withContext(Dispatchers.IO) {
                                        repository.preview(privateId)
                                    }
                                    if (preview.joined) {
                                        val group = CommunityGroup(
                                            id = preview.id,
                                            name = preview.name,
                                            description = preview.description,
                                            private = preview.private,
                                            leadDoctorName = preview.leadDoctorName,
                                            joined = true
                                        )
                                        selectedDoctorArticle = null
                                        onOpenGroup(group)
                                    } else {
                                        val joinedPreview = withContext(Dispatchers.IO) {
                                            repository.join(privateId)
                                        }
                                        val group = CommunityGroup(
                                            id = joinedPreview.id,
                                            name = joinedPreview.name,
                                            description = joinedPreview.description,
                                            private = joinedPreview.private,
                                            leadDoctorName = joinedPreview.leadDoctorName,
                                            joined = true
                                        )
                                        selectedDoctorArticle = null
                                        onOpenGroup(group)
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        e.localizedMessage ?: "Không thể kết nối phòng tư vấn",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    groupActionLoadingId = null
                                }
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val specialtyId = doctorArticle.authorSpecialtyGroupId
                val hasSpecialty = specialtyId != null
                DoctorActionBlock(
                    title = "Cộng đồng chuyên khoa",
                    description = "Tham gia nhóm cộng đồng do bác sĩ phụ trách để cùng thảo luận và nhận tin y khoa.",
                    enabled = hasSpecialty,
                    loading = groupActionLoadingId == specialtyId && specialtyId != null,
                    onClick = {
                        if (specialtyId != null) {
                            groupActionLoadingId = specialtyId
                            scope.launch {
                                try {
                                    val preview = withContext(Dispatchers.IO) {
                                        repository.preview(specialtyId)
                                    }
                                    if (preview.joined) {
                                        val group = CommunityGroup(
                                            id = preview.id,
                                            name = preview.name,
                                            description = preview.description,
                                            private = preview.private,
                                            leadDoctorName = preview.leadDoctorName,
                                            joined = true
                                        )
                                        selectedDoctorArticle = null
                                        onOpenGroup(group)
                                    } else {
                                        val joinedPreview = withContext(Dispatchers.IO) {
                                            repository.join(specialtyId)
                                        }
                                        val group = CommunityGroup(
                                            id = joinedPreview.id,
                                            name = joinedPreview.name,
                                            description = joinedPreview.description,
                                            private = joinedPreview.private,
                                            leadDoctorName = joinedPreview.leadDoctorName,
                                            joined = true
                                        )
                                        selectedDoctorArticle = null
                                        onOpenGroup(group)
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        e.localizedMessage ?: "Không thể tham gia cộng đồng chuyên khoa",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    groupActionLoadingId = null
                                }
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun DoctorActionBlock(
    title: String,
    description: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && !loading, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0xFFF8FAFC) else Color(0xFFF1F5F9)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) Color(0xFFE2E8F0) else Color(0xFFE2E8F0).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) Color(0xFF0F172A) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (enabled) description else "Dịch vụ hiện chưa được bác sĩ thiết lập.",
                    fontSize = 12.sp,
                    color = if (enabled) Color(0xFF64748B) else Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryBlue, strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (enabled) "Liên kết" else "Chưa mở",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (enabled) PrimaryBlue else Color(0xFF94A3B8)
                )
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
    onDoctorClick: (Article) -> Unit,
) {
    val tags = article.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.take(4).orEmpty()
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDoctorClick(article) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
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

            if (article.imageUrl?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }

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
