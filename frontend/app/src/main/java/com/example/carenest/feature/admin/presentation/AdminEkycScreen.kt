package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.admin.presentation.components.AdminTransientBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEkycScreen() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: AdminEkycViewModel = viewModel(
        factory = AdminEkycViewModelFactory(application.ekycRepository),
    )
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectTargetId by remember { mutableStateOf<Long?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        state.message?.let { message ->
            AdminTransientBanner(
                message = message,
                isError = false,
                onDismiss = viewModel::clearTransientMessage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        state.error?.let { error ->
            AdminTransientBanner(
                message = error,
                isError = true,
                onDismiss = viewModel::clearTransientMessage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1E3A8A),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF1E3A8A),
                )
            },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    viewModel.loadPending()
                },
                text = { Text("Chờ duyệt", fontWeight = FontWeight.Bold) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    viewModel.loadDoctors()
                },
                text = { Text("Bác sĩ đã xác thực", fontWeight = FontWeight.Bold) },
            )
        }

        when (selectedTab) {
            0 -> {
                when {
                    state.isLoadingPending && state.pendingList.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.pendingList.isEmpty() -> {
                        EmptyListPlaceholder("Không có hồ sơ nào đang chờ duyệt.")
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(state.pendingList, key = { it.id }) { item ->
                                PendingVerificationCard(
                                    item = item,
                                    onApprove = { viewModel.approveVerification(item.id) },
                                    onReject = {
                                        rejectTargetId = item.id
                                        rejectionReason = ""
                                        showRejectDialog = true
                                    },
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                when {
                    state.isLoadingDoctors && state.doctorList.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.doctorList.isEmpty() -> {
                        EmptyListPlaceholder("Chưa có bác sĩ nào đã được xác thực.")
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(state.doctorList, key = { it.id }) { doctor ->
                                DoctorSummaryCard(
                                    item = doctor,
                                    onRevoke = { viewModel.revokeDoctor(doctor.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Lý do từ chối", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Nhập lý do từ chối để gửi lại cho bác sĩ.",
                        color = Color(0xFF64748B),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ví dụ: ảnh chứng chỉ mờ hoặc thiếu thông tin.") },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejectTargetId?.let { id ->
                            viewModel.rejectVerification(id, rejectionReason)
                        }
                        showRejectDialog = false
                    },
                    enabled = rejectionReason.isNotBlank(),
                ) {
                    Text("Gửi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy")
                }
            },
        )
    }
}
