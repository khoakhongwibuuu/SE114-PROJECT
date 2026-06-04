package com.example.carenest.feature.social.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.core.presentation.theme.Error as ThemeError
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.presentation.components.CommentInputBar
import com.example.carenest.feature.social.presentation.components.CommentItem
import com.example.carenest.feature.social.presentation.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    viewModel: PostDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comments = viewModel.commentsFlow.collectAsLazyPagingItems()
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết bài viết",
                        style = CareNestTextStyles.titleLg,
                        color = PrimaryBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            CommentInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                replyingToName = replyingToComment?.authorName,
                onDismissReply = { replyingToComment = null },
                onSendClick = {
                    val parentId = replyingToComment?.id
                    if (parentId != null) {
                        Toast.makeText(context, "Đã gửi câu trả lời (Mock): $inputText", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Đã gửi bình luận (Mock): $inputText", Toast.LENGTH_SHORT).show()
                    }
                    inputText = ""
                    replyingToComment = null
                }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val refreshState = comments.loadState.refresh

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Item 1: Original Post Card
                item {
                    PostCard(
                        post = post,
                        onLikeClick = {
                            // Can call reactToPost or trigger callback
                            Toast.makeText(context, "Thích bài viết", Toast.LENGTH_SHORT).show()
                        },
                        onCommentClick = {
                            // Focus comment input
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Item 2: Horizontal Divider
                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Section Title: Comments
                item {
                    Text(
                        text = "Bình luận (${post.commentCount})",
                        style = CareNestTextStyles.labelMd,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }

                // Comments List & States
                when {
                    refreshState is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Lỗi",
                                    tint = ThemeError,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = refreshState.error.localizedMessage ?: "Lỗi tải bình luận.",
                                    style = CareNestTextStyles.bodyMd,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { comments.retry() }) {
                                    Text(
                                        text = "Thử lại",
                                        style = CareNestTextStyles.labelMd,
                                        color = PrimaryBlue
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
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có bình luận nào. Hãy là người đầu tiên bình luận!",
                                    style = CareNestTextStyles.bodyMd,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
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
                                    }
                                )
                            }
                        }

                        // Append load states
                        val appendState = comments.loadState.append
                        when (appendState) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = PrimaryBlue,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Không thể tải thêm bình luận.",
                                            style = CareNestTextStyles.bodySm,
                                            color = ThemeError,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { comments.retry() }) {
                                            Text(
                                                text = "Thử lại",
                                                style = CareNestTextStyles.labelSm,
                                                color = PrimaryBlue
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                // Do nothing
                            }
                        }
                    }
                }
            }
        }
    }
}
