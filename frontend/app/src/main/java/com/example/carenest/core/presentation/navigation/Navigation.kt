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
import com.example.carenest.feature.medical.presentation.MedicalScreen
import com.example.carenest.feature.medical.presentation.MedicineScheduleScreen
import com.example.carenest.feature.medical.presentation.OcrScannerScreen
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.auth.presentation.AuthViewModelFactory
import com.example.carenest.feature.auth.presentation.ForgotPasswordScreen
import com.example.carenest.feature.onboarding.presentation.OnboardingScreen
import com.example.carenest.feature.medical.presentation.AppointmentScheduleScreen
import com.example.carenest.feature.medical.presentation.VaccineScheduleScreen
import com.example.carenest.feature.medical.presentation.AddVaccineScreen

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

  // Setup ViewModels using Context
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModelFactory(application.authApi, application.secureSessionManager)
  )
  val vaccinationViewModel: com.example.carenest.feature.health.presentation.VaccinationViewModel = viewModel(
    factory = com.example.carenest.feature.health.presentation.VaccinationViewModelFactory(application.vaccinationApi)
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
            },
            onNavigateToForgotPassword = { backStack.add(ForgotPassword) }
          )
        }
        entry<Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }
        entry<ForgotPassword> {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }
        entry<MedicalAppointment> {
            val key = it as MedicalAppointment
            com.example.carenest.feature.appointment.presentation.AppointmentListScreen(
                profileId = key.profileId,
                onBack = { backStack.removeLastOrNull() },
                onAddAppointment = { backStack.add(AddAppointment(key.profileId)) }
            )
        }
        entry<AddAppointment> {
            val key = it as AddAppointment
            com.example.carenest.feature.appointment.presentation.AddAppointmentScreen(
                profileId = key.profileId,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<VaccinationTracker> {
            val key = it as VaccinationTracker
            com.example.carenest.feature.health.presentation.VaccinationTrackerScreen(
                profileId = key.profileId,
                viewModel = vaccinationViewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onAddVaccination = { profileId -> backStack.add(AddVaccinationSchedule(profileId = profileId)) },
                onEditDose = { profileId, recordId, doseId -> 
                    backStack.add(AddVaccinationSchedule(profileId = profileId, vaccineId = recordId, doseId = doseId)) 
                }
            )
        }
        entry<AddVaccinationSchedule> {
            val key = it as AddVaccinationSchedule
            com.example.carenest.feature.health.presentation.AddVaccinationScheduleScreen(
                profileId = key.profileId,
                vaccineId = key.vaccineId,
                doseId = key.doseId,
                viewModel = vaccinationViewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
        entry<MainDashboard> {
          MainScreen(
              onItemClick = { /* noop for now */ },
              onNavigateToAddMedicine = { backStack.add(AddMedicine) },
              onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
              onNavigateToAddMedicineSchedule = { backStack.add(AddMedicineSchedule) },
              onNavigateToOcrScanner = { backStack.add(OcrScanner) },
              onNavigateToAppointment = { backStack.add(AppointmentSchedule) },
              onNavigateToVaccine = { backStack.add(VaccineSchedule) },
              onNavigateToAppointments = { backStack.add(MedicalAppointment(0L)) },
              onNavigateToVaccinations = { profileId -> backStack.add(VaccinationTracker(profileId)) },
              onLogout = {
                  scope.launch {
                      application.secureSessionManager.clearAll()
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
        entry<AppointmentSchedule> {
            AppointmentScheduleScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<VaccineSchedule> {
            VaccineScheduleScreen(
                onBack = { backStack.removeLastOrNull() },
                onAddVaccine = { profileId, editId -> 
                    backStack.add(AddVaccine(profileId, editId)) 
                }
            )
        }
        entry<AddVaccine> { args ->
            AddVaccineScreen(
                profileId = args.profileId,
                editVaccineId = args.editVaccineId,
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
