package com.example.carenest.feature.auth.presentation

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    userName: String,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val setupState by viewModel.setupState.collectAsState()

    var selectedDate by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("MALE") }
    
    val genderOptions = listOf(
        "MALE" to "Nam",
        "FEMALE" to "Nữ",
        "OTHER" to "Khác"
    )

    // Handle success
    LaunchedEffect(setupState) {
        if (setupState is ProfileSetupState.Success) {
            onSetupComplete()
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDate = formattedDate
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thiết lập hồ sơ", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chào mừng, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Để cá nhân hóa trải nghiệm và sử dụng các tính năng theo dõi sức khỏe, vui lòng cung cấp thêm thông tin:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Date of birth
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Ngày sinh") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = "Chọn ngày sinh"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gender
            Text(
                text = "Giới tính",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                genderOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedGender == value,
                        onClick = { selectedGender = value },
                        label = { Text(label) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (setupState is ProfileSetupState.Error) {
                Text(
                    text = (setupState as ProfileSetupState.Error).error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = {
                    viewModel.completeProfile(
                        fullName = userName,
                        dateOfBirth = selectedDate,
                        gender = selectedGender
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = selectedDate.isNotBlank() && setupState !is ProfileSetupState.Loading
            ) {
                if (setupState is ProfileSetupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Hoàn tất thiết lập", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
