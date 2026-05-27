package com.example.carenest.feature.auth.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.R

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit = onNavigateToLogin,
    onNavigateToPolicy: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(false) }
    var googleDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToLogin()
            viewModel.resetState()
        }
    }

    if (googleDialog) {
        AlertDialog(
            onDismissRequest = { googleDialog = false },
            confirmButton = {
                TextButton(onClick = { googleDialog = false }) {
                    Text("Đóng")
                }
            },
            title = { Text("Sắp ra mắt") },
            text = { Text("Đăng ký bằng Google sẽ được bổ sung trong phiên bản tiếp theo.") }
        )
    }

    Scaffold(containerColor = LegacyBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LegacyBackground)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = LegacyOnSurface)
            }

            Image(
                painter = painterResource(R.drawable.carenest_logo_full),
                contentDescription = "CareNest",
                modifier = Modifier.size(132.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tạo tài khoản mới",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LegacyOnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bắt đầu hành trình chăm sóc sức khỏe gia đình",
                fontSize = 14.sp,
                color = LegacyOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                        label = "Họ và tên",
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Nguyễn Văn A",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LegacyOutline) }
                    )
                    AuthField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "email@vi-du.com",
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LegacyOutline) },
                        keyboardType = KeyboardType.Email
                    )
                    AuthField(
                        label = "Số điện thoại",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "0901234567",
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LegacyOutline) },
                        keyboardType = KeyboardType.Phone
                    )
                    AuthField(
                        label = "Mật khẩu",
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Tối thiểu 8 ký tự",
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LegacyOutline) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = LegacyOutline
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LegacyPrimary,
                                uncheckedColor = LegacyOutline,
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Tôi đồng ý với ")
                                pushStringAnnotation(tag = "terms", annotation = "terms")
                                withStyle(SpanStyle(color = LegacyPrimary, fontWeight = FontWeight.SemiBold)) {
                                    append("Điều khoản dịch vụ")
                                }
                                pop()
                                append(" và ")
                                pushStringAnnotation(tag = "policy", annotation = "policy")
                                withStyle(SpanStyle(color = LegacyPrimary, fontWeight = FontWeight.SemiBold)) {
                                    append("Chính sách bảo mật")
                                }
                                pop()
                            },
                            fontSize = 13.sp,
                            color = LegacyOnSurfaceVariant,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 12.dp)
                                .clickable(onClick = onNavigateToPolicy)
                        )
                    }

                    PrimaryPillButton(
                        text = if (isLoading) "Đang đăng ký..." else "Đăng ký",
                        loading = isLoading,
                        enabled = agreed && !isLoading,
                        onClick = { viewModel.register(email, password, fullName, phoneNumber) },
                        icon = {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                        }
                    )

                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).error,
                            color = LegacyError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    DividerRow()

                    OutlinePillButton(
                        text = "Tiếp tục với Google",
                        onClick = { googleDialog = true },
                        leading = {
                            Text(
                                text = "G",
                                color = Color(0xFF4285F4),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản?", fontSize = 14.sp, color = LegacyOnSurfaceVariant)
                Text(
                    text = " Đăng nhập",
                    fontSize = 14.sp,
                    color = LegacyPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
