package com.example.carenest.core.presentation.navigation

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
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.auth.presentation.LoginScreen
import com.example.carenest.feature.auth.presentation.RegisterScreen
import com.example.carenest.feature.main.presentation.MainScreen
import com.example.carenest.feature.medical.presentation.AddMedicineScreen
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.auth.presentation.AuthViewModelFactory

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

  // Setup ViewModel using Context
  val context = LocalContext.current
  val application = context.applicationContext as CareNestApplication
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModelFactory(application.authApi, application.secureSessionManager)
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
              modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<AddMedicine> {
          AddMedicineScreen(onBack = { backStack.removeLastOrNull() })
        }
      },
  )
}
