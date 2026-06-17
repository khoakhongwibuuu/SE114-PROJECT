package com.example.carenest.feature.medical.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.medical.data.remote.CabinetMedicineResponse

private enum class CabinetFilter(val label: String) {
    ALL("Tất cả"),
    EXPIRED("Hết hạn"),
    EXPIRING("Sắp hết hạn"),
    LOW_STOCK("Sắp hết hàng"),
    OUT_OF_STOCK("Hết hàng"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel,
    refreshTrigger: Int = 0,
    onAddMedicineClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onAddScheduleClick: () -> Unit = {},
) {
    val cabinetState by viewModel.cabinetState.collectAsState()
    val isActionLoading by viewModel.isActionLoading.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(CabinetFilter.ALL) }
    var selectedMedicine by remember { mutableStateOf<CabinetMedicineResponse?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }
    var isEditingQuantity by remember { mutableStateOf(false) }
    var quantityDraft by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(refreshTrigger) {
        viewModel.fetchCabinet()
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearActionMessage()
        }
    }

    // Apply filter on top of cabinet state
    val filteredMedicines = remember(cabinetState, selectedFilter) {
        val all = (cabinetState as? CabinetState.Success)?.medicines ?: emptyList()
        when (selectedFilter) {
            CabinetFilter.ALL -> all
            CabinetFilter.EXPIRED -> all.filter { it.isExpired }
            CabinetFilter.EXPIRING -> all.filter { it.isExpiring }
            CabinetFilter.OUT_OF_STOCK -> all.filter { it.quantity <= 0 }
            CabinetFilter.LOW_STOCK -> all.filter { !it.isExpired && it.quantity in 1..3 }
        }
    }

    val alertCount = remember(cabinetState) {
        val all = (cabinetState as? CabinetState.Success)?.medicines ?: emptyList()
        all.count { it.isExpired || it.isExpiring }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { MedicineHeader(alertCount = alertCount) }

            if (alertCount > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFDAD6))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF93000A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$alertCount loại thuốc cần kiểm tra",
                            color = Color(0xFF93000A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // OCR card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Quét toa thuốc", color = Color(0xFF64748B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "OCR đang tạm tắt trong MVP, sẽ hoàn thiện ở phase cuối", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Phase cuối",
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Quick actions
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickMedicalAction(title = "Lịch uống", icon = Icons.Default.CalendarToday, modifier = Modifier.weight(1f), onClick = onScheduleClick)
                    QuickMedicalAction(title = "Thêm lịch", icon = Icons.Default.Notifications, modifier = Modifier.weight(1f), onClick = onAddScheduleClick)
                }
            }

            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CabinetFilter.entries, key = { it.name }) { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) PrimaryBlue else Color(0xFFE5E8EC))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(text = filter.label, color = if (selected) Color.White else Color(0xFF181C1F), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // State-driven content
            when (val state = cabinetState) {
                is CabinetState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is CabinetState.Error -> item {
                    Text(text = "⚠️ ${state.message}", color = Color(0xFFC62828), fontSize = 14.sp, modifier = Modifier.padding(8.dp))
                }
                is CabinetState.Success -> {
                    item {
                        Text(text = "${filteredMedicines.size} loại thuốc", color = Color(0xFF404751), fontSize = 12.sp)
                    }
                    if (filteredMedicines.isEmpty()) {
                        item {
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(text = "Chưa có thuốc nào trong tủ", color = Color(0xFF404751), fontSize = 14.sp, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    } else {
                        items(filteredMedicines, key = { it.id }) { medicine ->
                            MedicineCabinetRow(
                                medicine = medicine,
                                onClick = {
                                    selectedMedicine = medicine
                                    quantityDraft = medicine.quantity.toString()
                                    isEditingQuantity = false
                                    sheetVisible = true
                                },
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddMedicineClick,
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
        ) {
            if (isActionLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Add, contentDescription = "Thêm thuốc", modifier = Modifier.size(28.dp))
            }
        }
    }

    // Bottom sheet for medicine actions
    if (sheetVisible && selectedMedicine != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetVisible = false },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            val medicine = selectedMedicine ?: return@ModalBottomSheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = medicine.medicineName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF181C1F), modifier = Modifier.fillMaxWidth())
                Text(text = "Đơn vị: ${medicine.unit} • Số lượng: ${medicine.quantity}", color = Color(0xFF404751), fontSize = 13.sp)

                if (!isEditingQuantity) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SheetActionRow(
                            icon = Icons.Default.LocalHospital,
                            text = "Uống nhanh 1 ${medicine.unit}",
                            onClick = {
                                // Quick take — decrease qty by 1
                                viewModel.updateMedicineQuantity(medicine.id, (medicine.quantity - 1).coerceAtLeast(0))
                                sheetVisible = false
                            }
                        )
                        SheetActionRow(
                            icon = Icons.Default.Edit,
                            text = "Chỉnh sửa số lượng",
                            onClick = { isEditingQuantity = true }
                        )
                        SheetActionRow(
                            icon = Icons.Default.Delete,
                            text = "Xóa khỏi tủ thuốc",
                            iconTint = Color(0xFFC62828),
                            textColor = Color(0xFFC62828),
                            background = Color(0xFFFFEBEE),
                            onClick = {
                                viewModel.deleteMedicine(medicine.id)
                                sheetVisible = false
                            }
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Nhập số lượng mới:", color = Color(0xFF404751), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = quantityDraft,
                            onValueChange = { quantityDraft = it.filter(Char::isDigit) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Số lượng") }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { isEditingQuantity = false },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E8EC), contentColor = Color(0xFF404751)),
                            ) { Text("Hủy") }
                            Button(
                                onClick = {
                                    val qty = quantityDraft.toIntOrNull() ?: 0
                                    viewModel.updateMedicineQuantity(medicine.id, qty)
                                    sheetVisible = false
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            ) { Text("Lưu", color = Color.White) }
                        }
                    }
                }

                Button(
                    onClick = { sheetVisible = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF404751)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFC7D3)),
                ) { Text("Đóng") }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun QuickMedicalAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F8FC))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, color = Color(0xFF181C1F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MedicineHeader(alertCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Tủ thuốc gia đình", color = Color(0xFF181C1F), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F8FC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Cảnh báo tủ thuốc", tint = Color(0xFF404751))
            }
            if (alertCount > 0) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(8.dp).clip(CircleShape).background(Color(0xFFBA1A1A))
                )
            }
        }
    }
}

