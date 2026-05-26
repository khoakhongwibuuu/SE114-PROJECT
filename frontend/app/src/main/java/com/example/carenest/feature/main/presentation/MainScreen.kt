package com.example.carenest.feature.main.presentation

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
import com.example.carenest.feature.community.domain.model.CommunityGroup
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.ChatScreen
import com.example.carenest.feature.community.presentation.CommunityScreen
import com.example.carenest.feature.dashboard.presentation.DashboardScreen
import com.example.carenest.feature.medical.presentation.MedicineScreen
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.dashboard.presentation.DashboardViewModelFactory
import com.example.carenest.feature.medical.presentation.MedicineViewModel

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var openedCommunityGroup by remember { mutableStateOf<CommunityGroup?>(null) }

    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(application.dashboardApi, application.secureSessionManager)
    )
    val medicineViewModel: MedicineViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chá»§") },
                    label = { Text("Trang chá»§", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Gia Ä‘Ã¬nh") },
                    label = { Text("Gia Ä‘Ã¬nh", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Thuá»‘c") },
                    label = { Text("Thuá»‘c", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Tin nháº¯n") },
                    label = { Text("Tin nháº¯n", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        openedCommunityGroup = null
                    },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Cá»™ng Ä‘á»“ng") },
                    label = { Text("Cá»™ng Ä‘á»“ng", fontSize = 10.sp) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "TÃ´i") },
                    label = { Text("TÃ´i", fontSize = 10.sp) },
                    colors = navColors()
                )
            }
        }
    ) { paddingValues ->
        Surface(modifier = modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = dashboardViewModel)
                1 -> Text("Gia Ä‘Ã¬nh Screen", modifier = Modifier.padding(16.dp))
                2 -> MedicineScreen(viewModel = medicineViewModel, onAddMedicineClick = onNavigateToAddMedicine)
                3 -> Text("Tin nháº¯n Screen", modifier = Modifier.padding(16.dp))
                4 -> {
                    val group = openedCommunityGroup
                    if (group == null) {
                        CommunityScreen(onOpenGroup = { openedCommunityGroup = it })
                    } else {
                        ChatScreen(groupId = group.id, groupName = group.name, onBack = { openedCommunityGroup = null })
                    }
                }
                5 -> Text("Profile Screen", modifier = Modifier.padding(16.dp))
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
