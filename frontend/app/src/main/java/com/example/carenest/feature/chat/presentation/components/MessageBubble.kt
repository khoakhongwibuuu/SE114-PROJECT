package com.example.carenest.feature.chat.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChatBlueSelf = Color(0xFF1A73E8)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    msg: ChatMessage,
    onLongClick: (ChatMessage) -> Unit = {},
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
    val isDoctor = msg.senderRole == "DOCTOR"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start,
    ) {
        if (!msg.isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDoctor) Color(0xFFE0F2FE) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    msg.senderName.take(1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f),
        ) {
            if (!msg.isMe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                ) {
                    Text(
                        msg.senderName,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                    )
                    if (isDoctor) {
                        DoctorBadge()
                    }
                }
            }

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (msg.isMe) 18.dp else 4.dp,
                            bottomEnd = if (msg.isMe) 4.dp else 18.dp,
                        ),
                    )
                    .background(
                        if (msg.isMe) {
                            ChatBlueSelf
                        } else if (isDoctor) {
                            Color(0xFFECFEFF)
                        } else {
                            Color(0xFFF1F5F9)
                        },
                    )
                    .combinedClickable(
                        onLongClick = { onLongClick(msg) },
                        onClick = {}
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                msg.replyPreview?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (msg.isMe) Color(0x33111111) else Color.White)
                            .padding(8.dp),
                    ) {
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = if (msg.isMe) Color.White else PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = msg.text,
                    fontSize = 15.sp,
                    color = if (msg.isMe) Color.White else Color(0xFF1E293B),
                    lineHeight = 20.sp,
                )
            }
            Text(
                timeStr,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

@Composable
private fun DoctorBadge() {
    Row(
        modifier = Modifier
            .padding(start = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE0F2FE))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF0EA5E9),
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "Bác sĩ",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0369A1)
        )
    }
}
