package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.medical.presentation.ParsedMedicine
import com.example.carenest.feature.medical.presentation.MedicineViewModel
import androidx.compose.runtime.collectAsState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private data class EditableMedicine(
    val name: String,
    val dosage: String,
    val frequency: String,
)

@Composable
fun OcrScannerScreen(
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val isActionLoading by medicineViewModel.isActionLoading.collectAsState()
    val currentProfileId by dashboardViewModel.currentProfileId.collectAsState()
    val activeProfileId = currentProfileId ?: 0L

    var state by remember { mutableStateOf("idle") }
    var clinicName by remember { mutableStateOf("Phòng khám Đa khoa CareNest") }
    var doctorName by remember { mutableStateOf("BS. Nguyễn Văn A") }
    var prescriptionDate by remember { mutableStateOf("25/05/2026") }
    var medicines by remember {
        mutableStateOf(
            listOf(
                EditableMedicine("Panadol Extra", "1 viên", "2"),
                EditableMedicine("Vitamin C", "1 viên", "1"),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Text(
                text = "Quét toa thuốc OCR",
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    "scanning" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang nhận diện toa thuốc...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    "result" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Nhận diện thành công!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(width = 180.dp, height = 120.dp)
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Dùng camera hoặc chọn ảnh toa thuốc để AI trích xuất",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state != "scanning") {
                Button(
                    onClick = {
                        state = "scanning"
                        state = "result"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chụp trực tiếp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { state = "result" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE), contentColor = PrimaryBlue),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (state == "result") "Chọn ảnh khác" else "Chọn từ thư viện",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }

            if (state == "result") {
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Kiểm tra và chỉnh sửa kết quả OCR trước khi lưu",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Truthful Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFE65100))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tính năng AI OCR đang thử nghiệm. Hệ thống tự động điền dữ liệu mẫu để trải nghiệm.",
                        color = Color(0xFFE65100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = clinicName, onValueChange = { clinicName = it }, label = { Text("Phòng khám") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = doctorName, onValueChange = { doctorName = it }, label = { Text("Bác sĩ") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = prescriptionDate, onValueChange = { prescriptionDate = it }, label = { Text("Ngày kê toa") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        medicines.forEachIndexed { index, medicine ->
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Thuốc ${index + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = medicine.name,
                                onValueChange = { newValue ->
                                    medicines = medicines.mapIndexed { i, current ->
                                        if (i == index) current.copy(name = newValue) else current
                                    }
                                },
                                label = { Text("Tên thuốc") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = medicine.dosage,
                                    onValueChange = { newValue ->
                                        medicines = medicines.mapIndexed { i, current ->
                                            if (i == index) current.copy(dosage = newValue) else current
                                        }
                                    },
                                    label = { Text("Liều dùng") },
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = medicine.frequency,
                                    onValueChange = { newValue ->
                                        medicines = medicines.mapIndexed { i, current ->
                                            if (i == index) current.copy(frequency = newValue) else current
                                        }
                                    },
                                    label = { Text("Số lần/ngày") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (activeProfileId <= 0L) {
                            Toast.makeText(context, "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        medicineViewModel.confirmOcrPrescription(
                            profileId = activeProfileId,
                            clinicName = clinicName,
                            doctorName = doctorName,
                            prescriptionDate = prescriptionDate,
                            medicines = medicines.map { ParsedMedicine(name = it.name, dosage = it.dosage, frequency = it.frequency) },
                            onSuccess = {
                                Toast.makeText(context, "Đã lưu đơn thuốc và lịch uống thành công!", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Không thể lưu đơn thuốc: $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isActionLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    if (isActionLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xác nhận và lưu vào hệ thống", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
