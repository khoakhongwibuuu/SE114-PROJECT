package com.example.carenest.feature.family.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.medical.presentation.MedicineScreen
import com.example.carenest.feature.medical.presentation.MedicineViewModel

private enum class FamilyTab(val label: String) {
    MEMBERS("Th\u00e0nh vi\u00ean"),
    MEDICINE("T\u1ee7 thu\u1ed1c"),
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication

    val familyViewModel: FamilyViewModel = viewModel(
        factory = FamilyViewModelFactory(application.familyRepository)
    )

    var activeTab by remember { mutableStateOf(FamilyTab.MEMBERS) }
    var currentScreen by remember { mutableStateOf("picker") }
    var managementMode by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = activeTab == FamilyTab.MEMBERS && currentScreen != "picker") {
        currentScreen = "picker"
    }

    LaunchedEffect(refreshTrigger, activeTab) {
        if (activeTab == FamilyTab.MEMBERS) {
            familyViewModel.loadFamilies()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (activeTab) {
                FamilyTab.MEMBERS -> {
                    when (currentScreen) {
                        "picker" -> {
                            FamilyPickerScreen(
                                viewModel = familyViewModel,
                                onNavigateToManagement = { mode ->
                                    managementMode = mode
                                    currentScreen = "management"
                                }
                            )
                        }

                        "management" -> {
                            FamilyManagementScreen(
                                viewModel = familyViewModel,
                                mode = managementMode,
                                onBack = { currentScreen = "picker" }
                            )
                        }
                    }
                }

                FamilyTab.MEDICINE -> {
                    MedicineScreen(
                        viewModel = medicineViewModel,
                        refreshTrigger = refreshTrigger,
                        onAddMedicineClick = onNavigateToAddMedicine,
                        onScheduleClick = onNavigateToMedicineSchedule,
                        onAddScheduleClick = onNavigateToAddSchedule,
                        onOcrClick = onNavigateToOcrScanner,
                    )
                }
            }
        }
    }
}
