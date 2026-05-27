package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue

private data class MedicineScheduleItem(
    val id: String,
    val session: String,
    val medicineName: String,
    val dosage: String,
    val note: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconContainer: Color,
    val iconTint: Color,
    val taken: Boolean,
)

@Composable
fun MedicineScheduleScreen(
    onBack: () -> Unit,
    onAddSchedule: () -> Unit = {},
) {
    var items by remember {
        mutableStateOf(
            listOf(
                MedicineScheduleItem("1", "Buổi sáng", "Panadol Extra", "1 viên", "Uống sau ăn", Icons.Default.LightMode, Color(0xFFFFF9C4), Color(0xFFF9A825), false),
                MedicineScheduleItem("2", "Buổi sáng", "Vitamin C", "1 viên", "Sau bữa sáng", Icons.Default.LightMode, Color(0xFFFFF9C4), Color(0xFFF9A825), true),
                MedicineScheduleItem("3", "Buổi trưa", "Amoxicillin", "1 gói", "Sau ăn trưa", Icons.Default.WbTwilight, Color(0xFFE3F2FD), Color(0xFF1976D2), false),
                MedicineScheduleItem("4", "Buổi tối", "Berberin", "2 viên", "Trước khi ngủ", Icons.Default.Bedtime, Color(0xFFEDE7F6), Color(0xFF7B1FA2), false),
            ),
        )
    }

    val grouped = items.groupBy { it.session }
    val takenCount = items.count { it.taken }
    val totalCount = items.size
    val progress = if (totalCount == 0) 0 else ((takenCount * 100f) / totalCount).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                    }
                    Text(
                        text = "Lịch uống thuốc",
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryBlue)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Hôm nay", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$takenCount/$totalCount lần uống đã hoàn thành",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$progress%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            grouped.forEach { (session, sessionItems) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val first = sessionItems.first()
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(first.iconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(first.icon, contentDescription = null, tint = first.iconTint, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(session, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column {
                            sessionItems.forEachIndexed { index, item ->
                                ScheduleRow(
                                    item = item,
                                    showDivider = index < sessionItems.lastIndex,
                                    onToggle = {
                                        items = items.map { current ->
                                            if (current.id == item.id) current.copy(taken = !current.taken) else current
                                        }
                                    },
                                    onDelete = {
                                        items = items.filterNot { current -> current.id == item.id }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Chưa có lịch thuốc nào cho hồ sơ đang chọn.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }

        FloatingActionButton(
            onClick = onAddSchedule,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm lịch")
        }
    }
}

@Composable
private fun ScheduleRow(
    item: MedicineScheduleItem,
    showDivider: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (item.taken) PrimaryBlue else Color.Transparent)
                .then(
                    if (!item.taken) Modifier
                        .border(2.dp, Color(0xFFBFC7D3), CircleShape)
                    else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (item.taken) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.medicineName,
                color = if (item.taken) Color(0xFF94A3B8) else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.dosage} - ${item.note}",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFDC2626))
        }
    }

    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE2E8F0)),
        )
    }
}
