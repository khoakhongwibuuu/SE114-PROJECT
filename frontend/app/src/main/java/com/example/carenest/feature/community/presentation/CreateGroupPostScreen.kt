package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.CareNestApplication
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateGroupPostScreen(
    groupId: Long,
    onBack: () -> Unit = {},
    onPostSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val communityRepository = application.communityRepository
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val titleError = title.isBlank() && title.isNotEmpty()
    val canSubmit = title.isNotBlank() && content.isNotBlank() && !isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
            }
            Text(
                text = "Đăng bài viết mới",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Scrollable form body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Moderation notice banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp).padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Bài viết sẽ được gửi chờ duyệt trước khi hiển thị công khai.",
                    fontSize = 13.sp,
                    color = PrimaryBlue,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title field — required
            Text(
                text = "Tiêu đề *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ví dụ: Mẹo chăm sóc trẻ bị sốt tại nhà", color = Color(0xFF94A3B8)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                isError = titleError
            )
            if (titleError) {
                Text("Tiêu đề không được bỏ trống", fontSize = 11.sp, color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content field — required
            Text(
                text = "Nội dung *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Chia sẻ kinh nghiệm, câu hỏi hoặc thông tin hữu ích...", color = Color(0xFF94A3B8)) },
                maxLines = 12,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tags field — optional
            Text(
                text = "Chủ đề / thẻ (tuỳ chọn)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhi khoa, sốt, dinh dưỡng (phân cách bằng dấu phẩy)", color = Color(0xFF94A3B8)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Error
            error?.let { message ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = message, color = Color(0xFFEF4444), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit button
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedContent = content.trim()
                    val trimmedTags = tags.trim().takeIf { it.isNotBlank() }
                    if (trimmedTitle.isNotBlank() && trimmedContent.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                withContext(Dispatchers.IO) {
                                    communityRepository.sendPost(
                                        groupId = groupId,
                                        title = trimmedTitle,
                                        content = trimmedContent,
                                        tags = trimmedTags
                                    )
                                }
                                onPostSuccess()
                            } catch (e: Exception) {
                                error = e.userMessage("Không thể đăng bài viết")
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        error = "Vui lòng điền đầy đủ tiêu đề và nội dung"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                } else {
                    Text("Gửi bài chờ duyệt", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
