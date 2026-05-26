package com.example.carenest.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.model.FamilySummary
import com.example.carenest.viewmodel.FamilyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyPickerScreen(
    viewModel: FamilyViewModel,
    onNavigateToManagement: (mode: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFamilies()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gia đình của tôi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0369A1))
                        .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF0369A1)
                )
            } else if (uiState.myFamilies.isEmpty()) {
                FamilyPickerEmptyState(onStart = { showBottomSheet = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    item {
                        Text(
                            text = "ĐANG THAM GIA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    items(uiState.myFamilies) { family ->
                        FamilyCard(
                            item = family,
                            isActive = family.id == uiState.activeFamilyId,
                            onPress = {
                                viewModel.selectFamily(family.id)
                                onNavigateToManagement(null)
                            }
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showBottomSheet = true }
                                .background(Color.Transparent)
                                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF0369A1),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thêm gia đình",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE2E8F0)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Thêm gia đình",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    BottomSheetOption(
                        icon = Icons.Default.Home,
                        iconBgColor = Color(0xFFEFF6FF),
                        iconColor = Color(0xFF0369A1),
                        title = "Tạo gia đình mới",
                        subtitle = "Bạn sẽ là Chủ hộ của gia đình này",
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                    onNavigateToManagement("create")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BottomSheetOption(
                        icon = Icons.Default.QrCodeScanner,
                        iconBgColor = Color(0xFFF0FDF4),
                        iconColor = Color(0xFF16A34A),
                        title = "Tham gia bằng mã",
                        subtitle = "Nhập code hoặc quét QR từ Chủ hộ",
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                    onNavigateToManagement("join")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyPickerEmptyState(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = Color(0xFF94A3B8)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Chưa có gia đình nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tạo gia đình mới hoặc tham gia bằng mã mời từ chủ hộ nhé!",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text("Bắt đầu ngay", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun FamilyCard(item: FamilySummary, isActive: Boolean, onPress: () -> Unit) {
    val bgColor = if (isActive) Color(0xFFEFF6FF) else Color.White
    val borderColor = if (isActive) Color(0xFF0369A1) else Color(0xFFF1F5F9)
    val iconBgColor = if (isActive) Color(0xFF0369A1) else Color(0xFFEFF6FF)
    val iconColor = if (isActive) Color.White else Color(0xFF0369A1)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onPress() },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        shadowElevation = if (isActive) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color(0xFF0369A1) else Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.memberCount} thành viên • ${item.ownerName}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                RoleBadge(role = item.myRole)
                Spacer(modifier = Modifier.height(6.dp))
                if (isActive) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val isOwner = role == "OWNER"
    val label = when (role) {
        "OWNER" -> "Chủ hộ"
        "FATHER" -> "Bố"
        "MOTHER" -> "Mẹ"
        "OLDER_BROTHER" -> "Anh"
        "OLDER_SISTER" -> "Chị"
        "YOUNGER" -> "Em"
        else -> "Thành viên"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOwner) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOwner) Color(0xFF0369A1) else Color(0xFF64748B)
        )
    }
}

@Composable
fun BottomSheetOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
        }
    }
}
