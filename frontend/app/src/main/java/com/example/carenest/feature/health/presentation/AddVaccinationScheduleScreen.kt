package com.example.carenest.feature.health.presentation

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.health.domain.model.AdministerDoseRequest
import com.example.carenest.feature.health.domain.model.CreateVaccinationRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationScheduleScreen(
    profileId: Long,
    vaccineId: Long?,
    doseId: Long?,
    viewModel: VaccinationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var vaccineName by remember { mutableStateOf("") }
    var selectedDose by remember { mutableStateOf(1) }
    var isCompleted by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var clinicName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val isEdit = doseId != null
    
    // Listen for submission success
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            Toast.makeText(context, if (isEdit) "Cập nhật thành công" else "Lưu thành công", Toast.LENGTH_SHORT).show()
            viewModel.resetSubmitState()
            onNavigateBack()
        }
    }
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
            viewModel.resetSubmitState()
        }
    }

    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(year, month, dayOfMonth)
            selectedDate = newCalendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Chỉnh sửa mũi tiêm" else "Ghi nhận tiêm chủng",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF0F7FF)
                )
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = {
                        if (vaccineName.isBlank() && !isEdit) {
                            Toast.makeText(context, "Vui lòng nhập tên vắc xin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val dateStr = dateFormat.format(selectedDate)
                        
                        if (isEdit && doseId != null) {
                            viewModel.administerDose(
                                doseId = doseId,
                                request = AdministerDoseRequest(
                                    dateAdministered = dateStr,
                                    location = clinicName.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                ),
                                onSuccess = {}
                            )
                        } else {
                            viewModel.createVaccinationPlan(
                                profileId = profileId,
                                request = CreateVaccinationRequest(
                                    vaccineName = vaccineName,
                                    doseNumber = selectedDose,
                                    status = if (isCompleted) "COMPLETED" else "PENDING",
                                    date = dateStr,
                                    location = clinicName.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                ),
                                onSuccess = {}
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = !uiState.isSubmitting
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isEdit) "Cập nhật mũi tiêm" else "Lưu mũi tiêm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF0F7FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                if (isEdit) "Cập nhật lại thông tin mũi tiêm đã ghi nhận trong hồ sơ sức khỏe."
                else "Ghi nhận từng mũi tiêm cụ thể để theo dõi đầy đủ và chính xác lịch trình phòng bệnh của bé.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
            )

            // Vắc xin Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Thông tin vắc xin", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 20.dp))
                    
                    if (!isEdit) {
                        Text("TÊN VẮC XIN", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 10.dp))
                        OutlinedTextField(
                            value = vaccineName,
                            onValueChange = { vaccineName = it },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Ví dụ: Vắc xin 6 trong 1 Hexaxim", color = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = PrimaryBlue
                            )
                        )
                        
                        Text("ĐÂY LÀ MŨI THỨ MẤY?", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1 to "Mũi 1", 2 to "Mũi 2", 3 to "Mũi 3", 4 to "Mũi 4", 99 to "Nhắc lại").forEach { (value, label) ->
                                val isSelected = selectedDose == value
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) PrimaryBlue else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSelected) PrimaryBlue else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                        .clickable { selectedDose = value }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = if (isSelected) Color.White else Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Text("TRẠNG THÁI", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(if (isCompleted) PrimaryBlue else Color(0xFFF1F5F9)).clickable { isCompleted = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isCompleted) Color.White else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đã tiêm", color = if (isCompleted) Color.White else Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        Row(
                            modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(if (!isCompleted) PrimaryBlue else Color(0xFFF1F5F9)).clickable { isCompleted = false },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = if (!isCompleted) Color.White else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lịch dự kiến", color = if (!isCompleted) Color.White else Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            
            // Chi tiết mũi tiêm Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Chi tiết mũi tiêm", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 20.dp))
                    
                    Text(if (isCompleted) "NGÀY TIÊM THỰC TẾ" else "NGÀY HẸN / DỰ KIẾN", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F5F9)).clickable { datePickerDialog.show() }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(displayFormat.format(selectedDate), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Text("ĐỊA ĐIỂM TIÊM (TÙY CHỌN)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Ví dụ: Trung tâm Tiêm chủng VNVC", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue
                        )
                    )

                    Text("GHI CHÚ (TÙY CHỌN)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Phản ứng sau tiêm hoặc lưu ý theo dõi sức khỏe cho bé...", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue
                        ),
                        maxLines = 4
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
