package com.example.carenest.feature.family.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel

@Composable
fun FamilyFlowScreen(
    viewModel: DashboardViewModel, // If we need it for something else, otherwise unused
    refreshTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    
    val familyViewModel: FamilyViewModel = viewModel(
        factory = FamilyViewModelFactory(application.familyRepository)
    )

    var currentScreen by remember { mutableStateOf("picker") }
    var managementMode by remember { mutableStateOf<String?>(null) } // "create", "join", null

    BackHandler(enabled = currentScreen != "picker") {
        currentScreen = "picker"
    }

    LaunchedEffect(refreshTrigger) {
        familyViewModel.loadFamilies()
    }

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
