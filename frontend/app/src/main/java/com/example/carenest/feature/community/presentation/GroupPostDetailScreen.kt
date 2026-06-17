package com.example.carenest.feature.community.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.GroupPost
import com.example.carenest.feature.community.domain.model.GroupPostComment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPostDetailScreen(
    groupId: Long,
    groupName: String,
    onBack: () -> Unit = {},
    onNavigateToCreatePost: (Long) -> Unit = {},
    onNavigateToManageGroup: (Long, String) -> Unit = { _, _ -> },
    onNavigateToDoctorProfile: (Long) -> Unit = {},
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: GroupPostDetailViewModel = viewModel(
        key = "group-post-$groupId",
        factory = GroupPostDetailViewModel.provideFactory(
            groupId = groupId,
            communityRepository = application.communityRepository,
            secureSessionManager = application.secureSessionManager,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val activeTab = state.activeTab

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var reportTarget by remember { mutableStateOf<GroupPost?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf<GroupPost?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editTags by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<GroupPost?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPosts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.isCommentSheetVisible) {
        if (!state.isCommentSheetVisible) {
            commentText = ""
        }
    }

    LaunchedEffect(state.error, state.message) {
        state.error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientMessage()
        }
        state.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Trở về")
            }
            Text(
                text = groupName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
            )
            IconButton(onClick = { onNavigateToManageGroup(groupId, groupName) }) {
                Icon(imageVector = Icons.Default.Groups, contentDescription = "Quản trị nhóm")
            }
        }

        Row(modifier = Modifier.fillMaxWidth().background(CardBackground)) {
            val tabs = if (state.isModerator) {
                GroupPostTab.entries
            } else {
                listOf(GroupPostTab.APPROVED, GroupPostTab.MY_POSTS)
            }

            tabs.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setTab(tab) }
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        style = CareNestTextStyles.labelMd,
                        color = if (activeTab == tab) PrimaryBlue else Outline,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (activeTab == tab) PrimaryBlue else Color.Transparent),
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                GroupPostTab.APPROVED -> ApprovedPostsPane(
                    posts = state.approvedPosts,
                    isLoading = state.isLoading,
                    error = state.error,
                    canModerate = state.isModerator,
                    onNavigateToCreatePost = { onNavigateToCreatePost(groupId) },
                    onLikeClick = { viewModel.toggleLike(it) },
                    onCommentClick = { viewModel.openCommentSheet(it) },
                    onDoctorClick = onNavigateToDoctorProfile,
                    onReportClick = { post ->
                        reportTarget = post
                        reportReason = ""
                    },
                    onDeleteClick = { post -> deleteTarget = post },
                    onRetry = viewModel::loadPosts,
                )

                GroupPostTab.MY_POSTS -> MyGroupPostsPane(
                    posts = state.myPosts,
                    isLoading = state.isLoading,
                    error = state.error,
                    onNavigateToCreatePost = { onNavigateToCreatePost(groupId) },
                    onLikeClick = { viewModel.toggleLike(it) },
                    onCommentClick = { viewModel.openCommentSheet(it) },
                    onDoctorClick = onNavigateToDoctorProfile,
                    onEditClick = { post ->
                        editTarget = post
                        editTitle = post.title ?: post.content.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
                        editContent = post.content
                        editTags = post.tags.orEmpty()
                    },
                    onDeleteClick = { post -> deleteTarget = post },
                    onRetry = viewModel::loadPosts,
                )

                GroupPostTab.PENDING -> ModerationQueuePane(
                    posts = state.pendingPosts,
                    isLoading = state.isLoading,
                    error = state.error,
                    onApprove = { viewModel.approvePost(it) },
                    onReject = { id, reason -> viewModel.rejectPost(id, reason) },
                    onRetry = viewModel::loadPosts,
                )
            }
        }
    }

    if (state.isCommentSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeCommentSheet() },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Bình luận",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (state.isCommentsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (!state.commentError.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.commentError.orEmpty(),
                            color = Color(0xFFDC2626),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                state.selectedPostIdForComments?.let(viewModel::openCommentSheet)
                            },
                        ) {
                            Text("Tải lại bình luận", color = PrimaryBlue)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).height(300.dp),
                    ) {
                        items(state.commentsList, key = { it.id }) { comment ->
                            GroupPostCommentItem(
                                comment = comment,
                                onDoctorClick = onNavigateToDoctorProfile,
                            )
                        }
                        if (state.commentsList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    Text("Chưa có bình luận nào.", color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Viết bình luận...") },
                        maxLines = 3,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.submitComment(commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = commentText.isNotBlank(),
                    ) {
                        Text("Gửi")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    reportTarget?.let { post ->
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("Báo cáo bài viết", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Nhập lý do để quản trị viên xem xét bài viết này.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lý do") },
                        maxLines = 4,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportPost(post.id, reportReason)
                        reportTarget = null
                        reportReason = ""
                    },
                    enabled = reportReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                ) {
                    Text("Gửi báo cáo")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) {
                    Text("Hủy")
                }
            },
        )
    }

    editTarget?.let { post ->
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Chỉnh sửa bài viết", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tiêu đề") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nội dung") },
                        minLines = 4,
                        maxLines = 8,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editTags,
                        onValueChange = { editTags = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tags") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePost(post.id, editTitle, editContent, editTags)
                        editTarget = null
                    },
                    enabled = editTitle.isNotBlank() && editContent.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                ) {
                    Text("Gửi lại duyệt")
                }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) {
                    Text("Hủy")
                }
            },
        )
    }

    deleteTarget?.let { post ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xóa bài viết?", fontWeight = FontWeight.Bold) },
            text = { Text("Bài viết sẽ bị xóa khỏi nhóm. Thao tác này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(post.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
fun GroupPostCommentItem(
    comment: GroupPostComment,
    onDoctorClick: ((Long) -> Unit)? = null,
) {
    val initials = (comment.authorName ?: "?").take(1).uppercase()
    val isDoctor = comment.authorRole == "DOCTOR"
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.then(
                    if (isDoctor && comment.authorId != null && onDoctorClick != null) {
                        Modifier.clickable { onDoctorClick(comment.authorId) }
                    } else {
                        Modifier
                    },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.authorName ?: "Thành viên",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                )
                if (isDoctor) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp),
            ) {
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                )
            }
        }
    }
}
