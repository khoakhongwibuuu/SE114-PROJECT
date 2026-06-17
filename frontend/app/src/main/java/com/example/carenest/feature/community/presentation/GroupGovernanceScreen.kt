package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.GroupMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupGovernanceState(
    val members: List<GroupMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isAdmin: Boolean = false,
    val currentUserId: Long? = null,
    val currentGroupRole: String? = null,
    val isGroupFrozen: Boolean = false,
    val memberOperationUserId: Long? = null,
    val isFreezeUpdating: Boolean = false
) {
    val canManageMembers: Boolean
        get() = isAdmin || currentGroupRole == "HOST"
}

private data class GovernanceAction(
    val title: String,
    val body: String,
    val confirmLabel: String,
    val confirmColor: Color,
    val onConfirm: () -> Unit
)

class GroupGovernanceViewModel(
    private val groupId: Long,
    private val repository: CommunityRepository,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GroupGovernanceState(
            currentUserId = sessionManager.getUserId(),
            isAdmin = sessionManager.getUserRole().normalizedAppRole() == "ADMIN"
        )
    )
    val uiState: StateFlow<GroupGovernanceState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val members = repository.getMembers(groupId)
                val preview = repository.preview(groupId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        members = members,
                        currentGroupRole = preview.myRole?.uppercase(),
                        isGroupFrozen = preview.isFrozen
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Không thể tải dữ liệu quản trị nhóm"
                    )
                }
            }
        }
    }

    fun updateRole(userId: Long, newRole: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberOperationUserId = userId, error = null, message = null) }
            try {
                repository.updateMemberRole(groupId, userId, newRole)
                loadData()
                _uiState.update { it.copy(memberOperationUserId = null, message = roleSuccessMessage(newRole)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        memberOperationUserId = null,
                        error = e.localizedMessage ?: "Không thể cập nhật vai trò thành viên"
                    )
                }
            }
        }
    }

    fun toggleFreeze() {
        viewModelScope.launch {
            val wasFrozen = _uiState.value.isGroupFrozen
            _uiState.update { it.copy(isFreezeUpdating = true, error = null, message = null) }
            try {
                if (wasFrozen) {
                    repository.unfreezeGroup(groupId)
                } else {
                    repository.freezeGroup(groupId)
                }
                loadData()
                _uiState.update {
                    it.copy(
                        isFreezeUpdating = false,
                        message = if (wasFrozen) {
                            "Nhóm đã được mở lại."
                        } else {
                            "Nhóm đã bị tạm khóa."
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFreezeUpdating = false,
                        error = e.localizedMessage ?: "Không thể thay đổi trạng thái nhóm"
                    )
                }
            }
        }
    }

    fun kickMember(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberOperationUserId = userId, error = null, message = null) }
            try {
                repository.kickMember(groupId, userId)
                loadData()
                _uiState.update { it.copy(memberOperationUserId = null, message = "Đã mời thành viên rời nhóm.") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        memberOperationUserId = null,
                        error = e.localizedMessage ?: "Không thể mời thành viên rời nhóm"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun roleSuccessMessage(newRole: String): String {
        return when (newRole.uppercase()) {
            "HOST" -> "Đã chuyển quyền trưởng nhóm."
            "MODERATOR" -> "Đã bổ nhiệm điều phối viên."
            else -> "Đã cập nhật quyền thành viên."
        }
    }

    companion object {
        fun provideFactory(
            groupId: Long,
            repository: CommunityRepository,
            sessionManager: SecureSessionManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupGovernanceViewModel(groupId, repository, sessionManager) as T
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupGovernanceScreen(
    groupId: Long,
    groupName: String,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as CareNestApplication
    val viewModel: GroupGovernanceViewModel = viewModel(
        factory = GroupGovernanceViewModel.provideFactory(
            groupId,
            app.communityRepository,
            app.secureSessionManager
        )
    )
    val state by viewModel.uiState.collectAsState()
    var pendingAction by remember { mutableStateOf<GovernanceAction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý hội nhóm",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadData) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
        ) {
            GovernanceHeaderCard(
                groupName = groupName,
                currentGroupRole = state.currentGroupRole,
                isAdmin = state.isAdmin,
                isGroupFrozen = state.isGroupFrozen
            )

            state.message?.let { message ->
                GovernanceBanner(
                    text = message,
                    containerColor = Color(0xFFDCFCE7),
                    textColor = Color(0xFF166534),
                    onDismiss = viewModel::clearMessage
                )
            }

            state.error?.let { error ->
                GovernanceBanner(
                    text = error,
                    containerColor = Color(0xFFFEE2E2),
                    textColor = Color(0xFFB91C1C),
                    onDismiss = viewModel::clearError
                )
            }

            if (state.isAdmin) {
                AdminGovernanceCard(
                    isGroupFrozen = state.isGroupFrozen,
                    isUpdating = state.isFreezeUpdating,
                    onToggleFreeze = {
                        pendingAction = GovernanceAction(
                            title = if (state.isGroupFrozen) "Mở lại nhóm" else "Tạm khóa nhóm",
                            body = if (state.isGroupFrozen) {
                                "Nhóm sẽ mở lại và thành viên có thể tiếp tục đăng bài, nhắn tin và tham gia."
                            } else {
                                "Nhóm bị tạm khóa sẽ không nhận thêm bài viết, bình luận, tin nhắn hoặc thành viên mới."
                            },
                            confirmLabel = if (state.isGroupFrozen) "Mở lại" else "Tạm khóa",
                            confirmColor = if (state.isGroupFrozen) Color(0xFF059669) else Color(0xFFDC2626),
                            onConfirm = viewModel::toggleFreeze
                        )
                    }
                )
            }

            Text(
                text = "Thành viên (${state.members.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.members, key = { it.userId }) { member ->
                        MemberItem(
                            member = member,
                            currentUserId = state.currentUserId,
                            currentGroupRole = state.currentGroupRole,
                            isAdmin = state.isAdmin,
                            isLoading = state.memberOperationUserId == member.userId,
                            onPromoteModerator = { viewModel.updateRole(member.userId, "MODERATOR") },
                            onDemoteToMember = { viewModel.updateRole(member.userId, "MEMBER") },
                            onPromoteHost = { viewModel.updateRole(member.userId, "HOST") },
                            onKick = {
                                pendingAction = GovernanceAction(
                                    title = "Mời rời nhóm",
                                    body = "Thành viên này sẽ mất quyền truy cập vào hội nhóm ngay sau khi xác nhận.",
                                    confirmLabel = "Xác nhận",
                                    confirmColor = Color(0xFFDC2626),
                                    onConfirm = { viewModel.kickMember(member.userId) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(action.title) },
            text = { Text(action.body) },
            confirmButton = {
                Button(
                    onClick = {
                        action.onConfirm()
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = action.confirmColor)
                ) {
                    Text(action.confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun GovernanceHeaderCard(
    groupName: String,
    currentGroupRole: String?,
    isAdmin: Boolean,
    isGroupFrozen: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = groupName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(
                    label = if (isAdmin) "Admin hệ thống" else roleLabel(currentGroupRole),
                    containerColor = if (isAdmin) Color(0xFFFFF7ED) else Color(0xFFEFF6FF),
                    textColor = if (isAdmin) Color(0xFFC2410C) else PrimaryBlue
                )
                StatusChip(
                    label = if (isGroupFrozen) "Đang tạm khóa" else "Đang hoạt động",
                    containerColor = if (isGroupFrozen) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    textColor = if (isGroupFrozen) Color(0xFFB91C1C) else Color(0xFF166534)
                )
            }
            Text(
                text = if (isAdmin) {
                    "Bạn đang dùng chế độ can thiệp hệ thống cho hội nhóm này."
                } else {
                    "Host có thể quản lý thành viên và điều phối viên. Admin có thể tạm khóa nhóm hoặc can thiệp vai trò khi cần."
                },
                color = Color(0xFF475569),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AdminGovernanceCard(
    isGroupFrozen: Boolean,
    isUpdating: Boolean,
    onToggleFreeze: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Điều hành hệ thống",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGroupFrozen) {
                        "Nhóm đang bị tạm khóa. Thành viên chỉ có thể xem dữ liệu hiện có."
                    } else {
                        "Có thể tạm khóa nhóm khi cần dừng tương tác mới để xử lý sự cố hoặc moderation."
                    },
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onToggleFreeze,
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGroupFrozen) Color(0xFF059669) else Color(0xFFDC2626)
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isGroupFrozen) "Mở lại nhóm" else "Tạm khóa")
                }
            }
        }
    }
}

@Composable
private fun GovernanceBanner(
    text: String,
    containerColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = textColor)
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: GroupMember,
    currentUserId: Long?,
    currentGroupRole: String?,
    isAdmin: Boolean,
    isLoading: Boolean,
    onPromoteModerator: () -> Unit,
    onDemoteToMember: () -> Unit,
    onPromoteHost: () -> Unit,
    onKick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isSelf = currentUserId == member.userId
    val isHostViewer = currentGroupRole == "HOST"
    val canHostManage = isHostViewer && !isSelf && member.role != "HOST"
    val canAdminManage = isAdmin && !isSelf

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSelf) "${member.name} (bạn)" else member.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val (roleColor, roleBg, roleText) = when (member.role) {
                        "HOST" -> Triple(Color(0xFF7C3AED), Color(0xFFEDE9FE), "Trưởng nhóm")
                        "MODERATOR" -> Triple(PrimaryBlue, PrimaryBlue.copy(alpha = 0.15f), "Điều phối viên")
                        else -> Triple(Color(0xFF475569), Color(0xFFF1F5F9), "Thành viên")
                    }
                    StatusChip(roleText, roleBg, roleColor)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryBlue,
                    strokeWidth = 2.dp
                )
            } else if (canHostManage || canAdminManage) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (canAdminManage) {
                            if (member.role != "HOST") {
                                DropdownMenuItem(
                                    text = { Text("Đặt làm trưởng nhóm") },
                                    onClick = {
                                        showMenu = false
                                        onPromoteHost()
                                    }
                                )
                            }
                            if (member.role != "MODERATOR") {
                                DropdownMenuItem(
                                    text = { Text("Đặt làm điều phối viên") },
                                    onClick = {
                                        showMenu = false
                                        onPromoteModerator()
                                    }
                                )
                            }
                            if (member.role != "MEMBER") {
                                DropdownMenuItem(
                                    text = { Text("Đặt làm thành viên") },
                                    onClick = {
                                        showMenu = false
                                        onDemoteToMember()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Mời rời nhóm", color = Color(0xFFDC2626)) },
                                onClick = {
                                    showMenu = false
                                    onKick()
                                }
                            )
                        } else if (canHostManage) {
                            if (member.role == "MEMBER") {
                                DropdownMenuItem(
                                    text = { Text("Bổ nhiệm điều phối viên") },
                                    onClick = {
                                        showMenu = false
                                        onPromoteModerator()
                                    }
                                )
                            }
                            if (member.role == "MODERATOR") {
                                DropdownMenuItem(
                                    text = { Text("Gỡ quyền điều phối") },
                                    onClick = {
                                        showMenu = false
                                        onDemoteToMember()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Mời rời nhóm", color = Color(0xFFDC2626)) },
                                onClick = {
                                    showMenu = false
                                    onKick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    containerColor: Color,
    textColor: Color,
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun roleLabel(role: String?): String {
    return when (role) {
        "HOST" -> "Trưởng nhóm"
        "MODERATOR" -> "Điều phối viên"
        else -> "Thành viên"
    }
}

private fun String?.normalizedAppRole(): String? {
    return this?.removePrefix("ROLE_")?.uppercase()
}
