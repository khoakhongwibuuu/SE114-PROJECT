package com.example.carenest.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val forgotState by viewModel.forgotPasswordState.collectAsState()
    val isLoading = when (val state = forgotState) {
        is ForgotPasswordState.Loading -> true
        is ForgotPasswordState.OtpSent -> state.isLoading
        else -> false
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetForgotPasswordState()
        }
    }

    Scaffold(containerColor = LegacyBackground) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LegacyBackground)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(paddingValues)
        ) {
            // Decorative blobs
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(LegacyPrimaryContainer.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.BottomStart)
                    .padding(bottom = 100.dp)
                    .offset(x = (-60).dp)
                    .clip(CircleShape)
                    .background(LegacyTertiaryContainer.copy(alpha = 0.10f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = LegacyOnSurface
                    )
                }

                when (val state = forgotState) {
                    is ForgotPasswordState.ResetSuccess -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(LegacyPrimaryContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LegacyPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Text(
                                text = "Đổi mật khẩu thành công!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = LegacyOnSurface
                            )

                            Text(
                                text = "Mật khẩu của bạn đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.",
                                fontSize = 14.sp,
                                color = LegacyOnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                letterSpacing = 0.1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            PrimaryPillButton(
                                text = "Quay lại đăng nhập",
                                loading = false,
                                enabled = true,
                                onClick = onNavigateToLogin
                            )
                        }
                    }

                    is ForgotPasswordState.OtpSent -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(LegacyPrimaryContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LegacyPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "Đặt lại mật khẩu",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LegacyOnSurface
                            )
                            Text(
                                text = "Nhập mã OTP gồm 6 chữ số đã được gửi đến $email và mật khẩu mới của bạn.",
                                fontSize = 14.sp,
                                color = LegacyOnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LegacySurface.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AuthField(
                                    label = "Mã OTP",
                                    value = otp,
                                    onValueChange = { otp = it.take(6) },
                                    placeholder = "Nhập 6 số",
                                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = LegacyOutline, modifier = Modifier.size(20.dp)) },
                                    keyboardType = KeyboardType.Number
                                )

                                AuthField(
                                    label = "Mật khẩu mới",
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    placeholder = "Ít nhất 6 ký tự",
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LegacyOutline, modifier = Modifier.size(20.dp)) },
                                    visualTransformation = PasswordVisualTransformation()
                                )

                                AuthField(
                                    label = "Xác nhận mật khẩu",
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = "Nhập lại mật khẩu",
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LegacyOutline, modifier = Modifier.size(20.dp)) },
                                    visualTransformation = PasswordVisualTransformation()
                                )

                                PrimaryPillButton(
                                    text = if (isLoading) "Đang xử lý..." else "Xác nhận",
                                    loading = isLoading,
                                    enabled = otp.length == 6 && newPassword.length >= 6 && confirmPassword == newPassword && !isLoading,
                                    onClick = { viewModel.resetPassword(email, otp, newPassword, confirmPassword) }
                                )

                                if (state.error != null) {
                                    Text(
                                        text = state.error,
                                        color = LegacyError,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Gửi lại email",
                                color = LegacyPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.forgotPassword(email) }
                            )
                        }
                    }

                    else -> {
                        // Idle / Loading / Error in initial email step
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(LegacyPrimaryContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LegacyPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "Quên mật khẩu?",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LegacyOnSurface
                            )
                            Text(
                                text = "Nhập email của bạn và CareNest sẽ gửi OTP khôi phục qua email.",
                                fontSize = 14.sp,
                                color = LegacyOnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LegacySurface.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AuthField(
                                    label = "Email",
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = "email@vi-du.com",
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LegacyOutline, modifier = Modifier.size(20.dp)) },
                                    keyboardType = KeyboardType.Email
                                )

                                PrimaryPillButton(
                                    text = if (isLoading) "Đang gửi..." else "Gửi mã khôi phục",
                                    loading = isLoading,
                                    enabled = email.isNotBlank() && !isLoading,
                                    onClick = { viewModel.forgotPassword(email) },
                                    icon = {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                )

                                val stepState = state as? ForgotPasswordState.EmailError
                                if (stepState != null) {
                                    Text(
                                        text = stepState.error,
                                        color = LegacyError,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Quay lại đăng nhập",
                                color = LegacyPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onNavigateToLogin)
                            )
                        }
                    }
                }
            }
        }
    }
}
