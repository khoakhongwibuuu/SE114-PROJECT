package com.example.carenest.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.theme.PrimaryLight
import com.example.carenest.theme.TextFieldBackground
import com.example.carenest.theme.TextPrimary
import com.example.carenest.theme.TextSecondary
import com.example.carenest.viewmodel.AuthState
import com.example.carenest.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToLogin()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.carenest.R.drawable.carenest_logo_full),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp).padding(bottom = 24.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            // Text CARENEST removed as it is in the logo

            // Titles
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tạo tài khoản mới",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bắt đầu hành trình chăm sóc sức khỏe gia đình",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    CustomTextField(
                        label = "Họ và tên",
                        value = fullName,
                        onValueChange = { fullName = it },
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        icon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        label = "Số điện thoại",
                        value = phone,
                        onValueChange = { phone = it },
                        icon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary) },
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        label = "Mật khẩu",
                        value = password,
                        onValueChange = { password = it },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms Checkbox
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            modifier = Modifier.padding(top = 0.dp)
                        )
                        Text(
                            text = "Tôi đồng ý với Điều khoản dịch vụ và Chính sách bảo mật",
                            style = MaterialTheme.typography.bodySmall.copy(color = PrimaryBlue),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = { viewModel.register(email, password, fullName, phone) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = agreedToTerms && (authState !is AuthState.Loading),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (authState as AuthState.Error).error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row {
                Text("Đã có tài khoản? ", color = TextSecondary)
                Text(
                    text = "Đăng nhập",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: @Composable () -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = icon,
            trailingIcon = if (isPassword) {
                {
                    Text(
                        text = if (passwordVisible) "Ẩn" else "Hiện",
                        modifier = Modifier.clickable { onPasswordVisibilityChange(!passwordVisible) },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldBackground,
                unfocusedContainerColor = TextFieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = PrimaryBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
