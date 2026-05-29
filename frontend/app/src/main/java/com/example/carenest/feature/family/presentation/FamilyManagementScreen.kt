package com.example.carenest.feature.family.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.feature.family.domain.model.FamilyMemberSummary

val JOIN_ROLE_OPTIONS = listOf(
    Pair("Bố", "FATHER"),
    Pair("Mẹ", "MOTHER"),
    Pair("Anh", "OLDER_BROTHER"),
    Pair("Chị", "OLDER_SISTER"),
    Pair("Em", "YOUNGER"),
    Pair("Khác", "OTHER")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementScreen(
    viewModel: FamilyViewModel,
    mode: String?, // "create", "join", or null
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    var currentMode by remember { mutableStateOf(mode) }
    
    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
        viewModel.loadJoinCode()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (currentMode) {
                            "create" -> "Tạo gia đình"
                            "join" -> "Tham gia gia đình"
                            else -> "Quản lý thành viên"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentMode != null) currentMode = null else onBack() 
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (currentMode == "create") {
                CreateFamilyContent(viewModel, onDone = { currentMode = null })
            } else if (currentMode == "join") {
                JoinFamilyContent(viewModel, onDone = { currentMode = null })
            } else {
                ManagementContent(viewModel)
            }
        }
    }
}

@Composable
fun CreateFamilyContent(viewModel: FamilyViewModel, onDone: () -> Unit) {
    var familyName by remember { mutableStateOf("") }
    
    Column {
        Text("Tên gia đình", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("VD: Tổ ấm thân thương") },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                viewModel.createFamily(familyName)
                onDone()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tạo gia đình", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun JoinFamilyContent(viewModel: FamilyViewModel, onDone: () -> Unit) {
    var joinCode by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF0369A1))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tham gia gia đình", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Nhập mã để tham gia", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))
        
        OutlinedTextField(
            value = joinCode,
            onValueChange = { joinCode = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nhập mã gia đình") },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Role Selector
        Text("Vai trò của bạn", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(JOIN_ROLE_OPTIONS) { role ->
                val isSelected = selectedRole == role.second
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF0369A1) else Color(0xFFF1F5F9))
                        .clickable { selectedRole = role.second }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = role.first,
                        color = if (isSelected) Color.White else Color(0xFF64748B),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                viewModel.joinFamilyByCode(joinCode, selectedRole)
                onDone()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tham gia", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("HOẶC", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { /* TODO: Launch QR Scanner intent */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF0369A1))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quét mã QR", color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManagementContent(viewModel: FamilyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }

    // Family Members
    uiState.activeFamily?.let { familyDetail ->
        Text("Thành viên gia đình", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        familyDetail.members.forEach { member ->
            FamilyMemberRow(member)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Received Invitations
    if (uiState.receivedInvitations.isNotEmpty()) {
        Text("Lời mời bạn đã nhận", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        uiState.receivedInvitations.forEach { invite ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(invite.name ?: "Gia đình", fontWeight = FontWeight.Bold)
                        Text(invite.senderEmail ?: "", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row {
                        Button(
                            onClick = { viewModel.handleInvitation(invite.inviteId, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text("Nhận", color = Color.White) }
                        OutlinedButton(
                            onClick = { viewModel.handleInvitation(invite.inviteId, false) }
                        ) { Text("Từ chối") }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Invite Section
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("THÊM THÀNH VIÊN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = inviteEmail,
                onValueChange = { inviteEmail = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("vidu@email.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            // Role Selector for Invite
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(JOIN_ROLE_OPTIONS) { role ->
                    val isSelected = selectedRole == role.second
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
                            .clickable { selectedRole = role.second }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = role.first,
                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    viewModel.inviteMember(inviteEmail, selectedRole)
                    inviteEmail = ""
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gửi lời mời", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
            
            // QR Code generation for owner
            uiState.joinCodeInfo?.let { codeInfo ->
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("QR Tham gia", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                
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
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)).padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(codeInfo.joinCode, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color(0xFF0369A1))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Sent Invitations
    Text("Lời mời đang chờ", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
    if (uiState.sentInvitations.isEmpty()) {
        Text("Chưa có lời mời nào", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
    } else {
        uiState.sentInvitations.forEach { invite ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(invite.receiverEmail?.take(1)?.uppercase() ?: "?", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(invite.receiverEmail ?: "Người thân", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (invite.status == "PENDING") "Đang chờ xác nhận" else "Đã xử lý", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FamilyMemberRow(member: FamilyMemberSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(member.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Text(member.role, fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
    }
}
