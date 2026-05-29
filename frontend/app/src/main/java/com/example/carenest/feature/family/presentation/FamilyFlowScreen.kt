package com.example.carenest.feature.family.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.R
import com.example.carenest.core.presentation.theme.BackgroundLight
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel

private sealed interface FamilyRoute {
    data object Picker : FamilyRoute
    data object Management : FamilyRoute
    data class HealthProfileDetail(val memberId: String) : FamilyRoute
}

private enum class ManagementMode {
    CREATE,
    JOIN,
}

private data class FamilySummaryUi(
    val id: String,
    val name: String,
    val memberCount: Int,
    val ownerName: String,
    val myRole: String,
)

private data class FamilyMemberUi(
    val id: String,
    val fullName: String,
    val role: String,
    val age: Int,
    val birthday: String,
    val gender: String,
    val bloodType: String,
    val height: Int,
    val weight: Int,
    val healthStatus: String,
    val allergy: String,
    val medicalHistory: String,
    val avatarUrl: String? = null,
)

private data class FamilyInvitationUi(
    val id: String,
    val name: String,
    val senderEmail: String,
)

private data class SentInvitationUi(
    val id: String,
    val receiverEmail: String,
    val status: String,
)

private data class JoinRoleOption(
    val label: String,
    val value: String,
)

