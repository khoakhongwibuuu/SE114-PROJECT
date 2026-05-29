package com.example.carenest.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.ChatScreen
import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.feature.community.presentation.CommunityScreen

import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.dashboard.presentation.DashboardViewModelFactory
import com.example.carenest.feature.ekyc.presentation.DoctorVerificationScreen
import com.example.carenest.feature.family.presentation.FamilyFlowScreen
import com.example.carenest.feature.profile.presentation.ProfileScreen
import com.example.carenest.feature.medical.presentation.MedicineScreen
import com.example.carenest.feature.medical.presentation.MedicineViewModel

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    onNavigateToMedicineSchedule: () -> Unit = {},
    onNavigateToAddMedicineSchedule: () -> Unit = {},
    onNavigateToAppointment: () -> Unit = {},
    onNavigateToVaccine: () -> Unit = {},
    onNavigateToOcrScanner: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var openedCommunityGroup by remember { mutableStateOf<CommunityGroup?>(null) }

    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(application.dashboardApi, application.secureSessionManager),
    )
    val medicineViewModel: MedicineViewModel = viewModel()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ", fontSize = 9.sp) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Gia đình") },
                    label = { Text("Gia đình", fontSize = 9.sp) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Thuốc") },
                    label = { Text("Thuốc", fontSize = 9.sp) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Tin nhắn") },
                    label = { Text("Tin nhắn", fontSize = 9.sp) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        openedCommunityGroup = null
                    },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Cộng đồng") },
                    label = { Text("Cộng đồng", fontSize = 9.sp) },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tôi") },
                    label = { Text("Tôi", fontSize = 9.sp) },
                    colors = navColors(),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
        ) {
            when (selectedTab) {
                0 -> HomeDashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMedicine = onNavigateToMedicineSchedule,
                    onNavigateToAppointment = onNavigateToAppointment,
                    onNavigateToVaccine = onNavigateToVaccine,
                    onNavigateToTask = {}
                )

                1 -> FamilyFlowScreen(
                    viewModel = dashboardViewModel,
                    modifier = Modifier.fillMaxSize(),
                )

                2 -> MedicineScreen(
                    viewModel = medicineViewModel,
                    onAddMedicineClick = onNavigateToAddMedicine,
                    onScheduleClick = onNavigateToMedicineSchedule,
                    onAddScheduleClick = onNavigateToAddMedicineSchedule,
                    onOcrClick = onNavigateToOcrScanner,
                )

                3 -> ChatHubScreen(
                    dashboardViewModel = dashboardViewModel,
                )

                4 -> {
                    val group = openedCommunityGroup
                    if (group == null) {
                        CommunityScreen(onOpenGroup = { openedCommunityGroup = it })
                    } else {
                        ChatScreen(
                            groupId = group.id,
                            groupName = group.name,
                            onBack = { openedCommunityGroup = null },
                        )
                    }
                }

                5 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    unselectedIconColor = Color(0xFF707882),
    unselectedTextColor = Color(0xFF707882),
    indicatorColor = Color(0xFFEFF6FF),
)
