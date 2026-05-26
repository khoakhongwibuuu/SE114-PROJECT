package com.example.carenest.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.model.CommunityGroup
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.ui.chat.ChatScreen
import com.example.carenest.ui.community.CommunityScreen
import com.example.carenest.ui.dashboard.DashboardScreen
import com.example.carenest.ui.medical.MedicineScreen
import com.example.carenest.viewmodel.DashboardViewModel
import com.example.carenest.viewmodel.DashboardViewModelFactory
import com.example.carenest.viewmodel.MedicineViewModel

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    dashboardViewModel: DashboardViewModel,
    familyViewModel: com.example.carenest.viewmodel.FamilyViewModel,
    profileViewModel: com.example.carenest.viewmodel.ProfileViewModel,
    medicineViewModel: MedicineViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var openedCommunityGroup by remember { mutableStateOf<CommunityGroup?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Gia đình") },
                    label = { Text("Gia đình", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Thuốc") },
                    label = { Text("Thuốc", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Tin nhắn") },
                    label = { Text("Tin nhắn", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        openedCommunityGroup = null
                    },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Cộng đồng") },
                    label = { Text("Cộng đồng", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tôi") },
                    label = { Text("Tôi", fontSize = 10.sp) },
                    colors = navColors()
                )
            }
        }
    ) { paddingValues ->
        Surface(modifier = modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> com.example.carenest.ui.main.HomeDashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMedicine = {},
                    onNavigateToAppointment = {},
                    onNavigateToVaccine = {},
                    onNavigateToTask = {}
                )
                1 -> com.example.carenest.ui.family.FamilyPickerScreen(
                    viewModel = familyViewModel,
                    onNavigateToManagement = { /* handle nested nav if needed */ }
                )
                2 -> MedicineScreen(viewModel = medicineViewModel, onAddMedicineClick = onNavigateToAddMedicine)
                3 -> Text("Tin nhắn Screen (Đang phát triển)", modifier = Modifier.padding(16.dp))
                4 -> {
                    val group = openedCommunityGroup
                    if (group == null) {
                        CommunityScreen(onOpenGroup = { openedCommunityGroup = it })
                    } else {
                        ChatScreen(groupId = group.id, groupName = group.name, onBack = { openedCommunityGroup = null })
                    }
                }
                5 -> com.example.carenest.ui.medical.HealthProfileDetailScreen(
                    viewModel = profileViewModel,
                    memberId = 1,
                    onBack = { selectedTab = 0 }
                )
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
)
