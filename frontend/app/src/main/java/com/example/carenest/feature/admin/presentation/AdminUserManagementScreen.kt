package com.example.carenest.feature.admin.presentation

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun AdminUserManagementScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: AdminUserManagementViewModel = viewModel(
        factory = AdminUserManagementViewModelFactory(application.adminRepository),
    )
    val state by viewModel.uiState.collectAsState()
    val users = viewModel.users.collectAsLazyPagingItems()

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = error.localizedMessage ?: "Không thể tải danh sách người dùng",
                        color = Color(0xFFB91C1C),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
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
                        AdminUserRow(
                            user = user,
                            status = status,
                            onToggleStatus = { viewModel.toggleUserStatus(user) },
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
}

@Composable
private fun AdminUserRow(
    user: AdminUserSummaryResponse,
    status: String,
    onToggleStatus: () -> Unit,
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
                        onClick = {},
                        label = { Text(user.role) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFEEF2FF),
                            labelColor = Color(0xFF3730A3),
                        ),
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(if (isBanned) "BANNED" else "ACTIVE") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isBanned) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                            labelColor = if (isBanned) Color(0xFFB91C1C) else Color(0xFF047857),
                        ),
                    )
                }
            }
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (isBanned) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = if (isBanned) "Unban user" else "Ban user",
                    tint = if (isBanned) Color(0xFF047857) else Color(0xFFB91C1C),
                )
            }
        }
    }
}
