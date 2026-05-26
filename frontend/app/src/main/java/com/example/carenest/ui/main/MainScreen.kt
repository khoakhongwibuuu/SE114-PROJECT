package com.example.carenest.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.ui.community.CommunityScreen
import com.example.carenest.ui.dashboard.DashboardScreen
import com.example.carenest.ui.medical.MedicineScreen
import com.example.carenest.viewmodel.DashboardViewModel
import com.example.carenest.viewmodel.MedicineViewModel
import com.example.carenest.viewmodel.DashboardViewModelFactory

@Composable
fun MainScreen(
    onItemClick: (Any) -> Unit,
    onNavigateToAddMedicine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // ViewModel setup
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(application.dashboardApi, application.dataStoreManager)
    )
    val medicineViewModel: MedicineViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Group, contentDescription = "Gia đình") },
                    label = { Text("Gia đình", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.MedicalServices, contentDescription = "Thuốc") },
                    label = { Text("Thuốc", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Chat, contentDescription = "Tin nhắn") },
                    label = { Text("Tin nhắn", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Public, contentDescription = "Cộng đồng") },
                    label = { Text("Cộng đồng", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = "Tôi") },
                    label = { Text("Tôi", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.carenest.theme.PrimaryBlue,
                        selectedTextColor = com.example.carenest.theme.PrimaryBlue,
                        indicatorColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = dashboardViewModel)
                1 -> Text("Gia đình Screen", modifier = Modifier.padding(16.dp))
                2 -> MedicineScreen(viewModel = medicineViewModel, onAddMedicineClick = onNavigateToAddMedicine)
                3 -> Text("Tin nhắn Screen", modifier = Modifier.padding(16.dp))
                4 -> CommunityScreen()
                5 -> Text("Profile Screen", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
