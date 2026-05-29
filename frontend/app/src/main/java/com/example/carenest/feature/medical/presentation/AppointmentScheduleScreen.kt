package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel

class AppointmentScheduleViewModel : ViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScheduleScreen(
    viewModel: AppointmentScheduleViewModel? = null,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch hẹn") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tính năng đang phát triển", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lịch hẹn khám bệnh sẽ sớm ra mắt.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}