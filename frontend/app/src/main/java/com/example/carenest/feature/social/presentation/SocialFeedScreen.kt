package com.example.carenest.feature.social.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.core.presentation.theme.Error
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.presentation.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    viewModel: SocialFeedViewModel,
    onBack: () -> Unit,
    onCommentClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val posts = viewModel.postsFlow.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bảng tin nhóm",
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
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val refreshState = posts.loadState.refresh

            when {
                // Initial Load State: Loading
                refreshState is LoadState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PageBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Initial Load State: Error
                refreshState is LoadState.Error -> {
                    val errorMessage = refreshState.error.localizedMessage ?: "Đã xảy ra lỗi khi tải bảng tin."
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PageBackground)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Cảnh báo lỗi",
                            tint = Error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            style = CareNestTextStyles.bodyLg,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { posts.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = "Thử lại",
                                style = CareNestTextStyles.labelMd,
                                color = Color.White
                            )
                        }
                    }
                }

                // Initial Load State: Success
                else -> {
                    if (posts.itemCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PageBackground)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có bài viết nào trong nhóm này.",
                                style = CareNestTextStyles.bodyLg,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(count = posts.itemCount) { index ->
                                val post = posts[index]
                                if (post != null) {
                                    PostCard(
                                        post = post,
                                        onLikeClick = { clickedPost ->
                                            viewModel.reactToPost(clickedPost.id)
                                        },
                                        onCommentClick = onCommentClick
                                    )
                                }
                            }

                            // Append load state handling
                            val appendState = posts.loadState.append
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
                                                text = "Không thể tải thêm bài viết.",
                                                style = CareNestTextStyles.bodySm,
                                                color = Error,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(
                                                onClick = { posts.retry() }
                                            ) {
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
                                    // Do nothing for NotLoading / end of pagination
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
