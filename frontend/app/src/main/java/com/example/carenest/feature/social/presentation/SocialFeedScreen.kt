package com.example.carenest.feature.social.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Error
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "B\u1ea3ng tin nh\u00f3m",
                        style = CareNestTextStyles.titleLg,
                        color = PrimaryBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay l\u1ea1i",
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

                refreshState is LoadState.Error -> {
                    val errorMessage = refreshState.error.localizedMessage ?: "\u0110\u00e3 x\u1ea3y ra l\u1ed7i khi t\u1ea3i b\u1ea3ng tin."
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
                            contentDescription = "C\u1ea3nh b\u00e1o l\u1ed7i",
                            tint = Error,
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = errorMessage,
                            style = CareNestTextStyles.bodyLg,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(24.dp))
                        Button(
                            onClick = { posts.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = "Th\u1eed l\u1ea1i",
                                style = CareNestTextStyles.labelMd,
                                color = Color.White
                            )
                        }
                    }
                }

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
                                text = "Ch\u01b0a c\u00f3 b\u00e0i vi\u1ebft n\u00e0o trong nh\u00f3m n\u00e0y.",
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
                                            scope.launch {
                                                val result = viewModel.reactToPost(clickedPost.id)
                                                if (result.isSuccess) {
                                                    posts.refresh()
                                                } else {
                                                    Toast.makeText(context, "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt l\u01b0\u1ee3t th\u00edch", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onCommentClick = onCommentClick
                                    )
                                }
                            }

                            when (val appendState = posts.loadState.append) {
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
                                                text = "Kh\u00f4ng th\u1ec3 t\u1ea3i th\u00eam b\u00e0i vi\u1ebft.",
                                                style = CareNestTextStyles.bodySm,
                                                color = Error,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { posts.retry() }) {
                                                Text(
                                                    text = "Th\u1eed l\u1ea1i",
                                                    style = CareNestTextStyles.labelSm,
                                                    color = PrimaryBlue
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
}
