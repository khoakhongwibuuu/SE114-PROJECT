package com.example.carenest.feature.profile.presentation

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.health.domain.model.GrowthChartPointResponse
import com.example.carenest.feature.health.domain.model.GrowthRecordResponse
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMedicalScreen(
    profileId: Long,
    viewModel: UserMedicalViewModel,
    onBack: () -> Unit,
    onNavigateToMedicineSchedule: () -> Unit,
    onNavigateToAppointmentList: () -> Unit,
    onNavigateToVaccinationTracker: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
        viewModel.loadGrowthData(profileId)
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hồ sơ y tế",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        TextButton(
                            onClick = { viewModel.saveMedicalProfile(profileId) },
                            enabled = !state.isSaving
                        ) {
                            Text(
                                text = if (state.isSaving) "Đang lưu" else "Lưu",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF8FAFC),
                    contentColor = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Thông tin", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Tăng trưởng", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Khẩn cấp", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Lịch", fontWeight = FontWeight.Bold) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> InfoTabContent(
                            state = state,
                            viewModel = viewModel,
                            profileId = profileId
                        )
                        1 -> GrowthTabContent(
                            state = state,
                            viewModel = viewModel,
                            profileId = profileId
                        )
                        2 -> EmergencyTabContent(
                            state = state,
                            viewModel = viewModel,
                            onDial = { phone ->
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Không thể mở ứng dụng cuộc gọi")
                                    }
                                }
                            }
                        )
                        3 -> SchedulesTabContent(
                            onNavigateToMedicineSchedule = onNavigateToMedicineSchedule,
                            onNavigateToAppointmentList = onNavigateToAppointmentList,
                            onNavigateToVaccinationTracker = onNavigateToVaccinationTracker
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoTabContent(
    state: UserMedicalUiState,
    viewModel: UserMedicalViewModel,
    profileId: Long
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Section: Vitals
        UserMedicalSectionLabel("Chỉ số cơ thể")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.height,
                        onValueChange = { viewModel.onHeightChange(it) },
                        label = { Text("Chiều cao (cm)") },
                        placeholder = { Text("Ví dụ: 170") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = PrimaryBlue) }
                    )

                    OutlinedTextField(
                        value = state.weight,
                        onValueChange = { viewModel.onWeightChange(it) },
                        label = { Text("Cân nặng (kg)") },
                        placeholder = { Text("Ví dụ: 65") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = PrimaryBlue) }
                    )
                }
            }
        }

        // Section: Health Information
        UserMedicalSectionLabel("Thông tin sức khỏe")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Blood Type Selector
                var expandedBloodType by remember { mutableStateOf(false) }
                val bloodTypes = listOf(
                    "A_POSITIVE" to "A+", "A_NEGATIVE" to "A-",
                    "B_POSITIVE" to "B+", "B_NEGATIVE" to "B-",
                    "AB_POSITIVE" to "AB+", "AB_NEGATIVE" to "AB-",
                    "O_POSITIVE" to "O+", "O_NEGATIVE" to "O-",
                    "UNKNOWN" to "Chưa rõ"
                )
                val selectedBloodLabel = bloodTypes.find { it.first == state.bloodType }?.second ?: state.bloodType

                ExposedDropdownMenuBox(
                    expanded = expandedBloodType,
                    onExpandedChange = { expandedBloodType = !expandedBloodType }
                ) {
                    OutlinedTextField(
                        value = selectedBloodLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nhóm máu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBloodType) },
                        leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBloodType,
                        onDismissRequest = { expandedBloodType = false }
                    ) {
                        bloodTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    viewModel.onBloodTypeChange(option.first)
                                    expandedBloodType = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Allergies Input
                OutlinedTextField(
                    value = state.allergies,
                    onValueChange = { viewModel.onAllergiesChange(it) },
                    label = { Text("Dị ứng") },
                    placeholder = { Text("Ví dụ: Phấn hoa, Hải sản") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chronic Diseases Input
                OutlinedTextField(
                    value = state.chronicDiseases,
                    onValueChange = { viewModel.onChronicDiseasesChange(it) },
                    label = { Text("Tiền sử bệnh lý") },
                    placeholder = { Text("Ví dụ: Huyết áp cao, Tiểu đường") },
                    leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section: Emergency Contact Edit
        UserMedicalSectionLabel("Liên hệ khẩn cấp")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = state.emergencyName,
                    onValueChange = { viewModel.onEmergencyNameChange(it) },
                    label = { Text("Tên người liên hệ") },
                    placeholder = { Text("Ví dụ: Vợ, Bố, Mẹ") },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.emergencyPhone,
                    onValueChange = { viewModel.onEmergencyPhoneChange(it) },
                    label = { Text("Số điện thoại khẩn cấp") },
                    placeholder = { Text("Ví dụ: 0987654321") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.saveMedicalProfile(profileId) },
            enabled = !state.isSaving,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Lưu thông tin hồ sơ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrowthTabContent(
    state: UserMedicalUiState,
    viewModel: UserMedicalViewModel,
    profileId: Long
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    fun showDatePicker() {
        val currentDate = parseIsoDateToDate(state.growthRecordDate) ?: Date()
        val calendar = Calendar.getInstance().apply { time = currentDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                viewModel.onGrowthRecordDateChange(selectedCalendar.time.toIsoDateString())
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = LocalDate.now().toDateAtStartOfDay().time
        }.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        UserMedicalSectionLabel("Ghi nhận chỉ số mới")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Ngày ghi nhận",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { showDatePicker() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = parseIsoDateToDate(state.growthRecordDate)?.let(displayFormat::format) ?: "Chọn ngày",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.growthWeight,
                        onValueChange = { viewModel.onGrowthWeightChange(it) },
                        label = { Text("Cân nặng (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = state.growthHeight,
                        onValueChange = { viewModel.onGrowthHeightChange(it) },
                        label = { Text("Chiều cao (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.growthHeadCircumference,
                    onValueChange = { viewModel.onGrowthHeadCircumferenceChange(it) },
                    label = { Text("Vòng đầu (cm, tùy chọn)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.growthNotes,
                    onValueChange = { viewModel.onGrowthNotesChange(it) },
                    label = { Text("Ghi chú") },
                    placeholder = { Text("Ví dụ: đo buổi sáng, sau ăn 2 giờ...") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                if (state.growthError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.growthError,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.saveGrowthRecord(profileId) },
                    enabled = !state.isGrowthSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.isGrowthSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu chỉ số", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        UserMedicalSectionLabel("Theo dõi tăng trưởng")
        when {
            state.isGrowthLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            state.growthRecords.isEmpty() -> {
                GrowthEmptyState(
                    error = state.growthError,
                    onRetry = { viewModel.loadGrowthData(profileId) }
                )
            }
            else -> {
                GrowthTrendPanel(points = state.growthChart)
                Spacer(modifier = Modifier.height(12.dp))
                state.growthRecords.forEach { record ->
                    GrowthRecordCard(record)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GrowthEmptyState(
    error: String?,
    onRetry: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (error == null) Icons.Default.TrendingUp else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (error == null) PrimaryBlue else Color(0xFFB91C1C),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error ?: "Chưa có chỉ số tăng trưởng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (error == null) Color(0xFF1E293B) else Color(0xFFB91C1C),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (error == null) {
                    "Thêm lần đo đầu tiên để xem lịch sử cân nặng, chiều cao và BMI."
                } else {
                    "Kiểm tra kết nối hoặc thử tải lại dữ liệu."
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(14.dp)) {
                    Text("Thử lại", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
private fun GrowthTrendPanel(points: List<GrowthChartPointResponse>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xu hướng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (points.size < 2) {
                Text(
                    "Cần ít nhất 2 lần đo để hiển thị xu hướng thay đổi.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
                return@Column
            }

            val first = points.first()
            val latest = points.last()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GrowthDeltaTile(
                    label = "Cân nặng",
                    value = "${formatSigned(latest.weightKg - first.weightKg)} kg"
                )
                GrowthDeltaTile(
                    label = "Chiều cao",
                    value = "${formatSigned(latest.heightCm - first.heightCm)} cm"
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Từ ${formatDateForDisplay(first.recordDate)} đến ${formatDateForDisplay(latest.recordDate)}",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun RowScope.GrowthDeltaTile(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun GrowthRecordCard(record: GrowthRecordResponse) {
    val alertColor = if (record.isAnomalous == true) Color(0xFFDC2626) else Color(0xFF16A34A)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(alertColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (record.isAnomalous == true) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = alertColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDateForDisplay(record.recordDate),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        if (record.isAnomalous == true) "Cần theo dõi thêm" else "Trong vùng theo dõi ổn định",
                        fontSize = 12.sp,
                        color = alertColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GrowthMetric("Cân nặng", "${record.weightKg.formatOne()} kg")
                GrowthMetric("Chiều cao", "${record.heightCm.formatOne()} cm")
                GrowthMetric("BMI", record.bmi?.formatOne() ?: "--")
            }

            if (record.weightPercentile != null || record.heightPercentile != null || record.notes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    listOfNotNull(
                        record.weightPercentile?.let { "Cân nặng/BMI: ${it.formatOne()}%" },
                        record.heightPercentile?.let { "Chiều cao: ${it.formatOne()}%" },
                        record.notes?.takeIf { it.isNotBlank() }
                    ).joinToString(" • "),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun RowScope.GrowthMetric(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EmergencyTabContent(
    state: UserMedicalUiState,
    viewModel: UserMedicalViewModel,
    onDial: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "YÊU CẦU TRỢ GIÚP KHẨN CẤP",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFEF4444),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Pulsing SOS button calling "115"
        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse circle background
            Box(
                modifier = Modifier
                    .size((130 * scale).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
            )
            // Core button
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .clickable { onDial("115") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SOS",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "GỌI 115",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SOS Summary Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Thông tin khẩn cấp y tế",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Nhóm máu: ", fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                    val bloodTypes = listOf(
                        "A_POSITIVE" to "A+", "A_NEGATIVE" to "A-",
                        "B_POSITIVE" to "B+", "B_NEGATIVE" to "B-",
                        "AB_POSITIVE" to "AB+", "AB_NEGATIVE" to "AB-",
                        "O_POSITIVE" to "O+", "O_NEGATIVE" to "O-",
                        "UNKNOWN" to "Chưa rõ"
                    )
                    val bloodText = bloodTypes.find { it.first == state.bloodType }?.second ?: "Chưa rõ"
                    Text(bloodText, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                }

                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Dị ứng: ", fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                    Text(state.allergies.ifBlank { "Không có" }, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                }

                Row {
                    Text("Tiền sử bệnh lý: ", fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                    Text(state.chronicDiseases.ifBlank { "Không có" }, color = Color(0xFF7F1D1D), fontSize = 14.sp)
                }
            }
        }

        // Primary emergency contact with a dial button
        if (!state.emergencyPhone.isBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.emergencyName.ifBlank { "Liên hệ khẩn cấp" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = state.emergencyPhone,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = { onDial(state.emergencyPhone) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Gọi", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SchedulesTabContent(
    onNavigateToMedicineSchedule: () -> Unit,
    onNavigateToAppointmentList: () -> Unit,
    onNavigateToVaccinationTracker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "LỐI TẮT LỊCH TRÌNH",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF64748B),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ShortcutOption(
            icon = Icons.Default.MedicalServices,
            title = "Lịch uống thuốc",
            description = "Theo dõi giờ uống thuốc định kỳ của bạn",
            color = Color(0xFF0EA5E9),
            bg = Color(0xFFE0F2FE),
            onClick = onNavigateToMedicineSchedule
        )

        ShortcutOption(
            icon = Icons.Default.CalendarMonth,
            title = "Lịch khám bệnh",
            description = "Danh sách lịch hẹn bác sĩ và tái khám",
            color = Color(0xFFA855F7),
            bg = Color(0xFFF3E8FF),
            onClick = onNavigateToAppointmentList
        )

        ShortcutOption(
            icon = Icons.Default.Vaccines,
            title = "Lịch tiêm chủng",
            description = "Thông tin vắc-xin và lộ trình tiêm phòng",
            color = Color(0xFF0097A7),
            bg = Color(0xFFE0F7FA),
            onClick = onNavigateToVaccinationTracker
        )
    }
}

@Composable
private fun ShortcutOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun UserMedicalSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

private fun parseIsoDateToDate(value: String?): Date? =
    value?.let { runCatching { LocalDate.parse(it).toDateAtStartOfDay() }.getOrNull() }

private fun Date.toIsoDateString(): String =
    toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()

private fun LocalDate.toDateAtStartOfDay(): Date =
    Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

private fun formatDateForDisplay(value: String): String =
    runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(LocalDate.parse(value).toDateAtStartOfDay())
    }.getOrDefault(value)

private fun Double.formatOne(): String = String.format(Locale.getDefault(), "%.1f", this)

private fun formatSigned(value: Double): String =
    "${if (value >= 0) "+" else ""}${value.formatOne()}"
