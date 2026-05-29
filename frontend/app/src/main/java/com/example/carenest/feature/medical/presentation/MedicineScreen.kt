package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.medical.domain.model.Medicine
import com.example.carenest.feature.medical.domain.model.MedicineStatus

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
    onAddMedicineClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onAddScheduleClick: () -> Unit = {},
    onOcrClick: () -> Unit = {},
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    var selectedFilter by remember { mutableStateOf(CabinetFilter.ALL) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }
    var isEditingQuantity by remember { mutableStateOf(false) }
    var quantityDraft by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredMedicines = remember(medicines, selectedFilter) {
        medicines.filter { medicine ->
            when (selectedFilter) {
                CabinetFilter.ALL -> true
                CabinetFilter.EXPIRED -> medicine.status == MedicineStatus.EXPIRED
                CabinetFilter.EXPIRING -> medicine.status == MedicineStatus.EXPIRING_SOON
                CabinetFilter.OUT_OF_STOCK -> medicine.status == MedicineStatus.OUT_OF_STOCK
                CabinetFilter.LOW_STOCK -> medicine.quantity in 1..3
            }
        }
    }
    val alertCount = medicines.count { it.status == MedicineStatus.EXPIRED || it.status == MedicineStatus.EXPIRING_SOON }

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
            item {
                MedicineHeader()
            }

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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFCFE5FF))
                        .clickable(onClick = onOcrClick)
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
                        Text(
                            text = "Quét toa thuốc",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Dùng AI để thêm thuốc và lịch uống nhanh hơn",
                            color = PrimaryBlue.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFBFC7D3))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickMedicalAction(
                        title = "Lịch uống",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f),
                        onClick = onScheduleClick,
                    )
                    QuickMedicalAction(
                        title = "Thêm lịch",
                        icon = Icons.Default.Notifications,
                        modifier = Modifier.weight(1f),
                        onClick = onAddScheduleClick,
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CabinetFilter.entries, key = { it.name }) { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) PrimaryBlue else Color(0xFFE5E8EC))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = filter.label,
                                color = if (selected) Color.White else Color(0xFF181C1F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "${filteredMedicines.size} loại thuốc",
                    color = Color(0xFF404751),
                    fontSize = 12.sp,
                )
            }

            if (filteredMedicines.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Chưa có thuốc nào trong tủ",
                                color = Color(0xFF404751),
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                            )
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

        FloatingActionButton(
            onClick = onAddMedicineClick,
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm thuốc", modifier = Modifier.size(28.dp))
        }
    }

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
                Text(
                    text = medicine.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF181C1F),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Đơn vị: ${medicine.unit} • Số lượng hiện tại: ${medicine.quantity}",
                    color = Color(0xFF404751),
                    fontSize = 13.sp,
                )

                if (!isEditingQuantity) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SheetActionRow(Icons.Default.LocalHospital, "Uống nhanh 1 ${medicine.unit}")
                        SheetActionRow(Icons.Default.Edit, "Chỉnh sửa số lượng") { isEditingQuantity = true }
                        SheetActionRow(
                            icon = Icons.Default.Delete,
                            text = "Xóa khỏi tủ thuốc",
                            iconTint = Color(0xFFC62828),
                            textColor = Color(0xFFC62828),
                            background = Color(0xFFFFEBEE),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Nhập số lượng mới:",
                            color = Color(0xFF404751),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = quantityDraft,
                            onValueChange = { quantityDraft = it.filter(Char::isDigit) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { isEditingQuantity = false },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E8EC), contentColor = Color(0xFF404751)),
                            ) {
                                Text("Hủy")
                            }
                            Button(
                                onClick = { sheetVisible = false },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            ) {
                                Text("Lưu", color = Color.White)
                            }
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
                ) {
                    Text("Đóng")
                }
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
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = Color(0xFF181C1F),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MedicineHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Tủ thuốc gia đình",
            color = Color(0xFF181C1F),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Box {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = Color(0xFF404751))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBA1A1A)),
            )
        }
    }
}

@Composable
private fun MedicineCabinetRow(
    medicine: Medicine,
    onClick: () -> Unit,
) {
    val config = remember(medicine.status, medicine.quantity) { cabinetStatusConfig(medicine) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFCFE5FF)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    color = Color(0xFF181C1F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${medicine.quantity} ${medicine.unit} • HSD: ${medicine.expiryDate}",
                    color = Color(0xFF404751),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(config.background)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = config.label,
                    color = config.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private data class CabinetStatusConfig(
    val label: String,
    val background: Color,
    val textColor: Color,
)

private fun cabinetStatusConfig(medicine: Medicine): CabinetStatusConfig {
    return when {
        medicine.status == MedicineStatus.EXPIRED -> CabinetStatusConfig("Hết hạn", Color(0xFFFFDAD6), Color(0xFF93000A))
        medicine.status == MedicineStatus.EXPIRING_SOON -> CabinetStatusConfig("Sắp hết hạn", Color(0xFFFFF3E0), Color(0xFFE65100))
        medicine.status == MedicineStatus.OUT_OF_STOCK -> CabinetStatusConfig("Hết hàng", Color(0xFFECEFF1), Color(0xFF546E7A))
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