@Composable
fun FamilyFlowScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val currentFamilyId by viewModel.currentFamilyId.collectAsState()

    var families by remember { mutableStateOf<List<FamilySummaryUi>>(emptyList()) }
    var membersByFamily by remember { mutableStateOf<Map<String, List<FamilyMemberUi>>>(emptyMap()) }
    var route by remember { mutableStateOf<FamilyRoute>(FamilyRoute.Picker) }
    var selectedFamilyId by remember { mutableStateOf<String?>(null) }
    var showAddFamilySheet by remember { mutableStateOf(false) }
    var managementMode by remember { mutableStateOf<ManagementMode?>(null) }
    var managementStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(dashboardState) {
        if (families.isNotEmpty()) return@LaunchedEffect

        val success = dashboardState as? DashboardState.Success
        val initialFamilies = success?.data?.families?.takeIf { it.isNotEmpty() }?.mapIndexed { index, family ->
            FamilySummaryUi(
                id = family.id,
                name = family.name,
                memberCount = if (index == 0) 4 else 3,
                ownerName = if (index == 0) "Nguyễn Minh An" else "Trần Thu Hà",
                myRole = if (index == 0) "OWNER" else "MEMBER",
            )
        } ?: sampleFamilies()
        val initialMembers = success?.data?.members?.takeIf { it.isNotEmpty() }?.let { dashboardMembers ->
            initialFamilies.associate { family ->
                family.id to dashboardMembers.mapIndexed { index, member ->
                    FamilyMemberUi(
                        id = "${family.id}-${member.id}",
                        fullName = member.name,
                        role = sampleRoleForIndex(index),
                        age = listOf(34, 31, 8, 4)[index % 4],
                        birthday = listOf("1992-05-10", "1995-08-21", "2018-06-02", "2022-11-15")[index % 4],
                        gender = if (index % 2 == 0) "MALE" else "FEMALE",
                        bloodType = listOf("O+", "A+", "B+", "AB+")[index % 4],
                        height = listOf(175, 162, 127, 98)[index % 4],
                        weight = listOf(72, 54, 26, 15)[index % 4],
                        healthStatus = if (index % 3 == 0) "Sức khỏe tốt" else "Theo dõi định kỳ",
                        allergy = if (index % 2 == 0) "Không có" else "Dị ứng hải sản",
                        medicalHistory = if (index % 2 == 0) "Khám sức khỏe định kỳ bình thường." else "Tiền sử viêm mũi dị ứng.",
                        avatarUrl = member.avatarUrl,
                    )
                }
            }
        } ?: sampleMembers()

        families = initialFamilies
        membersByFamily = initialMembers
        selectedFamilyId = currentFamilyId ?: initialFamilies.firstOrNull()?.id
    }

    LaunchedEffect(currentFamilyId) {
        if (!currentFamilyId.isNullOrBlank()) {
            selectedFamilyId = currentFamilyId
        }
    }

    val hasFamily = families.isNotEmpty()
    val selectedFamily = families.firstOrNull { it.id == selectedFamilyId } ?: families.firstOrNull()
    val selectedMembers = membersByFamily[selectedFamily?.id].orEmpty()
    val detailMember = when (val currentRoute = route) {
        is FamilyRoute.HealthProfileDetail -> membersByFamily.values.flatten().firstOrNull { it.id == currentRoute.memberId }
        else -> null
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
        when (val currentRoute = route) {
            FamilyRoute.Picker -> FamilyPickerScreen(
                families = families,
                activeFamilyId = selectedFamilyId,
                isLoading = families.isEmpty() && dashboardState is DashboardState.Loading,
                onAddFamily = { showAddFamilySheet = true },
                onSelectFamily = { family ->
                    selectedFamilyId = family.id
                    viewModel.switchFamily(Family(id = family.id, name = family.name))
                    managementMode = null
                    managementStep = 1
                    route = FamilyRoute.Management
                },
            )

            FamilyRoute.Management -> FamilyManagementScreen(
                hasFamily = hasFamily,
                familyName = selectedFamily?.name ?: "Gia đình",
                members = selectedMembers,
                isOwner = selectedFamily?.myRole == "OWNER",
                overrideMode = managementMode,
                currentStep = managementStep,
                onStepChange = { managementStep = it },
                onOpenAddMember = { },
                onBack = {
                    if (managementMode != null) {
                        managementMode = null
                        route = FamilyRoute.Picker
                    } else if (hasFamily) {
                        route = FamilyRoute.Picker
                    } else {
                        managementStep = 1
                        route = FamilyRoute.Picker
                    }
                },
                onCreateFamily = { familyName ->
                    val newFamilyId = "family-${families.size + 1}"
                    val createdFamily = FamilySummaryUi(
                        id = newFamilyId,
                        name = familyName,
                        memberCount = 1,
                        ownerName = "Bạn",
                        myRole = "OWNER",
                    )
                    families = families + createdFamily
                    membersByFamily = membersByFamily + (
                        newFamilyId to listOf(
                            FamilyMemberUi(
                                id = "$newFamilyId-owner",
                                fullName = "Bạn",
                                role = "OWNER",
                                age = 28,
                                birthday = "1998-03-12",
                                gender = "FEMALE",
                                bloodType = "O+",
                                height = 160,
                                weight = 50,
                                healthStatus = "Sức khỏe tốt",
                                allergy = "Không có",
                                medicalHistory = "Chưa có ghi chú bệnh lý.",
                            )
                        )
                    )
                    selectedFamilyId = newFamilyId
                    viewModel.switchFamily(Family(id = newFamilyId, name = familyName))
                    managementMode = null
                    managementStep = 1
                },
                onJoinFamily = {
                    if (!hasFamily) {
                        families = sampleJoinedFamilies()
                        membersByFamily = sampleMembers()
                    }
                    selectedFamilyId = families.firstOrNull()?.id ?: sampleJoinedFamilies().first().id
                    managementMode = null
                    managementStep = 1
                },
                onOpenHealthDetail = { memberId ->
                    route = FamilyRoute.HealthProfileDetail(memberId)
                },
            )

            is FamilyRoute.HealthProfileDetail -> HealthProfileDetailScreen(
                member = detailMember,
                onBack = { route = FamilyRoute.Management },
            )
        }

        if (showAddFamilySheet) {
            AddFamilyBottomSheet(
                onDismiss = { showAddFamilySheet = false },
                onCreateNew = {
                    showAddFamilySheet = false
                    managementMode = ManagementMode.CREATE
                    managementStep = 2
                    route = FamilyRoute.Management
                },
                onJoinByCode = {
                    showAddFamilySheet = false
                    managementMode = ManagementMode.JOIN
                    managementStep = 4
                    route = FamilyRoute.Management
                },
            )
        }
    }
}

