package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.medical.domain.model.VaccinationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineScheduleScreen(
    viewModel: VaccineScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    memberId: Long? = null,
    onBack: () -> Unit,
    onAddVaccine: (Long, Long?) -> Unit // profileId, editVaccineId
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(memberId) {
        viewModel.loadData(memberId)
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E293B))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3B82F6))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Lịch tiêm chủng",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "HỒ SƠ #${memberId ?: ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { uiState.currentMemberId?.let { onAddVaccine(it, null) } },
                containerColor = PrimaryBlue,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mũi tiêm", tint = Color.White)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.groups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Vaccines,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Chưa có dữ liệu tiêm chủng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Thêm mũi tiêm đầu tiên để bắt đầu theo dõi lịch sử vaccine.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 100.dp)
            ) {
                items(uiState.groups) { group ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = borderStroke()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Group Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = group.stageLabel,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Doses
                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                group.vaccinations.forEachIndexed { index, vaccine ->
                                    val isLast = index == group.vaccinations.size - 1
                                    VaccineDoseRow(
                                        vaccine = vaccine,
                                        isLast = isLast,
                                        onClick = { uiState.currentMemberId?.let { onAddVaccine(it, vaccine.id) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccineDoseRow(vaccine: VaccinationItem, isLast: Boolean, onClick: () -> Unit) {
    val isDone = vaccine.status == "DONE"
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDone) Color(0xFF3B82F6) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isDone) Color.White else PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${vaccine.vaccineName} • Mũi ${vaccine.doseNumber} • ${vaccine.dateGiven ?: vaccine.plannedDate ?: "Chưa có ngày"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(3.dp))
                if (!vaccine.clinicName.isNullOrBlank()) {
                    Text(
                        text = vaccine.clinicName,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    Text(
                        text = "Chưa cập nhật địa điểm",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8) // italic effect achieved implicitly or left as color
                    )
                }
            }
            Icon(
                Icons.Default.Edit,
                contentDescription = "Sửa",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
        }
        if (!isLast) {
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