@Composable
private fun MedicineCabinetRow(
    medicine: CabinetMedicineResponse,
    onClick: () -> Unit,
) {
    val config = remember(medicine) { cabinetStatusConfig(medicine) }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFCFE5FF)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicine.medicineName, color = Color(0xFF181C1F), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                val expiry = medicine.expiryDate?.let { " • HSD: $it" } ?: ""
                Text(text = "${medicine.quantity} ${medicine.unit}$expiry", color = Color(0xFF404751), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(config.background).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(text = config.label, color = config.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class CabinetStatusConfig(val label: String, val background: Color, val textColor: Color)

private fun cabinetStatusConfig(medicine: CabinetMedicineResponse): CabinetStatusConfig {
    return when {
        medicine.isExpired -> CabinetStatusConfig("Hết hạn", Color(0xFFFFDAD6), Color(0xFF93000A))
        medicine.isExpiring -> CabinetStatusConfig("Sắp hết hạn", Color(0xFFFFF3E0), Color(0xFFE65100))
        medicine.quantity <= 0 -> CabinetStatusConfig("Hết hàng", Color(0xFFECEFF1), Color(0xFF546E7A))
        medicine.quantity in 1..3 -> CabinetStatusConfig("Sắp hết hàng", Color(0xFFFFF3E0), Color(0xFFE65100))
        else -> CabinetStatusConfig("Ổn định", Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = PrimaryBlue,
    textColor: Color = Color(0xFF181C1F),
    background: Color = Color(0xFFE5E8EC),
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(background).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