@Composable
private fun FamilyPickerScreen(
    families: List<FamilySummaryUi>,
    activeFamilyId: String?,
    isLoading: Boolean,
    onAddFamily: () -> Unit,
    onSelectFamily: (FamilySummaryUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Gia đình của tôi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                ),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .clickable(onClick = onAddFamily),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm gia đình", tint = Color.White)
            }
        }

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            families.isEmpty() -> FamilyPickerEmptyState(onAddFamily = onAddFamily)

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "ĐANG THAM GIA",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                    )
                }
                items(families, key = { it.id }) { family ->
                    FamilyCard(
                        item = family,
                        isActive = family.id == activeFamilyId,
                        onClick = { onSelectFamily(family) },
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable(onClick = onAddFamily)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = PrimaryBlue,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm gia đình",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyPickerEmptyState(onAddFamily: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
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
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Chưa có gia đình nào",
            color = Color(0xFF1E293B),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tạo gia đình mới hoặc tham gia bằng mã mời từ chủ hộ nhé!",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddFamily,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text("Bắt đầu ngay", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FamilyCard(
    item: FamilySummaryUi,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFFEFF6FF) else Color.White)
            .border(
                width = 1.5.dp,
                color = if (isActive) PrimaryBlue else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isActive) PrimaryBlue else Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                tint = if (isActive) Color.White else PrimaryBlue,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = if (isActive) PrimaryBlue else Color(0xFF1E293B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.memberCount} thành viên • ${item.ownerName}",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            RoleBadge(role = item.myRole)
            Spacer(modifier = Modifier.height(6.dp))
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isActive) PrimaryBlue else Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val label = when (role) {
        "OWNER" -> "Chủ hộ"
        "FATHER" -> "Bố"
        "MOTHER" -> "Mẹ"
        "OLDER_BROTHER" -> "Anh"
        "OLDER_SISTER" -> "Chị"
        "YOUNGER" -> "Em"
        else -> "Thành viên"
    }
    val isOwner = role == "OWNER"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOwner) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = if (isOwner) PrimaryBlue else Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FamilyManagementScreen(
    hasFamily: Boolean,
    familyName: String,
    members: List<FamilyMemberUi>,
    isOwner: Boolean,
    overrideMode: ManagementMode?,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onOpenAddMember: () -> Unit,
    onBack: () -> Unit,
    onCreateFamily: (String) -> Unit,
    onJoinFamily: () -> Unit,
    onOpenHealthDetail: (String) -> Unit,
) {
    var tempName by remember { mutableStateOf("Tổ ấm thân thương") }
    var joinCodeInput by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf("Mẹ") }
    var selectedJoinRole by remember { mutableStateOf("MEMBER") }
    var inviteValue by remember { mutableStateOf("") }
    var addMemberModalVisible by remember { mutableStateOf(false) }

    val receivedInvitations = remember {
        listOf(
            FamilyInvitationUi(
                id = "invite-1",
                name = "Gia đình Minh Châu",
                senderEmail = "chauminh@example.com",
            )
        )
    }
    val sentInvitations = remember {
        listOf(
            SentInvitationUi(
                id = "sent-1",
                receiverEmail = "nguoithan@example.com",
                status = "Đang chờ xác nhận",
            )
        )
    }
    val joinRoleOptions = remember {
        listOf(
            JoinRoleOption("Thành viên", "MEMBER"),
            JoinRoleOption("Bố", "FATHER"),
            JoinRoleOption("Mẹ", "MOTHER"),
            JoinRoleOption("Anh", "OLDER_BROTHER"),
            JoinRoleOption("Chị", "OLDER_SISTER"),
            JoinRoleOption("Em", "YOUNGER"),
            JoinRoleOption("Người thân", "OTHER"),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        FamilyManagementTopBar(
            title = when {
                overrideMode == ManagementMode.CREATE -> "Tạo gia đình mới"
                overrideMode == ManagementMode.JOIN -> "Tham gia gia đình"
                hasFamily -> familyName
                else -> "Gia đình"
            },
            showBack = overrideMode != null || hasFamily,
            onBack = onBack,
        )

        when {
            overrideMode == ManagementMode.CREATE -> FamilySetupStep(
                tempName = tempName,
                onTempNameChange = { tempName = it },
                onSubmit = { onCreateFamily(tempName.trim().ifBlank { "Tổ ấm thân thương" }) },
            )

            overrideMode == ManagementMode.JOIN -> FamilyJoinStep(
                joinCodeInput = joinCodeInput,
                onJoinCodeChange = { joinCodeInput = it.uppercase() },
                selectedJoinRole = selectedJoinRole,
                onSelectJoinRole = { selectedJoinRole = it },
                joinRoleOptions = joinRoleOptions,
                receivedInvitations = receivedInvitations,
                onBack = onBack,
                onJoin = onJoinFamily,
            )

            !hasFamily && currentStep == 1 -> FamilyWelcomeStep(
                receivedInvitations = receivedInvitations,
                onCreate = { onStepChange(2) },
                onJoin = { onStepChange(4) },
            )

            !hasFamily && currentStep == 4 -> FamilyJoinStep(
                joinCodeInput = joinCodeInput,
                onJoinCodeChange = { joinCodeInput = it.uppercase() },
                selectedJoinRole = selectedJoinRole,
                onSelectJoinRole = { selectedJoinRole = it },
                joinRoleOptions = joinRoleOptions,
                receivedInvitations = receivedInvitations,
                onBack = { onStepChange(1) },
                onJoin = onJoinFamily,
            )

            !hasFamily -> FamilySetupStep(
                tempName = tempName,
                onTempNameChange = { tempName = it },
                onSubmit = { onCreateFamily(tempName.trim().ifBlank { "Tổ ấm thân thương" }) },
            )

            else -> FamilyMembersManagementStep(
                familyName = familyName,
                members = members,
                isOwner = isOwner,
                onOpenAddMember = { addMemberModalVisible = true; onOpenAddMember() },
                onOpenHealthDetail = onOpenHealthDetail,
            )
        }
    }

    if (addMemberModalVisible) {
        AddMemberModal(
            inviteValue = inviteValue,
            onInviteValueChange = { inviteValue = it },
            selectedRelation = selectedRelation,
            onSelectRelation = { selectedRelation = it },
            sentInvitations = sentInvitations,
            onDismiss = { addMemberModalVisible = false },
        )
    }
}

