package com.example.carenest.feature.medical.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.medical.data.remote.ParsedMedicationDto
import java.io.File

@Composable
fun OcrScannerScreen(
    dashboardViewModel: DashboardViewModel,
    medicineViewModel: MedicineViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) } // 0 = Scan/Raw Text, 1 = Confirm/Edit
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var rawText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var parsedMedications by remember { mutableStateOf<List<ParsedMedicationDto>>(emptyList()) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val sampleRawText = """
        TOA THUỐC ĐIỀU TRỊ
        Bệnh nhân: Nguyễn Văn A
        1. Paracetamol 500mg
        Số lượng: 10 viên
        Uống 1 viên mỗi lần, ngày 2 lần sau ăn.
        2. Amoxicillin 500mg
        Số lượng: 21 viên
        Uống 1 viên mỗi lần, ngày 3 lần.
        3. Decolgen
        Số lượng: 4 viên
        Uống khi nhức đầu.
    """.trimIndent()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            errorMessage = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { captured: Boolean ->
        val uri = pendingCameraUri
        if (captured && uri != null) {
            selectedImageUri = uri
            errorMessage = null
            Toast.makeText(context, "Đã chụp ảnh toa thuốc. Vui lòng nhập hoặc dán phần chữ OCR trước khi phân tích.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Chưa chụp được ảnh toa thuốc.", Toast.LENGTH_SHORT).show()
        }
    }

    fun createCameraImageUri(): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val imageFile = File.createTempFile("medicine_ocr_", ".jpg", cameraDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .border(width = 1.dp, color = Color(0xFFF1F5F9), shape = RoundedCornerShape(0.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (step == 1) {
                        step = 0
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (step == 0) "Quét toa thuốc OCR" else "Xác nhận thông tin thuốc",
                    color = Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (step == 0) {
                // Step 0: Scan image and get raw text
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Image Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Toa thuốc đã chọn",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            selectedImageUri = null
                                            rawText = ""
                                        }
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Xóa ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Chưa có ảnh nào được chọn",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vui lòng chọn ảnh từ thư viện hoặc chụp ảnh",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val cameraUri = createCameraImageUri()
                                pendingCameraUri = cameraUri
                                errorMessage = null
                                cameraLauncher.launch(cameraUri)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chụp ảnh", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEF2FF),
                                contentColor = Color(0xFF4F46E5)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thư viện", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Raw text field for OCR text review before AI parsing.
                    if (selectedImageUri != null || rawText.isNotBlank()) {
                        Text(
                            text = "Kết quả nhận dạng văn bản (OCR)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Nhập hoặc chỉnh sửa văn bản toa thuốc để gửi AI...") }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Parse buttons
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                medicineViewModel.parseOcrText(
                                    rawText = rawText,
                                    onSuccess = { results ->
                                        isLoading = false
                                        if (results.isEmpty()) {
                                            errorMessage = "AI trả về kết quả rỗng. Vui lòng kiểm tra lại văn bản hoặc cấu hình API Key."
                                        } else {
                                            parsedMedications = results
                                            step = 1
                                        }
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = rawText.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gửi AI Phân Tích Đơn Thuốc", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Display Error & honest bypass
                        AnimatedVisibility(visible = errorMessage != null) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lỗi phân tích OCR", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFF991B1B),
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Honest bypass for testing when API is not configured
                                Button(
                                    onClick = {
                                        parsedMedications = listOf(
                                            ParsedMedicationDto(medicineName = "Paracetamol 500mg", totalQuantity = 10, unit = "viên", dosage = "Uống 1 viên mỗi lần", notes = "ngày 2 lần sau ăn"),
                                            ParsedMedicationDto(medicineName = "Amoxicillin 500mg", totalQuantity = 21, unit = "viên", dosage = "Uống 1 viên mỗi lần", notes = "ngày 3 lần"),
                                            ParsedMedicationDto(medicineName = "Decolgen", totalQuantity = 4, unit = "viên", dosage = "Uống khi nhức đầu", notes = "")
                                        )
                                        errorMessage = null
                                        step = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Thử ngay với dữ liệu giả lập", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            } else {
                // Step 1: Confirmation & Edit screen
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                "Vui lòng kiểm tra và sửa đổi thông tin chính xác trước khi lưu vào tủ thuốc của bạn.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        itemsIndexed(parsedMedications) { index, med ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Thuốc #${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF4F46E5)
                                        )
                                        IconButton(onClick = {
                                            parsedMedications = parsedMedications.filterIndexed { i, _ -> i != index }
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Xóa dòng",
                                                tint = Color(0xFFEF4444)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Medicine Name
                                    OutlinedTextField(
                                        value = med.medicineName,
                                        onValueChange = { newName ->
                                            parsedMedications = parsedMedications.mapIndexed { i, m ->
                                                if (i == index) m.copy(medicineName = newName) else m
                                            }
                                        },
                                        label = { Text("Tên thuốc") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Quantity
                                        OutlinedTextField(
                                            value = (med.totalQuantity ?: "").toString(),
                                            onValueChange = { qtyStr ->
                                                val newQty = qtyStr.toIntOrNull()
                                                parsedMedications = parsedMedications.mapIndexed { i, m ->
                                                    if (i == index) m.copy(totalQuantity = newQty) else m
                                                }
                                            },
                                            label = { Text("Số lượng") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        // Unit
                                        OutlinedTextField(
                                            value = med.unit ?: "",
                                            onValueChange = { newUnit ->
                                                parsedMedications = parsedMedications.mapIndexed { i, m ->
                                                    if (i == index) m.copy(unit = newUnit) else m
                                                }
                                            },
                                            label = { Text("Đơn vị") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Dosage/Notes
                                    OutlinedTextField(
                                        value = med.dosage ?: "",
                                        onValueChange = { newDosage ->
                                            parsedMedications = parsedMedications.mapIndexed { i, m ->
                                                if (i == index) m.copy(dosage = newDosage) else m
                                            }
                                        },
                                        label = { Text("Liều dùng / Cách dùng") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    parsedMedications = parsedMedications + ParsedMedicationDto(
                                        medicineName = "",
                                        totalQuantity = 1,
                                        unit = "viên",
                                        dosage = ""
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF4F46E5)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = borderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm thuốc thủ công", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Bottom action bar
                    Card(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (parsedMedications.isEmpty()) {
                                        Toast.makeText(context, "Danh sách thuốc trống!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    // Verify all meds have a name
                                    if (parsedMedications.any { it.medicineName.isBlank() }) {
                                        Toast.makeText(context, "Tên thuốc không được để trống!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isSaving = true
                                    var savedCount = 0

                                    fun saveNext(idx: Int) {
                                        if (idx >= parsedMedications.size) {
                                            isSaving = false
                                            Toast.makeText(context, "Đã lưu thành công $savedCount thuốc vào tủ thuốc!", Toast.LENGTH_LONG).show()
                                            onBack()
                                            return
                                        }
                                        val med = parsedMedications[idx]
                                        medicineViewModel.addMedicine(
                                            name = med.medicineName,
                                            quantity = med.totalQuantity ?: 1,
                                            unit = med.unit ?: "viên",
                                            expiryDate = null,
                                            onSuccess = {
                                                savedCount++
                                                saveNext(idx + 1)
                                            },
                                            onError = { err ->
                                                isSaving = false
                                                Toast.makeText(context, "Lỗi khi lưu ${med.medicineName}: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }

                                    saveNext(0)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lưu Toàn Bộ Vào Tủ Thuốc", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
