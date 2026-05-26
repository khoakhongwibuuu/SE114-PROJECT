package com.example.carenest.ui.medical

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(onBack: () -> Unit) {
    var medicineName by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var syncDatabase by remember { mutableStateOf(true) }
    var checkInteractions by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Thêm thuốc mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lưu vào tủ thuốc", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Intro
            Text(
                "THÔNG TIN THUỐC",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Medicine Name
            Text(
                "Tên thuốc",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                placeholder = { Text("Nhập tên thuốc (vd: Paracetamol...)", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x66E2E8F0),
                    focusedContainerColor = Color(0x66E2E8F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Expiration Date
            Text(
                "Hạn sử dụng",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x66E2E8F0))
                    .clickable { /* Show Date Picker */ }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                if (expirationDate.isEmpty()) {
                    Text("Chọn ngày hết hạn", fontSize = 14.sp, color = Color(0xFF94A3B8))
                } else {
                    Text(expirationDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Database compatibility
            Text(
                "THÔNG TIN TƯƠNG THÍCH DATABASE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue,
                letterSpacing = 1.sp
            )
            Text(
                "Đồng bộ thông tin thuốc với cơ sở dữ liệu y tế trực tuyến để nhận cảnh báo tương tác thuốc và thông tin chính xác nhất.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Option 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Đồng bộ dữ liệu quốc gia", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Tự động điền thành phần", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = syncDatabase,
                            onCheckedChange = { syncDatabase = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Kiểm tra tương tác thuốc", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Cảnh báo khi dùng chung", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = checkInteractions,
                            onCheckedChange = { checkInteractions = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
