package com.example.carenest.feature.ekyc.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.BackgroundLight
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.ekyc.domain.model.VerificationStatus

@Composable
fun DoctorVerificationScreen(
    viewModel: EkycViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onCertificateSelected(uri)
    }
    val hasInitialLoadError =
        !state.isLoading &&
            !state.isSubmitting &&
            state.verification == null &&
            state.error != null &&
            state.certificationNumber.isBlank() &&
            state.specialty.isBlank() &&
            state.hospitalName.isBlank() &&
            state.selectedCertificateUri == null &&
            state.uploadedDocumentUrl.isNullOrBlank()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        containerColor = BackgroundLight,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        color = PrimaryBlue,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                hasInitialLoadError -> {
                    VerificationLoadErrorState(
                        message = state.error.orEmpty(),
                        onRetry = viewModel::loadStatus,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Header(onBack = onBack)
                        StatusBanner(state)
                        VerificationForm(
                            state = state,
                            onCertificationNumberChange = viewModel::onCertificationNumberChange,
                            onSpecialtyChange = viewModel::onSpecialtyChange,
                            onHospitalNameChange = viewModel::onHospitalNameChange,
                            onChooseImage = { imagePicker.launch("image/*") },
                            onSubmit = { viewModel.submit(context) },
                            onRetryStatus = viewModel::loadStatus,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationLoadErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Report,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Không thể tải trạng thái hồ sơ bác sĩ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Text("Thử lại", modifier = Modifier.padding(start = 8.dp), color = Color.White)
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF0F172A),
                )
            }
            Text(
                text = "Xác thực bác sĩ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
            )
        }
        Text(
            text = "Nộp chứng chỉ hành nghề để mở quyền đăng bài chuyên môn và phòng tư vấn riêng trong cộng đồng CareNest.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun StatusBanner(state: EkycUiState) {
    val status = state.status
    val banner = when (status) {
        VerificationStatus.PENDING -> BannerSpec(
            title = "Hồ sơ của bạn đang chờ admin duyệt",
            body = "Vui lòng chờ 24 đến 48 giờ. Trong thời gian này bạn chưa thể chỉnh sửa hồ sơ.",
            iconTint = Color(0xFFD97706),
            background = Color(0xFFFFFBEB),
            icon = Icons.Default.HourglassTop,
        )

        VerificationStatus.APPROVED -> BannerSpec(
            title = "Bạn đã được xác thực bác sĩ",
            body = "Tài khoản đã được cấp quyền bác sĩ. Bạn có thể tham gia tư vấn và đăng bài chuyên môn.",
            iconTint = Color(0xFF16A34A),
            background = Color(0xFFF0FDF4),
            icon = Icons.Default.CheckCircle,
        )

        VerificationStatus.REJECTED -> BannerSpec(
            title = "Hồ sơ bị từ chối",
            body = state.verification?.rejectionReason ?: "Vui lòng kiểm tra lại thông tin và gửi lại hồ sơ.",
            iconTint = Color(0xFFDC2626),
            background = Color(0xFFFEF2F2),
            icon = Icons.Default.Report,
        )

        null -> BannerSpec(
            title = "Chưa có hồ sơ xác thực",
            body = "Hãy điền đầy đủ thông tin và tải ảnh chứng chỉ để gửi hồ sơ cho admin duyệt.",
            iconTint = PrimaryBlue,
            background = Color.White,
            icon = Icons.Default.VerifiedUser,
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = banner.background),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(banner.icon, contentDescription = null, tint = banner.iconTint, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(banner.title, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text(
                    text = banner.body,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun VerificationForm(
    state: EkycUiState,
    onCertificationNumberChange: (String) -> Unit,
    onSpecialtyChange: (String) -> Unit,
    onHospitalNameChange: (String) -> Unit,
    onChooseImage: () -> Unit,
    onSubmit: () -> Unit,
    onRetryStatus: () -> Unit,
) {
    val enabled = !state.isLocked

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.certificationNumber,
                onValueChange = onCertificationNumberChange,
                enabled = enabled,
                singleLine = true,
                label = { Text("Số chứng chỉ hành nghề") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.specialty,
                onValueChange = onSpecialtyChange,
                enabled = enabled,
                singleLine = true,
                label = { Text("Chuyên khoa") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.hospitalName,
                onValueChange = onHospitalNameChange,
                enabled = enabled,
                singleLine = true,
                label = { Text("Nơi công tác") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(
                onClick = onChooseImage,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = when {
                        state.selectedCertificateUri != null -> "Đã chọn ảnh chứng chỉ mới"
                        !state.uploadedDocumentUrl.isNullOrBlank() -> "Đang dùng ảnh chứng chỉ đã nộp"
                        else -> "Chọn ảnh chứng chỉ"
                    },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            state.message?.let {
                Text(it, color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            state.error?.let {
                Text(it, color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            if (!state.isLoading && state.verification != null && state.error != null) {
                OutlinedButton(onClick = onRetryStatus, modifier = Modifier.fillMaxWidth()) {
                    Text("Tải lại trạng thái hồ sơ", color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Đang gửi hồ sơ...", modifier = Modifier.padding(start = 8.dp))
                } else {
                    Text(
                        when (state.status) {
                            VerificationStatus.REJECTED -> "Gửi lại hồ sơ xác thực"
                            else -> "Gửi hồ sơ xác thực"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (state.status == VerificationStatus.PENDING) {
                Text(
                    text = "Form đã được khóa vì hồ sơ đang chờ duyệt.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private data class BannerSpec(
    val title: String,
    val body: String,
    val iconTint: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
