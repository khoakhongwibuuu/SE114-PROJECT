package com.example.carenest

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.carenest.ui.auth.LoginScreen
import com.example.carenest.ui.auth.RegisterScreen
import com.example.carenest.ui.main.MainScreen
import com.example.carenest.ui.medical.AddMedicineScreen
import com.example.carenest.viewmodel.AuthViewModel
import com.example.carenest.viewmodel.AuthViewModelFactory

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

  // Setup ViewModel using Context
  val context = LocalContext.current
  val application = context.applicationContext as CareNestApplication
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModelFactory(application.authApi, application.dataStoreManager)
  )

  val familyRepository = com.example.carenest.data.FamilyRepository(application.familyApi, application.dataStoreManager)
  val familyViewModel: com.example.carenest.viewmodel.FamilyViewModel = viewModel(
    factory = com.example.carenest.viewmodel.FamilyViewModelFactory(familyRepository)
  )
  val profileViewModel: com.example.carenest.viewmodel.ProfileViewModel = viewModel(
    factory = com.example.carenest.viewmodel.ProfileViewModelFactory(familyRepository)
  )
  val dashboardViewModel: com.example.carenest.viewmodel.DashboardViewModel = viewModel(
    factory = com.example.carenest.viewmodel.DashboardViewModelFactory(application.dashboardApi, application.dataStoreManager)
  )

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(
            viewModel = authViewModel,
            onNavigateToRegister = { backStack.add(Register) },
            onLoginSuccess = { 
                backStack.clear()
                backStack.add(MainDashboard) 
            }
          )
        }
        entry<Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }
        entry<MainDashboard> {
          MainScreen(
              onItemClick = { /* noop for now */ },
              onNavigateToAddMedicine = { backStack.add(AddMedicine) },
              dashboardViewModel = dashboardViewModel,
              familyViewModel = familyViewModel,
              profileViewModel = profileViewModel,
              modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<AddMedicine> {
          AddMedicineScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<FamilyPicker> {
            com.example.carenest.ui.family.FamilyPickerScreen(
                viewModel = familyViewModel,
                onNavigateToManagement = { mode -> backStack.add(FamilyManagement(mode)) }
            )
        }
        entry<FamilyManagement> {
            val key = it as FamilyManagement
            com.example.carenest.ui.family.FamilyManagementScreen(
                viewModel = familyViewModel,
                mode = key.mode,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<FamilyList> {
            com.example.carenest.ui.family.FamilyListScreen(
                viewModel = familyViewModel,
                onNavigateToChat = { id, name -> backStack.add(ChatRoom(id, name)) }
            )
        }
        entry<HealthProfileDetail> {
            val key = it as HealthProfileDetail
            com.example.carenest.ui.medical.HealthProfileDetailScreen(
                viewModel = profileViewModel,
                memberId = key.memberId,
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
