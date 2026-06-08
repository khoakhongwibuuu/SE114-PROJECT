package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.TextSecondary

@Composable
fun FamilyChatPane() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bạn chưa có cuộc trò chuyện gia đình nào.",
                        style = CareNestTextStyles.bodyMd,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