@Composable
private fun FamilyManagementTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                if (showBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF1E293B),
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.carenest_logo_house),
                        contentDescription = "CareNest",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = PrimaryBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.width(32.dp))
    }
}

@Composable
private fun FamilyWelcomeStep(
    receivedInvitations: List<FamilyInvitationUi>,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(120.dp),
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Bắt đầu tổ ấm của bạn",
            color = Color(0xFF0F172A),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tạo gia đình để bắt đầu quản lý sức khỏe cho những người thân yêu.",
            color = Color(0xFF64748B),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onCreate,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tạo gia đình", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onJoin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(
                "Tham gia một gia đình hiện có",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(
            icon = Icons.Default.Lightbulb,
            title = "Mẹo nhỏ cho bạn",
            message = "Việc kết nối các thành viên giúp bạn theo dõi lịch tiêm chủng và nhắc nhở uống thuốc tự động.",
        )
        if (receivedInvitations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            InvitationCard(receivedInvitations = receivedInvitations)
        }
    }
}

@Composable
private fun FamilySetupStep(
    tempName: String,
    onTempNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.ime)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(100.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Đặt tên cho tổ ấm",
            color = Color(0xFF0F172A),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tên này sẽ hiển thị trên dashboard và các báo cáo sức khỏe chung của gia đình.",
            color = Color(0xFF64748B),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = tempName,
            onValueChange = onTempNameChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = TextSecondary) },
            placeholder = { Text("Ví dụ: Gia đình hạnh phúc...") },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
        InfoCard(
            icon = Icons.Default.Info,
            title = "Mẹo nhỏ",
            message = "Bạn có thể thay đổi tên này bất cứ lúc nào trong phần cài đặt gia đình.",
        )
        Spacer(modifier = Modifier.weight(1f, fill = true))
        Button(
            onClick = onSubmit,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Tiếp tục", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun FamilyJoinStep(
    joinCodeInput: String,
    onJoinCodeChange: (String) -> Unit,
    selectedJoinRole: String,
    onSelectJoinRole: (String) -> Unit,
    joinRoleOptions: List<JoinRoleOption>,
    receivedInvitations: List<FamilyInvitationUi>,
    onBack: () -> Unit,
    onJoin: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.ime)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0F172A))
                            )
                        ),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Tham gia gia đình",
                    color = Color(0xFF0F172A),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Nhập mã được chia sẻ bởi người thân để kết nối với tổ ấm của bạn.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "MÃ GIA ĐÌNH",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = joinCodeInput,
                    onValueChange = onJoinCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    placeholder = { Text("Nhập mã hoặc quét mã QR") },
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VAI TRÒ CỦA BẠN",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    joinRoleOptions.forEach { option ->
                        val selected = selectedJoinRole == option.value
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Color(0xFF7DD3FC) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(999.dp),
                                )
                                .clickable { onSelectJoinRole(option.value) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selected) Color(0xFF0369A1) else Color(0xFF64748B),
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = option.label,
                                color = if (selected) Color(0xFF0369A1) else Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onJoin,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    Text("Tham gia bằng mã", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    Text(
                        text = "HOẶC",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedActionButton(
                    icon = Icons.Default.QrCode2,
                    text = "Quét mã QR",
                    onClick = { },
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quay lại", color = Color(0xFF1E293B))
                }
            }
        }

        if (receivedInvitations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            InvitationCard(receivedInvitations = receivedInvitations)
        }
    }
}

