package com.example.carenest.feature.appointment.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    onBack: () -> Unit,
    profileId: Long = 0L,
    viewModel: AppointmentViewModel? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication

    val vm: AppointmentViewModel = viewModel ?: viewModel(
        factory = AppointmentViewModelFactory(application.appointmentApi, application.secureSessionManager)
    )

    val isActionLoading by vm.isActionLoading.collectAsState()

    var facility by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }

    val activeProfileId = remember(profileId) {
        profileId.takeIf { it > 0L }
    }

    val hasActiveProfile = activeProfileId != null && activeProfileId > 0L
    val canSubmit = facility.isNotBlank() && doctor.isNotBlank() && !isActionLoading && hasActiveProfile

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDateTime = selectedDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
        },
        selectedDateTime.year,
        selectedDateTime.monthValue - 1,
        selectedDateTime.dayOfMonth
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedDateTime = selectedDateTime.withHour(hourOfDay).withMinute(minute)
        },
        selectedDateTime.hour,
        selectedDateTime.minute,
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
            }
            Text(text = "Lịch hẹn mới", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        ScrollViewContent(
            facility = facility,
            onFacilityChange = { facility = it },
            doctor = doctor,
            onDoctorChange = { doctor = it },
            address = address,
            onAddressChange = { address = it },
            notes = notes,
            onNotesChange = { notes = it },
            selectedDateTime = selectedDateTime,
            onDateClick = { datePickerDialog.show() },
            onTimeClick = { timePickerDialog.show() },
            canSubmit = canSubmit,
            hasActiveProfile = hasActiveProfile,
            isActionLoading = isActionLoading,
            onSubmit = {
                val targetProfileId = activeProfileId ?: return@ScrollViewContent
                val isoDate = selectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toString()
                vm.createAppointment(
                    profileId = targetProfileId,
                    hospitalName = facility,
                    doctorName = doctor,
                    isoDate = isoDate,
                    address = address.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() },
                    onSuccess = {
                        Toast.makeText(context, "Đã lưu lịch hẹn", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun ColumnScope.ScrollViewContent(
    facility: String, onFacilityChange: (String) -> Unit,
    doctor: String, onDoctorChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    selectedDateTime: LocalDateTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    canSubmit: Boolean,
    hasActiveProfile: Boolean,
    isActionLoading: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("HEALTHCARE SCHEDULING", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, letterSpacing = 1.sp)
            Text("Tạo lịch hẹn mới", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), modifier = Modifier.padding(top = 8.dp))
            Text("Sắp xếp các buổi khám bệnh của gia đình với dữ liệu thật từ hệ thống CareNest.", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 8.dp))
        }

        // Section: Facility & Doctor
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PHÒNG KHÁM / BỆNH VIỆN", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x66E2E8F0)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = facility,
                    onValueChange = onFacilityChange,
                    placeholder = { Text("Tên phòng khám...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("BÁC SĨ CHUYÊN KHOA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x66E2E8F0)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = doctor,
                    onValueChange = onDoctorChange,
                    placeholder = { Text("Tên bác sĩ...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        // Section: Date & Time
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("NGÀY KHÁM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x66E2E8F0)).clickable(onClick = onDateClick).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(selectedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GIỜ KHÁM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x66E2E8F0)).clickable(onClick = onTimeClick).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(selectedDateTime.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Section: Address
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ĐỊA CHỈ", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x66E2E8F0)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    placeholder = { Text("Địa chỉ phòng khám...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        // Section: Notes
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("GHI CHÚ THÊM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF475569))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Ghi chú các triệu chứng hoặc điều cần hỏi bác sĩ...", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x66E2E8F0),
                    focusedContainerColor = Color(0x66E2E8F0),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedBorderColor = PrimaryBlue
                ),
                maxLines = 4
            )
        }

        if (!hasActiveProfile) {
            Text(
                text = "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước khi lưu lịch hẹn.",
                color = Color(0xFFC62828),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Submit
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB), disabledContainerColor = Color(0xFF3498DB).copy(alpha = 0.5f)),
            enabled = canSubmit
        ) {
            if (isActionLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Lưu lịch hẹn", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
