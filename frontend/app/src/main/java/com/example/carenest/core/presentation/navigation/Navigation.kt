package com.example.carenest.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
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
import com.example.carenest.feature.auth.presentation.ForgotPasswordScreen
import com.example.carenest.feature.onboarding.presentation.OnboardingScreen
import com.example.carenest.feature.medical.presentation.AppointmentScheduleScreen
import com.example.carenest.feature.medical.presentation.VaccineScheduleScreen
import com.example.carenest.feature.medical.presentation.AddVaccineScreen
import com.example.carenest.feature.notifications.presentation.NotificationsCenterScreen
import com.example.carenest.feature.notifications.presentation.NotificationsCenterViewModel
import com.example.carenest.feature.notifications.presentation.NotificationsCenterViewModelFactory
import com.example.carenest.feature.ekyc.presentation.AdminVerificationScreen
import com.example.carenest.feature.ekyc.presentation.AdminVerificationViewModel
import com.example.carenest.feature.ekyc.presentation.AdminVerificationViewModelFactory
import com.example.carenest.feature.ekyc.presentation.DoctorVerificationScreen
import com.example.carenest.feature.profile.presentation.UserMedicalScreen
import com.example.carenest.feature.profile.presentation.UserMedicalViewModel
import com.example.carenest.feature.profile.presentation.UserMedicalViewModelFactory
import com.example.carenest.feature.profile.presentation.PolicyScreen
import com.example.carenest.feature.ekyc.presentation.EkycViewModel
import com.example.carenest.feature.ekyc.presentation.EkycViewModelFactory
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.dashboard.presentation.DashboardViewModelFactory
import com.example.carenest.feature.medical.presentation.MedicineViewModel
import com.example.carenest.feature.medical.presentation.MedicineViewModelFactory
import com.example.carenest.feature.chat.presentation.ChatScreen

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
  val dashboardViewModel: DashboardViewModel = viewModel(
    factory = DashboardViewModelFactory(
      application.dashboardApi,
      application.authApi,
      application.familyRepository,
      application.secureSessionManager
    )
  )
  val medicineViewModel: MedicineViewModel = viewModel(
    factory = MedicineViewModelFactory(
      application.medicineApi,
      application.secureSessionManager
    )
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
              onItemClick = { backStack.add(it as NavKey) },
              onNavigateToAddMedicine = { backStack.add(AddMedicine) },
              onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
              onNavigateToAddMedicineSchedule = { backStack.add(AddMedicineSchedule) },
              onNavigateToOcrScanner = { backStack.add(OcrScanner) },
              onNavigateToAppointment = { backStack.add(AppointmentSchedule) },
              onNavigateToVaccine = { backStack.add(VaccineSchedule) },
              onNavigateToAppointments = { profileId -> backStack.add(MedicalAppointment(profileId)) },
              onNavigateToVaccinations = { profileId -> backStack.add(VaccinationTracker(profileId)) },
              onNavigateToNotifications = { backStack.add(NotificationsCenter) },
              onNavigateToDoctorVerification = { backStack.add(DoctorVerification) },
              onNavigateToAdminVerification = { backStack.add(AdminVerification) },
              onNavigateToPolicy = { backStack.add(Policy) },
              onNavigateToMedicalRecord = { profileId -> backStack.add(UserMedical(profileId)) },
              onLogout = {
                  scope.launch {
                      application.secureSessionManager.clearAll()
                      backStack.clear()
                      backStack.add(Login)
                  }
              },
              dashboardViewModel = dashboardViewModel,
              medicineViewModel = medicineViewModel,
              modifier = Modifier
          )
        }
        entry<AddMedicine> {
          AddMedicineScreen(
            viewModel = medicineViewModel,
            onBack = { backStack.removeLastOrNull() },
            onOpenOcrScanner = { backStack.add(OcrScanner) }
          )
        }
        entry<MedicineSchedule> {
          MedicineScheduleScreen(
            onBack = { backStack.removeLastOrNull() },
            onAddSchedule = { backStack.add(AddMedicineSchedule) },
            viewModel = medicineViewModel
          )
        }
        entry<AddMedicineSchedule> {
          AddMedicineScheduleScreen(
            dashboardViewModel = dashboardViewModel,
            medicineViewModel = medicineViewModel,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<ChatRoom> {
          val key = it as ChatRoom
          ChatScreen(
            groupId = key.id,
            groupName = key.name,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<OcrScanner> {
          OcrScannerScreen(
            dashboardViewModel = dashboardViewModel,
            medicineViewModel = medicineViewModel,
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
        entry<NotificationsCenter> {
            val viewModel: NotificationsCenterViewModel = viewModel(
                factory = NotificationsCenterViewModelFactory(application.notificationApi)
            )
            NotificationsCenterScreen(
                profileId = null,
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<AdminVerification> {
            val viewModel: AdminVerificationViewModel = viewModel(
                factory = AdminVerificationViewModelFactory(application.ekycRepository)
            )
            AdminVerificationScreen(
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<DoctorVerification> {
            val viewModel: EkycViewModel = viewModel(
                factory = EkycViewModelFactory(application.ekycRepository)
            )
            DoctorVerificationScreen(
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<UserMedical> {
            val key = it as UserMedical
            val viewModel: UserMedicalViewModel = viewModel(
                factory = UserMedicalViewModelFactory(application.familyRepository)
            )
            UserMedicalScreen(
                profileId = key.profileId,
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() },
                onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
                onNavigateToAppointmentList = { backStack.add(MedicalAppointment(key.profileId.toLong())) },
                onNavigateToVaccinationTracker = { backStack.add(VaccinationTracker(key.profileId.toLong())) }
            )
        }
        entry<Policy> {
            PolicyScreen(
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