@Composable
private fun FamilyMembersManagementStep(
    familyName: String,
    members: List<FamilyMemberUi>,
    isOwner: Boolean,
    onOpenAddMember: () -> Unit,
    onOpenHealthDetail: (String) -> Unit,
) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        floatingActionButton = {
            if (isOwner) {
                FloatingActionButton(
                    onClick = onOpenAddMember,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm thành viên")
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = familyName,
                            color = Color(0xFF0F172A),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "${members.size} Thành viên",
                                color = PrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quản lý sức khỏe và lịch trình của cả gia đình.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                    )
                }
            }

            items(members, key = { it.id }) { member ->
                MemberCard(
                    member = member,
                    onOpenHealthDetail = { onOpenHealthDetail(member.id) },
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: FamilyMemberUi,
    onOpenHealthDetail: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(
                    fullName = member.fullName,
                    avatarUrl = member.avatarUrl,
                    size = 64.dp,
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenHealthDetail),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.fullName,
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = formatRole(member.role),
                                color = PrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${member.age} Tuổi",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                    )
                }
            }
            if (member.age < 18) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0F2FE))
                        .clickable(onClick = onOpenHealthDetail)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.ChildCare, contentDescription = null, tint = Color(0xFF0369A1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "THEO DÕI PHÁT TRIỂN",
                        color = Color(0xFF0369A1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0369A1))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMemberModal(
    inviteValue: String,
    onInviteValueChange: (String) -> Unit,
    selectedRelation: String,
    onSelectRelation: (String) -> Unit,
    sentInvitations: List<SentInvitationUi>,
    onDismiss: () -> Unit,
) {
    val relations = listOf("Bố", "Mẹ", "Anh", "Chị", "Em", "Khác")
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFF8FAFC)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.ime),
        ) {
            Text(
                text = "Thêm thành viên",
                color = Color(0xFF0F172A),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mời người thân bằng email hoặc tạo mã QR để họ tham gia nhanh vào gia đình của bạn.",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "EMAIL NGƯỜI THÂN",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inviteValue,
                        onValueChange = onInviteValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null, tint = TextSecondary) },
                        placeholder = { Text("vidu@email.com") },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "MỐI QUAN HỆ",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        relations.forEach { relation ->
                            val selected = relation == selectedRelation
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFF7DD3FC) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable { onSelectRelation(relation) }
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (selected) Color(0xFF0369A1) else Color(0xFF64748B),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = relation,
                                    color = if (selected) Color(0xFF0369A1) else Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                    ) {
                        Text("Gửi lời mời", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF0369A1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lời mời đang chờ",
                            color = Color(0xFF0F172A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    sentInvitations.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            InitialCircle(initial = item.receiverEmail.firstOrNull()?.uppercase() ?: "N")
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.receiverEmail, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                Text(item.status, color = Color(0xFF64748B), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InvitationCard(receivedInvitations: List<FamilyInvitationUi>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Lời mời bạn đã nhận",
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            receivedInvitations.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InitialCircle(initial = item.name.firstOrNull()?.uppercase() ?: "G")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        Text(item.senderEmail, color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallActionButton(text = "Nhận", container = Color(0xFFDCFCE7), content = Color(0xFF166534))
                        SmallActionButton(text = "Từ chối", container = Color(0xFFFEE2E2), content = Color(0xFFB91C1C))
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallActionButton(text: String, container: Color, content: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, color = content, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = Color(0xFF64748B), fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun OutlinedActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF0369A1)),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HealthProfileDetailScreen(
    member: FamilyMemberUi?,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E293B))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = member?.fullName ?: "Chi tiết hồ sơ",
                    color = PrimaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarCircle(fullName = member?.fullName ?: "Thành viên", avatarUrl = member?.avatarUrl, size = 88.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = member?.fullName ?: "Đang tải...",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD3E2ED))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = formatRole(member?.role),
                            color = Color(0xFF526069),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    val statusColor = if ((member?.healthStatus ?: "").contains("Theo")) Color(0xFFE65100) else Color(0xFF2E7D32)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.13f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = member?.healthStatus ?: "Sức khỏe tốt",
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        item {
            DetailSectionCard(title = "Thông tin cơ bản") {
                DetailInfoRow(Icons.Default.Vaccines, "Nhóm máu", member?.bloodType ?: "Chưa cập nhật")
                DetailInfoRow(Icons.Default.Person, "Ngày sinh", member?.birthday?.toDisplayDate() ?: "Chưa cập nhật")
                DetailInfoRow(Icons.Default.Person, "Giới tính", member?.gender.toDisplayGender())
                DetailInfoRow(
                    Icons.Default.MonitorWeight,
                    "Chiều cao / Cân nặng",
                    "${member?.height ?: "--"} cm / ${member?.weight ?: "--"} kg",
                )
            }
        }
        item {
            DetailSectionCard(title = "Tiền sử bệnh") {
                Text(
                    text = member?.medicalHistory ?: "Chưa có thông tin",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        item {
            DetailSectionCard(title = "Dị ứng") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFDAD6))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = member?.allergy ?: "Không có dị ứng",
                        color = Color(0xFF93000A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        item {
            DetailSectionCard(title = "Thông tin bổ sung") {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF707882), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hồ sơ đang hiển thị dữ liệu sức khỏe thật từ backend.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lịch tiêm chủng", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFE5FF), contentColor = PrimaryBlue),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Theo dõi phát triển", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFamilyBottomSheet(
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onJoinByCode: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Thêm gia đình",
                color = Color(0xFF1E293B),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SheetOptionRow(
                icon = Icons.Default.Home,
                iconContainer = Color(0xFFEFF6FF),
                iconTint = PrimaryBlue,
                title = "Tạo gia đình mới",
                subtitle = "Bạn sẽ là Chủ hộ của gia đình này",
                onClick = onCreateNew,
            )
            SheetOptionRow(
                icon = Icons.Default.QrCode2,
                iconContainer = Color(0xFFF0FDF4),
                iconTint = Color(0xFF16A34A),
                title = "Tham gia bằng mã",
                subtitle = "Nhập code hoặc quét QR từ Chủ hộ",
                onClick = onJoinByCode,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SheetOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainer: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(subtitle, color = Color(0xFF64748B), fontSize = 13.sp)
        }
    }
}

@Composable
private fun AvatarCircle(
    fullName: String,
    avatarUrl: String?,
    size: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = fullName.initials(),
                color = PrimaryBlue,
                fontWeight = FontWeight.Black,
                fontSize = (size.value / 3).sp,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
                .border(2.dp, Color.White, CircleShape),
        )
    }
}

@Composable
private fun InitialCircle(initial: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF6FF)),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = PrimaryBlue, fontWeight = FontWeight.Black)
    }
}

