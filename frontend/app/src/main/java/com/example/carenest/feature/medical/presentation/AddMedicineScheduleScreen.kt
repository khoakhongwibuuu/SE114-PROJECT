package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private data class ScheduleMember(val id: Int, val name: String)
private data class ScheduleMedicine(val id: Int, val name: String, val quantity: String)

@Composable
fun AddMedicineScheduleScreen(
    onBack: () -> Unit,
) {
    val members = remember {
        listOf(
            ScheduleMember(1, "Bố"),
            ScheduleMember(2, "Mẹ"),
            ScheduleMember(3, "Con gái"),
        )
    }
    val medicines = remember {
        listOf(
            ScheduleMedicine(1, "Panadol Extra", "12 viên"),
            ScheduleMedicine(2, "Vitamin C", "24 viên"),
            ScheduleMedicine(3, "Siro ho Prospan", "1 chai"),
        )
    }

    var selectedMemberId by remember { mutableIntStateOf(1) }
    var selectedMedicineId by remember { mutableIntStateOf(1) }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("2") }
    var startDate by remember { mutableStateOf("2026-05-28") }
    var endDate by remember { mutableStateOf("2026-06-03") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.ime),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.padding(start = (-8).dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                }
                Text(
                    text = "QUẢN LÝ Y TẾ",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Text(
                text = "Thêm lịch uống thuốc",
                color = Color(0xFF0F172A),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Thiết lập lịch nhắc uống thuốc thật từ dữ liệu tủ thuốc gia đình.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Thành viên gia đình", color = Color(0xFF475569), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(members, key = { it.id }) { member ->
                        val selected = member.id == selectedMemberId
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) Color.White else Color(0xFFEDF2F7))
                                .clickable { selectedMemberId = member.id }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(member.name.take(1), color = PrimaryBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Text(
                                text = member.name,
                                color = if (selected) PrimaryBlue else Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chọn thuốc từ tủ", color = Color(0xFF475569), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        medicines.forEach { medicine ->
                            val selected = medicine.id == selectedMedicineId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) Color(0xFFEFF6FF) else Color.Transparent)
                                    .clickable { selectedMedicineId = medicine.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F1FF)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(medicine.name, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(medicine.quantity, color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ScheduleInput(label = "LIỀU DÙNG", value = dosage, onValueChange = { dosage = it }, placeholder = "VD: 1 viên sau ăn")
                    ScheduleInput(label = "SỐ LẦN / NGÀY", value = frequency, onValueChange = { frequency = it.filter(Char::isDigit) }, leadingIcon = Icons.Default.Person)
                    ScheduleInput(label = "NGÀY BẮT ĐẦU", value = startDate, onValueChange = { startDate = it }, leadingIcon = Icons.Default.CalendarToday)
                    ScheduleInput(label = "NGÀY KẾT THÚC", value = endDate, onValueChange = { endDate = it }, leadingIcon = Icons.Default.AccessTime)
                    ScheduleInput(label = "GHI CHÚ", value = notes, onValueChange = { notes = it }, placeholder = "VD: Uống sau ăn", leadingIcon = Icons.Default.LocalHospital)
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        ) {
            Text("Lưu lịch", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ScheduleInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp)) }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
    }
}
