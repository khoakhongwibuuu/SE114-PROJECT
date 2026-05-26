package com.example.carenest.ui.community

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.model.CommunityGroup
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.viewmodel.CommunityViewModel
import com.example.carenest.viewmodel.CommunityViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onOpenGroup: (CommunityGroup) -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(application.communityRepository)
    )
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Nhóm của bạn", "Tất cả")

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Hội nhóm", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                OutlinedTextField(
                    value = state.search,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text("Tìm nhóm, chuyên khoa hoặc bác sĩ...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.White, contentColor = PrimaryBlue) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(label, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                if (page == 0) {
                    GroupList(
                        groups = state.myGroups,
                        emptyTitle = "Bạn chưa tham gia nhóm nào",
                        emptyText = "Hãy chuyển sang tab Tất cả để khám phá các cộng đồng phù hợp nhé.",
                        item = { group -> MyGroupRow(group, onOpenGroup) }
                    )
                } else {
                    GroupList(
                        groups = state.discoverGroups,
                        emptyTitle = "Không tìm thấy nhóm phù hợp",
                        emptyText = state.error ?: "Hãy thử đổi từ khóa tìm kiếm hoặc quay lại sau.",
                        item = { group ->
                            DiscoverGroupRow(
                                group = group,
                                joining = state.joiningGroupId == group.id,
                                onJoin = { viewModel.join(group) },
                                onPreview = { onOpenGroup(group) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupList(
    groups: List<CommunityGroup>,
    emptyTitle: String,
    emptyText: String,
    item: @Composable (CommunityGroup) -> Unit
) {
    if (groups.isEmpty()) {
        EmptyGroups(title = emptyTitle, text = emptyText)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(groups, key = { it.id }) { group ->
                item(group)
            }
        }
    }
}

@Composable
private fun MyGroupRow(group: CommunityGroup, onOpenGroup: (CommunityGroup) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenGroup(group) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(group.latestMessage ?: "Nhóm vừa được tạo", color = Color(0xFF64748B), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DiscoverGroupRow(
    group: CommunityGroup,
    joining: Boolean,
    onJoin: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${group.memberCount} thành viên", color = Color(0xFF64748B), fontSize = 12.sp)
                    group.leadDoctorName?.let {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(14.dp))
                        Text(it, color = Color(0xFF64748B), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                group.category?.let {
                    Text("#$it", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onJoin,
                enabled = !joining,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                if (joining) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tham gia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GroupAvatar(group: CommunityGroup) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (group.private) Color(0xFFE0F2FE) else Color(0xFFEFF6FF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (group.private) Icons.Default.LocalHospital else Icons.Default.Groups,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EmptyGroups(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(58.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
        Text(text, fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 6.dp), textAlign = TextAlign.Center)
    }
}
