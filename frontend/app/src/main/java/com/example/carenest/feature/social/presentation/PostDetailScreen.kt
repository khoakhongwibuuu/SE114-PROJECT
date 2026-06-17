package com.example.carenest.feature.social.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Error as ThemeError
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.presentation.components.CommentInputBar
import com.example.carenest.feature.social.presentation.components.CommentItem
import com.example.carenest.feature.social.presentation.components.PostCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    viewModel: PostDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val comments = viewModel.commentsFlow.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var inputText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }
    var localLikeCount by remember(post.id) { mutableStateOf(post.likeCount) }
    var localIsLiked by remember(post.id) { mutableStateOf(post.likedByMe) }
    var localCommentCount by remember(post.id) { mutableStateOf(post.commentCount) }

    val mutationState by viewModel.mutationState.collectAsState()

    LaunchedEffect(mutationState) {
        when (mutationState) {
            is CommentMutationState.Success -> {
                snackbarHostState.showSnackbar("Đã gửi bình luận thành công")
                inputText = ""
                replyingToComment = null
                localCommentCount += 1
                comments.refresh()
                viewModel.clearMutationState()
            }

            is CommentMutationState.Error -> {
                snackbarHostState.showSnackbar((mutationState as CommentMutationState.Error).message)
                viewModel.clearMutationState()
            }

            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết bài viết",
                        style = CareNestTextStyles.titleLg,
                        color = PrimaryBlue,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = PrimaryBlue,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            CommentInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                replyingToName = replyingToComment?.authorName,
                onDismissReply = { replyingToComment = null },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        val parent = replyingToComment
                        if (parent != null) {
                            viewModel.createReply(parentCommentId = parent.id, content = inputText)
                        } else {
                            viewModel.createComment(content = inputText)
                        }
                    }
                },
            )
        },
        containerColor = PageBackground,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val refreshState = comments.loadState.refresh

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    PostCard(
                        post = post.copy(likeCount = localLikeCount, commentCount = localCommentCount),
                        isLiked = localIsLiked,
                        onLikeClick = {
                            scope.launch {
                                val wasLiked = localIsLiked
                                val originalLikeCount = localLikeCount

                                if (localIsLiked) {
                                    localLikeCount = (localLikeCount - 1).coerceAtLeast(0)
                                    localIsLiked = false
                                } else {
                                    localLikeCount += 1
                                    localIsLiked = true
                                }

                                val result = viewModel.reactToPost()
                                if (result.isFailure) {
                                    localIsLiked = wasLiked
                                    localLikeCount = originalLikeCount
                                    snackbarHostState.showSnackbar(
                                        result.exceptionOrNull()
                                            ?.userMessage("Không thể cập nhật lượt thích")
                                            ?: "Không thể cập nhật lượt thích",
                                    )
                                }
                            }
                        },
                        onCommentClick = {},
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }

                item {
                    Text(
                        text = "Bình luận ($localCommentCount)",
                        style = CareNestTextStyles.labelMd,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                    )
                }

                when {
                    refreshState is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                    }

                    refreshState is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Lỗi",
                                    tint = ThemeError,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = refreshState.error.userMessage("Không thể tải bình luận"),
                                    style = CareNestTextStyles.bodyMd,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { comments.retry() }) {
                                    Text(
                                        text = "Thử lại",
                                        style = CareNestTextStyles.labelMd,
                                        color = PrimaryBlue,
                                    )
                                }
                            }
                        }
                    }

                    comments.itemCount == 0 -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Chưa có bình luận nào. Hãy là người đầu tiên chia sẻ ý kiến.",
                                    style = CareNestTextStyles.bodyMd,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    else -> {
                        items(count = comments.itemCount) { index ->
                            val comment = comments[index]
                            if (comment != null) {
                                CommentItem(
                                    comment = comment,
                                    onReplyClick = { clickedComment ->
                                        replyingToComment = clickedComment
                                    },
                                )
                            }
                        }

                        when (comments.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            color = PrimaryBlue,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }

                            is LoadState.Error -> {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = "Không thể tải thêm bình luận.",
                                            style = CareNestTextStyles.bodySm,
                                            color = ThemeError,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(onClick = { comments.retry() }) {
                                            Text(
                                                text = "Thử lại",
                                                style = CareNestTextStyles.labelSm,
                                                color = PrimaryBlue,
                                            )
                                        }
                                    }
                                }
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
