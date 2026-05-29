package com.example.carenest.feature.profile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("Nguyễn Văn A") }
    var email by remember { mutableStateOf("nguyenvana@gmail.com") }
    var phone by remember { mutableStateOf("0901234567") }
    var emergencyPhone by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("01/01/1990") }
    var gender by remember { mutableStateOf("Nam") }
    var bloodType by remember { mutableStateOf("O+") }
    
    var medReminder by remember { mutableStateOf(true) }
    var apptReminder by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Handle back if needed */ }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                text = "Thông tin tài khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            TextButton(onClick = {
                if (!isEditing) isEditing = true else {
                    isSaving = true
                    // Simulate save
                    isSaving = false
                    isEditing = false
                }
            }) {
                Text(
                    text = if (isSaving) "Đang lưu" else if (isEditing) "Lưu" else "Sửa",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            // Avatar Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) // Placeholder for Image
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(36.dp)
                            .background(PrimaryBlue, CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(fullName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Chủ gia đình", fontSize = 14.sp, color = Color(0xFF64748B))
            }

            // Medical Record Button
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryBlue, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hồ sơ y tế", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Xem tiền sử, dị ứng và nhóm máu", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
                }
            }

            // Personal Info
            SectionLabel("Thông tin cá nhân")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    InputField(icon = Icons.Default.Person, label = "Họ và tên", value = fullName, onValueChange = { fullName = it }, editable = isEditing)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Mail, label = "Email", value = email, onValueChange = { email = it }, editable = isEditing, keyboardType = KeyboardType.Email)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Phone, label = "Số điện thoại", value = phone, onValueChange = { phone = it }, editable = isEditing, keyboardType = KeyboardType.Phone)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Phone, label = "Số điện thoại khẩn cấp", value = emergencyPhone, onValueChange = { emergencyPhone = it }, editable = isEditing, placeholder = "Để trống nếu chưa có")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.CalendarToday, label = "Ngày sinh", value = birthday, onValueChange = { birthday = it }, editable = isEditing)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Cake, label = "Tuổi", value = "36", onValueChange = {}, editable = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Wc, label = "Giới tính", value = gender, onValueChange = { gender = it }, editable = isEditing)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Bloodtype, label = "Nhóm máu", value = bloodType, onValueChange = { bloodType = it }, editable = isEditing)
                }
            }

            // Notification Settings
            SectionLabel("Cài đặt thông báo")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    SettingsRowSwitch(icon = Icons.Default.Medication, iconBg = Color(0xFFF0F9FF), iconTint = Color(0xFF0EA5E9), label = "Nhắc uống thuốc", checked = medReminder, onCheckedChange = { medReminder = it })
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowSwitch(icon = Icons.Default.CalendarMonth, iconBg = Color(0xFFFDF2F8), iconTint = Color(0xFFDB2777), label = "Nhắc lịch tái khám", checked = apptReminder, onCheckedChange = { apptReminder = it })
                }
            }

            // App Settings
            SectionLabel("Ứng dụng")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Verified, iconBg = Color(0xFFECFDF5), iconTint = Color(0xFF16A34A), label = "Xác thực Bác sĩ")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.MedicalServices, iconBg = Color(0xFFEFF6FF), iconTint = Color(0xFF2563EB), label = "Duyệt hồ sơ Bác sĩ")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.Language, iconBg = Color(0xFFF5F3FF), iconTint = Color(0xFF7C3AED), label = "Ngôn ngữ", value = "Tiếng Việt")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.Security, iconBg = Color(0xFFF0FDFA), iconTint = Color(0xFF0D9488), label = "Chính sách bảo mật")
                }
            }

            // Support
            SectionLabel("Hỗ trợ")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    SettingsRow(icon = Icons.AutoMirrored.Filled.HelpCenter, iconBg = Color(0xFFFFF7ED), iconTint = Color(0xFFEA580C), label = "Trung tâm hỗ trợ")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.BugReport, iconBg = Color(0xFFEFF6FF), iconTint = Color(0xFF2563EB), label = "Báo cáo sự cố")
                }
            }

            // Logout Button
            Box(modifier = Modifier.padding(bottom = 20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(24.dp))
                        .clickable { onLogout() }
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đăng xuất tài khoản", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF64748B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
fun InputField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    editable: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            if (editable) {
                // In a real app we'd use a BasicTextField here for perfectly clean UI.
                // Using Text directly for non-editable is easier.
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
                        }
                        innerTextField()
                    }
                )
            } else {
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (value.isEmpty()) Color(0xFF94A3B8) else Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, iconBg: Color, iconTint: Color, label: String, value: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* noop */ }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 4.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
    }
}

@Composable
fun SettingsRowSwitch(icon: ImageVector, iconBg: Color, iconTint: Color, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}
