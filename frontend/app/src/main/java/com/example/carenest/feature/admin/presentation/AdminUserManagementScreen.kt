package com.example.carenest.feature.admin.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.admin.data.AdminUserAuditLogItem
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse

private data class RoleDialogState(
    val user: AdminUserSummaryResponse,
    val targetRole: String
)

private data class StatusDialogState(
    val user: AdminUserSummaryResponse,
    val targetStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    showAuditLogs: Boolean = false,
    onDismissAuditLogs: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: AdminUserManagementViewModel = viewModel(
        factory = AdminUserManagementViewModelFactory(application.adminRepository)
    )
    val state by viewModel.uiState.collectAsState()
    val users = viewModel.users.collectAsLazyPagingItems()
    val roleDialogState = remember { mutableStateOf<RoleDialogState?>(null) }
    val statusDialogState = remember { mutableStateOf<StatusDialogState?>(null) }
    val currentUserId = application.secureSessionManager.getUserId()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(state.error, state.message) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientMessage()
        }
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.search,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            placeholder = {
                Text("Tìm theo email hoặc tên người dùng")
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFCBD5E1),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        when {
            users.loadState.refresh is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            users.loadState.refresh is LoadState.Error -> {
                val error = (users.loadState.refresh as LoadState.Error).error
                com.example.carenest.feature.admin.presentation.components.AdminErrorState(
                    message = error.localizedMessage ?: "Không thể tải danh sách người dùng",
                    onRetry = { users.retry() }
                )
            }

            users.itemCount == 0 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không tìm thấy người dùng phù hợp",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(count = users.itemCount) { index ->
                        val user = users[index] ?: return@items
                        val status = state.optimisticStatuses[user.id] ?: user.status
                        val role = state.optimisticRoles[user.id] ?: user.role
                        AdminUserRow(
                            user = user,
                            status = status,
                            role = role,
                            isCurrentAdmin = user.id == currentUserId,
                            isBusy = state.pendingUserIds.contains(user.id),
                            onToggleStatus = {
                                val nextStatus = if (status.equals("BANNED", ignoreCase = true)) "ACTIVE" else "BANNED"
                                statusDialogState.value = StatusDialogState(user, nextStatus)
                            },
                            onToggleAdminRole = {
                                val nextRole = if (role.equals("ADMIN", ignoreCase = true)) "USER" else "ADMIN"
                                roleDialogState.value = RoleDialogState(user, nextRole)
                            }
                        )
                    }

                    if (users.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    roleDialogState.value?.let { dialogState ->
        var reason by remember(dialogState.user.id, dialogState.targetRole) { mutableStateOf("") }
        val targetRole = dialogState.targetRole
        AlertDialog(
            onDismissRequest = { roleDialogState.value = null },
            title = {
                Text(
                    text = if (targetRole == "ADMIN") "Cấp quyền admin" else "Gỡ quyền admin",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (targetRole == "ADMIN") {
                            "Người dùng này sẽ có quyền truy cập khu vực quản trị. Chỉ cấp quyền cho tài khoản vận hành đáng tin cậy."
                        } else {
                            "Người dùng này sẽ mất quyền truy cập khu vực quản trị. Hệ thống vẫn chặn thao tác nếu đây là admin hoạt động cuối cùng."
                        },
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        label = { Text("Lý do thao tác") },
                        placeholder = {
                            Text(
                                if (targetRole == "ADMIN") {
                                    "Ví dụ: bổ sung người trực vận hành cuối tuần"
                                } else {
                                    "Ví dụ: bàn giao lại quyền quản trị"
                                }
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleAdminRole(dialogState.user, reason)
                        roleDialogState.value = null
                    },
                    enabled = reason.trim().isNotEmpty()
                ) {
                    Text(if (targetRole == "ADMIN") "Cấp quyền" else "Gỡ quyền")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleDialogState.value = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    statusDialogState.value?.let { dialogState ->
        var reason by remember(dialogState.user.id, dialogState.targetStatus) { mutableStateOf("") }
        val isUnlocking = dialogState.targetStatus.equals("ACTIVE", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { statusDialogState.value = null },
            title = {
                Text(
                    text = if (isUnlocking) "Mở lại tài khoản" else "Khóa tài khoản",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isUnlocking) {
                            "Người dùng sẽ có thể đăng nhập và sử dụng CareNest trở lại ngay sau khi xác nhận."
                        } else {
                            "Người dùng sẽ không thể đăng nhập hoặc sử dụng CareNest cho đến khi tài khoản được mở lại."
                        },
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        label = { Text("Lý do thao tác") },
                        placeholder = {
                            Text(
                                if (isUnlocking) {
                                    "Ví dụ: đã xử lý xong khiếu nại tài khoản"
                                } else {
                                    "Ví dụ: tài khoản vi phạm chính sách vận hành"
                                }
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserStatus(dialogState.user, reason)
                        statusDialogState.value = null
                    },
                    enabled = reason.trim().isNotEmpty()
                ) {
                    Text(if (isUnlocking) "Mở lại" else "Khóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { statusDialogState.value = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showAuditLogs) {
        ModalBottomSheet(
            onDismissRequest = onDismissAuditLogs,
            sheetState = sheetState,
            containerColor = Color(0xFFF8FAFC)
        ) {
            Box(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                UserAuditLogCard(
                    logs = state.auditLogs,
                    isLoading = state.isAuditLoading,
                    onRefresh = viewModel::refreshAuditLogs
                )
            }
        }
    }
}

@Composable
private fun UserAuditLogCard(
    logs: List<AdminUserAuditLogItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF2563EB)
                    )
                    Column {
                        Text(
                            text = "Nhật ký override gần đây",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Mọi thay đổi role hoặc trạng thái đều phải có lý do và xuất hiện tại đây.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
                TextButton(onClick = onRefresh, enabled = !isLoading) {
                    Text("Làm mới")
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }

                logs.isEmpty() -> {
                    Text(
                        text = "Chưa có lịch sử override nào trong phiên dữ liệu hiện tại.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        logs.forEach { log ->
                            AuditLogRow(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogRow(log: AdminUserAuditLogItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(log.action.adminAuditLabel()) },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = log.action.adminAuditColor().copy(alpha = 0.12f),
                        disabledLabelColor = log.action.adminAuditColor()
                    )
                )
                log.createdAt?.let { createdAt ->
                    Text(
                        text = createdAt,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = buildAuditHeadline(log),
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
            Text(
                text = log.targetUserEmail ?: "Không có email mục tiêu",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
            log.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = "Lý do: $reason",
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AdminUserRow(
    user: AdminUserSummaryResponse,
    status: String,
    role: String,
    isCurrentAdmin: Boolean,
    isBusy: Boolean,
    onToggleStatus: () -> Unit,
    onToggleAdminRole: () -> Unit
) {
    val isBanned = status.equals("BANNED", ignoreCase = true) || status.equals("INACTIVE", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.fullName?.take(1)?.uppercase() ?: user.email.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName?.takeIf { it.isNotBlank() } ?: "Người dùng CareNest",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    AssistChip(
                        onClick = onToggleAdminRole,
                        enabled = !isBusy && !isBanned && !isCurrentAdmin,
                        label = { Text(role) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (role.equals("ADMIN", ignoreCase = true)) Color(0xFFFFF7ED) else Color(0xFFEEF2FF),
                            labelColor = if (role.equals("ADMIN", ignoreCase = true)) Color(0xFFC2410C) else Color(0xFF3730A3)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(
                                color = if (isBanned) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBanned) "Đã khóa" else "Hoạt động",
                            color = if (isBanned) Color(0xFFB91C1C) else Color(0xFF047857),
                            fontSize = 14.sp
                        )
                    }
                    if (isCurrentAdmin) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Bạn", maxLines = 1) }
                        )
                    }
                }
            }
            IconButton(
                onClick = onToggleStatus,
                enabled = !isBusy && !isCurrentAdmin
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (isBanned) "Mở khóa người dùng" else "Khóa người dùng",
                        tint = if (isBanned) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                }
            }
        }
    }
}

private fun String?.adminAuditLabel(): String {
    return when (this) {
        "USER_BANNED" -> "Khóa tài khoản"
        "USER_REACTIVATED" -> "Mở lại tài khoản"
        "ADMIN_ROLE_GRANTED" -> "Cấp quyền admin"
        "ADMIN_ROLE_REVOKED" -> "Gỡ quyền admin"
        else -> "Override người dùng"
    }
}

private fun String?.adminAuditColor(): Color {
    return when (this) {
        "USER_BANNED", "ADMIN_ROLE_REVOKED" -> Color(0xFFDC2626)
        "USER_REACTIVATED" -> Color(0xFF059669)
        "ADMIN_ROLE_GRANTED" -> Color(0xFF2563EB)
        else -> Color(0xFF475569)
    }
}

private fun buildAuditHeadline(log: AdminUserAuditLogItem): String {
    val actor = log.actorName ?: "Quản trị viên"
    val target = log.targetUserName ?: "người dùng"
    return when (log.action) {
        "USER_BANNED" -> "$actor đã khóa tài khoản của $target."
        "USER_REACTIVATED" -> "$actor đã mở lại tài khoản của $target."
        "ADMIN_ROLE_GRANTED" -> "$actor đã cấp quyền admin cho $target."
        "ADMIN_ROLE_REVOKED" -> "$actor đã gỡ quyền admin của $target."
        else -> "$actor đã thực hiện một thao tác override với $target."
    }
}
