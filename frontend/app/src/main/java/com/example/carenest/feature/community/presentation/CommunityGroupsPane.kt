package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.domain.model.CommunityGroup

private enum class GroupTab(val label: String) {
    MINE("Nhóm của bạn"),
    DISCOVER("Tất cả"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityGroupsPane(
    onOpenGroup: (CommunityGroup) -> Unit = {},
) {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(application.communityRepository),
    )
    val state by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf(GroupTab.MINE) }
    var showPreview by remember { mutableStateOf(false) }
    var selectedGroupForPreview by remember { mutableStateOf<CommunityGroup?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(state.error) {
        state.error?.let { err ->
            android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        OutlinedTextField(
            value = state.search,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            placeholder = {
                Text(
                    "Tìm nhóm, chuyên khoa hoặc bác sĩ...",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
            },
            trailingIcon = if (state.search.isNotBlank()) {
                {
                    IconButton(onClick = { viewModel.onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color(0xFF94A3B8))
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = Color(0xFFE2E8F0),
                unfocusedBorderColor = Color(0xFFE2E8F0),
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
        ) {
            GroupTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = tab }
                        .padding(bottom = 11.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (activeTab == tab) PrimaryBlue else Color(0xFF64748B),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (activeTab == tab) PrimaryBlue else Color.Transparent),
                    )
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            activeTab == GroupTab.MINE -> {
                CommunityGroupList(
                    groups = state.myGroups,
                    emptyIcon = Icons.Default.Groups,
                    emptyTitle = "Bạn chưa tham gia nhóm nào",
                    emptyText = "Bạn chưa tham gia nhóm nào. Hãy chuyển sang tab \"Tất cả\" để khám phá nhé!",
                    itemContent = { group -> MyGroupItem(group, onClick = { onOpenGroup(group) }) },
                )
            }

            else -> {
                CommunityGroupList(
                    groups = state.discoverGroups,
                    emptyIcon = Icons.Default.PersonSearch,
                    emptyTitle = "Không tìm thấy nhóm phù hợp",
                    emptyText = state.error ?: "Hãy thử đổi từ khóa tìm kiếm hoặc quay lại sau.",
                    itemContent = { group ->
                        DiscoverGroupItem(
                            group = group,
                            joining = state.joiningGroupId == group.id,
                            onPreview = {
                                selectedGroupForPreview = group
                                showPreview = true
                                viewModel.loadGroupPreview(group.id)
                            },
                            onJoin = {
                                viewModel.join(group, onSuccess = { joined ->
                                    onOpenGroup(joined)
                                })
                            },
                        )
                    },
                )
            }
        }
    }

    if (showPreview && selectedGroupForPreview != null) {
        val basicGroup = selectedGroupForPreview!!
        val preview = state.previewGroup
        ModalBottomSheet(
            onDismissRequest = {
                showPreview = false
                viewModel.clearPreview()
            },
            containerColor = Color.White,
            sheetState = previewSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFCBD5E1)),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    GroupAvatar(group = basicGroup, large = true)
                    IconButton(onClick = {
                        showPreview = false
                        viewModel.clearPreview()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFF64748B))
                    }
                }
                
                if (state.isPreviewLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else {
                    val groupName = preview?.name ?: basicGroup.name
                    val memberCount = preview?.memberCount ?: basicGroup.memberCount
                    val leadDoctorName = preview?.leadDoctorName ?: basicGroup.leadDoctorName
                    val description = preview?.description ?: basicGroup.description ?: "Không gian trao đổi kinh nghiệm chăm sóc sức khỏe trong cộng đồng CareNest."
                    val rules = preview?.rules ?: "Tôn trọng thành viên khác, không đăng nội dung sai lệch y khoa và luôn giữ hội nhóm là nơi trao đổi an toàn."
                    val joined = preview?.joined ?: basicGroup.joined

                    Text(groupName, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append("$memberCount thành viên")
                            if (!leadDoctorName.isNullOrBlank()) append(" · Host: $leadDoctorName")
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64748B),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        description,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Color(0xFF334155),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFBEB))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rules,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            lineHeight = 18.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            if (joined) {
                                showPreview = false
                                viewModel.clearPreview()
                                onOpenGroup(basicGroup)
                            } else {
                                if (preview != null) {
                                    viewModel.joinFromPreview(preview, onSuccess = { joinedGroup ->
                                        showPreview = false
                                        viewModel.clearPreview()
                                        onOpenGroup(joinedGroup)
                                    })
                                } else {
                                    viewModel.join(basicGroup, onSuccess = { joinedGroup ->
                                        showPreview = false
                                        viewModel.clearPreview()
                                        onOpenGroup(joinedGroup)
                                    })
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    ) {
                        if (state.joiningGroupId == basicGroup.id) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (joined) "Vào phòng chat" else "Tham gia nhóm", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CommunityGroupList(
    groups: List<CommunityGroup>,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    emptyTitle: String,
    emptyText: String,
    itemContent: @Composable (CommunityGroup) -> Unit,
) {
    if (groups.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(emptyIcon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(46.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(emptyTitle, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(emptyText, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                }
            }
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items = groups, key = { it.id }) { group -> itemContent(group) }
        }
    }
}

@Composable
private fun MyGroupItem(group: CommunityGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group = group)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(group.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatGroupTime(group.latestActivityAt), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(group.latestMessage ?: "Nhóm vừa được tạo", fontSize = 13.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DiscoverGroupItem(
    group: CommunityGroup,
    joining: Boolean,
    onPreview: () -> Unit,
    onJoin: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPreview),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group = group)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${group.memberCount} thành viên", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    if (!group.leadDoctorName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(group.leadDoctorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                group.category?.takeIf { it.isNotBlank() }?.let { category ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFFEEF6FF)).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(category, fontSize = 10.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onJoin,
                enabled = !joining,
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            ) {
                if (joining) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Tham gia", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GroupAvatar(group: CommunityGroup, large: Boolean = false) {
    val size = if (large) 54.dp else 48.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(if (large) RoundedCornerShape(12.dp) else CircleShape)
            .background(if (group.private) Color(0xFFE0F2FE) else Color(0xFFEFF6FF)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (group.private) Icons.Default.LocalHospital else Icons.Default.Groups,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(if (large) 28.dp else 24.dp),
        )
    }
}

private fun formatGroupTime(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return try {
        val cleanValue = value.trim()
        val instant = if (cleanValue.endsWith("Z")) {
            java.time.Instant.parse(cleanValue)
        } else {
            val hasZone = cleanValue.contains("+") || (cleanValue.lastIndexOf("-") > 10)
            if (hasZone) {
                java.time.Instant.parse(cleanValue)
            } else {
                java.time.LocalDateTime.parse(cleanValue).atZone(java.time.ZoneId.of("UTC")).toInstant()
            }
        }
        val systemZone = java.time.ZoneId.systemDefault()
        val localDateTime = java.time.LocalDateTime.ofInstant(instant, systemZone)
        val today = java.time.LocalDate.now(systemZone)
        if (localDateTime.toLocalDate().isEqual(today)) {
            localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
        }
    } catch (e: Exception) {
        try {
            if (value.contains("T")) {
                val parts = value.split("T")
                val datePart = parts[0]
                val timePart = parts[1].take(5)
                val todayStr = java.time.LocalDate.now().toString()
                if (datePart == todayStr) {
                    timePart
                } else {
                    val dateParts = datePart.split("-")
                    if (dateParts.size == 3) {
                        "${dateParts[2]}/${dateParts[1]}"
                    } else {
                        datePart
                    }
                }
            } else {
                value.take(10)
            }
        } catch (ex: Exception) {
            value.take(16)
        }
    }
}
