package com.example.carenest.feature.health.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carenest.R
import com.example.carenest.core.presentation.theme.PrimaryBlue
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationTrackerScreen(
    profileId: Long,
    viewModel: VaccinationViewModel,
    onNavigateBack: () -> Unit,
    onAddVaccination: (profileId: Long) -> Unit,
    onEditDose: (profileId: Long, recordId: Long, doseId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(profileId) {
        if (profileId > 0L) {
            viewModel.loadVaccinations(profileId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("V", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Lịch tiêm chủng", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E3A8A))
                            Text("HỒ SƠ #$profileId", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddVaccination(profileId) },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm")
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        // Guard: user chưa có hồ sơ cá nhân
        if (profileId <= 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = PrimaryBlue.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Chưa có hồ sơ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bạn chưa có hồ sơ y tế cá nhân. Vui lòng thiết lập hồ sơ để theo dõi lịch tiêm chủng.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(uiState.error ?: "Không thể tải lịch tiêm chủng", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadVaccinations(profileId) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Thử lại", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else if (uiState.vaccinationGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_add), // Placeholder for syringe
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chưa có dữ liệu tiêm chủng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Thêm mũi tiêm đầu tiên để bắt đầu theo dõi lịch sử vaccine.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { onAddVaccination(profileId) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm mũi tiêm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.vaccinationGroups) { group ->
                    VaccineGroupCard(group = group, onEditDose = { recordId, doseId ->
                        onEditDose(profileId, recordId, doseId)
                    })
                }
            }
        }
    }
}

@Composable
fun VaccineGroupCard(
    group: VaccinationTrackerGroup,
    onEditDose: (recordId: Long, doseId: Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces), // Placeholder
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = group.stageLabel,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E3A8A)
                )
            }
            
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            // Dose List
            Column(modifier = Modifier.padding(top = 4.dp)) {
                group.vaccinations.forEachIndexed { index, dose ->
                    val isCompleted = dose.status == "COMPLETED"
                    val visualState = dose.visualState()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    .clickable(enabled = !isCompleted) { onEditDose(dose.recordId, dose.doseId) }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(visualState.iconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.DateRange,
                                contentDescription = null,
                                tint = visualState.iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mũi ${dose.doseNumber} • ${dose.dateGiven ?: dose.plannedDate ?: "Chưa có ngày"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(visualState.badgeBackground)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = visualState.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = visualState.badgeColor
                                )
                            }
                            if (dose.clinicName != null) {
                                Text(
                                    text = dose.clinicName,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            } else {
                                Text(
                                    text = "Chưa cập nhật địa điểm",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                        
                        if (!isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                        }
                    }
                    
                    if (index < group.vaccinations.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

private data class DoseVisualState(
    val label: String,
    val iconBackground: Color,
    val iconColor: Color,
    val badgeBackground: Color,
    val badgeColor: Color
)

private fun VaccinationDoseUiModel.visualState(): DoseVisualState {
    if (status == "COMPLETED") {
        return DoseVisualState(
            label = "Đã tiêm",
            iconBackground = PrimaryBlue,
            iconColor = Color.White,
            badgeBackground = Color(0xFFDCFCE7),
            badgeColor = Color(0xFF15803D)
        )
    }

    val planned = plannedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val today = LocalDate.now()
    return when {
        planned == null -> DoseVisualState(
            label = "Chờ lên lịch",
            iconBackground = Color(0xFFEFF6FF),
            iconColor = PrimaryBlue,
            badgeBackground = Color(0xFFEFF6FF),
            badgeColor = Color(0xFF2563EB)
        )
        planned.isBefore(today) -> DoseVisualState(
            label = "Quá hạn",
            iconBackground = Color(0xFFFEE2E2),
            iconColor = Color(0xFFDC2626),
            badgeBackground = Color(0xFFFEE2E2),
            badgeColor = Color(0xFFB91C1C)
        )
        planned == today -> DoseVisualState(
            label = "Đến lịch hôm nay",
            iconBackground = Color(0xFFFFEDD5),
            iconColor = Color(0xFFF97316),
            badgeBackground = Color(0xFFFFEDD5),
            badgeColor = Color(0xFFC2410C)
        )
        else -> DoseVisualState(
            label = "Sắp tới",
            iconBackground = Color(0xFFEFF6FF),
            iconColor = PrimaryBlue,
            badgeBackground = Color(0xFFEFF6FF),
            badgeColor = Color(0xFF2563EB)
        )
    }
}
