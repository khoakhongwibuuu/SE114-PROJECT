package com.example.carenest.feature.profile.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMedicalScreen(
    profileId: Int,
    viewModel: UserMedicalViewModel,
    onBack: () -> Unit,
    onNavigateToMedicineSchedule: () -> Unit,
    onNavigateToAppointmentList: () -> Unit,
    onNavigateToVaccinationTracker: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
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
                        text = { Text("Khẩn cấp", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
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
                        1 -> EmergencyTabContent(
                            state = state,
                            viewModel = viewModel,
                            onDial = { phone ->
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Không thể mở ứng dụng cuộc gọi", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        2 -> SchedulesTabContent(
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
    profileId: Int
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
                        label = { Text("C�n n?ng (kg)") },
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
