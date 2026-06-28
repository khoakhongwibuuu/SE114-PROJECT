package com.example.carenest.feature.family.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.AppElevation
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.PrimaryFixed
import com.example.carenest.core.presentation.theme.SurfaceHigh
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.family.domain.model.FamilyMemberSummary

val JOIN_ROLE_OPTIONS = listOf(
    "Thành viên" to "MEMBER",
    "Bố" to "FATHER",
    "Mẹ" to "MOTHER",
    "Anh" to "OLDER_BROTHER",
    "Chị" to "OLDER_SISTER",
    "Em" to "YOUNGER",
    "Khác" to "OTHER"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementScreen(
    viewModel: FamilyViewModel,
    mode: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var currentMode by remember { mutableStateOf(mode) }

    LaunchedEffect(currentMode, uiState.activeFamilyId) {
        viewModel.loadInvitations()
        if (currentMode == null && uiState.activeFamilyId != null) {
            viewModel.loadJoinCode()
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentMode) {
                            "create" -> "Tạo gia đình"
                            "join" -> "Tham gia gia đình"
                            else -> "Quản lý thành viên"
                        },
                        style = CareNestTextStyles.titleLg,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentMode != null) currentMode = null else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(AppSpacing.lg)
        ) {
            FamilyFeedback(
                error = uiState.error,
                message = uiState.message,
                onDismissError = viewModel::clearError,
                onDismissMessage = viewModel::clearMessage
            )
            when (currentMode) {
                "create" -> CreateFamilyContent(viewModel, onDone = { currentMode = null })
                "join" -> JoinFamilyContent(viewModel, onDone = { currentMode = null })
                else -> ManagementContent(viewModel)
            }
        }
    }
}

@Composable
private fun FamilyFeedback(
    error: String?,
    message: String?,
    onDismissError: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val text = error ?: message ?: return
    val isError = error != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.md),
        shape = RoundedCornerShape(AppRadius.lg),
        color = if (isError) Color(0xFFFFF1F2) else Color(0xFFF0FDF4)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = CareNestTextStyles.bodyMd,
                color = if (isError) Color(0xFFBE123C) else Color(0xFF15803D),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = if (isError) onDismissError else onDismissMessage) {
                Text("Ẩn")
            }
        }
    }
}

@Composable
fun CreateFamilyContent(viewModel: FamilyViewModel, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var familyName by remember { mutableStateOf("") }
    val canSubmit = familyName.trim().length >= 2 && !uiState.isBusy

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.clearMessage()
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Tạo gia đình thành công") {
            onDone()
        }
    }

    Column {
        Text("Tên gia đình", style = CareNestTextStyles.labelMd, color = TextPrimary)
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ví dụ: Tổ ấm thân thương", style = CareNestTextStyles.bodyMd) },
            shape = RoundedCornerShape(AppRadius.lg)
        )
        if (familyName.isNotBlank() && familyName.trim().length < 2) {
            Text(
                text = "Tên gia đình cần tối thiểu 2 ký tự",
                style = CareNestTextStyles.bodySm,
                color = Color(0xFFBE123C),
                modifier = Modifier.padding(top = AppSpacing.xs)
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.x2))
        Button(
            onClick = { viewModel.createFamily(familyName) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.lg),
            enabled = canSubmit
        ) {
            Text(
                if (uiState.isBusy) "Đang tạo..." else "Tạo gia đình",
                style = CareNestTextStyles.labelLg,
                color = Color.White
            )
        }
    }
}

