package com.example.carenest.feature.medical.presentation

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun AddMedicineScreen(
    viewModel: MedicineViewModel,
    onBack: () -> Unit,
    onOpenOcrScanner: () -> Unit = {},
) {
    val isActionLoading by viewModel.isActionLoading.collectAsState()
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("viên") }
    var expiryDate by remember { mutableStateOf("") }
    var selectedExpiryDate by remember { mutableStateOf<LocalDate?>(null) }
    val quantityValue = quantity.toIntOrNull()
    val canSubmit = name.isNotBlank() &&
        quantityValue != null &&
        quantityValue >= 1 &&
        selectedExpiryDate != null &&
        !isActionLoading
    val units = listOf("viên", "gói", "chai", "tuýp", "hộp")
    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val datePickerDialog = remember(selectedExpiryDate) {
        val initialDate = selectedExpiryDate ?: LocalDate.now()
        val calendar = Calendar.getInstance().apply {
            set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                selectedExpiryDate = date
                expiryDate = date.format(dateFormatter)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.ime),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Text(
                text = "Thêm thuốc vào tủ",
                color = Color(0xFF181C1F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable(onClick = onOpenOcrScanner)
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
                        text = "Quét toa thuốc tự động",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "OCR đang tạm tắt trong MVP. Chạm để xem trạng thái tính năng.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Mở thông tin OCR",
                    tint = Color(0xFF94A3B8),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFBFC7D3)))
                Text("hoặc nhập tay", color = Color(0xFF404751), fontSize = 12.sp)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFBFC7D3)))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên thuốc *") },
                        placeholder = { Text("VD: Panadol Extra") },
                        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số lượng *") },
                        placeholder = { Text("1") },
                        leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        singleLine = true,
                        isError = quantity.isNotBlank() && (quantityValue == null || quantityValue < 1),
                        supportingText = {
                            if (quantity.isNotBlank() && (quantityValue == null || quantityValue < 1)) {
                                Text("Số lượng phải lớn hơn hoặc bằng 1")
                            }
                        },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Đơn vị", color = Color(0xFF404751), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            units.forEach { item ->
                                val selected = item == unit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) PrimaryBlue else Color(0xFFE5E8EC))
                                        .clickable { unit = item }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = item,
                                        color = if (selected) Color.White else Color(0xFF181C1F),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Hạn sử dụng *") },
                            placeholder = { Text("Chọn ngày") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            readOnly = true,
                            singleLine = true,
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { datePickerDialog.show() }
                        )
                    }
                    Text(
                        text = "Chọn ngày bằng lịch để tránh sai định dạng. Hạn dùng không được nằm trong quá khứ.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            Button(
                onClick = {
                    viewModel.addMedicine(
                        name = name,
                        quantity = quantityValue ?: 0,
                        unit = unit,
                        expiryDate = expiryDate,
                        onSuccess = {
                            Toast.makeText(context, "Đã thêm thuốc thành công", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_LONG).show()
                        },
                    )
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f),
                ),
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm vào tủ thuốc", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
