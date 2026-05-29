package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.CommunityGroup

private enum class CommunityTopTab(val label: String) {
    WIKI("Cẩm nang"),
    GROUPS("Hội nhóm"),
}

@Composable
fun CommunityScreen(
    onOpenGroup: (CommunityGroup) -> Unit = {},
) {
    var activeTab by remember { mutableStateOf(CommunityTopTab.WIKI) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
        ) {
            androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
                CommunityTopTab.entries.forEach { tab ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = tab }
                            .padding(top = 14.dp, bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeTab == tab) PrimaryBlue else Color(0xFF707882),
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(if (activeTab == tab) PrimaryBlue else Color.Transparent),
                        )
                    }
                }
            }
        }

        when (activeTab) {
            CommunityTopTab.WIKI -> CommunityWikiScreen()
            CommunityTopTab.GROUPS -> CommunityGroupsPane(onOpenGroup = onOpenGroup)
        }
    }
}
