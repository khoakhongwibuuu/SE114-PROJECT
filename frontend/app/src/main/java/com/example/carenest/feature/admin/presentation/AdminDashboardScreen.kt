package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.presentation.components.AdminErrorState

@Composable
fun AdminDashboardScreen(
    onOpenUsers: () -> Unit = {},
    onOpenEkyc: () -> Unit = {},
    onOpenModeration: () -> Unit = {},
) {
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
            AdminErrorState(
                message = state.error.orEmpty(),
                onRetry = { viewModel.refresh() },
            )
        }

        else -> {
            val stats = state.stats ?: AdminDashboardStatsResponse()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7FAFE)) // LegacyBackground
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Tổng quan hệ thống",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF181C1F), // LegacyOnSurface
                )

                SummaryGrid(
                    stats = stats,
                    onOpenUsers = onOpenUsers,
                    onOpenEkyc = onOpenEkyc,
                    onOpenModeration = onOpenModeration,
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Xu hướng số lượng người dùng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF181C1F), // LegacyOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Biểu đồ đường theo dõi biến động số lượng người dùng của hệ thống qua thời gian.",
                            fontSize = 13.sp,
                            color = Color(0xFF404751), // LegacyOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        UserTrendLineChart(values = stats.trend)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    stats: AdminDashboardStatsResponse,
    onOpenUsers: () -> Unit,
    onOpenEkyc: () -> Unit,
    onOpenModeration: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Tổng người dùng",
                value = stats.totalUsers.toString(),
                accent = Color(0xFF00629D), // LegacyPrimary
                modifier = Modifier.weight(1f),
                onClick = onOpenUsers,
            )
            SummaryCard(
                title = "Bác sĩ đang hoạt động",
                value = stats.totalDoctors.toString(),
                accent = Color(0xFF42A5F5), // LegacyPrimaryContainer
                modifier = Modifier.weight(1f),
                onClick = onOpenUsers,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Hồ sơ eKYC chờ duyệt",
                value = stats.pendingEkycCount.toString(),
                accent = Color(0xFFFFB300), // Amber/Yellow
                modifier = Modifier.weight(1f),
                onClick = onOpenEkyc,
            )
            SummaryCard(
                title = "Báo cáo chờ xử lý",
                value = stats.moderationQueueCount.toString(),
                accent = Color(0xFFBA1A1A), // LegacyError
                modifier = Modifier.weight(1f),
                onClick = onOpenModeration,
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
    onClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accent, RoundedCornerShape(999.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 13.sp, color = Color(0xFF404751), fontWeight = FontWeight.Bold) // LegacyOnSurfaceVariant
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF181C1F)) // LegacyOnSurface
        }
    }
}

@Composable
private fun UserTrendLineChart(values: List<Long>) {
    if (values.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Chưa có dữ liệu xu hướng", color = Color(0xFF94A3B8))
        }
        return
    }

    val dates = remember {
        val formatter = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        List(values.size) { index ->
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, - ((values.size - 1) - index))
            formatter.format(calendar.time)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            val max = values.maxOrNull()?.coerceAtLeast(1L)?.toFloat() ?: 1f
            val min = values.minOrNull()?.toFloat() ?: 0f
            val spread = (max - min).takeIf { it > 0f } ?: 1f
            val horizontalStep = if (values.size > 1) size.width / (values.size - 1) else size.width

            val path = Path()
            val fillPath = Path()

            values.forEachIndexed { index, rawValue ->
                val x = index * horizontalStep
                val normalized = (rawValue - min) / spread
                val y = size.height - (normalized * (size.height - 32.dp.toPx())) - 16.dp.toPx()

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == values.size - 1) {
                    fillPath.lineTo(x, size.height)
                    fillPath.close()
                }
            }

            // Draw area gradient under the curve
            drawPath(
                path = fillPath,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00629D).copy(alpha = 0.2f),
                        Color(0xFF00629D).copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = size.height
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = Color(0xFF00629D),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // Draw dots & values on top of dots
            values.forEachIndexed { index, rawValue ->
                val x = index * horizontalStep
                val normalized = (rawValue - min) / spread
                val y = size.height - (normalized * (size.height - 32.dp.toPx())) - 16.dp.toPx()

                // Dot background & center
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFF00629D),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw value text above dot
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        rawValue.toString(),
                        x,
                        y - 8.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#181C1F") // LegacyOnSurface
                            textSize = 11.dp.toPx()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // Dates Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dates.forEach { date ->
                Text(
                    text = date,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF404751) // LegacyOnSurfaceVariant
                )
            }
        }
    }
}