@Composable
fun JoinFamilyContent(viewModel: FamilyViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var joinCode by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }
    val canSubmit = joinCode.isNotBlank() && !uiState.isBusy
    val qrPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.joinFamilyByQr(context.applicationContext, uri, selectedRole)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.clearMessage()
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Tham gia thành công" || uiState.message == "Tham gia bằng QR thành công") {
            onDone()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = PrimaryBlue)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text("Tham gia gia đình", style = CareNestTextStyles.titleXl, color = TextPrimary)
        Text("Nhập mã để tham gia", style = CareNestTextStyles.bodyMd, color = TextSecondary, modifier = Modifier.padding(bottom = AppSpacing.x2))

        OutlinedTextField(
            value = joinCode,
            onValueChange = { joinCode = it.filterNot(Char::isWhitespace).uppercase().take(12) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nhập mã gia đình", style = CareNestTextStyles.bodyMd) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
            shape = RoundedCornerShape(AppRadius.lg)
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))

        Text("Vai trò của bạn", style = CareNestTextStyles.labelMd, color = TextPrimary, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(JOIN_ROLE_OPTIONS) { role ->
                val isSelected = selectedRole == role.second
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.full))
                        .background(if (isSelected) PrimaryBlue else SurfaceHigh)
                        .clickable { selectedRole = role.second }
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
                ) {
                    Text(
                        text = role.first,
                        style = CareNestTextStyles.labelMd,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.x2))
        Button(
            onClick = { viewModel.joinFamilyByCode(joinCode, selectedRole) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.lg),
            enabled = canSubmit
        ) {
            Text(
                if (uiState.isBusy) "Đang tham gia..." else "Tham gia",
                style = CareNestTextStyles.labelLg,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text("HOẶC", style = CareNestTextStyles.overline, color = Outline)
        Spacer(modifier = Modifier.height(AppSpacing.lg))

        OutlinedButton(
            onClick = { qrPicker.launch("image/*") },
            enabled = !uiState.isBusy,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(AppRadius.lg),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(
                    if (uiState.isBusy) "Đang xử lý..." else "Chọn ảnh QR để tham gia",
                    style = CareNestTextStyles.labelMd
                )
            }
        }
    }
}

