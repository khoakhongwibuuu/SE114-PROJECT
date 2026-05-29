package com.example.carenest.feature.medical.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Giáº£ láº­p model members nhÆ° bÃªn Dashboard
data class SimpleMember(val id: String, val name: String)
val dummyMembers = listOf(
    SimpleMember("1", "Bá»‘"),
    SimpleMember("2", "Máº¹"),
    SimpleMember("3", "Con GÃ¡i")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalScreen(
    onBack: () -> Unit = {}
) {
    var selectedMemberId by remember { mutableStateOf<String?>(dummyMembers.first().id) }
    var facility by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val canSubmit = selectedMemberId != null && facility.isNotBlank() && doctor.isNotBlank() && !isSaving

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lịch hẹn mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Header Intro
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "HEALTHCARE SCHEDULING",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue,
                letterSpacing = 1.sp
            )
            Text(
                "Táº¡o lá»‹ch háº¹n má»›i",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "Sáº¯p xáº¿p cÃ¡c buá»•i khÃ¡m bá»‡nh cá»§a gia Ä‘Ã¬nh báº¡n vá»›i dá»¯ liá»‡u tháº­t tá»« há»‡ thá»‘ng CareNest.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // ThÃ nh viÃªn
            SectionLabel("THÃ€NH VIÃŠN GIA ÄÃŒNH")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                items(dummyMembers) { member ->
                    MemberSelectCard(
                        member = member,
                        isSelected = selectedMemberId == member.id,
                        onClick = { selectedMemberId = member.id }
                    )
                }
            }

            // PhÃ²ng khÃ¡m & BÃ¡c sÄ©
            SectionLabel("PHÃ’NG KHÃM / Bá»†NH VIá»†N")
            InputCard(
                value = facility,
                onValueChange = { facility = it },
                placeholder = "TÃªn phÃ²ng khÃ¡m...",
                icon = Icons.Default.LocalHospital
            )
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("BÃC SÄ¨ CHUYÃŠN KHOA")
            InputCard(
                value = doctor,
                onValueChange = { doctor = it },
                placeholder = "TÃªn bÃ¡c sÄ©...",
                icon = Icons.Default.MedicalServices
            )
            Spacer(modifier = Modifier.height(24.dp))

            // NgÃ y giá»
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("NGÃ€Y KHÃM")
                    ReadOnlyInputCard(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date()),
                        icon = Icons.Default.CalendarToday,
                        onClick = { /* TODO: Show DatePicker */ }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("GIá»œ KHÃM")
                    ReadOnlyInputCard(
                        value = SimpleDateFormat("HH:mm", Locale.forLanguageTag("vi-VN")).format(Date()),
                        icon = Icons.Default.AccessTime,
                        onClick = { /* TODO: Show TimePicker */ }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Äá»‹a chá»‰
            SectionLabel("Äá»ŠA CHá»ˆ")
            InputCard(
                value = address,
                onValueChange = { address = it },
                placeholder = "Äá»‹a chá»‰ phÃ²ng khÃ¡m...",
                icon = Icons.Default.LocationOn
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Ghi chÃº
            SectionLabel("GHI CHÃš THÄ‚M KHÃM")
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Ghi chÃº cÃ¡c triá»‡u chá»©ng hoáº·c Ä‘iá»u cáº§n há»i bÃ¡c sÄ©...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x66E2E8F0),
                    focusedContainerColor = Color(0x66E2E8F0),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedBorderColor = PrimaryBlue,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // NÃºt LÆ°u (Chá»‘ng double-submit)
            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true
                    coroutineScope.launch {
                        delay(2000) // Giáº£ láº­p call API
                        snackbarHostState.showSnackbar("ÄÃ£ lÆ°u thÃ nh cÃ´ng!")
                        facility = ""
                        doctor = ""
                        address = ""
                        notes = ""
                        isSaving = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3498DB),
                    disabledContainerColor = Color(0xFF3498DB).copy(alpha = 0.5f)
                ),
                enabled = canSubmit
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("LÆ°u lá»‹ch háº¹n", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Text(
                "Lá»‹ch háº¹n nÃ y sáº½ Ä‘Æ°á»£c Ä‘á»“ng bá»™ vá»›i há»“ sÆ¡ sá»©c khá»e Ä‘Ã£ chá»n.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF475569),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun MemberSelectCard(member: SimpleMember, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0x052563EB) else Color.White
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9)
    val avatarBgColor = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFDBEAFE)

    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(avatarBgColor)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) PrimaryBlue else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = member.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) PrimaryBlue else Color(0xFF64748B)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputCard(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF94A3B8), fontSize = 14.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0x66E2E8F0),
            focusedContainerColor = Color(0x66E2E8F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedTextColor = TextPrimary,
            focusedTextColor = TextPrimary
        ),
        singleLine = true
    )
}

@Composable
fun ReadOnlyInputCard(value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66E2E8F0))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
