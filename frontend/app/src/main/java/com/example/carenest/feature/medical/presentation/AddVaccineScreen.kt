package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccineScreen(
    viewModel: AddVaccineViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    profileId: Long,
    editVaccineId: Long? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var vaccineName by remember { mutableStateOf("") }
    var selectedDose by remember { mutableIntStateOf(1) }
    var isCompleted by remember { mutableStateOf(true) }
    var date by remember { mutableStateOf("10/05/2026") }
    var clinicName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isEdit = editVaccineId != null

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF0F7FF),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                }
                Text(
                    text = if (isEdit) "Chỉnh sửa mũi tiêm" else "Ghi nhận tiêm chủng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
                Box(modifier = Modifier.size(40.dp))
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveVaccination(
                            profileId, 
                            com.example.carenest.feature.medical.domain.model.CreateVaccinationRequest(
                                vaccineName = vaccineName,
                                doseNumber = selectedDose,
                                status = if (isCompleted) "DONE" else "PENDING",
                                date = date,
                                location = clinicName,
                                notes = notes
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEdit) "Cập nhật mũi tiêm" else "Lưu mũi tiêm",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (isEdit) "C?p nh?t l?i th�ng tin mui ti�m d� ghi nh?n trong h? so s?c kh?e." else "Ghi nh?n t?ng mui ti�m c? th? d? theo d�i d?y d? v� ch�nh x�c l?ch tr�nh ph�ng b?nh c?a b�.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp, start = 10.dp, end = 10.dp)
            )

            // Card 1
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF1A73E8)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Thông tin vắc xin", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 20.dp))

                    Text("TÊN VẮC XIN", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 10.dp, start = 4.dp))
                    CustomTextField(value = vaccineName, onValueChange = { vaccineName = it }, placeholder = "Ví dụ: Vắc xin 6 trong 1 Hexaxim", icon = Icons.Default.Vaccines)

                    Text("��Y L� MUI TH? M?Y?", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp))
                    // Grid for doses
                    val doseOptions = listOf(1 to "Mũi 1", 2 to "Mũi 2", 3 to "Mũi 3", 4 to "Mũi 4", 99 to "Nhắc lại")
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        doseOptions.forEach { (value, label) ->
                            val isSelected = selectedDose == value
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryBlue else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isSelected) PrimaryBlue else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                    .clickable { selectedDose = value }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF475569))
                            }
                        }
                    }

                    Text("TRẠNG THÁI", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusChip(
                            label = "�� ti�m",
                            icon = Icons.Default.CheckCircle,
                            isSelected = isCompleted,
                            onClick = { isCompleted = true },
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(
                            label = "Lịch dự kiến",
                            icon = Icons.Default.Schedule,
                            isSelected = !isCompleted,
                            onClick = { isCompleted = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card 2
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF1A73E8)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Chi tiết mũi tiêm", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 20.dp))

                    Text(if (isCompleted) "NGÀY TIÊM THỰC TẾ" else "NGÀY HẸN / DỰ KIẾN", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 10.dp, start = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { /* Show date picker */ }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        Text(date, modifier = Modifier.padding(start = 12.dp).weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }

                    Text("ĐỊA ĐIỂM TIÊM (TÙY CHỌN)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp))
                    CustomTextField(value = clinicName, onValueChange = { clinicName = it }, placeholder = "V� d?: Trung t�m Ti�m ch?ng VNVC", icon = Icons.Default.LocalHospital)

                    Text("GHI CHÚ (TÙY CHỌN)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.padding(start = 12.dp).fillMaxSize(),
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B)),
                            decorationBox = { innerTextField ->
                                if (notes.isEmpty()) {
                                    Text("Phản ứng sau tiêm hoặc lưu ý theo dõi sức khỏe cho bé...", color = Color(0xFF94A3B8), fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B)),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0xFF94A3B8), fontSize = 15.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun StatusChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryBlue else Color(0xFFF1F5F9))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color(0xFF64748B), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF475569))
        }
    }
}
