package com.example.carenest.feature.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.notifications.domain.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Locale

enum class NotificationOpenResult {
    OPENED,
    CONSUMED,
    UNHANDLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsCenterScreen(
    profileId: Long?,
    viewModel: NotificationsCenterViewModel,
    onBack: () -> Unit,
    onOpenNotification: (NotificationItem) -> NotificationOpenResult = { NotificationOpenResult.UNHANDLED }
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(profileId) {
        viewModel.loadNotifications(profileId)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trung tâm thông báo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadNotifications(profileId) },
                        enabled = !state.isActionLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = PrimaryBlue)
                    }
                    if (state.unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() },
                            enabled = !state.isActionLoading
                        ) {
                            Text(
                                text = if (state.isActionLoading) "Đang xử lý" else "Đọc tất cả",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.notifications.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.notifications.isEmpty() -> {
                    EmptyNotificationState(
                        error = state.error,
                        onRetry = { viewModel.loadNotifications(profileId) }
                    )
                }

                else -> {
                    NotificationsList(
                        state = state,
                        onNotificationClick = { notification ->
                            if (!state.isActionLoading) {
                                val openResult = onOpenNotification(notification)
                                if (
                                    openResult != NotificationOpenResult.UNHANDLED &&
                                    !notification.isRead &&
                                    notification.id > 0L
                                ) {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationState(
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (error == null) Icons.Default.NotificationsNone else Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (error == null) "Không có thông báo nào" else "Không thể tải thông báo",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error ?: "CareNest sẽ cập nhật tại đây khi có lịch hẹn, lời mời gia đình hoặc nhắc nhở mới.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Thử lại", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NotificationsList(
    state: NotificationsUiState,
    onNotificationClick: (NotificationItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        state.error?.let { error ->
            InlineStatusBanner(message = error, isError = true)
        }
        if (state.unreadCount > 0) {
            InlineStatusBanner(message = "Bạn có ${state.unreadCount} thông báo chưa đọc")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            state.groupedNotifications.forEach { (groupKey, groupItems) ->
                item {
                    Text(
                        text = groupTitle(groupKey),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                items(groupItems, key = { it.id }) { item ->
                    NotificationRow(
                        item = item,
                        onClick = { onNotificationClick(item) }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun InlineStatusBanner(
    message: String,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isError) Color(0xFFFFF1F2) else Color(0xFFEFF6FF))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = if (isError) Color(0xFFE11D48) else PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) Color(0xFFBE123C) else Color(0xFF1E40AF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val (icon, bgColor, iconColor) = getNotificationStyle(item.type)
    val rowBg = if (item.isRead) Color.White else Color(0xFFEFF6FF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = if (item.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                    color = if (item.isRead) Color(0xFF334155) else Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                fontSize = 13.sp,
                color = if (item.isRead) Color(0xFF64748B) else Color(0xFF475569),
                lineHeight = 18.sp,
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Medium
            )

            item.createdAt?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatIsoTime(it),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private fun groupTitle(groupKey: String): String {
    return when (groupKey) {
        "today" -> "HÔM NAY"
        "yesterday" -> "HÔM QUA"
        "this_week" -> "TUẦN NÀY"
        "older" -> "CŨ HƠN"
        else -> groupKey.uppercase()
    }
}

private fun getNotificationStyle(type: String): Triple<ImageVector, Color, Color> {
    return when (type.uppercase()) {
        "MEDICATION" -> Triple(Icons.Default.Medication, Color(0xFFE0F2FE), Color(0xFF0EA5E9))
        "APPOINTMENT" -> Triple(Icons.Default.CalendarMonth, Color(0xFFD1FAE5), Color(0xFF10B981))
        "VACCINATION" -> Triple(Icons.Default.Vaccines, Color(0xFFF3E8FF), Color(0xFF8B5CF6))
        "FAMILY" -> Triple(Icons.Default.Groups, Color(0xFFFFEDD5), Color(0xFFF97316))
        "CHAT" -> Triple(Icons.AutoMirrored.Filled.Chat, Color(0xFFE0E7FF), Color(0xFF4F46E5))
        "GROWTH" -> Triple(Icons.Default.MonitorHeart, Color(0xFFFCE7F3), Color(0xFFEC4899))
        else -> Triple(Icons.Default.Notifications, Color(0xFFF1F5F9), Color(0xFF64748B))
    }
}

private fun formatIsoTime(isoStr: String): String {
    val date = parseIsoDate(isoStr) ?: return isoStr
    return SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(date)
}
