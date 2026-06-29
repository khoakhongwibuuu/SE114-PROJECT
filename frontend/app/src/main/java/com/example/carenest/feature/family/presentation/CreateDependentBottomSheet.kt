package com.example.carenest.feature.family.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.SurfaceHigh
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDependentBottomSheet(
    isBusy: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    var expandedRelation by remember { mutableStateOf(false) }
    var relation by remember { mutableStateOf("Con cái") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dob = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                dobError = null
            },
            2020, 0, 1
        )
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Thêm người phụ thuộc", style = CareNestTextStyles.titleLg, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tạo hồ sơ cho trẻ em hoặc người lớn tuổi không có tài khoản riêng.", style = CareNestTextStyles.bodyMd, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = null
                },
                label = { Text("Họ và tên") },
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.lg)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày sinh") },
                    isError = dobError != null,
                    supportingText = { dobError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextPrimary,
                        disabledBorderColor = if (dobError != null) androidx.compose.material3.MaterialTheme.colorScheme.error else Outline,
                        disabledLabelColor = if (dobError != null) androidx.compose.material3.MaterialTheme.colorScheme.error else TextSecondary,
                        disabledSupportingTextColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(AppRadius.lg)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text("Giới tính", style = CareNestTextStyles.labelMd, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Nam", "Nữ", "Khác").forEach { g ->
                    val isSelected = gender == g
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadius.full))
                            .background(if (isSelected) PrimaryBlue else SurfaceHigh)
                            .clickable { gender = g }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(g, color = if (isSelected) Color.White else TextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Mối quan hệ", style = CareNestTextStyles.labelMd, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                Box(modifier = Modifier.fillMaxWidth().clickable { expandedRelation = true }) {
                    OutlinedTextField(
                        value = relation,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = Outline
                        ),
                        shape = RoundedCornerShape(AppRadius.lg)
                    )
                }
                DropdownMenu(
                    expanded = expandedRelation,
                    onDismissRequest = { expandedRelation = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf("Con cái", "Bố/Mẹ", "Ông/Bà", "Khác").forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                relation = r
                                expandedRelation = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    var isValid = true
                    
                    if (name.isBlank()) {
                        nameError = "Họ và tên không được để trống"
                        isValid = false
                    } else if (name.length > 50) {
                        nameError = "Họ và tên không được vượt quá 50 ký tự"
                        isValid = false
                    } else if (!name.matches(Regex("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$"))) {
                        nameError = "Họ và tên không được chứa số hoặc ký tự đặc biệt"
                        isValid = false
                    }

                    if (dob.isBlank()) {
                        dobError = "Ngày sinh không được để trống"
                        isValid = false
                    } else {
                        try {
                            val parsedDob = LocalDate.parse(dob, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            if (parsedDob.isAfter(LocalDate.now())) {
                                dobError = "Ngày sinh không được ở tương lai"
                                isValid = false
                            }
                        } catch (e: Exception) {
                            dobError = "Ngày sinh không hợp lệ"
                            isValid = false
                        }
                    }

                    if (isValid) {
                        onSubmit(name, dob, gender, relation)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(AppRadius.lg),
                enabled = !isBusy
            ) {
                if (isBusy) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu hồ sơ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
