package com.example.carenest.feature.family.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.FamilyChatDirectoryPane
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.family.domain.model.FamilySummary
import com.example.carenest.feature.medical.presentation.MedicineScreen
import com.example.carenest.feature.medical.presentation.MedicineViewModel

private enum class FamilyTab(val label: String) {
    MEMBERS("Thành viên"),
    MEDICINE("Tủ thuốc"),
    CHAT("Trò chuyện"),
}

@Composable
fun FamilyFlowScreen(
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
    refreshTrigger: Int = 0,
    onNavigateToAddMedicine: () -> Unit = {},
    onNavigateToMedicineSchedule: () -> Unit = {},
    onNavigateToAddSchedule: () -> Unit = {},
    onNavigateToOcrScanner: () -> Unit = {},
    onOpenFamilyChat: (FamilySummary) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val familyViewModel: FamilyViewModel = viewModel(
        factory = FamilyViewModelFactory(application.familyRepository),
    )
    val familyUiState by familyViewModel.uiState.collectAsState()

    var activeTabName by rememberSaveable { mutableStateOf(FamilyTab.MEMBERS.name) }
    val activeTab = try {
        FamilyTab.valueOf(activeTabName)
    } catch (e: Exception) {
        FamilyTab.MEMBERS
    }

    var currentScreen by rememberSaveable { mutableStateOf("picker") }
    var managementMode by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = activeTab == FamilyTab.MEMBERS && currentScreen != "picker") {
        currentScreen = "picker"
    }
    BackHandler(enabled = activeTab == FamilyTab.MEDICINE || activeTab == FamilyTab.CHAT) {
        activeTabName = FamilyTab.MEMBERS.name
    }

    LaunchedEffect(refreshTrigger, activeTab) {
        if (activeTab == FamilyTab.MEMBERS) {
            familyViewModel.loadFamilies()
        }
    }

    val activeFamilyId = familyUiState.activeFamily?.id ?: familyUiState.activeFamilyId
    val activeFamilySummary = familyUiState.myFamilies.firstOrNull { it.id == activeFamilyId }

    fun openTab(tab: FamilyTab) {
        val requiresFamily = tab == FamilyTab.MEDICINE || tab == FamilyTab.CHAT
        if (requiresFamily && familyUiState.myFamilies.isEmpty()) {
            Toast.makeText(
                context,
                "Hãy tạo hoặc tham gia gia đình trước khi dùng tính năng này.",
                Toast.LENGTH_SHORT,
            ).show()
            activeTabName = FamilyTab.MEMBERS.name
            currentScreen = "picker"
            return
        }
        activeTabName = tab.name
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground),
        ) {
            FamilyTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openTab(tab) }
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            when (activeTab) {
                FamilyTab.MEMBERS -> {
                    when (currentScreen) {
                        "picker" -> FamilyPickerScreen(
                            viewModel = familyViewModel,
                            onNavigateToManagement = { mode ->
                                managementMode = mode
                                currentScreen = "management"
                            },
                        )

                        "management" -> FamilyManagementScreen(
                            viewModel = familyViewModel,
                            mode = managementMode,
                            onBack = { currentScreen = "picker" },
                        )
                    }
                }

                FamilyTab.MEDICINE -> MedicineScreen(
                    viewModel = medicineViewModel,
                    refreshTrigger = refreshTrigger,
                    onAddMedicineClick = onNavigateToAddMedicine,
                    onScheduleClick = onNavigateToMedicineSchedule,
                    onAddScheduleClick = onNavigateToAddSchedule,
                    onOcrClick = onNavigateToOcrScanner,
                )

                FamilyTab.CHAT -> FamilyChatDirectoryPane(
                    families = familyUiState.myFamilies,
                    activeFamilyId = activeFamilyId,
                    onOpenMembersTab = { activeTabName = FamilyTab.MEMBERS.name },
                    onSelectFamily = { family ->
                        familyViewModel.selectFamily(family.id)
                        onOpenFamilyChat(family)
                    },
                )
            }
        }
    }
}
