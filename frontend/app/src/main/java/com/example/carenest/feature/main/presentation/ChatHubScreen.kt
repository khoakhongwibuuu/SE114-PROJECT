package com.example.carenest.feature.main.presentation

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.chat.presentation.ConsultationInboxPane

private enum class ChatHubTab(val label: String) {
    AI("AI Care"),
    DOCTOR("Bác sĩ"),
}

@Composable
fun ChatHubScreen(
    onNavigateToConsultationRoom: (Long) -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(ChatHubTab.DOCTOR) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground),
        ) {
            ChatHubTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = tab }
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        style = CareNestTextStyles.labelMd,
                        color = if (activeTab == tab) PrimaryBlue else Outline,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (activeTab == tab) PrimaryBlue else Color.Transparent),
                    )
                }
            }
        }

        when (activeTab) {
            ChatHubTab.AI -> AiCareDisabledPane(onOpenDoctorInbox = { activeTab = ChatHubTab.DOCTOR })
            ChatHubTab.DOCTOR -> ConsultationInboxPane(onNavigateToConsultationRoom = onNavigateToConsultationRoom)
        }
    }
}

@Composable
private fun AiCareDisabledPane(onOpenDoctorInbox: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground)
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CareNestIcon(
            name = "chat",
            contentDescription = "AI Care",
            tint = PrimaryBlue,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = "AI Care đang tạm tắt trong MVP",
            style = CareNestTextStyles.titleMd,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "Tính năng AI sẽ được hoàn thiện ở phase cuối với backend thật và kiểm chứng y tế rõ ràng. Trong MVP, hãy dùng luồng tư vấn bác sĩ và thông báo thật.",
            style = CareNestTextStyles.bodyMd.copy(lineHeight = 22.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppSpacing.lg)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        Button(
            onClick = onOpenDoctorInbox,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.md),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Mở tư vấn bác sĩ",
                style = CareNestTextStyles.labelMd,
                color = Color.White
            )
        }
    }
}
