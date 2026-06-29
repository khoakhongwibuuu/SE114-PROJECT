package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carenest.R

private val AdminNavy = Color(0xFF00629D) // Sử dụng màu xanh chủ đạo của phía User

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
        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Duyệt bác sĩ") },
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAuditLogs by rememberSaveable { mutableStateOf(false) }
    val currentTab = AdminTab.entries.getOrNull(selectedTab) ?: AdminTab.DASHBOARD

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.carenest_logo_house),
                            contentDescription = "CareNest Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Text("CareNest Admin", fontWeight = FontWeight.Black)
                    }
                },
                actions = {
                    if (selectedTab == AdminTab.USERS.ordinal) {
                        IconButton(onClick = { showAuditLogs = true }) {
                            Icon(Icons.Default.History, contentDescription = "Nhật ký override")
                        }
                    }
                    IconButton(onClick = onNavigateToGroupRequests) {
                        Icon(Icons.Default.Groups, contentDescription = "Duyệt yêu cầu nhóm")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất")
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
                            unselectedIconColor = Color(0xFFB3E5FC),
                            unselectedTextColor = Color(0xFFB3E5FC),
                            indicatorColor = Color(0xFF004D7A),
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
                AdminTab.DASHBOARD -> AdminDashboardScreen(
                    onOpenUsers = { selectedTab = AdminTab.USERS.ordinal },
                    onOpenEkyc = { selectedTab = AdminTab.EKYC.ordinal },
                    onOpenModeration = { selectedTab = AdminTab.MODERATION.ordinal },
                )

                AdminTab.USERS -> AdminUserManagementScreen(
                    showAuditLogs = showAuditLogs,
                    onDismissAuditLogs = { showAuditLogs = false }
                )
                AdminTab.EKYC -> AdminEkycScreen()
                AdminTab.MODERATION -> AdminModerationScreen()
            }
        }
    }
}
