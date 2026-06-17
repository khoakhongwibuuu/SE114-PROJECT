package com.example.carenest.feature.profile.presentation

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.core.presentation.theme.PrimaryBlue
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    refreshTrigger: Int = 0,
    hasActiveHealthProfile: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onNavigateToMedicalRecord: () -> Unit = {},
    onNavigateToFamilySetup: () -> Unit = {},
    onNavigateToDoctorVerification: () -> Unit = {},
    onNavigateToDoctorWorkspace: () -> Unit = {},
    onNavigateToPatientBookingCenter: () -> Unit = {},
    onNavigateToPolicy: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(refreshTrigger) {
        viewModel.loadCurrentUser()
    }

    // Observe messages
    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null) {
            Toast.makeText(context, state.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(ProfileEvent.ClearMessage)
        }
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(ProfileEvent.ClearMessage)
        }
    }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.onEvent(ProfileEvent.AvatarSelected(uri))
            }
        }
    )

    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            viewModel.onEvent(ProfileEvent.BirthdayChanged(formattedDate))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                text = "Thông tin tài khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A)
            )
            TextButton(
                onClick = {
                    if (!state.isEditing) {
                        viewModel.onEvent(ProfileEvent.EditClicked)
                    } else {
                        viewModel.onEvent(ProfileEvent.SaveClicked)
                    }
                },
                enabled = !state.isSaving
            ) {
                Text(
                    text = if (state.isSaving) "Đang lưu" else if (state.isEditing) "Lưu" else "Sửa",
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
                    if (state.avatarUri != null) {
                        AsyncImage(
                            model = state.avatarUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
                        }
                    }

                    if (state.isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(36.dp)
                            .background(PrimaryBlue, CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .clickable {
                                if (!state.isUploadingAvatar) {
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(state.fullName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.memberRole, fontSize = 14.sp, color = Color(0xFF64748B))
            }

            // Medical Record Button
            if (!hasActiveHealthProfile) {
                MissingHealthProfileCard(
                    onNavigateToFamilySetup = onNavigateToFamilySetup,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .clickable(enabled = hasActiveHealthProfile) { onNavigateToMedicalRecord() }
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
                    InputField(icon = Icons.Default.Person, label = "Họ và tên", value = state.fullName, onValueChange = { viewModel.onEvent(ProfileEvent.FullNameChanged(it)) }, editable = state.isEditing)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Mail, label = "Email", value = state.email, onValueChange = { viewModel.onEvent(ProfileEvent.EmailChanged(it)) }, editable = state.isEditing, keyboardType = KeyboardType.Email)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Phone, label = "Số điện thoại", value = state.phone, onValueChange = { viewModel.onEvent(ProfileEvent.PhoneChanged(it)) }, editable = state.isEditing, keyboardType = KeyboardType.Phone)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Phone, label = "Số điện thoại khẩn cấp", value = state.emergencyPhone, onValueChange = { viewModel.onEvent(ProfileEvent.EmergencyPhoneChanged(it)) }, editable = state.isEditing, placeholder = "Để trống nếu chưa có")
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    
                    // Birthday Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = state.isEditing) { datePickerDialog.show() }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ngày sinh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.birthday.ifEmpty { "dd/mm/yyyy" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.birthday.isEmpty()) Color(0xFF94A3B8) else Color(0xFF1E293B)
                            )
                        }
                    }
                    
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    InputField(icon = Icons.Default.Cake, label = "Tuổi", value = state.age, onValueChange = {}, editable = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    
                    // Gender Dropdown
                    SelectField(
                        icon = Icons.Default.Wc,
                        label = "Giới tính",
                        value = state.gender,
                        options = listOf("MALE" to "Nam", "FEMALE" to "Nữ", "OTHER" to "Khác"),
                        editable = state.isEditing,
                        onSelect = { viewModel.onEvent(ProfileEvent.GenderChanged(it)) }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    
                    // BloodType Dropdown
                    SelectField(
                        icon = Icons.Default.Bloodtype,
                        label = "Nhóm máu",
                        value = state.bloodType,
                        options = listOf(
                            "A_POSITIVE" to "A+", "A_NEGATIVE" to "A-",
                            "B_POSITIVE" to "B+", "B_NEGATIVE" to "B-",
                            "AB_POSITIVE" to "AB+", "AB_NEGATIVE" to "AB-",
                            "O_POSITIVE" to "O+", "O_NEGATIVE" to "O-",
                            "UNKNOWN" to "Chưa rõ"
                        ),
                        editable = state.isEditing,
                        onSelect = { viewModel.onEvent(ProfileEvent.BloodTypeChanged(it)) }
                    )
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
                    SettingsRowSwitch(icon = Icons.Default.Medication, iconBg = Color(0xFFF0F9FF), iconTint = Color(0xFF0EA5E9), label = "Nhắc uống thuốc", checked = state.medReminder, onCheckedChange = { viewModel.onEvent(ProfileEvent.MedReminderChanged(it)) })
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowSwitch(icon = Icons.Default.CalendarMonth, iconBg = Color(0xFFFDF2F8), iconTint = Color(0xFFDB2777), label = "Nhắc lịch tái khám", checked = state.apptReminder, onCheckedChange = { viewModel.onEvent(ProfileEvent.ApptReminderChanged(it)) })
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
                    if (state.role == "USER") {
                        SettingsRow(icon = Icons.Default.Verified, iconBg = Color(0xFFECFDF5), iconTint = Color(0xFF16A34A), label = "Xác thực Bác sĩ", onClick = onNavigateToDoctorVerification)
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                    if (state.role == "DOCTOR") {
                        SettingsRow(icon = Icons.Default.LocalHospital, iconBg = Color(0xFFEFF6FF), iconTint = Color(0xFF3B82F6), label = "Phòng khám của tôi", onClick = onNavigateToDoctorWorkspace)
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                    SettingsRow(icon = Icons.Default.History, iconBg = Color(0xFFF0FDF4), iconTint = Color(0xFF22C55E), label = "Lịch sử đặt khám", onClick = onNavigateToPatientBookingCenter)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.Language, iconBg = Color(0xFFF5F3FF), iconTint = Color(0xFF7C3AED), label = "Ngôn ngữ", value = "Tiếng Việt", enabled = false)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRow(icon = Icons.Default.Security, iconBg = Color(0xFFF0FDFA), iconTint = Color(0xFF0D9488), label = "Chính sách bảo mật", onClick = onNavigateToPolicy)
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
private fun MissingHealthProfileCard(
    onNavigateToFamilySetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Chưa có hồ sơ sức khỏe đang hoạt động",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cần chọn hoặc tạo hồ sơ trong phần Gia đình trước khi xem hồ sơ y tế, lịch khám hoặc tiêm chủng.",
                fontSize = 13.sp,
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(14.dp))
            TextButton(onClick = onNavigateToFamilySetup, contentPadding = PaddingValues(0.dp)) {
                Text("Mở Gia đình", fontWeight = FontWeight.Bold, color = PrimaryBlue)
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
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectField(
    icon: ImageVector,
    label: String,
    value: String,
    options: List<Pair<String, String>>, // Pair<Key, DisplayValue>
    editable: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = options.find { it.first == value }?.second ?: value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = editable) { expanded = true }
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
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (editable) expanded = !expanded }
            ) {
                Text(
                    text = displayValue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = editable
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.second) },
                            onClick = {
                                onSelect(option.first)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        if (editable) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFCBD5E1))
        }
    }
}


@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val textColor = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8)
    val chevronColor = if (enabled) Color(0xFFCBD5E1) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
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
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textColor, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 4.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = chevronColor)
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
