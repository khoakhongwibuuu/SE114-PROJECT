package com.example.carenest.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chính sách bảo mật",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Policy Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Cam kết bảo mật của CareNest",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Cập nhật lần cuối: 30/05/2026",
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // Section 1: Thu thập thông tin
            PolicySectionCard(
                title = "1. Thu thập thông tin cá nhân",
                content = "CareNest chỉ thu thập các thông tin cá nhân cần thiết để cung cấp và cải thiện dịch vụ của chúng tôi:\n" +
                        "• Thông tin đăng ký tài khoản (họ tên, email, số điện thoại, ngày sinh, giới tính).\n" +
                        "• Thông tin sức khỏe cá nhân và gia đình (nhóm máu, chiều cao, cân nặng, tiền sử dị ứng, hồ sơ khám bệnh, lịch trình thuốc uống) do bạn tự nguyện chia sẻ.\n" +
                        "• Thông tin ảnh đính kèm (ảnh đại diện, ảnh chụp giấy tờ xác minh chứng chỉ bác sĩ)."
            )

            // Section 2: Sử dụng thông tin
            PolicySectionCard(
                title = "2. Sử dụng thông tin của bạn",
                content = "Chúng tôi sử dụng thông tin thu thập được cho các mục đích:\n" +
                        "• Hỗ trợ bạn lên lịch hẹn khám sức khỏe, quản lý lịch nhắc thuốc và theo dõi chỉ số BMI.\n" +
                        "• Xác minh danh tính của Bác sĩ tham gia tư vấn để bảo đảm tính chính xác và an toàn của hệ thống.\n" +
                        "• Phát hiện, ngăn chặn và xử lý kịp thời các sự cố bảo mật hoặc các hành vi gian lận.\n" +
                        "• Gửi các thông báo quan trọng liên quan đến tài khoản và sức khỏe của bạn."
            )

            // Section 3: Bảo mật thông tin
            PolicySectionCard(
                title = "3. Bảo mật và Lưu trữ dữ liệu",
                content = "CareNest áp dụng các biện pháp bảo vệ dữ liệu tiên tiến để giảm thiểu rủi ro bị mất mát, lạm dụng hoặc truy cập trái phép:\n" +
                        "• Mã hóa dữ liệu truyền tải sử dụng giao thức HTTPS bảo mật.\n" +
                        "• Lưu trữ mã xác thực (Token) và thông tin phiên làm việc một cách an toàn trên thiết bị thông qua hệ thống lưu trữ bảo mật (Encrypted Shared Preferences/DataStore).\n" +
                        "• Dữ liệu y tế của bạn được coi là thông tin mật tuyệt đối và không chia sẻ cho bên thứ ba khi chưa có sự đồng ý rõ ràng."
            )

            // Section 4: Quyền lợi người dùng
            PolicySectionCard(
                title = "4. Quyền của bạn đối với dữ liệu",
                content = "Bạn có toàn quyền kiểm soát thông tin của mình trên CareNest:\n" +
                        "• Xem, chỉnh sửa hoặc cập nhật hồ sơ y tế của bạn bất cứ lúc nào.\n" +
                        "• Rút lại sự đồng ý hoặc gửi yêu cầu xóa tài khoản cùng toàn bộ thông tin đi kèm ra khỏi hệ thống.\n" +
                        "• Báo cáo sự cố hoặc khiếu nại thông qua Trung tâm hỗ trợ của CareNest."
            )

            // Section 5: Điều khoản sử dụng
            PolicySectionCard(
                title = "5. Điều khoản sử dụng & Trách nhiệm",
                content = "Bằng cách sử dụng CareNest, bạn đồng ý:\n" +
                        "• Cung cấp thông tin chính xác, trung thực khi cập nhật hồ sơ sức khỏe và xác minh chứng chỉ bác sĩ.\n" +
                        "• Tự chịu trách nhiệm bảo mật thông tin đăng nhập tài khoản của mình.\n" +
                        "• CareNest là công cụ hỗ trợ theo dõi sức khỏe và lịch trình y tế, không thể thay thế hoàn toàn cho chẩn đoán hoặc điều trị trực tiếp từ các bác sĩ chuyên khoa có chuyên môn cao."
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mọi thắc mắc về chính sách bảo mật, xin liên hệ email: support@carenest.vn",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun PolicySectionCard(
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )
        }
    }
}