@Composable
fun ManagementContent(viewModel: FamilyViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }
    val trimmedInviteEmail = inviteEmail.trim()
    val inviteEmailInvalid = trimmedInviteEmail.isNotBlank() &&
            (!trimmedInviteEmail.contains("@") || !trimmedInviteEmail.contains("."))
    val canInvite = uiState.activeFamilyId != null &&
            trimmedInviteEmail.isNotBlank() &&
            !inviteEmailInvalid &&
            !uiState.isBusy

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Đã gửi lời mời") {
            inviteEmail = ""
        }
    }

    uiState.activeFamily?.let { familyDetail ->
        val adultMembers = familyDetail.members.filter { !it.isChild }
        val dependentMembers = familyDetail.members.filter { it.isChild }

        Text("Thành viên (${adultMembers.size})", style = CareNestTextStyles.titleMd, color = TextPrimary, modifier = Modifier.padding(bottom = AppSpacing.sm))
        adultMembers.forEach { member ->
            FamilyMemberRow(member)
        }
        
        if (dependentMembers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text("Người phụ thuộc / Trẻ em (${dependentMembers.size})", style = CareNestTextStyles.titleMd, color = TextPrimary, modifier = Modifier.padding(bottom = AppSpacing.sm))
            dependentMembers.forEach { member ->
                FamilyMemberRow(member)
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.x2))
    } ?: run {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.x2),
            shape = RoundedCornerShape(AppRadius.lg),
            color = SurfaceHigh
        ) {
            Text(
                text = "Chọn hoặc tạo gia đình để quản lý thành viên và mã mời.",
                style = CareNestTextStyles.bodyMd,
                color = TextSecondary,
                modifier = Modifier.padding(AppSpacing.lg)
            )
        }
    }

    if (uiState.receivedInvitations.isNotEmpty()) {
        Text("Lời mời bạn đã nhận", style = CareNestTextStyles.titleMd, color = TextPrimary, modifier = Modifier.padding(bottom = AppSpacing.sm))
        uiState.receivedInvitations.forEach { invite ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm),
                shape = RoundedCornerShape(AppRadius.lg),
                shadowElevation = AppElevation.sm
            ) {
                Row(modifier = Modifier.padding(AppSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(invite.name ?: "Gia đình", style = CareNestTextStyles.labelLg, color = TextPrimary)
                        Text(invite.senderEmail ?: "", style = CareNestTextStyles.bodySm, color = TextSecondary)
                    }
                    Row {
                        Button(
                            onClick = { viewModel.handleInvitation(invite.inviteId, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.padding(end = AppSpacing.sm)
                        ) { Text("Nhận", color = Color.White) }
                        OutlinedButton(onClick = { viewModel.handleInvitation(invite.inviteId, false) }) {
                            Text("Từ chối")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.x2))
    }

    Surface(
        shape = RoundedCornerShape(AppRadius.xl),
        shadowElevation = AppElevation.sm,
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Text("THÊM THÀNH VIÊN", style = CareNestTextStyles.overline, color = Outline)
            Spacer(modifier = Modifier.height(AppSpacing.md))

            OutlinedTextField(
                value = inviteEmail,
                onValueChange = { inviteEmail = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("vidu@email.com", style = CareNestTextStyles.bodyMd) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(AppRadius.lg),
                isError = inviteEmailInvalid,
                supportingText = {
                    if (inviteEmailInvalid) {
                        Text("Email không đúng định dạng")
                    }
                },
                enabled = uiState.activeFamilyId != null && !uiState.isBusy
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                items(JOIN_ROLE_OPTIONS) { role ->
                    val isSelected = selectedRole == role.second
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadius.xl))
                            .background(if (isSelected) PrimaryFixed else SurfaceHigh)
                            .clickable { selectedRole = role.second }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = role.first,
                            style = CareNestTextStyles.labelSm,
                            color = if (isSelected) PrimaryBlue else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Button(
                onClick = { viewModel.inviteMember(inviteEmail, selectedRole) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(AppRadius.lg),
                enabled = canInvite
            ) {
                Text(
                    if (uiState.isBusy) "Đang gửi..." else "Gửi lời mời",
                    style = CareNestTextStyles.labelLg,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            }

            uiState.joinCodeInfo?.let { codeInfo ->
                Spacer(modifier = Modifier.height(AppSpacing.x2))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Text("QR tham gia", style = CareNestTextStyles.titleMd, color = TextPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                if (!codeInfo.qrCodeBase64.isNullOrEmpty()) {
                    val decodedString = Base64.decode(codeInfo.qrCodeBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.lg)
                        .background(PrimaryFixed, RoundedCornerShape(AppRadius.md))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(codeInfo.joinCode, style = CareNestTextStyles.headlineMd.copy(letterSpacing = 2.sp), color = PrimaryBlue)
                }
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(codeInfo.joinCode))
                        Toast.makeText(context, "Đã sao chép mã mời", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !uiState.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm),
                    shape = RoundedCornerShape(AppRadius.lg)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text("Sao chép mã mời")
                }
                OutlinedButton(
                    onClick = { viewModel.rotateJoinCode() },
                    enabled = !uiState.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm),
                    shape = RoundedCornerShape(AppRadius.lg)
                ) {
                    Text(if (uiState.isBusy) "Đang tạo lại..." else "Tạo lại mã mời")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(AppSpacing.x2))

    Text("Lời mời đang chờ", style = CareNestTextStyles.titleMd, color = TextPrimary, modifier = Modifier.padding(bottom = AppSpacing.sm))
    if (uiState.sentInvitations.isEmpty()) {
        Text("Chưa có lời mời nào", style = CareNestTextStyles.bodyMd, color = TextSecondary, modifier = Modifier.padding(bottom = AppSpacing.lg))
    } else {
        uiState.sentInvitations.forEach { invite ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(invite.receiverEmail?.take(1)?.uppercase() ?: "?", style = CareNestTextStyles.labelMd, color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text(invite.receiverEmail ?: "Người thân", style = CareNestTextStyles.labelMd, color = TextPrimary)
                    Text(
                        invitationStatusLabel(invite.status),
                        style = CareNestTextStyles.bodySm,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyMemberRow(member: FamilyMemberSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm),
        shape = RoundedCornerShape(AppRadius.lg),
        shadowElevation = AppElevation.sm,
        color = CardBackground
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.fullName.take(1).uppercase().ifBlank { "?" },
                    style = CareNestTextStyles.titleLg,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.fullName, style = CareNestTextStyles.labelLg.copy(fontSize = 15.sp), color = TextPrimary)
                Text(familyRoleLabel(member.role), style = CareNestTextStyles.bodySm.copy(fontSize = 13.sp), color = TextSecondary)
            }
            if (member.isEditable) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun familyRoleLabel(role: String?): String {
    return when (role) {
        "OWNER" -> "Chủ hộ"
        "ADMIN" -> "Quản trị viên"
        "FATHER" -> "Bố"
        "MOTHER" -> "Mẹ"
        "OLDER_BROTHER" -> "Anh"
        "OLDER_SISTER" -> "Chị"
        "YOUNGER" -> "Em"
        "OTHER" -> "Khác"
        "MEMBER" -> "Thành viên"
        else -> "Thành viên"
    }
}

private fun invitationStatusLabel(status: String?): String {
    return when (status) {
        "PENDING" -> "Đang chờ xác nhận"
        "ACCEPTED" -> "Đã chấp nhận"
        "REJECTED" -> "Đã từ chối"
        "EXPIRED" -> "Đã hết hạn"
        else -> "Đã xử lý"
    }
}
