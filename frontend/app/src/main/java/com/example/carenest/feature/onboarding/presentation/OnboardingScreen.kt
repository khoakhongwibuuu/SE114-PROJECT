package com.example.carenest.feature.onboarding.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.R
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import kotlinx.coroutines.launch

// Legacy RN onboarding colors from colors.ts
private val OnboardingPrimary = Color(0xFF00629D)
private val OnboardingOutline = Color(0xFF707882)
private val OnboardingOutlineVariant = Color(0xFFBFC7D3)

private data class OnboardingSlide(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int
)

private val onboardingSlides = listOf(
    OnboardingSlide(
        id = "ob-1",
        title = "Quản lý sức khỏe\ngia đình toàn diện",
        description = "Lưu trữ hồ sơ y tế, theo dõi sức khỏe và chăm sóc từng thành viên trong gia đình.",
        imageRes = R.drawable.carenest_logo_full
    ),
    OnboardingSlide(
        id = "ob-2",
        title = "Nhắc nhở uống thuốc\nthông minh",
        description = "Không bao giờ quên uống thuốc với hệ thống nhắc nhở theo giờ, tên thuốc và liều dùng.",
        imageRes = R.drawable.carenest_logo_house
    ),
    OnboardingSlide(
        id = "ob-3",
        title = "Trợ lý AI hỗ trợ\nchăm sóc sức khỏe",
        description = "Hỏi AI về sức khỏe gia đình bằng giọng nói hoặc văn bản, nhanh chóng và dễ dàng.",
        imageRes = R.drawable.carenest_logo_house
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    val scope = rememberCoroutineScope()
    val isLastSlide = pagerState.currentPage == onboardingSlides.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 210.dp, y = (-90).dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE).copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDE9FE).copy(alpha = 0.2f))
        )

        TextButton(
            onClick = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 24.dp)
        ) {
            Text("Bỏ qua", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnboardingOutline)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingSlidePage(onboardingSlides[page])
            }

            DotsIndicator(currentIndex = pagerState.currentPage, count = onboardingSlides.size)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLastSlide) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OnboardingPrimary),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = if (isLastSlide) "Bắt đầu" else "Tiếp theo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingSlidePage(slide: OnboardingSlide) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE0F2FE))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(slide.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = slide.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = slide.description,
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun DotsIndicator(currentIndex: Int, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (index == currentIndex) 24.dp else 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index == currentIndex) OnboardingPrimary else OnboardingOutlineVariant)
            )
        }
    }
}
