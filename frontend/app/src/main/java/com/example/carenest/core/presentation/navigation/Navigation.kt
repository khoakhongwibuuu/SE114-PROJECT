package com.example.carenest.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.carenest.CareNestApplication
import com.example.carenest.feature.admin.presentation.AdminMainScreen
import com.example.carenest.feature.auth.domain.model.AppRole
import com.example.carenest.feature.auth.presentation.LoginScreen
import com.example.carenest.feature.auth.presentation.RegisterScreen
import com.example.carenest.feature.main.presentation.MainScreen
import com.example.carenest.feature.main.presentation.MainTabTarget
import com.example.carenest.feature.medical.presentation.AddMedicineScreen
import com.example.carenest.feature.medical.presentation.AddMedicineScheduleScreen
import com.example.carenest.feature.medical.presentation.MedicineScheduleScreen
import com.example.carenest.feature.medical.presentation.OcrScannerScreen
import com.example.carenest.feature.auth.presentation.AuthViewModel
import com.example.carenest.feature.auth.presentation.AuthViewModelFactory
import com.example.carenest.feature.auth.presentation.ForgotPasswordScreen
import com.example.carenest.feature.onboarding.presentation.OnboardingScreen
import com.example.carenest.feature.notifications.presentation.NotificationsCenterScreen
import com.example.carenest.feature.notifications.presentation.NotificationOpenResult
import com.example.carenest.feature.notifications.presentation.NotificationsCenterViewModel
import com.example.carenest.feature.notifications.presentation.NotificationsCenterViewModelFactory
import com.example.carenest.feature.notifications.domain.model.NotificationItem
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
import android.widget.Toast