private fun formatRole(role: String?): String = when (role) {
    "OWNER" -> "Chủ gia đình"
    "FATHER" -> "Bố"
    "MOTHER" -> "Mẹ"
    "OLDER_BROTHER" -> "Anh"
    "OLDER_SISTER" -> "Chị"
    "YOUNGER" -> "Em"
    "MEMBER" -> "Thành viên"
    "OTHER" -> "Người thân"
    else -> "Thành viên"
}

private fun String?.toDisplayGender(): String = when (this) {
    "MALE" -> "Nam"
    "FEMALE" -> "Nữ"
    else -> "Chưa cập nhật"
}

private fun String.toDisplayDate(): String {
    val parts = split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else this
}

private fun String.initials(): String = trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.take(1).uppercase() }
    .ifBlank { "CN" }

private fun sampleRoleForIndex(index: Int): String = when (index) {
    0 -> "OWNER"
    1 -> "MOTHER"
    2 -> "YOUNGER"
    else -> "MEMBER"
}

private fun sampleFamilies(): List<FamilySummaryUi> = listOf(
    FamilySummaryUi(
        id = "family-1",
        name = "Gia đình Minh An",
        memberCount = 4,
        ownerName = "Nguyễn Minh An",
        myRole = "OWNER",
    ),
    FamilySummaryUi(
        id = "family-2",
        name = "Gia đình Hạnh Phúc",
        memberCount = 3,
        ownerName = "Trần Thu Hà",
        myRole = "MEMBER",
    ),
)

