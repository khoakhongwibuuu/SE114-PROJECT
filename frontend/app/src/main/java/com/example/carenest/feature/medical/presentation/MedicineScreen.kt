package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.feature.medical.domain.model.Medicine
import com.example.carenest.feature.medical.domain.model.MedicineStatus
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.medical.presentation.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(viewModel: MedicineViewModel, onAddMedicineClick: () -> Unit = {}) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val medicines by viewModel.medicines.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicineClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tá»§ thuá»‘c gia Ä‘Ã¬nh",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = "Cabinet",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tá»§ thuá»‘c chÃ­nh - Táº§ng 1",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = PrimaryBlue
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("TÃ¬m kiáº¿m thuá»‘c...", color = Color(0xFF94A3B8), fontSize = 15.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFE2E8F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionCard("Lá»‹ch thuá»‘c", Icons.Default.Medication, Color(0xFFEFF6FF), PrimaryBlue)
                QuickActionCard("Lá»‹ch háº¹n", Icons.Default.CalendarToday, Color(0xFFFAF5FF), Color(0xFF9333EA))
                QuickActionCard("TiÃªm chá»§ng", Icons.Default.Vaccines, Color(0xFFF0FDF4), Color(0xFF059669))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Filter Chips
            val filters = listOf("Táº¥t cáº£", "Sáº¯p háº¿t háº¡n", "Háº¿t hÃ ng")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryBlue else Color(0xFFE2E8F0))
                            .clickable { viewModel.onFilterSelected(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medicine List
            if (medicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("KhÃ´ng tÃ¬m tháº¥y káº¿t quáº£ nÃ o", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(medicines) { medicine ->
                        MedicineCardItem(medicine)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .width(100.dp)
            .clickable { }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun MedicineCardItem(medicine: Medicine) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Background
            val iconBgColor = when(medicine.status) {
                MedicineStatus.EXPIRED -> Color(0xFFFEE2E2)
                MedicineStatus.EXPIRING_SOON -> Color(0xFFE0F2FE)
                MedicineStatus.NORMAL -> Color(0xFFDBEAFE)
                MedicineStatus.OUT_OF_STOCK -> Color(0xFFF1F5F9)
            }
            val iconTintColor = when(medicine.status) {
                MedicineStatus.EXPIRED -> Color(0xFFDC2626)
                MedicineStatus.EXPIRING_SOON -> Color(0xFF475569)
                MedicineStatus.NORMAL -> Color(0xFF2563EB)
                MedicineStatus.OUT_OF_STOCK -> Color(0xFF94A3B8)
            }
            val icon = when(medicine.status) {
                MedicineStatus.EXPIRED -> Icons.Default.MedicalInformation
                MedicineStatus.EXPIRING_SOON -> Icons.Default.LocalHospital
                MedicineStatus.NORMAL -> Icons.Default.Vaccines
                MedicineStatus.OUT_OF_STOCK -> Icons.Default.MedicalServices
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${medicine.quantity} ${medicine.unit}",
                        fontSize = 12.sp,
                        color = if(medicine.status == MedicineStatus.OUT_OF_STOCK) Color(0xFFDC2626) else Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFCBD5E1)))
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val dateIcon = if (medicine.status == MedicineStatus.OUT_OF_STOCK) Icons.Default.History else Icons.Default.CalendarToday
                    val dateColor = if (medicine.status == MedicineStatus.EXPIRED) Color(0xFFDC2626) else if (medicine.status == MedicineStatus.EXPIRING_SOON) Color(0xFFEA580C) else Color(0xFF0284C7)
                    Icon(dateIcon, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (medicine.status == MedicineStatus.OUT_OF_STOCK) Color(0xFF94A3B8) else dateColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = medicine.expiryDate,
                        fontSize = 12.sp,
                        color = if (medicine.status == MedicineStatus.OUT_OF_STOCK) Color(0xFF94A3B8) else dateColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Status Badge
            val (badgeText, badgeBgColor, badgeTextColor) = when(medicine.status) {
                MedicineStatus.EXPIRED -> Triple("Háº¾T Háº N", Color(0xFFDC2626), Color.White)
                MedicineStatus.EXPIRING_SOON -> Triple("Sáº®P Háº¾T Háº N", Color(0xFFF97316), Color.White)
                MedicineStatus.NORMAL -> Triple("á»”N Äá»ŠNH", Color(0xFF0284C7), Color.White)
                MedicineStatus.OUT_OF_STOCK -> Triple("Háº¾T HÃ€NG", Color(0xFF94A3B8), Color.White)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeTextColor
                )
            }
        }
    }
}