import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val application = context.applicationContext as CareNestApplication
  val storedRole = application.secureSessionManager.getUserRole().toAppRole()
  val startDestination = remember {
    if (application.secureSessionManager.isOnboardingDone()) {
      if (application.secureSessionManager.getAccessToken()?.isNotBlank() == true) {
        authenticatedRootFor(storedRole)
      } else {
        Login
      }
    } else {
      Onboarding
    }
  }
  val backStack = rememberNavBackStack(startDestination)
  val scope = rememberCoroutineScope()
  var mainTabTarget by remember { mutableStateOf<MainTabTarget?>(null) }

  // Setup ViewModels using Context
  val authViewModel: AuthViewModel = viewModel(
    factory = AuthViewModelFactory(application.authApi, application.secureSessionManager)
  )
  val currentUserRole by authViewModel.currentUserRole.collectAsState()
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

  fun closeNotificationsCenterIfVisible() {
    if (backStack.lastOrNull() == NotificationsCenter) {
      backStack.removeLastOrNull()
    }
  }

  fun routeToMainTab(target: MainTabTarget) {
    mainTabTarget = target
    closeNotificationsCenterIfVisible()
    if (backStack.lastOrNull() != MainDashboard) {
      backStack.add(MainDashboard)
    }
  }

  val openNotificationTarget: (NotificationItem) -> NotificationOpenResult = { notification ->
    val referenceType = notification.referenceType?.uppercase()
    val referenceId = notification.referenceId
    val role = currentUserRole ?: application.secureSessionManager.getUserRole().toAppRole()

    val handled = when (referenceType) {
      "BOOKING_REQUEST" -> {
        if (referenceId != null && referenceId > 0L && role != AppRole.DOCTOR) {
          closeNotificationsCenterIfVisible()
          backStack.add(PatientBookingCenter)
          NotificationOpenResult.OPENED
        } else if (role == AppRole.DOCTOR) {
          closeNotificationsCenterIfVisible()
          backStack.add(DoctorWorkspace)
          NotificationOpenResult.OPENED
        } else {
          Toast.makeText(context, "Không thể mở thông báo đặt lịch này", Toast.LENGTH_SHORT).show()
          NotificationOpenResult.UNHANDLED
        }
      }
      "DOCTOR_VERIFICATION" -> {
        authViewModel.refreshCurrentUser()
        closeNotificationsCenterIfVisible()
        backStack.add(DoctorVerification)
        NotificationOpenResult.OPENED
      }
      "ADMIN_USER_ROLE", "ADMIN_USER_STATUS" -> {
        authViewModel.refreshCurrentUser()
        Toast.makeText(context, "Đã cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show()
        NotificationOpenResult.CONSUMED
      }
      "FAMILY", "FAMILY_INVITATION", "FAMILY_CHAT" -> {
        routeToMainTab(MainTabTarget.FAMILY)
        NotificationOpenResult.OPENED
      }
      "MEDICATION_LOG" -> {
        closeNotificationsCenterIfVisible()
        backStack.add(MedicineSchedule)
        NotificationOpenResult.OPENED
      }
      "APPOINTMENT" -> {
        routeToMainTab(MainTabTarget.HOME)
        NotificationOpenResult.OPENED
      }
      "GROWTH_RECORD" -> {
        routeToMainTab(MainTabTarget.PROFILE)
        NotificationOpenResult.OPENED
      }
      else -> {
        Toast.makeText(context, "Chưa hỗ trợ mở đích cho thông báo này", Toast.LENGTH_SHORT).show()
        NotificationOpenResult.UNHANDLED
      }
    }
    if (handled == NotificationOpenResult.OPENED) {
      dashboardViewModel.fetchDashboard()
    }
    handled
  }

  LaunchedEffect(currentUserRole, application.secureSessionManager.getAccessToken()) {
    val token = application.secureSessionManager.getAccessToken()
    val currentTop = backStack.lastOrNull()
    if (token.isNullOrBlank()) return@LaunchedEffect

    val expectedRoot = authenticatedRootFor(currentUserRole)
    if (currentTop == MainDashboard && expectedRoot == AdminMain) {
      backStack.clear()
      backStack.add(AdminMain)
    } else if (currentTop == AdminMain && expectedRoot == MainDashboard) {
      backStack.clear()
      backStack.add(MainDashboard)
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
                backStack.add(authenticatedRootFor(authViewModel.currentUserRole.value)) 
            },
            onNavigateToForgotPassword = { backStack.add(ForgotPassword) }
          )
        }
        entry<Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() },
                onNavigateToPolicy = { backStack.add(Policy) }
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
            GuardedProfileRoute(
                profileId = key.profileId,
                onInvalid = { backStack.removeLastOrNull() }
            ) { validProfileId ->
                com.example.carenest.feature.appointment.presentation.AppointmentListScreen(
                    profileId = validProfileId,
                    onBack = { backStack.removeLastOrNull() },
                    onAddAppointment = { backStack.add(AddAppointment(validProfileId)) }
                )
            }
        }
        entry<AddAppointment> {
            val key = it as AddAppointment
            GuardedProfileRoute(
                profileId = key.profileId,
                onInvalid = { backStack.removeLastOrNull() }
            ) { validProfileId ->
                com.example.carenest.feature.appointment.presentation.AddAppointmentScreen(
                    profileId = validProfileId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
        entry<VaccinationTracker> {
            val key = it as VaccinationTracker
            GuardedProfileRoute(
                profileId = key.profileId,
                onInvalid = { backStack.removeLastOrNull() }
            ) { validProfileId ->
                com.example.carenest.feature.health.presentation.VaccinationTrackerScreen(
                    profileId = validProfileId,
                    viewModel = vaccinationViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onAddVaccination = { profileId -> backStack.add(AddVaccinationSchedule(profileId = profileId)) },
                    onEditDose = { profileId, recordId, doseId ->
                        backStack.add(AddVaccinationSchedule(profileId = profileId, vaccineId = recordId, doseId = doseId))
                    }
                )
            }
        }
        entry<AddVaccinationSchedule> {
            val key = it as AddVaccinationSchedule
            GuardedProfileRoute(
                profileId = key.profileId,
                onInvalid = { backStack.removeLastOrNull() }
            ) { validProfileId ->
                com.example.carenest.feature.health.presentation.AddVaccinationScheduleScreen(
                    profileId = validProfileId,
                    vaccineId = key.vaccineId,
                    doseId = key.doseId,
                    viewModel = vaccinationViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
        entry<MainDashboard> {
          MainScreen(
              onItemClick = { backStack.add(it as NavKey) },
              onNavigateToAddMedicine = { backStack.add(AddMedicine) },
              onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
              onNavigateToAddMedicineSchedule = { backStack.add(AddMedicineSchedule) },
              onNavigateToOcrScanner = { backStack.add(OcrScanner) },
              onNavigateToAppointments = { profileId -> backStack.add(MedicalAppointment(profileId)) },
              onNavigateToVaccinations = { profileId -> backStack.add(VaccinationTracker(profileId)) },
              onNavigateToNotifications = { backStack.add(NotificationsCenter) },
              onNavigateToDoctorVerification = { backStack.add(DoctorVerification) },
              onNavigateToDoctorWorkspace = { backStack.add(DoctorWorkspace) },
              onNavigateToPatientBookingCenter = { backStack.add(PatientBookingCenter) },
              onNavigateToConsultationRoom = { bookingId -> backStack.add(ConsultationRoom(bookingId)) },
              onNavigateToPolicy = { backStack.add(Policy) },
              onNavigateToMedicalRecord = { profileId -> backStack.add(UserMedical(profileId)) },
              onNavigateToFamilyChat = { familyId, familyName, memberCount ->
                  backStack.add(FamilyChatRoom(familyId, familyName, memberCount))
              },
              onNavigateToDoctorProfile = { doctorId -> 
                  backStack.add(DoctorProfile(doctorId)) 
              },
              tabTarget = mainTabTarget,
              onTabTargetHandled = { mainTabTarget = null },
              onLogout = {
                  scope.launch {
                      application.secureSessionManager.clearAll()
                      backStack.clear()
                      backStack.add(Login)
                  }
              },
              authViewModel = authViewModel,
              dashboardViewModel = dashboardViewModel,
              medicineViewModel = medicineViewModel,
              modifier = Modifier
          )
        }
        entry<AdminMain> {
          val resolvedRole = currentUserRole ?: application.secureSessionManager.getUserRole().toAppRole()
          val token = application.secureSessionManager.getAccessToken()
          if (resolvedRole != AppRole.ADMIN) {
            LaunchedEffect(resolvedRole, token) {
              backStack.clear()
              backStack.add(if (token.isNullOrBlank()) Login else MainDashboard)
            }
          } else {
            AdminMainScreen(
              onLogout = {
                scope.launch {
                  application.secureSessionManager.clearAll()
                  backStack.clear()
                  backStack.add(Login)
                }
              },
              onNavigateToGroupRequests = { backStack.add(AdminGroupRequests) }
            )
          }
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
        entry<DoctorProfile> {
            val key = it as DoctorProfile
            com.example.carenest.feature.doctor.presentation.DoctorProfileScreen(
                doctorId = key.doctorId,
                onNavigateToConsultationRoom = { bookingId -> backStack.add(ConsultationRoom(bookingId)) },
                onNavigateToPatientBookingCenter = { backStack.add(PatientBookingCenter) },
                onBack = { backStack.removeLastOrNull() }
            )
        }
        
        entry<GroupPostDetail> {
          val key = it as GroupPostDetail
          com.example.carenest.feature.community.presentation.GroupPostDetailScreen(
            groupId = key.groupId,
            groupName = key.groupName,
            onBack = { backStack.removeLastOrNull() },
            onNavigateToCreatePost = { id -> backStack.add(CreateGroupPost(id)) },
            onNavigateToManageGroup = { id, name -> backStack.add(GroupGovernance(id, name)) },
            onNavigateToDoctorProfile = { doctorId -> backStack.add(DoctorProfile(doctorId)) }
          )
        }
        entry<CreateGroupPost> {
          val key = it as CreateGroupPost
          com.example.carenest.feature.community.presentation.CreateGroupPostScreen(
            groupId = key.groupId,
            onBack = { backStack.removeLastOrNull() },
            onPostSuccess = { backStack.removeLastOrNull() }
          )
        }
        entry<CreateGroupRequest> {
            com.example.carenest.feature.community.presentation.CreateGroupRequestScreen(
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
        entry<GroupGovernance> {
            val key = it as GroupGovernance
            com.example.carenest.feature.community.presentation.GroupGovernanceScreen(
                groupId = key.groupId,
                groupName = key.groupName,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<AdminGroupRequests> {
            com.example.carenest.feature.admin.presentation.AdminGroupRequestsScreen(
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
        entry<FamilyChatRoom> {
          val key = it as FamilyChatRoom
          com.example.carenest.feature.chat.presentation.FamilyChatPane(
            familyId = key.id,
            familyName = key.name,
            memberCount = key.memberCount,
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
        entry<NotificationsCenter> {
            val viewModel: NotificationsCenterViewModel = viewModel(
                factory = NotificationsCenterViewModelFactory(application.notificationApi)
            )
            NotificationsCenterScreen(
                profileId = null,
                viewModel = viewModel,
                onBack = {
                    dashboardViewModel.fetchDashboard()
                    backStack.removeLastOrNull()
                },
                onOpenNotification = openNotificationTarget
            )
        }
        entry<DoctorVerification> {
            val viewModel: EkycViewModel = viewModel(
                factory = EkycViewModelFactory(application.ekycRepository)
            )
            LaunchedEffect(Unit) {
                authViewModel.refreshCurrentUser()
            }
            DoctorVerificationScreen(
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<UserMedical> {
            val key = it as UserMedical
            val viewModel: UserMedicalViewModel = viewModel(
                factory = UserMedicalViewModelFactory(application.familyRepository, application.growthApi)
            )
            GuardedProfileRoute(
                profileId = key.profileId,
                onInvalid = { backStack.removeLastOrNull() }
            ) { validProfileId ->
                UserMedicalScreen(
                    profileId = validProfileId,
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToMedicineSchedule = { backStack.add(MedicineSchedule) },
                    onNavigateToAppointmentList = { backStack.add(MedicalAppointment(validProfileId)) },
                    onNavigateToVaccinationTracker = { backStack.add(VaccinationTracker(validProfileId)) }
                )
            }
        }
        entry<Policy> {
            PolicyScreen(
                onBack = { backStack.removeLastOrNull() }
            )
        }
        entry<DoctorWorkspace> {
            com.example.carenest.feature.booking.presentation.doctorworkspace.DoctorWorkspaceScreen(
                onBack = { backStack.removeLastOrNull() },
                onNavigateToConsultationRoom = { bookingId -> backStack.add(ConsultationRoom(bookingId)) }
            )
        }
        entry<PatientBookingCenter> {
            com.example.carenest.feature.booking.presentation.patient.PatientBookingCenterScreen(
                onBack = { backStack.removeLastOrNull() },
                onNavigateToConsultationRoom = { bookingId -> backStack.add(ConsultationRoom(bookingId)) }
            )
        }
        entry<ConsultationRoom> { args ->
            val viewModel: com.example.carenest.feature.booking.presentation.consultation.ConsultationRoomViewModel = viewModel(
                factory = com.example.carenest.feature.booking.presentation.consultation.ConsultationRoomViewModelFactory(
                    repository = application.bookingRepository,
                    webSocketClient = com.example.carenest.feature.booking.data.remote.ConsultationWebSocketClient(application.secureSessionManager)
                )
            )
            com.example.carenest.feature.booking.presentation.consultation.ConsultationRoomScreen(
                bookingId = args.bookingId,
                viewModel = viewModel,
                onBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}

private fun authenticatedRootFor(role: AppRole?): NavKey {
  return if (role == AppRole.ADMIN) AdminMain else MainDashboard
}

@Composable
private fun GuardedProfileRoute(
  profileId: Long,
  onInvalid: () -> Unit,
  content: @Composable (Long) -> Unit
) {
  if (!profileId.isValidHealthProfileId()) {
    val context = LocalContext.current
    LaunchedEffect(profileId) {
      Toast.makeText(context, "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước", Toast.LENGTH_SHORT).show()
      onInvalid()
    }
  } else {
    content(profileId)
  }
}

private fun String?.toAppRole(): AppRole? {
  val normalized = this?.trim()?.removePrefix("ROLE_")?.uppercase() ?: return null
  return AppRole.entries.firstOrNull { it.name == normalized }
}
