package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.domain.model.ChatGroup

private enum class CommunityTopTab(val label: String) {
    WIKI("Cẩm nang"),
    GROUPS("Hội nhóm"),
}

@Composable
fun CommunityScreen(
    canCreateArticle: Boolean = false,
    refreshTrigger: Int = 0,
    onOpenGroup: (ChatGroup) -> Unit = {},
    onOpenGroupPosts: (ChatGroup) -> Unit = {},
    onNavigateToDoctorProfile: (Long) -> Unit = {}
) {
    var activeTabName by rememberSaveable { mutableStateOf(CommunityTopTab.WIKI.name) }
    val activeTab = try {
        CommunityTopTab.valueOf(activeTabName)
    } catch (e: Exception) {
        CommunityTopTab.WIKI
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CommunityTopTab.entries.forEach { tab ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTabName = tab.name }
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
        }

        when (activeTab) {
            CommunityTopTab.WIKI -> CommunityWikiScreen(
                canCreateArticle = canCreateArticle,
                refreshTrigger = refreshTrigger,
                onOpenGroup = onOpenGroup,
                onOpenGroupPosts = onOpenGroupPosts,
                onNavigateToDoctorProfile = onNavigateToDoctorProfile
            )

            CommunityTopTab.GROUPS -> SocialGroupsPane(
                onOpenGroup = { socialGroup ->
                    onOpenGroup(
                        ChatGroup(
                            id = socialGroup.id,
                            name = socialGroup.name,
                            description = socialGroup.description ?: "",
                            category = socialGroup.category ?: "",
                            tags = "",
                            private = false,
                        )
                    )
                },
                onOpenGroupPosts = { socialGroup ->
                    onOpenGroupPosts(
                        ChatGroup(
                            id = socialGroup.id,
                            name = socialGroup.name,
                            description = socialGroup.description ?: "",
                            category = socialGroup.category ?: "",
                            tags = "",
                            private = false,
                        )
                    )
                }
            )
        }
    }
}
