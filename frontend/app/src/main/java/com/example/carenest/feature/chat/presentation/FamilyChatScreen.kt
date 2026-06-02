package com.example.carenest.feature.chat.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.components.MessageBubble

private val ChatBlueSelf = Color(0xFF1A73E8)
private val ChatBackIcon = Color(0xFF0369A1)
private val HeaderSubText = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyChatScreen(
    familyId: Long,
    familyName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: FamilyChatViewModel = viewModel(
        key = "family-chat-$familyId",
        factory = FamilyChatViewModelFactory(
            familyId = familyId,
            repository = application.familyChatRepository,
            familyRepository = application.familyRepository
        )
    )
    val state by viewModel.uiState.collectAsState()
    val canSend = state.inputText.isNotBlank() && state.isConnected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // --- Custom Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = ChatBackIcon
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Family Icon Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = ChatBackIcon,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = familyName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = buildString {
                        append(if (state.isConnected) "🟢 " else "🔴 ")
                        val count = state.memberCount
                        if (count != null && count > 0) {
                            append("$count Thành viên")
                        } else {
                            append("Chat nhóm gia đình")
                        }
                    },
                    fontSize = 12.sp,
                    color = HeaderSubText
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // --- Main Chat Content Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC)) // Match ChatRoomV2 style or PageBackground
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Đang tải tin nhắn...",
                            color = HeaderSubText,
                            fontSize = 14.sp
                        )
                    }
                }

                state.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có tin nhắn nào",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hãy gửi lời chào đầu tiên đến tổ ấm của bạn!",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                else -> {
                    val lazyListState = rememberLazyListState()
                    
                    // Infinite scroll listener
                    val shouldLoadEarlier = remember {
                        derivedStateOf {
                            val layoutInfo = lazyListState.layoutInfo
                            val totalItemsNumber = layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            lastVisibleItemIndex >= totalItemsNumber - 5
                        }
                    }

                    LaunchedEffect(shouldLoadEarlier.value) {
                        if (shouldLoadEarlier.value) {
                            viewModel.loadEarlierMessages()
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(msg = message)
                        }
                    }
                }
            }
        }

        // --- Bottom Input Bar ---
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                state.error?.takeIf { it.isNotBlank() }?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = state.inputText,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp, max = 112.dp),
                        placeholder = {
                            Text(
                                text = "Nhập tin nhắn...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(23.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (canSend) ChatBlueSelf else Color(0xFFCBD5E1))
                            .clickable(enabled = canSend) {
                                viewModel.sendMessage()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
