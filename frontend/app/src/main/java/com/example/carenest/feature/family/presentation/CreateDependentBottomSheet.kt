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
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.SurfaceHigh
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDependentBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    var expandedRelation by remember { mutableStateOf(false) }
    var relation by remember { mutableStateOf("Con cái") }

    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dob = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
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
                onValueChange = { name = it },
                label = { Text("Họ và tên") },
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextPrimary,
                        disabledBorderColor = Outline,
                        disabledLabelColor = TextSecondary
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
                onClick = { onSubmit(name, dob, gender, relation) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(AppRadius.lg),
                enabled = name.isNotBlank() && dob.isNotBlank()
            ) {
                Text("Lưu hồ sơ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
