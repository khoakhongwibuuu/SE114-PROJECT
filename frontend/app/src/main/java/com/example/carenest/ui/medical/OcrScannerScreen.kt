package com.example.carenest.ui.medical

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.theme.TextPrimary
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

data class EditableMedicine(
    var name: String = "",
    var dosage: String = "",
    var frequency: String = "1",
    var duration: String = "7 ngày",
    var note: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var ocrState by remember { mutableStateOf("idle") } // idle, scanning, result
    var clinicName by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var prescriptionDate by remember { mutableStateOf("") }
    var medicines by remember { mutableStateOf(listOf<EditableMedicine>()) }
    var isConfirming by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun processImage(image: InputImage) {
        ocrState = "scanning"
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Basic parsing for demonstration
                val text = visionText.text
                clinicName = "Phòng khám Đa khoa CareNest" // Mock parsed data
                doctorName = "BS. Nguyễn Văn A"
                prescriptionDate = "25/05/2026"
                
                val lines = text.split("\n")
                val foundMeds = mutableListOf<EditableMedicine>()
                // Giả lập bóc tách thuốc từ raw text (ML Kit chỉ trả raw text)
                foundMeds.add(EditableMedicine(name = "Panadol Extra", dosage = "1 viên", frequency = "2", note = "Uống sau ăn"))
                if (lines.size > 2) {
                    foundMeds.add(EditableMedicine(name = "Vitamin C", dosage = "1 viên", frequency = "1"))
                }
                
                medicines = foundMeds
                ocrState = "result"
            }
            .addOnFailureListener { e ->
                ocrState = "idle"
                Toast.makeText(context, "Lỗi nhận diện: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val image = InputImage.fromBitmap(bitmap, 0)
            processImage(image)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                processImage(image)
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Quét toa thuốc OCR", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Go Back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Camera Box Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                when (ocrState) {
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
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Dùng camera hoặc chọn ảnh toa thuốc để AI trích xuất", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (ocrState != "scanning") {
                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chụp trực tiếp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE), contentColor = PrimaryBlue),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (ocrState == "result") "Chọn ảnh khác" else "Chọn từ thư viện", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (ocrState == "result") {
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kiểm tra và chỉnh sửa kết quả OCR trước khi lưu", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = clinicName, onValueChange = { clinicName = it }, label = { Text("Phòng khám") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = doctorName, onValueChange = { doctorName = it }, label = { Text("Bác sĩ") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = prescriptionDate, onValueChange = { prescriptionDate = it }, label = { Text("Ngày kê toa") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        medicines.forEachIndexed { index, medicine ->
                            Divider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Thuốc ${index + 1}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = medicine.name,
                                onValueChange = { newName ->
                                    val newMeds = medicines.toMutableList()
                                    newMeds[index] = newMeds[index].copy(name = newName)
                                    medicines = newMeds
                                },
                                label = { Text("Tên thuốc") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = medicine.dosage,
                                    onValueChange = { newDosage ->
                                        val newMeds = medicines.toMutableList()
                                        newMeds[index] = newMeds[index].copy(dosage = newDosage)
                                        medicines = newMeds
                                    },
                                    label = { Text("Liều dùng") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = medicine.frequency,
                                    onValueChange = { newFreq ->
                                        val newMeds = medicines.toMutableList()
                                        newMeds[index] = newMeds[index].copy(frequency = newFreq)
                                        medicines = newMeds
                                    },
                                    label = { Text("Số lần/ngày") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        isConfirming = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1000)
                            isConfirming = false
                            Toast.makeText(context, "Đã lưu toa thuốc", Toast.LENGTH_SHORT).show()
                            // Go back
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(999.dp),
                    enabled = !isConfirming
                ) {
                    if (isConfirming) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
