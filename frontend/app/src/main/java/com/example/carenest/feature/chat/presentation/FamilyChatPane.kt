package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.components.MessageBubble
import com.example.carenest.feature.family.domain.model.FamilySummary

private val FamilyChatBlue = Color(0xFF1A73E8)

@Composable
fun FamilyChatDirectoryPane(
    families: List<FamilySummary>,
    activeFamilyId: Long?,
    onOpenMembersTab: () -> Unit,
    onSelectFamily: (FamilySummary) -> Unit,
) {
    if (families.isEmpty()) {
        FamilyChatEmptyState(
            icon = Icons.Default.Home,
            title = "Chưa có gia đình",
            description = "Hãy tạo hoặc tham gia gia đình trước để bắt đầu trò chuyện.",
            actionLabel = "Mở tab Thành viên",
            onAction = onOpenMembersTab,
        )
        return
    }

    FamilyChatFamilyList(
        families = families,
        activeFamilyId = activeFamilyId,
        onSelectFamily = onSelectFamily,
    )
}

@Composable
fun FamilyChatPane(
    familyId: Long,
    familyName: String,
    memberCount: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: FamilyChatViewModel = viewModel(
        factory = FamilyChatViewModelFactory(application.familyChatRepository),
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(familyId) {
        viewModel.bindFamily(familyId)
    }

    DisposableEffect(familyId) {
        onDispose { viewModel.unbind() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFEEF4F8)),
    ) {
        FamilyChatHeader(
            familyName = familyName,
            memberCount = memberCount,
            onBack = onBack,
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    FamilyChatEmptyState(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        title = "Chưa có tin nhắn nào",
                        description = "Hãy gửi lời chào đầu tiên đến tổ ấm của bạn.",
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(msg = message)
                    }
                    if (state.hasMore || state.isLoadingMore) {
                        item(key = "load-more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryBlue,
                                    )
                                } else {
                                    TextButton(onClick = viewModel::loadMore) {
                                        Text("Tải thêm tin nhắn cũ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FamilyChatComposer(
            inputText = state.inputText,
            connectionHint = state.connectionHint,
            error = state.error,
            isConnected = state.isConnected,
            isSending = state.isSending,
            canSend = state.inputText.trim().isNotBlank() && state.isConnected && !state.isSending,
            onInputChange = viewModel::onInputChange,
            onSend = viewModel::sendMessage,
        )
    }
}

@Composable
private fun FamilyChatFamilyList(
    families: List<FamilySummary>,
    activeFamilyId: Long?,
    onSelectFamily: (FamilySummary) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF4F8)),
    ) {
        Surface(color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "Chọn phòng trò chuyện gia đình",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mỗi gia đình có một phòng trò chuyện riêng cho cả nhà.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(families, key = { it.id }) { family ->
                FamilyChatFamilyCard(
                    family = family,
                    isActive = family.id == activeFamilyId,
                    onClick = { onSelectFamily(family) },
                )
            }
        }
    }
}

@Composable
private fun FamilyChatFamilyCard(
    family: FamilySummary,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = CardBackground,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFF0369A1),
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = family.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang chọn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${family.memberCount} thành viên • ${family.ownerName}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                )
            }
            Text(
                text = "Mở",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
            )
        }
    }
}

@Composable
private fun FamilyChatHeader(
    familyName: String,
    memberCount: Int,
    onBack: () -> Unit,
) {
    Surface(color = Color.White) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại danh sách gia đình",
                        tint = Color(0xFF0369A1),
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = familyName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                    )
                    Text(
                        text = "$memberCount thành viên",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
        }
    }
}

@Composable
private fun FamilyChatComposer(
    inputText: String,
    connectionHint: String?,
    error: String?,
    isConnected: Boolean,
    isSending: Boolean,
    canSend: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .windowInsetsPadding(WindowInsets.ime),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val visibleHint = when {
                !error.isNullOrBlank() -> error
                !isConnected && !connectionHint.isNullOrBlank() -> connectionHint
                else -> null
            }
            if (!visibleHint.isNullOrBlank()) {
                Text(
                    text = visibleHint,
                    fontSize = 12.sp,
                    color = if (!error.isNullOrBlank()) Color(0xFFDC2626) else Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 112.dp),
                    placeholder = {
                        Text(
                            text = if (isSending) "Đang gửi..." else "Nhập tin nhắn gia đình...",
                            color = Color(0xFF94A3B8),
                        )
                    },
                    enabled = !isSending,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) FamilyChatBlue else Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = onSend,
                        enabled = canSend,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi tin nhắn",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyChatEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue.copy(alpha = 0.5f),
            modifier = Modifier.size(58.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
        )
        val safeActionLabel = actionLabel
        if (!safeActionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction) {
                Text(text = safeActionLabel)
            }
        }
    }
}
