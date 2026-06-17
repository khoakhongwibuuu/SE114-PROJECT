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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.admin.data.AdminContentType
import com.example.carenest.feature.admin.data.AdminReportSummaryResponse
import com.example.carenest.feature.admin.presentation.components.AdminErrorState
import com.example.carenest.feature.admin.presentation.components.AdminTransientBanner

@Composable
fun AdminModerationScreen() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: AdminModerationViewModel = viewModel(
        factory = AdminModerationViewModelFactory(application.adminRepository),
    )
    val state by viewModel.uiState.collectAsState()
    val reports = viewModel.reports.collectAsLazyPagingItems()
    var deleteTarget by remember { mutableStateOf<AdminReportSummaryResponse?>(null) }
    val visibleReportCount = (0 until reports.itemCount).count { index ->
        reports.peek(index)?.id !in state.hiddenReportIds
    }

    when {
        reports.loadState.refresh is LoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        reports.loadState.refresh is LoadState.Error -> {
            val error = (reports.loadState.refresh as LoadState.Error).error
            AdminErrorState(
                message = error.userMessage("Không thể tải danh sách báo cáo"),
                onRetry = { reports.retry() },
            )
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(reports.itemCount) { index ->
                        val report = reports[index] ?: return@items
                        if (report.id in state.hiddenReportIds) return@items
                        ReportCard(
                            report = report,
                            onDelete = { deleteTarget = report },
                            onDismiss = { viewModel.resolveReport(report, ModerationAction.DISMISS) },
                        )
                    }

                    if (visibleReportCount == 0 && reports.loadState.append !is LoadState.Loading) {
                        item {
                            EmptyModerationState()
                        }
                    }

                    if (reports.loadState.append is LoadState.Loading) {
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

    deleteTarget?.let { report ->
        val contentLabel = report.normalizedContentType().moderationLabel()
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xóa $contentLabel", fontWeight = FontWeight.Bold) },
            text = {
                Text("Bạn có chắc chắn muốn xóa $contentLabel bị báo cáo này không? Hành động này không thể hoàn tác.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveReport(report, ModerationAction.DELETE_CONTENT)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                ) {
                    Text("Xóa $contentLabel", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun ReportCard(
    report: AdminReportSummaryResponse,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contentType = report.normalizedContentType()
    val contentTypeLabel = contentType.moderationLabel()
    val contentBadgeColor = when (contentType) {
        AdminContentType.COMMENT -> Color(0xFFF5F3FF) to Color(0xFF7C3AED)
        AdminContentType.MESSAGE -> Color(0xFFFFFBEB) to Color(0xFFB45309)
        AdminContentType.POST -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFDC2626))
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.reporterName?.takeIf { it.isNotBlank() } ?: "Người dùng CareNest",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                    )
                    Text(
                        text = report.reporterEmail?.takeIf { it.isNotBlank() } ?: "Ẩn email",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                    )
                }
                Box(
                    modifier = Modifier
                        .background(contentBadgeColor.first, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = contentTypeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentBadgeColor.second,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Lý do báo cáo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(report.reason, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))

            Spacer(modifier = Modifier.height(14.dp))
            Text("$contentTypeLabel bị báo cáo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(6.dp))
            report.previewText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (contentType == AdminContentType.POST) {
                report.previewImageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Ảnh xem trước",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Text(
                text = "Tác giả: ${report.contentAuthorName?.takeIf { it.isNotBlank() } ?: "Không rõ"}",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Xóa $contentTypeLabel", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Bỏ qua", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyModerationState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Không còn báo cáo chờ xử lý",
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun AdminContentType.moderationLabel(): String {
    return when (this) {
        AdminContentType.COMMENT -> "bình luận"
        AdminContentType.MESSAGE -> "tin nhắn"
        AdminContentType.POST -> "bài viết"
    }
}
