package com.example.carenest.feature.doctor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import androidx.compose.ui.platform.LocalContext
import com.example.carenest.core.presentation.navigation.isValidHealthProfileId
import kotlinx.coroutines.launch
import com.example.carenest.feature.booking.presentation.BookingRequestSheet
import com.example.carenest.feature.booking.domain.model.DuplicateActiveConsultationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    doctorId: Long,
    onNavigateToConsultationRoom: (Long) -> Unit = {},
    onNavigateToPatientBookingCenter: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val repository = application.doctorRepository

    val viewModel: DoctorProfileViewModel = viewModel(
        key = "DoctorProfile_$doctorId",
        factory = DoctorProfileViewModel.Factory(doctorId, repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    val bookingRepository = application.bookingRepository
    val activeProfileId by application.secureSessionManager.activeProfileIdFlow.collectAsState()
    var showBookingSheet by remember { mutableStateOf(false) }
    var bookingLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var duplicateException by remember { mutableStateOf<DuplicateActiveConsultationException?>(null) }
    val canCreateBooking = uiState.profile != null && !bookingLoading && activeProfileId.isValidHealthProfileId()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ bác sĩ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!activeProfileId.isValidHealthProfileId()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bạn chưa có hồ sơ y tế cá nhân. Vui lòng cập nhật hồ sơ để sử dụng tính năng này.",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { showBookingSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = canCreateBooking,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Đặt lịch khám",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Thử lại")
                        }
                    }
                }
                uiState.profile != null -> {
                    val profile = uiState.profile ?: return@Box
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            // Header Profile
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFE0F2FE),
                                                Color(0xFFF8FAFC)
                                            )
                                        )
                                    )
                                    .padding(vertical = 32.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (profile.avatarUrl != null) {
                                        AsyncImage(
                                            model = profile.avatarUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDBEAFE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = profile.fullName.take(1).uppercase(),
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E3A8A)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.fullName,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        if (profile.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF0EA5E9),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    if (profile.specialty != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = profile.specialty,
                                            fontSize = 16.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Info Cards
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Thông tin chuyên môn",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (profile.hospitalName != null) {
                                            Text(
                                                text = "Nơi công tác",
                                                color = Color(0xFF64748B),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = profile.hospitalName,
                                                color = Color(0xFF0F172A),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                            )
                                        }
                                        
                                        if (profile.certificationNumber != null) {
                                            Text(
                                                text = "Chứng chỉ hành nghề",
                                                color = Color(0xFF64748B),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = profile.certificationNumber,
                                                color = Color(0xFF0F172A),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Giới thiệu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Text(
                                        text = "Bác sĩ chưa cập nhật giới thiệu",
                                        color = Color(0xFF64748B),
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBookingSheet && uiState.profile != null) {
            BookingRequestSheet(
                doctorId = doctorId,
                onDismissRequest = { showBookingSheet = false },
                onSubmitRequest = { _, requestType, note, preferredTimeNote ->
                    scope.launch {
                        bookingLoading = true
                        try {
                            val healthProfileId = application.secureSessionManager.getActiveProfileId()
                                ?: application.secureSessionManager.getProfileId()
                            if (healthProfileId == null) {
                                bookingLoading = false
                                showBookingSheet = false
                                snackbarHostState.showSnackbar("Vui lòng chọn hồ sơ sức khỏe trước khi đặt lịch")
                                return@launch
                            }
                            bookingRepository.createBooking(
                                doctorId = doctorId,
                                healthProfileId = healthProfileId,
                                type = requestType,
                                preferredSchedule = preferredTimeNote,
                                patientNote = note
                            )
                            bookingLoading = false
                            showBookingSheet = false
                            snackbarHostState.showSnackbar("Đã gửi yêu cầu tư vấn thành công")
                            onNavigateToPatientBookingCenter()
                        } catch (e: Exception) {
                            bookingLoading = false
                            showBookingSheet = false
                            if (e is DuplicateActiveConsultationException) {
                                duplicateException = e
                            } else {
                                snackbarHostState.showSnackbar("Lỗi: ${e.message}")
                            }
                        }
                    }
                },
                isLoading = bookingLoading
            )
        }
    }

    duplicateException?.let { ex ->
        val isPending = ex.status == "PENDING"
        AlertDialog(
            onDismissRequest = { duplicateException = null },
            title = {
                Text(
                    text = "Lưu ý",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Text(
                    text = ex.message ?: "Bạn đang có phiên làm việc với bác sĩ này.",
                    fontSize = 16.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        duplicateException = null
                        if (isPending) {
                            onNavigateToPatientBookingCenter()
                        } else {
                            onNavigateToConsultationRoom(ex.existingBookingId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isPending) "Lịch sử đặt khám" else "Đến phòng tư vấn")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { duplicateException = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đóng")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