private fun sampleJoinedFamilies(): List<FamilySummaryUi> = listOf(
    FamilySummaryUi(
        id = "family-joined",
        name = "Gia đình Thu Hà",
        memberCount = 3,
        ownerName = "Trần Thu Hà",
        myRole = "MEMBER",
    )
)

private fun sampleMembers(): Map<String, List<FamilyMemberUi>> = mapOf(
    "family-1" to listOf(
        FamilyMemberUi(
            id = "m-1",
            fullName = "Nguyễn Minh An",
            role = "OWNER",
            age = 34,
            birthday = "1992-05-10",
            gender = "MALE",
            bloodType = "O+",
            height = 175,
            weight = 72,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Khám sức khỏe định kỳ bình thường.",
        ),
        FamilyMemberUi(
            id = "m-2",
            fullName = "Lê Thu Ngân",
            role = "MOTHER",
            age = 31,
            birthday = "1995-08-21",
            gender = "FEMALE",
            bloodType = "A+",
            height = 162,
            weight = 54,
            healthStatus = "Theo dõi định kỳ",
            allergy = "Dị ứng hải sản",
            medicalHistory = "Tiền sử viêm mũi dị ứng.",
        ),
        FamilyMemberUi(
            id = "m-3",
            fullName = "Nguyễn Bảo Châu",
            role = "YOUNGER",
            age = 8,
            birthday = "2018-06-02",
            gender = "FEMALE",
            bloodType = "B+",
            height = 127,
            weight = 26,
            healthStatus = "Theo dõi tăng trưởng",
            allergy = "Không có",
            medicalHistory = "Đang theo dõi chiều cao định kỳ.",
        ),
        FamilyMemberUi(
            id = "m-4",
            fullName = "Nguyễn Gia Huy",
            role = "YOUNGER",
            age = 4,
            birthday = "2022-11-15",
            gender = "MALE",
            bloodType = "AB+",
            height = 98,
            weight = 15,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Lịch tiêm chủng đầy đủ.",
        ),
    ),
    "family-2" to listOf(
        FamilyMemberUi(
            id = "m-5",
            fullName = "Trần Thu Hà",
            role = "OWNER",
            age = 33,
            birthday = "1993-04-08",
            gender = "FEMALE",
            bloodType = "A+",
            height = 160,
            weight = 52,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Theo dõi giấc ngủ và dinh dưỡng.",
        ),
        FamilyMemberUi(
            id = "m-6",
            fullName = "Trần Đức Khang",
            role = "FATHER",
            age = 35,
            birthday = "1991-01-20",
            gender = "MALE",
            bloodType = "O+",
            height = 172,
            weight = 70,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Khám tổng quát hàng năm.",
        ),
        FamilyMemberUi(
            id = "m-7",
            fullName = "Trần Bảo Minh",
            role = "YOUNGER",
            age = 6,
            birthday = "2020-02-14",
            gender = "MALE",
            bloodType = "B+",
            height = 112,
            weight = 19,
            healthStatus = "Theo dõi định kỳ",
            allergy = "Dị ứng thời tiết",
            medicalHistory = "Theo dõi hô hấp theo mùa.",
        ),
    ),
    "family-joined" to listOf(
        FamilyMemberUi(
            id = "m-8",
            fullName = "Trần Thu Hà",
            role = "OWNER",
            age = 33,
            birthday = "1993-04-08",
            gender = "FEMALE",
            bloodType = "A+",
            height = 160,
            weight = 52,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Khám định kỳ mỗi 6 tháng.",
        ),
        FamilyMemberUi(
            id = "m-9",
            fullName = "Nguyễn Hoài Nam",
            role = "MEMBER",
            age = 27,
            birthday = "1999-09-19",
            gender = "MALE",
            bloodType = "O+",
            height = 171,
            weight = 64,
            healthStatus = "Sức khỏe tốt",
            allergy = "Không có",
            medicalHistory = "Không có ghi chú bệnh lý.",
        ),
    ),
)
