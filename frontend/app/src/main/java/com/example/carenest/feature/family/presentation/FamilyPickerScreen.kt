package com.example.carenest.feature.family.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.theme.AppElevation
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.PrimaryFixed
import com.example.carenest.core.presentation.theme.SurfaceHigh
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.family.domain.model.FamilySummary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyPickerScreen(
    viewModel: FamilyViewModel,
    onNavigateToManagement: (mode: String?) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFamilies()
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Gia đình của tôi",
                    style = CareNestTextStyles.titleXl,
                    color = TextPrimary,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center,
                ) {
                    CareNestIcon(name = "add", contentDescription = "Thêm", tint = Color.White)
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(padding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue,
                    )
                }

                !uiState.error.isNullOrBlank() && uiState.myFamilies.isEmpty() -> {
                    FamilyPickerErrorState(
                        message = uiState.error.orEmpty(),
                        onRetry = viewModel::loadFamilies,
                        onOpenActions = { showBottomSheet = true },
                    )
                }

                uiState.myFamilies.isEmpty() -> {
                    FamilyPickerEmptyState(onStart = { showBottomSheet = true })
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
                    ) {
                        if (!uiState.error.isNullOrBlank()) {
                            item {
                                InlineFamilyErrorCard(
                                    message = uiState.error.orEmpty(),
                                    onRetry = viewModel::loadFamilies,
                                )
                            }
                        }

                        item {
                            Text(
                                text = "ĐANG THAM GIA",
                                style = CareNestTextStyles.overline,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = AppSpacing.md),
                            )
                        }

                        items(uiState.myFamilies, key = { it.id }) { family ->
                            FamilyCard(
                                item = family,
                                isActive = family.id == uiState.activeFamilyId,
                                onPress = {
                                    viewModel.selectFamily(family.id)
                                    onNavigateToManagement(null)
                                },
                            )
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = AppSpacing.xs)
                                    .clip(RoundedCornerShape(AppRadius.xl))
                                    .clickable { showBottomSheet = true }
                                    .background(Color.Transparent)
                                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(AppRadius.xl))
                                    .padding(vertical = AppSpacing.lg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CareNestIcon(
                                        name = "add_circle",
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Text(
                                        text = "Thêm gia đình",
                                        style = CareNestTextStyles.labelMd,
                                        color = PrimaryBlue,
                                    )
                                }
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
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE2E8F0)) },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.x2, vertical = AppSpacing.lg)
                        .padding(bottom = AppSpacing.x2),
                ) {
                    Text(
                        text = "Thêm gia đình",
                        style = CareNestTextStyles.titleLg,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = AppSpacing.lg),
                    )

                    BottomSheetOption(
                        iconName = "home",
                        iconBgColor = PrimaryFixed,
                        iconColor = PrimaryBlue,
                        title = "Tạo gia đình mới",
                        subtitle = "Bạn sẽ là chủ hộ của gia đình này",
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showBottomSheet = false
                                onNavigateToManagement("create")
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    BottomSheetOption(
                        iconName = "qr_code",
                        iconBgColor = Color(0xFFF0FDF4),
                        iconColor = Color(0xFF16A34A),
                        title = "Tham gia bằng mã",
                        subtitle = "Nhập code hoặc quét QR từ chủ hộ",
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showBottomSheet = false
                                onNavigateToManagement("join")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyPickerErrorState(
    message: String,
    onRetry: () -> Unit,
    onOpenActions: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.x3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF1F2)),
            contentAlignment = Alignment.Center,
        ) {
            CareNestIcon(
                name = "notifications_active",
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFDC2626),
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        Text(
            text = "Không thể tải danh sách gia đình",
            style = CareNestTextStyles.titleLg,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = message,
            style = CareNestTextStyles.bodyMd,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.x2))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.xl),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text("Thử lại", style = CareNestTextStyles.labelLg, color = Color.White)
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        OutlinedButton(
            onClick = onOpenActions,
            shape = RoundedCornerShape(AppRadius.xl),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
            Text("Tạo hoặc tham gia gia đình", style = CareNestTextStyles.labelLg, color = PrimaryBlue)
        }
    }
}

@Composable
private fun InlineFamilyErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.md),
        shape = RoundedCornerShape(AppRadius.lg),
        color = Color(0xFFFFF7F7),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = "Lần tải gần nhất chưa thành công.",
                style = CareNestTextStyles.labelLg,
                color = Color(0xFFB91C1C),
            )
            Text(
                text = message,
                style = CareNestTextStyles.bodySm,
                color = Color(0xFF7F1D1D),
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(AppRadius.full),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            ) {
                Text("Tải lại danh sách", color = Color(0xFFB91C1C))
            }
        }
    }
}

@Composable
fun FamilyPickerEmptyState(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.x3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center,
        ) {
            CareNestIcon(
                name = "home",
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = Color(0xFF94A3B8),
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        Text(
            text = "Chưa có gia đình nào",
            style = CareNestTextStyles.titleLg,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "Tạo gia đình mới hoặc tham gia bằng mã mời từ chủ hộ.",
            style = CareNestTextStyles.bodyMd,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.x2))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.xl),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text("Bắt đầu ngay", style = CareNestTextStyles.labelLg, color = Color.White)
        }
    }
}

@Composable
fun FamilyCard(item: FamilySummary, isActive: Boolean, onPress: () -> Unit) {
    val bgColor = if (isActive) PrimaryFixed else CardBackground
    val borderColor = if (isActive) PrimaryBlue else Color(0xFFF1F5F9)
    val iconBgColor = if (isActive) PrimaryBlue else PrimaryFixed
    val iconColor = if (isActive) Color.White else PrimaryBlue

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.md)
            .clickable { onPress() },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = if (isActive) 0.dp else AppElevation.sm,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                CareNestIcon(name = "home", contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = CareNestTextStyles.labelLg.copy(fontSize = 16.sp),
                    color = if (isActive) PrimaryBlue else TextPrimary,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "${item.memberCount} thành viên • ${item.ownerName}",
                    style = CareNestTextStyles.bodySm,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                RoleBadge(role = item.myRole)
                Spacer(modifier = Modifier.height(6.dp))
                if (isActive) {
                    CareNestIcon(name = "check_circle", contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                } else {
                    CareNestIcon(name = "chevron_right", contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
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
            .background(if (isOwner) PrimaryFixed else SurfaceHigh)
            .padding(horizontal = AppSpacing.sm, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = CareNestTextStyles.labelSm.copy(fontSize = 11.sp),
            color = if (isOwner) PrimaryBlue else TextSecondary,
        )
    }
}

@Composable
fun BottomSheetOption(
    iconName: String,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            CareNestIcon(name = iconName, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, style = CareNestTextStyles.labelLg.copy(fontSize = 15.sp), color = TextPrimary)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = subtitle, style = CareNestTextStyles.bodySm.copy(fontSize = 13.sp), color = TextSecondary)
        }
    }
}
