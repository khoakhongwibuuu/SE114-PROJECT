package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse

@Composable
fun AdminDashboardScreen() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(application.adminRepository),
    )
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            com.example.carenest.feature.admin.presentation.components.AdminErrorState(
                message = state.error.orEmpty(),
                onRetry = { viewModel.refresh() }
            )
        }

        else -> {
            val stats = state.stats ?: AdminDashboardStatsResponse()
            val trend = stats.trend.ifEmpty {
                listOf(stats.totalUsers, stats.totalDoctors, stats.pendingEkycCount, stats.moderationQueueCount)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Tổng quan hệ thống",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                )

                SummaryGrid(stats = stats)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Xu hướng hoạt động",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Theo dõi nhanh các chỉ số quan trọng của khu vực quản trị.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TrendChart(values = trend)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryGrid(stats: AdminDashboardStatsResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Tổng người dùng",
                value = stats.totalUsers.toString(),
                accent = Color(0xFF2563EB),
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "Bác sĩ đang hoạt động",
                value = stats.totalDoctors.toString(),
                accent = Color(0xFF059669),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Hồ sơ eKYC chờ duyệt",
                value = stats.pendingEkycCount.toString(),
                accent = Color(0xFFD97706),
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "Báo cáo chờ xử lý",
                value = stats.moderationQueueCount.toString(),
                accent = Color(0xFFDC2626),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accent, RoundedCornerShape(999.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
        }
    }
}

@Composable
private fun TrendChart(values: List<Long>) {
    if (values.isEmpty()) {
        Text(text = "Chưa có dữ liệu xu hướng", color = Color(0xFF94A3B8))
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val max = values.maxOrNull()?.coerceAtLeast(1L)?.toFloat() ?: 1f
        val min = values.minOrNull()?.toFloat() ?: 0f
        val spread = (max - min).takeIf { it > 0f } ?: 1f
        val horizontalStep = if (values.size > 1) size.width / (values.size - 1) else size.width

        val path = Path()
        values.forEachIndexed { index, rawValue ->
            val x = index * horizontalStep
            val normalized = (rawValue - min) / spread
            val y = size.height - (normalized * (size.height - 24.dp.toPx())) - 12.dp.toPx()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 4.dp.toPx(),
                center = Offset(x, y),
            )
        }

        drawPath(
            path = path,
            color = Color(0xFF2563EB),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}
