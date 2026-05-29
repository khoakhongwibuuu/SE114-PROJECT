package com.example.carenest.ui.medical

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.model.HealthProfile
import com.example.carenest.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileDetailScreen(
    viewModel: ProfileViewModel,
    memberId: Int,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(memberId) {
        viewModel.loadProfile(memberId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CareNest", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0369A1)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0369A1))
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Lỗi tải dữ liệu", color = Color.Red)
            }
        } else if (uiState.profileData != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                HeaderSection(uiState.profileData!!)
                CustomTabRow(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = uiState.selectedTab == 0) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        HealthOverviewSection(uiState.profileData!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        VitalsSection(uiState.profileData!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        MedicalHistorySection(uiState.profileData!!)
                    }
                }
                
                AnimatedVisibility(visible = uiState.selectedTab == 1) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Đang cập nhật biểu đồ theo dõi phát triển...", color = Color.Gray, modifier = Modifier.padding(32.dp))
                    }
                }

                AnimatedVisibility(visible = uiState.selectedTab == 2) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmergencyContactCard(uiState.profileData!!)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(profile: HealthProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
            }
            if (profile.isVerified) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = profile.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDBEAFE))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(profile.role.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${profile.age ?: "--"} tuổi • ${profile.location ?: "Chưa rõ"}",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun CustomTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Thông tin", "Theo dõi phát triển", "Khẩn cấp")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE2E8F0))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF0369A1) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun HealthOverviewSection(profile: HealthProfile) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Blood Type Card
        Surface(
            modifier = Modifier.weight(0.4f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFDF2F8)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("NHÓM MÁU", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                Spacer(modifier = Modifier.height(8.dp))
                Text(profile.bloodType ?: "N/A", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBE185D))
            }
        }

        // Allergies Card
        Surface(
            modifier = Modifier.weight(0.6f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF7ED)
        ) {
            Box {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp),
                    tint = Color(0xFFFFEDD5)
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DỊ ỨNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (profile.allergies.isEmpty()) {
                        Text("Không có ghi nhận", fontSize = 14.sp, color = Color(0xFF9A3412))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            profile.allergies.forEach { allergy ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEDD5))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(allergy.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
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
fun VitalsSection(profile: HealthProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Chiều cao", fontSize = 12.sp, color = Color.Gray)
                    Text("${profile.height ?: "--"} cm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cân nặng", fontSize = 12.sp, color = Color.Gray)
                    Text("${profile.weight ?: "--"} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val bmi = profile.bmi ?: 0f
            val bmiStatus = when {
                bmi == 0f -> "Chưa rõ"
                bmi < 18.5 -> "Thiếu cân"
                bmi < 25 -> "Bình thường"
                bmi < 30 -> "Thừa cân"
                else -> "Béo phì"
            }
            val progress = (bmi / 40f).coerceIn(0f, 1f)
            val barColor = when {
                bmi < 18.5 -> Color(0xFFFBBF24)
                bmi < 25 -> Color(0xFF10B981)
                bmi < 30 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Chỉ số BMI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.weight(1f))
                Text(String.format("%.1f", bmi), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = barColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("($bmiStatus)", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = Color(0xFFE2E8F0),
            )
        }
    }
}

@Composable
fun MedicalHistorySection(profile: HealthProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF0369A1))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TIỀN SỬ BỆNH LÝ", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (profile.medicalHistory.isEmpty()) {
                Text("Chưa có thông tin tiền sử bệnh lý.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                profile.medicalHistory.forEach { condition ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(condition.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(condition.description, fontSize = 14.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactCard(profile: HealthProfile) {
    val context = LocalContext.current
    val contact = profile.emergencyContact

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF01579B)
    ) {
        if (contact == null) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chưa có liên hệ khẩn cấp", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF0277BD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LIÊN HỆ KHẨN CẤP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(contact.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(contact.relation, fontSize = 14.sp, color = Color(0xFFB3E5FC))
                }
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(48.dp).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Gọi", tint = Color(0xFF01579B))
                }
            }
        }
    }
}
