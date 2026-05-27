package com.example.carenest.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.auth.presentation.LoginScreen
import com.example.carenest.feature.auth.presentation.RegisterScreen
import com.example.carenest.feature.main.presentation.MainScreen
import com.example.carenest.feature.medical.presentation.AddMedicineScreen
import com.example.carenest.feature.medical.presentation.AddMedicineScheduleScreen
import com.example.carenest.feature.medical.presentation.MedicineScheduleScreen
import com.example.carenest.feature.medical.presentation.OcrScannerScreen
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.auth.presentation.AuthViewModelFactory
import com.example.carenest.feature.onboarding.presentation.OnboardingScreen
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val application = context.applicationContext as CareNestApplication
  val startDestination = remember {
    if (application.secureSessionManager.isOnboardingDone()) {
      if (application.secureSessionManager.getAccessToken()?.isNotBlank() == true) {
        MainDashboard
      } else {
        Login
      }
    } else {
      Onboarding
    }
  }
  val backStack = rememberNavBackStack(startDestination)
  val scope = rememberCoroutineScope()

  // Setup ViewModel using Context
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModelFactory(application.authApi, application.secureSessionManager)
  )

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Onboarding> {
          OnboardingScreen(
            onComplete = {
              scope.launch {
                application.secureSessionManager.completeOnboarding()
                backStack.clear()
                backStack.add(Login)
              }
            }
          )
        }
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
              onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
              onNavigateToAddMedicineSchedule = { backStack.add(AddMedicineSchedule) },
              onNavigateToOcrScanner = { backStack.add(OcrScanner) },
              onLogout = {
                  scope.launch {
                      application.secureSessionManager.logout()
                      backStack.clear()
                      backStack.add(Login)
                  }
              },
              modifier = Modifier
          )
        }
        entry<AddMedicine> {
          AddMedicineScreen(
            onBack = { backStack.removeLastOrNull() },
            onOpenOcrScanner = { backStack.add(OcrScanner) }
          )
        }
        entry<MedicineSchedule> {
          MedicineScheduleScreen(
            onBack = { backStack.removeLastOrNull() },
            onAddSchedule = { backStack.add(AddMedicineSchedule) }
          )
        }
        entry<AddMedicineSchedule> {
          AddMedicineScheduleScreen(
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<OcrScanner> {
          OcrScannerScreen(
            onBack = { backStack.removeLastOrNull() }
          )
        }
      },
  )
}
