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
import androidx.compose.foundation.layout.offset
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
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.medical.presentation.MedicineViewModel
import com.example.carenest.feature.medical.presentation.CabinetState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddMedicineScheduleScreen(
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val cabinetState by medicineViewModel.cabinetState.collectAsState()
    val isActionLoading by medicineViewModel.isActionLoading.collectAsState()

    // 1. Get family members safely
    val members = remember(dashboardState) {
        val successState = dashboardState as? DashboardState.Success
        successState?.data?.members.orEmpty()
    }

    // 2. Get cabinet medicines safely
    val medicines = remember(cabinetState) {
        val successState = cabinetState as? CabinetState.Success
        successState?.medicines.orEmpty()
    }

    var selectedMemberId by remember(members) {
        mutableStateOf(members.firstOrNull()?.id ?: "")
    }
    var selectedMedicineId by remember(medicines) {
        mutableStateOf(medicines.firstOrNull()?.id ?: -1L)
    }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("2") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(6).toString()) }
    var notes by remember { mutableStateOf("") }
    val parsedFrequency = frequency.toIntOrNull()
    val parsedStartDate = remember(startDate) { runCatching { LocalDate.parse(startDate) }.getOrNull() }
    val parsedEndDate = remember(endDate) { runCatching { LocalDate.parse(endDate) }.getOrNull() }
    val validationError = validateMedicationScheduleInput(
        selectedMemberId = selectedMemberId,
        selectedMedicineId = selectedMedicineId,
        dosage = dosage,
        frequencyValue = parsedFrequency,
        startDate = parsedStartDate,
        endDate = parsedEndDate,
    )
    val canSubmit = validationError == null && !isActionLoading

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
                IconButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp)) {
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
                if (members.isEmpty()) {
                    Text("Đang tải danh sách thành viên...", color = Color(0xFF64748B), fontSize = 13.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(members) { member ->
                            val safeId = (member.id as String?) ?: ""
                            val selected = safeId == selectedMemberId
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) Color.White else Color(0xFFEDF2F7))
                                    .clickable { selectedMemberId = safeId }
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
                                    val safeName = (member.name as String?) ?: "User"
                                    Text(safeName.take(1), color = PrimaryBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                                val displayName = (member.name as String?) ?: "User"
                                Text(
                                    text = displayName,
                                    color = if (selected) PrimaryBlue else Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 10.dp),
                                )
                            }
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
                        if (medicines.isEmpty()) {
                            Text("Chưa có thuốc trong tủ. Hãy thêm thuốc trước khi tạo lịch.", color = Color(0xFF64748B), fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp))
                        } else {
                            medicines.forEach { medicine ->
                                val safeMedId = (medicine.id as Long?) ?: -1L
                                val selected = safeMedId == selectedMedicineId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable { selectedMedicineId = safeMedId }
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
                                        val safeMedName = (medicine.medicineName as String?) ?: "Thuốc"
                                        Text(safeMedName, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${medicine.quantity} ${medicine.unit}", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val showDatePicker: (String, LocalDate?, (String) -> Unit) -> Unit = { initialDateStr, minDate, onDateSelected ->
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val initialDate = runCatching { LocalDate.parse(initialDateStr, formatter) }
                    .getOrElse { minDate ?: LocalDate.now() }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selected = LocalDate.of(year, month + 1, dayOfMonth)
                        onDateSelected(selected.format(formatter))
                    },
                    initialDate.year,
                    initialDate.monthValue - 1,
                    initialDate.dayOfMonth
                ).apply {
                    val minimumDate = minDate ?: LocalDate.now()
                    datePicker.minDate = minimumDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }.show()
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ScheduleInput(label = "LIỀU DÙNG", value = dosage, onValueChange = { dosage = it }, placeholder = "VD: 1 viên sau ăn")
                    ScheduleInput(label = "SỐ LẦN / NGÀY", value = frequency, onValueChange = { frequency = it.filter(Char::isDigit) }, leadingIcon = Icons.Default.Person)
                    ScheduleInput(
                        label = "NGÀY BẮT ĐẦU",
                        value = startDate,
                        onValueChange = { startDate = it },
                        leadingIcon = Icons.Default.CalendarToday,
                        onClick = {
                            showDatePicker(startDate, LocalDate.now()) { selected ->
                                startDate = selected
                                val selectedDate = LocalDate.parse(selected)
                                if (parsedEndDate != null && parsedEndDate.isBefore(selectedDate)) {
                                    endDate = selected
                                }
                            }
                        }
                    )
                    ScheduleInput(
                        label = "NGÀY KẾT THÚC",
                        value = endDate,
                        onValueChange = { endDate = it },
                        leadingIcon = Icons.Default.AccessTime,
                        onClick = { showDatePicker(endDate, parsedStartDate ?: LocalDate.now()) { endDate = it } }
                    )
                    ScheduleInput(label = "GHI CHÚ", value = notes, onValueChange = { notes = it }, placeholder = "VD: Uống sau ăn", leadingIcon = Icons.Default.LocalHospital)
                }
            }
            validationError?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Button(
            onClick = {
                val chosenMemberId = selectedMemberId.toLongOrNull()
                val chosenMedicine = medicines.find { it.id == selectedMedicineId }
                if (validationError != null) {
                    Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (chosenMemberId == null || chosenMedicine == null) {
                    Toast.makeText(context, "Vui lòng chọn thành viên và thuốc", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                medicineViewModel.createSchedule(
                    profileId = chosenMemberId,
                    medicineName = chosenMedicine.medicineName,
                    dosage = dosage.ifBlank { "1 viên" },
                    timesPerDay = parsedFrequency ?: 1,
                    startDate = startDate,
                    endDate = endDate,
                    notes = notes,
                    onSuccess = {
                        Toast.makeText(context, "Lưu lịch nhắc uống thuốc thành công!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        ) {
            if (isActionLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Lưu lịch", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

internal fun validateMedicationScheduleInput(
    selectedMemberId: String,
    selectedMedicineId: Long,
    dosage: String,
    frequencyValue: Int?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    today: LocalDate = LocalDate.now(),
): String? {
    if (selectedMemberId.toLongOrNull() == null) {
        return "Vui lòng chọn thành viên cần uống thuốc."
    }
    if (selectedMedicineId <= 0L) {
        return "Vui lòng chọn thuốc từ tủ thuốc."
    }
    if (dosage.isBlank()) {
        return "Vui lòng nhập liều dùng."
    }
    if (frequencyValue == null || frequencyValue !in 1..3) {
        return "Số lần uống mỗi ngày chỉ hỗ trợ từ 1 đến 3 lần."
    }
    if (startDate == null || endDate == null) {
        return "Ngày bắt đầu và kết thúc phải đúng định dạng."
    }
    if (startDate.isBefore(today)) {
        return "Ngày bắt đầu không được nằm trong quá khứ."
    }
    if (endDate.isBefore(startDate)) {
        return "Ngày kết thúc không được trước ngày bắt đầu."
    }
    return null
}

@Composable
private fun ScheduleInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        ) {
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
                enabled = onClick == null,
                readOnly = onClick != null,
                colors = if (onClick != null) {
                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF0F172A),
                        disabledBorderColor = Color(0xFFCBD5E1),
                        disabledLeadingIconColor = Color(0xFFCBD5E1),
                    )
                } else androidx.compose.material3.OutlinedTextFieldDefaults.colors()
            )
        }
    }
}
