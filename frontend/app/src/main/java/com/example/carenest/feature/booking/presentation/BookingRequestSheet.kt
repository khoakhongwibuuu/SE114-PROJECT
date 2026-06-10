package com.example.carenest.feature.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.booking.domain.model.BookingRequestType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestSheet(
    doctorId: Long,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (doctorId: Long, requestType: BookingRequestType, note: String, preferredTimeNote: String?) -> Unit,
    isLoading: Boolean
) {
    var selectedType by remember { mutableStateOf(BookingRequestType.ONLINE_CHAT) }
    var note by remember { mutableStateOf("") }
    var preferredTime by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Đặt lịch khám",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Request Type Selection
            Text("Hình thức khám", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeOption(
                    title = "Tư vấn trực tuyến",
                    description = "Qua chat/video",
                    isSelected = selectedType == BookingRequestType.ONLINE_CHAT,
                    onClick = { selectedType = BookingRequestType.ONLINE_CHAT },
                    modifier = Modifier.weight(1f)
                )
                TypeOption(
                    title = "Khám trực tiếp",
                    description = "Tại phòng khám",
                    isSelected = selectedType == BookingRequestType.OFFLINE_CLINIC,
                    onClick = { selectedType = BookingRequestType.OFFLINE_CLINIC },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note (Required)
            Text("Mô tả triệu chứng / Lý do khám *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Mô tả ngắn gọn tình trạng của bạn...", color = Color(0xFF94A3B8)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preferred Time (Optional)
            Text("Thời gian mong muốn (Không bắt buộc)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = preferredTime,
                onValueChange = { preferredTime = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ví dụ: Sáng thứ 3, hoặc sau 17h...", color = Color(0xFF94A3B8)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (note.isNotBlank()) {
                        onSubmitRequest(doctorId, selectedType, note, preferredTime.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = !isLoading && note.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Gửi yêu cầu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TypeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) Color(0xFFF0F9FF) else Color.White

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isSelected) PrimaryBlue else Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(description, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}
