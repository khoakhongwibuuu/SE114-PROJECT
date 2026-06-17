package com.example.carenest.feature.health.presentation

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.health.domain.model.AdministerDoseRequest
import com.example.carenest.feature.health.domain.model.CreateVaccinationRequest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationScheduleScreen(
    profileId: Long,
    vaccineId: Long?,
    doseId: Long?,
    viewModel: VaccinationViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var vaccineName by remember { mutableStateOf("") }
    var selectedDose by remember { mutableStateOf(1) }
    var isCompleted by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var clinicName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val isEdit = doseId != null
    var hasPrefilledEdit by remember(doseId) { mutableStateOf(false) }
    val editingDose = remember(uiState.vaccinationGroups, vaccineId, doseId) {
        uiState.vaccinationGroups
            .flatMap { it.vaccinations }
            .firstOrNull { dose ->
                dose.doseId == doseId && (vaccineId == null || dose.recordId == vaccineId)
            }
    }
    val canSubmit = !uiState.isSubmitting && (isEdit || vaccineName.isNotBlank())

    LaunchedEffect(editingDose?.doseId) {
        if (isEdit && editingDose != null && !hasPrefilledEdit) {
            selectedDose = editingDose.doseNumber
            isCompleted = true
            clinicName = editingDose.clinicName.orEmpty()
            notes = editingDose.notes.orEmpty()
            selectedDate = parseIsoDate(editingDose.dateGiven ?: editingDose.plannedDate) ?: Date()
            hasPrefilledEdit = true
        }
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            snackbarHostState.showSnackbar(
                if (isEdit) "Đã cập nhật mũi tiêm" else "Đã lưu mũi tiêm",
            )
            viewModel.resetSubmitState()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error ?: "Không thể lưu lịch tiêm")
            viewModel.resetSubmitState()
        }
    }

    fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)
                selectedDate = newCalendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).apply {
            val todayMillis = LocalDate.now().toDateAtStartOfDay().time
            if (isEdit || isCompleted) {
                datePicker.maxDate = todayMillis
            } else {
                datePicker.minDate = todayMillis
            }
        }.show()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Chỉnh sửa mũi tiêm" else "Ghi nhận tiêm chủng",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue,
                        fontSize = 20.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF0F7FF),
                ),
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = {
                        if (vaccineName.isBlank() && !isEdit) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập tên vắc xin")
                            }
                            return@Button
                        }
                        val selectedLocalDate = selectedDate.toLocalDate()
                        val today = LocalDate.now()
                        if ((isEdit || isCompleted) && selectedLocalDate.isAfter(today)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Ngày tiêm thực tế không được ở tương lai")
                            }
                            return@Button
                        }
                        if (!isEdit && !isCompleted && selectedLocalDate.isBefore(today)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Ngày dự kiến không được ở quá khứ")
                            }
                            return@Button
                        }

                        val dateStr = dateFormat.format(selectedDate)
                        val trimmedClinicName = clinicName.trim().ifBlank { null }
                        val trimmedNotes = notes.trim().ifBlank { null }

                        if (doseId != null) {
                            viewModel.administerDose(
                                doseId = doseId,
                                request = AdministerDoseRequest(
                                    dateAdministered = dateStr,
                                    location = trimmedClinicName,
                                    notes = trimmedNotes,
                                ),
                                onSuccess = {},
                            )
                        } else {
                            viewModel.createVaccinationPlan(
                                profileId = profileId,
                                request = CreateVaccinationRequest(
                                    vaccineName = vaccineName.trim(),
                                    doseNumber = selectedDose,
                                    status = if (isCompleted) "COMPLETED" else "PENDING",
                                    date = dateStr,
                                    location = trimmedClinicName,
                                    notes = trimmedNotes,
                                ),
                                onSuccess = {},
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = canSubmit,
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        if (isEdit) "Cập nhật mũi tiêm" else "Lưu mũi tiêm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        },
        containerColor = Color(0xFFF0F7FF),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = if (isEdit) {
                    "Cập nhật lại thông tin mũi tiêm đã ghi nhận trong hồ sơ sức khỏe."
                } else {
                    "Ghi nhận từng mũi tiêm để theo dõi đầy đủ và chính xác lịch phòng bệnh."
                },
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Thông tin vắc xin",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    if (!isEdit) {
                        Text(
                            "TÊN VẮC XIN",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                        OutlinedTextField(
                            value = vaccineName,
                            onValueChange = { vaccineName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = {
                                Text("Ví dụ: Vắc xin 6 trong 1 Hexaxim", color = Color(0xFF94A3B8))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = PrimaryBlue,
                            ),
                        )

                        Text(
                            "ĐÂY LÀ MŨI THỨ MẤY?",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                1 to "Mũi 1",
                                2 to "Mũi 2",
                                3 to "Mũi 3",
                                4 to "Mũi 4",
                                99 to "Nhắc lại",
                            ).forEach { (value, label) ->
                                val isSelected = selectedDose == value
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimaryBlue else Color(0xFFE2E8F0),
                                            RoundedCornerShape(20.dp),
                                        )
                                        .clickable { selectedDose = value }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        label,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "TRẠNG THÁI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    )
                    if (isEdit) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryBlue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                "Ghi nhận là đã tiêm",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isCompleted) PrimaryBlue else Color(0xFFF1F5F9))
                                    .clickable { isCompleted = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isCompleted) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    "Đã tiêm",
                                    color = if (isCompleted) Color.White else Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isCompleted) PrimaryBlue else Color(0xFFF1F5F9))
                                    .clickable { isCompleted = false },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (!isCompleted) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    "Lịch dự kiến",
                                    color = if (!isCompleted) Color.White else Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Chi tiết mũi tiêm",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    Text(
                        if (isCompleted) "NGÀY TIÊM THỰC TẾ" else "NGÀY HẸN / DỰ KIẾN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { showDatePicker() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF64748B))
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            displayFormat.format(selectedDate),
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }

                    Text(
                        "ĐỊA ĐIỂM TIÊM (TÙY CHỌN)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    )
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text("Ví dụ: Trung tâm Tiêm chủng VNVC", color = Color(0xFF94A3B8))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue,
                        ),
                    )

                    Text(
                        "GHI CHÚ (TÙY CHỌN)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                "Phản ứng sau tiêm hoặc lưu ý cần theo dõi thêm...",
                                color = Color(0xFF94A3B8),
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue,
                        ),
                        maxLines = 4,
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private fun Date.toLocalDate(): LocalDate =
    toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toDateAtStartOfDay(): Date =
    Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

private fun parseIsoDate(value: String?): Date? =
    value?.let { runCatching { LocalDate.parse(it).toDateAtStartOfDay() }.getOrNull() }
