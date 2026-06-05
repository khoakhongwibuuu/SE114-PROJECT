package com.example.carenest.feature.chat.presentation

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.chat.presentation.components.MessageBubble

private val ChatBlue = Color(0xFF1A73E8)
private val ChatBackIcon = Color(0xFF0369A1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    groupId: Long,
    groupName: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: ChatViewModel = viewModel(
        key = "chat-$groupId",
        factory = ChatViewModelFactory(groupId = groupId, repository = application.chatRepository),
    )
    val state by viewModel.uiState.collectAsState()
    val currentRole by application.secureSessionManager.userRoleFlow.collectAsState()
    val canSend = state.inputText.isNotBlank() && state.slowCountdown == 0 && !state.isSending
    val canModerate = currentRole == "DOCTOR" || currentRole == "ADMIN"
    val isFallbackMode = !state.isConnected && state.error?.contains("Đã lưu", ignoreCase = true) == true
    var showOptions by remember { mutableStateOf(false) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var showReportDialog by remember { mutableStateOf<ChatMessage?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var showKickConfirmation by remember { mutableStateOf<ChatMessage?>(null) }

    if (showOptions) {
        ModalBottomSheet(
            onDismissRequest = { showOptions = false },
            containerColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tùy chọn của phòng trò chuyện",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.height(18.dp))
                ChatOptionRow(
                    icon = {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                        )
                    },
                    title = "Tắt thông báo",
                    subtitle = "Tính năng này sẽ được hoàn thiện ở lượt tiếp theo.",
                    onClick = {
                        showOptions = false
                        Toast.makeText(context, "Đã lưu tùy chọn tắt thông báo.", Toast.LENGTH_SHORT).show()
                    },
                )
                ChatOptionRow(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                        )
                    },
                    title = "Rời nhóm",
                    subtitle = "Rời khỏi phòng trò chuyện cộng đồng này.",
                    titleColor = Color(0xFFDC2626),
                    onClick = {
                        showOptions = false
                        viewModel.leaveGroup(
                            onSuccess = {
                                Toast.makeText(context, "Đã rời nhóm thành công.", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    val msgOptions = selectedMessageForOptions
    if (msgOptions != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForOptions = null },
            containerColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Tin nhắn của ${msgOptions.senderName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(18.dp))
                ChatOptionRow(
                    icon = {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                        )
                    },
                    title = "Báo cáo tin nhắn",
                    subtitle = "Báo cáo tin nhắn này vì vi phạm quy chuẩn cộng đồng.",
                    onClick = {
                        selectedMessageForOptions = null
                        showReportDialog = msgOptions
                    },
                )
                if (canModerate && !msgOptions.isMe && msgOptions.senderId != null) {
                    ChatOptionRow(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                            )
                        },
                        title = "Mời khỏi nhóm",
                        subtitle = "Mời thành viên này rời khỏi nhóm trò chuyện.",
                        titleColor = Color(0xFFDC2626),
                        onClick = {
                            selectedMessageForOptions = null
                            showKickConfirmation = msgOptions
                        },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    val reportMsg = showReportDialog
    if (reportMsg != null) {
        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            title = { Text("Báo cáo tin nhắn", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nhập lý do báo cáo tin nhắn này:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Lý do vi phạm...") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reason = reportReason.trim().ifBlank { "Vi phạm quy chuẩn" }
                        showReportDialog = null
                        reportReason = ""
                        viewModel.reportPost(
                            postId = reportMsg.id.toLongOrNull() ?: 0L,
                            reason = reason,
                            onSuccess = {
                                Toast.makeText(context, "Đã gửi báo cáo tin nhắn thành công.", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Gửi báo cáo", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = null }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            }
        )
    }

    val kickMsg = showKickConfirmation
    if (kickMsg != null) {
        AlertDialog(
            onDismissRequest = { showKickConfirmation = null },
            title = { Text("Mời khỏi nhóm", fontWeight = FontWeight.Bold) },
            text = {
                Text("Bạn có chắc chắn muốn mời thành viên ${kickMsg.senderName} rời khỏi nhóm trò chuyện này không?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showKickConfirmation = null
                        val userId = kickMsg.senderId
                        if (userId != null) {
                            viewModel.kickMember(
                                userId = userId,
                                onSuccess = {
                                    Toast.makeText(context, "Đã mời thành viên rời nhóm thành công.", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Không thể xác định ID thành viên.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Mời rời nhóm", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickConfirmation = null }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF4F8))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = ChatBackIcon,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = groupName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                )
                Text(
                    text = when {
                        state.memberCount != null -> "${state.memberCount} thành viên"
                        isFallbackMode -> "Chế độ dự phòng"
                        state.isLoading -> "Đang tải tin nhắn..."
                        else -> "Phòng trò chuyện cộng đồng"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        state.isConnected -> Color(0xFF94A3B8)
                        isFallbackMode -> Color(0xFF0F766E)
                        else -> Color(0xFF0F766E)
                    },
                )
            }

            IconButton(onClick = { showOptions = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn nhóm",
                    tint = Color(0xFF0F172A),
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFBEB))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nội dung trong phòng chat chỉ mang tính tham khảo, không thay thế tư vấn, chẩn đoán hoặc điều trị y khoa trực tiếp.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp,
                color = Color(0xFF92400E),
            )
        }

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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(42.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có tin nhắn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hãy bắt đầu cuộc trò chuyện đầu tiên trong nhóm.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(
                            msg = message,
                            onLongClick = { selectedMessageForOptions = it }
                        )
                    }
                }
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .windowInsetsPadding(WindowInsets.ime),
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val errorMsg = state.error
                if (!errorMsg.isNullOrBlank()) {
                    val isFallbackSuccess = errorMsg.contains("Đã lưu", ignoreCase = true)
                    val textColor = if (isFallbackSuccess) Color(0xFF0F766E) else Color(0xFFDC2626)
                    Text(
                        text = errorMsg,
                        color = textColor,
                        fontSize = 12.sp,
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
                        value = state.inputText,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp, max = 112.dp),
                        placeholder = {
                            Text(
                                text = when {
                                    state.slowCountdown > 0 -> "Chờ ${state.slowCountdown}s để gửi tiếp..."
                                    state.isSending -> "Đang gửi..."
                                    else -> "Nhập tin nhắn..."
                                },
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp,
                            )
                        },
                        enabled = !state.isSending,
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
                            .background(if (canSend) ChatBlue else Color(0xFFCBD5E1)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = viewModel::sendMessage, enabled = canSend) {
                            if (state.slowCountdown > 0) {
                                Text(
                                    text = state.slowCountdown.toString(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gửi",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: Color = Color(0xFF0F172A),
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

