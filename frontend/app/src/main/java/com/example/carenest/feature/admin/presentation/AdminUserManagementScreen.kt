package com.example.carenest.feature.admin.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse
import com.example.carenest.feature.admin.presentation.components.AdminErrorState
import com.example.carenest.feature.admin.presentation.components.AdminTransientBanner

@Composable
fun AdminUserManagementScreen() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: AdminUserManagementViewModel = viewModel(
        factory = AdminUserManagementViewModelFactory(application.adminRepository),
    )
    val state by viewModel.uiState.collectAsState()
    val users = viewModel.users.collectAsLazyPagingItems()
    val roleTarget = remember { mutableStateOf<AdminUserSummaryResponse?>(null) }
    val statusTarget = remember { mutableStateOf<AdminUserSummaryResponse?>(null) }
    val currentUserId = application.secureSessionManager.getUserId()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.message?.let { message ->
            AdminTransientBanner(
                message = message,
                isError = false,
                onDismiss = viewModel::clearTransientMessage,
            )
        }

        state.error?.let { error ->
            AdminTransientBanner(
                message = error,
                isError = true,
                onDismiss = viewModel::clearTransientMessage,
            )
        }

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
                unfocusedBorderColor = Color(0xFFE2E8F0),
            ),
        )

        when {
            users.loadState.refresh is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            users.loadState.refresh is LoadState.Error -> {
                val error = (users.loadState.refresh as LoadState.Error).error
                AdminErrorState(
                    message = error.message ?: "Không thể tải danh sách người dùng",
                    onRetry = { users.retry() },
                )
            }

            users.itemCount == 0 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không tìm thấy người dùng phù hợp",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                            onToggleStatus = { statusTarget.value = user },
                            onToggleAdminRole = { roleTarget.value = user },
                        )
                    }

                    if (users.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    roleTarget.value?.let { user ->
        val currentRole = state.optimisticRoles[user.id] ?: user.role
        val targetRole = if (currentRole.equals("ADMIN", ignoreCase = true)) "USER" else "ADMIN"
        AlertDialog(
            onDismissRequest = { roleTarget.value = null },
            title = {
                Text(
                    text = if (targetRole == "ADMIN") "Cấp quyền admin" else "Gỡ quyền admin",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (targetRole == "ADMIN") {
                        "Người dùng này sẽ có quyền truy cập khu vực quản trị. Chỉ cấp quyền cho tài khoản vận hành đáng tin cậy."
                    } else {
                        "Người dùng này sẽ mất quyền truy cập khu vực quản trị. Hệ thống sẽ từ chối nếu đây là admin hoạt động cuối cùng."
                    },
                    color = Color(0xFF475569),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleAdminRole(user)
                        roleTarget.value = null
                    },
                ) {
                    Text(if (targetRole == "ADMIN") "Cấp quyền" else "Gỡ quyền")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleTarget.value = null }) {
                    Text("Hủy")
                }
            },
        )
    }

    statusTarget.value?.let { user ->
        val currentStatus = state.optimisticStatuses[user.id] ?: user.status
        val isBanned = currentStatus.equals("BANNED", ignoreCase = true) ||
            currentStatus.equals("INACTIVE", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { statusTarget.value = null },
            title = {
                Text(
                    text = if (isBanned) "Mở lại tài khoản" else "Khóa tài khoản",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (isBanned) {
                        "Người dùng sẽ đăng nhập và sử dụng CareNest trở lại."
                    } else {
                        "Người dùng sẽ không thể đăng nhập hoặc sử dụng các luồng CareNest cho đến khi được mở lại. Hệ thống sẽ từ chối nếu đây là admin hoạt động cuối cùng."
                    },
                    color = Color(0xFF475569),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserStatus(user)
                        statusTarget.value = null
                    },
                    enabled = !state.pendingUserIds.contains(user.id),
                ) {
                    Text(if (isBanned) "Mở lại" else "Khóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { statusTarget.value = null }) {
                    Text("Hủy")
                }
            },
        )
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
    onToggleAdminRole: () -> Unit,
) {
    val isBanned = status.equals("BANNED", ignoreCase = true) || status.equals("INACTIVE", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
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
                    .background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.fullName?.take(1)?.uppercase() ?: user.email.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
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
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = onToggleAdminRole,
                        enabled = !isBusy && !isBanned && !isCurrentAdmin,
                        label = { Text(if (role.equals("ADMIN", true)) "Admin" else "User") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (role.equals("ADMIN", ignoreCase = true)) Color(0xFFFFF7ED) else Color(0xFFEEF2FF),
                            labelColor = if (role.equals("ADMIN", ignoreCase = true)) Color(0xFFC2410C) else Color(0xFF3730A3),
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isBanned) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = if (isBanned) "Đã khóa" else "Hoạt động",
                            color = if (isBanned) Color(0xFFB91C1C) else Color(0xFF047857),
                            fontSize = 14.sp,
                        )
                    }
                    if (isCurrentAdmin) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Bạn") },
                        )
                    }
                }
            }
            IconButton(
                onClick = onToggleStatus,
                enabled = !isBusy && !isCurrentAdmin,
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (isBanned) "Mở khóa người dùng" else "Khóa người dùng",
                        tint = if (isBanned) Color(0xFF047857) else Color(0xFFB91C1C),
                    )
                }
            }
        }
    }
}
