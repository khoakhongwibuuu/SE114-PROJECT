package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val AdminNavy = Color(0xFF1E293B)

private enum class AdminTab(
    val title: String,
    val label: String,
    val icon: @Composable () -> Unit,
) {
    DASHBOARD(
        title = "Bảng điều khiển quản trị",
        label = "Tổng quan",
        icon = { Icon(Icons.Default.Analytics, contentDescription = "Tổng quan") },
    ),
    USERS(
        title = "Quản lý người dùng",
        label = "Người dùng",
        icon = { Icon(Icons.Default.Group, contentDescription = "Người dùng") },
    ),
    EKYC(
        title = "Duyệt hồ sơ bác sĩ",
        label = "Bác sĩ",
        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "eKYC") },
    ),
    MODERATION(
        title = "Kiểm duyệt nội dung",
        label = "Kiểm duyệt",
        icon = { Icon(Icons.Default.Report, contentDescription = "Kiểm duyệt") },
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    onLogout: () -> Unit = {},
    onNavigateToGroupRequests: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentTab = AdminTab.entries[selectedTab]

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentTab.title,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = onNavigateToGroupRequests) {
                        Icon(Icons.Default.Group, contentDescription = "Yêu cầu nhóm", tint = Color.White)
                    }
                    androidx.compose.material3.TextButton(onClick = onLogout) {
                        Text(text = "Đăng xuất", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AdminNavy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AdminNavy,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                AdminTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = tab.icon,
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = Color(0xFF334155),
                        ),
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (currentTab) {
                AdminTab.DASHBOARD -> AdminDashboardScreen()
                AdminTab.USERS -> AdminUserManagementScreen()
                AdminTab.EKYC -> AdminEkycScreen()
                AdminTab.MODERATION -> AdminModerationScreen()
            }
        }
    }
}
